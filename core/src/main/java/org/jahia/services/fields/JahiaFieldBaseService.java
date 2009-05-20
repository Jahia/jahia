/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.fields;

import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.data.JahiaDOMObject;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.JahiaFieldsDefinitionManager;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JahiaFieldBaseService extends JahiaFieldService {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaFieldBaseService.class);

    /** the unique instance of this class */
    private static JahiaFieldBaseService instance;
    
    private Map<Integer, String> fieldClassNames;
    private Map<Integer, Constructor<? extends JahiaField>> fieldClassConstructor = new ConcurrentHashMap<Integer, Constructor<? extends JahiaField>> ();

    // the Fields cache name.
    public static final String FIELD_CACHE = "FieldCache";

    private JahiaFieldsDefinitionManager definitionManager = null;
    private JahiaFieldsDataManager dataManager = null;

    private CacheService cacheService;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setDefinitionManager(JahiaFieldsDefinitionManager definitionManager) {
        this.definitionManager = definitionManager;
    }

    public void setDataManager(JahiaFieldsDataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Defaulf constructor, creates a new <code>JahiaFieldBaseService</code> instance.
     */
    protected JahiaFieldBaseService () {
    }


    /**
     * Return the unique instance of this class
     *
     * @return the unique instance of this class
     */
    public static synchronized JahiaFieldBaseService getInstance () {

        if (instance == null) {
            instance = new JahiaFieldBaseService ();
        }
        return instance;
    }


    /**
     * Initialize the service
     *
     */
    public void start()
            throws JahiaInitializationException {


        // get the Map which give the class name
        // which correspond to the field type.
        fieldClassNames = FieldTypes.getInstance ().getFieldClassNames ();

//        cacheFields = cacheService.createCacheInstance(FIELD_CACHE);

        // create field definitions directory for flat files
        createFileDefinitionRepository ();
    }

    public void stop() {}


    /**
     * create a JahiaField.
     * The only method to instanciate a new JahiaField
     * It call the constructor corresponding to the field type.
     * This method must NOT be called directly,
     * but only by JahiaFieldSet.declareField()
     * and JahiaFieldDB.db_load_field().
     */
    public JahiaField createJahiaField (int ID,
                                        int jahiaID,
                                        int pageID,
                                        int ctnid,
                                        int fieldDefID,
                                        int fieldType,
                                        int connectType,
                                        String fieldValue,
                                        int rank,
                                        int aclID,
                                        int versionID,
                                        int versionStatus,
                                        String languageCode)
            throws JahiaException {
        JahiaField theField;

        fieldValue = checkFieldEnumerationValues (fieldValue);

        try {

            logger.debug ("fieldType: " + fieldType + " class: "
                    + fieldClassNames.get (new Integer (fieldType)));

            // get the constructor by its name
            // the name come from the Map
            Integer fieldTypeInt = new Integer (fieldType);
            Constructor<? extends JahiaField> thisConstructor = null;
            if (fieldClassConstructor.containsKey (fieldTypeInt)) {
                thisConstructor = fieldClassConstructor.get (fieldTypeInt);
            } else {
                // define the types of the parameter of the constructor
                Class<?>[] theParams = {Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     String.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     Integer.class,
                                     String.class};

                String fieldClassName = fieldClassNames.
                        get (new Integer (fieldType));
                if (fieldClassName != null) {
                    thisConstructor = Class.forName (fieldClassName).asSubclass(JahiaField.class).
                            getDeclaredConstructor (theParams);
                    fieldClassConstructor.put (fieldTypeInt, thisConstructor);
                } else {
                    throw new JahiaException("Error accessing field",
                            "Couldn't find field class name for type " + fieldType +
                                    " pageID=" + pageID +
                                    " fieldDefID=" + fieldDefID +
                                    " ctnID=" + ctnid,
                            JahiaException.PERSISTENCE_ERROR,
                            JahiaException.ERROR_SEVERITY);
                }
            }

            // the parameter values of the constructor
            Object args[] = {new Integer (ID), new Integer (jahiaID),
                             new Integer (pageID), new Integer (ctnid),
                             new Integer (fieldDefID),
                             new Integer (fieldType),
                             new Integer (connectType), fieldValue,
                             new Integer (rank), new Integer (aclID),
                             new Integer (versionID), new Integer (versionStatus),
                             new String (languageCode)};


            // call the constructor
            theField = thisConstructor.newInstance (args);
            if ( theField != null && theField.getID() > 0){
                ContentField contentField = this.dataManager.loadContentField(theField.getID());
                theField.setIsMetadata(contentField.isMetadata());
                theField.setMetadataOwnerObjectKey(contentField.getMetadataOwnerObjectKey());
            }

        } catch (ClassNotFoundException cnfe) {
            logger.debug ("exception (class nf) " + cnfe.toString (), cnfe);
            throw new JahiaException ("JahiaFieldBaseService:createJahiaField",
                    "Class not found!",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, cnfe);

        } catch (NoSuchMethodException nsme) {
            logger.debug ("createJahiaField: (method nf) " + nsme.toString (), nsme);
            throw new JahiaException ("JahiaFieldBaseService:createJahiaField",
                    "Method not found!",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, nsme);

        } catch (IllegalAccessException iae) {
            logger.debug ("createJahiaField: (illegal access) " + iae.toString (), iae);
            throw new JahiaException ("JahiaFieldBaseService:createJahiaField",
                    "Illegal access",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, iae);

        } catch (InvocationTargetException ite) {
            logger.debug ("createJahiaField: (invocation) " + ite.toString (), ite);
            throw new JahiaException ("JahiaFieldBaseService:createJahiaField",
                    "InvocationTarget exception",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, ite);

        } catch (InstantiationException ie) {
            logger.debug ("createJahiaField: (instantiation) " + ie.toString (), ie);
            throw new JahiaException ("JahiaFieldBaseService:createJahiaField",
                    "Instantiation exception",
                    JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, ie);
        }

        // return the new JahiaField
        return theField;
    }


    /**
     * Create a new field
     *
     * @param ID          the field identification
     * @param jahiaID
     * @param pageID      the related page identification
     * @param ctnid       the related container identification
     * @param fieldDefID
     * @param fieldType
     * @param connectType
     * @param fieldValue  the field value
     * @param rank        the field position
     * @param aclID       the ACL identification
     *
     * @return Returns a new <code>JahiaField</code> instance, initialized with the
     *         specified arguments.
     *
     * @throws JahiaException when a creation failure occured
     */
    public JahiaField createJahiaField (int ID, int jahiaID, int pageID, int ctnid,
                                        int fieldDefID, int fieldType, int connectType,
                                        String fieldValue, int rank, int aclID)
            throws JahiaException {
        fieldValue = checkFieldEnumerationValues (fieldValue);

        return createJahiaField (ID, jahiaID, pageID, ctnid, fieldDefID, fieldType,
                connectType, fieldValue, rank, aclID, 0,
                EntryLoadRequest.STAGING_WORKFLOW_STATE,
                Jahia.getSettings ().getDefaultLanguageCode ());
    }

    /**
     * Gets all field list IDs in a page (from page ID)
     *
     * @param pageID the page ID
     *
     * @return A List of Integer corresponding to the field IDs.
     */
    public List<Integer> getNonContainerFieldIDsInPage (int pageID)
            throws JahiaException {
        return dataManager.getNonContainerFieldIDsInPageByWorkflowState(pageID, null);
    } // getFieldListIDsInPage

    /**
     * Gets all field IDs in a page in a specified workflow state
     *
     * @param pageID      the page ID.
     * @param loadVersion the workflow state field to load.
     *
     * @return A List of Integer corresponding to the field IDs.
     */
    public List<Integer> getNonContainerFieldIDsInPageByWorkflowState (int pageID,
                                                                EntryLoadRequest loadVersion)
            throws JahiaException {
        return dataManager.getNonContainerFieldIDsInPageByWorkflowState (pageID, loadVersion);
    }

    /**
     * Gets all field IDs in a page in a specified workflow state
     *
     * @param pageID      the page ID.
     * @param loadVersion the workflow state field to load.
     *
     * @return A List of Integer corresponding to the field IDs.
     */
    public List<Object[]> getNonContainerFieldIDsAndTypesInPageByWorkflowState (int pageID,
                                                                EntryLoadRequest loadVersion)
            throws JahiaException {
        return dataManager.getNonContainerFieldIDsAndTypesInPageByWorkflowState (pageID, loadVersion);
    }

    /**
     * Retrieves all the ContentFieldEntryStates for the fields on a page that
     * are *NOT* contained in a container (ie directly attached to the page).
     *
     * @param pageID the identifier of the page for which to retrieve the
     *               field entry states
     *
     * @return a SortedSet of ContentFieldEntryState objects that contain the
     *         cumulation of all the field's entrystates.
     *
     * @throws JahiaException thrown if there was a problem retrieving field
     *                        data from the database.
     */
    public SortedSet<ContentObjectEntryState> getNonContainerFieldEntryStateInPage (int pageID)
            throws JahiaException {
        SortedSet<ContentObjectEntryState> entryStates = new TreeSet<ContentObjectEntryState> ();
        List<Integer> fieldIDs = getNonContainerFieldIDsInPage (pageID);
        for (Integer curFieldID : fieldIDs) {
            ContentField curField = ContentField.getField (curFieldID.intValue ());
            entryStates.addAll (curField.getEntryStates ());
        }
        return entryStates;
    }

    /**
     * Given a page ID, get all the IDs of the page links contained in
     * the page, whether of type DIRECT, INTERNAL or URL
     *
     * @param pageID the page ID
     *
     * @return a List of Field list IDs
     */
    public List<Integer> getPagefieldIDsInPage (int pageID)
            throws JahiaException {
        return dataManager.getPageFieldIdsInPage(pageID);
    } // getPagefieldIDsInPage


    /**
     * Gets a field ID from its name and page id
     *
     * @param fieldName the field name
     * @param pageID    the page ID
     *
     * @return a field id
     */
    public int getFieldID (String fieldName, int pageID)
            throws JahiaException {
        return dataManager.getFieldId(fieldName, pageID);
    } // getFieldListIDsInPage


    /**
     * Get all the fields ID for a gived site (for the search engine)
     *
     * @param siteID the site id
     *
     * @return a List of field ids
     */
    public List<Integer> getAllFieldIDs (int siteID)
            throws JahiaException {

        return dataManager.getAllFieldsId (siteID);

    }


    /**
     * Get all the fields ID (for the search engine)
     *
     * @return a List of field ids
     */
    public List<Integer> getAllFieldIDs ()
            throws JahiaException {
        return dataManager.getAllFieldsId ();
    } // getAllFieldIDs


    /**
     * gets all the field definition ids
     * to be changed, using a fielddefinition cache instead and changing the def registry.
     *
     * @return a List of field definition ids
     */
    public List<Integer> getAllFieldDefinitionIDs ()
            throws JahiaException {
        return definitionManager.getAllFieldDefinitionIds();
    } // getAllFieldDefinitionIDs

    /**
     * gets all the field definition ids for a site
     * to be changed, using a fielddefinition cache instead and changing the def registry.
     *
     * @return a List of field definition ids for a site
     */
    public List<Integer> getAllFieldDefinitionIDs (int siteId)
            throws JahiaException {
        return definitionManager.getAllFieldDefinitionIds(siteId);
    } // getAllFieldDefinitionIDs

    /**
     * loads a field from the database, no other info than what's in the database
     * NO RIGHTS CHECKS! use another loadField method (with jParams) for that.
     *
     * @param        fieldID             the field ID
     * @return       a JahiaField object
     * @see          org.jahia.data.fields.JahiaField
     *
     */

    /*
 private JahiaField loadFieldInfo( int fieldID )
 throws JahiaException
 {
     return loadFieldInfo (fieldID, EntryLoadRequest.CURRENT);
 }

 private JahiaField loadFieldInfo( int fieldID, EntryLoadRequest loadVersion )
 throws JahiaException
 {
     // if we don't load current version, we load it from database
     if (!loadVersion.isCurrent())
     {
         return fieldsDB.db_load_field( fieldID, loadVersion );
     } else
     // else we load it from cache
     {
         JahiaField theField = (JahiaField) cacheFields.getValue (new Integer(fieldID));
         // not already in cache -> we must load it
         if (theField == null)
         {
             theField = fieldsDB.db_load_field( fieldID, loadVersion );
             if (theField != null) {
                 // we only cache it if it's the active version
                 if (loadVersion.isCurrent())
                     cacheFields.setValue ( new Integer(fieldID), theField);
             } else {
                 throw new JahiaException ("Error loading field data",
                                           "Error loading field with ID " +
                                           Integer.toString(fieldID),
                                           JahiaException.DATABASE_ERROR,
                                           JahiaException.ERROR_SEVERITY);
             }
         }
         return (JahiaField)theField.clone();
     }

 } // loadField
 */


    /**
     * loads a field from the database, according to the loadflags
     * NO RIGHTS CHECKS! use another loadField method (with jParams) for that.
     *
     * @param fieldID  the field ID
     * @param loadFlag the load flags
     *
     * @return a JahiaField object
     *
     * @see org.jahia.data.fields.JahiaField
     * @see org.jahia.data.fields.LoadFlags
     */
    public JahiaField loadField (int fieldID, int loadFlag)
            throws JahiaException {
        return loadField (fieldID, loadFlag, null, EntryLoadRequest.CURRENT);
    } // end loadField


    /**
     * loads a field from the database, with all values
     *
     * @param fieldID the field ID
     * @param jParams the ProcessingContext object with request and response
     *
     * @return a JahiaField object
     *
     * @see org.jahia.data.fields.JahiaField
     * @see org.jahia.data.fields.LoadFlags
     */
    public JahiaField loadField (int fieldID, ProcessingContext jParams)
            throws JahiaException {
        return loadField(fieldID, LoadFlags.ALL, jParams,
                jParams != null ? jParams.getEntryLoadRequest()
                        : EntryLoadRequest.CURRENT);
    } // loadField


    /**
     * loads a field, and accept loadFlag and ProcessingContext <br>
     * uses loadField ( int fieldID ) method to access cache/database
     *
     * @param fieldID  the field ID
     * @param loadFlag the load flags
     * @param jParams   ProcessingContext
     *
     * @return a JahiaField object
     *         DJ 28.01.2001 added ACL support
     *         DJ 28.01.2001 added ACL support
     *         DJ 28.01.2001 added ACL support
     */
    // DJ 28.01.2001 added ACL support

    public JahiaField loadField (int fieldID, int loadFlag, ProcessingContext jParams)
            throws JahiaException {
        if (jParams != null) {
            return loadField (fieldID, loadFlag, jParams, jParams.getEntryLoadRequest ());
        } else {
            logger.debug (
                    "LEGACY : Loading current version by default ! Try to use the newer APIs to avoid this message! ");
            return loadField (fieldID, loadFlag, jParams, EntryLoadRequest.CURRENT);

        }
    }

    public JahiaField loadField(int fieldID, int loadFlag,
            ProcessingContext jParams, EntryLoadRequest loadVersion)
            throws JahiaException {
        return loadField(fieldID, loadFlag, jParams, loadVersion, 0);
    }
    
    public JahiaField loadField (int fieldID, int loadFlag, ProcessingContext jParams,
                                 EntryLoadRequest loadVersion, int parentCtnListID)
            throws JahiaException {
        if (logger.isDebugEnabled()) {
            String msg = "loading field: id=" + fieldID ;
            if ( loadVersion != null ){
                msg += " loadVersion=[" + loadVersion.toString() + "]";
            }
            logger.debug(msg);
        }

        ContentField  contentField = ContentField.getField(fieldID);
        if (contentField == null) {
            return null;
        }
        JahiaField theField = contentFieldToJahiaField(contentField, loadVersion);

        if (theField == null) {
            return null;
        }
        if ( loadVersion.isCurrent()
                && theField.getWorkflowState() != EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
            // we only allow live data
            return null;
        } else if ( loadVersion.isStaging() && !ParamBean.COMPARE.equals(jParams.getOpMode()) ){
            if (!loadVersion.isWithMarkedForDeletion()
                    && contentField.isMarkedForDelete(theField.getLanguageCode())) {
                // we don't want marked for delete data
                return null;
            }
        }

        /* Doesn't work with engine like "searchengine"
        if ( theField != null
        && !"core".equalsIgnoreCase(jParams.getEngine()) ){
        // we found an active but we need to check that it is not marked for delete
        if (contentField == null) {
        contentField = ContentField.getField(fieldID);
        }
        if ( contentField.isMarkedForDelete(theField.getLanguageCode()) ){
        return null;
        }
        }*/

        // check fields ACL
        if (jParams == null) {
            // logger.debug ("loadField(" + theField.getDefinition().getName() + "): can't check ACL, method called without ProcessingContext");
        } else {
            JahiaUser currentUser = jParams.getUser ();
            if (currentUser != null) {
                // logger.debug("loadField(" + theField.getDefinition().getName() + "): checking rights...");
                // If containerId is greater than zero then the fieldRights are defined in the container list properties
                // so we must get the container list and check
                if (theField.getctnid() > 0) {
                    if (parentCtnListID == 0) {
                        ContentContainerKey cck = (ContentContainerKey) ((ContentObjectKey) contentField
                                .getObjectKey()).getParent(loadVersion);
                        if (cck != null) {
                            ContentContainerListKey cclk = (ContentContainerListKey) cck
                                    .getParent(loadVersion);
                            parentCtnListID = cclk.getIdInType();
                        }
                    }
                    ContentContainerList ccl = ContentContainerList
                            .getContainerList(parentCtnListID);
                    if (!ccl.isFieldReadable(
                            theField.getDefinition().getName(), currentUser)) {
                        if (theField.getValue().toUpperCase().indexOf(
                                "JAHIA_LINKONLY") == -1) {
                            theField.setValue(""); // return an empty field
                        }
                        return theField;
                    }

                }
                // if the user has no read rights, return the field with an empty value.
                else if (!theField.checkReadAccess (currentUser)) {
                    //logger.debug ("loadField(" + theField.getDefinition().getName() + "): NO read rights! -> return an empty field");
                    if (theField.getValue ().toUpperCase ().indexOf ("JAHIA_LINKONLY") == -1) {
                        theField.setValue ("");	// return an empty field
                    }
                    return theField;
                }
                // logger.debug ("loadField(" + theField.getDefinition().getName() + "): read rights OK");
            } else {
                throw new JahiaException ("No user present !",
                        "No current user defined in the params in loadField() method.",
                        JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
            }
        }

        // We can use the Field Cache only to store instance of JahiaField
        // but each time we get it from this cache, we need to call it's load()
        // method to ensure dynamically Expression evaluation and Contextual
        // ( multilanguage ) processing of field value.

        boolean compareMode = (jParams != null && ProcessingContext.COMPARE.equals(jParams.getOpMode()));
        EntryLoadRequest effectiveLoadRequest = (EntryLoadRequest)loadVersion.clone();
        effectiveLoadRequest.setCompareMode(compareMode);
        if ( effectiveLoadRequest.isVersioned() ){
            if ( theField.getVersionID() > 0 ){
                effectiveLoadRequest.setVersionID(theField.getVersionID());
            }
        }
        loadField(theField, loadFlag, jParams, effectiveLoadRequest);

        return theField;

    } // loadField

    public List<JahiaField> loadFields(List<Integer> fieldIDs, int loadFlag,
            ProcessingContext jParams, EntryLoadRequest loadVersion,
            int parentCtnListID) throws JahiaException {
        if (logger.isDebugEnabled()) {
            String msg = "loading field: id=" + fieldIDs;
            if (loadVersion != null) {
                msg += " loadVersion=[" + loadVersion.toString() + "]";
            }
            logger.debug(msg);
        }

        List<ContentField> contentFields = ContentField.getFields(fieldIDs,
                loadVersion, false);

        if (contentFields == null) {
            return null;
        }
        List<JahiaField> theFields = new ArrayList<JahiaField>(contentFields.size());
        
        for (ContentField contentField : contentFields) {
            JahiaField theField = contentFieldToJahiaField(contentField,
                    loadVersion);

            if (theField == null) {
                continue;
            }
            
            boolean compareMode = (jParams != null && ProcessingContext.COMPARE
                    .equals(jParams.getOpMode()));
            
            if (loadVersion.isCurrent()
                    && theField.getWorkflowState() != EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                // we only allow live data
                continue;
            } else if (loadVersion.isStaging()
                    && !compareMode
                    && !loadVersion.isWithMarkedForDeletion()
                    && contentField.isMarkedForDelete(theField
                            .getLanguageCode())) {
                // we don't want marked for delete data
                continue;
            }

            // check fields ACL
            if (jParams != null) {
                JahiaUser currentUser = jParams.getUser();
                if (currentUser != null) {
                    // logger.debug("loadField(" + theField.getDefinition().getName() + "): checking rights...");
                    // If containerId is greater than zero then the fieldRights are defined in the container list properties
                    // so we must get the container list and check
                    if (theField.getctnid() > 0) {
                        if (parentCtnListID == 0) {
                            ContentContainerKey cck = (ContentContainerKey) ((ContentObjectKey) contentField
                                    .getObjectKey()).getParent(loadVersion);
                            if (cck != null) {
                                ContentContainerListKey cclk = (ContentContainerListKey) cck
                                        .getParent(loadVersion);
                                parentCtnListID = cclk.getIdInType();
                            }
                        }
                        ContentContainerList ccl = ContentContainerList
                                .getContainerList(parentCtnListID);
                        if (!ccl.isFieldReadable(theField.getDefinition()
                                .getName(), currentUser)) {
                            if (theField.getValue().toUpperCase().indexOf(
                                    "JAHIA_LINKONLY") == -1) {
                                theField.setValue(""); // return an empty field
                            }
                            theFields.add(theField);
                            continue;
                        }

                    }
                    // if the user has no read rights, return the field with an empty value.
                    else if (!theField.checkReadAccess(currentUser)) {
                        // logger.debug ("loadField(" + theField.getDefinition().getName() + "): NO read rights! -> return an empty field");
                        if (theField.getValue().toUpperCase().indexOf(
                                "JAHIA_LINKONLY") == -1) {
                            theField.setValue(""); // return an empty field
                        }
                        theFields.add(theField);
                        continue;
                    }
                    // logger.debug ("loadField(" + theField.getDefinition().getName() + "): read rights OK");
                } else {
                    throw new JahiaException(
                            "No user present !",
                            "No current user defined in the params in loadField() method.",
                            JahiaException.USER_ERROR,
                            JahiaException.ERROR_SEVERITY);
                }
            }

            // We can use the Field Cache only to store instance of JahiaField
            // but each time we get it from this cache, we need to call it's load()
            // method to ensure dynamically Expression evaluation and Contextual
            // ( multilanguage ) processing of field value.

            EntryLoadRequest effectiveLoadRequest = (EntryLoadRequest) loadVersion
                    .clone();
            effectiveLoadRequest.setCompareMode(compareMode);
            if (effectiveLoadRequest.isVersioned()
                    && theField.getVersionID() > 0) {
                effectiveLoadRequest.setVersionID(theField.getVersionID());
            }
            
            theFields.add(loadField(theField, loadFlag, jParams,
                    effectiveLoadRequest));
        }
        return theFields;

    } // loadFields    
    
    /**
     * Performing the complete load on an existing JahiaField instance
     *
     * @param theField
     * @param loadFlag
     * @param jParams
     * @param loadVersion
     * @return
     * @throws JahiaException
     */
    private JahiaField loadField (JahiaField theField, int loadFlag, ProcessingContext jParams,
                                 EntryLoadRequest loadVersion)
            throws JahiaException {

        if ( theField == null ){
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                "loading field: id=" + theField.getID() + " loadVersion=[" +
                loadVersion.toString());
        }

//        ContentPage contentPage = ContentPage.getPage (theField.getPageID ());
//        int pageDefID = contentPage.getPageTemplateID (loadVersion);
//        //int pageDefID = ServicesRegistry.getInstance().getJahiaPageService().lookupPage (theField.getObjectKey(), jParams).getPageTemplateID();
//        int fieldType = (theDef.getType (pageDefID) != FieldTypes.UNDEFINED) ?
//                theDef.getType (pageDefID) : theField.getType ();
//
//
        // now we compose the content of fieldValue, depending of the field type and the loadFlag

        boolean fieldLoadedSuccessfully = false;
        // @todo : find a better way
        if (jParams != null ){
            EntryLoadRequest savedEntryLoadRequest = jParams.getSubstituteEntryLoadRequest();
            try {
                jParams.setSubstituteEntryLoadRequest (loadVersion);
                logger.debug ("Calling field specific load");
                theField.load (loadFlag, jParams, jParams.getEntryLoadRequest());
                theField.setHasChanged(false);
                fieldLoadedSuccessfully = true;
            } catch (Exception t) {
                logger.warn ("Error calling field load method fid[" + theField.getID () + "]", t);
                // Return the field anyway
                /*
                throw new JahiaException ("Error calling field specific load method !",
                "Error calling field specific load method !",
                JahiaException.DATA_ERROR, JahiaException.ERROR_SEVERITY);
                */
            } finally {
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
            }
        } else {
            theField.load (loadFlag, null, loadVersion);
            theField.setHasChanged(false);
        }

        if (theField.getValue () != null && theField.getValue ().equals ("<empty>")) {
            theField.setValue ("");
        }

        if (theField.getRawValue() != null && theField.getRawValue().equals ("<empty>")) {
            theField.setRawValue("");
        }

        if ((theField != null) && (fieldLoadedSuccessfully)) {
            if ( !(loadVersion.isVersioned()) ) {
                // FIXME it is a quick short way to deactivate cache of JahiaField by never cache them.
                // We should condidere caching ContentField object only,
                // not JahiaField.
                // cacheFields.put(getCacheFieldEntryKey(theField.getID(), theField.getWorkflowState()),
                //                theField);
            }
        }

        return theField;
    } // loadField

    /**
     * saves a field if the user has the correct rights.
     * Check user's rights
     *
     * @param theField    the JahiaField to save
     * @param parentAclID the Acl parent ID (or 0 to keep existing parent ACL ID)
     * @param jParams     ProcessingContext
     */
    public void saveField (JahiaField theField, int parentAclID, ProcessingContext jParams)
            throws JahiaException {
        logger.debug ("saving field #" + theField.getID ());
        String createField = "";
        if (theField.getObject () instanceof String) {
            createField = (String) theField.getObject ();
        }

        // Check the ACL.
        if (jParams != null) {
            JahiaUser currentUser = jParams.getUser ();
            if (currentUser != null) {
                logger.debug ("checking rights...");
                // if the user has no write rights, exit.
                // if fieldID = 0 then let's say the user can write (add)
                if ((!theField.checkWriteAccess (currentUser)) && (theField.getID () != 0)) {
                    if (createField.equals ("createFieldWithCurrentID")) {
                        logger.debug ("write rights OK");
                    } else {
                        logger.debug ("NO write right");
                        return;
                    }
                }
                //logger.debug ("saveField(): write rights OK");
            } else {
                throw new JahiaException ("No user present !",
                        "No current user defined in the params in saveField() method.",
                        JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
            }
        }

        int savemode = 1;       // 0 = create,  1 = update


        if (createField.equals ("createFieldWithCurrentID")) {
            //logger.debug("########### createFieldWithcurrentID- set savemode");
            theField.setObject (null);
            // the fielID is given in theField, because we want to re-create the field
            savemode = 0;
        }

        if (theField.getID () == 0) {
            savemode = 0;
        }

        String errorMsg = "";
        if (theField.getDefinition ().getName ().equals ("")) {
            errorMsg = "Error in FieldBaseService : field name value is an empty string";
        }
        if (theField.getDefinition ().getTitle ().equals ("")) {
            errorMsg = "Error in FieldBaseService : field title value is an empty string";
        }
        if ((theField.getValue () != null) && theField.getValue ().equals ("")) {
            theField.setValue ("<empty>");
        }

        if (errorMsg != "") {
            //logger.debug( errorMsg );
            throw new JahiaException ("Cannot update fields in the database",
                    errorMsg, JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY);
        }

        // sets field value in tmpVal
        String tmpVal = "";

        if (theField.getValue () != null) {
            tmpVal = theField.getValue ();
        }

        // creates ACL, if needed
        if (theField.getAclID () == 0) {
            if ( theField.getctnid() == 0 && ! theField.getIsMetadata() ){
                // End Create ACL
                theField.setAclID (parentAclID);
            } else {
                theField.setAclID (parentAclID);
            }    
        }

        //--------- start add to search engine + fire event --------------
        if (savemode == 0)
        // CREATE
        {
            logger.debug ("CREATE - savemode: " + savemode);
            //JahiaField theTempField = (JahiaField)theField.clone();
            //theTempField.setValue (tmpVal);

            theField.setValue (tmpVal);

            theField.save (jParams);

            if (theField.getDefinition().getJahiaID() != 0) {
                ContentField contentField = ContentField.getField(theField.getID());
                JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, contentField);
                ServicesRegistry.getInstance ().getJahiaEventService ().fireContentObjectCreated(objectCreatedEvent);
            }

            // if not in staging, we set the value

            /*
            if (!ServicesRegistry.getInstance ().getJahiaVersionService ().getSiteSaveVersion (
                    theField.getJahiaID ())
                    .isStaging ())
                cacheFields.put (getCacheFieldEntryKey(theField.getID (), theField.getWorkflowState()), theField);
             */
            invalidateCacheField(theField.getID());

            // Search Engine
            /*
            if ( (theField.getConnectType() == ConnectionTypes.LOCAL) ||
                 (theField.getConnectType() == ConnectionTypes.HTMLEDITOR) ||
                 (theField.getConnectType() == ConnectionTypes.HTMLEDITORAX) )
            {
                int workflowState = theField.getWorkflowState();
                if ( workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                    workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                }
                ServicesRegistry.getInstance().getJahiaSearchService().addFieldToSearchEngine( theField , workflowState);
            }*/

        } else
        // UPDATE
        {
            logger.debug ("UPDATE - savemode: " + savemode);
            //boolean isStagingEnabled = ServicesRegistry.getInstance ().getJahiaVersionService ().isStagingEnabled (theField.getJahiaID ());

            // never used ??
            //JahiaField oldField  = this.loadField( theField.getID(), LoadFlags.TEXTS, jParams );

            logger.debug ("UPDATE - step1 ");

            /*
            JahiaField theTempField = (JahiaField)theField.clone();
            theTempField.setValue (tmpVal);
            */
            theField.setValue (tmpVal);

            logger.debug ("UPDATE - step2 ");

            ContentField contentField = ContentField.getField(theField.getID());
            JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, contentField);
            ServicesRegistry.getInstance ().getJahiaEventService ()
                .fireContentObjectUpdated(objectCreatedEvent);

            logger.debug ("UPDATE - step3 ");

            // if it is current version, we set the value

            /*
            if (!ServicesRegistry.getInstance ().getJahiaVersionService ().getSiteSaveVersion (
                    theField.getJahiaID ())
                    .isStaging ())
                cacheFields.put (getCacheFieldEntryKey(theField.getID (), theField.getWorkflowState()), theField);
             */

            invalidateCacheField(theField.getID());

            /*
            int workflowState = theField.getWorkflowState();
            if ( workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                workflowState = EntryLoadRequest.STAGING_WORKFLOW_STATE;
            }
            if (theField.getConnectType() == ConnectionTypes.LOCAL) {
                ServicesRegistry.getInstance().getJahiaSearchService().addFieldToSearchEngine( theField, workflowState );
            }
            // JB 02.08.2001 - Add HTMLEditor Types
            if (theField.getConnectType() == ConnectionTypes.HTMLEDITOR) {
                ServicesRegistry.getInstance().getJahiaSearchService().addFieldToSearchEngine( theField, workflowState);
            }
            if (theField.getConnectType() == ConnectionTypes.HTMLEDITORAX) {
                ServicesRegistry.getInstance().getJahiaSearchService().addFieldToSearchEngine( theField, workflowState );
            }*/

            logger.debug ("UPDATE - step4 ");

            // fire event if we're not in a PortletList container
            if (logMe (theField)) {
                JahiaEvent theEvent = new JahiaEvent (this, jParams, theField);
                ServicesRegistry.getInstance ().getJahiaEventService ().
                        fireUpdateField (theEvent);
            }
            theField.save (jParams);
        }
        //--------- end add to search engine + fire event --------------

        //JahiaSaveVersion saveVersion = ServicesRegistry.getInstance ().getJahiaVersionService ().getSiteSaveVersion (theField.getJahiaID ());
        // saves field additionnal info, if needed

        // fire event if we're not in a PortletList container
        if (jParams != null) {
            // fire event if we're not in a PortletList container
            if (logMe (theField)) {
                JahiaEvent theEvent = new JahiaEvent (this, jParams, theField);
                if (savemode == 0) {
                    ServicesRegistry.getInstance ().getJahiaEventService ().
                            fireAddField (theEvent);
                } else {
                    ServicesRegistry.getInstance ().getJahiaEventService ().
                            fireUpdateField (theEvent);
                }
            }
        }

    } // saveField

    public void markPageFieldsLanguageForDeletion (int pageID,
                                                   JahiaUser user,
                                                   String languageCode,
                                                   StateModificationContext
            stateModifContext)
            throws JahiaException {
        for (int id : dataManager.getActiveOrStagedFieldIDsInPage (pageID)) {
            ContentField theField = ContentField.getField (id);
            // We have to delete direct page field only ( fields that aren't in a container ! )
            // fields that are inside a container should not be deleted here,
            // but when deleting these containers.
            if (theField != null && (theField.getContainerID () == 0)) {
                theField.markLanguageForDeletion (user, languageCode, stateModifContext);
            }
        }
    }

    public void purgePageFields (int pageID)
            throws JahiaException {
        for (int id : dataManager.getNonContainerFieldIDsInPageByWorkflowState(pageID,null)) {
            ContentField theField = ContentField.getField (id);
            theField.purge ();
        }
    }

    public ActivationTestResults areFieldsValidForActivation (
            Set<String> languageCodes,
            int pageID,
            JahiaUser user,
            JahiaSaveVersion saveVersion,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {

        ActivationTestResults activationResults = new ActivationTestResults ();
        // for each field, we check if the user has write+admin access to it,
        // if so we can validate it
        for (int id : dataManager.getOnlyStagedFieldIdsInPage(pageID)) {
            ContentField theField = ContentField.getField (id);
            ActivationTestResults fieldResult = theField.isValidForActivation (languageCodes,
                    jParams, stateModifContext);

            if (fieldResult.getStatus () == ActivationTestResults.FAILED_OPERATION_STATUS) {
                fieldResult.setStatus (ActivationTestResults.PARTIAL_OPERATION_STATUS);
                fieldResult.moveErrorsToWarnings ();
            }
            activationResults.merge (fieldResult);

        }
        return activationResults;

    }

    public ActivationTestResults areNonContainerFieldsValidForActivation (
            Set<String> languageCodes,
            int pageID,
            JahiaUser user,
            JahiaSaveVersion saveVersion,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationResults = new ActivationTestResults();
        if (org.jahia.settings.SettingsBean.getInstance().areDeprecatedNonContainerFieldsUsed()) {
            // for each field, we check if the user has write+admin access to it,
            // if so we can validate it
            for (int id : dataManager
                    .getOnlyStagedNonContainerFieldIdsInPage(pageID)) {
                ContentField theField = ContentField.getField(id);
                ActivationTestResults fieldResult = theField
                        .isValidForActivation(languageCodes, jParams,
                                stateModifContext);
                activationResults.merge(fieldResult);
            }
        }
        return activationResults;
    }


    /**
     * Validate the fields of the page to which the user has admin AND write access
     * i.e. now Staged fields are Active.
     * No rights checks are done here.
     *
     * @param saveVersion it must contain the right versionID and staging/versioning
     *                    info of the current site
     */
    public ActivationTestResults activateStagedFields (
            Set<String> languageCodes,
            int pageID,
            JahiaUser user,
            JahiaSaveVersion saveVersion,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {

        ActivationTestResults activationResults = new ActivationTestResults ();

        activationResults.merge (
                areFieldsValidForActivation (languageCodes, pageID, user, saveVersion, jParams,
                        stateModifContext));
        if (activationResults.getStatus () == ActivationTestResults.FAILED_OPERATION_STATUS) {
            return activationResults;
        }

        // for each field, we check if the user has write+admin access to it,
        // if so we can validate it
        for (int id : dataManager.getOnlyStagedFieldIdsInPage(pageID)) {
            logger.debug ("Attempting to validate field : " + id);

            // should we add right checks here as they were previously ?

            ContentField contentField = ContentField.getField (id);
            if ( contentField instanceof ContentPageField ){
                // NK
                // There are some issues with Page Move and Activation
                // Suppose this page field is marked for delete because its page
                // is moved to another location.
                // If we activate this field without acitvating
                // the moved page, we create an inconsistant state that can
                // make the moved page a Phantom page ( not referred by any page field),
                // it is the case if we fail activate the moved page accordingly with this page field.
                // We can either skip activate this field ( it would be activate only when
                // directly applying activation on the moved page, which internally handle correctly
                // page move ).
                //..handled in contentField is content valid for activation check
                ActivationTestResults fieldResult =
                    contentField.isValidForActivation (languageCodes, jParams, stateModifContext);
                if ( fieldResult.getStatus () ==
                     ActivationTestResults.FAILED_OPERATION_STATUS ){
                    continue;
                }
            }

             // fire event
             JahiaEvent theEvent = new JahiaEvent(  saveVersion, jParams, contentField );
             ServicesRegistry.getInstance().getJahiaEventService()
                     .fireBeforeFieldActivation(theEvent);

            ActivationTestResults contentResult = contentField.activate (languageCodes,
                    saveVersion.getVersionID (), jParams, stateModifContext);

            activationResults.merge (contentResult);
            if (contentResult.getStatus () == ActivationTestResults.COMPLETED_OPERATION_STATUS) {
                // invalidate corresponding cache entries
                invalidateCacheField(id);
            } else {
                logger.debug (
                        "Field " + id + " activation not completely performed. testResult=" + contentResult);
            }
        }


        return activationResults;
    }

    /**
     * Sets the workflow state for all the field in a page for a given set
     * of languages.
     *
     * @param languageCodes    the set of language for which to change the state
     *                         in the fields
     * @param newWorkflowState the new workflow state for the fields
     * @param pageID           the page on which
     * @param jParams          a ProcessingContext used by the individual fields to change their
     *                         state
     *
     * @throws JahiaException in the case there were problems communicating
     *                        with the persistant storage system
     */
    public void setFieldsLanguageStates (
            Set<String> languageCodes,
            int newWorkflowState,
            int pageID,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {
        // for each field, we check if the user has write+admin access to it,
        // if so we can validate it
        for (int id : dataManager.getOnlyStagedFieldIdsInPage(pageID)) {
            // should we add right checks here as they were previously ?
            
            // PAP: forceLoadFromDB, because a new cache object should be put to 
            // enable rollback functionality
            ContentField contentField = ContentField.getField (id, true); 
            contentField.setWorkflowState (languageCodes, newWorkflowState, jParams,
                    stateModifContext);
        }

    }


//    /**
//     * Returns the staging workflow state of the fields for all the languages
//     * on the specified page.
//     * Normally as currently we only do state changes on a page basis these
//     * should all be equal. This does not retrieve the state of absolute fields.
//     *
//     * @param pageID the page id on which to retrieve the fields workflow state
//     *
//     * @return a Map that contains all the states of the languages for the
//     *         fields on the page (for the moment we assume all the objects are in the
//     *         same state)
//     *
//     * @throws JahiaException in the case we couldn't load a field.
//     */

//    public Map getFieldsLanguagesState (int pageID)
//            throws JahiaException {
//
//        List fieldIDs = f_utils.getActiveOrStagedFieldIDsInPage (pageID);
//        ContentField.preloadActiveOrStagedFieldsByPageID (pageID);
//
//        Map result = new HashMap();
//
//        for (int i = 0; i < fieldIDs.size (); i++) {
//            int id = ((Integer) fieldIDs.elementAt (i)).intValue ();
//
//            ContentField theField = ContentField.getField (id);
//            Map fieldLanguagesStates = theField.getLanguagesStates ();
//            Iterator fieldLanguagesStatesIter = fieldLanguagesStates.keySet ().iterator ();
//            while (fieldLanguagesStatesIter.hasNext ()) {
//                String curLanguageCode = (String) fieldLanguagesStatesIter.next ();
//                Integer languageState = (Integer) fieldLanguagesStates.get (curLanguageCode);
//                Integer resultState = (Integer) result.get (curLanguageCode);
//                if (resultState != null) {
//                    if (resultState.intValue () < languageState.intValue ()) {
//                        result.put (curLanguageCode, languageState);
//                    }
//                } else {
//                    result.put (curLanguageCode, languageState);
//                }
//            }
//        }
//        return result;
//    }

    /**
     * Loads a Field Definition by its id
     *
     * @param defID the field definition id
     *
     * @return a JahiaFieldDefinition object
     *
     * @see org.jahia.data.fields.JahiaFieldDefinition
     */
    public JahiaFieldDefinition loadFieldDefinition (int defID)
            throws JahiaException {
        return definitionManager.loadFieldDefinition(defID);
    } // loadFieldDefinition

    /**
     * Load a field definition by it's siteID and definition name
     *
     * @param siteID         the identifier for the site for which to load the
     *                       definition
     * @param definitionName the unique identifier name for the definition on
     *                       the site
     *
     * @return a valid JahiaFieldDefinition object if found in the database.
     *
     * @throws JahiaException if there was a problem communicating with the
     *                        database.
     */
    public JahiaFieldDefinition loadFieldDefinition (int siteID,
                                                     String definitionName)
            throws JahiaException {
        return definitionManager.loadFieldDefinition(siteID, definitionName);
    } // loadFieldDefinition

    /**
     * Load all def Ids of the given name.
     *
     * @param definitionName
     * @param isMetadata if true, return only metadata field definition
     * @param definitionName
     * @param isMetadata
     * @return
     * @throws JahiaException
     */
    public List<Integer> loadFieldDefinitionIds(String definitionName, boolean isMetadata)
    throws JahiaException {
        return definitionManager.getDefinitionIds(definitionName,isMetadata);
    }

    /**
     * Load all def Ids of the given name.
     *
     * @param definitionNames
     * @param isMetadata if true, return only metadata field definition
     * @param isMetadata
     * @return
     * @throws JahiaException
     */
    public List<Integer> loadFieldDefinitionIds(String[] definitionNames, boolean isMetadata)
    throws JahiaException {
        return definitionManager.getDefinitionIds(definitionNames,isMetadata);    
    }

    /**
     * Load all def Ids of the given name.
     *
     * @param ctnType
     * @param isMetadata if true, return only metadata field definition
     * @param isMetadata
     * @return
     * @throws JahiaException
     */
    public List<Integer> getFieldDefinitionNameFromCtnType(String ctnType, boolean isMetadata)
    throws JahiaException {
        return definitionManager.getFieldDefinitionNameFromCtnType(new String[]{ctnType},isMetadata);
    }

    /**
     * Load all def Ids of the given name.
     *
     * @param ctnTypes
     * @param isMetadata if true, return only metadata field definition
     * @param isMetadata
     * @return
     * @throws JahiaException
     */
    public List<Integer> getFieldDefinitionNameFromCtnType(String[] ctnTypes, boolean isMetadata)
    throws JahiaException {
        return definitionManager.getFieldDefinitionNameFromCtnType(ctnTypes,isMetadata);
    }

    /**
     * Saves a Field Definition
     *
     * @param theDef the JahiaFieldDefinition object to save
     *
     * @see org.jahia.data.fields.JahiaFieldDefinition
     */
    public void saveFieldDefinition (JahiaFieldDefinition theDef)
            throws JahiaException {
        definitionManager.saveFieldDefinition(theDef);
    } // saveFieldDefinition

    /**
     * Delete a Field Definition and it's sub definition.
     *
     * @param fieldDefID the field def ID
     */
    public void deleteFieldDefinition (int fieldDefID)
            throws JahiaException {

        logger.debug ("fieldDef=" + fieldDefID);
        //remove from registry
        JahiaFieldDefinitionsRegistry.getInstance ().removeFieldDefinition (fieldDefID);

        // remove from database
        definitionManager.deleteFieldDefinition(fieldDefID);
    }

    //-------------------------------------------------------------------------
    private boolean logMe (JahiaField theField) {
        boolean out = false;
        int ctnid = theField.getctnid ();
        if (ctnid > 0) {
            ServicesRegistry sReg = ServicesRegistry.getInstance ();
            if (sReg != null) {
                JahiaContainersService ctnSrv = sReg.getJahiaContainersService ();
                if (ctnSrv != null) {
                    try {
                        ContentContainer container = ContentContainer.getContainer(ctnid);
                        if (container != null) {
                            String containerName = ctnSrv.loadContainerDefinition(container.getDefinitionID (null)).getName ();
                            if (!containerName.equals ("PortletList")) {
                                out = true;
                            }
                        }
                    } catch (JahiaException je) {
                        // do nothing
                    }
                }
            }
        }
        return out;
    }

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all fields of a site
     *
     * @param siteID
     */
    public JahiaDOMObject getFieldsAsDOM (int siteID) throws JahiaException {
        return null;
    }


    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all field def of a site
     *
     * @param siteID
     */
    public JahiaDOMObject getFieldDefsAsDOM (int siteID) throws JahiaException {

        return null;

    }


    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all field def props of a site
     *
     * @param siteID
     */
    public JahiaDOMObject getFieldDefPropsAsDOM (int siteID) throws JahiaException {

        return null;

    }

    //--------------------------------------------------------------------------
    /**
     * Returns a List of all Acl ID used by fields for a site
     * Need this for site extraction
     *
     * @param siteID
     */
    public List<Integer> getAclIDs (int siteID)
            throws JahiaException {
        return dataManager.getAllAclId (siteID);
    }


    /**
     * Creates a JahiaField from a Content field.
     *
     * @param contentField the content field source
     *                     state for which to construct the JahiaField
     *
     * @return a JahiaField corresponding to the language, workflow state and
     *         eventually version ID specified in the EntryLoadRequest object inside
     *         the ProcessingContext object.
     *
     * @throws JahiaException in case we have trouble loading or accessing the
     *                        entry state of the content field.
     */
    public JahiaField contentFieldToJahiaField (
            ContentField contentField,
            EntryLoadRequest entryLoadRequest)
            throws JahiaException {

        logger.debug ("Making JahiaField facade for field " + contentField.getID ());

        ContentObjectEntryState entryState =
                contentField.getEntryState (entryLoadRequest);

        boolean forComparisonOnly = false;

        if (entryState == null) {

            boolean compareMode = ( Jahia.getThreadParamBean() != null
                    && ProcessingContext.COMPARE.equals(Jahia.getThreadParamBean().getOpMode()));
            if ( compareMode && entryLoadRequest != null ){
                EntryLoadRequest loadRequest = (EntryLoadRequest)EntryLoadRequest.STAGED.clone();
                loadRequest.setLocales(entryLoadRequest.getLocales());
                loadRequest.setWithDeleted(true);
                loadRequest.setWithMarkedForDeletion(true);
                entryState = contentField.getEntryState (loadRequest);
                forComparisonOnly = true;
            }
        if (entryState == null) {
            logger.debug (
                    "Entry state " + entryLoadRequest + " not found for field " + contentField.getID () + ", returning null field !");
            return null;
        }
        }
        JahiaField jahiaField = createJahiaField (
                contentField.getID (),
                contentField.getSiteID (),
                contentField.getPageID (),
                contentField.getContainerID (),
                contentField.getFieldDefID (),
                contentField.getType (),
                contentField.getConnectType (),
                contentField.getDBValue (entryState),
                0, // ranking in field is deprecated.
                contentField.getAclID (),
                entryState.getVersionID (),
                entryState.getWorkflowState (),
                entryState.getLanguageCode ());
        jahiaField.setForComparisonOnly(forComparisonOnly);

        logger.debug (
                "Returning JahiaField facade for field " + contentField.getID () + " using language code=" + jahiaField.getLanguageCode ());
        return jahiaField;
    }

    /**
     * Compose the file absolute path to a flat file containing the default value for a field definition.
     *
     * @param siteID
     * @param pageDefID
     * @param name
     *
     * @return a String containing the file path that points to the default value for the field definition.
     */
    public String composeFieldDefDefaultValueFilePath (int siteID, int pageDefID, String name) {
        StringBuffer buff = new StringBuffer (Jahia.getSettings ().getJahiaVarDiskPath ());
        buff.append (File.separator);
        buff.append ("field_definitions");
        buff.append (File.separator);
        buff.append (siteID);
        buff.append ("_");
        if (pageDefID > 0) {
            buff.append (pageDefID);
            buff.append ("_");
        }
        buff.append (name);
        buff.append ("_defaultvalue.txt");
        return buff.toString ();
    }

    /**
     * Invalidates a JahiaField cache entry.
     * @param fieldID the identifier for the field entry to invalidate in the
     * cache.
     */
    public void invalidateCacheField (int fieldID) {
        ContentField.removeFromCache(fieldID);
    }

    private void createFileDefinitionRepository () {

        StringBuffer buff =
                new StringBuffer (settingsBean.getJahiaVarDiskPath ());
        buff.append (File.separator);
        buff.append ("field_definitions");
        File f = new File (buff.toString ());
        f.mkdir ();
    }

    private String checkFieldEnumerationValues (String fieldValue) {
        String value = fieldValue;
        if (fieldValue != null
                && (fieldValue.toLowerCase ().startsWith ("<jahia_multivalue"))) {
            value = "";
            int pos = fieldValue.indexOf ("[");
            if (pos != -1) {
                int pos2 = fieldValue.indexOf ("]>");
                if (pos2 != -1) {
                    value = fieldValue.substring (pos2 + 2, fieldValue.length ());
                }
            }
        }
        return value;
    }

    public ContentObjectKey getFieldParentObjectKey(int id, EntryLoadRequest req) {
        int[] i = dataManager.getParentIds(id, req);
        if (i[1] > 0) {
            return new ContentContainerKey(i[1]);
        } else if (i[0] > 0) {
            return new ContentPageKey(i[0]);
        } else {
            return null;
        }
    }

    public int[] getSubPageIdAndType(int id, EntryLoadRequest req) {
        return dataManager.getSubPageId(id, req);
    }

    public List<Integer> getFieldIDsOnPagesHavingAcls(Set<Integer> pageIDs, Set<Integer> aclIDs) {
        return dataManager.getFieldIDsOnPagesHavingAcls(pageIDs, aclIDs);
    }

    public Map<String, String> getVersions(int site, String lang) {
        return dataManager.getVersions(site, lang);
    }


    public List<ContentField> findFieldsByPropertyNameAndValue(String name, String value) throws JahiaException {
        List<Integer> l = dataManager.findFieldIdByPropertyNameAndValue(name, value);
        List<ContentField> r = new ArrayList<ContentField>();
        for (Integer integer : l) {
            r.add(ContentField.getField(integer.intValue()));
        }
        return r;
    }

    public List<Object[]> getFieldPropertiesByName(String name) {
        return dataManager.getFieldPropertiesByName(name);
    }


}