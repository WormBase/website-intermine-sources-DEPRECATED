package org.intermine.model.userprofile;

import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.intermine.NotXmlParser;
import org.intermine.objectstore.intermine.NotXmlRenderer;
import org.intermine.objectstore.proxy.ProxyReference;
import org.intermine.util.StringConstructor;
import org.intermine.util.TypeUtil;

public class SavedBag implements org.intermine.model.InterMineObject
{
    // Attr: org.intermine.model.userprofile.SavedBag.name
    protected java.lang.String name;
    public java.lang.String getName() { return name; }
    public void setName(final java.lang.String name) { this.name = name; }

    // Attr: org.intermine.model.userprofile.SavedBag.type
    protected java.lang.String type;
    public java.lang.String getType() { return type; }
    public void setType(final java.lang.String type) { this.type = type; }

    // Attr: org.intermine.model.userprofile.SavedBag.description
    protected java.lang.String description;
    public java.lang.String getDescription() { return description; }
    public void setDescription(final java.lang.String description) { this.description = description; }

    // Attr: org.intermine.model.userprofile.SavedBag.dateCreated
    protected java.util.Date dateCreated;
    public java.util.Date getDateCreated() { return dateCreated; }
    public void setDateCreated(final java.util.Date dateCreated) { this.dateCreated = dateCreated; }

    // Attr: org.intermine.model.userprofile.SavedBag.osbId
    protected int osbId;
    public int getOsbId() { return osbId; }
    public void setOsbId(final int osbId) { this.osbId = osbId; }

    // Attr: org.intermine.model.userprofile.SavedBag.state
    protected java.lang.String state;
    public java.lang.String getState() { return state; }
    public void setState(final java.lang.String state) { this.state = state; }

    // Ref: org.intermine.model.userprofile.SavedBag.userProfile
    protected org.intermine.model.InterMineObject userProfile;
    public org.intermine.model.userprofile.UserProfile getUserProfile() { if (userProfile instanceof org.intermine.objectstore.proxy.ProxyReference) { return ((org.intermine.model.userprofile.UserProfile) ((org.intermine.objectstore.proxy.ProxyReference) userProfile).getObject()); }; return (org.intermine.model.userprofile.UserProfile) userProfile; }
    public void setUserProfile(final org.intermine.model.userprofile.UserProfile userProfile) { this.userProfile = userProfile; }
    public void proxyUserProfile(final org.intermine.objectstore.proxy.ProxyReference userProfile) { this.userProfile = userProfile; }
    public org.intermine.model.InterMineObject proxGetUserProfile() { return userProfile; }

    // Attr: org.intermine.model.InterMineObject.id
    protected java.lang.Integer id;
    public java.lang.Integer getId() { return id; }
    public void setId(final java.lang.Integer id) { this.id = id; }

