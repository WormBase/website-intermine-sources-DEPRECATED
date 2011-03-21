package org.intermine.api.profile;

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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.intermine.api.search.Scope;
import org.intermine.api.search.SearchRepository;
import org.intermine.api.search.WebSearchable;
import org.intermine.api.tag.TagTypes;
import org.intermine.api.template.TemplateQuery;
import org.intermine.api.tracker.TrackerDelegate;
import org.intermine.model.userprofile.Tag;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;

/**
 * Class to represent a user of the webapp
 *
 * @author Mark Woodbridge
 * @author Thomas Riley
 */
public class Profile
{
    protected ProfileManager manager;
    protected String username;
    protected Integer userId;
    protected String password;
    protected Map<String, SavedQuery> savedQueries = new TreeMap<String, SavedQuery>();
    protected Map<String, InterMineBag> savedBags = new TreeMap<String, InterMineBag>();
    protected Map<String, TemplateQuery> savedTemplates = new TreeMap<String, TemplateQuery>();

    protected Map queryHistory = new ListOrderedMap();
    private boolean savingDisabled;
    private SearchRepository searchRepository;

    /**
     * Construct a Profile
     * @param manager the manager for this profile
     * @param username the username for this profile
     * @param userId the id of this user
     * @param password the password for this profile
     * @param savedQueries the saved queries for this profile
     * @param savedBags the saved bags for this profile
     * @param savedTemplates the saved templates for this profile
     */
    public Profile(ProfileManager manager, String username, Integer userId, String password,
                   Map<String, SavedQuery> savedQueries, Map<String, InterMineBag> savedBags,
                   Map<String, TemplateQuery> savedTemplates) {
        this.manager = manager;
        this.username = username;
        this.userId = userId;
        this.password = password;
        if (savedQueries != null) {
            this.savedQueries.putAll(savedQueries);
        }
        if (savedBags != null) {
            this.savedBags.putAll(savedBags);
        }
        if (savedTemplates != null) {
            this.savedTemplates.putAll(savedTemplates);
        }
        searchRepository = new SearchRepository(this, Scope.USER);
    }

    /**
     * Return the ProfileManager that was passed to the constructor.
     * @return the ProfileManager
     */
    public ProfileManager getProfileManager() {
        return manager;

    }

    /**
     * Get the value of username
     * @return the value of username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Return a first part of the username before the "@" sign (used in metabolicMine)
     * @author radek
     *
     * @return String
     */
    public String getName() {
        int atPos = username.indexOf("@");
        if (atPos > 0) {
            return username.substring(0, atPos);
        } else {
            return username;
        }
    }

    /**
     * Return true if and only if the user is logged is (and the Profile will be written to the
     * userprofile).
     * @return Return true if logged in
     */
    public boolean isLoggedIn() {
        return getUsername() != null;
    }

    /**
     * Get the value of userId
     * @return an Integer
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Set the userId
     *
     * @param userId an Integer
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Get the value of password
     * @return the value of password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Disable saving until enableSaving() is called.  This is called before many templates or
     * queries need to be saved or deleted because each call to ProfileManager.saveProfile() is
     * slow.
     */
    public void disableSaving() {
        savingDisabled = true;
    }

    /**
     * Re-enable saving when saveTemplate(), deleteQuery() etc. are called.  Also calls
     * ProfileManager.saveProfile() to write this Profile to the database and rebuilds the
     * template description index.
     */
    public void enableSaving() {
        savingDisabled = false;
        if (manager != null) {
            manager.saveProfile(this);
        }
        reindex(TagTypes.TEMPLATE);
        reindex(TagTypes.BAG);
    }

    /**
     * Get the users saved templates
     * @return saved templates
     */
    public Map<String, TemplateQuery> getSavedTemplates() {
        return Collections.unmodifiableMap(savedTemplates);
    }


