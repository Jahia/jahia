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

//
//                          \/_      ____\/_
//                       __/\\______|    |\_/\.     _______\/_
//            __\/_.____|    |       \   |    +----+       \\
//    _______|  /--|    |    |    -   \  _    |    :    -   \_________\/_
//   \\______: :---|    :    :           |    :    |         \________>\
//           |__\---\_____________:______:    :____|____:_____\
//                                    _\/_____|
//                                     /\
//                 . . . i n   j a h i a   w e   t r u s t . . .
//

package org.jahia.services.containers;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaDOMObject;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerSet;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.QueryObjectModelImpl;
import org.jahia.services.JahiaService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;

public abstract class JahiaContainersService extends JahiaService {

    /**
     * builds the complete container structure for a specific page
     * builds the complete container structure (containerlists->containers->fields/containerlists)
     * for a specific page
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param jData JahiaData
     *
     * @return a List of containerlist IDs
     */
    public abstract JahiaContainerSet buildContainerStructureForPage (ProcessingContext processingContext, JahiaPage page)
        throws JahiaException;

    /**
     * builds the complete container definition structure for a specific page template
     */
    public abstract Map<String, Integer> buildContainerDefinitionsForTemplate(String typeName, int siteId, int pageDefID, JahiaContainerSet theSet) throws JahiaException;
    
    /**
     * gets all container definitions ids on a page
     *
     * @param thePage the page object
     *
     * @return a List of container definition IDs
     */
    public abstract List<JahiaContainerDefinition> getctndefidsInPage (ContentPage contentPage,
                                               EntryLoadRequest
                                               entryLoadRequest)
        throws JahiaException;

    /**
     * gets all container list ids on a page, by their definition id
     *
     * @param pageID the page id
     * @param defID  the container definition id
     *
     * @return a List of containerlist IDs
     */
    public abstract List<Integer> getContainerListIDs (int pageID, int defID,
                                                EntryLoadRequest loadVersion)
        throws JahiaException;

    /**
     * gets all container list ids on a page
     *
     * @param pageID the page id
     * @param defID  the container definition id
     *
     * @return a List of containerlist IDs
     */
    public abstract List<Integer> getContainerListIDsInPage (ContentPage
        contentPage, EntryLoadRequest entryLoadRequest)
        throws JahiaException;

    /**
     * Retrieves all the entry states for the container lists in a pages.
     * This is a recursive operation that will trickle down to all the sub
     * content.
     *
     * @param pageID the page for which to retrieve the entry states.
     *
     * @return a SortedSet of ContentFieldEntryStates of all the subcontent
     *         that we have processed
     *
     * @throws JahiaException thrown if there was an error communicating with
     *                        the database.
     */
    public abstract SortedSet<ContentObjectEntryState> getContainerListInPageEntryStates (int pageID)
        throws JahiaException;

    /**
     * gets all container ids for a given entryLoadRequest
     *
     * @param loadVersion
     *
     * @return
     *
     * @throws JahiaException
     */
    public abstract List<Integer> getCtnIds (EntryLoadRequest loadVersion)
        throws JahiaException;

    /**
     * gets all container ids in a container list
     *
     * @param listID the container list id
     *
     * @return a List of container IDs
     */
    public abstract List<Integer> getctnidsInList (int listID,
                                            EntryLoadRequest loadVersion)
        throws JahiaException;

    public abstract List<Integer> getctnidsInList (int listID)
        throws JahiaException;

    /**
         * Gets all container ids in a container list with ordering on a given field.
     * If the fieldName is null, not ordering applied
     *
     * @param int     listID          the container list id
     * @param String  fieldName    the fieldname on which to filter
     * @param boolean asc         Asc. or desc. ordering ( true = asc )
     *
     * @return a List of container IDs
     *
     * @todo we might want to cache these values in memory in order to
     * improve performance.
     * @author NK
     */
    public abstract List<Integer> getctnidsInList (int listID, String fieldName,
                                            boolean asc,
                                            EntryLoadRequest loadVersion)
        throws JahiaException;

    /**
     * gets all container ids for a given site. This list uses stored
     * ranks to order its values
     *
     * @param int siteID, the given site id.
     *
     * @return List a List of container IDs
     */
    public abstract List<Integer> getCtnIds (int siteID)
        throws JahiaException;

