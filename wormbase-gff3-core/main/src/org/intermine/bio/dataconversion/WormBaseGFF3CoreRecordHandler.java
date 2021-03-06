package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2012 The WormBase Consortium
 * 
 * California Institute of Technology
 * Ontario Institute for Cancer Research 
 * Burroughs Wellcome Trust Sanger Institute
 * Washington University St. Louis
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.intermine.bio.io.gff3.GFF3Record;
import org.intermine.metadata.Model;
import org.intermine.xml.full.Item;

/**
 * A generic munger of WormBase GFF3 files
 *
 * @author Todd Harris (todd@wormbase.org)
 */

public class WormBaseGFF3CoreRecordHandler extends GFF3RecordHandler
{
    private Map<String, Item> elementsMap = new HashMap<String, Item>();

    /**
     * Create a new WormBaseGFF3RecordHandler for the given target model.
     * @param tgtModel the model for which items will be created
     */
    public WormBaseGFF3CoreRecordHandler (Model tgtModel) {
        super(tgtModel);
        // refsAndCollections controls references and collections that are set from the
        // Parent= attributes in the GFF3 file.
        //refsAndCollections.put("Exon", "transcripts");
        refsAndCollections.put("Exon", "transcript");
	//        refsAndCollections.put("CDS",  "transcript");
	refsAndCollections.put("MRNA", "gene");
    }

    /**
     * {@inheritDoc}
     */
    /*    @Override necessary */
    public void process(GFF3Record record) {
        Item feature = getFeature();

	// Skips exons which have no id
	// if (feature.getAttribute("primaryIdentifier") == null) {
	//    return;
	//}
	
        String clsName = feature.getClassName();
	//	System.out.println("class name is " + clsName);

	// Set the primary identifier to the Name
	if ("Gene".equals(clsName)) {	
	    // move Gene.primaryIdentifier to Gene.secondaryIdentifier
	    // move Gene.symbol to Gene.primaryIdentifier
		    
	            if (feature.getAttribute("primaryIdentifier") != null) {
	               String secondary = feature.getAttribute("primaryIdentifier").getValue();
	                feature.setAttribute("secondaryIdentifier", secondary);
	            }
	            if (feature.getAttribute("symbol") != null) {
	                String primary = feature.getAttribute("symbol").getValue();
	                feature.setAttribute("primaryIdentifier", primary);
	               feature.removeAttribute("symbol");
		    }
	}
    }
}

    
 
    // This is an example from the dros-del on handling deletions in GFF.
    //    String note = record.getNote();
    //    if ("ChromosomalDeletion".equals(record.getType())) {
    //	if ("made".equals(note)) {
    //	    feature.setAttribute("available", "true");
    //	} else {
    //	    feature.setAttribute("available", "false");
    //	}
    //	String identifier = feature.getAttribute("primaryIdentifier").getValue();
    //	// don't need a primaryIdentifier
    //	feature.removeAttribute("primaryIdentifier");
    //	feature.setAttribute("secondaryIdentifier", identifier);
    //    } else {
    //	throw new RuntimeException("unknown type: " + record.getType());
    //    }

    // /**
    //     * Return false - get locations from FlyBase.
    //     * {@inheritDoc}
    //     */
    //    @Override
    //	protected boolean createLocations(@SuppressWarnings("unused") GFF3Record record) {
    //        return false;
    //    }
    
    //    /**
    //     * Return items that need extra processing that can only be done after all other GFF features
    //     * have been read.  For this class TransposableElementInsertionSite items
    //     * @return the final Items
    //     */
    //    @Override
    //	public Collection<Item> getFinalItems() {
    //       return elementsMap.values();
    //    }
    //}
