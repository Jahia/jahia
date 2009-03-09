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

package org.jahia.services.containers;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.CoreFilterNames;
import org.jahia.data.containers.*;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;

import java.util.*;


/**
 * Container Factory.
 * Load Containers and Container Lists, applying filtering and pagination.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerFactory {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContainerFactory.class);

    private static ContainerFactory instance = null;

    // the preloaded container acl id by container list.
    public static final String FULLY_LOADED_LIST_CACHE = "FullyLoadedListCache";
//    private static Cache preloadedCtnrACLIDByCtnrListCache;
    private JahiaContainersService jahiaContainersService;
    //--------------------------------------------------------------------------

    /**
     * Constructor
     */
    private ContainerFactory() {
        jahiaContainersService = ServicesRegistry.getInstance().getJahiaContainersService();
    }

    public static synchronized ContainerFactory getInstance() {
        if (instance == null) {
            instance = new ContainerFactory();
        }
        return instance;
    }

    /**
     * Load all container lists for a given container. This is performed only if
     * the container.isContainerListsLoaded() return false.
     *
     * @param container                  , a container that is not completely loaded.
     * @param jParams                    , used to handle all filtering
     * @param loadFlag
     * @param cachedFieldsFromContainers map with key=ctnid, obj=List of the ctn's field ids.
     *                                   can be null, but for performance this map should be loaded with all fields of same page ID as the container's pageID.
     * @throws JahiaException
     */
    public void fullyLoadContainer(final JahiaContainer container,
                                   final int loadFlag,
                                   final ProcessingContext jParams,
                                   final EntryLoadRequest loadRequest,
                                   final Map<Integer, List<Integer>> cachedFieldsFromContainers,
                                   final Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                   final Map<Integer, List<Integer>> cachedContainerListsFromContainers)
            throws JahiaException {

        if (container == null) {
            return;
        }

        if (!container.isFieldsLoaded()) {
            synchronized (container) {
                container.clearFields();
                container.setFieldsLoaded(true);
                // not already in fullyLoadedList -> load it
                final List<Integer> fieldIDs = jahiaContainersService
                        .getFieldIDsInContainer(container.getID(), loadRequest);
                container.addFields(ServicesRegistry.getInstance()
                        .getJahiaFieldService().loadFields(fieldIDs, loadFlag,
                                jParams, loadRequest, container.getListID()));
            }
        }

        if (!container.isContainerListsLoaded()) {
            synchronized (container) {
                container.clearContainerLists();
                container.setContainerListsLoaded(true);

                // load container lists
                // load the the containers for this one
                // apply containers search and filtering.
                // listIDs = ctnServ.getCtnListIDsInContainer(container.getID());
                final List<Integer> listIDs = jahiaContainersService
                        .getSubContainerListIDs(container.getID(), loadRequest);
                for (Integer listID : listIDs) {
                    final JahiaContainerList cList = jahiaContainersService.loadContainerList(
                            listID.intValue(), loadFlag, jParams, loadRequest,
                            cachedFieldsFromContainers,
                            cachedContainersFromContainerLists,
                            cachedContainerListsFromContainers);

                    if (cList != null) {
                        container.addContainerList(cList);
                    }
                }
            }
        }
    }

    /**
     * Fully load a container list (containers and their own container lists...).
     * This is performed only if the containerList.isContainersLoaded() return false.
     *
     * @param cList                      , a container list that is not completely loaded.
     * @param jParams                    , used to handle all filtering
     * @param loadFlag
     * @param cachedFieldsFromContainers map with key=ctnid, obj=List of the ctn's field ids.
     *                                   can be null, but for performance this map should be loaded with all fields of same page ID as the container's pageID.
     * @throws JahiaException
     */
    public JahiaContainerList fullyLoadContainerList(final JahiaContainerList cList,
                                                     final int loadFlag,
                                                     final ProcessingContext jParams,
                                                     final EntryLoadRequest loadRequest,
                                                     final Map<Integer, List<Integer>> cachedFieldsFromContainers,
                                                     final Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                                     final Map<Integer, List<Integer>> cachedContainerListsFromContainers,
                                                     final String listViewId)
            throws JahiaException {
        if (cList == null) {
            return null;
        }
        if (!cList.isContainersLoaded()) {
            cList.clearContainers();
            cList.setIsContainersLoaded(true);
            // start check for correct rights.
            if (jParams != null) { // no jParams, can't check for rights
                final JahiaUser currentUser = jParams.getUser();
                if (currentUser != null) {
                    logger.debug("loadContainerList(): checking rights...");
                    // if the user has no read rights, return an empty list.
                    if (!cList.checkReadAccess(currentUser)) {
                        logger.debug("loadContainerList(): NO read rights! -> returning empty list");
                        return cList;
                    }
                    logger.debug("loadContainerList(): read rights OK");
                } else {
                    throw new JahiaException("No user present !",
                            "No current user defined in the params in loadContainerList() method.",
                            JahiaException.USER_ERROR, JahiaException.ERROR_SEVERITY);
                }

                ContainerListLoader listLoader = ContainerListLoader.getInstance(jParams,cList);
                ContainerListLoaderContext listLoaderContext = new ContainerListLoaderContext(jParams,loadRequest,cList,
                        LoadFlags.ALL,cachedContainersFromContainerLists,null,null);
                listLoaderContext.setListViewId(listViewId);
                List<Integer> ctnids = listLoader.doContainerFilterSearchSort(listLoaderContext);

                if (ctnids == null) {
                    ctnids = jahiaContainersService.
                            getctnidsInList(cList.getID(), loadRequest);
                    listLoaderContext.setLoadingUseSingleSearchQuery(Boolean.FALSE);
                }
                if (ctnids == null) {
                    ctnids = new ArrayList<Integer>();
                }
                if (!cList.isContainersLoaded() || !listLoaderContext.getLoadingUseSingleSearchQuery().booleanValue()){
                    cList.setIsContainersLoaded(true);
                    ctnids = loadContainerListWithContainers(cList, ctnids, loadFlag, jParams, loadRequest,
                            cachedFieldsFromContainers, cachedContainersFromContainerLists,
                            cachedContainerListsFromContainers, currentUser, listViewId);
                } else {
                    ctnids = repaginateContainerListWithContainers(cList, ctnids, jParams, currentUser, listViewId);
                }
                final SessionState session = jParams.getSessionState();
                if (session != null) {
                    if ( ctnids != null && !ctnids.isEmpty() ){
                        session.setAttribute("getSorteredAndFilteredCtnIds" + cList.getID(), ctnids);
                    } else {
                        session.removeAttribute("getSorteredAndFilteredCtnIds" + cList.getID());
                    }
                }
            }
        }
        return cList;
    }

    /**
     * Returns the Container IDs of a container list. These Ids are sorted and filtered.
     *
     * @param jParams , used to handle all filtering
     * @param cList   , a container list that is not completely loaded.
     * @throws JahiaException
     */
    public List<Integer> getSorteredAndFilteredCtnIds(final ProcessingContext jParams,
                                               final EntryLoadRequest loadRequest,
                                               final JahiaContainerList cList,
                                               final Map<Integer, List<Integer>> cachedContainersFromContainerLists)
            throws JahiaException {
        /*
        List ctnids = doContainerFilterSearchSort(jParams, loadRequest,
                cList.getDefinition().getName(), cList,
                cachedContainersFromContainerLists);
        */
        ContainerListLoader listLoader = ContainerListLoader.getInstance(jParams,cList);
        ContainerListLoaderContext listLoaderContext = new ContainerListLoaderContext(jParams,loadRequest,cList,
                LoadFlags.ALL,cachedContainersFromContainerLists,null,null);
        List<Integer> ctnids = listLoader.doContainerFilterSearchSort(listLoaderContext);

        if (ctnids == null) {
            ctnids = jahiaContainersService.
                    getctnidsInList(cList.getID(), loadRequest);
        }
        if (ctnids == null) {
            ctnids = new ArrayList<Integer>();
        }

        return ctnids;
    }

    /**
     * Fully load a container list (containers and their own container lists...).
     *
     * @param ctnListID                  , a container list ID.
     * @param jParams                    , used to handle all filtering
     * @param loadFlag
     * @param cachedFieldsFromContainers map with key=ctnid, obj=List of the ctn's field ids.
     *                                   can be null, but for performance this map should be loaded with all fields of same page ID as the container's pageID.
     * @return a fully loaded Container.
     * @throws JahiaException
     */
    public JahiaContainerList fullyLoadContainerList(final int ctnListID,
                                                     final int loadFlag,
                                                     final ProcessingContext jParams,
                                                     final EntryLoadRequest loadRequest,
                                                     final Map<Integer, List<Integer>> cachedFieldsFromContainers,
                                                     final Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                                     final Map<Integer, List<Integer>> cachedContainerListsFromContainers)
            throws JahiaException {
        JahiaContainerList cList = jahiaContainersService.
                loadContainerListInfo(ctnListID, loadRequest);
        /*
         * We bybass lazy load here! 
        cList = fullyLoadContainerList(cList, loadFlag, jParams, loadRequest,
                cachedFieldsFromContainers, cachedContainersFromContainerLists,
                cachedContainerListsFromContainers);
        */
        if (cList != null) {
            final ContainerListFactoryProxy cListFactory =
                    new ContainerListFactoryProxy(loadFlag,
                            jParams,
                            loadRequest,
                            cachedFieldsFromContainers,
                            cachedContainersFromContainerLists,
                            cachedContainerListsFromContainers);
            cList.setFactoryProxy(cListFactory);
        }
        return cList;
    }

    //-------------------------------------------------------------------------
    /**
     * Loads a set of containers for a given container list
     * Loads container list info and containers, but not dependant container lists
     * this method can load request-specific values (i.e. applications).
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param theContainerList        the container list
     * @param ctnids                  List of container ids to load.
     * @param loadFlag                the loadFlag
     * @param jParams                 the ProcessingContext object, containing request and response
     * @param loadVersion
     * @param cachedFieldsInContainer
     * @param cachedContainersFromContainerLists
     * @param cachedContainersListsFromContainers
     * @param currentUser
     * @param listViewId   
     *
     * @throws JahiaException
     */
    protected List<Integer> loadContainerListWithContainers(
            final JahiaContainerList theContainerList,
            List<Integer> ctnids,
            final int loadFlag,
            final ProcessingContext jParams,
            final EntryLoadRequest loadVersion,
            final Map<Integer, List<Integer>> cachedFieldsInContainer,
            final Map<Integer, List<Integer>> cachedContainersFromContainerLists,
            final Map<Integer, List<Integer>> cachedContainerListsFromContainers,
            final JahiaUser currentUser,
            final String listViewId)
            throws JahiaException {

        if (theContainerList == null) {
            return null;
        }
        List<Integer> res = new ArrayList<Integer>(ctnids.size());

        Map<Integer, JahiaContainer> loadedContainers = new HashMap<Integer, JahiaContainer>();

        Integer lastEditedItemIntId = (Integer)jParams.getSessionState()
            .getAttribute("ContextualContainerList_"+String.valueOf(theContainerList.getID()));
        int lastEditedItemId = 0;
        if(lastEditedItemIntId!=null){
            lastEditedItemId = lastEditedItemIntId.intValue();
        }

        // start check for correct rights.
        if (jParams != null) { // no jParams, can't check for rights

            if (ctnids.size() > 0) {
                // Check for expired container
                boolean disableTimeBasedPublishingFilter = jParams.isFilterDisabled(CoreFilterNames.
                        TIME_BASED_PUBLISHING_FILTER);

                // lets set the full size of the data for the container list.
                theContainerList.setFullSize(ctnids.size());

                // init the container list pagination
                JahiaContainerListPagination cListPagination = theContainerList.getCtnListPagination(false);
                if (cListPagination !=null){
                    cListPagination = new JahiaContainerListPagination(theContainerList, jParams,
                            cListPagination.getWindowSize(),new ArrayList<Integer>(ctnids), lastEditedItemId, listViewId);
                } else {
                    cListPagination = new JahiaContainerListPagination(theContainerList, jParams, -1,
                            new ArrayList<Integer>(ctnids),lastEditedItemId, listViewId);
                }
                theContainerList.setCtnListPagination(cListPagination);

                int endPos = cListPagination.getSize();

                if (cListPagination.isValid()) {
                    endPos = cListPagination.getLastItemIndex();
                }
                ContainerACLRetriever ctnACLRetriever = null;
                if (ctnids.size()>10){
                    int batchSize = 100;
                    if (cListPagination.isValid()){
                        batchSize = cListPagination.getWindowSize();
                        if (batchSize<100){
                            batchSize = 100;
                        } else if (batchSize>org.jahia.settings.SettingsBean.getInstance().getDBMaxElementsForInClause()){
                            batchSize = org.jahia.settings.SettingsBean.getInstance().getDBMaxElementsForInClause();
                        }
                    }
                    ctnACLRetriever = new ContainerACLRetriever(ctnids,batchSize);
                }

                List<Integer> v = new ArrayList<Integer>();
                for (int i = 0, size = ctnids.size(); i < size; i++) {
                    try {
                        Integer ctnID = ctnids.get(i);
                        int aclID = ctnACLRetriever != null ? ctnACLRetriever
                                .getACL(ctnID.intValue(), i)
                                : jahiaContainersService
                                        .getContainerACLID(ctnID.intValue());                        
                        JahiaAcl acl = null;
                        if (aclID == -1){
                            continue;
                        }
                        acl = ServicesRegistry.getInstance().getJahiaACLManagerService().lookupACL(aclID);
                        if (acl!= null && acl.getPermission(currentUser, JahiaBaseACL.READ_RIGHTS)) {
                            if ( !disableTimeBasedPublishingFilter ){
                                final TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance()
                                        .getTimeBasedPublishingService();
                                if ( ParamBean.NORMAL.equals(jParams.getOperationMode()) ){
                                    if (!tbpServ.isValid(new ContentContainerKey(ctnID),
                                            jParams.getUser(),jParams.getEntryLoadRequest(),jParams.getOperationMode(),
                                            (Date)null)){
                                        continue;
                                    }
                                } else if ( ParamBean.PREVIEW.equals(jParams.getOperationMode()) ){
                                    if (!tbpServ.isValid(new ContentContainerKey(ctnID),
                                            jParams.getUser(),jParams.getEntryLoadRequest(),jParams.getOperationMode(),
                                            AdvPreviewSettings.getThreadLocaleInstance())){
                                        continue;
                                    }
                                }
                            }
                            try {
                                JahiaContainer container = jahiaContainersService.loadContainer(ctnID.intValue(), loadFlag, jParams,
                                        loadVersion,
                                        cachedFieldsInContainer,
                                        cachedContainersFromContainerLists,
                                        cachedContainerListsFromContainers);
                                if (container != null && container.getID() != -1 && !v.contains(ctnID)) {
                                    v.add(ctnID);
                                    loadedContainers.put(ctnID,container);
                                } else {
                                    continue;
                                }
                            } catch (Exception t) {
                                String errorMsg = "Error loading container [" + ctnID.intValue() + "]";
                                if (loadVersion != null) {
                                    errorMsg += " loadVersion=" + loadVersion.toString();
                                }
                                logger.debug(errorMsg);
                            }

                            if (acl.getPermission(currentUser, JahiaBaseACL.WRITE_RIGHTS)) {
                                if (ctnID.intValue() != -1
                                        && !res.contains(ctnID)) {
                                    res.add(ctnID);
                                }
                            }
                            if ( (v.size() >= theContainerList.getMaxSize()) ||
                                    (v.size() > endPos + org.jahia.settings.SettingsBean.getInstance().getPreloadedItemsForPagination()) ){
                                break;
                            }
                        }
                    } catch (Exception t) {
                        logger.error(t);
                    }
                }
                ctnids = v;
            }
        }

        // lets set the full size of the data for the container list.
        theContainerList.setFullSize(ctnids.size());

        // init the container list pagination
        JahiaContainerListPagination cListPagination = theContainerList.getCtnListPagination(false);
        if (cListPagination !=null){
            cListPagination = new JahiaContainerListPagination(theContainerList, jParams,
                    cListPagination.getWindowSize(),new ArrayList<Integer>(ctnids), lastEditedItemId, listViewId);
        } else {
            cListPagination = new JahiaContainerListPagination(theContainerList, jParams, -1,new ArrayList<Integer>(ctnids), lastEditedItemId, listViewId);
        }
        theContainerList.setCtnListPagination(cListPagination);

        int startPos = 0;
        int endPos = cListPagination.getSize();

        if (cListPagination.isValid()) {
            startPos = cListPagination.getFirstItemIndex();
            endPos = cListPagination.getLastItemIndex();
        }
        if (endPos < cListPagination.getSize()) {
            endPos += 1;
        }

        JahiaContainer thisContainer = null;
        for (int i = startPos; i < endPos; i++) {
            int ctnid = ((Integer) ctnids.get(i)).intValue();
            try {
                thisContainer = (JahiaContainer)loadedContainers.get(new Integer(ctnid));
                if (thisContainer==null){
                    thisContainer = jahiaContainersService.loadContainer(ctnid, loadFlag, jParams, loadVersion,
                        cachedFieldsInContainer,
                        cachedContainersFromContainerLists,
                        cachedContainerListsFromContainers);
                }
            } catch (Exception t) {
                String errorMsg = "Error loading container [" + ctnid + "]";
                if (loadVersion != null) {
                    errorMsg += " loadVersion=" + loadVersion.toString();
                }
                logger.debug(errorMsg);
            }

            if (thisContainer != null && thisContainer.getID() != -1) { // no read rights on this container
                theContainerList.addContainer(thisContainer);
            }
        }
        if (theContainerList != null) {
            theContainerList.setIsContainersLoaded(true);
        }
        return res;
    }

    /**
     * The container list already contains the loaded containers.
     * We just need to apply the correct pagination and returns the List of ids for which there is WRITE access allowed
     * for Editing engine.
     *
     * DO NOT CACHE THIS METHOD (it depends on other caches values) !!
     *
     * @param theContainerList
     * @param ctnids
     * @param jParams
     * @param currentUser
     * @param listViewId
     * @return
     * @throws JahiaException
     */
    protected List<Integer> repaginateContainerListWithContainers(
            final JahiaContainerList theContainerList,
            List<Integer> ctnids,
            final ProcessingContext jParams,
            final JahiaUser currentUser,
            final String listViewId)
            throws JahiaException {

        if (theContainerList == null){
            return Collections.emptyList();
        }
        theContainerList.setIsContainersLoaded(true);
        if (theContainerList.getContainersList()==null || theContainerList.getContainersList().isEmpty()){
            return Collections.emptyList();
        }

        Integer lastEditedItemIntId = (Integer)jParams.getSessionState()
            .getAttribute("ContextualContainerList_"+String.valueOf(theContainerList.getID()));
        int lastEditedItemId = 0;
        if(lastEditedItemIntId!=null){
            lastEditedItemId = lastEditedItemIntId.intValue();
        }
        
        List<Integer> res = new ArrayList<Integer>(ctnids.size());

        JahiaContainer thisContainer = null;
        List<JahiaContainer> containers = new ArrayList<JahiaContainer>();
        containers.addAll(theContainerList.getContainersList());
        Iterator<JahiaContainer> it = containers.iterator();
        while(it.hasNext()){
            thisContainer = (JahiaContainer)it.next();
            if (thisContainer.getACL().getPermission(currentUser, JahiaBaseACL.WRITE_RIGHTS)) {
                res.add(new Integer(thisContainer.getID()));
            }
        }

        // init the container list pagination
        JahiaContainerListPagination cListPagination = theContainerList.getCtnListPagination(false);
        if (cListPagination !=null){
            cListPagination = new JahiaContainerListPagination(theContainerList, jParams,
                    cListPagination.getWindowSize(),res,lastEditedItemId, listViewId);
        } else {
            cListPagination = new JahiaContainerListPagination(theContainerList, jParams, -1,res, lastEditedItemId, listViewId);
        }
        theContainerList.setCtnListPagination(cListPagination);

        int startPos = 0;
        int endPos = ctnids.size();

        if (!cListPagination.isValid()) {
            return res;
        }
        if (cListPagination.isValid()) {
            startPos = cListPagination.getFirstItemIndex();
            endPos = cListPagination.getLastItemIndex();
        }
        if (endPos < cListPagination.getSize()) {
            endPos += 1;
        }

        theContainerList.clearContainers();
        for (int i = startPos; i < endPos; i++) {
            thisContainer = (JahiaContainer)containers.get(i);
            theContainerList.addContainer(thisContainer);
        }
        theContainerList.setIsContainersLoaded(true);
        return res;

    }

}
