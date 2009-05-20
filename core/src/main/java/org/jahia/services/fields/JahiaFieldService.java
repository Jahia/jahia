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
//
//  FieldServices
//  EV      31.10.2000
//  FH      28.12.2000  Changed interface to abstract class
//  DJ      05.01.2001  Changed almost everything
//

package org.jahia.services.fields;

import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaDOMObject;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.jahia.utils.xml.XMLSerializationOptions;
import org.jahia.utils.xml.XmlWriter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Map;

public abstract class JahiaFieldService extends JahiaService {

    /**
     * create a JahiaField.
     * The only method to instanciate a new JahiaField
     * It call the constructor corresponding to the field type.
     *
     * @param ID
     * @param jahiaID
     * @param pageID
     * @param ctnid
     * @param fieldDefID
     * @param fieldType
     * @param connectType
     * @param fieldValue
     * @param rank
     * @param aclID
     * @param versionID
     * @param workflowState
     * @param languageCode
     *
     * @return
     *
     * @throws JahiaException
     */
    public abstract JahiaField createJahiaField (int ID,
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
                                                 int workflowState,
                                                 String languageCode)
            throws JahiaException;

    public abstract JahiaField createJahiaField (int ID,
                                                 int jahiaID,
                                                 int pageID,
                                                 int ctnid,
                                                 int fieldDefID,
                                                 int fieldType,
                                                 int connectType,
                                                 String fieldValue,
                                                 int rank,
                                                 int aclID)
            throws JahiaException;

    /**
     * Gets all field IDs in a page (from page ID)
     *
     * @param pageID the page ID
     *
     * @return a List of Field list IDs
     */
    public abstract List<Integer> getNonContainerFieldIDsInPage (int pageID)
            throws JahiaException;

    /**
     * Gets all field IDs in a page in a specified workflow state
     *
     * @param pageID      the page ID.
     * @param loadVersion the workflow state field to load.
     *
     * @return A List of Integer corresponding to the field IDs.
     */
    public abstract List<Integer> getNonContainerFieldIDsInPageByWorkflowState (int
        pageID,
                                                                         EntryLoadRequest loadVersion)
            throws JahiaException;

    /**
     * Gets all field IDs in a page in a specified workflow state
     *
     * @param pageID      the page ID.
     * @param loadVersion the workflow state field to load.
     *
     * @return A List of Integer corresponding to the field IDs.
     */
    public abstract List<Object[]> getNonContainerFieldIDsAndTypesInPageByWorkflowState (int
        pageID,
                                                                         EntryLoadRequest loadVersion)
            throws JahiaException;

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
    public abstract SortedSet<ContentObjectEntryState> getNonContainerFieldEntryStateInPage (int pageID)
            throws JahiaException;

    /**
     * Given a page ID, get all the IDs of the page links contained in
     * the page, whether of type DIRECT, INTERNAL or URL
     *
     * @param pageID the page ID
     *
     * @return a List of Field list IDs
     */
    public abstract List<Integer> getPagefieldIDsInPage (int pageID)
            throws JahiaException;

    /**
     * Gets a field ID from its name and page id
     *
     * @param fieldName the field name
     * @param pageID    the page ID
     *
     * @return a field id
     */
    public abstract int getFieldID (String fieldName, int pageID)
            throws JahiaException;

    /**
     * Get all the fields ID (for the search engine)
     *
     * @return a List of field ids
     */
    public abstract List<Integer> getAllFieldIDs ()
            throws JahiaException;

    /**
     * Get all the fields ID for a gived site (for the search engine)
     *
     * @return a List of field ids
     *
     */
    public abstract List<Integer> getAllFieldIDs (int siteID)
            throws JahiaException;

    /**
     * gets all the field definition ids
     * to be changed, using a fielddefinition cache instead and changing the def registry.
     *
     * @return a List of field definition ids
     */
    public abstract List<Integer> getAllFieldDefinitionIDs ()
            throws JahiaException;
    
