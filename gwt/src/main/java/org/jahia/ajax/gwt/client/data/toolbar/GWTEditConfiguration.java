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
package org.jahia.ajax.gwt.client.data.toolbar;

import java.io.Serializable;
import java.util.List;

/**
 *
 * User: toto
 * Date: Mar 29, 2010
 * Time: 4:38:09 PM
 *
 */
public class GWTEditConfiguration extends GWTConfiguration implements Serializable {

    private List<GWTJahiaToolbar> topToolbars;
    private GWTJahiaToolbar contextMenu;
    private GWTJahiaToolbar sidePanelToolbar;
    private GWTJahiaToolbar mainModuleToolbar;
    private DragAndDropBehavior dragAndDropBehavior;
    private String defaultUrlMapping;
    private String defaultLocation;
    private boolean useFullPublicationInfoInMainAreaModules;
    private boolean supportChannelsDisplay;
    private boolean refreshOnExternalModification = true;
    private boolean needFrameParsing = true;
    private boolean refreshEnabled = true;
    private int createChildrenDirectButtonsLimit;

    private List<GWTSidePanelTab> tabs;

    private boolean eventDispatchingEnabled;

    public GWTEditConfiguration() {
    }

    public GWTJahiaToolbar getTopToolbar() {
        return (topToolbars != null && topToolbars.size() > 0) ? topToolbars.get(0) : null;
    }

    public List<GWTJahiaToolbar> getTopToolbars() {
        return topToolbars;
    }

    public void setTopToolbars(List<GWTJahiaToolbar> topToolbars) {
        this.topToolbars = topToolbars;
    }

    public GWTJahiaToolbar getSidePanelToolbar() {
        return sidePanelToolbar;
    }

    public void setSidePanelToolbar(GWTJahiaToolbar sidePanelToolbar) {
        this.sidePanelToolbar = sidePanelToolbar;
    }

    public GWTJahiaToolbar getMainModuleToolbar() {
        return mainModuleToolbar;
    }

    public void setMainModuleToolbar(GWTJahiaToolbar mainModuleToolbar) {
        this.mainModuleToolbar = mainModuleToolbar;
    }

    public GWTJahiaToolbar getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(GWTJahiaToolbar contextMenu) {
        this.contextMenu = contextMenu;
    }

    public List<GWTSidePanelTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<GWTSidePanelTab> tabs) {
        this.tabs = tabs;
    }

    public boolean isDragAndDropEnabled() {
        return (dragAndDropBehavior != DragAndDropBehavior.DISABLED);
    }

    public DragAndDropBehavior getDragAndDropBehavior() {
        return dragAndDropBehavior;
    }

    public void setDragAndDropBehavior(DragAndDropBehavior dragAndDropBehavior) {
        this.dragAndDropBehavior = dragAndDropBehavior;
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

    public String getDefaultUrlMapping() {
        return defaultUrlMapping;
    }

    public void setDefaultUrlMapping(String defaultUrlMapping) {
        this.defaultUrlMapping = defaultUrlMapping;
    }

    public String getDefaultLocation() {
        return defaultLocation;
    }

    public void setDefaultLocation(String defaultLocation) {
        this.defaultLocation = defaultLocation;
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
