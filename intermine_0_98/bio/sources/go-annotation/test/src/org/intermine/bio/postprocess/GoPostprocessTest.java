package org.intermine.bio.postprocess;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.custommonkey.xmlunit.XMLTestCase;
import org.intermine.model.bio.GOAnnotation;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.Protein;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.ObjectStoreWriterFactory;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.SingletonResults;
import org.intermine.util.DynamicUtil;

/**
 * Tests for the GoPostprocess class.
 */
public class GoPostprocessTest extends XMLTestCase {

    private ObjectStoreWriter osw;

    public void setUp() throws Exception {
        super.setUp();
        osw = ObjectStoreWriterFactory.getObjectStoreWriter("osw.bio-test");
        osw.getObjectStore().flushObjectById();
        setUpData();
    }

    public void tearDown() throws Exception {
        Query q = new Query();
        QueryClass qc = new QueryClass(InterMineObject.class);
        q.addFrom(qc);
        q.addToSelect(qc);
        ObjectStore os = osw.getObjectStore();
        SingletonResults res = os.executeSingleton(q);
        Iterator resIter = res.iterator();
        osw.beginTransaction();
        while (resIter.hasNext()) {
            InterMineObject o = (InterMineObject) resIter.next();
            osw.delete(o);
        }
        osw.commitTransaction();
        osw.close();
    }

    public void testPostProcess() throws Exception {
        GoPostprocess gp = new GoPostprocess(osw);
        gp.postProcess();
        
        Gene resGene = (Gene) getFromDb(Gene.class).iterator().next();

        // Gene should come back with a collection of GO terms
        assertEquals(2, resGene.getGoAnnotation().size());
    }


    // Store a gene with two protein, each protein has a GO term
    private void setUpData() throws Exception {
        Gene gene = (Gene) DynamicUtil.createObject(Collections.singleton(Gene.class));
        Protein protein1 = (Protein) DynamicUtil.createObject(Collections.singleton(Protein.class));
        protein1.addGenes(gene);
        Protein protein2 = (Protein) DynamicUtil.createObject(Collections.singleton(Protein.class));
        protein2.addGenes(gene);
        
        GOAnnotation go1 = (GOAnnotation) DynamicUtil.createObject(Collections.singleton(GOAnnotation.class));
        go1.setSubject(protein1);
        GOAnnotation go2 = (GOAnnotation) DynamicUtil.createObject(Collections.singleton(GOAnnotation.class));
        go2.setSubject(protein2);
        
        List toStore = new ArrayList(Arrays.asList(new Object[] {gene, protein1, protein2, go1, go2}));

        osw.beginTransaction();
        Iterator i = toStore.iterator();
        while (i.hasNext()) {
            osw.store((InterMineObject) i.next());
        }
        osw.commitTransaction();
    }

    private Set<InterMineObject> getFromDb(Class relClass) throws Exception {
        Query q = new Query();
        QueryClass qc = new QueryClass(relClass);
        q.addToSelect(qc);
        q.addFrom(qc);
        SingletonResults res = osw.getObjectStore().executeSingleton(q);
        Set<InterMineObject> results = new HashSet<InterMineObject>();
        Iterator resIter = res.iterator();
        while(resIter.hasNext()) {
            results.add((InterMineObject) resIter.next());
        }
        ObjectStore os = osw.getObjectStore();
        os.flushObjectById();
        return results;
    }
}
