/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.uicomponents.bean.contentmanager;

import org.jahia.services.uicomponents.bean.editmode.Engine;
import org.jahia.services.uicomponents.bean.editmode.EngineTab;
import org.jahia.services.uicomponents.bean.toolbar.ToolbarSet;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 12, 2010
 * Time: 2:54:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManagerConfiguration implements Serializable {

    private List<Column> treeColumns;
    private List<Column> tableColumns;
    private List<Repository> repositories;
    private List<EngineTab> tabs;

    private boolean hideLeftPanel = false;

    private List<String> folderTypes = new ArrayList<String>();
    private List<String> nodeTypes = new ArrayList<String>();
    private List<String> filters = new ArrayList<String>();
    private List<String> mimeTypes = new ArrayList<String>();

    private String defaultView;
    private boolean enableFileDoubleClick = true;
    private boolean useCheckboxForSelection = true;

    private List<Engine> createEngines;
    private List<Engine> editEngines;

    private ToolbarSet toolbarSet;


    private boolean expandRoot = false;

    private boolean displaySearch = true;

    private boolean displaySearchInPage = true;
    private boolean displaySearchInTag = true;
    private boolean displaySearchInFile = true;
    private boolean displaySearchInContent = true;



    public ManagerConfiguration() {
        tableColumns = new ArrayList<Column>();
        treeColumns = new ArrayList<Column>();
        repositories = new ArrayList<Repository>();
        tabs = new ArrayList<EngineTab>();
    }

    public List<EngineTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<EngineTab> tabs) {
        this.tabs = tabs;
    }

    public void setHideLeftPanel(boolean hide) {
        this.hideLeftPanel = hide;
    }

    public List<Column> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<Column> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public List<Column> getTreeColumns() {
        return treeColumns;
    }

    public void setTreeColumns(List<Column> treeColumns) {
        this.treeColumns = treeColumns;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public boolean isHideLeftPanel() {
        return hideLeftPanel;
    }

    public List<String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<String> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public List<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public String getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(String defaultView) {
        this.defaultView = defaultView;
    }

    public boolean isEnableFileDoubleClick() {
        return enableFileDoubleClick;
    }

    public void setEnableFileDoubleClick(boolean enableFileDoubleClick) {
        this.enableFileDoubleClick = enableFileDoubleClick;
    }

    public ToolbarSet getToolbarSet() {
        return toolbarSet;
    }

    public void setToolbarSet(ToolbarSet toolbarSet) {
        this.toolbarSet = toolbarSet;
    }

    public boolean isUseCheckboxForSelection() {
        return useCheckboxForSelection;
    }

    public void setUseCheckboxForSelection(boolean useCheckboxForSelection) {
        this.useCheckboxForSelection = useCheckboxForSelection;
    }

    public boolean isExpandRoot() {
        return expandRoot;
    }

    public void setExpandRoot(boolean expandRoot) {
        this.expandRoot = expandRoot;
    }

    public boolean isDisplaySearch() {
        return displaySearch;
    }

    public void setDisplaySearch(boolean displaySearch) {
        this.displaySearch = displaySearch;
    }

    public boolean isDisplaySearchInPage() {
        return displaySearchInPage;
    }

    public void setDisplaySearchInPage(boolean displaySearchInPage) {
        this.displaySearchInPage = displaySearchInPage;
    }

    public boolean isDisplaySearchInTag() {
        return displaySearchInTag;
    }

    public void setDisplaySearchInTag(boolean displaySearchInTag) {
        this.displaySearchInTag = displaySearchInTag;
    }

    public boolean isDisplaySearchInFile() {
        return displaySearchInFile;
    }

    public void setDisplaySearchInFile(boolean displaySearchInFile) {
        this.displaySearchInFile = displaySearchInFile;
    }

    public boolean isDisplaySearchInContent() {
        return displaySearchInContent;
    }

    public void setDisplaySearchInContent(boolean displaySearchInContent) {
        this.displaySearchInContent = displaySearchInContent;
    }

    public List<Engine> getCreateEngines() {
        return createEngines;
    }

    public void setCreateEngines(List<Engine> createEngines) {
        this.createEngines = createEngines;
    }

    public List<Engine> getEditEngines() {
        return editEngines;
    }

    public void setEditEngines(List<Engine> editEngines) {
        this.editEngines = editEngines;
    }

}
