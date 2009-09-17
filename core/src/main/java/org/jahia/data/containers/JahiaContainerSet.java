/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
//
//  JahiaContainerSet
//  EV      01.01.2001      Happy New Year, Folks !
//
package org.jahia.data.containers;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.fields.*;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerFactory;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.content.PageDefinitionKey;

import java.util.*;

import javax.jcr.nodetype.NoSuchNodeTypeException;

/**
 * @version $Rev$
 */
public class JahiaContainerSet implements Map<String, JahiaContainerList> {

    /**
     * this container name is used for backward compatibility with
     * the method declareField(String fieldName, String fieldTitle,
     * int fieldType, String defaultValue)
     * without giving any container list name
     * Declaring such field names has the limitation that theses field names
     * should be uniques for all container of the template
     */
    public static final String SHARED_CONTAINER = "@@@SHARED_CONTAINER@@@";

    private static Logger logger = Logger.getLogger(JahiaContainerSet.class);

    private static final String CLASS_NAME = JahiaContainerSet.class.getName();

    /**
     * associates JahiaContainerList
     */
    private Map<String, JahiaContainerList> containerLists;

    /**
     * associates String
     */
    private Set<String> declaredContainers; // list of per-request declared containers

    /**
     * associates String (key = container name/ value = List of field name)
     */
    private Map<String, List<String>> declaredFields;


    /**
     * This is used to store during a request all the container lists that have
     * been accessed in absolute manner. This will be used to figure out if the
     * absolute references have changed since the last time this template was
     * processed.
     */
    private Set<Integer> absoluteContainerListAccesses = new HashSet<Integer>();


    /**
     * Optional cached containers fields
     */
    private Map<Integer, List<Integer>> cachedFieldsFromContainers;

    /**
     * Optional cached containers in container list
     */
    private Map<Integer, List<Integer>> cachedContainersFromContainerLists;

    /**
     * Optional cached containerlists in containers
     */
    private Map<Integer, List<Integer>> cachedContainerListsFromContainers;

    private JahiaContainersService jahiaContainersService;
    private ProcessingContext processingContext;
    private JahiaPage page;


    private Set<Integer> requestedContainers;
    private Set<Integer> requestedLists;

    //-------------------------------------------------------------------------
    /***
     * constructor
     *
     *
     */
    public JahiaContainerSet(ProcessingContext processingContext, JahiaPage page) {
        this.processingContext = processingContext;
        this.page = page;
        this.containerLists = new HashMap<String, JahiaContainerList>();
        this.declaredContainers = new HashSet<String>();
        this.declaredFields = new HashMap<String, List<String>>();
        this.cachedFieldsFromContainers = new HashMap<Integer, List<Integer>>();
        this.cachedContainersFromContainerLists = new HashMap<Integer, List<Integer>>();
        this.jahiaContainersService = ServicesRegistry.getInstance().getJahiaContainersService();

        String ids = processingContext.getParameter("getctnid");
        if (ids != null) {
            requestedContainers = new HashSet<Integer>();
            requestedLists = new HashSet<Integer>();
            String[] s = ids.split(",");
            for (int i = 0; i < s.length; i++) {
                String id = s[i];
                ContentContainer cc = (ContentContainer) ContentContainer.getChildInstance(id);
                if (cc != null) {
                    requestedContainers.add(cc.getID());
                    requestedLists.add(cc.getParentContainerListID());
                }
            }
        }
        String listids = processingContext.getParameter("getctnlistid");
        if (listids != null) {
            if (requestedLists == null) {
                requestedLists = new HashSet<Integer>();
            }
            String[] s = listids.split(",");
            for (int i = 0; i < s.length; i++) {
                String id = s[i];
                requestedLists.add(Integer.parseInt(id));
            }
        }
    } // end constructor

    
    public static String resolveNodeType(String containerName, int pageTemplateID, int pageID, 
            ProcessingContext processingContext) throws NoSuchNodeTypeException {
        String n = null;
        try {
            if (pageTemplateID > 0) {
                n = JahiaPageDefinition.getContentDefinitionInstance(new PageDefinitionKey(pageTemplateID))
                        .getNodeType().getName();
            } else if (pageID > 0) {
                n = JahiaPageDefinition.getContentDefinitionInstance(
                        new PageDefinitionKey(ContentPage.getPage(pageID).getPageTemplateID(
                                processingContext.getEntryLoadRequest()))).getNodeType().getName();
            }  
            n = NodeTypeRegistry.getInstance().getNodeType(n).getNodeDefinition(containerName)
                    .getRequiredPrimaryTypes()[0].getUnstructuredChildNodeDefinitions().values().iterator().next().getRequiredPrimaryTypes()[0]
                    .getName();
        } catch (Exception e) {
            logger.warn("Error retrieving page definitions", e);
        }
        return n;
    }
    
