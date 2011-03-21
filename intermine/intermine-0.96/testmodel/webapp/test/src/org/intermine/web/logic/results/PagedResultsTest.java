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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.intermine.api.InterMineAPI;
import org.intermine.api.query.MainHelper;
import org.intermine.api.results.ResultElement;
import org.intermine.api.results.WebResults;
import org.intermine.api.results.flatouterjoins.MultiRowFirstValue;
import org.intermine.metadata.AttributeDescriptor;
import org.intermine.metadata.Model;
import org.intermine.model.testmodel.Company;
import org.intermine.model.testmodel.Department;
import org.intermine.model.testmodel.Employee;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.dummy.ObjectStoreDummyImpl;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.iql.IqlQuery;
import org.intermine.pathquery.Path;
import org.intermine.pathquery.PathQuery;
import org.intermine.pathquery.PathQueryBinding;
import org.intermine.util.DynamicUtil;

public class PagedResultsTest extends TestCase
{
    public PagedResultsTest(String arg) {
        super(arg);
    }

    private ObjectStoreDummyImpl os;
    private IqlQuery fq;
    private Model model;
    private List columnPath;
    private Map pathToQueryNode;
    private Map classKeys;
    private PathQuery pathQuery;
    private InterMineAPI im;

    public void setUp() throws Exception {

        os = new ObjectStoreDummyImpl();
        os.setResultsSize(15);
        fq = new IqlQuery("select c1, c2, d1, d2 from Company as c1, Company as c2, Department as d1, Department as d2", "org.intermine.model.testmodel");
        model = Model.getInstanceByName("testmodel");
        columnPath = new ArrayList();
        columnPath.addAll(Arrays.asList(new Object[] {new Path(model,"Company.name"), new Path(model,"Department.name"), new Path(model,"Company.name"), new Path(model,"Department.name")}));
        pathToQueryNode = new HashMap();
        pathToQueryNode.put("Company.name", new QueryField(new QueryClass(Company.class), "name"));
        pathToQueryNode.put("Department.name", new QueryField(new QueryClass(Department.class) ,"name"));
        classKeys = new HashMap();
        List defaultClassKeys = new ArrayList();
        defaultClassKeys.add(new AttributeDescriptor("name","java.lang.String"));
        classKeys.put("Employee", defaultClassKeys);
        classKeys.put("Department", defaultClassKeys);
        classKeys.put("Company", defaultClassKeys);
        pathQuery = new PathQuery(model);
        im = new InterMineAPI(os, null, null, null, null, null, null);
    }

    private PagedTable getEmptyResults() throws Exception {
        os.setResultsSize(0);
        Results results = os.execute(fq.toQuery(), 20, true, true, true);
        try {
            results.get(0);
        } catch (IndexOutOfBoundsException e) {
        }
        WebResults webResults = new WebResults(im, pathQuery, results, pathToQueryNode, null);
        return new PagedTable(webResults);
    }

    private PagedTable getExactResults() throws Exception {
        Results results = os.execute(fq.toQuery(), 20, true, true, true);
        // Make sure we definitely know the end
        results.get(0);
        WebResults webResults = new WebResults(im, pathQuery, results, pathToQueryNode, null);
        return new PagedTable(webResults);
    }

    private PagedTable getEstimateTooHighResults() throws Exception {
        os.setEstimatedResultsSize(25);
        Results results = os.execute(fq.toQuery(), 1, true, true, true);
        WebResults webResults = new WebResults(im, pathQuery, results, pathToQueryNode, null);
        return new PagedTable(webResults);
    }

    private PagedTable getEstimateTooLowResults() throws Exception {
        os.setEstimatedResultsSize(10);
        Results results = os.execute(fq.toQuery(), 1, true, true, true);
        WebResults webResults = new WebResults(im, pathQuery, results, pathToQueryNode, null);
        return new PagedTable(webResults);
    }

    public void testSizeExact() throws Exception {
        PagedTable dr = getExactResults();
        dr.setPageSize(10);
        assertEquals(15, dr.getEstimatedSize());
    }

    public void testSizeHigh() throws Exception {
        PagedTable dr = getEstimateTooHighResults();
        dr.setPageSize(10);
        assertEquals(25, dr.getEstimatedSize());
    }

