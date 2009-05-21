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
package org.jahia.services.containers;

import org.jahia.params.ProcessingContext;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.data.containers.JahiaContainerList;

import java.util.List;
import java.util.Map;

/**
 * Context Bean used with ContainerListLoader class
 *
 * User: hollis
 * Date: 14 mars 2008
 * Time: 12:52:38
 * To change this template use File | Settings | File Templates.
 */
public class ContainerListLoaderContext {

    private ProcessingContext context;
    private EntryLoadRequest loadVersion;
    private Map<Integer, List<Integer>> cachedContainersFromContainerLists;
    private Map<Integer, List<Integer>> cachedFieldsInContainer;
    private Map<Integer, List<Integer>> cachedContainerListsFromContainers;
    private int loadFlag;
    private JahiaContainerList cList;
    private Boolean isLoadingUseSingleSearchQuery = Boolean.FALSE;
    private String listViewId;

    public String getListViewId() {
        return listViewId;
    }

    public void setListViewId(String listViewId) {
        this.listViewId = listViewId;
    }

    /**
     *
     * @param context
     * @param loadVersion
     * @param cList
     * @param loadFlag
     * @param cachedContainersFromContainerLists
     * @param cachedFieldsInContainer
     * @param cachedContainerListsFromContainers
     */
    public ContainerListLoaderContext( ProcessingContext context,
                                        EntryLoadRequest loadVersion,
                                        JahiaContainerList cList,
                                        int loadFlag,
                                        Map<Integer, List<Integer>> cachedContainersFromContainerLists,
                                        Map<Integer, List<Integer>> cachedFieldsInContainer,
                                        Map<Integer, List<Integer>> cachedContainerListsFromContainers) {
        this.context = context;
        this.loadVersion = loadVersion;
        this.cachedContainersFromContainerLists = cachedContainersFromContainerLists;
        this.cachedFieldsInContainer = cachedFieldsInContainer;
        this.cList = cList;
        this.loadFlag = loadFlag;
        this.cachedContainerListsFromContainers = cachedContainerListsFromContainers;
    }

    public ProcessingContext getContext() {
        return context;
    }

    public void setContext(ProcessingContext context) {
        this.context = context;
    }

    public EntryLoadRequest getLoadVersion() {
        return loadVersion;
    }

    public void setLoadVersion(EntryLoadRequest loadVersion) {
        this.loadVersion = loadVersion;
    }

    public Map<Integer, List<Integer>> getCachedContainersFromContainerLists() {
        return cachedContainersFromContainerLists;
    }

    public void setCachedContainersFromContainerLists(Map<Integer, List<Integer>> cachedContainersFromContainerLists) {
        this.cachedContainersFromContainerLists = cachedContainersFromContainerLists;
    }

    public Map<Integer, List<Integer>> getCachedFieldsInContainer() {
        return cachedFieldsInContainer;
    }

    public void setCachedFieldsInContainer(Map<Integer, List<Integer>> cachedFieldsInContainer) {
        this.cachedFieldsInContainer = cachedFieldsInContainer;
    }

    public Map<Integer, List<Integer>> getCachedContainerListsFromContainers() {
        return cachedContainerListsFromContainers;
    }

    public void setCachedContainerListsFromContainers(Map<Integer, List<Integer>> cachedContainerListsFromContainers) {
        this.cachedContainerListsFromContainers = cachedContainerListsFromContainers;
    }

    public int getLoadFlag() {
        return loadFlag;
    }

    public void setLoadFlag(int loadFlag) {
        this.loadFlag = loadFlag;
    }

    public JahiaContainerList getCList() {
        return cList;
    }

    public void setCList(JahiaContainerList cList) {
        this.cList = cList;
    }

    /**
     * true if filtering/searching/sorting are all done as one single Search query.
     * In such case, defined query property's like <code>JahiaQueryObjectModelConstants.SEARCH_MAX_HITS</code> can be applyed efficiently to just load mininal search hits.
     * If 10 is defined as SEARCH_MAX_HITS, then iteration over the search hits will stop as soon as the number of 10 requested
     * containers are loadable ( right checking and time based checking applyed ).
     *
     * @return
     */
    public Boolean getLoadingUseSingleSearchQuery() {
        return isLoadingUseSingleSearchQuery;
    }

    public void setLoadingUseSingleSearchQuery(Boolean loadingUseSingleSearchQuery) {
        isLoadingUseSingleSearchQuery = loadingUseSingleSearchQuery;
    }

}
