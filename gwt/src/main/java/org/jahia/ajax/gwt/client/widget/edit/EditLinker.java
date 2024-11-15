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
package org.jahia.ajax.gwt.client.widget.edit;


import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanel;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Helper object that contains edit mode context information.
 *
 * @author rincevent
 */
public class EditLinker implements Linker {

    private GWTEditConfiguration config;
    private String mainPath;
    private String template;

    private LinkerSelectionContext selectionContext = new LinkerSelectionContext();
    private Module selectedModule;
    private EditModeDNDListener dndListener;
    private ActionToolbarLayoutContainer toolbar;
    private MainModule mainModule;
    private SidePanel sidePanel;
    private ModuleSelectionListener selectionListener;
    private Widget mainAreaComponent;
    private String locale;
    private GWTJahiaChannel activeChannel;
    private String activeChannelVariant = null;
    private boolean isInSettingsPage = true;
    private long lastRefreshInfoDisplayTime;

    /**
     * Initializes an instance of this class.
     *
     * @param mainModule
     *            the current main module
     * @param sidePanel
     *            reference to the side panel
     * @param toolbar
     *            toolbar container object
     * @param config
     *            the edit mode configuration settings
     */
    public EditLinker(MainModule mainModule, SidePanel sidePanel, ActionToolbarLayoutContainer toolbar,
                      GWTEditConfiguration config) {
        if (config.isDragAndDropEnabled()) {
            this.dndListener = new EditModeDNDListener(this);
        }
        this.mainModule = mainModule;
        this.sidePanel = sidePanel;
        this.toolbar = toolbar;
        this.config = config;
        registerLinker();
    }

    /**
     * Returns a side panel object.
     *
     * @return a side panel object
     */
    public SidePanel getSidePanel() {
        return sidePanel;
    }

    /**
     * Sets the site panel reference.
     *
     * @param sidePanel
     *            the site panel reference
     */
    public void setSidePanel(SidePanel sidePanel) {
        this.sidePanel = sidePanel;
        if (sidePanel != null) {
            try {
                sidePanel.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker", e);
            }
        }
    }

    /**
     * Returns main module object.
     *
     * @return main module object
     */
    public MainModule getMainModule() {
        return mainModule;
    }

    /**
     * Returns edit mode configuration settings.
     *
     * @return edit mode configuration settings
     */
    @Override
    public GWTEditConfiguration getConfig() {
        return config;
    }

    /**
     * Switch edit mode configuration
     *
     * @param config
     * @param newPath
     * @param updateSidePanel
     * @param updateToolbar
     */
    public void switchConfig(GWTEditConfiguration config, String newPath, boolean updateSidePanel, boolean updateToolbar, String enforcedWorkspace) {
        this.config = config;

        JahiaGWTParameters.setSiteNode(config.getSiteNode());
        JahiaGWTParameters.setSitesMap(config.getSitesMap());
        JahiaGWTParameters.setChannels(config.getChannels());
        if (enforcedWorkspace != null) {
            JahiaGWTParameters.setWorkspace(enforcedWorkspace);
        }
        PermissionsUtils.loadPermissions(config.getPermissions());

        if (updateSidePanel) {
            setActiveChannel(null);
            this.activeChannelVariant = null;
        }

        mainModule.setConfig(config, newPath);
        if (updateSidePanel) {
            sidePanel.setConfig(config);
        }

        if (updateToolbar) {
            toolbar.setConfig(config);
        }
        EditPanelViewport.getInstance().setViewportStyleName(config.getName());
        registerLinker(true, updateSidePanel, updateToolbar);
    }

    /**
     * Returns edit mode drag&drop listener instance.
     *
     * @return edit mode drag&drop listener instance
     */
    public EditModeDNDListener getDndListener() {
        return dndListener;
    }

    /**
     * Returns current content locale.
     *
     * @return current content locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets current content locale.
     *
     * @param locale
     *            current content locale
     */
    public void setLocale(GWTJahiaLanguage locale) {
        if (locale != null) {
            this.locale = locale.getLanguage();
            JahiaGWTParameters.setLanguage(locale);
        } else {
            this.locale = null;
        }
    }

    /**
     * Returns currently selected module.
     * @return currently selected module
     */
    public Module getSelectedModule() {
        return selectedModule;
    }