    public static String resolveContainerName(String containerName, int pageTemplateID, int pageID, 
            ProcessingContext processingContext) {
        String n = null;
        try {
            if (pageTemplateID > 0) {
                n = JahiaPageDefinition.getContentDefinitionInstance(new PageDefinitionKey(pageTemplateID))
                        .getNodeType().getName();
            } else if (pageID > 0) {
                int defID = ContentPage.getPage(pageID).getPageTemplateID(processingContext.getEntryLoadRequest());
                if (defID == -1) {
                    return null;
                }
                n = JahiaPageDefinition.getContentDefinitionInstance(new PageDefinitionKey(defID)).getNodeType().getName();
            }
            n = n.replace(':', '_') + "_";
            if (!containerName.startsWith(n)) {
                containerName = n + containerName;
            }
        } catch (Exception e) {
            logger.warn("Error retrieving page definitions", e);
        }

        return containerName;
    }

    //-------------------------------------------------------------------------
    /***
     * adds a container list to the set.
     *
     * @param        aContainerList  the JahiaContainerList to add
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list  
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * @throws org.jahia.exceptions.JahiaException
     */
    private void addContainerList(JahiaContainerList aContainerList, String listViewId) throws JahiaException {
        containerLists.put((listViewId != null
                && listViewId.length() > 0 ? listViewId + "_"
                : "")
                + aContainerList.getDefinition().getName(), aContainerList);
    } // end addContainerList

    /**
     * Set that the container definition has been declared and create fake container list
     *
     * @param containerName          the container name
     * @param defId                  the definition ID
     */
    public void addDeclaredContainer(String containerName, int defId)
            throws JahiaException {
        // last check that the definition was really created
        declaredContainers.add(containerName);

        // last, we create a new fake container list if the
        // container list is empty
        int clistID = jahiaContainersService.getContainerListID(
                containerName, page.getID());
        if (clistID == -1) {

            // if (getQueryContainerList(containerName) == null) {
            // we create a fake container list, and save it into memory
            JahiaContainerList fakeContainerList = new JahiaContainerList(
                    0, 0, page.getID(), defId, 0);

            // we can't create a *real* container list in the database
            // here
            // since we might be in the case of a sub container list and
            // these can only be instantiated when the parent container
            // is created.
            addContainerList(fakeContainerList, null);
        }
    }     
    
    public void addDeclaredField(String containerName, String fieldName) {
        List<String> fieldNames = declaredFields.get(containerName);
        if (fieldNames == null) {
            fieldNames = new ArrayList<String>();
        }
        fieldNames.add(fieldName);
        declaredFields.put(containerName, fieldNames);
    }
    
    public void reloadContainerList(String containerName) throws JahiaException {
        declaredContainers.add(containerName);
        containerLists.remove(containerName);
        getContainerList(containerName);
        // hack, will be added below...
        declaredContainers.remove(containerName);
    }

    /**
     * enables a jahia template programmer to declare an edit view definition to
     * use for a given container list.
     *
     * @param containerName
	 *            the container name
	 * @param editView ,
	 *            the view definition
	 */
    public void declareContainerEditView(String containerName,
                                         ContainerEditView editView)
            throws JahiaException {

        logger.debug("for container [" + containerName + "]");
        /** todo ensure that the data provided by the user has no special chars in it */

        // check if a container has already been declared with the same name
        if (containerName.length() != 0) {
            // third, let's check to see if the declared container has already a container definition in the
            // ContainersDefinitionsRegistry
            JahiaContainerDefinition aDef = JahiaContainerDefinitionsRegistry
                    .getInstance()
                    .getDefinition(page.
                            getSiteID(), containerName);
            if (aDef != null) {
                aDef.setContainerEditView(editView);
                logger.debug("for container [" + containerName + "] done successfully ");
            }
        }
    }


