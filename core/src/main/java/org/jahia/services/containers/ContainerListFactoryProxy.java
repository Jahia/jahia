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
package org.jahia.services.containers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.data.containers.JahiaContainerList;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Container List Factory Proxy
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerListFactoryProxy {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContainerListFactoryProxy.class);

    private int loadFlag;
    private ProcessingContext jParams;
    private EntryLoadRequest loadRequest;
    private Map<Integer, List<Integer>> cachedFieldsFromContainers;
    private Map<Integer, List<Integer>> cachedContainersFromContainerLists;
    private Map<Integer, List<Integer>> cachedContainerListsFromContainers;
    
    private String listViewId;

    //--------------------------------------------------------------------------
    /**
     * @param loadFlag
     * @param jParams
     * @param loadRequest
     * @param cachedFieldsFromContainers
     * @param cachedContainersFromContainerLists
     *
     * @param cachedContainerListsFromContainers
     *
     */
    public ContainerListFactoryProxy(int loadFlag,
                                     ProcessingContext jParams,
                                     EntryLoadRequest loadRequest,
                                     Map<Integer, List<Integer>> cachedFieldsFromContainers,
                                     Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                     Map<Integer, List<Integer>> cachedContainerListsFromContainers) {
        this.loadFlag = loadFlag;
        this.jParams = jParams;
        this.loadRequest = loadRequest;
        this.cachedFieldsFromContainers = cachedFieldsFromContainers;
        if (this.cachedFieldsFromContainers == null) {
            this.cachedFieldsFromContainers = new HashMap<Integer, List<Integer>>();
        }
        this.cachedContainersFromContainerLists = cachedContainersFromContainerLists;
        if (this.cachedContainersFromContainerLists == null) {
            this.cachedContainersFromContainerLists = new HashMap<Integer, List<Integer>>();
        }
        this.cachedContainerListsFromContainers = cachedContainerListsFromContainers;
        if (this.cachedContainerListsFromContainers == null) {
            this.cachedContainerListsFromContainers = new HashMap<Integer, List<Integer>>();
        }
    }

    /**
     * Load this container's sub containers list if not already loaded
     */
    public void load(JahiaContainerList containerList) {
        if (containerList == null || containerList.isContainersLoaded()) {
            return;
        }
        try {
            // When requesting an archived loadRequest
            EntryLoadRequest currentLoadRequest =
                    (EntryLoadRequest) loadRequest.clone();
            if (jParams.showRevisionDiff()) {
                currentLoadRequest.setWithDeleted(true);
                currentLoadRequest.setWithMarkedForDeletion(true);
            } else {
                currentLoadRequest.setWithDeleted(false);
                if (jParams.getOpMode().equals(ProcessingContext.EDIT)) {
                    currentLoadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                } else {
                    currentLoadRequest.setWithMarkedForDeletion(false);
                }
            }
            ContainerFactory.getInstance().fullyLoadContainerList(containerList,
                    loadFlag, jParams, currentLoadRequest, cachedFieldsFromContainers,
                    this.cachedContainersFromContainerLists,
                    this.cachedContainerListsFromContainers, getListViewId());
            containerList.setIsContainersLoaded(true);
        } catch (JahiaException je) {
            logger.debug("Exception occured when loading Container List ["
                    + containerList.getID() + "]", je);
        }
    }

    public String getListViewId() {
        return listViewId;
    }

    public void setListViewId(String listViewId) {
        this.listViewId = listViewId;
    }
}
