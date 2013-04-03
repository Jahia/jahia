/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.storage.client.Storage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.widget.edit.ToolbarHeader;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.toolbar.action.DeployTemplatesActionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Side panel widget that allows creation of new content using drag and drop from different sources
 * (new content panel, file repository, image repository, page tree, portlets, etc.).
 * User: toto
 * Date: Dec 18, 2009
 * Time: 5:27:33 PM
 */
public class SidePanel extends ContentPanel {
    private final List<SidePanelTabItem> tabs;
    private GWTEditConfiguration config;
    private ToolButton refreshButton;
    private TabPanel tabPanel;

    public SidePanel(GWTEditConfiguration config) {
        super(new FitLayout());
        this.head = new ToolbarHeader();
        tabs = new ArrayList<SidePanelTabItem>();
        initTabs(config);
    }

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
                    storage.setItem(SidePanel.this.config.getName() +"_selectedTab", Integer.toString(tabPanel.getItems().indexOf(tabPanel.getSelectedItem())));
                }
            }
        });

        for (GWTSidePanelTab tabConfig : config.getTabs()) {
            SidePanelTabItem tabItem = tabConfig.getTabItem();
            tabs.add(tabItem);

            tabPanel.add(tabItem.create(tabConfig));
        }

        if (storage != null && storage.getItem(config.getName() +"_selectedTab") != null) {
            int selectedTab = Integer.parseInt(storage.getItem(config.getName() + "_selectedTab"));
            tabPanel.setSelection(tabPanel.getItem(selectedTab));
            tabs.get(selectedTab).restoreState();
        } else {
            tabPanel.setSelection(tabPanel.getItem(0));
        }

        add(tabPanel);
    }

    public void initWithLinker(final EditLinker editLinker) {
        ((ToolbarHeader) head).removeAllTools();
        if (config.getSidePanelToolbar() != null) {
            for (GWTJahiaToolbarItem item : config.getSidePanelToolbar().getGwtToolbarItems()) {
                ((ToolbarHeader)head).addItem(editLinker, item);
            }
        }
        layout();
        refreshButton = new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_ALL, true);
                if (tabs.size() == 1) {
                    tabs.get(0).refresh(data);
                } else {
                    ((SidePanelTabItem) tabPanel.getSelectedItem().getData("tabItem")).refresh(data);
                }
                DeployTemplatesActionItem.refreshAllMenus(editLinker);
                updateRefreshButton();
            }
        });
        refreshButton.setId("JahiaGxtRefreshSidePanelButton");
        head.addTool(refreshButton);

        for (SidePanelTabItem tab : tabs) {
            tab.initWithLinker(editLinker);
        }
    }

    private void updateRefreshButton() {
        boolean needRefresh = ((SidePanelTabItem) tabPanel.getSelectedItem().getData("tabItem")).isNeedManualRefresh();
        if (needRefresh && !refreshButton.getStyleName().contains("x-tool-refresh-red")) {
            refreshButton.setToolTip(Messages.get("label.refresh.modify"));
            refreshButton.removeStyleName("x-tool-refresh");
            refreshButton.addStyleName("x-tool-refresh-red");
            refreshButton.getToolTip().show();
        } else if (!needRefresh && refreshButton.getStyleName().contains("x-tool-refresh-red")) {
            refreshButton.removeStyleName("x-tool-refresh-red");
            refreshButton.addStyleName("x-tool-refresh");
            refreshButton.removeToolTip();
        }
    }

    public void handleNewModuleSelection(Module selectedModule) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewModuleSelection(selectedModule);
        }
        ((ToolbarHeader)head).handleNewModuleSelection(selectedModule);
    }

    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewMainNodeLoaded(node);
        }
        ((ToolbarHeader)head).handleNewMainNodeLoaded(node);
        layout();
    }

    public void handleNewMainSelection(String path) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewMainSelection(path);
        }
    }

    public void markForManualRefresh(Map<String, Object> data) {
        for (SidePanelTabItem tab : tabs) {
            tab.markForManualRefresh(data);
        }
        updateRefreshButton();
    }

    public void refresh(Map<String, Object> data) {
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

    public void setConfig(GWTEditConfiguration config) {
        this.config = config;
        setVisible(!config.getTabs().isEmpty());
        initTabs(config);
    }
}