    @Override public boolean equals(Object o) { return (o instanceof SavedBag && id != null) ? id.equals(((SavedBag)o).getId()) : this == o; }
    @Override public int hashCode() { return (id != null) ? id.hashCode() : super.hashCode(); }
    @Override public String toString() { return "SavedBag [dateCreated=\"" + dateCreated + "\", description=\"" + description + "\", id=\"" + id + "\", name=\"" + name + "\", osbId=\"" + osbId + "\", state=\"" + state + "\", type=\"" + type + "\", userProfile=" + (userProfile == null ? "null" : (userProfile.getId() == null ? "no id" : userProfile.getId().toString())) + "]"; }
    public Object getFieldValue(final String fieldName) throws IllegalAccessException {
        if ("name".equals(fieldName)) {
            return name;
        }
        if ("type".equals(fieldName)) {
            return type;
        }
        if ("description".equals(fieldName)) {
            return description;
        }
        if ("dateCreated".equals(fieldName)) {
            return dateCreated;
        }
        if ("osbId".equals(fieldName)) {
            return Integer.valueOf(osbId);
        }
        if ("state".equals(fieldName)) {
            return state;
        }
        if ("userProfile".equals(fieldName)) {
            if (userProfile instanceof ProxyReference) {
                return ((ProxyReference) userProfile).getObject();
            } else {
                return userProfile;
            }
        }
        if ("id".equals(fieldName)) {
            return id;
        }
        if (!org.intermine.model.userprofile.SavedBag.class.equals(getClass())) {
            return TypeUtil.getFieldValue(this, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public Object getFieldProxy(final String fieldName) throws IllegalAccessException {
        if ("name".equals(fieldName)) {
            return name;
        }
        if ("type".equals(fieldName)) {
            return type;
        }
        if ("description".equals(fieldName)) {
            return description;
        }
        if ("dateCreated".equals(fieldName)) {
            return dateCreated;
        }
        if ("osbId".equals(fieldName)) {
            return Integer.valueOf(osbId);
        }
        if ("state".equals(fieldName)) {
            return state;
        }
        if ("userProfile".equals(fieldName)) {
            return userProfile;
        }
        if ("id".equals(fieldName)) {
            return id;
        }
        if (!org.intermine.model.userprofile.SavedBag.class.equals(getClass())) {
            return TypeUtil.getFieldProxy(this, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public void setFieldValue(final String fieldName, final Object value) {
        if ("name".equals(fieldName)) {
            name = (java.lang.String) value;
        } else if ("type".equals(fieldName)) {
            type = (java.lang.String) value;
        } else if ("description".equals(fieldName)) {
            description = (java.lang.String) value;
        } else if ("dateCreated".equals(fieldName)) {
            dateCreated = (java.util.Date) value;
        } else if ("osbId".equals(fieldName)) {
            osbId = ((Integer) value).intValue();
        } else if ("state".equals(fieldName)) {
            state = (java.lang.String) value;
        } else if ("userProfile".equals(fieldName)) {
            userProfile = (org.intermine.model.InterMineObject) value;
        } else if ("id".equals(fieldName)) {
            id = (java.lang.Integer) value;
        } else {
            if (!org.intermine.model.userprofile.SavedBag.class.equals(getClass())) {
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
        if ("type".equals(fieldName)) {
            return java.lang.String.class;
        }
        if ("description".equals(fieldName)) {
            return java.lang.String.class;
        }
        if ("dateCreated".equals(fieldName)) {
            return java.util.Date.class;
        }
        if ("osbId".equals(fieldName)) {
            return Integer.TYPE;
        }
        if ("state".equals(fieldName)) {
            return java.lang.String.class;
        }
        if ("userProfile".equals(fieldName)) {
            return org.intermine.model.userprofile.UserProfile.class;
        }
        if ("id".equals(fieldName)) {
            return java.lang.Integer.class;
        }
        if (!org.intermine.model.userprofile.SavedBag.class.equals(getClass())) {
            return TypeUtil.getFieldType(org.intermine.model.userprofile.SavedBag.class, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
    public StringConstructor getoBJECT() {
        if (!org.intermine.model.userprofile.SavedBag.class.equals(getClass())) {
            return NotXmlRenderer.render(this);
        }
        StringConstructor sb = new StringConstructor();
        sb.append("$_^org.intermine.model.userprofile.SavedBag");
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
        if (type != null) {
            sb.append("$_^atype$_^");
            String string = type;
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
        if (description != null) {
            sb.append("$_^adescription$_^");
            String string = description;
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
        if (dateCreated != null) {
            sb.append("$_^adateCreated$_^").append(dateCreated.getTime());
        }
        sb.append("$_^aosbId$_^").append(osbId);
        if (state != null) {
            sb.append("$_^astate$_^");
            String string = state;
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
        if (userProfile != null) {
            sb.append("$_^ruserProfile$_^").append(userProfile.getId());
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
        if (!org.intermine.model.userprofile.SavedBag.class.equals(getClass())) {
            throw new IllegalStateException("Class " + getClass().getName() + " does not match code (org.intermine.model.userprofile.SavedBag)");
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
            if ((i < notXml.length) && "atype".equals(notXml[i])) {
                i++;
                StringBuilder string = null;
                while ((i + 1 < notXml.length) && (notXml[i + 1].charAt(0) == 'd')) {
                    if (string == null) string = new StringBuilder(notXml[i]);
                    i++;
                    string.append("$_^").append(notXml[i].substring(1));
                }
                type = string == null ? notXml[i] : string.toString();
                i++;
            }
            if ((i < notXml.length) && "adescription".equals(notXml[i])) {
                i++;
                StringBuilder string = null;
                while ((i + 1 < notXml.length) && (notXml[i + 1].charAt(0) == 'd')) {
                    if (string == null) string = new StringBuilder(notXml[i]);
                    i++;
                    string.append("$_^").append(notXml[i].substring(1));
                }
                description = string == null ? notXml[i] : string.toString();
                i++;
            }
            if ((i < notXml.length) && "adateCreated".equals(notXml[i])) {
                i++;
                dateCreated = new java.util.Date(Long.parseLong(notXml[i]));
                i++;
            }
            if ((i < notXml.length) && "aosbId".equals(notXml[i])) {
                i++;
                osbId = Integer.parseInt(notXml[i]);
                i++;
            }
            if ((i < notXml.length) && "astate".equals(notXml[i])) {
                i++;
                StringBuilder string = null;
                while ((i + 1 < notXml.length) && (notXml[i + 1].charAt(0) == 'd')) {
                    if (string == null) string = new StringBuilder(notXml[i]);
                    i++;
                    string.append("$_^").append(notXml[i].substring(1));
                }
                state = string == null ? notXml[i] : string.toString();
                i++;
            }
            if ((i < notXml.length) &&"ruserProfile".equals(notXml[i])) {
                i++;
                userProfile = new ProxyReference(os, Integer.valueOf(notXml[i]), org.intermine.model.userprofile.UserProfile.class);
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
            if (!org.intermine.model.userprofile.SavedBag.class.equals(getClass())) {
                TypeUtil.addCollectionElement(this, fieldName, element);
                return;
            }
            throw new IllegalArgumentException("Unknown collection " + fieldName);
        }
    }
    public Class<?> getElementType(final String fieldName) {
        if (!org.intermine.model.userprofile.SavedBag.class.equals(getClass())) {
            return TypeUtil.getElementType(org.intermine.model.userprofile.SavedBag.class, fieldName);
        }
        throw new IllegalArgumentException("Unknown field " + fieldName);
    }
}
