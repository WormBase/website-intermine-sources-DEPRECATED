package org.intermine.objectstore;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Arrays;
import java.util.Properties;

import junit.framework.Test;

public class ObjectStoreSummaryTest extends StoreDataTestCase
{
    public ObjectStoreSummaryTest(String arg) {
        super(arg);
    }

    public void setUp() throws Exception {
        super.setUp();
        strictTestQueries = false;
    }

    public static void oneTimeSetUp() throws Exception {
        StoreDataTestCase.oneTimeSetUp();
    }

    public void executeTest(String type) {

    }

    public static Test suite() {
        return buildSuite(ObjectStoreSummaryTest.class);
    }

    public void testGetCount() throws Exception {
        ObjectStore os = ObjectStoreFactory.getObjectStore("os.unittest");
        ObjectStoreSummary oss = new ObjectStoreSummary(os, new Properties());
        assertEquals(2, oss.getClassCount("org.intermine.model.testmodel.Company"));

        System.out.println("" + oss.toProperties());
    }

    public void testGetFieldValues() throws Exception {
        Properties config = new Properties();
        config.put("max.field.value", "10");
        config.put("org.intermine.model.testmodel.Employee.fields", "age name");
        config.put("org.intermine.model.testmodel.Manager.fields", "title");
        ObjectStore os = ObjectStoreFactory.getObjectStore("os.unittest");
        ObjectStoreSummary oss = new ObjectStoreSummary(os, config);
        assertEquals(Arrays.asList(new Object [] {"10", "20", "30", "40", "50", "60"}),
                     oss.getFieldValues("org.intermine.model.testmodel.Employee", "age"));

        assertEquals(Arrays.asList(new Object [] {"Mr.", null}),
                     oss.getFieldValues("org.intermine.model.testmodel.Manager", "title"));

        // null because Bank.name isn't in the objectstoresummary.config.properties file
        assertNull(oss.getFieldValues("org.intermine.model.testmodel.Bank", "name"));

        // null because max.field.values exceeded
        assertNull(oss.getFieldValues("org.intermine.model.testmodel.Thing", "id"));
        assertNull(oss.getFieldValues("org.intermine.model.InterMineObject", "id"));
    }

    public void testToProperties() throws Exception {
        Properties config = new Properties();
        config.put("max.field.value", "10");
        ObjectStore os = ObjectStoreFactory.getObjectStore("os.unittest");
        ObjectStoreSummary oss = new ObjectStoreSummary(os, config);

        Properties out = oss.toProperties();
        oss = new ObjectStoreSummary(out);

        assertEquals(out, oss.toProperties());
    }
}