    public void testGetIndexForColumn() throws Exception {
        Map queries = readQueries();
        Map headers = new HashMap();
        Map expected = new HashMap();
        Map results = new HashMap();

        Employee e1 = new Employee();
        e1.setName("employee1");
        e1.setId(new Integer(101));

        Employee e2 = new Employee();
        e2.setName("employee2");
        e2.setId(new Integer(102));

        Department d1 = new Department();
        d1.setName("DepartmentA1");
        d1.setId(new Integer(201));

        Company c1 = (Company) DynamicUtil.instantiateObject("org.intermine.model.testmodel.Company", null);
        c1.setName("Company1");
        c1.setId(new Integer(301));

        e1.setDepartment(d1);
        d1.setCompany(c1);
        d1.setEmployees(new HashSet(toList(new Object [] {e2})));

        ObjectStore os = new ObjectStoreDummyImpl();
        results.put("employeeName", toList(new Object[][] { { e1 } }));
        expected.put("employeeName", Collections.singletonList(Collections.singletonList(new MultiRowFirstValue(new ResultElement(e1, new Path(model, "Employee.name"), false), 1))));
        headers.put("employeeName", toList(new Object[] {new Path(model, "Employee.name")}));

        results.put("employeeDepartmentName", toList(new Object[][] { { e1, d1 } }));
        expected.put("employeeDepartmentName", Collections.singletonList(Arrays.asList(new MultiRowFirstValue(new ResultElement(e1, new Path(model, "Employee.name"), false), 1),
                        new MultiRowFirstValue(new ResultElement(d1, new Path(model, "Department.name"), false), 1))));
        headers.put("employeeDepartmentName", toList(new Object[] {new Path(model, "Employee.name"), new Path(model, "Employee.department.name")}));

        results.put("employeeDepartmentCompany", toList(new Object[][] { { e1, d1, c1 } }));
        expected.put("employeeDepartmentCompany", Collections.singletonList(Arrays.asList(new MultiRowFirstValue(new ResultElement(e1, new Path(model, "Employee.name"), false), 1),
                        new MultiRowFirstValue(new ResultElement(d1, new Path(model, "Department.name"), false), 1),
                        new MultiRowFirstValue(new ResultElement(c1, new Path(model, "Company.name"), false), 1))));
        headers.put("employeeDepartmentCompany", toList(new Object[] {new Path(model, "Employee.name"), new Path(model, "Employee.department.name"), new Path(model, "Employee.department.company.name")}));

        results.put("employeeCompany", toList(new Object[][] { { e1, c1 } }));
        expected.put("employeeCompany", Collections.singletonList(Arrays.asList(new MultiRowFirstValue(new ResultElement(e1, new Path(model, "Employee.name"), false), 1),
                        new MultiRowFirstValue(new ResultElement(c1, new Path(model, "Company.name"), false), 1))));
        headers.put("employeeCompany", toList(new Object[] {new Path(model, "Employee.name"), new Path(model, "Employee.department.company.name")}));

//        results.put("employeeDepartmentEmployees", toList(new Object[][] { { e1, d1, e2 } } ));
//        expected.put("employeeDepartmentEmployees", Arrays.asList(new Object[] {new ResultElement(os,e1.getName(),e1.getId(), "Employee", new Path(model, "Employee.name"), false),
//                new ResultElement(os, e1.getDepartment().getName(),e1.getDepartment().getId(),"Department", new Path(model, "Department.name"), false),
//                new ResultElement(os, Employee (e1.getDepartment().getEmployees().iterator().next()).getName(),e2.getId(),"Employee", new Path(model, "Employee.name"), false)}));
//        headers.put("employeeDepartmentEmployees", toList(new Object[] {new Path(model, "Employee"), new Path(model, "Employee.department"), new Path(model, "Employee.department.employees")}));

        // check all queries, fail if no expected values set
        Iterator queryIter = queries.entrySet().iterator();
        while (queryIter.hasNext()) {
            Map.Entry entry = (Map.Entry) queryIter.next();
            String queryName = (String) entry.getKey();
            if (!expected.containsKey(queryName)) {
                continue;
            }

            PathQuery pq = (PathQuery) entry.getValue();
            Map pathToQueryNode = new HashMap();
            Query q = MainHelper.makeQuery(pq, new HashMap(), pathToQueryNode, null, null);
            Results r = new DummyResults(os, q, (List) results.get(queryName));
//            ((Object) pq).setViewPaths((List) headers.get(queryName));
            WebResults webResults = new WebResults(im, pq, r, pathToQueryNode, null);
            PagedTable pr = new PagedTable(webResults);
            assertEquals("Failed with query: " + queryName + ". ", (List) expected.get(queryName), (List) pr.getRows().get(0));
         }
    }

    private class DummyResults extends Results {
        List rows;

        public DummyResults(ObjectStore os, Query query, List rows) {
            super(query, os, ObjectStore.SEQUENCE_IGNORE);
            this.rows = rows;
//            minSize = rows.size();
//            maxSize = rows.size();
        }

        public Object get(int index) {
            return rows.get(index);
        }
    }

    private List toList(Object array[][]) {
        List rows = new ArrayList();
        for(int i=0;i<array.length;i++) {
            rows.add(new ResultsRow(Arrays.asList((Object[])array[i])));
        }
        return rows;
    }

    private List toList(Object array[]) {
        List rows = new ArrayList();
        for(int i=0;i<array.length;i++) {
            rows.add(array[i]);
        }
        return rows;
    }

