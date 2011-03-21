package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.intermine.bio.chado.config.ConfigAction;
import org.intermine.bio.chado.config.SetFieldConfigAction;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.TypeUtil;
import org.intermine.xml.full.Attribute;
import org.intermine.xml.full.Item;
import org.intermine.xml.full.Reference;

/**
 * A processor that loads feature referred to by the modENCODE metadata.  This class is designed
 * to be used by ModEncodeMetaDataProcessor and will be called once for each submission that has
 * metadata.
 * @author Kim Rutherford
 */
public class ModEncodeFeatureProcessor extends SequenceProcessor
{
    private static final Logger LOG = Logger.getLogger(ModEncodeFeatureProcessor.class);

    private final String dataSetIdentifier;
    private final String title;
    private final String scoreProtocolItemId;
    private final String dataIdsTableName;

    private Set<String> commonFeatureInterMineTypes = new HashSet<String>();

    private static final String SUBFEATUREID_TEMP_TABLE_NAME = "modmine_subfeatureid_temp";

    // feature type to query from the feature table
    private static final List<String> FEATURES = Arrays.asList(
            "gene", "mRNA", "transcript",
            "CDS", "intron", "exon", "EST",
            "five_prime_untranslated_region",
            "five_prime_UTR", "three_prime_untranslated_region",
            "three_prime_UTR", "origin_of_replication",
            "binding_site", "protein_binding_site", "TF_binding_site",
            "insulator_binding_site",
            "transcript_region", "histone_binding_site", "copy_number_variation",
            "natural_transposable_element", "start_codon", "stop_codon"
            , "cDNA", "cDNA_match", "miRNA"
            , "three_prime_RACE_clone", "three_prime_RST", "three_prime_UST"
            , "polyA_site", "polyA_signal_sequence", "overlapping_EST_set", "exon_region"
            , "SL1_acceptor_site", "SL2_acceptor_site"
            , "transcription_end_site", "TSS", "under-replicated-region"
    );

    // the FB name for the mitochondrial genome
    private static final String MITOCHONDRION = "dmel_mitochondrion_genome";
    // ...
    private static final String CHROMOSOME = "Chromosome";

    // the configuration for this processor, set when getConfig() is called the first time
    private final Map<Integer, MultiKeyMap> config = new HashMap<Integer, MultiKeyMap>();

    private Map<Integer, FeatureData> commonFeaturesMap = new HashMap<Integer, FeatureData>();

    /**
     * Create a new ModEncodeFeatureProcessor.
     * @param chadoDBConverter     the parent converter
     * @param dataSetIdentifier    the item identifier of the DataSet,
     *                             i.e. the submissionItemIdentifier
     * @param dataSourceIdentifier the item identifier of the DataSource,
     *                             i.e. the labItemIdentifier
     * @param dataIdsTableName     name of a temporary table containing the data ids for the sub
     * @param title                the title
     * @param scoreProtocolItemId  score protocol item id
     */

    public ModEncodeFeatureProcessor(ChadoDBConverter chadoDBConverter,
            String dataSetIdentifier, String dataSourceIdentifier,
            String dataIdsTableName, String title, String scoreProtocolItemId) {
        super(chadoDBConverter);
        this.dataSetIdentifier = dataSetIdentifier;
        this.title = title;
        this.scoreProtocolItemId = scoreProtocolItemId;
        this.dataIdsTableName = dataIdsTableName;

        for (String chromosomeType : getChromosomeFeatureTypes()) {
            commonFeatureInterMineTypes.add(
                    TypeUtil.javaiseClassName(fixFeatureType(chromosomeType)));
        }
        commonFeatureInterMineTypes.add("Gene");
        commonFeatureInterMineTypes.add("MRNA");
        commonFeatureInterMineTypes.add("Transcript");
        commonFeatureInterMineTypes.add("CDNA");
        commonFeatureInterMineTypes.add("EST");
        commonFeatureInterMineTypes.add("CDS");
    }

    /**
     * Get a list of the chado/so types of the LocatedSequenceFeatures we wish to load.  The list
     * will not include chromosome-like features.
     * @return the list of features
     */
    @Override
    protected List<String> getFeatures() {
        return FEATURES;
    }