    /**
     * Save a template
     * @param name the template name
     * @param template the template
     */
    public void saveTemplate(String name, TemplateQuery template) {
        savedTemplates.put(name, template);
        if (manager != null && !savingDisabled) {
            manager.saveProfile(this);
            reindex(TagTypes.TEMPLATE);
        }
    }

    /**
     * get a template
     * @param name the template
     * @return template
     */
    public TemplateQuery getTemplate(String name) {
        return savedTemplates.get(name);
    }

    /**
     * Delete a template and its tags, rename the template tracks adding the prefix "deleted_"
     * to the previous name. If trackerDelegate is null, the template tracks are not renamed
     * @param name the template name
     * @param trackerDelegate used to rename the template tracks. 
     */
    public void deleteTemplate(String name, TrackerDelegate trackerDelegate) {
        savedTemplates.remove(name);
        if (manager != null) {
            if (!savingDisabled) {
                manager.saveProfile(this);
                reindex(TagTypes.TEMPLATE);
            }
        }
        TagManager tagManager = getTagManager();
        tagManager.deleteObjectTags(name, TagTypes.TEMPLATE, username);
        if (trackerDelegate != null) {
            trackerDelegate.updateTemplateName(name, "deleted_" + name);
        }
    }

    /**
     * Get the value of savedQueries
     * @return the value of savedQueries
     */
    public Map<String, SavedQuery> getSavedQueries() {
        return Collections.unmodifiableMap(savedQueries);
    }

    /**
     * Save a query
     * @param name the query name
     * @param query the query
     */
    public void saveQuery(String name, SavedQuery query) {
        savedQueries.put(name, query);
        if (manager != null && !savingDisabled) {
            manager.saveProfile(this);
        }
    }

    /**
     * Delete a query
     * @param name the query name
     */
    public void deleteQuery(String name) {
        savedQueries.remove(name);
        if (manager != null && !savingDisabled) {
            manager.saveProfile(this);
        }
    }

    /**
     * Get the session query history.
     * @return map from query name to SavedQuery
     */
    public Map<String, SavedQuery> getHistory() {
        return Collections.unmodifiableMap(queryHistory);
    }

    /**
     * Save a query to the query history.
     * @param query the SavedQuery to save to the history
     */
    public void saveHistory(SavedQuery query) {
        queryHistory.put(query.getName(), query);
    }

    /**
     * Remove an item from the query history.
     * @param name the of the SavedQuery from the history
     */
    public void deleteHistory(String name) {
        queryHistory.remove(name);
    }

