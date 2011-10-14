package org.intermine.bio.webservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.intermine.api.InterMineAPI;
import org.intermine.api.bag.BagManager;
import org.intermine.bio.web.logic.GenomicRegionSearchQueryRunner;
import org.intermine.bio.web.logic.GenomicRegionSearchUtil;
import org.intermine.bio.web.model.ChromosomeInfo;
import org.intermine.bio.web.model.GenomicRegion;
import org.intermine.bio.web.model.GenomicRegionSearchConstraint;
import org.intermine.bio.web.model.RegionParseException;
import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.MetaDataException;
import org.intermine.metadata.Model;
import org.intermine.web.logic.session.SessionMethods;
import org.intermine.webservice.server.exceptions.BadRequestException;
import org.intermine.webservice.server.exceptions.InternalErrorException;
import org.intermine.webservice.server.lists.ListInput;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GenomicRegionSearchListInput extends ListInput {

    private final InterMineAPI api;
    private final GenomicRegionSearchInfo info;
    
    /**
     * A representation of a request to a region based webservice. It knows how 
     * to parse and validate its own input.
     * @param request
     * @param bagManager
     * @param im
     * @throws JSONException
     */
    public GenomicRegionSearchListInput(HttpServletRequest request,
            BagManager bagManager, InterMineAPI im) throws JSONException {
        super(request, bagManager);
        api = im;
        info = parseRegionRequest();
    }

    private GenomicRegionSearchInfo parseRegionRequest() throws JSONException {
        JSONObject jsonRequest = new JSONObject(request.getParameter("query"));
        GenomicRegionSearchInfo parsed = new GenomicRegionSearchInfo();
        parsed.setOrganism(jsonRequest.getString("organism"));
        if (!jsonRequest.isNull("isInterbase")) {
            parsed.setInterbase(jsonRequest.getBoolean("isInterbase"));
        }
        if (!jsonRequest.isNull("extension")) {
            parsed.setExtension(jsonRequest.optInt("extension", 0));
        }
        JSONArray fts = jsonRequest.getJSONArray("featureTypes");
        int noOfTypes = fts.length();
        List<String> featureTypes = new ArrayList<String>();
        for (int i = 0; i < noOfTypes; i++) {
            featureTypes.add(fts.getString(i));
        }
        parsed.setFeatureTypes(featureTypes);
        
        JSONArray regs = jsonRequest.getJSONArray("regions");
        int noOfRegs = regs.length();
        List<String> regions = new ArrayList<String>();
        for (int i = 0; i < noOfRegs; i++) {
            regions.add(regs.getString(i));
        }
        parsed.setRegions(regions);
        return parsed;
    }
    
    public GenomicRegionSearchInfo getSearchInfo() {
        return info;
    }

    public class GenomicRegionSearchInfo {
        private final String SF = "org.intermine.model.bio.SequenceFeature";
        private String organism;
        private Set<String> featureTypes;
        private Set<ClassDescriptor> featureCds;
        private List<String> regions;
        private int extension = 0;
        private boolean isInterbase = false;
        private Set<String> invalidSpans = new HashSet<String>(); 
        
        public Set<String> getInvalidSpans() {
            return invalidSpans;
        }
        public String getOrganism() {
            return organism;
        }
        public void setOrganism(String organism) {
            this.organism = organism;
        }
        public Set<String> getFeatureTypes() {
            return Collections.unmodifiableSet(featureTypes);
        }
        
        /** 
         * Set the feature types for this request. Immediately parses the class
         * names to ClassDescriptors and fails as soon as possible.
         * 
         * @param featureTypes A collection of feature type names. 
         * @throws BadRequestException if the feature types are not mappable to classes, and if 
         *                             those classes are not Sequence Features.
         */
        public void setFeatureTypes(Collection<String> featureTypes) {
            this.featureTypes = new HashSet<String>(featureTypes);
            this.featureCds = new HashSet<ClassDescriptor>();
            
            Set<String> badTypes = new HashSet<String>();
            Model model = api.getModel();
            for (String f : this.featureTypes) {
                ClassDescriptor cld = model.getClassDescriptorByName(f);
                if (cld == null) {
                    badTypes.add(f);
                } else {
                    try {
                        if (!SF.equals(f) && !ClassDescriptor.findSuperClassNames(model, f).contains(SF)) {
                            throw new BadRequestException(f + " is not a " + SF);
                        }
                    } catch (MetaDataException e) {
                        // This should never happen.
                        throw new InternalErrorException(e);
                    }
                    featureCds.add(cld);
                    for (ClassDescriptor subCld : model.getAllSubs(cld)) {
                        featureCds.add(subCld);
                    }
                }
            }
            if (!badTypes.isEmpty()) {
                throw new BadRequestException("The following feature types are not " 
                        + "valid feature class names: " + badTypes);
            }
        }
        
        /**
         * Returns an unmodifiable set of the classdescriptors corresponding to the
         * feature types in this query.
         * @return
         */
        public Set<ClassDescriptor> getFeatureCds() {
            return Collections.unmodifiableSet(featureCds);
        }

        /**
         * Returns an unmodifiable set of the classes that the Class-Descriptors
         * in this query represent.
         */
        public Set<Class<?>> getFeatureClasses() {
            Set<Class<?>> ftSet = new HashSet<Class<?>>();
            for (ClassDescriptor cld : getFeatureCds()) {
                ftSet.add(cld.getType());
            }
            return Collections.unmodifiableSet(ftSet);
        }

        public List<String> getRegions() {
            return regions;
        }

        public void setRegions(List<String> regions) {
            this.regions = regions;
        }

        public List<GenomicRegion> getGenomicRegions() {
            Set<String> spans = new HashSet<String>(getRegions());
            List<GenomicRegion> regions = new ArrayList<GenomicRegion>();
            Map<String, ChromosomeInfo> chromsForOrg 
                = GenomicRegionSearchQueryRunner.getChromosomeInfo(
                        api, SessionMethods.getProfile(request.getSession())).get(getOrganism());
            for (String span : spans) {
                try {
                    regions.add(GenomicRegionSearchUtil.parseRegion(span, isInterbase(), chromsForOrg));
                } catch (RegionParseException e) {
                    invalidSpans.add(span + "; " + e.getMessage());
                }
            }
            return regions;
        }
        
        public int getExtension() {
            return extension;
        }
        public void setExtension(int extension) {
            this.extension = extension;
        }
        public boolean isInterbase() {
            return isInterbase;
        }
        public void setInterbase(boolean isInterbase) {
            this.isInterbase = isInterbase;
        }
        
        public GenomicRegionSearchConstraint asSearchConstraint() {
            GenomicRegionSearchConstraint grsc = new GenomicRegionSearchConstraint();
            grsc.setOrgName(organism);
            grsc.setFeatureTypes(getFeatureClasses());
            grsc.setGenomicRegionList(getGenomicRegions());
            grsc.setExtededRegionSize(extension);
            return grsc;
        }
        
    }


}