    /**
     * Get a map of features that are expected to be common between submissions.  This map can be
     * used to initialise this processor for a subsequent run.  The feature types added to this map
     * are governed by the addToFeatureMap method in this class.
     * @return a map of chado feature id to FeatureData objects
     */
    protected Map<Integer, FeatureData> getCommonFeaturesMap() {
        return commonFeaturesMap;
    }

    /**
     * Initialise SequenceProcessor with features that have already been processed and put the same
     * features data into a commonFeaturesMap that tracks features (e.g. Chromosomes) that appear
     * in multiple submissions but should only be processed once.
     * @param initialMap map of chado feature id to FeatureData objects
     */
    protected void initialiseCommonFeatures(Map<Integer, FeatureData> initialMap) {
        super.initialiseFeatureMap(initialMap);
        commonFeaturesMap.putAll(initialMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getExtraFeatureConstraint() {
        return "(cvterm.name = 'chromosome' OR cvterm.name = 'chromosome_arm') AND "
            + " feature_id IN ( SELECT featureloc.srcfeature_id "
            + " FROM featureloc, " + SUBFEATUREID_TEMP_TABLE_NAME
            + " WHERE featureloc.feature_id = " + SUBFEATUREID_TEMP_TABLE_NAME + ".feature_id) "
            + " OR feature_id IN ( SELECT feature_id "
            + " FROM " + SUBFEATUREID_TEMP_TABLE_NAME + " ) ";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void extraProcessing(Connection connection, Map<Integer, FeatureData> featureDataMap)
        throws ObjectStoreException, SQLException {
        // TODO: check if there is already a method to get all the match types
        // (and merge the methods)
        // also: add match to query and do everything here

        // process indirect locations via match features and featureloc feature<->match<->feature
        ResultSet matchLocRes = getMatchLocResultSet(connection);
        processLocationTable(connection, matchLocRes);

        List<String> types = getMatchTypes(connection);
        Iterator<String> t = types.iterator();
        while (t.hasNext()) {
            String featType = t.next();
            if (featType.equalsIgnoreCase("cDNA_match")) {
                continue;
            }
            ResultSet matchTypeLocRes = getMatchLocResultSet(connection, featType);
            processLocationTable(connection, matchTypeLocRes);
        }

        // adding scores
        processFeatureScores(connection);

        // do experimental features (expression levels)
        processExpressionLevels(connection);
        
        // adding sources (result files) for binding sites
        processPeaksSources(connection);        
        
    }

    /**
     * Method to set the source for gene
     * for modencode datasources it will add the title
     * @param imObjectId the im object id
     * @param dataSourceName the data source
     * @throws ObjectStoreException the exception
     *
     */
    @Override
    protected void setGeneSource(Integer imObjectId, String dataSourceName)
        throws ObjectStoreException {
        String source = dataSourceName + "-" + title;
        setAttribute(imObjectId, "source", source);
    }

    /**
     * Override method that adds completed features to featureMap.  Also put features that will
     * appear in multiple submissions in a map made available at the end of processing.
     * @param featureId the chado feature id
     * @param fdat feature information
     */
    protected void addToFeatureMap(Integer featureId, FeatureData fdat) {
        super.addToFeatureMap(featureId, fdat);
        // We know chromosomes will be common between submissions so add them here
        if (commonFeatureInterMineTypes.contains(fdat.getInterMineType())
                && !commonFeaturesMap.containsKey(featureId)) {
            commonFeaturesMap.put(featureId, fdat);
        }
    }

    private List<String> getMatchTypes(Connection connection) throws SQLException {
        List<String> types = new ArrayList<String>();
        ResultSet res = getMatchTypesResultSet(connection);
        while (res.next()) {
            String type = res.getString("name");
            types.add(type.substring(0, type.indexOf('_')));
        }
        res.close();
        return types;
    }


    /**
     * Return the match types, used to determine which additional query to run
     * This is a protected method so that it can be overriden for testing
     * @param connection the db connection
     * @return the SQL result set
     * @throws SQLException if a database problem occurs
     */
    protected ResultSet getMatchTypesResultSet(Connection connection) throws SQLException {
        String query =
            "SELECT name from cvterm where name like '%_match' ";
        LOG.info("executing: " + query);
        long bT = System.currentTimeMillis();
        Statement stmt = connection.createStatement();
        ResultSet res = stmt.executeQuery(query);
        LOG.debug("QUERY TIME feature match types: " + (System.currentTimeMillis() - bT));
        return res;
    }


    /**
     * Return the interesting feature (EST, UST, RST, other?) matches
     * from the featureloc and feature tables.
     *
     * feature<->featureloc<->match_feature<->featureloc<->feature
     * This is a protected method so that it can be overriden for testing
     * @param connection the db connection
     * @param featType the type of feature (EST,UST, etc)
     * @return the SQL result set
     * @throws SQLException if a database problem occurs
     */
    protected ResultSet getMatchLocResultSet(Connection connection, String featType)
        throws SQLException {
        String query =
            "SELECT -1 AS featureloc_id, feat.feature_id, chrloc.fmin, "
            + " chrloc.srcfeature_id AS srcfeature_id, chrloc.fmax, FALSE AS is_fmin_partial, "
            + " featloc.strand "
            + " FROM feature feat, featureloc featloc, cvterm featcv, feature mf, "
            + " cvterm mfcv, featureloc chrloc, feature chr, cvterm chrcv "
            + " WHERE feat.type_id = featcv.cvterm_id "
            + " AND featcv.name = '" + featType + "' "
            + " AND feat.feature_id = featloc.srcfeature_id "
            + " AND featloc.feature_id = mf.feature_id "
            + " AND mf.feature_id = chrloc.feature_id "
            + " AND chrloc.srcfeature_id = chr.feature_id "
            + " AND chr.type_id = chrcv.cvterm_id "
            + " AND chrcv.name = 'chromosome' "
            + " AND mf.type_id = mfcv.cvterm_id "
            + " AND mfcv.name = '" + featType + "_match' "
            + " AND feat.feature_id IN "
            + " (select feature_id from " + SUBFEATUREID_TEMP_TABLE_NAME + " ) ";
        LOG.info("executing: " + query);
        long bT = System.currentTimeMillis();
        Statement stmt = connection.createStatement();
        ResultSet res = stmt.executeQuery(query);
        LOG.info("QUERY TIME feature " + featType + "_match: " + (System.currentTimeMillis() - bT));
        return res;
    }

    /**
     * {@inheritDoc}
     *
     * see FlyBaseProcessor for many more examples of configuration
     */
    @Override
    protected Map<MultiKey, List<ConfigAction>> getConfig(int taxonId) {
        MultiKeyMap map = config.get(new Integer(taxonId));
        if (map == null) {
            map = new MultiKeyMap();
            config.put(new Integer(taxonId), map);

            // TODO: check possible conflicts with our sql matching
            // map.put(new MultiKey("relationship", "ESTMatch", "evidence_for_feature", "Intron"),
            //        Arrays.asList(new SetFieldConfigAction("intron")));

            map.put(new MultiKey("relationship", "ThreePrimeUTR", "adjacent_to", "CDS"),
                    Arrays.asList(new SetFieldConfigAction("CDS")));

            map.put(new MultiKey("relationship", "PolyASite",
                    "derives_from", "ThreePrimeRACEClone"),
                    Arrays.asList(new SetFieldConfigAction("threePrimeRACEClone")));
            map.put(new MultiKey("relationship", "ThreePrimeRST",
                    "derives_from", "ThreePrimeRACEClone"),
                    Arrays.asList(new SetFieldConfigAction("threePrimeRACEClone")));

            // evidence_for_feature
            map.put(new MultiKey("relationship", "OverlappingESTSet",
                    "evidence_for_feature", "TranscriptRegion"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));

            map.put(new MultiKey("relationship", "ExperimentalFeature",
                    "evidence_for_feature", "Transcript"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "ExperimentalFeature",
                    "evidence_for_feature", "Exon"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "ExperimentalFeature",
                    "evidence_for_feature", "Intron"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "ExperimentalFeature",
                    "evidence_for_feature", "ExonRegion"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));

            // partial_evidence_for_feature
            map.put(new MultiKey("relationship", "OverlappingESTSet",
                    "partial_evidence_for_feature", "MRNA"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "TranscriptRegion",
                    "partial_evidence_for_feature", "MRNA"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));

            // complete_evidence_for_feature
            map.put(new MultiKey("relationship", "ThreePrimeUST",
                    "complete_evidence_for_feature", "ThreePrimeUTR"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "OverlappingESTSet",
                    "complete_evidence_for_feature", "Intron"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "OverlappingESTSet",
                    "complete_evidence_for_feature", "PolyASite"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "OverlappingESTSet",
                    "complete_evidence_for_feature", "SL1AcceptorSite"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "OverlappingESTSet",
                    "complete_evidence_for_feature", "SL2AcceptorSite"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "OverlappingESTSet",
                    "complete_evidence_for_feature", "TranscriptionEndSite"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "OverlappingESTSet",
                    "complete_evidence_for_feature", "TSS"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));

            // full_evidence_for_feature
            //          map.put(new MultiKey("relationship", "OverlappingESTSet",
            //                  "full_evidence_for_feature", "Gene"),
            //                  Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "OverlappingESTSet",
                    "full_evidence_for_feature", "MRNA"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));
            map.put(new MultiKey("relationship", "TranscriptRegion",
                    "full_evidence_for_feature", "MRNA"),
                    Arrays.asList(new SetFieldConfigAction("supportedFeatures")));

            // additional properties
            map.put(new MultiKey("prop", "CDS", "status"),
                    Arrays.asList(new SetFieldConfigAction("status")));
            map.put(new MultiKey("prop", "CDS", "wormpep"),
                    Arrays.asList(new SetFieldConfigAction("wormpep")));

            map.put(new MultiKey("prop", "MRNA", "cds"),
                    Arrays.asList(new SetFieldConfigAction("CDS")));
            map.put(new MultiKey("prop", "MRNA", "wormpep"),
                    Arrays.asList(new SetFieldConfigAction("wormpep")));

            map.put(new MultiKey("prop", "PolyASite", "external_evidence"),
                    Arrays.asList(new SetFieldConfigAction("externalEvidence")));

            map.put(new MultiKey("prop", "ThreePrimeRST", "genbank_acc"),
                    Arrays.asList(new SetFieldConfigAction("GenBankAcc")));
            map.put(new MultiKey("prop", "ThreePrimeRST", "ncbi_dbest"),
                    Arrays.asList(new SetFieldConfigAction("NCBIdbEST")));

            map.put(new MultiKey("prop", "ThreePrimeUTR", "external_evidence"),
                    Arrays.asList(new SetFieldConfigAction("externalEvidence")));

            map.put(new MultiKey("prop", "BindingSite", "qvalue"),
                    Arrays.asList(new SetFieldConfigAction("qvalue")));

            map.put(new MultiKey("prop", "Exon", "acceptor"),
                    Arrays.asList(new SetFieldConfigAction("acceptor")));
            map.put(new MultiKey("prop", "Exon", "connected_to_wormbase_transcript"),
                    Arrays.asList(new SetFieldConfigAction("connectedToWormbaseTranscript")));
            map.put(new MultiKey("prop", "Exon", "donor"),
                    Arrays.asList(new SetFieldConfigAction("donor")));
            map.put(new MultiKey("prop", "Exon", "overlapping_wormbase_transcript"),
                    Arrays.asList(new SetFieldConfigAction("overlappingWormbaseTranscript")));
            map.put(new MultiKey("prop", "Exon", "polyA"),
                    Arrays.asList(new SetFieldConfigAction("polyA")));
            map.put(new MultiKey("prop", "Exon", "tes"),
                    Arrays.asList(new SetFieldConfigAction("tes")));
            map.put(new MultiKey("prop", "Exon", "tsl"),
                    Arrays.asList(new SetFieldConfigAction("tsl")));
            map.put(new MultiKey("prop", "Exon", "tss"),
                    Arrays.asList(new SetFieldConfigAction("tss")));

            map.put(new MultiKey("prop", "OverlappingESTSet", "fdr"),
                    Arrays.asList(new SetFieldConfigAction("fdr")));
            map.put(new MultiKey("prop", "OverlappingESTSet", "fp"),
                    Arrays.asList(new SetFieldConfigAction("fp")));
            map.put(new MultiKey("prop", "OverlappingESTSet", "overlap"),
                    Arrays.asList(new SetFieldConfigAction("overlap")));
            map.put(new MultiKey("prop", "OverlappingESTSet", "reads"),
                    Arrays.asList(new SetFieldConfigAction("reads")));
            map.put(new MultiKey("prop", "OverlappingESTSet", "strands_confirmed"),
                    Arrays.asList(new SetFieldConfigAction("strandsConfirmed")));
            map.put(new MultiKey("prop", "OverlappingESTSet", "trimT"),
                    Arrays.asList(new SetFieldConfigAction("trimT")));

            map.put(new MultiKey("prop", "Transcript", "transcribed"),
                    Arrays.asList(new SetFieldConfigAction("transcribed")));

            map.put(new MultiKey("prop", "TranscriptRegion", "marginal_fpr"),
                    Arrays.asList(new SetFieldConfigAction("marginalFpr")));
            map.put(new MultiKey("prop", "TranscriptRegion", "marginal_sensitivity"),
                    Arrays.asList(new SetFieldConfigAction("marginalSensitivity")));
            map.put(new MultiKey("prop", "TranscriptRegion", "mean_intensity"),
                    Arrays.asList(new SetFieldConfigAction("meanIntensity")));
            map.put(new MultiKey("prop", "TranscriptRegion", "rank_score"),
                    Arrays.asList(new SetFieldConfigAction("rankScore")));

            map.put(new MultiKey("prop", "SequenceFeature", "prediction_status"),
                    Arrays.asList(new SetFieldConfigAction("predictionStatus")));
            map.put(new MultiKey("prop", "SequenceFeature", "note"),
                    Arrays.asList(new SetFieldConfigAction("note")));

        }
        return map;
    }

    /**
     * copied from FlyBaseProcessor
     * {@inheritDoc}
     */
    @Override
    protected Item makeFeature(Integer featureId, String chadoFeatureType, String interMineType,
            String name, String uniqueName, int seqlen, int taxonId) {
        String realInterMineType = interMineType;

        if ("chromosome_arm".equals(chadoFeatureType)
                || "ultra_scaffold".equals(chadoFeatureType)) {
            realInterMineType = "Chromosome";

            if (uniqueName.startsWith("chr")) {
                // this is to fix some data problem with sub 146 in modmine
                // where there are duplicated chromosome_arm features, with
                // and without a 'chr' prefix (e.g. 3R and chr3R)
                // The chr ones are not the location for any other feature.
                // So we skip them.
                return null;
            }
        }
        return getChadoDBConverter().createItem(realInterMineType);
    }

    /**
     * Create a temporary table of all feature_ids for a given submission.
     * @param connection the connection
     * @throws SQLException if there is a database problem
     */
    protected void createSubFeatureIdTempTable(Connection connection) throws SQLException {

        String query =
            " CREATE TEMPORARY TABLE " + SUBFEATUREID_TEMP_TABLE_NAME
            + " AS SELECT df.feature_id "
            + " FROM data_feature df, " + dataIdsTableName + " d"
            + " WHERE df.data_id = d.data_id";

        Statement stmt = connection.createStatement();
        LOG.info("executing: " + query);
        long bT = System.currentTimeMillis();
        stmt.execute(query);
        LOG.info("TIME feature creating TEMP table: " + (System.currentTimeMillis() - bT));
        String idIndexQuery = "CREATE INDEX " + SUBFEATUREID_TEMP_TABLE_NAME + "_feature_index ON "
            + SUBFEATUREID_TEMP_TABLE_NAME + "(feature_id)";
        LOG.info("executing: " + idIndexQuery);
        long bT1 = System.currentTimeMillis();
        stmt.execute(idIndexQuery);
        LOG.info("TIME feature creating INDEX: " + (System.currentTimeMillis() - bT1));
        String analyze = "ANALYZE " + SUBFEATUREID_TEMP_TABLE_NAME;
        LOG.info("executing: " + analyze);
        long bT2 = System.currentTimeMillis();
        stmt.execute(analyze);
        LOG.debug("TIME feature analyzing: " + (System.currentTimeMillis() - bT2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void earlyExtraProcessing(Connection connection) throws  SQLException {
        // to limit the process to the current submission
        createSubFeatureIdTempTable(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finishedProcessing(Connection connection,
            Map<Integer, FeatureData> featureDataMap)
        throws SQLException {
        super.finishedProcessing(connection, featureDataMap);

        String query = "DROP TABLE " + SUBFEATUREID_TEMP_TABLE_NAME;
        Statement stmt = connection.createStatement();
        LOG.info("executing: " + query);
        stmt.execute(query);
    }

    /**
     * Process the identifier and return a "cleaned" version.  Implemented in sub-classes to fix
     * data problem.
     * @param type the InterMine type of the feature that this identifier came from
     * @param identifier the identifier
     * @return a cleaned identifier
     */
    /**
     * {@inheritDoc}
     */
    @Override
    protected String fixIdentifier(FeatureData fdat, String identifier) {

        String uniqueName = fdat.getChadoFeatureUniqueName();
        String name = fdat.getChadoFeatureName();
        String type = fdat.getInterMineType();

        if (identifier.equalsIgnoreCase(uniqueName)) {
            if (type.equalsIgnoreCase(CHROMOSOME)) {
                if (uniqueName.equalsIgnoreCase("M")) {
                    // this is to fix some data problem in modmine
                    // where submissions (e.g. 100) refer to M chromosome instead
                    // of dmel_mitochondrion_genome as in FlyBase
                    uniqueName = MITOCHONDRION;
                    return uniqueName;
                }
            }

            // Piano submissions have Gene: and Transcript:
            // in front of gene and transcript identifiers
            if (type.equalsIgnoreCase("Gene")) {
                if (uniqueName.startsWith("Gene:")) {
                    return uniqueName.substring(5);
                }
            }
        }

        if (StringUtils.isEmpty(identifier)) {
            if (StringUtils.isEmpty(name)) {
                String fixedName = uniqueName.substring(uniqueName.lastIndexOf('.') + 1);
                return fixedName;
            } else {
                return name;
            }
        }
        return identifier;
    }


    private void processPeaksSources(Connection connection) throws SQLException,
    ObjectStoreException {
        ResultSet res = getPeaksSources(connection);
        while (res.next()) {

            Integer featureId = res.getInt("feature_id");
//            Integer score = res.getInt("data_id");
            String sourceFile = res.getString("value");

            if (featureMap.containsKey(featureId)) {
                FeatureData fData = featureMap.get(featureId);
                Integer storedFeatureId = fData.getIntermineObjectId();

                Attribute sourceFileAttribute = new Attribute("sourceFile", sourceFile);
                getChadoDBConverter().store(sourceFileAttribute, storedFeatureId);

//                if (scoreProtocolItemId != null) {
//                    Reference scoreProtocolRef =
//                        new Reference("scoreProtocol", scoreProtocolItemId);
//                    getChadoDBConverter().store(scoreProtocolRef, storedFeatureId);
//                }
            }
        }
        res.close();
    }

    private ResultSet getPeaksSources(Connection connection) throws SQLException {
        String query =
            "SELECT df.feature_id, df.data_id, d.value "
            + "FROM data_feature df, feature f, cvterm c, data d "
            + "WHERE f.feature_id = df.feature_id "
            + "AND c.cvterm_id = f.type_id " 
            + "AND d.data_id = df.data_id "
            + "AND c.name like '%binding_site' " ;
        LOG.info("executing: " + query);
        long bT = System.currentTimeMillis();
        Statement stmt = connection.createStatement();
        ResultSet res = stmt.executeQuery(query);
        LOG.info("QUERY TIME feature sources: " + (System.currentTimeMillis() - bT));
        return res;
    }

    
    
    
    private void processFeatureScores(Connection connection) throws SQLException,
    ObjectStoreException {
        ResultSet res = getFeatureScores(connection);
        while (res.next()) {

            Integer featureId = res.getInt("feature_id");
            Double score = res.getDouble("score");
            String program = res.getString("program");

            if (featureMap.containsKey(featureId)) {
                FeatureData fData = featureMap.get(featureId);
                Integer storedFeatureId = fData.getIntermineObjectId();

                Attribute scoreAttribute = new Attribute("score", score.toString());
                getChadoDBConverter().store(scoreAttribute, storedFeatureId);

                Attribute scoreTypeAttribute = new Attribute("scoreType", program);
                getChadoDBConverter().store(scoreTypeAttribute, storedFeatureId);

                if (scoreProtocolItemId != null) {
                    Reference scoreProtocolRef =
                        new Reference("scoreProtocol", scoreProtocolItemId);
                    getChadoDBConverter().store(scoreProtocolRef, storedFeatureId);
                }
            }
        }
        res.close();
    }

    private ResultSet getFeatureScores(Connection connection) throws SQLException {
        String query =
            "SELECT af.feature_id as feature_id, af.rawscore as score, a.program as program"
            + " FROM analysisfeature af, analysis a "
            + " WHERE af.analysis_id = a.analysis_id "
            + " AND af.feature_id IN "
            + " (select feature_id from " + SUBFEATUREID_TEMP_TABLE_NAME + " ) ";
        LOG.info("executing: " + query);
        long bT = System.currentTimeMillis();
        Statement stmt = connection.createStatement();
        ResultSet res = stmt.executeQuery(query);
        LOG.info("QUERY TIME feature scores: " + (System.currentTimeMillis() - bT));
        return res;
    }

    private void processExpressionLevels(Connection connection) throws SQLException,
    ObjectStoreException {
        ResultSet res = getExpressionLevels(connection);

        Integer previousId = -1;
        Item level = null;

        while (res.next()) {
            Integer id = res.getInt("expression_id");
            Integer featureId = res.getInt("feature_id");
            String name = res.getString("uniquename");
            String value = res.getString("value");
            String property = res.getString("property");
            String propValue = res.getString("propvalue");

            LOG.debug("EL: " + id + "|" + previousId + "->"
                    + featureId + ":" + property + "|" + propValue);

            if (!id.equals(previousId)) {
                // if not first store prev level
                if (previousId > 0) {
                    getChadoDBConverter().store(level);
                }
                // create new
                level = getChadoDBConverter().createItem("ExpressionLevel");
                level.setAttribute("name", name);
                if (!StringUtils.isBlank(value)) {
                    level.setAttribute("value", value);
                } else {
                    LOG.warn("ExpressionLevel found with blank value for uniquename: " + name);
                }
                if (featureMap.containsKey(featureId)) {
                    FeatureData fData = featureMap.get(featureId);
                    String referenceName = "feature";
                    String featureItemId = fData.getItemIdentifier();
                    level.setReference(referenceName, featureItemId);
                    level.setReference("submission", dataSetIdentifier);
                }
            }

            if (property.equalsIgnoreCase("dcpm") && !propValue.contains(".")) {
                // in some cases (~ 60000, waterston) the value for dcpm is
                // 'nan' or 'na' instead of a decimal number
                previousId = id;
                continue;
            }
            level.setAttribute(getPropName(property), propValue);
            previousId = id;
        }
        if (res.isAfterLast()) {
            getChadoDBConverter().store(level);
        }
        res.close();
    }

    private String getPropName(String property) {
        if (property.equalsIgnoreCase("read_count")) {
            return "readCount";
        }
        if (property.equalsIgnoreCase("dcpm_bases")) {
            return "dcpmBases";
        }
        if (property.equalsIgnoreCase("prediction_status")) {
            return "predictionStatus";
        }
        return property;
    }

    private ResultSet getExpressionLevels(Connection connection) throws SQLException {

        String query = "SELECT subject_id as expression_id, f1.uniquename, af.rawscore as value "
            + " , object_id as feature_id, cp.name as property, fp.value as propvalue "
            + " FROM feature_relationship, cvterm c1, feature f1, analysisfeature af, "
            + "   featureprop fp, cvterm cp "
            + " WHERE c1.cvterm_id = f1.type_id "
            + " AND f1.feature_id = subject_id "
            + " and af.feature_id = f1.feature_id "
            + " and f1.feature_id = fp.feature_id "
            + " and cp.cvterm_id = fp.type_id "
            + " and c1.name= 'experimental_feature' "
            + " AND subject_id IN (select feature_id from " + SUBFEATUREID_TEMP_TABLE_NAME + " ) "
            + " ORDER BY subject_id";

        LOG.info("executing: " + query);
        long bT = System.currentTimeMillis();
        Statement stmt = connection.createStatement();
        ResultSet res = stmt.executeQuery(query);
        LOG.info("QUERY TIME expression levels: " + (System.currentTimeMillis() - bT));
        return res;
    }

}