    /**
     * gets all container ids of the system.
     *
     * @return List a List of container IDs
     */
    public abstract List<Integer> getCtnIds ()
        throws JahiaException;

    /**
     * Correct version to sort a containerList without any sort defined
     *
     * @param ids
     * @param loadRequest
     * @return
     * @throws JahiaException
     */
    public abstract List<Integer> getCtnIds(BitSet ids, EntryLoadRequest loadRequest)
        throws JahiaException;    
    
    /**
     * gets all container list ids in a container.
     *
     * @param listID the container list id
     *
     * @return a List of container IDs
     *
     * @todo we might want to cache these values in memory in order to
     * improve performance.
     */
    public abstract List<Integer> getCtnListIDsInContainer (int ctnID)
        throws JahiaException;

    /**
     * gets all field ids in a container
     *
     * @param ctnid the container id
     *
     * @return a List of field IDs
     */
    public abstract List<Integer> getFieldIDsInContainer (int ctnid)
        throws JahiaException;

    /**
     * gets all field ids in a container
     *
     * @param ctnid the container id
     *
     * @return a List of field IDs
     */
    public abstract List<Integer> getFieldIDsInContainer (int ctnid,
        EntryLoadRequest loadVersion)
        throws JahiaException;

    /**
     * gets all field ids and types in a container
     *
     * @param ctnid the container id
     *
     * @return a List of field IDs
     */
    public abstract List<Object[]> getFieldIDsAndTypesInContainer (int ctnid,
        EntryLoadRequest loadVersion)
        throws JahiaException;

    /**
     * gets a container list id by its container list name and page id
     *
     * @param containerName the container name
     * @param pageID        the page ID
     *
     * @return a List of field IDs
     */
    public abstract int getContainerListID (String containerName, int pageID)
        throws JahiaException;

    /**
     * gets all container definition ids in Jahia
     *
     * @return a List of container definition IDs
     */
    public abstract List<Integer> getAllContainerDefinitionIDs ()
        throws JahiaException;

    /**
     * loads a container info, by its container id
     *
     * @param ctnid the container id
     *
     * @return a JahiaContainer object, without field values
     *
     * @see org.jahia.data.containers.JahiaContainer
     */
    public abstract JahiaContainer loadContainerInfo (int ctnid)
        throws JahiaException;

    public abstract JahiaContainer loadContainerInfo (int ctnid,
        EntryLoadRequest loadVersion)
        throws JahiaException;

    /**
     * loads a container by its container id
     * loads a container info and fields by its container id, but not dependant containerlists
     * this method cannot load request-specific values (i.e. applications);
     * see the loadContainer( ctnid, loadFlag, jParams) for that.
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param ctnid    the container id
     * @param loadFlag the loadFlag
     *
     * @see org.jahia.data.containers.JahiaContainer
     * @see org.jahia.data.fields.LoadFlags
     */

//    public abstract JahiaContainer loadContainer (int ctnid, int loadFlag)
//        throws JahiaException;

    /**
     * loads a container by its container id
     * loads a container info and fields by its container id, but not dependant containerlists
     * this method can load request-specific values (i.e. applications);
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param ctnid    the container id
     * @param loadFlag the loadFlag
     * @param jParams  the ProcessingContext object, containing request and response
     *
     * @see org.jahia.data.containers.JahiaContainer
     * @see org.jahia.data.fields.LoadFlags
     */
    public abstract JahiaContainer loadContainer (int ctnid, int loadFlag,
                                                  ProcessingContext jParams)
        throws JahiaException;

    public abstract JahiaContainer loadContainer (int ctnid, int loadFlag,
                                                  ProcessingContext jParams,
                                                  EntryLoadRequest loadVersion)
        throws JahiaException;


    /**
     *
     * @param ctnid
     * @param loadFlag
     * @param jParams
     * @param loadVersion
     * @param cachedFieldsInContainer
     * @return
     * @throws JahiaException
     */
    // the Map cachedFieldsInContainer is here if we already have a Map
    // with containerID as key, and a List of fieldIDs in object, see buildContainerStructure.
    public abstract JahiaContainer loadContainer (int ctnid, int loadFlag,
                                         ProcessingContext jParams,
                                         EntryLoadRequest loadVersion,
                                         Map<Integer, List<Integer>> cachedFieldsInContainer,
                                         Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                         Map<Integer, List<Integer>> cachedContainerListsFromContainers)
        throws JahiaException;


