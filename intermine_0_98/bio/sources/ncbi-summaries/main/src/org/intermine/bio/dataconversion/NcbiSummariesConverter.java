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

import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.intermine.dataconversion.ItemWriter;
import org.intermine.metadata.Model;
import org.intermine.util.FormattedTextParser;
import org.intermine.xml.full.Item;


/**
 * Read file generated from NCBI web service that includes Entrez gene ids with text summaries from
 * RefSeq or OMIM.
 * @author Richard Smith
 */
public class NcbiSummariesConverter extends BioFileConverter
{
    //
    private static final String DATASET_TITLE = "NCBI Gene summaries";
    private static final String DATA_SOURCE_NAME = "NCBI Gene";

    private static final String HUMAN_TAXON_ID = "9606";

    /**
     * Constructor
     * @param writer the ItemWriter used to handle the resultant items
     * @param model the Model
     */
    public NcbiSummariesConverter(ItemWriter writer, Model model) {
        super(writer, model, DATA_SOURCE_NAME, DATASET_TITLE);
    }

    /**
     *
     *
     * {@inheritDoc}
     */
    public void process(Reader reader) throws Exception {
        // Data has format:
        // id | summary
        Iterator lineIter = FormattedTextParser.parseTabDelimitedReader(reader);
        while (lineIter.hasNext()) {
            String[] line = (String[]) lineIter.next();
            String entrez = line[0];
            String summary = line[1];

            if (!StringUtils.isBlank(summary)) {
                Item gene = createItem("Gene");
                gene.setAttribute("ncbiGeneNumber", entrez);
                gene.setAttribute("summary", summary);
                gene.setReference("organism", getOrganism(HUMAN_TAXON_ID));
                store(gene);
            }
        }
    }
}
