package org.intermine.metadata;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashSet;

public class ClassDescriptorTest extends TestCase
{
    private static final Set EMPTY_SET = Collections.EMPTY_SET;
    private static final String ENDL = System.getProperty("line.separator");
    
    public ClassDescriptorTest(String arg) {
        super(arg);
    }

    public void testConstructNameNull() {
        try {
            ClassDescriptor cld = new ClassDescriptor("", null, false,
                                                      new HashSet(), new HashSet(), new HashSet());
            fail("Expected: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

    }

    public void testSetModel() throws Exception {
        ClassDescriptor cld = new ClassDescriptor("package.name.Class1", null, false,
                                                  new HashSet(), new HashSet(), new HashSet());
        try {
            Model model1 = new Model("model1", "package.name", Collections.singleton(cld));
        } catch (IllegalStateException e) {
            fail("Model should have been set correctly");
        }

        try {
            Model model2 = new Model("model2", "package.name", Collections.singleton(cld));
            fail("Model already set, expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    public void testInterfaceDescriptors() throws Exception {
        ClassDescriptor int1 = new ClassDescriptor("package.name.Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor int2 = new ClassDescriptor("package.name.Interface2", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("package.name.Class1", "package.name.Interface1 package.name.Interface2", false,
                                                  new HashSet(), new HashSet(), new HashSet());

        Set clds = new HashSet(Arrays.asList(new Object[] {int1, int2, cld}));
        Model model = new Model("test", "package.name", clds);

        Set interfaces = new HashSet(Arrays.asList(new Object[] {int1, int2}));
        assertEquals(interfaces, cld.getSuperDescriptors());

    }

    public void testImplementsNotExists() throws Exception {
        // construct class implementing Interface3 which does not exist
        ClassDescriptor int1 = new ClassDescriptor("package.name.Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor int2 = new ClassDescriptor("package.name.Interface2", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("package.name.Class1", "package.name.Interface1 package.name.Interface3", false,
                                                  new HashSet(), new HashSet(), new HashSet());

        Set clds = new HashSet(Arrays.asList(new Object[] {int1, int2, cld}));
        try {
            Model model = new Model("test", "package.name", clds);
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testInterfaceNotImplemented() throws Exception {
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("model", "package.name", Collections.singleton(cld1));
        assertTrue(cld1.getSubDescriptors().size() == 0);
    }

    public void testSuperClassExists() throws Exception {
        ClassDescriptor superCld = new ClassDescriptor("package.name.superCld", null, false,
                                                       new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("package.name.cld", "package.name.superCld", false,  new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("test", "package.name", new HashSet(Arrays.asList(new Object[] {cld, superCld})));
        assertEquals(superCld, cld.getSuperclassDescriptor());
    }

    public void testSuperClassNotExists() throws Exception {
        ClassDescriptor superCld = new ClassDescriptor("package.name.superCld", null, false,
                                                       new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("package.name.cld", "package.name.anotherCld", false,  new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("test", "package.name", new HashSet(Arrays.asList(new Object[] {cld, superCld})));
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testSuperClassWrongType() throws Exception {
        // test where superclass is not an interface, subclass is
        ClassDescriptor superCld = new ClassDescriptor("package.name.superCld", null, false,
                                                       new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor interface1 = new ClassDescriptor("package.name.interface1", "package.name.superCld", true,  new HashSet(), new HashSet(), new HashSet());
        try {
            Model model2 = new Model("test", "package.name", new HashSet(Arrays.asList(new Object[] {interface1, superCld})));
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultipleSuperClass() throws Exception {
        ClassDescriptor superCld1 = new ClassDescriptor("package.name.superCld1", null, false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor superCld2 = new ClassDescriptor("package.name.superCld2", null, false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("package.name.cld", "package.name.superCld1 package.name.superCld2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("test", "package.name", new HashSet(Arrays.asList(new Object[] {cld, superCld1, superCld2})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testFieldDescriptorByName() throws Exception {
        Set attributes = getAttributes();
        Set refs = getReferences();
        Set cols = getCollections();
        ClassDescriptor cld = new ClassDescriptor("package.name.Class1", null, false,
                                                  attributes, refs, cols);
        cld.setAllFieldDescriptors();
        assertNotNull(cld.getFieldDescriptorByName("atd1"));
        assertNotNull(cld.getFieldDescriptorByName("atd2"));
        assertNotNull(cld.getFieldDescriptorByName("rfd1"));
        assertNotNull(cld.getFieldDescriptorByName("rfd2"));
        assertNotNull(cld.getFieldDescriptorByName("cld1"));
        assertNotNull(cld.getFieldDescriptorByName("cld2"));
    }

    public void testGetAllAttributeDescriptors() throws Exception {
        // three superclass levels with one attribute each, getAllAttributeDescriptors on cld3 should return all 3
        AttributeDescriptor atb1 = new AttributeDescriptor("att1", "java.lang.String");
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, false, Collections.singleton(atb1), new HashSet(), new HashSet());
        AttributeDescriptor atb2 = new AttributeDescriptor("att2", "java.lang.String");
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", "package.name.Class1", false, Collections.singleton(atb2), new HashSet(), new HashSet());
        AttributeDescriptor atb3 = new AttributeDescriptor("att3", "java.lang.String");
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class2", false, Collections.singleton(atb3), new HashSet(), new HashSet());

        Model model = new Model("test", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));

        FieldDescriptor id = cld3.getFieldDescriptorByName("id");
        Set atts = new HashSet(Arrays.asList(new Object[] {atb3, atb2, atb1, id}));

        assertEquals(atts, cld3.getAllAttributeDescriptors());
    }

    public void testGetSubDescriptors() throws Exception {
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", "package.name.Class1", false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1", false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld4 = new ClassDescriptor("package.name.Class4", "package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());

        Model model2 = new Model("test2", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3, cld4})));

        // getSubDescriptrors should just return direct subclasses (cld2, cld3)
        Set subs = new HashSet(Arrays.asList(new Object[] {cld2, cld3}));

        assertEquals(subs, cld1.getSubDescriptors());
    }

    public void testGetImplementorDescriptors() throws Exception {
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", "package.name.Interface1", false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Interface1", false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld4 = new ClassDescriptor("package.name.Class4", "package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());

        Model model2 = new Model("test2", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3, cld4})));

        // getImplementorDescriptrors should just return direct implementations (cld2, cld3)
        Set impls = new HashSet(Arrays.asList(new Object[] {cld2, cld3}));

        assertEquals(impls, cld1.getSubDescriptors());
    }

    public void testEquals() throws Exception {
        ClassDescriptor col1 = new ClassDescriptor("class1", null, true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col2 = new ClassDescriptor("class1", null, true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col3 = new ClassDescriptor("class1", "Super", true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col4 = new ClassDescriptor("class1", "Super", true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col5 = new ClassDescriptor("class1", "Interface", true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col6 = new ClassDescriptor("class1", null, false, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col7 = new ClassDescriptor("class1", null, true, Collections.singleton(new AttributeDescriptor("field", "int")), EMPTY_SET, EMPTY_SET);

        assertEquals(col1, col2);
        assertEquals(col1.hashCode(), col2.hashCode());
        assertFalse(col1.equals(col3));
        assertEquals(col3, col4);
        assertEquals(col3.hashCode(), col4.hashCode());
        assertFalse(col1.equals(col5));
        assertFalse(col1.equals(col6));
        assertFalse(col1.equals(col7));
    }

    public void testToString() throws Exception {
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, false, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class2 package.name.Interface1", false, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        String expected = "<class name=\"Class3\" extends=\"Class2 Interface1\" is-interface=\"false\">"
            + "</class>" + ENDL;
        Model model = new Model("test", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
        assertEquals(expected, cld3.toString());
    }

    public void testToString2() throws Exception {
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", "package.name.Interface1", false, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        String expected = "<class name=\"Class2\" extends=\"Interface1\" is-interface=\"false\">"
            + "</class>" + ENDL;
        Model model = new Model("test", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2})));
        assertEquals(expected, cld2.toString());
    }
    // ============================================

    private Set getAttributes() {
        Set attributes = new HashSet();
        AttributeDescriptor atd1 = new AttributeDescriptor("atd1", "java.lang.String");
        AttributeDescriptor atd2 = new AttributeDescriptor("atd2", "java.lang.Integer");
        attributes.add(atd1);
        attributes.add(atd2);
        return attributes;
    }

    private Set getReferences() {
        Set references = new HashSet();
        ReferenceDescriptor rfd1 = new ReferenceDescriptor("rfd1", "String", "reverse1");
        ReferenceDescriptor rfd2 = new ReferenceDescriptor("rfd2", "Integer", "reverse2");
        references.add(rfd1);
        references.add(rfd2);
        return references;
    }

    private Set getCollections() {
        Set collections = new HashSet();
        CollectionDescriptor cld1 = new CollectionDescriptor("cld1", "String", "reverse1");
        CollectionDescriptor cld2 = new CollectionDescriptor("cld2", "Integer", "reverse2");
        collections.add(cld1);
        collections.add(cld2);
        return collections;
    }

    public void testMultiInheritanceLegal() throws Exception {
        AttributeDescriptor atd1 = new AttributeDescriptor("atd1", "int");
        Set atds1 = new HashSet(Collections.singleton(atd1));
        AttributeDescriptor atd2 = new AttributeDescriptor("atd1", "int");
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, atds1, new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, atds2, new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("model", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
    }

    public void testMultiInheritanceLegalRef() throws Exception {
        ReferenceDescriptor atd1 = new ReferenceDescriptor("atd1", "package.name.Class2", null);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        ReferenceDescriptor atd2 = new ReferenceDescriptor("atd1", "package.name.Class2", null);
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), atds1, new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, new HashSet(), atds2, new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("model", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
        ReferenceDescriptor rd = cld3.getReferenceDescriptorByName("atd1", true);
        assertEquals("package.name.Class2", rd.getReferencedClassName());
    }

    public void testMultiInheritanceLegalCol() throws Exception {
        CollectionDescriptor atd1 = new CollectionDescriptor("atd1", "package.name.Class2", null);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        CollectionDescriptor atd2 = new CollectionDescriptor("atd1", "package.name.Class2", null);
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), new HashSet(), atds1);
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, new HashSet(), new HashSet(), atds2);
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("model", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
        ReferenceDescriptor rd = cld3.getCollectionDescriptorByName("atd1", true);
        assertEquals("package.name.Class2", rd.getReferencedClassName());
    }

    public void testMultiInheritanceIllegalAtt() throws Exception {
        AttributeDescriptor atd1 = new AttributeDescriptor("atd1", "int");
        Set atds1 = new HashSet(Collections.singleton(atd1));
        AttributeDescriptor atd2 = new AttributeDescriptor("atd1", "float");
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, atds1, new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, atds2, new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultiInheritanceIllegalAttRef() throws Exception {
        ReferenceDescriptor atd1 = new ReferenceDescriptor("atd1", "package.name.Class2", null);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        AttributeDescriptor atd2 = new AttributeDescriptor("atd1", "float");
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), atds1, new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, atds2, new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultiInheritanceIllegalAttCol() throws Exception {
        CollectionDescriptor atd1 = new CollectionDescriptor("atd1", "package.name.Class2", null);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        AttributeDescriptor atd2 = new AttributeDescriptor("atd1", "float");
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), new HashSet(), atds1);
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, atds2, new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultiInheritanceIllegalRefCol() throws Exception {
        CollectionDescriptor atd1 = new CollectionDescriptor("atd1", "package.name.Class2", null);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        ReferenceDescriptor atd2 = new ReferenceDescriptor("atd1", "package.name.Class2", null);
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), new HashSet(), atds1);
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, new HashSet(), atds2, new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", "package.name", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testFindSuperClassNames() throws Exception {
        String class1Name = "org.intermine.model.testmodel.Class1";
        String class3Name = "org.intermine.model.testmodel.Class3";
        String class2Name = "org.intermine.model.testmodel.Class2";
        String class4Name = "org.intermine.model.testmodel.Class4";
        ClassDescriptor c1 = new ClassDescriptor(class1Name, class3Name, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor c2 = new ClassDescriptor(class2Name, class3Name, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor c3 = new ClassDescriptor(class3Name, class4Name, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor c4 = new ClassDescriptor(class4Name, null, true, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("model", "org.intermine.model.testmodel", new HashSet(Arrays.asList(new Object[] {c1, c2, c3, c4})));

        Set<String> supers = new LinkedHashSet<String>();
        ClassDescriptor.findSuperClassNames(model, class1Name, supers);
        assertEquals(2, supers.size());
        Set<String> expected = new LinkedHashSet<String>();
        expected.add(class3Name);
        expected.add(class4Name);
        assertEquals(expected, supers);

        supers = new LinkedHashSet<String>();
        ClassDescriptor.findSuperClassNames(model, class2Name, supers);
        assertEquals(2, supers.size());

        supers = new LinkedHashSet<String>();
        ClassDescriptor.findSuperClassNames(model, class3Name, supers);
        assertEquals(1, supers.size());

        supers = new LinkedHashSet<String>();
        ClassDescriptor.findSuperClassNames(model, class4Name, supers);
        assertEquals(0, supers.size());
    }

    public void testClassInheritanceCompare() throws Exception {
        String class1Name = "org.intermine.model.testmodel.Class1";
        String class2Name = "org.intermine.model.testmodel.Class2";
        String class3Name = "org.intermine.model.testmodel.Class3";
        String class4Name = "org.intermine.model.testmodel.Class4";
        ClassDescriptor c1 = new ClassDescriptor(class1Name, class2Name + " " + class3Name, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor c2 = new ClassDescriptor(class2Name, class4Name, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor c3 = new ClassDescriptor(class3Name, class4Name, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor c4 = new ClassDescriptor(class4Name, null, true, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("model", "org.intermine.model.testmodel", new HashSet(Arrays.asList(new Object[] {c1, c2, c3, c4})));

        int comp = ClassDescriptor.classInheritanceCompare(model, class1Name, class2Name);
        assertEquals(1, comp);

        comp = ClassDescriptor.classInheritanceCompare(model, class2Name, class1Name);
        assertEquals(-1, comp);

        comp = ClassDescriptor.classInheritanceCompare(model, class1Name, class3Name);
        assertEquals(1, comp);

        comp = ClassDescriptor.classInheritanceCompare(model, class1Name, class4Name);
        assertEquals(1, comp);

        comp = ClassDescriptor.classInheritanceCompare(model, class1Name, class2Name);
        assertEquals(1, comp);

        comp = ClassDescriptor.classInheritanceCompare(model, class2Name, class3Name);
        assertEquals(0, comp);
    }

    public void testClassInheritanceCompare2() throws Exception {
        String class1Name = "org.intermine.model.testmodel.Class1";
        String class2Name = "org.intermine.model.testmodel.Class2";
        String class3Name = "org.intermine.model.testmodel.Class3";
        ClassDescriptor c1 = new ClassDescriptor(class1Name, class2Name + " " + class3Name, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor c2 = new ClassDescriptor(class2Name, class3Name, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor c3 = new ClassDescriptor(class3Name, null, true, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("model", "org.intermine.model.testmodel", new HashSet(Arrays.asList(new Object[] {c1, c2, c3})));

        int comp = ClassDescriptor.classInheritanceCompare(model, class1Name, class2Name);
        assertEquals(1, comp);

        comp = ClassDescriptor.classInheritanceCompare(model, class2Name, class1Name);
        assertEquals(-1, comp);

        comp = ClassDescriptor.classInheritanceCompare(model, class1Name, class3Name);
        assertEquals(1, comp);

        comp = ClassDescriptor.classInheritanceCompare(model, class2Name, class3Name);
        assertEquals(1, comp);
    }

}