    /**
     * Load a list of containers
     *
     * @param ctnids
     * @param loadFlag
     * @param jParams
     * @param loadVersion
     * @return
     * @throws JahiaException
     */
    public abstract List<JahiaContainer> loadContainers (List<Integer> ctnids, int loadFlag,
                                         ProcessingContext jParams,
                                         EntryLoadRequest loadVersion)
        throws JahiaException;

    /**
     * saves the container info
     * saves the container info
     * if id=0, assigns a new id to container and creates it in datasource
         * if listid=0, assigns a new listid to container and creates it in datasource
     *
     * @param theContainer a JahiaContainer object
     * @param parentID     parent container id
     * @param parentAclID  the Acl parent ID
     *
     * @param		jParams				the current ProcessingContext
     * @see org.jahia.data.containers.JahiaContainer
     */
    public abstract void saveContainerInfo (JahiaContainer theContainer,
                                            int parentID,
                                            int parentAclID,
                                            ProcessingContext jParams)
        throws JahiaException;

    /**
     * saves the container info and fields
         * saves the container info and fields, but not the dependant container lists
     * if id=0, assigns a new id to container and creates it in datasource
         * if listid=0, assigns a new listid to container and creates it in datasource
     *
     * @param theContainer a JahiaContainer object
     *
     * @see org.jahia.data.containers.JahiaContainer
     */
    public abstract void saveContainer (JahiaContainer theContainer,
                                        int containerParentID,
                                        ProcessingContext jParams)
        throws JahiaException;

    /**
     * deletes the container info, fields and sublists
     * deletes the container info, fields and sublists
     *
     * @param ctnid   the container id
     * @param jParams the request parameters
     *
     * @see org.jahia.data.containers.JahiaContainer
     * @deprecated use markContainerLanguageForDeletion instead
     */
    public abstract void deleteContainer (int ctnid, ProcessingContext jParams)
        throws JahiaException;

    /**
     * Checks if a container is valid for activation, by checking the container
     * info data as well as going down into the field. This may fail
     * completely in the case where some of the fields don't match a mandatory
     * criteria such as mandatory language values. A container will also not
         * be valid for activation if any of it's fields is not valid for activation,
     * guaranteeing integrity of the container.
     *
     * @param id           the identifier of the container to activate.
     * @param user         the user who is requesting the activation
     * @param saveVersion  contains the version ID to use for the activation as
     *                     well as versioning capability of this Jahia site
         * @param jParams      a ProcessingContext object mostly used to pass to the fields so
     *                     they can use it for getting the current site information, current user,
     *                     etc...
     * @param withSubPages specifies whether subPages should be tested for
     *                     activation validity also. If no, a page field contained in this
     *                     container might fail.
     *
     * @return an ActivationTestResults objet containing a status, warnings
         *         and errors related to the activation validity of all the sub objects.
     *
     * @throws JahiaException thrown if there were errors accessing the
     *                        content while processing the validity tests.
     */
    public abstract ActivationTestResults isContainerValidForActivation (
        Set<String> languageCodes,
        int id, JahiaUser user, JahiaSaveVersion saveVersion,
        ProcessingContext jParams, StateModificationContext stateModifContext)
        throws JahiaException;

    /**
     * Tries to activate a container. Note that this method supposes you have
     * validated all the fields it contains beforehand. All it does is check
     * that the fields are ready for activation, by checking the container
     * info data as well as going down into the field. This may fail
     * completely in the case where some of the fields don't match a mandatory
     * criteria such as mandatory language values. A container will also not
         * be valid for activation if any of it's fields is not valid for activation,
     * guaranteeing integrity of the container.
     *
     * @param id                the identifier of the container to activate.
     * @param user              the user who is requesting the activation
         * @param saveVersion       contains the version ID to use for the activation as
         *                          well as versioning capability of this Jahia site
     * @param jParams           a ProcessingContext object mostly used to pass to the fields so
     *                          they can use it for getting the current site information, current user,
     *                          etc...
     * @param stateModifContext contains the current context of the activation,
     *                          including the current tree path stack and options that indicate how
     *                          the activation should be processed such as whether to recursively descend
     *                          in sub pages.
     *
     * @return an ActivationTestResults objet containing a status, warnings
         *         and errors related to the activation validity of all the sub objects.
     *
     * @throws JahiaException thrown if there were errors accessing the
     *                        content while processing the validity tests.
     */
    public abstract ActivationTestResults activateStagedContainer (
        Set<String> languageCodes,
        int id, JahiaUser user, JahiaSaveVersion saveVersion,
        ProcessingContext jParams, StateModificationContext stateModifContext)
        throws JahiaException;

