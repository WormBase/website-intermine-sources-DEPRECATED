package org.intermine.api.bag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.intermine.api.config.ClassKeyHelper;
import org.intermine.metadata.Model;
import org.intermine.model.testmodel.Department;
import org.intermine.objectstore.query.Query;

public class BagQueryHelperTest extends TestCase {

    Model model;
    Map classKeys;
    BagQueryConfig bagQueryConfig = new BagQueryConfig(new HashMap(), new HashMap(), new HashMap());
    BagQueryConfig bagQueryConfigWithExtraConstraint = new BagQueryConfig(new HashMap(), new HashMap(), new HashMap());

    public BagQueryHelperTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        model = Model.getInstanceByName("testmodel");
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("class_keys.properties"));
        classKeys = ClassKeyHelper.readKeys(model, props);
        bagQueryConfigWithExtraConstraint.setConnectField("department");
        bagQueryConfigWithExtraConstraint.setExtraConstraintClassName(Department.class.getName());
        bagQueryConfigWithExtraConstraint.setConstrainField("name");
    }

    public void testCreateDefaultBagQuerySingle() throws Exception {
        List input = new ArrayList(Arrays.asList(new Object[] {"EmployeeA1", "EmployeeA2"}));
        Query q = BagQueryHelper.createDefaultBagQuery(model.getPackageName() + ".Employee",
                                                      bagQueryConfig, model, classKeys, input);
//        BagQuery bq = BagQueryHelper.createDefaultBagQuery(model.getPackageName() + ".Employee",
//                                                           bagQueryConfig, model, classKeys, input);
        String expected = "SELECT DISTINCT a1_.id AS a2_, a1_.name AS a3_ FROM org.intermine.model.testmodel.Employee AS a1_ WHERE LOWER(a1_.name) IN ? 1: [employeea1, employeea2]";
        //assertEquals(expected, bq.getQuery(input, "DepartmentB1").toString());
        assertEquals(expected, q.toString());
    }

    public void testCreateDefaultBagQueryWithExtra() throws Exception {
        List input = new ArrayList(Arrays.asList(new Object[] {"EmployeeA1", "EmployeeA2"}));

        Query q = BagQueryHelper.createDefaultBagQuery(model.getPackageName() + ".Employee",
                                                       bagQueryConfigWithExtraConstraint,
                                                       model, classKeys, input);
        BagQuery bq = new BagQuery(bagQueryConfigWithExtraConstraint, model, classKeys, model.getPackageName() + ".Employee");

        //        BagQuery bq = BagQueryHelper.createDefaultBagQuery(model.getPackageName() + ".Employee",
//                                                           bagQueryConfigWithExtraConstraint,
//                                                           model, classKeys, input);
        String expected = "SELECT DISTINCT a1_.id AS a2_, a1_.name AS a3_ FROM org.intermine.model.testmodel.Employee AS a1_, org.intermine.model.testmodel.Department AS a4_ WHERE (LOWER(a1_.name) IN ? AND a1_.department CONTAINS a4_ AND a4_.name = \'DepartmentB1\') 1: [employeea1, employeea2]";
        assertEquals(expected, bq.getQuery(input, "DepartmentB1").toString());
        //assertEquals(expected, q.toString());
    }

    public void testCreateDefaultBagQueryMultiple() throws Exception {
        List input = new ArrayList(Arrays.asList(new Object[] {"EmployeeA1", "EmployeeB1"}));
        Query q = BagQueryHelper.createDefaultBagQuery(model.getPackageName() + ".Manager",
                                                         bagQueryConfig, model, classKeys, input);
        String expected = "SELECT DISTINCT a1_.id AS a2_, a1_.name AS a3_, a1_.title AS a4_ FROM org.intermine.model.testmodel.Manager AS a1_ WHERE (LOWER(a1_.name), LOWER(a1_.title)) IN ? 1: [employeea1, employeeb1]";
        assertEquals(expected, q.toString());
    }
}