    //-------------------------------------------------------------------------
    /***
     * checks if a container has already been declared
     * Note : we don't need to call this before a container declaration,
     * this is already done in the declareContainer implementation
     *
     * @param        containerName   the container name
     * @return true if already declared, false if not
     *
     */
    public boolean checkDeclared(String containerName) {
        if(Jahia.getThreadParamBean().getOperationMode().equals(ProcessingContext.NORMAL) && ! declaredContainers.contains(containerName))
        declaredContainers.add(containerName);
        return declaredContainers.contains(containerName);

    } // end checkDeclared

    //-------------------------------------------------------------------------
    /***
     * checks if a container field has already been declared
     * Note : we don't need to do this before a container declaration. This
     * is already done for us in the declareContainer and declareField
     * implementations.
     *
     * @param        containerName   the container name
     * @return true if already declared, false if not
     *
     */
    public boolean checkDeclaredField(String containerName, String fieldName) {

        List<String> fields = declaredFields.get(JahiaContainerSet.SHARED_CONTAINER);
        if (fields != null && fields.contains(fieldName)) {
            return true;
        }
        fields = declaredFields.get(containerName);
        return (fields != null && fields.contains(fieldName));

    } // end checjDeclaredField

    //-------------------------------------------------------------------------
    /***
     * checks if a field name already declared in any containers
     *
     * @param name
     * @return
     */
    public boolean checkDeclaredField(String name) {
        for (List<String> fields : declaredFields.values()) {
            if (fields.contains(name)) {
                return true;
            }
        }
        return false;
    }

    //-------------------------------------------------------------------------
    /***
     * gets the first occurence of a container list
     *
     * @param        containerName   the container name
     * @return a JahiaContainer object
     * @see          org.jahia.data.containers.JahiaContainer
     *
     */
    public JahiaContainer getContainer(String containerName)
            throws JahiaException {
        return getContainer(containerName, 0);
    } // end getContainer

