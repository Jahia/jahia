/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.services.uicomponents.bean.toolbar.Toolbar;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.*;

/**
 * Represent edit mode configuration, including side panel and side panel toolbar,
 * top toolbar, main module toolbar and context menu.
 * Core side of GWTConfiguration. See link between this and GWTConfiguration in {@link org.jahia.ajax.gwt.helper.UIConfigHelper}
 * @author ktlili
 * Date: Apr 14, 2010
 * Time: 12:26:47 PM
 */
public class EditConfiguration implements Serializable, BeanNameAware {

    private static final long serialVersionUID = -5469841056796249203L;

    private String name;

    private Toolbar topToolbar;
    private Toolbar sidePanelToolbar;
    private Toolbar mainModuleToolbar;
    private Toolbar contextMenu;

    private List<SidePanelTab> tabs;

    private Map<String, EngineConfiguration> engineConfigurations;

    private String defaultLocation;

    private String sitesLocation;
    private String forcedSite;

    private boolean enableDragAndDrop = true;
    private boolean refreshOnExternalModification = true;

    private String requiredPermission;
    private String nodeCheckPermission;
    private String defaultUrlMapping;

    private List<String> componentsPaths = Arrays.asList("$site/components/*");

    private Set<String> editableTypes;
    private Set<String> nonEditableTypes;
    private Set<String> visibleTypes;
    private Set<String> nonVisibleTypes;
    private Set<String> excludedNodeTypes;

    private Set<String> bypassModeForTypes;

    private Set<String> skipMainModuleTypesDomParsing;

    private boolean forceHeaders = false;

    private boolean loadSitesList = true;

    private boolean useFullPublicationInfoInMainAreaModules;

    private boolean supportChannelsDisplay = true;

    public void addTab(SidePanelTab tab) {
        tabs.add(tab);
    }

    public void addTab(int index, SidePanelTab tab) {
        tabs.add(index, tab);
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Toolbar getTopToolbar() {
        return topToolbar;
    }

    public void setTopToolbar(Toolbar topToolbar) {
        this.topToolbar = topToolbar;
    }

    public Toolbar getSidePanelToolbar() {
        return sidePanelToolbar;
    }

    public void setSidePanelToolbar(Toolbar sidePanelToolbar) {
        this.sidePanelToolbar = sidePanelToolbar;
    }

    public Toolbar getMainModuleToolbar() {
        return mainModuleToolbar;
    }

    public void setMainModuleToolbar(Toolbar mainModuleToolbar) {
        this.mainModuleToolbar = mainModuleToolbar;
    }

    public Toolbar getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Toolbar contextMenu) {
        this.contextMenu = contextMenu;
    }

