package org.intermine.web.struts;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.HashMap;

import org.intermine.api.profile.Profile;
import org.intermine.web.logic.Constants;
import org.intermine.web.logic.session.SessionMethods;

import servletunit.struts.MockStrutsTestCase;

/**
 * Tests for the LoadQueryAction class
 *
 * @author Kim Rutherford
 */
public class LoadQueryActionTest extends MockStrutsTestCase
{
    public LoadQueryActionTest(String arg) {
        super(arg);
    }


    public void tearDown() throws Exception {
         getActionServlet().destroy();
    }

    public void testLoadXml() {
        String xml = "<query name=\"\" model=\"testmodel\" view=\"Employee Employee.name\">\n" +
                "</query>";

        getSession().setAttribute(Constants.PROFILE,
                                  new Profile(null, null, null, null,
                                              new HashMap(), new HashMap(), new HashMap(), true));
        addRequestParameter("method", "xml");
        addRequestParameter("query", xml);
        addRequestParameter("skipBuilder", "false");

        setRequestPathInfo("/loadQuery");

        actionPerform();
        verifyNoActionErrors();
        verifyForward("query");
        assertNotNull(SessionMethods.getQuery(getSession()));
    }

    public void testLoadXmlSkipBuilder() {
        String xml = "<query name=\"\" model=\"testmodel\" view=\"Employee Employee.name\">\n" +
                "</query>";
        getSession().setAttribute(Constants.PROFILE,
                                  new Profile(null, null, null, null,
                                              new HashMap(), new HashMap(), new HashMap(), true));
        addRequestParameter("method", "xml");
        addRequestParameter("query", xml);
        addRequestParameter("skipBuilder", "true");

        setRequestPathInfo("/loadQuery");

        actionPerform();
        verifyNoActionErrors();

        //assertEquals("/pollQuery.do?qid=0", getActualForward());
        assertNotNull(SessionMethods.getQuery(getSession()));
    }
}