    //-------------------------------------------------------------------------
    /***
     * gets an occurence of a container list
     *
     * @param        containerName   the container name
     * @param        index           the occurence
     * @return a JahiaContainer object
     * @see          org.jahia.data.containers.JahiaContainer
     *
     * @exception JahiaException if container not found
     *
     */
    public JahiaContainer getContainer(String containerName, int index)
            throws JahiaException {

        if (checkDeclared(containerName)) {
            JahiaContainerList theContainerList = (JahiaContainerList)
                    containerLists.get(containerName);
            if (theContainerList != null) {
                JahiaContainer theContainer = theContainerList.getContainer(
                        index);
                if (theContainer != null) {
                    return theContainer;
                } else {
                    String errorMsg = "Container is null : " + containerName +
                            "[" + index + "] -> BAILING OUT";
                    logger.error(errorMsg);
                    throw new JahiaException("Error while returning field " +
                            containerName, errorMsg,
                            JahiaException.TEMPLATE_ERROR,
                            JahiaException.CRITICAL_SEVERITY);
                }
            } else {
                String errorMsg = "Container not declared : " + containerName;
                logger.error(errorMsg + " -> BAILING OUT");
                throw new JahiaException(errorMsg, errorMsg,
                        JahiaException.TEMPLATE_ERROR,
                        JahiaException.CRITICAL_SEVERITY);
            }
        } else {
            String errorMsg = "Container not declared : " + containerName;
            logger.error(errorMsg + " -> BAILING OUT");
            throw new JahiaException(errorMsg, errorMsg,
                    JahiaException.TEMPLATE_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        }
    } // end getContainer

    //-------------------------------------------------------------------------
    /***
     * gets a container through its ID
     *
     * @param        ctnid     the container ID
     * @return a JahiaContainer object (may be null)
     * @see          org.jahia.data.containers.JahiaContainer
     *
     * @exception JahiaException if container not found
     *
     * !!! Warning : this method can return a null value !!!
     *
     */
    public JahiaContainer getContainer(int ctnid)
            throws JahiaException {

        for (JahiaContainerList aList : containerLists.values()) {
            for (JahiaContainer aContainer : aList.getContainersList()) {
                if (aContainer.getID() == ctnid) {
                    return aContainer;
                }
            }
        }
        return null;
    } // end getContainer

    //-------------------------------------------------------------------------
    /***
     * gets a containerlist through its name
     *
     * @param        containerName   the container list name
     * @return a JahiaContainerList object
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * @exception JahiaException if container list not found
     *
     */
    public JahiaContainerList getContainerList(String containerName)
            throws JahiaException {
     return getContainerList(containerName, null);   
    }
    
    //-------------------------------------------------------------------------
    /***
     * gets a containerlist through its name
     *
     * @param        containerName   the container list name
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list 
     * @return a JahiaContainerList object
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * @exception JahiaException if container list not found
     *
     */
    public JahiaContainerList getContainerList(String containerName, String listViewId)
            throws JahiaException {
        containerName = resolveContainerName(containerName, page.getPageTemplateID(), 0, processingContext);

        if (checkDeclared(containerName)) {
            JahiaContainerList theContainerList = (JahiaContainerList)
                    containerLists.get((listViewId != null
                            && listViewId.length() > 0? listViewId + "_"
                                    : "")
                                    + containerName);
            boolean bypass = false;
            if (theContainerList == null) {
                int clistID = jahiaContainersService.getContainerListID(containerName,
                                page.getID());
                if (clistID != -1) {
                    if (requestedLists == null || requestedLists.contains(clistID)) {
                        theContainerList = getContainerList(clistID, page.getID());
                    } else {
                        JahiaContainerDefinition containerDefinition =
                                JahiaContainerDefinitionsRegistry.getInstance().
                                        getDefinition(page.getSiteID(), containerName);
                        theContainerList = new JahiaContainerList(0, 0, page.getID(), containerDefinition.getID(), 0);
                        bypass = true;
                    }
                }
            }
            
            if (theContainerList != null && theContainerList.getID() != 0
                    && !theContainerList.getContentContainerList().hasActiveOrStagingEntries()) {
                // create staged entries
                jahiaContainersService.saveContainerListInfo(
                        theContainerList, theContainerList.getACL().getParentID(),
                        processingContext);
            }

            if ((theContainerList != null && theContainerList.getID() == 0) || theContainerList == null) {
                if (!bypass) {
                JahiaContainerDefinition containerDefinition =
                        JahiaContainerDefinitionsRegistry.getInstance().
                                getDefinition(page.getSiteID(), containerName);
                // just create the container list now
                //
                // we created the container list but we return a containerlist with id==0 !!!!
                // The consequence is that in the template, one can check for id==0 and create the
                // container list again !!!!!!
                    if (containerDefinition != null) {
                        theContainerList = ensureContainerList(containerDefinition,
                                                               page.getID(), 0, listViewId);
                    }
                }
            }

            if (theContainerList != null) {
                /* Load on first access only
                if ( !theContainerList.isContainersLoaded() && theContainerList.getID() != 0){ // not a fake
                    ContainerFactory.getInstance()
                            .fullyLoadContainerList(theContainerList,LoadFlags.ALL,
                            this.jData.getProcessingContext(),usedLoadRequest,
                            this.cachedFieldsFromContainers,
                            this.cachedContainersFromContainerLists,
                            this.cachedContainerListsFromContainers);
                }*/
                addContainerList(theContainerList, listViewId);
            }
            return theContainerList;
        } else {
            String errorMsg = "Container not declared : " + containerName;
            logger.error(errorMsg + " -> BAILING OUT");
            throw new JahiaException(errorMsg, errorMsg,
                    JahiaException.TEMPLATE_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        }
    } // end getQueryContainerList

    //-------------------------------------------------------------------------
    /***
     * gets a containerlist through its ID
     *
     * @param        listID     the container list ID
     * @return a JahiaContainerList object, may return null
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     */
    public JahiaContainerList getContainerList(int listID) throws JahiaException {
        return getContainerList(listID, null);
    }
    /***
     * gets a containerlist through its ID
     *
     * @param        listID     the container list ID
     * @param        listViewId     the ID in containerList-tag to support multiple views of same list in a page
     * @return a JahiaContainerList object, may return null
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     */
    public JahiaContainerList getContainerList(int listID, String listViewId) throws JahiaException {

        for (Map.Entry<String, JahiaContainerList> entry : containerLists.entrySet()) {
            JahiaContainerList aList = entry.getValue(); 
            if (aList.getID() == listID
                    && (listViewId == null
                            || listViewId.length() == 0 || entry.getKey()
                            .startsWith(listViewId + "_"))) {
                if (!aList.isContainersLoaded()) {
                    // When requesting an archived loadRequest
                    EntryLoadRequest loadRequest =
                            (EntryLoadRequest) processingContext.
                                    getEntryLoadRequest().clone();
                    if (processingContext.showRevisionDiff()) {
                        loadRequest.setWithDeleted(true);
                        loadRequest.setWithMarkedForDeletion(true);
                    } else {
                        loadRequest.setWithDeleted(false);
                        if (processingContext.getOpMode().equals(ProcessingContext.EDIT)) {
                            loadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                        } else {
                            loadRequest.setWithMarkedForDeletion(false);
                        }
                    }
                    ContainerFactory.getInstance()
                            .fullyLoadContainerList(aList, LoadFlags.ALL,
                                    processingContext, loadRequest,
                                    this.cachedFieldsFromContainers,
                                    this.cachedContainersFromContainerLists,
                                    this.cachedContainerListsFromContainers, listViewId);
                }
                return aList;
            }
        }
        return null;
    } // end getQueryContainerList

    //-------------------------------------------------------------------------
    /***
     * gets a container list in another page (absolute page reference by its id)
     *
     * @param        containerName   the container list name
     * @param        pageID          the page ID
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * !!! Warning : this method can return a null value !!!
     * !!! Warning 2 : this method should *NEVER* be called with the name
     * of a sub container list !
     */
    public JahiaContainerList getAbsoluteContainerList(String containerName,
            int pageID) throws JahiaException {
        return getAbsoluteContainerList(containerName, pageID, null);
    }
    
    //-------------------------------------------------------------------------
    /***
     * gets a container list in another page (absolute page reference by its id)
     *
     * @param        containerName   the container list name
     * @param        pageID          the page ID
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list  
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * !!! Warning : this method can return a null value !!!
     * !!! Warning 2 : this method should *NEVER* be called with the name
     * of a sub container list !
     */
    public JahiaContainerList getAbsoluteContainerList(String containerName,
                                                       int pageID, String listViewId)
            throws JahiaException {

        // quick check for a valid pageID
        if (pageID <= 0) {
            logger.error("Called with invalid pageID of " + pageID +
                    ", returning null container list.");
            throw new JahiaException(
                    "Error while loading absolute container list",
                    "Invalid pageID passed to an absolute container list call : pageID=" +
                            pageID + ", containerName=" + containerName,
                    JahiaException.DATA_ERROR,
                    JahiaException.ERROR_SEVERITY);

        }
        containerName = resolveContainerName(containerName, 0, pageID, processingContext);

        // now let's check if we are accessing a top level container list or
        // not using only the definitions (as the container lists might not
        // yet exist). If it's not a top level container list, throw an
        // exception !
        JahiaContainerDefinition containerDefinition =
                JahiaContainerDefinitionsRegistry.getInstance().
                        getDefinition(page.getSiteID(), containerName);

        if (containerDefinition != null) {
            /**
             * todo possible bug here, should we check the parent sub
             * container definitions for their page ID ? Because container
             * definitions are not local to a page, but sub container definitions
             * are...
             */
            if (org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode() && jahiaContainersService.
                    hasContainerDefinitionParents(containerDefinition.getID())) {
                throw new JahiaException(
                        "Error while loading absolute container list",
                        "Cannot load a sub container list with this method ! " +
                                pageID,
                        JahiaException.DATA_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }
        }
        JahiaContainerList theContainerList = null;
        int containerListID = jahiaContainersService.
                getContainerListID(containerName, pageID);
        if (containerListID != -1) {
            if (requestedLists == null || requestedLists.contains(containerListID)) {
                theContainerList = getContainerList(containerListID, pageID);

                /** what this code does for NK **/
                if (theContainerList == null) {
                    theContainerList = this.getContainerList(containerName);
                    theContainerList.setID(containerListID);
                } else {
                    int containerType = containerDefinition.getContainerListType();
                    if ((containerType & JahiaContainerDefinition.MANDATORY_TYPE) != 0) {
                        ensureMandatoryContainer(theContainerList, containerType);
                    }
                }
            } else {
                theContainerList = new JahiaContainerList(0, 0, processingContext.getPage().getID(), containerDefinition.getID(), 0);

            }
        } else if (containerDefinition != null) {
            ContentPage contentPage = ContentPage.getPage(pageID);
            int pageACLID = contentPage.getAclID();

            JahiaContainerList fakeContainerList = new JahiaContainerList(0, 0, 
                    pageID, containerDefinition.getID(), 0);

            jahiaContainersService.saveContainerListInfo(fakeContainerList, pageACLID, processingContext);
            
            int containerType = containerDefinition.getContainerListType();
            if ((containerType & JahiaContainerDefinition.MANDATORY_TYPE) != 0) {
                ensureMandatoryContainer(fakeContainerList, containerType);
            }
            
            theContainerList = fakeContainerList;
        }

        if (theContainerList != null) {
            if (pageID != this.page.getID()) {
                // let's update cross reference list
                absoluteContainerListAccesses.add(new Integer(theContainerList.getID()));
            } else {
                addContainerList(theContainerList, listViewId);                
            }
        }    
        return theContainerList;
    } // end getAbsoluteContainerList
    
    private JahiaContainerList getContainerList(int containerListID, int pageID){
        JahiaContainerList theContainerList = null;
        try {
            Map<Integer, List<Integer>> currentCachedFieldsFromContainers = this.cachedFieldsFromContainers;
            Map<Integer, List<Integer>> currentCachedContainersFromContainerLists = this.cachedContainersFromContainerLists;
            Map<Integer, List<Integer>> currentCachedContainerListsFromContainers = this.cachedContainerListsFromContainers;
            if ( pageID != this.page.getID()){
                cachedFieldsFromContainers= new HashMap<Integer, List<Integer>>();
                cachedContainersFromContainerLists= new HashMap<Integer, List<Integer>>();
                cachedContainerListsFromContainers= new HashMap<Integer, List<Integer>>();
            }

            EntryLoadRequest ctnLoadRequest = processingContext.
                    getEntryLoadRequest();
            if (ctnLoadRequest.isVersioned()) {
                // does the container exists at this archive date
                theContainerList = jahiaContainersService.loadContainerListInfo(containerListID,
                                ctnLoadRequest);
                if (theContainerList != null && theContainerList.getID()>0){
                    theContainerList = ServicesRegistry.getInstance().getJahiaContainersService().loadContainerList(
                        theContainerList.getID(), LoadFlags.ALL, processingContext, ctnLoadRequest,
                        currentCachedFieldsFromContainers, currentCachedContainersFromContainerLists,
                        currentCachedContainerListsFromContainers);
                }
            }
            if (ctnLoadRequest.isVersioned() && theContainerList == null) {
                // return the active version of the container list but without any containers !
                theContainerList = jahiaContainersService.loadContainerListInfo(containerListID);
                // to avoid reload of containers
                theContainerList.setIsContainersLoaded(true);
            } else {

                // When requesting an archived loadRequest
                EntryLoadRequest loadRequest =
                        (EntryLoadRequest) ctnLoadRequest.clone();
                if (processingContext.showRevisionDiff()) {
                    loadRequest.setWithDeleted(true);
                    loadRequest.setWithMarkedForDeletion(true);
                } else {
                    loadRequest.setWithDeleted(false);
                    if (processingContext.getOpMode().equals(ProcessingContext.EDIT)) {
                        loadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                    } else {
                        loadRequest.setWithMarkedForDeletion(false);
                    }
                }

                if (theContainerList == null) {
                    /*
                     * @fixme : what is the reason to not use lazy load ?
                     * Khue commented that.
                    theContainerList = ContainerFactory.getInstance()
                            .fullyLoadContainerList(containerListID,
                                    LoadFlags.ALL, this.jData.getProcessingContext(),
                                    loadRequest,
                                    currentCachedFieldsFromContainers,
                                    currentCachedContainersFromContainerLists,
                                    currentCachedContainerListsFromContainers);
                    */
                    theContainerList = ServicesRegistry.getInstance().getJahiaContainersService().loadContainerList(
                            containerListID, LoadFlags.ALL, processingContext, loadRequest,
                            currentCachedFieldsFromContainers, currentCachedContainersFromContainerLists,
                            currentCachedContainerListsFromContainers);
                }
                /*
                 * @fixme : what is the reason to not use lazy load ? Khue
                 * commented that. 
                 * else if (!theContainerList.isContainersLoaded()) {
                 * ContainerFactory.getInstance()
                 *     .fullyLoadContainerList(theContainerList, LoadFlags.ALL,
                 *         this.jData.getProcessingContext(), loadRequest,
                 *         currentCachedFieldsFromContainers,
                 *         currentCachedContainersFromContainerLists,
                 *         currentCachedContainerListsFromContainers); }
                 */                
            }
        } catch (final Throwable t) {
            logger.error("Error in getAbsoluteContainerList", t);
        }
        
        return theContainerList;
    }

    /**
     * Retrieves an absolutely referenced container list by its name and page URL key.
     *
     * @param        containerName   the container list name
     * @param        pageUrlKey      the page URL key
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     */
    public JahiaContainerList getAbsoluteContainerList(String containerName,
                                                       String pageUrlKey)
            throws JahiaException {
        return getAbsoluteContainerList(containerName, ServicesRegistry
                .getInstance().getJahiaPageService()
                .getPageIDByURLKeyAndSiteID(pageUrlKey,
                        page.getSiteID()), null);
    }       
    
    /**
     * Retrieves an absolutely referenced container list by its name and page URL key.
     *
     * @param        containerName   the container list name
     * @param        pageUrlKey      the page URL key
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list   
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     */
    public JahiaContainerList getAbsoluteContainerList(String containerName,
                                                       String pageUrlKey, String listViewId)
            throws JahiaException {
        return getAbsoluteContainerList(containerName, ServicesRegistry
                .getInstance().getJahiaPageService()
                .getPageIDByURLKeyAndSiteID(pageUrlKey,
                        page.getSiteID()), listViewId);
    }    

    //-------------------------------------------------------------------------
    /***
     * gets a container list in another page (relative page reference by
     * number of levels to go up)
     *
     * @param        containerName   the container list name
     * @param        levelNb         the numbers of level to go up in the
     *                               site structure;
     *                               -1 = Home Page
     * @param        listViewId   the container list ID in the tag for supporting 
     *                                    different views of the same list 
     * @return a JahiaContainerList object, or null if nothing found
     * @see          org.jahia.data.containers.JahiaContainerList
     *
     * !!! Warning : this method can return a null value !!!
     */
    public JahiaContainerList getRelativeContainerList(String containerName,
                                                       int levelNb, String listViewId)
            throws JahiaException {
        logger.debug("into relative....................");
        int pageID = ServicesRegistry.getInstance().getJahiaPageService().
                findPageIDFromLevel(
                        page.getID(), levelNb, processingContext);
        logger.debug(".........page id found : " + pageID);
        if (pageID != -1) {
            return getAbsoluteContainerList(containerName, pageID, listViewId);
        } else {
            return null;
        }
    } // end getRelativeContainerList


    /**
     * Returns a map that contains the current mapping of container list -> page
     * that represent the container lists that have been accessed by pages using
     * the absolute addressing method.
     *
     * @return a Set containing Integer objects that represent the container
     *         list IDs that have been accessed absolutely from the current page.
     */
    public Set<Integer> getAbsoluteContainerListAccesses() {
        return absoluteContainerListAccesses;
    }

    /*
        Map interface implementation methods
     */

    public boolean equals(Object o) {
        return containerLists.equals(o);
    }

    public int hashCode() {
        return containerLists.hashCode();
    }

    public Set<Map.Entry<String, JahiaContainerList>> entrySet() {
        return containerLists.entrySet();
    }

    public Collection<JahiaContainerList> values() {
        return containerLists.values();
    }

    public Set<String> keySet() {
        return containerLists.keySet();
    }

    public void clear()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Jahia container lists collection is read-only!");
    }

    public void putAll(Map<? extends String, ? extends JahiaContainerList> t)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Jahia container lists collection is read-only!");
    }

    public JahiaContainerList remove(Object key)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Jahia container lists collection is read-only!");
    }