    /**
     * gets all the field definition ids for a site
     * to be changed, using a fielddefinition cache instead and changing the def registry.
     *
     * @return a List of field definition ids for a site
     */
    public abstract List<Integer> getAllFieldDefinitionIDs (int siteId)
            throws JahiaException;    

    /***
     * loads a field from the database, no other info than what's in the database<br>
     * NO RIGHTS CHECK!
     *
     * @param        fieldID             the field ID
     * @return       a JahiaField object
     * @see          org.jahia.data.fields.JahiaField
     *
     */
    /*
 public abstract JahiaField loadFieldInfo( int fieldID )
     throws JahiaException;
 public abstract JahiaField loadFieldInfo( int fieldID, EntryLoadRequest loadVersion )
     throws JahiaException;
*/

    /**
     * loads a field from the database, according to the loadflags.
     * Checks user's rights.
     *
     * @param fieldID  the field ID
     * @param loadFlag the load flags
     *
     * @return a JahiaField object
     *
     * @see org.jahia.data.fields.JahiaField
     * @see org.jahia.data.fields.LoadFlags
     */
    public abstract JahiaField loadField (int fieldID, int loadFlag)
            throws JahiaException;

    /**
     * loads a field from the database, with all values.
     * Checks user's rights.
     *
     * @param fieldID the field ID
     * @param jParams the ProcessingContext object with request and response
     *
     * @return a JahiaField object
     *
     * @see org.jahia.data.fields.JahiaField
     * @see org.jahia.data.fields.LoadFlags
     */
    public abstract JahiaField loadField (int fieldID, ProcessingContext jParams)
            throws JahiaException;

    /**
     * loads a field, and accept loadFlag and ProcessingContext <br>
     * uses loadField ( int fieldID ) method to access cache/database.
     * Checks user's rights.
     *
     * @param fieldID  the field ID
     * @param loadFlag the load flags
     *
     * @return a JahiaField object
     */
    public abstract JahiaField loadField (int fieldID, int loadFlag,
                                          ProcessingContext jParams)
            throws JahiaException;

    public abstract JahiaField loadField (int fieldID, int loadFlag,
                                          ProcessingContext jParams,
                                          EntryLoadRequest loadVersion)
            throws JahiaException;
    
    public abstract JahiaField loadField(int fieldID, int loadFlag,
            ProcessingContext jParams, EntryLoadRequest loadVersion,
            int parentListID) throws JahiaException;    

    public abstract List<JahiaField> loadFields(List<Integer> fieldIDs, int loadFlag,
            ProcessingContext jParams, EntryLoadRequest loadVersion,
            int parentCtnListID) throws JahiaException;
    
    /***
     * saves a field<br>
     * NO RIGHTS CHECK!
     *
     * @param        theField            the JahiaField to save
     * @param        parentAclID         the Acl parent ID
     *
     */

//    public abstract void saveField( JahiaField theField, int parentAclID )
//        throws JahiaException;

    /**
     * saves a field if the user has the correct rights.
     * Check user's rights
     *
     * @param theField    the JahiaField to save
     * @param parentAclID the Acl parent ID
     * @param jParam      ProcessingContext
     */
    public abstract void saveField (JahiaField theField, int parentAclID,
                                    ProcessingContext jParams)
            throws JahiaException;

    /**
     * Validates staged fields from a page, if the user has write+admin access to the fields
     */
    public abstract ActivationTestResults activateStagedFields (
            Set<String> languageCodes,
            int pageID,
            JahiaUser user, JahiaSaveVersion saveVersion,
            ProcessingContext jParams, StateModificationContext stateModifContext)
            throws JahiaException;

