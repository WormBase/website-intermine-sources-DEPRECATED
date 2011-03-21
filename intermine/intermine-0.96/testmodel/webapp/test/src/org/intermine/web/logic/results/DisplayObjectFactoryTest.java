package org.intermine.web.logic.results;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.intermine.api.InterMineAPI;
import org.intermine.metadata.Model;
import org.intermine.model.testmodel.Company;
import org.intermine.model.testmodel.Department;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.dummy.ObjectStoreDummyImpl;
import org.intermine.util.DynamicUtil;
import org.intermine.web.logic.Constants;
import org.intermine.web.logic.config.WebConfig;
import org.intermine.web.logic.results.DisplayObject;
import org.intermine.web.logic.results.DisplayObjectFactory;
import org.intermine.web.logic.session.SessionMethods;

import servletunit.struts.MockStrutsTestCase;

/**
 * Tests for DisplayObjectCache
 */
public class DisplayObjectFactoryTest extends MockStrutsTestCase
{
    ObjectStore os;

    public DisplayObjectFactoryTest(String arg) {
        super(arg);
    }

    public void setUp() throws Exception {
        super.setUp();
        Department d = new Department();
        Set classes = new HashSet();
        classes.add(Company.class);
        d.setCompany((Company) DynamicUtil.createObject(classes));
        os = new ObjectStoreDummyImpl();
        os.cacheObjectById(new Integer(42), d);
        ((ObjectStoreDummyImpl) os).setModel(Model.getInstanceByName("testmodel"));
    }

    public void testDisplayObjectCache() throws Exception {
        InterMineAPI im = new InterMineAPI(os, null, null, null, null, null, null);
        SessionMethods.setInterMineAPI(getSession().getServletContext(), im);
        Map webProperties = new HashMap();
        getSession().getServletContext().setAttribute(Constants.WEB_PROPERTIES, webProperties);
        WebConfig webConfig = new WebConfig();
        getSession().getServletContext().setAttribute(Constants.WEBCONFIG, webConfig);

        DisplayObjectFactory doc = new DisplayObjectFactory(getSession());

        Company company1 = (Company) DynamicUtil.createObject(Collections.singleton(Company.class));
        company1.setName("Company1");
        company1.setId(new Integer(1));

        Company company2 = (Company) DynamicUtil.createObject(Collections.singleton(Company.class));
        company2.setName("Company2");
        company2.setId(new Integer(2));

        DisplayObject displayObject1 = (DisplayObject) doc.get(company1);
        DisplayObject displayObject2 = (DisplayObject) doc.get(company1);
        DisplayObject displayObject3 = (DisplayObject) doc.get(company2);

        assertNotNull(displayObject1);
        assertTrue(displayObject1 == displayObject2);
        assertNotNull(displayObject3);
        assertTrue(displayObject1 != displayObject3);
    }
}