    /**
     * Registers the module selection listener.
     *
     * @param selectionListener
     *            the module selection listener
     */
    public void setSelectionListener(ModuleSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    /**
     * Callback for the module selection event.
     *
     * @param selection
     *            the currently selected module
     */
    public void onModuleSelection(Module selection) {
        if (this.selectionListener == null) {
            selectedModule = selection;

            handleNewModuleSelection();
            if (selectedModule != null) {
                selectedModule.setDraggable(true);
            }
        } else {
            selectionListener.onModuleSelection(selection);
        }
    }

    /**
     * Callback for the main module selection.
     *
     * @param mainPath
     *            the main module path
     * @param template
     *            the template
     */
    public void onMainSelection(String mainPath, String template) {
        this.mainPath = mainPath;
        this.selectedModule = null;
        this.template = template;
    }

    /**
     * Performs refresh of the main module and side panel using provided refresh data.
     *
     * @param data
     *            the refresh data
     */
    @Override
    public void refresh(Map<String, Object> data) {

        if (!config.getRefreshOnExternalModification() && Boolean.TRUE.equals(data.get(Linker.EXTERNAL_REFRESH))) {

            if (sidePanel != null) {
                sidePanel.markForManualRefresh(data);
            }

            long time = System.currentTimeMillis();

            InfoConfig infoConfig = new InfoConfig(Messages.get("label.atmosphere.editorial.content.update", "a content has been updated."), Messages.get
                    ("label.refresh.modify", "Click refresh to see updated content"));

            // Check that previously displayed box (if any) has already disappeared.
            if (time - lastRefreshInfoDisplayTime > infoConfig.display) {
                Info.display(infoConfig);
                lastRefreshInfoDisplayTime = time;
            }
        } else {
            mainModule.refresh(data);
            if (sidePanel != null) {
                sidePanel.refresh(data);
            }
        }
    }

    /**
     * Callback for the module selection event.
     */
    public void handleNewModuleSelection() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        toolbar.handleNewLinkerSelection();
        mainModule.handleNewModuleSelection(selectedModule);
        if (sidePanel != null) {
            sidePanel.handleNewModuleSelection(selectedModule);
        }
    }