    /**
     * Validate the containers of the page to which the user has admin AND write access
     * i.e. now Staged containers are Active.
     *
     * @param saveVersion       it must contain the right versionID and staging/versioning
     *                          info of the current site
     * @param stateModifContext contains the current context of the activation,
     *                          including the current tree path stack and options that indicate how
     *                          the activation should be processed such as whether to recursively descend
     *                          in sub pages.
     *
     * @return true if all the containers were successfully validated
     */
    public abstract ActivationTestResults activateStagedContainers (
        Set<String> languageCodes,
        int pageID, JahiaUser user, JahiaSaveVersion saveVersion,
        ProcessingContext jParams, StateModificationContext stateModifContext)
        throws JahiaException;

    /**
     * Validate the container lists of the page to which the user has admin AND write access
     * i.e. now Staged container lists are Active.
     *
     * @param saveVersion       it must contain the right versionID and staging/versioning
     *                          info of the current site
     * @param stateModifContext contains the current context of the activation,
     *                          including the current tree path stack and options that indicate how
     *                          the activation should be processed such as whether to recursively descend
     *                          in sub pages.
     *
     * @return true if the container list and it's content where sucessfully validated
     */
    public abstract ActivationTestResults activateStagedContainerLists (
        Set<String> languageCodes,
        int pageID, JahiaUser user,
        JahiaSaveVersion saveVersion,
        StateModificationContext stateModifContext)
        throws JahiaException;

    public abstract ActivationTestResults activateStagedContainerList(
            Set<String> languageCodes,
            int containerListID, JahiaUser user,
            JahiaSaveVersion saveVersion,
            ProcessingContext jParams, StateModificationContext stateModifContext)
            throws JahiaException;


    /**
     * @param languageCodes
     * @param pageID
     * @param user
     * @param saveVersion
     * @param jParams
     * @param stateModifContext contains the current context of the activation,
     *                          including the current tree path stack and options that indicate how
     *                          the activation should be processed such as whether to recursively descend
     *                          in sub pages.
     *
     * @return
     *
     * @throws JahiaException
     */
    public abstract ActivationTestResults areContainersValidForActivation (
        Set<String> languageCodes,
        int pageID,
        JahiaUser user,
        JahiaSaveVersion saveVersion,
        ProcessingContext jParams,
        StateModificationContext stateModifContext)
        throws JahiaException;

    public abstract ActivationTestResults isContainerListValidForActivation (
            Set<String> languageCodes,
            int containerListID,
            JahiaUser user,
            JahiaSaveVersion saveVersion,
            StateModificationContext stateModifContext)
            throws JahiaException;


    /**
     * @param languageCodes
     * @param pageID
     * @param user
     * @param saveVersion
     * @param stateModifContext contains the current context of the activation,
     *                          including the current tree path stack and options that indicate how
     *                          the activation should be processed such as whether to recursively descend
     *                          in sub pages.
     *
     * @return
     *
     * @throws JahiaException
     */
    public abstract ActivationTestResults areContainerListsValidForActivation (
        Set<String> languageCodes,
        int pageID,
        JahiaUser user,
        JahiaSaveVersion saveVersion,
        StateModificationContext stateModifContext)
        throws JahiaException;

    /**
     * loads a container list info
     * loads container list info, but not dependant fields and container lists
     *
     * @param containerListID the container list id
     *
     * @see org.jahia.data.containers.JahiaContainerList
     */
    public abstract JahiaContainerList loadContainerListInfo (int
        containerListID)
        throws JahiaException;

    public abstract JahiaContainerList loadContainerListInfo (int
        containerListID, EntryLoadRequest loadVersion)
        throws JahiaException;

    /**
     * loads a container list
         * loads container list info and containers, but not dependant container lists
     * this method cannot load request-specific values (i.e. applications);
     * see the loadContainerList( containerListID, loadFlag, jParams) for that.
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param containerListID the container list id
     * @param loadFlag        the loadFlag
     *
     * @see org.jahia.data.containers.JahiaContainerList
     * @see org.jahia.data.fields.LoadFlags
     */
    public abstract JahiaContainerList loadContainerList (int containerListID,
        int loadFlag)
        throws JahiaException;

