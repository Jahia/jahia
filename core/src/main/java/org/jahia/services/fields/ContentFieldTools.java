/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.fields;

import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.StateModificationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContentFieldTools {

    /** this class unique instance */
    private static ContentFieldTools instance;

    private Map<Integer, Class<? extends ContentField>> fieldClassNames;
    private Map<Integer, Constructor<? extends ContentField>> fieldClassConstructor = new ConcurrentHashMap<Integer, Constructor<? extends ContentField>>(53);
    private JahiaFieldsDataManager fieldsDataManager = null;

    /**
     * Default constructor, creates a new <code>ContentFieldTools</code> instance.
     */
    private ContentFieldTools () {
        // get the Map which give the class name
        // which correspond to the field type.
        fieldClassNames = ContentFieldTypes.getInstance ().getFieldClassNames ();
        fieldsDataManager = (JahiaFieldsDataManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldsDataManager.class.getName());
    }

    /**
     * Return the unique instance of this class
     *
     * @return the unique instance of this class
     */
    public static synchronized ContentFieldTools getInstance () {
        if (instance == null) {
            instance = new ContentFieldTools ();
        }

        return instance;
    }

    /**
     * Creates a ContentField value in RAM, with no content
     * This method should be called to create a field, and the field content should be set right
     * after that! This method allocates a new AclID and fieldID so don't play with it if you
     * don't have a firm intention to create a field ;)
     *
     * @return the created content field. Do a set on it right after, if no set is done, the
     *         field will never be written to the DB ! (but its acl won't be deleted)
     */
    protected ContentField createField (int siteID, int pageID, int containerID, int fieldDefID, int typeField,
                                        int connectType, int parentAclID, int aclID)
            throws JahiaException {
        // create new field id
        int theFieldID = 0;

        // create new ACL id
        if (aclID == 0) {
            aclID = parentAclID;
        }

        return createContentFieldInstance (theFieldID, siteID, pageID, containerID,
                fieldDefID, typeField, connectType,
                aclID, new ArrayList<ContentObjectEntryState> (), new HashMap<ContentObjectEntryState, String> ());
    }

    /**
     * Preloads all the active or staged fields for a given page into the
     * fields cache.
     *
     * @param pageID the page ID for which to preload all the content fields
     *
     * @throws JahiaException thrown if there was an error while loading the
     *                        fields from the database.
     */
    protected void preloadActiveOrStagedFieldsByPageID (int pageID)
            throws JahiaException {
        fieldsDataManager.preloadActiveOrStagedFieldsByPageID (pageID);
    }

    /**
     * Preloads all the active or staged fields for a given container into the
     * field cache.
     *
     * @param containerID the container ID for which to preload all the content fields
     *
     * @throws JahiaException thrown if there was an error while loading the fields from the database.
     */
    protected void preloadActiveOrStagedFieldsByContainerID (int containerID)
            throws JahiaException {
        fieldsDataManager.preloadActiveOrStagedFieldsByContainerID (containerID);
    }

    /**
     * Preloads all the active or staged fields for a given metadata owner key into the
     * field cache.
     *
     * @param metadataOwnerKey
     * @throws JahiaException thrown if there was an error while loading the fields from the database.
     */
    public void preloadActiveOrStagedFieldsByMetadataOwner (ObjectKey metadataOwnerKey)
            throws JahiaException {
        fieldsDataManager.preloadActiveOrStagedFieldsByMetadataOwner (metadataOwnerKey);
    }

    /**
     * Preloads all the staged fields for a given metadata owner key into the
     * field cache.
     *
     * @param metadataOwnerKey
     * @throws JahiaException thrown if there was an error while loading the fields from the database.
     */
    public void preloadStagedFieldsByMetadataOwner (ObjectKey metadataOwnerKey)
            throws JahiaException {
        fieldsDataManager.preloadStagedFieldsByMetadataOwner (metadataOwnerKey);
    }

    /**
     * loads all the staged fields for a given metadata owner key into the
     * field cache.
     *
     * @param metadataOwnerKey
     * @throws JahiaException thrown if there was an error while loading the fields from the database.
     */
    public void findStagedFieldsByMetadataOwner(ObjectKey metadataOwnerKey)
            throws JahiaException {
        fieldsDataManager.findStagedFieldsByMetadataOwner(metadataOwnerKey);
    }

    /**
     * Get a ContentField from its ID
     *
     * @param fieldID
     *
     * @throws JahiaException if the field doesn't exist, or there's a DB error
     */
    public ContentField getField (int fieldID)
            throws JahiaException {
        return getField (fieldID, false);
    }

    /**
     * Get a ContentField from its ID , look in cache only
     *
     * @param fieldId
     *
     */
    public ContentField getFieldFromCacheOnly (int fieldId) {
        return fieldsDataManager.loadContentFieldFromCacheOnly(fieldId);
    }

    /**
     * Get a ContentField from its ID
     *
     * @param fieldID
     * @param forceLoadFromDB if true, force loading from db
     *
     * @throws JahiaException If the field doesn't exist, or there's a DB error
     */
    public ContentField getField (int fieldID, boolean forceLoadFromDB)
            throws JahiaException {
        return fieldsDataManager.loadContentField(fieldID, forceLoadFromDB);
    }
    
    /**
     * Get ContentFields from its IDs
     *
     * @param fieldIDs
     * @param forceLoadFromDB if true, force loading from db
     *
     * @throws JahiaException If a field doesn't exist, or there's a DB error
     */
    public List<ContentField> getFields (List<Integer> fieldIDs, EntryLoadRequest loadVersion, boolean forceLoadFromDB)
            throws JahiaException {
        return fieldsDataManager.loadContentFields(fieldIDs, loadVersion, forceLoadFromDB);
    }    

    /**
     * Delete a ContentField from its ID
     * (This is a user request. In fact, it just puts a "delete flag" on every language of
     * The field, and the field will really be delete when the page is staging-validated
     * and versioning is disabled)
     *
     * @param theField                    the ID of the field to delete (should be synchronized!)
     * @param user                        the user performing the marking
     * @param activeAndStagingEntryStates the corresponding List coming from the contentfield
     * @param stateModifContext           used to detect loops in deletion marking.
     */
    public void markFieldForDeletion (ContentField theField,
                                      JahiaUser user,
                                      List<ContentObjectEntryState> activeAndStagingEntryStates,
                                      StateModificationContext stateModifContext)
            throws JahiaException {
        for (ContentObjectEntryState entryState : activeAndStagingEntryStates) {
            // we now have a new save request where we've specified the lanstateModifContextguage!
            theField.markLanguageForDeletion (user, entryState.getLanguageCode (), stateModifContext);
        }
    }

    /**
     * This method should ONLY be called when a field must be COMPLETELY deleted.
     * It's the case when a validate is done and no more active&staging version of the
     * field exist, and versioning is disabled (so there are no versioning versions either).
     * In this case, this method should be called by the validate method.
     *
     * @param theField the field to delete
     */
    public void purgeFieldData (ContentField theField)
            throws JahiaException {
        // removes ACL & ACL entries corresponding to this field
        // it's a page field
        if ( theField.getContainerID() == 0 && !theField.isMetadata() && !theField.isAclSameAsParent()){
            theField.getACL ().delete ();
        }
    }

    /**
     * Set a field (dump it to database etc)
     * 1. if indexed for the search engine, remove old field value
     * 2. if versioning is enable & modified field is active, calls backupFieldVersion for this field
     * 3. do database work and update versions of the loadedFieldData Map & Lists in the field
     * 3.b. if value is <no_value> then delete the version of this field
     * 4. add new field value to search engine
     * @param theField the field to dump to database
     * @param newVersionID the new version ID of this field (from VersionService.getCurrentVersionID())

     */

/*    public void setField (ContentField theField, int newVersionID)
        throws JahiaException
    {
        synchronized (theField)
        {
            for (Enumeration e = theField.loadedDBValues.keys() ; e.hasNext() ;)
            {
                ContentFieldVersionInfo thisVersionInfo = (ContentFieldVersionInfo)e.next();
                // we only update active & staged fields, not inactive ones
                if (thisVersionInfo.isStaging() || thisVersionInfo.isActive())
                {
                    ContentField.DBValue dbValue = (ContentField.DBValue)theField.loadedDBValues.get(thisVersionInfo);
                    // is this field modified or new ?
                    if ((dbValue.getOldValue() != null) ||
                       (thisVersionInfo.isNew()))
                    {
                        // yessss, let's start the update process

                        // if this field ain't new and is active, remove field content from search engine
                        if ((!thisVersionInfo.isNew())
                            && (thisVersionInfo.isActive()))
                        {
                            //TODO remove from search engine
                            String s = theField.getValueForSearch(dbValue.getOldValue(), thisVersionInfo.getLanguageCode());
                            //if ((s!=null)&&(!s.equals("")))
                            //ServicesRegistry.getInstance().getJahiaSearchService().removeFieldFromSearchEngine(  );
                        }

                        // if versioning is enable, and staging is not enabled backup old version of this field
                        if ( theField.verService.isVersioningEnabled(theField.getSiteID()) &&
                             thisVersionInfo.isActive() )
                        {
                            backupDBValue(theField);
                        }

                        // if its value is "<no_value>" it means we want to delete it
                        if (dbValue.getValue().equals(ContentField.NO_VALUE))
                        {
                            deleteDBValue(theField, thisVersionInfo);

                            // remove the version from the activeAndStagingVersionInfo
                            theField.activeAndStagingVersionInfo.remove(thisVersionInfo);
                            // we must remove it from the Map too
                            theField.loadedDBValues.remove(thisVersionInfo);


                        } else
                        // okay, we're here to create or update, but not delete!
                        {
                            // remove the version from the activeAndStagingVersionInfo
                            theField.activeAndStagingVersionInfo.remove(thisVersionInfo);
                            // we must remove it from the Map to change the version ID
                            theField.loadedDBValues.remove(thisVersionInfo);
                            // update versionID with new version
                            thisVersionInfo.setVersionID(newVersionID);
                            // we put it back in the Map, with oldvalue = null so we now it's synched with DB
                            theField.loadedDBValues.put(thisVersionInfo, theField.newDBValue(dbValue.getValue(),null));
                            // remove the version from the activeAndStagingVersionInfo
                            theField.activeAndStagingVersionInfo.add(thisVersionInfo);

                            // if the version is new we create a new field version
                            if (thisVersionInfo.isNew())
                            {
                                createDBValue(theField, thisVersionInfo);
                            }

                            // we update it with the new value
                            updateDBValue(theField, thisVersionInfo);
                            // add new field content from search engine
                            if (thisVersionInfo.isActive())
                            {
                                //TODO add to search engine
                                String s = theField.getValueForSearch(dbValue.getValue(), thisVersionInfo.getLanguageCode());
                                //if ((s!=null)&&(!s.equals("")))
                                //ServicesRegistry.getInstance().getJahiaSearchService().addFieldToSearchEngine(  );
                            }
                        }
                    }
                }
            }
        }
    }*/

    /**
     * Delete a ContentField
     * In fact, it puts <no_value> for every language of this field, and then
     * call setField
     */

/*    public void deleteField (int fieldID)
        throws JahiaException
    {
        ContentField theField = getField(fieldID);
        boolean staged = theField.verService.isStagingEnabled(theField.getSiteID());
        synchronized (theField)
        {
            for (int i=0;i<theField.activeAndStagingVersionInfo.size();i++)
            {
                ContentFieldVersionInfo verInfo=(ContentFieldVersionInfo)theField.activeAndStagingVersionInfo.elementAt(i);
                theField.setDBValue(verInfo.getLanguageCode(), ContentField.NO_VALUE);
            }
        }
        setField (theField, theField.verService.getCurrentVersionID());
    }*/

    /**
     * Clone a ContentField
     */
    protected ContentField cloneField (int fieldID)
            throws JahiaException {
        return null; /** todo implement cloneField */
    }

    /**
     * create a new ContentField instance. This is the only
     * method that should be called to create a new JahiaField
     * object, and usually you shouldn't need to call this
     * method but use getField or createField instead!
     */
    public ContentField createContentFieldInstance (int ID,
                                                       int jahiaID,
                                                       int pageID,
                                                       int ctnid,
                                                       int fieldDefID,
                                                       int fieldType,
                                                       int connectType,
                                                       int aclID,
                                                       List<ContentObjectEntryState> activeAndStagingEntryStates,
                                                       Map<ContentObjectEntryState, String> activeAndStagedDBValues)
            throws JahiaException {
        ContentField theField;

        try {

            // get the constructor by its name
            // the name come from the Map
            Integer fieldTypeInt = new Integer (fieldType);
            final Constructor<? extends ContentField> thisConstructor;
            if (fieldClassConstructor.containsKey (fieldTypeInt)) {
                thisConstructor = fieldClassConstructor.get (fieldTypeInt);
            } else {
                // define the types of the parameter of the constructor
                Class<?> theParams[] = {Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     List.class,
                                     Map.class};

                thisConstructor = fieldClassNames.get (new Integer (fieldType)).asSubclass(ContentField.class).
                        getDeclaredConstructor (theParams);
                fieldClassConstructor.put (fieldTypeInt, thisConstructor);
            }

            // the parameter values of the constructor
            Object args[] = {new Integer (ID), new Integer (jahiaID),
                             new Integer (pageID), new Integer (ctnid),
                             new Integer (fieldDefID),
                             new Integer (fieldType),
                             new Integer (connectType),
                             new Integer (aclID),
                             activeAndStagingEntryStates,
                             activeAndStagedDBValues};

            // call the constructor
            theField = (ContentField) thisConstructor.newInstance (args);


        } catch (NoSuchMethodException nsme) {
            throw new JahiaException ("JahiaContentBaseService:createContentFieldInstance",
                    "Method not found!",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, nsme);

        } catch (IllegalAccessException iae) {
            throw new JahiaException ("JahiaContentBaseService:createContentFieldInstance",
                    "Illegal access",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, iae);

        } catch (InvocationTargetException ite) {
            throw new JahiaException ("JahiaContentBaseService:createContentFieldInstance",
                    "InvocationTarget exception",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY,
                    ite.getTargetException ());

        } catch (InstantiationException ie) {
            throw new JahiaException ("JahiaContentBaseService:createContentFieldInstance",
                    "Instantiation exception",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, ie);
        }
        // return the new ContentField
        return theField;
    }


}
