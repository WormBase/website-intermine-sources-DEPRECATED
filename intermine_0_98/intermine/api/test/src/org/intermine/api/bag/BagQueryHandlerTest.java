package org.intermine.api.bag;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.intermine.metadata.Model;
import org.intermine.util.SAXParser;
import org.xml.sax.InputSource;

public class BagQueryHandlerTest extends TestCase
{

    public BagQueryHandlerTest(String arg0) {
        super(arg0);
    }

    public void testParse() throws Exception {
        Model model = Model.getInstanceByName("testmodel");
        InputStream is = getClass().getClassLoader().getResourceAsStream("BagQueryHandlerTest.xml");
        if (is == null) {
            throw new IllegalArgumentException("is was null");
        }
        BagQueryHandler handler = new BagQueryHandler(model);
        SAXParser.parse(new InputSource(is), handler);
        Map actual =  handler.getBagQueries();
        System.out.println("bagQueries: " + actual);
        assertNotNull(actual.get("Employee"));
        assertEquals(1, ((List) actual.get("Employee")).size());

    }

}
