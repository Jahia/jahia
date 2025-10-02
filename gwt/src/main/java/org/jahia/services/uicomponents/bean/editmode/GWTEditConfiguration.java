/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.ajax.gwt.client.data.toolbar.GWTConfiguration;
import org.jahia.ajax.gwt.helper.UIConfigHelper;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.uicomponents.bean.toolbar.Toolbar;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.*;

/**
 * Represent edit mode configuration, including side panel and side panel toolbar,
 * top toolbar, main module toolbar and context menu.
 * Core side of GWTConfiguration. See link between this and GWTConfiguration in {@link UIConfigHelper}
 * @author ktlili
 * Date: Apr 14, 2010
 * Time: 12:26:47 PM
 * @deprecated
 */
public class GWTEditConfiguration extends EditConfiguration implements Serializable, BeanNameAware {

    private static final long serialVersionUID = -5469841056796249203L;

    private List<Toolbar> topToolbars;
    private Toolbar sidePanelToolbar;
    private Toolbar mainModuleToolbar;
    private Toolbar contextMenu;

    private List<SidePanelTab> tabs;

    private Map<String, EngineConfiguration> engineConfigurations;

    private String sitesLocation;
    private String forcedSite;

    private GWTConfiguration.DragAndDropBehavior dragAndDropBehavior;
    private boolean refreshOnExternalModification = true;
    private boolean refreshEnabled = true;
    private int createChildrenDirectButtonsLimit;

    private List<String> componentsPaths = Collections.singletonList("$site/components/*");

    private Set<String> excludedNodeTypes;

    private boolean loadSitesList = true;

    private boolean useFullPublicationInfoInMainAreaModules;

    private boolean supportChannelsDisplay = true;

    private boolean needFrameParsing = true;

    private boolean eventDispatchingEnabled;

    public void addTab(SidePanelTab tab) {
        tabs.add(tab);
    }

    public void addTab(int index, SidePanelTab tab) {
        tabs.add(index, tab);
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Toolbar getTopToolbar() {
        return (topToolbars != null && topToolbars.size() > 0) ? topToolbars.get(0) : null;
    }

    public void setTopToolbar(Toolbar topToolbar) {
        this.topToolbars = Collections.singletonList(topToolbar);
    }

    public List<Toolbar> getTopToolbars() {
        return topToolbars;
    }

    public void setTopToolbars(List<Toolbar> topToolbars) {
        this.topToolbars = topToolbars;
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

    public GWTConfiguration.DragAndDropBehavior getDragAndDropBehavior() {
        return dragAndDropBehavior;
    }

    public void setDragAndDropBehavior(GWTConfiguration.DragAndDropBehavior dragAndDropBehavior) {
        this.dragAndDropBehavior = dragAndDropBehavior;
    }

    // Setter for backward compatibility
    public void setEnableDragAndDrop(boolean enableDragAndDrop) {
        setDragAndDropBehavior(enableDragAndDrop ? org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration.DragAndDropBehavior.ENABLED : org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration.DragAndDropBehavior.DISABLED);
    }

    public boolean getRefreshOnExternalModification() {
        return refreshOnExternalModification;
    }

    public void setRefreshOnExternalModification(boolean refreshOnExternalModification) {
        this.refreshOnExternalModification = refreshOnExternalModification;
    }

    public int getCreateChildrenDirectButtonsLimit() {
        return createChildrenDirectButtonsLimit;
    }

    public void setCreateChildrenDirectButtonsLimit(int createChildrenDirectButtonsLimit) {
        this.createChildrenDirectButtonsLimit = createChildrenDirectButtonsLimit;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public void setNodeCheckPermission(String nodeCheckPermission) {
        this.nodeCheckPermission = nodeCheckPermission;
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

    public void setEditableTypes(Set<String> editableTypes) {
        this.editableTypes = editableTypes;
    }

    public void setNonEditableTypes(Set<String> nonEditableTypes) {
        this.nonEditableTypes = nonEditableTypes;
    }

    public void setSkipMainModuleTypesDomParsing(Set<String> skipMainModuleTypesDomParsing) {
        this.skipMainModuleTypesDomParsing = skipMainModuleTypesDomParsing;
    }

    public void setVisibleTypes(Set<String> visibleTypes) {
        this.visibleTypes = visibleTypes;
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

    public void setBypassModeForTypes(Set<String> bypassModeForTypes) {
        this.bypassModeForTypes = bypassModeForTypes;
    }

    public void setForceHeaders(boolean forceHeaders) {
        this.forceHeaders = forceHeaders;
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
        return ChannelService.getInstance().getAllChannels().size() > 1 && supportChannelsDisplay;
    }

    public void setSupportChannelsDisplay(boolean supportChannelsDisplay) {
        this.supportChannelsDisplay = supportChannelsDisplay;
    }

    public boolean isNeedFrameParsing() {
        return needFrameParsing;
    }

    public void setNeedFrameParsing(boolean needFrameParsing) {
        this.needFrameParsing = needFrameParsing;
    }

    public boolean isRefreshEnabled() {
        return refreshEnabled;
    }

    public void setRefreshEnabled(boolean refreshEnabled) {
        this.refreshEnabled = refreshEnabled;
    }

    /**
     * Indicates if the events (using Atmosphere framework) should be dispatched to consumers, registered via JavaScript callbacks.
     *
     * @return <code>true</code> if the events should be dispatched; <code>false</code> if not
     */
    public boolean isEventDispatchingEnabled() {
        return eventDispatchingEnabled;
    }

    /**
     * Should the events (using Atmosphere framework) be dispatched to consumers, registered via JavaScript callbacks or not.
     *
     * @param dispatchEvents <code>true</code> enables event dispatching; <code>false</code> disables it
     */
    public void setEventDispatchingEnabled(boolean dispatchEvents) {
        this.eventDispatchingEnabled = dispatchEvents;
    }
}