    /**
     * loads a container list
         * loads container list info and containers, but not dependant container lists
     * this method can load request-specific values (i.e. applications)
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param containerListID the container list id
     * @param loadFlag        the loadFlag
         * @param jParams         the ProcessingContext object, containing request and response
     *
     * @see org.jahia.data.containers.JahiaContainerList
     * @see org.jahia.data.fields.LoadFlags
     */
    public abstract JahiaContainerList loadContainerList (int containerListID,
        int loadFlag, ProcessingContext jParams)
        throws JahiaException;

    /**
     * Loads a set of containers for a given container list
     * Loads container list info and all containers, but not dependant container lists
     * this method can load request-specific values (i.e. applications).
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param containerListID the container list id
     * @param loadFlag        the loadFlag
     * @param jParams         the ProcessingContext object, containing request and response
     *
     * @author		 NK
     * @see org.jahia.data.containers.JahiaContainerList
     * @see org.jahia.data.fields.LoadFlags
     */
    public abstract JahiaContainerList loadContainerList (int containerListID,
        int loadFlag,
        ProcessingContext jParams,
        EntryLoadRequest loadVersion,
        Map<Integer, List<Integer>> cachedFieldsInContainer,
        Map<Integer, List<Integer>> cachedContainersFromContainerLists,
        Map<Integer, List<Integer>> cachedContainerListsFromContainers)
        throws JahiaException;

    /**
     * Loads a set of containers for a given container list
         * Loads container list info and containers, but not dependant container lists
     * this method can load request-specific values (i.e. applications).
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param containerListID the container list id
     * @param List          ctnids, List of container ids to load.
     * @param loadFlag        the loadFlag
         * @param jParams         the ProcessingContext object, containing request and response
     *
     * @author		 NK
     * @see org.jahia.data.containers.JahiaContainerList
     * @see org.jahia.data.fields.LoadFlags
     */
    public abstract JahiaContainerList loadContainerList (int containerListID,
        List<Integer> ctnids,
        int loadFlag,
        ProcessingContext jParams,
        EntryLoadRequest loadVersion,
        Map<Integer, List<Integer>> cachedFieldsInContainer,
        Map<Integer, List<Integer>> cachedContainersFromContainerLists,
        Map<Integer, List<Integer>> cachedContainerListsFromContainers)
        throws JahiaException;

    /**
     * saves a container list info
     * saves container list info, but not dependant fields and container lists
         * if id=0, attributes a new id to container list and creates it in datasource
     *
     * @param theContainerList a JahiaContainerList object
     * @param parentAclID      the Acl parent ID
     * @deprecated, use the method with the ProcessingContext
     *
     * @see org.jahia.data.containers.JahiaContainerList
     */
    public abstract void saveContainerListInfo (JahiaContainerList
                                                theContainerList,
                                                int parentAclID)
        throws JahiaException;

    /**
     * saves a container list info
     * saves container list info, but not dependant fields and container lists
     * if id=0, attributes a new id to container list and creates it in datasource
     *
     * @param theContainerList a JahiaContainerList object
     * @param parentAclID      the Acl parent ID
     * @param user The creator
     *
     * @see org.jahia.data.containers.JahiaContainerList
     */
    public abstract void saveContainerListInfo (JahiaContainerList
            theContainerList, int parentAclID, ProcessingContext jParams)
            throws JahiaException;

    /***
     * deletes a container list info
     * deletes a container list info, but not dependant containers
     * see deleteContainerList for that
     *
     * @param        listID              the container list id to delete
     *
     */

//    public abstract void deleteContainerListInfo( int listID )
//        throws JahiaException;

    /**
     * deletes a container list info
     * deletes a container list info, including dependant containers
     * see deleteContainerList for that
     *
     * @param listID the container list id to delete
     */
    public abstract void deleteContainerList (int listID, ProcessingContext jParams)
        throws JahiaException;

    /**
     * loads a container definition by its id
     *
     * @param definitionID the container definition id
     *
     * @return a JahiaContainerDefinition object
     *
     * @see org.jahia.data.containers.JahiaContainerDefinition
     * @see org.jahia.data.containers.JahiaContainerStructure
     */
    public abstract JahiaContainerDefinition loadContainerDefinition (int
        definitionID)
        throws JahiaException;