    public List<SidePanelTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<SidePanelTab> tabs) {
        this.tabs = tabs;
    }

    public EngineConfiguration getDefaultEditConfiguration() {
        if (engineConfigurations == null) {
            engineConfigurations = new HashMap<String, EngineConfiguration>();
        }
        if (!engineConfigurations.containsKey("nt:base")) {
            engineConfigurations.put("nt:base", new EngineConfiguration());
        }
        return engineConfigurations.get("nt:base");
    }

    public Map<String, EngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }

    public void setEngineConfigurations(Map<String, EngineConfiguration> engineConfigurations) {
        this.engineConfigurations = engineConfigurations;
    }

    public String getSitesLocation() {
        return sitesLocation;
    }

    public void setSitesLocation(String sitesLocation) {
        this.sitesLocation = sitesLocation;
    }

    public String getForcedSite() {
        return forcedSite;
    }

    public void setForcedSite(String forcedSite) {
        this.forcedSite = forcedSite;
    }

    public boolean isEnableDragAndDrop() {
        return enableDragAndDrop;
    }

    public void setEnableDragAndDrop(boolean enableDragAndDrop) {
        this.enableDragAndDrop = enableDragAndDrop;
    }

    public boolean getRefreshOnExternalModification() {
        return refreshOnExternalModification;
    }

    public void setRefreshOnExternalModification(boolean refreshOnExternalModification) {
        this.refreshOnExternalModification = refreshOnExternalModification;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public String getNodeCheckPermission() {
        return nodeCheckPermission;
    }

    public void setNodeCheckPermission(String nodeCheckPermission) {
        this.nodeCheckPermission = nodeCheckPermission;
    }

    public String getDefaultUrlMapping() {
        return defaultUrlMapping;
    }

    public void setDefaultUrlMapping(String defaultUrlMapping) {
        this.defaultUrlMapping = defaultUrlMapping;
    }

    public void removeTab(String tabKey) {
        if (tabKey != null && tabKey.length() > 0) {
            for (Iterator<SidePanelTab> iterator = getTabs().iterator(); iterator.hasNext();) {
                SidePanelTab tab = iterator.next();
                if (tab.getKey() != null && tab.getKey().equals(tabKey)) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * @return List of paths to the components
     */
    public List<String> getComponentsPaths() {
        return componentsPaths;
    }

    public void setComponentsPaths(List<String> componentsPaths) {
        this.componentsPaths = componentsPaths;
    }

    /**
     * @return Set of editable types
     */
    public Set<String> getEditableTypes() {
        return editableTypes;
    }

    public void setEditableTypes(Set<String> editableTypes) {
        this.editableTypes = editableTypes;
    }

    /**
     * @return Set of non editable types
     */
    public Set<String> getNonEditableTypes() {
        return nonEditableTypes;
    }

    public void setNonEditableTypes(Set<String> nonEditableTypes) {
        this.nonEditableTypes = nonEditableTypes;
    }

    /**
     * @return Set of types on witch we avoid to parse dom for module lookup (ex javascript, css, etc ..)
     */
    public Set<String> getSkipMainModuleTypesDomParsing() {
        return skipMainModuleTypesDomParsing;
    }

    public void setSkipMainModuleTypesDomParsing(Set<String> skipMainModuleTypesDomParsing) {
        this.skipMainModuleTypesDomParsing = skipMainModuleTypesDomParsing;
    }

    /**
     * @return Set of visible Types
     */
    public Set<String> getVisibleTypes() {
        return visibleTypes;
    }

    public void setVisibleTypes(Set<String> visibleTypes) {
        this.visibleTypes = visibleTypes;
    }

    /**
     * @return Set of types to hide
     */
    public Set<String> getNonVisibleTypes() {
        return nonVisibleTypes;
    }

    public void setNonVisibleTypes(Set<String> nonVisibleTypes) {
        this.nonVisibleTypes = nonVisibleTypes;
    }

    /**
     * Excluded node types from creation
     * @return
     */
    public Set<String> getExcludedNodeTypes() {
        return excludedNodeTypes;
    }

    public void setExcludedNodeTypes(Set<String> excludedNodeTypes) {
        this.excludedNodeTypes = excludedNodeTypes;
    }

    public Set<String> getBypassModeForTypes() {
        return bypassModeForTypes;
    }

    public void setBypassModeForTypes(Set<String> bypassModeForTypes) {
        this.bypassModeForTypes = bypassModeForTypes;
    }

    public boolean isForceHeaders() {
        return forceHeaders;
    }

    public void setForceHeaders(boolean forceHeaders) {
        this.forceHeaders = forceHeaders;
    }

    public String getDefaultLocation() {
        return defaultLocation;
    }

    public void setDefaultLocation(String defaultLocation) {
        this.defaultLocation = defaultLocation;
    }

    public boolean isLoadSitesList() {
        return loadSitesList;
    }

    public void setLoadSitesList(boolean loadSitesList) {
        this.loadSitesList = loadSitesList;
    }

    public boolean isUseFullPublicationInfoInMainAreaModules() {
        return useFullPublicationInfoInMainAreaModules;
    }

    public void setUseFullPublicationInfoInMainAreaModules(boolean useFullPublicationInfoInMainAreaModules) {
        this.useFullPublicationInfoInMainAreaModules = useFullPublicationInfoInMainAreaModules;
    }

    public boolean isSupportChannelsDisplay() {
        return supportChannelsDisplay;
    }

    public void setSupportChannelsDisplay(boolean supportChannelsDisplay) {
        this.supportChannelsDisplay = supportChannelsDisplay;
    }
}