    /**
     * Rename an item in the history.
     * @param oldName the name of the old item
     * @param newName the new name
     */
    public void renameHistory(String oldName, String newName) {
        Map<String, SavedQuery> newMap = new ListOrderedMap();
        Iterator<String> iter = queryHistory.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            SavedQuery sq = (SavedQuery) queryHistory.get(name);
            if (name.equals(oldName)) {
                sq = new SavedQuery(newName, sq.getDateCreated(), sq.getPathQuery());
            }
            newMap.put(sq.getName(), sq);
        }
        queryHistory = newMap;
    }

    /**
     * Get the value of savedBags
     * @return the value of savedBags
     */
    public Map<String, InterMineBag> getSavedBags() {
        return Collections.unmodifiableMap(savedBags);
    }

    /**
     * Stores a new bag in the profile. Note that bags are always present in the user profile
     * database, so this just adds the bag to the in-memory list of this profile.
     *
     * @param name the name of the bag
     * @param bag the InterMineBag object
     */
    public void saveBag(String name, InterMineBag bag) {
        if (StringUtils.isBlank(name)) {
            throw new RuntimeException("No name specified for the list to save.");
        }
        savedBags.put(name, bag);
        reindex(TagTypes.BAG);
    }

    /**
     * Create a bag and save it to the userprofile database.
     *
     * @param name the bag name
     * @param type the bag type
     * @param description the bag description
     * @return the new bag
     * @throws ObjectStoreException if something goes wrong
     */
    public InterMineBag createBag(String name, String type,
            String description) throws ObjectStoreException {
        ObjectStore os = manager.getProductionObjectStore();
        ObjectStoreWriter uosw = manager.getProfileObjectStoreWriter();
        InterMineBag bag = new InterMineBag(name, type, description, new Date(), os, userId, uosw);
        savedBags.put(name, bag);
        reindex(TagTypes.BAG);
        return bag;
    }

    /**
     * Delete a bag from the user account, if user is logged in also deletes from the userprofile
     * database.
     * @param name the bag name
     * @throws ObjectStoreException if problems deleting bag
     */
    public void deleteBag(String name) throws ObjectStoreException {
        InterMineBag bagToDelete = savedBags.get(name);
        if (isLoggedIn()) {
            bagToDelete.delete();
        }
        savedBags.remove(name);

        TagManager tagManager = getTagManager();
        tagManager.deleteObjectTags(name, TagTypes.BAG, username);
        reindex(TagTypes.BAG);
    }


    /**
     * Rename an existing bag, throw exceptions when bag doesn't exist of if new name already
     * exists.  Moves tags from old bag to new bag.
     * @param oldName the bag to rename
     * @param newName new name for the bag
     * @throws ObjectStoreException if problems storing
     */
    public void renameBag(String oldName, String newName) throws ObjectStoreException {
        if (!savedBags.containsKey(oldName)) {
            throw new IllegalArgumentException("Attempting to rename a bag that doesn't"
                    + " exist: " + oldName);
        }
        if (savedBags.containsKey(newName)) {
            throw new ProfileAlreadyExistsException("Attempting to rename a bag to a new name that"
                    + " already exists: " + newName);
        }

        InterMineBag bag = savedBags.get(oldName);
        savedBags.remove(oldName);
        bag.setName(newName);
        saveBag(newName, bag);
        moveTagsToNewObject(oldName, newName, TagTypes.BAG);
    }

    /**
     * Update an existing template, throw exceptions when template doesn't exist.
     * Moves tags from old template to new template.
     * @param oldName the template to rename
     * @param template the new template
     * @throws ObjectStoreException if problems storing
     */
    public void updateTemplate(String oldName, TemplateQuery template) throws ObjectStoreException {
        if (!savedTemplates.containsKey(oldName)) {
            throw new IllegalArgumentException("Attempting to rename a template that doesn't"
                    + " exist: " + oldName);
        }

        savedTemplates.remove(oldName);
        saveTemplate(template.getName(), template);
        if (!oldName.equals(template.getName())) {
            moveTagsToNewObject(oldName, template.getName(), TagTypes.TEMPLATE);
        }
    }


    private void moveTagsToNewObject(String oldTaggedObj, String newTaggedObj, String type) {
        TagManager tagManager = getTagManager();
        List<Tag> tags = tagManager.getTags(null, oldTaggedObj, type, username);
        for (Tag tag : tags) {
            tagManager.addTag(tag.getTagName(), newTaggedObj, type, username);
            tagManager.deleteTag(tag);
        }
    }

    private TagManager getTagManager() {
        return new TagManagerFactory(manager).getTagManager();
    }

    /**
     * Create a map from category name to a list of templates contained
     * within that category.
     */
    private void reindex(String type) {
        // We also take this opportunity to index the user's template queries, bags, etc.
        searchRepository.addWebSearchables(type);
    }

    /**
     * Return a WebSearchable Map for the given type.
     * @param type the type (from TagTypes)
     * @return the Map
     */
    public Map<String, ? extends WebSearchable> getWebSearchablesByType(String type) {
        if (type.equals(TagTypes.TEMPLATE)) {
            return savedTemplates;
        }
        if (type.equals(TagTypes.BAG)) {
            return getSavedBags();
        }
        throw new RuntimeException("unknown type: " + type);
    }

    /**
     * Get the SearchRepository for this Profile.
     * @return the SearchRepository for the user
     */
    public SearchRepository getSearchRepository() {
        return searchRepository;
    }
}