    /**
     * Marks all the fields in a page for deletion in a given set of languages
     *
     * @param pageID            the page for which to mark fields for deletion
     * @param user              the user performing the operation
     * @param languageCode      the language to mark for deletion
     * @param stateModifContext used to detect loops in deletion marking.
     *
     * @throws JahiaException thrown in case there are problems communicating
     *                        with the persistant store.
     */
    public abstract void markPageFieldsLanguageForDeletion (int pageID,
                                                            JahiaUser user,
                                                            String languageCode,
                                                            StateModificationContext
            stateModifContext)
            throws JahiaException;

    /**
     * Tests fields to know if they are ready for an activation process,
     * generating either warnings (such as a depending page not being validated)
     * or errors (such as a mandatory language missing)
     *
     * @param pageID
     * @param user
     * @param saveVersion
     * @param jParams
     *
     * @return
     *
     * @throws JahiaException
     */
    public abstract ActivationTestResults areFieldsValidForActivation (
            Set<String> languageCodes,
            int pageID,
            JahiaUser user,
            JahiaSaveVersion saveVersion,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException;

    /**
     * Tests non container fields to know if they are ready for an activation
     * process, generating either warnings (such as a depending page not being
     * validated) or errors (such as a mandatory language missing). Non
     * container fields are fields that are directly attached to a page and
     * aren't contained in any container.
     *
     * @param pageID
     * @param user
     * @param saveVersion
     * @param jParams
     *
     * @return
     *
     * @throws JahiaException
     */
    public abstract ActivationTestResults
        areNonContainerFieldsValidForActivation (
            Set<String> languageCodes,
            int pageID,
            JahiaUser user,
            JahiaSaveVersion saveVersion,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException;


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
    public abstract void setFieldsLanguageStates (
            Set<String> languageCodes,
            int newWorkflowState,
            int pageID,
            ProcessingContext jParams,
        StateModificationContext stateModifContext)
        throws JahiaException;

    /**
     * Loads a Field Definition by its id
     *
     * @param defID the field definition id
     *
     * @return a JahiaFieldDefinition object
     *
     * @see org.jahia.data.fields.JahiaFieldDefinition
     */
    public abstract JahiaFieldDefinition loadFieldDefinition (int defID)
            throws JahiaException;

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
    public abstract JahiaFieldDefinition loadFieldDefinition (int siteID,
                                                              String definitionName)
            throws JahiaException;

    /**
     * Load all def Ids of the given name.
     *
     * @param definitionName
     * @param isMetadata if true, return only metadata field definition
     * @return
     * @throws JahiaException
     */
    public abstract List<Integer> loadFieldDefinitionIds(String definitionName, boolean isMetadata)
            throws JahiaException;

    /**
     * Load all def Ids of the given names.
     *
     * @param definitionNames
     * @param isMetadata if true, return only metadata field definition
     * @return
     * @throws JahiaException
     */
    public abstract List<Integer> loadFieldDefinitionIds(String[] definitionNames, boolean isMetadata)
            throws JahiaException;

    /**
     * Load all def Ids of the given name.
     *
     * @param ctnType
     * @param isMetadata if true, return only metadata field definition
     * @param isMetadata
     * @return
     * @throws JahiaException
     */
    public abstract List<Integer> getFieldDefinitionNameFromCtnType(String ctnType, boolean isMetadata)
            throws JahiaException;

    /**
     * Load all def Ids of the given name.
     *
     * @param ctnTypes
     * @param isMetadata if true, return only metadata field definition
     * @param isMetadata
     * @return
     * @throws JahiaException
     */
    public abstract List<Integer> getFieldDefinitionNameFromCtnType(String[] ctnTypes, boolean isMetadata)
            throws JahiaException;

    /**
     * Saves a Field Definition
     *
     * @param theDef the JahiaFieldDefinition object to save
     *
     * @see org.jahia.data.fields.JahiaFieldDefinition
     */
    public abstract void saveFieldDefinition (JahiaFieldDefinition theDef)
            throws JahiaException;

    /**
     * Delete a Field Definition and it's sub definition.
     *
     * @param fieldDefID the field def ID
     */
    public abstract void deleteFieldDefinition (int fieldDefID)
            throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all fields of a site
     *
     * @param siteID siteID
     *
     * @auhtor NK
     */
    public abstract JahiaDOMObject getFieldsAsDOM (int siteID)
            throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all field def of a site
     *
     * @param siteID
     *
     * @auhtor NK
     */
    public abstract JahiaDOMObject getFieldDefsAsDOM (int siteID)
            throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all field def props of a site
     *
     * @param siteID
     *
     * @auhtor NK
     */
    public abstract JahiaDOMObject getFieldDefPropsAsDOM (int siteID)
            throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Returns a List of all Acl ID used by fields for a site
     * Need this for site extraction
     *
     * @auhtor NK
     */
    public abstract List<Integer> getAclIDs (int siteID)
            throws JahiaException;

    /**
     * Serializes in XML format all the fields on a page that are not within
     * containers.
     *
     * @param xmlWriter               the XML writer in which we ouput the XML
     * @param xmlSerializationOptions the options that allow us to modify the
     *                                behavior of the XML output.
     * @param pageID                  the page ID for which to serialize the non container
     *                                fields.
     * @param processingContext               specifies context of serialization, such as current
     *                                user, current request parameters, entry load request, URL generation
     *                                information such as ServerName, ServerPort, ContextPath, etc... URL
     *                                generation is an important part of XML serialization and this is why
     *                                we pass this parameter down, as well as user rights checking.
     *
     * @throws IOException occurs if there was an error writing to the
     *                     XML writer.
     */
    public abstract void serializeNonContainerFieldsToXML (XmlWriter xmlWriter,
                                                           XMLSerializationOptions xmlSerializationOptions, int pageID,
                                                           ProcessingContext processingContext)
            throws IOException;

    /**
     * Compose the file absolute path to a flat file containing the default value for a field definition.
     *
     * @param siteID
     * @param pageDefID
     * @param name
     *
     * @return
     */
    public abstract String composeFieldDefDefaultValueFilePath (int siteID, int pageDefID, 
        String name);

    /**
     * Purge all the fields on a given page, including all their versions, their
     * subcontent, everything !
     *
     * @param pageID the page id for which to remove all the fields directly
     *               attached to the page.
     *
     * @throws JahiaException in the case where there was an error while
     *                        removing all the content.
     */
    public abstract void purgePageFields (int pageID)
        throws JahiaException;

    /**
     * Creates a JahiaField from a Content field.
     *
     * @param contentField the content field source
     *
     * @return a JahiaField corresponding to the language, workflow state and
     *         eventually version ID specified in the EntryLoadRequest object inside
     *         the ProcessingContext object.
     *
     * @throws JahiaException in case we have trouble loading or accessing the
     *                        entry state of the content field.
     */
    public abstract JahiaField contentFieldToJahiaField (
            ContentField contentField,
            EntryLoadRequest entryLoadRequest)
            throws JahiaException;

    /**
     * Invalidates a JahiaField cache entry.
     * @param fieldID the identifier for the field entry to invalidate in the
     * cache.
     */
    public abstract void invalidateCacheField (int fieldID);


    public abstract ContentObjectKey getFieldParentObjectKey(int id, EntryLoadRequest req);

    public abstract int[] getSubPageIdAndType(int id, EntryLoadRequest req);

    /**
     * Gets all field ids on a set of pages, which are having one of the given ACL-ids
     * 
     * @param pageIDs
     *                the page ids
     * @param aclIDs
     *                the ACL ids
     * 
     * @return a List of field IDs
     */
    public abstract List<Integer> getFieldIDsOnPagesHavingAcls(Set<Integer> pageIDs,
            Set<Integer> aclIDs);

    public abstract Map<String, String> getVersions(int site, String lang);

    public abstract List<ContentField> findFieldsByPropertyNameAndValue(String name, String value) throws JahiaException;

    public abstract List<Object[]> getFieldPropertiesByName(String name);               
} // end JahiaFieldServices
