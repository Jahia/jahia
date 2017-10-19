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
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.storage.client.Storage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.ToolbarHeader;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.toolbar.action.SiteSwitcherActionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Side panel widget that allows creation of new content using drag and drop from different sources
 * (new content panel, file repository, image repository, page tree, portlets, etc.).
 *
 * @author toto
 */
public class SidePanel extends ContentPanel {
    private final List<SidePanelTabItem> tabs;
    private GWTEditConfiguration config;
    private ToolButton refreshButton;
    private TabPanel tabPanel;

    /**
     * Initializes an instance of this class.
     *
     * @param config the edit mode configuration settings
     */
    public SidePanel(GWTEditConfiguration config) {
        super(new FitLayout());
        this.head = new ToolbarHeader();
        tabs = new ArrayList<SidePanelTabItem>();
        initTabs(config);
    }

    /**
     * Initializes the tabs based on the edit mode settings
     *
     * @param config the edit mode configuration settings
     */
    public void initTabs(GWTEditConfiguration config) {
        removeAll();

        tabs.clear();

        this.config = config;

        final Storage storage = Storage.getSessionStorageIfSupported();
        tabPanel = new TabPanel();
        tabPanel.setBorders(false);
        tabPanel.setBodyBorder(false);

        // this id is for the container, each tab has its own ID concatenated with this one
        tabPanel.setId("JahiaGxtSidePanelTabs");

        tabPanel.addListener(Events.Select, new Listener<TabPanelEvent>() {
            public void handleEvent(TabPanelEvent be) {
                SidePanelTabItem selectedTab = ((SidePanelTabItem) tabPanel.getSelectedItem().getData("tabItem"));
                selectedTab.refresh(selectedTab.getAutoRefreshData());
                updateRefreshButton();
                if (storage != null) {
                    storage.setItem(SidePanel.this.config.getName() + "_selectedTab", Integer.toString(tabPanel.getItems().indexOf(tabPanel.getSelectedItem())));
                }
            }
        });
        int selectedTab = 0;
        if (storage != null && storage.getItem(config.getName() + "_selectedTab") != null) {
            selectedTab = Integer.parseInt(storage.getItem(config.getName() + "_selectedTab"));
        }

        for (GWTSidePanelTab tabConfig : config.getTabs()) {
            SidePanelTabItem tabItem = tabConfig.getTabItem();
            tabs.add(tabItem);

            tabPanel.add(tabItem.create(tabConfig));
        }
        tabPanel.setSelection(tabPanel.getItem(selectedTab));
        add(tabPanel);
    }

    /**
     * Performs tabs initialization using edit linker object.
     *
     * @param editLinker edit linker object
     */
    public void initWithLinker(final EditLinker editLinker) {
        Component collapse = null;
        if (head.getTools().size() == 1) {
            HorizontalPanel hp = ((HorizontalPanel) head.getTools().get(0));
            collapse = hp.getItem(hp.getItemCount() - 1);
        }
        ((ToolbarHeader) head).removeAllTools();
        if (config.getSidePanelToolbar() != null) {
            for (GWTJahiaToolbarItem item : config.getSidePanelToolbar().getGwtToolbarItems()) {
                ((ToolbarHeader) head).addItem(editLinker, item);
            }
        }
        refreshButton = new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_ALL, true);
                if (tabs.size() == 1) {
                    tabs.get(0).refresh(data);
                } else {
                    ((SidePanelTabItem) tabPanel.getSelectedItem().getData("tabItem")).refresh(data);
                }
                updateRefreshButton();
                SiteSwitcherActionItem.reloadAndRefreshAllSitesList(config.getSitesLocation(), editLinker);
            }
        });
        refreshButton.setId("JahiaGxtRefreshSidePanelButton");
        head.addTool(refreshButton);
        if (collapse != null) {
            head.addTool(collapse);
        }
        ((ToolbarHeader) head).attachTools();

        for (SidePanelTabItem tab : tabs) {
            tab.initWithLinker(editLinker);
        }

        layout();
    }

    public void enable() {
        for (SidePanelTabItem tab : tabs) {
                tab.enable();
        }
    }

    public void disable() {
        for (SidePanelTabItem tab : tabs) {
            tab.disable();
        }
    }


    private void updateRefreshButton() {
        if (tabPanel.getSelectedItem() != null) {
            boolean needRefresh = ((SidePanelTabItem) tabPanel.getSelectedItem().getData("tabItem")).isNeedManualRefresh();
            if (needRefresh && !refreshButton.getStyleName().contains("x-tool-refresh-red")) {
                refreshButton.setToolTip(Messages.get("label.refresh.modify"));
                refreshButton.removeStyleName("x-tool-refresh");
                refreshButton.addStyleName("x-tool-refresh-red");
            } else if (!needRefresh && refreshButton.getStyleName().contains("x-tool-refresh-red")) {
                refreshButton.removeStyleName("x-tool-refresh-red");
                refreshButton.addStyleName("x-tool-refresh");
                refreshButton.removeToolTip();
            }
        }
    }

    /**
     * Callback for module selection event.
     *
     * @param selectedModule the selected module
     */
    public void handleNewModuleSelection(Module selectedModule) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewModuleSelection(selectedModule);
        }
        ((ToolbarHeader) head).handleNewLinkerSelection();
    }

    /**
     * Callback for the main module loaded event
     *
     * @param node the main module node
     */
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewMainNodeLoaded(node);
        }
        ((ToolbarHeader) head).handleNewMainNodeLoaded(node);
        layout();
    }

    /**
     * Callback for the selection of a main module.
     *
     * @param path the path of the new main module node
     */
    public void handleNewMainSelection(String path) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewMainSelection(path);
        }
        ((ToolbarHeader) head).handleNewLinkerSelection();
    }

    /**
     * Specifies the items to be refreshed manually.
     *
     * @param data the refresh data
     */
    public void markForManualRefresh(Map<String, Object> data) {
        for (SidePanelTabItem tab : tabs) {
            tab.markForManualRefresh(data);
        }
        updateRefreshButton();
    }

    /**
     * Performs the refresh of the side panel tabs, based on the provided data.
     *
     * @param data the refresh data
     */
    public void refresh(Map<String, Object> data) {
        if (tabPanel.getSelectedItem() != null) {
            SidePanelTabItem selected = ((SidePanelTabItem) tabPanel.getSelectedItem().getData("tabItem"));

            for (SidePanelTabItem tab : tabs) {
                if (tab == selected) {
                    tab.refresh(data);
                } else {
                    tab.markForAutoRefresh(data);
                }
            }
            updateRefreshButton();
        }
    }

    /**
     * Sets the edit mode configuration settings.
     *
     * @param config the edit mode configuration settings
     */
    public void setConfig(GWTEditConfiguration config) {
        this.config = config;
        setVisible(!config.getTabs().isEmpty());
        tabPanel.unmask();
        initTabs(config);
    }
}