    /**
     * Callback for the new main module selection event.
     */
    public void handleNewMainSelection() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        mainModule.handleNewMainSelection(mainPath, template);
        mainModule.handleNewModuleSelection(null);
        if (sidePanel != null) {
            sidePanel.handleNewMainSelection(mainPath);
        }
        toolbar.handleNewLinkerSelection();
    }

    /**
     * Callback for the main module node loaded event.
     */
    public void handleNewMainNodeLoaded() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        GWTJahiaNode node = mainModule.getNode();
        toolbar.handleNewMainNodeLoaded(node);
        if (sidePanel != null) {
            sidePanel.handleNewMainNodeLoaded(node);
        }
        Document.get().getBody().setAttribute("data-main-node-displayname", node.getDisplayName());
        Document.get().getBody().setAttribute("data-main-node-path", node.getPath());
        Document.get().getBody().setAttribute("data-sitesettings", Boolean.toString(node.equals(JahiaGWTParameters.getSiteNode())));
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker() {
        registerLinker(true, true, true);
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker(boolean doMainModule, boolean doSidePanel, boolean doToolbar) {
        if (mainModule != null && doMainModule) {
            try {
                mainModule.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker", e);
            }
        }
        if (sidePanel != null && doSidePanel) {
            try {
                sidePanel.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker", e);
            }
        }
        if (toolbar != null && doToolbar) {
            try {
                toolbar.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker", e);
            }
        }
    }


    /**
     * Performs the selection of the specified module.
     *
     * @param o
     *            the module object
     */
    @Override
    public void select(Object o) {
        if (o == null || o instanceof Module) {
            onModuleSelection((Module) o);
        }
    }

    /**
     * Indicates that a processing action is finished and we can unmask the main module area.
     */
    @Override
    public void loaded() {
        mainModule.unmask();
    }

    /**
     * Masks the main module area and adds the specified message into the loading indicator.
     *
     * @param resource
     *            the message text to show in the loading indicator
     */
    @Override
    public void loading(String resource) {
        mainModule.mask(resource, "x-mask-loading");

    }

    /**
     * Callback for the event of data update.
     *
     * @param paths
     *            the updated items paths
     *
     */
    @Override
    public void setSelectPathAfterDataUpdate(List<String> paths) {
        // no implementation so far
    }

    /**
     * Returns the current selection context.
     *
     * @return the current selection context
     */
    @Override
    public LinkerSelectionContext getSelectionContext() {
        return selectionContext;
    }

    /**
     * Performs the synchronization with the current selection as a result of (main) module change or main module node loaded event.
     *
     * @param context
     *            the type of the context; see {@link LinkerSelectionContext}
     */
    @Override
    public void syncSelectionContext(int context) {
        selectionContext.setMainNode(getMainModule().getNode());
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        if (mainModule.isCtrlActive()) {
            nodes.addAll(selectionContext.getSelectedNodes());
        }
        if (getSelectedModule() != null && getSelectedModule().getNode() != null && !(getSelectedModule() instanceof MainModule)) {
            if (!nodes.contains(getSelectedModule().getNode())) {
                nodes.add(getSelectedModule().getNode());
            }
        }
        selectionContext.setSelectedNodes(nodes);
        selectionContext.refresh(context);
        if (selectionContext.getSingleSelection() != null) {
            setSelectionOnBodyAttributes(selectionContext.getSingleSelection());
        }
        Document.get().getBody().setAttribute("data-selection-count", selectionContext.getSelectedNodes() != null ? Integer.toString(selectionContext.getSelectedNodes().size()): "0");
    }

    public static void setSelectionOnBodyAttributes(GWTJahiaNode node) {
        Document.get().getBody().setAttribute("data-singleselection-node-displayname", node.getDisplayName());
        Document.get().getBody().setAttribute("data-singleselection-node-path", node.getPath());
    }

    /**
     * Replaces the content of the main area with the specified widget (usually content edit engine).
     *
     * @param w
     *            the widget to replace the main module area with
     */
    public void replaceMainAreaComponent(Widget w) {
        ContentPanel m;
        if (mainAreaComponent == null) {
            m = (ContentPanel) mainModule.getParent();
            mainModule.saveCurrentFramePosition();
            mainModule.setStyleAttribute("display", "none");
        } else {
            m = (ContentPanel) mainAreaComponent.getParent();
            m.remove(mainAreaComponent);
        }
        mainAreaComponent = w;
        m.insert(mainAreaComponent, 0, new BorderLayoutData(Style.LayoutRegion.CENTER));
        m.layout();
    }

    /**
     * Restores the view of the main module area.
     */
    public void restoreMainArea() {
        // if mainAreaComponent null means area already restored
        if (mainAreaComponent != null) {
            ContentPanel m = (ContentPanel) mainAreaComponent.getParent();
            m.remove(mainAreaComponent);
            mainAreaComponent = null;
            mainModule.setStyleAttribute("display", "block");
            m.layout();
        mainModule.moveToSavePosition();
        }
    }

    /**
     * Indicates if we need to display hidden properties.
     *
     * @return <code>true</code> if teh hidden properties should be shown
     */
    @Override
    public boolean isDisplayHiddenProperties() {
        return false;
    }

    /**
     * Returns an active channel instance.
     *
     * @return an active channel instance
     */
    public GWTJahiaChannel getActiveChannel() {
        return activeChannel;
    }

    /**
     * Sets currently active channel instance.
     *
     * @param activeChannel
     *            currently active channel instance
     */
    public void setActiveChannel(GWTJahiaChannel activeChannel) {
        this.activeChannel = activeChannel;
        if (activeChannel == null || activeChannel.getValue().equals("generic")) {
            Document.get().getBody().removeAttribute("data-channel");
        } else {
            Document.get().getBody().setAttribute("data-channel", activeChannel.getValue());
        }
    }

    /**
     * Returns an identifier of the currently active channel instance if any.
     *
     * @return an identifier of the currently active channel instance if any; <code>null</code> if there is no currently active channel
     */
    public String getActiveChannelIdentifier() {
        return activeChannel != null ? activeChannel.getValue() : null;
    }

    /**
     * Returns active channel variant.
     *
     * @return active channel variant
     */
    public String getActiveChannelVariant() {
        return activeChannelVariant;
    }

    /**
     * Sets active channel variant.
     *
     * @param activeChannelVariant
     *            active channel variant
     */
    public void setActiveChannelVariant(String activeChannelVariant) {
        this.activeChannelVariant = activeChannelVariant;
    }

    public boolean isInSettingsPage() {
        return isInSettingsPage;
    }

    public void setInSettingsPage(boolean inSettingsPage) {
        this.isInSettingsPage = inSettingsPage;
    }

    /**
     * Returns an index of the currently active channel variant.
     *
     * @return an index of the currently active channel variant
     */
    public int getActiveChannelVariantIndex() {
        int result = 0;
        if (getActiveChannel() != null && getActiveChannelVariant() != null) {
            String[] variantValueArray = getActiveChannel().getCapability("variants").split(",");
            for (int i = 0; i < variantValueArray.length; i++) {
                if (variantValueArray[i].equals(getActiveChannelVariant())) {
                    // we found the active variant !
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    public Widget getMainAreaComponent() {
        return mainAreaComponent;
    }
}
