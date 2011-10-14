package org.intermine.model.fulldata;

import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.intermine.NotXmlParser;
import org.intermine.objectstore.intermine.NotXmlRenderer;
import org.intermine.objectstore.proxy.ProxyReference;
import org.intermine.util.StringConstructor;
import org.intermine.util.TypeUtil;

public class ReferenceList implements org.intermine.model.InterMineObject
{
    // Attr: org.intermine.model.fulldata.ReferenceList.name
    protected java.lang.String name;
    public java.lang.String getName() { return name; }
    public void setName(final java.lang.String name) { this.name = name; }

    // Attr: org.intermine.model.fulldata.ReferenceList.refIds
    protected java.lang.String refIds;
    public java.lang.String getRefIds() { return refIds; }
    public void setRefIds(final java.lang.String refIds) { this.refIds = refIds; }

    // Ref: org.intermine.model.fulldata.ReferenceList.item
    protected org.intermine.model.InterMineObject item;
    public org.intermine.model.fulldata.Item getItem() { if (item instanceof org.intermine.objectstore.proxy.ProxyReference) { return ((org.intermine.model.fulldata.Item) ((org.intermine.objectstore.proxy.ProxyReference) item).getObject()); }; return (org.intermine.model.fulldata.Item) item; }
    public void setItem(final org.intermine.model.fulldata.Item item) { this.item = item; }
    public void proxyItem(final org.intermine.objectstore.proxy.ProxyReference item) { this.item = item; }
    public org.intermine.model.InterMineObject proxGetItem() { return item; }

    // Attr: org.intermine.model.InterMineObject.id
    protected java.lang.Integer id;
    public java.lang.Integer getId() { return id; }
    public void setId(final java.lang.Integer id) { this.id = id; }

