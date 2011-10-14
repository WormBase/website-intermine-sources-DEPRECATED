package org.intermine.web;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.intermine.api.config.ClassKeyHelper;
import org.intermine.api.profile.InterMineBag;
import org.intermine.api.profile.Profile;
import org.intermine.api.profile.ProfileManager;
import org.intermine.api.profile.TagManager;
import org.intermine.api.profile.TagManagerFactory;
import org.intermine.api.profile.InterMineBag.BagValue;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.api.tracker.xml.TrackManagerBinding;
import org.intermine.api.tracker.xml.TrackManagerHandler;
import org.intermine.model.userprofile.Tag;
import org.intermine.modelproduction.MetadataManager;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.sql.Database;
import org.intermine.sql.DatabaseUtil;
import org.intermine.util.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Code for reading and writing ProfileManager objects as XML
 *
 * @author Kim Rutherford
 */

public class ProfileManagerBinding
{
    private static final Logger LOG = Logger.getLogger(ProfileManagerBinding.class);

    /**
     * Default version of profile if it is not specified.
     */
    public static final String ZERO_PROFILE_VERSION = "0";

    /**
     * Convert the contents of a ProfileManager to XML and write the XML to the given writer.
     * @param profileManager the ProfileManager
     * @param writer the XMLStreamWriter to write to
     */
    public static void marshal(ProfileManager profileManager, XMLStreamWriter writer) {
        try {
            writer.writeStartElement("userprofiles");
            String profileVersion = getProfileVersion(profileManager.getProfileObjectStoreWriter());
            writer.writeAttribute(MetadataManager.PROFILE_FORMAT_VERSION, profileVersion);
            List usernames = profileManager.getProfileUserNames();

            for (Object userName : usernames) {
                Profile profile = profileManager.getProfile((String) userName);
                LOG.info("Writing profile: " + profile.getUsername());
                long startTime = System.currentTimeMillis();

                ProfileBinding.marshal(profile, profileManager.getProductionObjectStore(), writer,
                                       profileManager.getVersion(),
                                       getClassKeys(profileManager.getProductionObjectStore()));

                long totalTime = System.currentTimeMillis() - startTime;
                LOG.info("Finished writing profile: " + profile.getUsername()
                         + " took " + totalTime + "ms.");
            }

            TrackManagerBinding.marshal(profileManager.getProfileObjectStoreWriter(), writer);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    /** Returns profile format version from database. Version 0 corresponds to
     * situation when constraint values are saved in the internal format. Internal
     * format is format where '*' are replaced with % and other changes.
     * @see Util.wildcardSqlToUser()
     * @return saved format version or "0" if not saved in database
     */
    private static String getProfileVersion(ObjectStore os) {
        Database database = ((ObjectStoreInterMineImpl) os).getDatabase();
        try {
            String ret = MetadataManager.retrieve(database, MetadataManager.PROFILE_FORMAT_VERSION);
            LOG.info("Userprofile database has version \"" + ret + "\"");
            if (ret == null) {
                ret = ZERO_PROFILE_VERSION;
            }
            return ret;
        } catch (SQLException e) {
            throw new RuntimeException("Error during retrieving profile version from database.", e);
        }
    }

    private static Map<String, List<FieldDescriptor>> getClassKeys(ObjectStore os) {
        Properties classKeyProps = new Properties();
        try {
            InputStream inputStream = ProfileManagerBinding.class.getClassLoader()
                                      .getResourceAsStream("class_keys.properties");
            classKeyProps.load(inputStream);
        } catch (IOException ioe) {
            new BuildException("class_keys.properties not found", ioe);
        }
        return ClassKeyHelper.readKeys(os.getModel(), classKeyProps);
    }

    /**
     * Read a ProfileManager from an XML stream Reader
     * @param reader contains the ProfileManager XML
     * @param profileManager the ProfileManager to store the unmarshalled Profiles to
     * @param osw ObjectStoreWriter used to resolve object ids and write bags
     * correspond to object in old bags.
     * @param abortOnError if true, throw an exception if there is a problem.  If false, log the
     * problem and continue if possible (used by read-userprofile-xml).
     */
    public static void unmarshal(Reader reader, ProfileManager profileManager,
                                 ObjectStoreWriter osw, boolean abortOnError) {
        try {
            ProfileManagerHandler profileManagerHandler =
                new ProfileManagerHandler(profileManager, osw, abortOnError);
            SAXParser.parse(new InputSource(reader), profileManagerHandler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Read a ProfileManager from an XML stream Reader.  If there is a problem, throw an
     * exception.
     * @param reader contains the ProfileManager XML
     * @param profileManager the ProfileManager to store the unmarshalled Profiles to
     * @param osw ObjectStoreWriter used to resolve object ids and write bags
     * correspond to object in old bags.
     */
    public static void unmarshal(Reader reader, ProfileManager profileManager,
                                 ObjectStoreWriter osw) {
        unmarshal(reader, profileManager, osw, true);
    }
}

/**
 * Extension of DefaultHandler to handle parsing ProfileManagers
 * @author Kim Rutherford
 */
class ProfileManagerHandler extends DefaultHandler
{
    private ProfileHandler profileHandler = null;
    private TrackManagerHandler trackHandler = null;
    private ProfileManager profileManager = null;
    private ObjectStoreWriter osw;
    private boolean abortOnError;
    private long startTime = 0;
    private int version;
    private static final Logger LOG = Logger.getLogger(ProfileManagerBinding.class);

    /**
     * Create a new ProfileManagerHandler
     * @param profileManager the ProfileManager to store the unmarshalled Profile to
     * correspond to object in old bags.
     * @param osw an ObjectStoreWriter to the production database, to write bags
     * @param abortOnError if true, throw an exception if there is a problem.  If false, log the
     * problem and continue if possible (used by read-userprofile-xml).
     */
    public ProfileManagerHandler(ProfileManager profileManager, ObjectStoreWriter osw,
                                 boolean abortOnError) {
        super();
        this.profileManager = profileManager;
        this.osw = osw;
        this.abortOnError = abortOnError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs)
        throws SAXException {
        if ("userprofiles".equals(qName)) {
            String value = attrs.getValue(MetadataManager.PROFILE_FORMAT_VERSION);
            if (value == null) {
                version = 0;
            } else {
                version = Integer.parseInt(value);
            }
            ObjectStoreWriter osw = profileManager.getProfileObjectStoreWriter();
            try {
                Connection con = ((ObjectStoreInterMineImpl) osw).getConnection();
                if (!DatabaseUtil.tableExists(con, "bagvalues")) {
                    DatabaseUtil.createBagValuesTables(con);
                }
                ((ObjectStoreInterMineImpl) osw).releaseConnection(con);
            } catch (SQLException sqle) {
                LOG.error("Problem retrieving connection", sqle);
            }
        }
        if ("userprofile".equals(qName)) {
            startTime = System.currentTimeMillis();
            profileHandler = new ProfileHandler(profileManager, osw, version);
        }
        if (profileHandler != null) {
            profileHandler.startElement(uri, localName, qName, attrs);
        }

        if ("tracks".equals(qName)) {
            trackHandler = new TrackManagerHandler(profileManager.getProfileObjectStoreWriter());
        }

        if (trackHandler != null) {
            trackHandler.startElement(uri, localName, qName, attrs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if ("userprofile".equals(qName)) {
            Profile profile = profileHandler.getProfile();
            profileManager.createProfileWithoutBags(profile);
            try {
                Map<String, Set<BagValue>> bagValues = profileHandler.getBagsValues();
                for (InterMineBag bag : profile.getSavedBags().values()) {
                    bag.saveWithBagValues(profile.getUserId(), bagValues.get(bag.getName()));
                }
            } catch (ObjectStoreException ose) {
                throw new RuntimeException(ose);
            }
            Set<Tag> tags = profileHandler.getTags();
            TagManager tagManager =
                new TagManagerFactory(profile.getProfileManager()).getTagManager();
            for (Tag tag : tags) {
                try {
                    tagManager.addTag(tag.getTagName(), tag.getObjectIdentifier(), tag.getType(),
                            profile.getUsername());
                } catch (RuntimeException e) {
                    LOG.error("Error during adding tag: " + tag.toString(), e);
                    if (abortOnError) {
                        throw e;
                    }
                }
            }
            profileHandler = null;
            long totalTime = System.currentTimeMillis() - startTime;
            LOG.info("Finished profile: " + profile.getUsername() + " took " + totalTime + "ms.");
        }
        if (profileHandler != null) {
            profileHandler.endElement(uri, localName, qName);
        }

        if (trackHandler != null) {
            trackHandler.endElement(uri, localName, qName);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (profileHandler != null) {
            profileHandler.characters(ch, start, length);
        }
    }
}