    /**
     * Load a container definition by it's site ID and it's definition name
     *
         * @param siteID         the site identifier on which to retrieve the definition
     * @param definitionName the unique name for the definition
     *
     * @return a JahiaContainerDefinition if found
     *
     * @throws JahiaException in case there was a problem communicating with
     *                        the database
     */
    public abstract JahiaContainerDefinition loadContainerDefinition (int
        siteID,
        String definitionName)
        throws JahiaException;

    /**
     * saves a container definition
         * if id=0, assigns a new id to the definition and creates it in the datasource
     *
     * @param theDefinition the JahiaContainerDefinition to save
     *
     * @see org.jahia.data.containers.JahiaContainerDefinition
     * @see org.jahia.data.containers.JahiaContainerStructure
     */
    public abstract void saveContainerDefinition (JahiaContainerDefinition
                                                  theDefinition)
        throws JahiaException;

    /**
     * deletes a container definition
     *
     * @param theDefinition the JahiaContainerDefinition to delete
     *
     * @see org.jahia.data.containers.JahiaContainerDefinition
     * @see org.jahia.data.containers.JahiaContainerStructure
     */
    public abstract void deleteContainerDefinition (int definitionID)
        throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all containers of a site
     *
     * @param int siteID
     */
    public abstract JahiaDOMObject getContainersAsDOM (int siteID)
        throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container lists of a site
     *
     * @param int siteID
     */
    public abstract JahiaDOMObject getContainerListsAsDOM (int siteID)
        throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container lists props of a site
     *
     * @param int siteID
     *
     */
    public abstract JahiaDOMObject getContainerListPropsAsDOM (int siteID)
        throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container def of a site
     *
     * @param int siteID
     *
     */
    public abstract JahiaDOMObject getContainerDefsAsDOM (int siteID)
        throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container def prop of a site
     *
     * @param int siteID
     *
     */
    public abstract JahiaDOMObject getContainerDefPropsAsDOM (int siteID)
        throws JahiaException;

    //--------------------------------------------------------------------------
    /**
         * returns a DOM representation of all containers extented properties of a site
     *
     * @param int siteID
     */
    public abstract JahiaDOMObject getContainerExtendedPropsAsDOM (int siteID)
        throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all container structure of a site
     *
     * @param int siteID
     *
     */
    public abstract JahiaDOMObject getContainerStructsAsDOM (int siteID)
        throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Returns a List of all Acl ID used by container for a site
     * Need this for site extraction
     *
     * @param int siteID
     *
     */
    public abstract List<Integer> getAclIDs (int siteID)
        throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Returns a List of all ctn lists fields ( field def ) Acl ID for a site
     * Used for site extraction
     *
     * @param int siteID
     *
     */
    public abstract List<Integer> getCtnListFieldAclIDs (int siteID)
        throws JahiaException;

    /**
     * Marks all the container lists in a given page for deletion. Prior to
     * calling this method you should have already marked all the fields in
     * the page for deletion, in the languages that are to be deleted.
     *
     * @param pageID            the page for which to flag all the content
     * @param user              the user performing this marking operation.
     * @param languageCode      the language to mark for deletion
     * @param stateModifContext use to detect loop in deletions
     *
     * @return true if all the content could be removed, or false if some
     *         containers lists were not completely removed.
     *
     * @throws JahiaException in case there were problems communicating with
     *                        the persistant store
     */
    public abstract boolean markPageContainerListsLanguageForDeletion (int
        pageID,
        JahiaUser user,
        String languageCode,
        StateModificationContext stateModifContext)
        throws JahiaException;

    /**
     * Removes all the data in the containers lists for the given page ID,
     * including all the versions, etc...
     *
     * @param pageID the page for which to purge the container lists.
     *
     * @throws JahiaException thrown if an error occurs while purging the
     *                        container lists.
     */
    public abstract void purgePageContainerLists (int pageID)
        throws JahiaException;