    @Override public boolean equals(Object o) { return (o instanceof ReferenceList && id != null) ? id.equals(((ReferenceList)o).getId()) : this == o; }
    @Override public int hashCode() { return (id != null) ? id.hashCode() : super.hashCode(); }
    @Override public String toString() { return "ReferenceList [id=\"" + id + "\", item=" + (item == null ? "null" : (item.getId() == null ? "no id" : item.getId().toString())) + ", name=\"" + name + "\", refIds=\"" + refIds + "\"]"; }
    public Object getFieldValue(final String fieldName) throws IllegalAccessException {
        if ("name".equals(fieldName)) {
            return name;
        }
        if ("refIds".equals(fieldName)) {
            return refIds;
        }
        if ("item".equals(fieldName)) {
            if (item instanceof ProxyReference) {
                return ((ProxyReference) item).getObject();
            } else {
                return item;
            }
        }
        if ("id".equals(fieldName)) {
            return id;
        }
        if (!org.intermine.model.fulldata.ReferenceList.class.equals(getClass())) {
            return TypeUtil.getFieldValue(this, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public Object getFieldProxy(final String fieldName) throws IllegalAccessException {
        if ("name".equals(fieldName)) {
            return name;
        }
        if ("refIds".equals(fieldName)) {
            return refIds;
        }
        if ("item".equals(fieldName)) {
            return item;
        }
        if ("id".equals(fieldName)) {
            return id;
        }
        if (!org.intermine.model.fulldata.ReferenceList.class.equals(getClass())) {
            return TypeUtil.getFieldProxy(this, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public void setFieldValue(final String fieldName, final Object value) {
        if ("name".equals(fieldName)) {
            name = (java.lang.String) value;
        } else if ("refIds".equals(fieldName)) {
            refIds = (java.lang.String) value;
        } else if ("item".equals(fieldName)) {
            item = (org.intermine.model.InterMineObject) value;
        } else if ("id".equals(fieldName)) {
            id = (java.lang.Integer) value;
        } else {
            if (!org.intermine.model.fulldata.ReferenceList.class.equals(getClass())) {
                TypeUtil.setFieldValue(this, fieldName, value);
                return;
            }
            throw new IllegalArgumentException("Unknown field " + fieldName);
        }
    }
    public Class<?> getFieldType(final String fieldName) {
        if ("name".equals(fieldName)) {
            return java.lang.String.class;
        }
        if ("refIds".equals(fieldName)) {
            return java.lang.String.class;
        }
        if ("item".equals(fieldName)) {
            return org.intermine.model.fulldata.Item.class;
        }
        if ("id".equals(fieldName)) {
            return java.lang.Integer.class;
        }
        if (!org.intermine.model.fulldata.ReferenceList.class.equals(getClass())) {
            return TypeUtil.getFieldType(org.intermine.model.fulldata.ReferenceList.class, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public StringConstructor getoBJECT() {
        if (!org.intermine.model.fulldata.ReferenceList.class.equals(getClass())) {
            return NotXmlRenderer.render(this);
        }
        StringConstructor sb = new StringConstructor();
        sb.append("$_^org.intermine.model.fulldata.ReferenceList");
        if (name != null) {
            sb.append("$_^aname$_^");
            String string = name;
            while (string != null) {
                int delimPosition = string.indexOf("$_^");
                if (delimPosition == -1) {
                    sb.append(string);
                    string = null;
                } else {
                    sb.append(string.substring(0, delimPosition + 3));
                    sb.append("d");
                    string = string.substring(delimPosition + 3);
                }
            }
        }
        if (refIds != null) {
            sb.append("$_^arefIds$_^");
            String string = refIds;
            while (string != null) {
                int delimPosition = string.indexOf("$_^");
                if (delimPosition == -1) {
                    sb.append(string);
                    string = null;
                } else {
                    sb.append(string.substring(0, delimPosition + 3));
                    sb.append("d");
                    string = string.substring(delimPosition + 3);
                }
            }
        }
        if (item != null) {
            sb.append("$_^ritem$_^").append(item.getId());
        }
        if (id != null) {
            sb.append("$_^aid$_^").append(id);
        }
        return sb;
    }
    public void setoBJECT(String notXml, ObjectStore os) {
        setoBJECT(NotXmlParser.SPLITTER.split(notXml), os);
    }
    public void setoBJECT(final String[] notXml, final ObjectStore os) {
        if (!org.intermine.model.fulldata.ReferenceList.class.equals(getClass())) {
            throw new IllegalStateException("Class " + getClass().getName() + " does not match code (org.intermine.model.fulldata.ReferenceList)");
        }
        for (int i = 2; i < notXml.length;) {
            int startI = i;
            if ((i < notXml.length) && "aname".equals(notXml[i])) {
                i++;
                StringBuilder string = null;
                while ((i + 1 < notXml.length) && (notXml[i + 1].charAt(0) == 'd')) {
                    if (string == null) string = new StringBuilder(notXml[i]);
                    i++;
                    string.append("$_^").append(notXml[i].substring(1));
                }
                name = string == null ? notXml[i] : string.toString();
                i++;
            }
            if ((i < notXml.length) && "arefIds".equals(notXml[i])) {
                i++;
                StringBuilder string = null;
                while ((i + 1 < notXml.length) && (notXml[i + 1].charAt(0) == 'd')) {
                    if (string == null) string = new StringBuilder(notXml[i]);
                    i++;
                    string.append("$_^").append(notXml[i].substring(1));
                }
                refIds = string == null ? notXml[i] : string.toString();
                i++;
            }
            if ((i < notXml.length) &&"ritem".equals(notXml[i])) {
                i++;
                item = new ProxyReference(os, Integer.valueOf(notXml[i]), org.intermine.model.fulldata.Item.class);
                i++;
            };
            if ((i < notXml.length) && "aid".equals(notXml[i])) {
                i++;
                id = Integer.valueOf(notXml[i]);
                i++;
            }
            if (startI == i) {
                throw new IllegalArgumentException("Unknown field " + notXml[i]);
            }
        }
    }
    public void addCollectionElement(final String fieldName, final org.intermine.model.InterMineObject element) {
        {
            if (!org.intermine.model.fulldata.ReferenceList.class.equals(getClass())) {
                TypeUtil.addCollectionElement(this, fieldName, element);
                return;
            }
            throw new IllegalArgumentException("Unknown collection " + fieldName);
        }
    }
    public Class<?> getElementType(final String fieldName) {
        if (!org.intermine.model.fulldata.ReferenceList.class.equals(getClass())) {
            return TypeUtil.getElementType(org.intermine.model.fulldata.ReferenceList.class, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
}
