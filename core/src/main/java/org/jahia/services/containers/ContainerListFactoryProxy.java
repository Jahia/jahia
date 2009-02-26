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
