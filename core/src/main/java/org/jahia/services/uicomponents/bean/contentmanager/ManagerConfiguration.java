/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.uicomponents.bean.contentmanager;

import org.jahia.ajax.gwt.client.widget.content.CustomPickerConfiguration;
import org.jahia.services.uicomponents.bean.editmode.EngineConfiguration;
import org.jahia.services.uicomponents.bean.editmode.EngineTab;
import org.jahia.services.uicomponents.bean.toolbar.Toolbar;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.*;

/**
 * Content manager configuration settings.
 * User: ktlili
 * Date: Apr 12, 2010
 * Time: 2:54:37 PM
 */
public class ManagerConfiguration implements Serializable, BeanNameAware {

    private static final long serialVersionUID = -8372296247741073414L;

    private String name;

    private List<Column> treeColumns;
    private List<Column> tableColumns;
    private List<Repository> repositories;

    private boolean hideLeftPanel = false;

    private List<String> folderTypes = new ArrayList<String>();
    private List<String> nodeTypes = new ArrayList<String>();
    private List<String> filters = new ArrayList<String>();
    private List<String> mimeTypes = new ArrayList<String>();

    private List<String> hiddenTypes = new ArrayList<String>();
    private String hiddenRegex;

    private String defaultView;
    private boolean enableDragAndDrop = true;
    private boolean enableFileDoubleClick = true;
    private boolean allowsMultipleSelection = true;
    private List<String> allowedNodeTypesForDragAndDrop;
    private List<String> forbiddenNodeTypesForDragAndDrop;
    private Set<String> excludedNodeTypes;

    private Map<String, EngineConfiguration> engineConfigurations;
    private List<EngineTab> engineTabs;

    private List<Toolbar> toolbars;
    private Toolbar contextMenu;

    private boolean expandRoot = false;

    private boolean allowRootNodeEditing;

    private boolean displaySearch = true;

    private boolean displaySearchInPage = true;
    private boolean displaySearchInTag = true;
    private boolean displaySearchInFile = true;
    private boolean displaySearchInContent = true;
    private boolean displaySearchInDateMeta = true;
    private boolean searchInFile = true;
    private boolean searchInContent = true;
    private boolean searchInCurrentSiteOnly = false;
    private String searchBasePath = null;

    private String requiredPermission;
    private boolean showOnlyNodesWithTemplates = false;

    private boolean editableGrid;

    private List<String> componentsPaths = Arrays.asList("$site/components/*");

    private String titleKey = null;

    private boolean suppressTreePublicationInfo;
    private boolean suppressTablePublicationInfo;

    private boolean useLargeThumbnails;

    private CustomPickerConfiguration customPickerConfiguration;

    public ManagerConfiguration() {
        tableColumns = new ArrayList<Column>();
        treeColumns = new ArrayList<Column>();
        repositories = new ArrayList<Repository>();
        engineTabs = new ArrayList<EngineTab>();
    }

    public String getName() {
        return name;
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
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

    public boolean isEnableDragAndDrop() {
        return enableDragAndDrop;
    }

    public void setEnableDragAndDrop(boolean enableDragAndDrop) {
        this.enableDragAndDrop = enableDragAndDrop;
    }

    public List<String> getAllowedNodeTypesForDragAndDrop() {
        return allowedNodeTypesForDragAndDrop;
    }

    public void setAllowedNodeTypesForDragAndDrop(List<String> allowedNodeTypesForDragAndDrop) {
        this.allowedNodeTypesForDragAndDrop = allowedNodeTypesForDragAndDrop;
    }

    public Set<String> getExcludedNodeTypes() {
        return excludedNodeTypes;
    }

    public void setExcludedNodeTypes(Set<String> excludedNodeTypes) {
        this.excludedNodeTypes = excludedNodeTypes;
    }

    public List<String> getForbiddenNodeTypesForDragAndDrop() {
        return forbiddenNodeTypesForDragAndDrop;
    }

    public void setForbiddenNodeTypesForDragAndDrop(List<String> forbiddenNodeTypesForDragAndDrop) {
        this.forbiddenNodeTypesForDragAndDrop = forbiddenNodeTypesForDragAndDrop;
    }

    public boolean isEnableFileDoubleClick() {
        return enableFileDoubleClick;
    }

    public void setEnableFileDoubleClick(boolean enableFileDoubleClick) {
        this.enableFileDoubleClick = enableFileDoubleClick;
    }

    public List<Toolbar> getToolbars() {
        return toolbars;
    }

    public void setToolbars(List<Toolbar> toolbars) {
        this.toolbars = toolbars;
    }

    public Toolbar getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Toolbar contextMenu) {
        this.contextMenu = contextMenu;
    }

    public boolean isAllowsMultipleSelection() {
        return allowsMultipleSelection;
    }