    /**
     * Marks a specific container list for deletion. Prior to calling this all
     * the marking on the fields should have already been done for the various
     * languages as this is not done by this method.
     *
     * @param listID            the list to mark for deletion if possible (only marked if
     *                          all the containers can be marked for deletion)
     * @param user              the user performing this marking operation.
     * @param languageCode      the language to mark for deletion
     * @param stateModifContext use to detect loop in deletions
     *
         * @return true if this container list was successfully marked for deletion,
     *         implying that all the containers inside it were also successfully marked
     *         for deletion.
     *
     * @throws JahiaException in case there were problems communicating with
     *                        the persistant store
     */
    public abstract boolean markContainerListLanguageForDeletion (int listID,
        JahiaUser user,
        String languageCode,
        StateModificationContext
        stateModifContext)
        throws JahiaException;

    /**
     * Marks a specific container for deletion. Prior to calling this all
     * the marking on the fields should have already been done for the various
     * languages as this is not done by this method.
     *
     * @param id                the container to mark for deletion if possible (only marked if
     *                          all the fields can be marked for deletion in all the languages)
     * @param user              the user performing this marking operation.
     * @param languageCode      the language to mark for deletion.
     * @param stateModifContext use to detect loop in deletions
     * @param withFieldContent  specifies whether the field's specific content
     *                          should be marked for deletion or not. Useful for pages in certain
     *                          cases.
     *
     * @return true if this container was successfully marked for deletion,
         *         implying that all the fields inside it were also successfully marked
     *         for deletion in all languages.
     *
     * @throws JahiaException in case there were problems communicating with
     *                        the persistant store
     */
    public abstract boolean markContainerLanguageForDeletion (
        int id, JahiaUser user, String languageCode,
        StateModificationContext stateModifContext)
        throws JahiaException;

    /**
     * Retrieves all the top level container lists IDs for a given page and
     * load request.
     *
     * @param pageID
     * @param loadRequest
     *
         * @return a SortedSet of container list IDs for the top level container lists
     *         for this EntryLoadRequest.
     *
     * @throws JahiaException
     */
    public abstract SortedSet<Integer> getAllPageTopLevelContainerListIDs (int pageID,
        EntryLoadRequest loadRequest)
        throws JahiaException;

    /**
     * Returns true if container-definition is a sub-definition
     *
     * @param containerDefinitionID an integer specifying the container
     *                              definition for which to find the parent sub definitions.
     *
     * @return true if container definition is a sub-definition
     *
     * @throws JahiaException if there was an error while communicating with the
     *                        database
     */
    public abstract boolean hasContainerDefinitionParents (int
        containerDefinitionID)
        throws JahiaException;

    /**
     * Retrieves all the container list IDs in a site that correspond to the
     * passed name. Note that the structure of these lists may be different
     * because they may have originated in different templates !
     *
         * @param siteID      the site for which to retrieve the container lists by name
     * @param name        the name (definition name) for which to retrieve the
     *                    container lists in the site.
     * @param loadRequest the EntryLoadRequest for which to retrieve the
     *                    container list IDs. Maybe null if we want to get absolutely EVERY
     *                    container list regardless of status/version, etc...
     *
         * @return a SortedSet containing Integer that are subContainerDefinitionIDs
     *         that contain the container definition we are looking for.
     *
         * @throws JahiaException if there was an error while communicating with the
     *                        database
     */
    public abstract SortedSet<Integer> getSiteTopLevelContainerListsIDsByName (int
        siteID,
        String name,
        EntryLoadRequest loadRequest)
        throws JahiaException;

    /**
     * Retrieves all the properties for a given container
     *
     * @param containerID the identifier of the container whose properties
     *                    we want to retrieve from the database
     *
     * @return a Properties object that contains all the properties that are
     *         available for this container in the database
     *
     * @throws JahiaException generated if there were problems executing the
     *                        query or communicating with the database.
     */
    public abstract Map<Object, Object> getContainerProperties (int containerID)
        throws JahiaException;

    public abstract Map<Object, Object> getContainerListProperties (int containerListID)
            throws JahiaException;

    /**
     * Saves a whole set of properties in the database for the specified
     * container.
     *
     * @param containerID         the container whose properties we are serializing
     *                            in the database.
     * @param jahiaID             the site id
     * @param containerProperties the Properties object that contains all
     *                            the properties to save in the database. Only what is passed here will
     *                            exist in the database. If the database contained properties that aren't
     *                            in this Map they will be deleted.
     *
     * @throws JahiaException generated if there were problems executing the
     *                        query or communicating with the database.
     */
    public abstract void setContainerProperties (int containerID,
                                                 int jahiaID,
                                                 Map<Object, Object> containerProperties)
        throws JahiaException;