    public JahiaContainerList put(String key,
                      JahiaContainerList value)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Jahia container lists collection is read-only!");
    }

    public JahiaContainerList get(Object key) {
        return containerLists.get(key);
    }

    public boolean containsValue(Object value) {
        return containerLists.containsValue(value);
    }

    public boolean containsKey(Object key) {
        return containerLists.containsKey(key);
    }

    public boolean isEmpty() {
        return containerLists.isEmpty();
    }

    public int size() {
        return containerLists.size();
    }

    public JahiaContainerList ensureContainerList(JahiaContainerDefinition def, int page, int parent, String listViewId) throws JahiaException {
        synchronized (JahiaContainerSet.CLASS_NAME) {
            int id = -1;
            if (parent == 0) {
                id = jahiaContainersService.getContainerListID(def.getName(), page);
            } else {
                id = jahiaContainersService.getContainerListID(def.getName(), page, parent);
            }
            JahiaContainerList list;
            if (id == -1) {
                int parentAclID = 0;
                if (parent != 0) {
                    parentAclID = jahiaContainersService.
                            loadContainerInfo(parent, processingContext.getEntryLoadRequest()).
                            getAclID();
                } else {
                    parentAclID = this.page.getAclID();
                }
                list = new JahiaContainerList(0, parent, page, def.getID(), 0);
                jahiaContainersService.saveContainerListInfo(list, parentAclID, processingContext);
            } else {
                list = jahiaContainersService.loadContainerListInfo(id, processingContext.getEntryLoadRequest());
            }
            if (list != null) {
                // When requesting an archived loadRequest
                EntryLoadRequest loadRequest = (EntryLoadRequest) processingContext.getEntryLoadRequest().clone();
                if (loadRequest.isVersioned()) {
                    if (processingContext.showRevisionDiff()) {
                        // todo not fully handle so we dont load deleted yet
                        loadRequest.setWithDeleted(true);
                        loadRequest.setWithMarkedForDeletion(true);
                    } else {
                        loadRequest.setWithDeleted(false);
                        if (processingContext.getOpMode().equals(ProcessingContext.EDIT)) {
                            loadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                        } else {
                            loadRequest.setWithMarkedForDeletion(false);
                        }
                    }
                }
                try {
                    list = ContainerFactory.getInstance().fullyLoadContainerList(list.getID(),
                            LoadFlags.ALL,
                            processingContext, loadRequest,
                            this.cachedFieldsFromContainers,
                            this.cachedContainersFromContainerLists,
                            this.cachedContainerListsFromContainers);
                } catch (final Throwable t) {
                   logger.error("Error in getContainerList", t);
                }
                this.addContainerList(list, listViewId);

                ensureMandatoryContainer(list, def.getContainerListType());
            }
            return list;
        }
    }

    private void ensureMandatoryContainer(JahiaContainerList list, int containerType) throws JahiaException{
        if (((containerType & JahiaContainerDefinition.MANDATORY_TYPE) != 0)
                && jahiaContainersService.getctnidsInList(list.getID(), EntryLoadRequest.STAGED).isEmpty()) {

            JahiaContainer container = new JahiaContainer(0,
                    processingContext.getJahiaID(),
                    list.getPageID(),
                    list.getID(),
                    0, /* rank */
                    list.getAclID(),
                    list.getctndefid(),
                    0, 2);
            jahiaContainersService.
                    saveContainer(container, list.getID(), processingContext);
            container.setLanguageCode(processingContext.getLocale().toString());
            container.fieldsStructureCheck(processingContext);
            jahiaContainersService.
                    saveContainer(container, list.getID(), processingContext);
            list.addContainer(container);
        }        
    }
    
    public Set<Integer> getRequestedContainers() {
        return requestedContainers;
    }

    public Set<Integer> getRequestedLists() {
        return requestedLists;
    }
} // end JahiaContainerSet