    public void setAllowsMultipleSelection(boolean allowsMultipleSelection) {
        this.allowsMultipleSelection = allowsMultipleSelection;
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

    /**
     * @return Map of engine configurations
     */
    public Map<String, EngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }

    public void setEngineConfigurations(Map<String, EngineConfiguration> engineConfigurations) {
        this.engineConfigurations = engineConfigurations;
    }

    public List<EngineTab> getEngineTabs() {
        return engineTabs;
    }

    public void setEngineTabs(List<EngineTab> engineTabs) {
        this.engineTabs = engineTabs;
    }

    public boolean isSearchInFile() {
        return searchInFile;
    }

    public void setSearchInFile(boolean searchInFile) {
        this.searchInFile = searchInFile;
    }

    public boolean isSearchInContent() {
        return searchInContent;
    }

    public void setSearchInContent(boolean searchInContent) {
        this.searchInContent = searchInContent;
    }

    public boolean isSearchInCurrentSiteOnly() {
        return searchInCurrentSiteOnly;
    }

    public void setSearchInCurrentSiteOnly(boolean searchInCurrentSiteOnly) {
        this.searchInCurrentSiteOnly = searchInCurrentSiteOnly;
    }

    public String getSearchBasePath() {
        return searchBasePath;
    }

    public void setSearchBasePath(String searchBasePath) {
        this.searchBasePath = searchBasePath;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public List<String> getHiddenTypes() {
        return hiddenTypes;
    }

    public void setHiddenTypes(List<String> hiddenTypes) {
        this.hiddenTypes = hiddenTypes;
    }

    public String getHiddenRegex() {
        return hiddenRegex;
    }

    public void setHiddenRegex(String hiddenRegex) {
        this.hiddenRegex = hiddenRegex;
    }

    public boolean isShowOnlyNodesWithTemplates() {
        return showOnlyNodesWithTemplates;
    }

    public void setShowOnlyNodesWithTemplates(boolean showOnlyNodesWithTemplates) {
        this.showOnlyNodesWithTemplates = showOnlyNodesWithTemplates;
    }

    public boolean isAllowRootNodeEditing() {
        return allowRootNodeEditing;
    }

    public void setAllowRootNodeEditing(boolean allowRootNodeEditing) {
        this.allowRootNodeEditing = allowRootNodeEditing;
    }

    public boolean isDisplaySearchInDateMeta() {
        return displaySearchInDateMeta;
    }

    public void setDisplaySearchInDateMeta(boolean displaySearchInDateMeta) {
        this.displaySearchInDateMeta = displaySearchInDateMeta;
    }

    public boolean isEditableGrid() {
        return editableGrid;
    }

    public void setEditableGrid(boolean editableGrid) {
        this.editableGrid = editableGrid;
    }

    public List<String> getComponentsPaths() {
        return componentsPaths;
    }

    public void setComponentsPaths(List<String> componentsPaths) {
        this.componentsPaths = componentsPaths;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public boolean isSuppressTreePublicationInfo() {
        return suppressTreePublicationInfo;
    }

    public boolean isSuppressTablePublicationInfo() {
        return suppressTablePublicationInfo;
    }

    /**
     * @deprecated use setSuppressTreePublicationInfo and/or setSuppressTablePublicationInfo
     */
    @Deprecated
    public void setSuppressPublicationInfo(boolean suppressPublicationInfo) {
        this.suppressTreePublicationInfo = suppressPublicationInfo;
        this.suppressTablePublicationInfo = suppressPublicationInfo;
    }

    public void setSuppressTreePublicationInfo(boolean suppressTreePublicationInfo) {
        this.suppressTreePublicationInfo = suppressTreePublicationInfo;
    }

    public void setSuppressTablePublicationInfo(boolean suppressTablePublicationInfo) {
        this.suppressTablePublicationInfo = suppressTablePublicationInfo;
    }

    /**
     * @deprecated since 7.2.3.1
     */
    @Deprecated
    public void setShowWorkInProgress(boolean showWorkInProgress) {
    }

    /**
     * Indicates if larger thumbnails should be used in the thumbnails view of this manager.
     *
     * @return <code>true</code> if larger thumbnails should be used; <code>false</code> - if standard ones
     */
    public boolean isUseLargeThumbnails() {
        return useLargeThumbnails;
    }

    /**
     * Flag to indicate if larger thumbnails should be used in the thumbnails view of this manager.
     *
     * @param useLargeThumbnails <code>true</code> if larger thumbnails should be used; <code>false</code> - if standard ones
     */
    public void setUseLargeThumbnails(boolean useLargeThumbnails) {
        this.useLargeThumbnails = useLargeThumbnails;
    }


    public CustomPickerConfiguration getCustomPickerConfiguration() {
        return customPickerConfiguration;
    }

    public void setCustomPickerConfiguration(CustomPickerConfiguration customPickerConfiguration) {
        this.customPickerConfiguration = customPickerConfiguration;
    }

}