    public abstract void setContainerListProperties (int containerListID,
                                        int jahiaID,
                                        Map<Object, Object> containerProperties)
            throws JahiaException;

    /**
     * Removes all container infos from the cache if it was present in the cache. If not,
     * this does nothing.
     *
     * @param containerID the identifier for the container to try to remove from the
     *                cache.
     */
    public abstract void invalidateContainerFromCache (int containerID);

    /**
     * Removes all container list from the cache if it was present in the cache. If not,
     * this does nothing.
     *
     * @param containerListID the identifier for the container list to try to remove from the
     *                cache.
     */
    public abstract void invalidateContainerListFromCache (int containerListID);

    /**
     * Loads all the container definitions that include a specific template ID in their
     * sub definitions.
     * @param templateID the template ID for which to retrieve all the container definitions.
     * @return a list containing JahiaContainerDefinition objects.                                                                                
     */
    public abstract List<JahiaContainerDefinition> loadContainerDefinitionInTemplate(int templateID);

    public abstract int getContainerListID(String containerListName, int pageID, int containerParentID) throws JahiaException;

    public abstract void saveContainerRankInfo(JahiaContainer c, ProcessingContext jParams) throws JahiaException;

    public abstract List<Integer> getSubContainerListIDs(int containerID, EntryLoadRequest request);

    public abstract int getContainerParentListId(int id, EntryLoadRequest req);

    public abstract ContentObjectKey getListParentObjectKey(int id, EntryLoadRequest req);

    public abstract List<ContentContainer> findContainersByPropertyNameAndValue(String name, String value) throws JahiaException;

    public abstract List<ContentContainerList> findContainerListsByPropertyNameAndValue(String name, String value) throws JahiaException;

    public abstract List<Object[]> getContainerPropertiesByName(String name);

    public abstract List<Object[]> getContainerListPropertiesByName(String name);


    /**
     * Gets all container list ids on a set of pages, which are having one of the given ACL-ids
     * 
     * @param pageIDs
     *                the page ids
     * @param aclIDs
     *                the ACL ids
     * 
     * @return a List of containerlist IDs
     */
    public abstract List<Integer> getContainerListIDsOnPagesHavingAcls(Set<Integer> pageIDs,
            Set<Integer> aclIDs);
    
    public abstract List<Integer> getContainerIDsHavingAcls(Set<Integer> aclIDs);

    /**
     * Gets all container ids on a set of pages, which are having one of the given ACL-ids
     * 
     * @param pageIDs
     *                the page ids
     * @param aclIDs
     *                the ACL ids
     * 
     * @return a List of container IDs
     */
    public abstract List<Integer> getContainerIDsOnPagesHavingAcls(Set<Integer> pageIDs,
            Set<Integer> aclIDs);
        
    public abstract List<Integer> getContainerListIDsHavingAcls(Set<Integer> aclIDs);

    /**
     * Returns a list of JahiaContainerDefinition's names using the given type
     * @param type
     * @return
     */
    public abstract List<String> getContainerDefinitionNamesWithType(String type);

    /**
     * Returns a list of JahiaContainerDefinition's names using the given types.
     * @param types
     * @return
     */
    public abstract List<String> getContainerDefinitionNamesWithType(Set<String> types);    

    /**
     * Create a ContainerQueryBean from a given QueryObjectModel
     *
     * @param queryModel
     * @param queryContextCtnID
     * @param parameters
     * @param jParams
     * @throws JahiaException
     * @return
     */
    public abstract ContainerQueryBean createContainerQueryBean(QueryObjectModelImpl queryModel, int queryContextCtnID,
                                                                Properties parameters, ProcessingContext jParams)
    throws JahiaException;

    /**
     * Returns a map of acl ids for the given list of ctnIds
     *
     * @param ctnIds
     * @return
     */
    public abstract Map<Integer, Integer> getContainerACLIDs(List<Integer> ctnIds);

    /**
     * Returns the acl ID for a given Container ID
     * @param ctnID
     * @return
     */
    public abstract int getContainerACLID(int ctnID);

    public abstract Map<String, String> getVersions(int site);

} // end JahiaContainersService
