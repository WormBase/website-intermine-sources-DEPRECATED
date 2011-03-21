package org.intermine.web.logic.config;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import junit.framework.TestCase;

import org.intermine.metadata.Model;

public class WebConfigTest extends TestCase
{

    public WebConfigTest(String arg) {
        super(arg);
    }

    public void testParse() throws Exception{
        InputStream is = getClass().getClassLoader().getResourceAsStream("WebConfigTest.xml");
        WebConfig wc1 = WebConfig.parse(is, Model.getInstanceByName("testmodel"));

        Displayer employeeDisplayer = new Displayer();
        employeeDisplayer.setSrc("/model/employee.jsp");

        Type employeeType = new Type();
        employeeType.setClassName("org.intermine.model.testmodel.Employee");
        employeeType.addLongDisplayer(employeeDisplayer);
        FieldConfig df1 = new FieldConfig();
        df1.setFieldExpr("name");
        df1.setShowInInlineCollection(true);
        df1.setShowInResults(true);
        df1.setShowInSummary(true);
        employeeType.addFieldConfig(df1);
        FieldConfig df2 = new FieldConfig();
        df2.setFieldExpr("department.name");
        df2.setShowInInlineCollection(true);
        df2.setShowInResults(true);
        df2.setShowInSummary(true);
        employeeType.addFieldConfig(df2);

        Displayer managerDisplayer = new Displayer();
        managerDisplayer.setSrc("/model/manager.jsp");
        managerDisplayer.setAspects("Aspect1, Aspect2");
        Displayer tdisp = new Displayer();
        tdisp.setSrc("/model/tableManager.jsp");

        Type managerType = new Type();
        managerType.setClassName("org.intermine.model.testmodel.Manager");
        managerType.addLongDisplayer(managerDisplayer);
        managerType.setTableDisplayer(tdisp);

        FieldConfig df3 = new FieldConfig();
        df3.setFieldExpr("name");
        df3.setShowInInlineCollection(true);
        df3.setShowInResults(true);
        df3.setShowInSummary(true);
        managerType.addFieldConfig(df3);
        FieldConfig df4 = new FieldConfig();
        df4.setFieldExpr("seniority");
        df4.setShowInInlineCollection(true);
        df4.setShowInResults(true);
        df4.setShowInSummary(true);
        managerType.addFieldConfig(df4);
        FieldConfig df5 = new FieldConfig();
        df5.setFieldExpr("title");
        df5.setDoNotTruncate(true);
        managerType.addFieldConfig(df5);

        Displayer disp2 = new Displayer();
        disp2.setSrc("/model/page4.jsp");
        Displayer disp3 = new Displayer();
        disp3.setSrc("tile2.tile");

        Type thingType = new Type();
        thingType.setClassName("org.intermine.model.testmodel.Thing");
        thingType.addLongDisplayer(disp2);
        thingType.addLongDisplayer(disp3);

        TableExportConfig tableExportConfig = new TableExportConfig();
        tableExportConfig.setId("myExporter");
        tableExportConfig.setClassName("java.lang.String");

        WebConfig wc2 = new WebConfig();
        wc2.addType(employeeType);
        wc2.addType(managerType);
        wc2.addType(thingType);
        wc2.addTableExportConfig(tableExportConfig);
        wc2.setSubClassConfig(Model.getInstanceByName("testmodel"));

        HashMap displayerAspects = new HashMap();
        displayerAspects.put("Aspect1", Arrays.asList(
                new Object[]{managerType.getLongDisplayers().iterator().next()}));
        displayerAspects.put("Aspect2", Arrays.asList(
                new Object[]{managerType.getLongDisplayers().iterator().next()}));
        assertEquals(displayerAspects, (wc1.getTypes().get("org.intermine.model.testmodel.Manager"))
                .getAspectDisplayers());

        assertEquals(wc2, wc1);

    }

    public void testParseNull() throws Exception{
        try {
            WebConfig.parse(null, Model.getInstanceByName("testmodel"));
            fail("Expected: NullPointerException");
        } catch (NullPointerException e) {
        }

    }

}