    private Map readQueries() throws Exception {
        // TODO PagedResultsTest.xml is a copy of MainHelperTest.xml, it can be removed if this
        // test is ever moved into intermine/web/test
        InputStream is = getClass().getClassLoader().getResourceAsStream("PagedResultsTest.xml");
        return PathQueryBinding.unmarshal(new InputStreamReader(is), 1);
    }

//     public void testSizeLow() throws Exception {
//         PagedTable dr = getEstimateTooLowResults();
//         dr.setPageSize(10);
//         // Calling size() affects the estimate as it tries to fetch
//         // more rows.  I think the best thing to do here is to check
//         // that the size is greater than 10 and less than 15 to prove
//         // that the size is not stuck at the estimate
//         assertTrue(dr.getSize() > 10);
//         assertTrue(dr.getSize() <= 15);
//     }

     public void testSizeEmpty() throws Exception {
         PagedTable dr = getEmptyResults();
         dr.setPageSize(10);
         assertEquals(0, dr.getEstimatedSize());
     }

//     public void testEndExact() throws Exception {
//         // At the beginning
//         PagedTable dr = getExactResults();
//         dr.setPageSize(10);
//         dr.setStartIndex(0);
//         assertEquals(9, dr.getEndIndex());
//         // Abutting the end
//         dr = getExactResults();
//         dr.setPageSize(10);
//         dr.setStartIndex(5);
//         assertEquals(14, dr.getEndIndex());
//         // Overlapping the end
//         dr = getExactResults();
//         dr.setPageSize(10);
//         dr.setStartIndex(10);
//         assertEquals(14, dr.getEndIndex());
//     }

//     public void testEndHigh() throws Exception {
//         // At the beginning
//         PagedTable dr = getEstimateTooHighResults();
//         dr.setPageSize(10);
//         dr.setStartIndex(0);
//         assertEquals(9, dr.getEndIndex());
//         // Abutting the end
//         dr = getEstimateTooHighResults();
//         dr.setPageSize(10);
//         dr.setStartIndex(5);
//         assertEquals(14, dr.getEndIndex());
//         // Overlapping the end
//         dr = getEstimateTooHighResults();
//         dr.setPageSize(10);
//         dr.setStartIndex(10);
//         assertEquals(14, dr.getEndIndex());
//     }

//     public void testEndLow() throws Exception {
//         // At the beginning
//         PagedTable dr = getEstimateTooLowResults();
//         dr.setPageSize(10);
//         dr.setStartIndex(0);
//         assertEquals(9, dr.getEndIndex());
//         // Abutting the end
//         dr = getEstimateTooLowResults();
//         dr.setPageSize(10);
//         dr.setStartIndex(5);
//         assertEquals(14, dr.getEndIndex());
//         // Overlapping the end
//         dr = getEstimateTooLowResults();
//         dr.setPageSize(10);
//         dr.setStartIndex(10);
//         assertEquals(14, dr.getEndIndex());
//     }

    // For the moment the end is -1 if the underlying results is empty
    // Anything using PagedTable should call getSize() first
//     public void testEndEmpty() throws Exception {
//         PagedTable dr = getEmptyResults();
//         dr.setPageSize(10);
//         assertEquals(-1, dr.getEndIndex());
//     }

//     public void testButtonsExact() throws Exception {
//         PagedTable dr = getExactResults();
//         dr.setPageSize(10);
//         // At the beginning
//         dr.setStartRow(0);
//         assertTrue(dr.isFirstPage());
//         assertFalse(dr.isLastPage());
//         // Abutting the end
//         dr.setStartRow(5);
//         assertFalse(dr.isFirstPage());
//         assertTrue(dr.isLastPage());
//         // Overlapping the end
//         dr.setStartRow(10);
//         assertFalse(dr.isFirstPage());
//         assertTrue(dr.isLastPage());
//     }

//     public void testButtonsHigh() throws Exception {
//         PagedTable dr = getEstimateTooHighResults();
//         dr.setPageSize(10);
//         // At the beginning
//         dr.setStartRow(0);
//         assertTrue(dr.isFirstPage());
//         assertFalse(dr.isLastPage());
//         // Abutting the end
//         dr.setStartRow(5);
//         assertFalse(dr.isFirstPage());
//         assertTrue(dr.isLastPage());
//         // Overlapping the end
//         dr.setStartRow(10);
//         assertFalse(dr.isFirstPage());
//         assertTrue(dr.isLastPage());
//     }

//     public void testButtonsLow() throws Exception {
//         PagedTable dr = getEstimateTooLowResults();
//         dr.setPageSize(10);
//         // At the beginning (this abuts the estimated end)
//         dr.setStartRow(0);
//         assertTrue(dr.isFirstPage());
//         assertFalse(dr.isLastPage());
//         // Abutting the end
//         dr.setStartRow(5);
//         assertFalse(dr.isFirstPage());
//         assertTrue(dr.isLastPage());
//         // Overlapping the end
//         dr.setStartRow(10);
//         assertFalse(dr.isFirstPage());
//         assertTrue(dr.isLastPage());
//
//     }

}
