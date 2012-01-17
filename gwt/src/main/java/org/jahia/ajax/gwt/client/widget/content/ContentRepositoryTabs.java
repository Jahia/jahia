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

package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTRepository;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.google.gwt.user.client.Window;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:57:08
 */
public class ContentRepositoryTabs extends LeftComponent {

    // common declarations
    private TabPanel m_component;
    private TabItem browseTabITem;
    private TabItem searchTabITem;

    private LayoutContainer browseComponent;
    private ContentSearchForm contentSearchForm;
    private JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

    // repositories
    private List<RepositoryTab> repositories = new ArrayList<RepositoryTab>();

    // my search
    private ContentPanel savedSearchPanel;
    private DataList queryList;
    private GWTManagerConfiguration config;

    /**
     * Constructor (UI)
     *
     * @param config the configuration to use (generated in ManagerConfigurationFactory)
     */
    public ContentRepositoryTabs(GWTManagerConfiguration config, final List<String> selectedPaths) {
        this.config = config;
        m_component = new TabPanel();
        m_component.setBodyBorder(false);
        m_component.setBorders(false);

        browseTabITem = new TabItem(Messages.get("browse.label", "Browse"));
        searchTabITem = new TabItem(Messages.get("label.search", "Search"));

        for (GWTRepository repo : config.getRepositories()) {
            repositories.add(new RepositoryTab(this, repo, selectedPaths, config));
        }

        ////////////////////////////
        // SEARCH PANEL ACCORDION //
        ////////////////////////////
        savedSearchPanel = new ContentPanel(new FitLayout());
        savedSearchPanel.setBodyBorder(false);
        savedSearchPanel.setBorders(false);
        savedSearchPanel.setScrollMode(Style.Scroll.NONE);
        savedSearchPanel.setHeading(Messages.get("repository.savedSearch.label"));
        savedSearchPanel.getHeader().setIcon(StandardIconsProvider.STANDARD_ICONS.savedSearch());
        savedSearchPanel.getHeader().setBorders(false);

        queryList = new DataList();
        queryList.setBorders(false);
        queryList.setFlatStyle(true);
        queryList.setScrollMode(Style.Scroll.AUTO);
        queryList.setSelectionMode(Style.SelectionMode.SINGLE);
        savedSearchPanel.add(queryList);
        queryList.addListener(Events.SelectionChange, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (queryList.getSelectedItems().size() == 1) {
                    getLinker().getTopRightObject().setContent(queryList.getSelectedItem().getData("query"));
                }
            }
        });
        Menu queryMenu = new Menu();
        final MenuItem removeQuery = new MenuItem(Messages.get("label.remove"), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent event) {
                final DataListItem item = queryList.getSelectedItem();
                if (item != null) {
                    List<String> queryNode = new ArrayList<String>();
                    queryNode.add(((GWTJahiaNode) item.getData("query")).getPath());
                    service.deletePaths(queryNode, new BaseAsyncCallback() {
                        public void onApplicationFailure(Throwable throwable) {
                            Window.alert("Query deletion failed\n\n" + throwable.getLocalizedMessage());
                        }

                        public void onSuccess(Object o) {
                            queryList.remove(item);
                            retrieveSavedSearch();
                        }
                    });
                }
            }
        });

        final MenuItem renameQuery = new MenuItem(Messages.get("label.rename"), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent event) {
                final DataListItem item = queryList.getSelectedItem();
                if (item != null) {
                    renameSearch(((GWTJahiaNode) item.getData("query")));
                }
            }
        });

        queryMenu.addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                removeQuery.setEnabled(queryList.getSelectedItem() != null);
            }
        });
        queryMenu.add(removeQuery);
        queryMenu.add(renameQuery);
        queryList.setContextMenu(queryMenu);
        savedSearchPanel.getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                retrieveSavedSearch();
            }
        }));

        // init main panel and add accordions
        ChangeAccordionListener<ComponentEvent> accordionListener = new ChangeAccordionListener<ComponentEvent>();
        browseComponent = new LayoutContainer(new AccordionLayout());
        browseComponent.setScrollMode(Style.Scroll.NONE);
        browseComponent.setBorders(true);
        for (RepositoryTab tab : repositories) {
            browseComponent.add(tab);
            if (tab.getRepository().getKey().equals(config.getSelectedAccordion())) {
                tab.setExpanded(true);
            }
            tab.addListener(Events.Expand, accordionListener);

        }
        browseComponent.add(savedSearchPanel);
        browseTabITem.setLayout(new FitLayout());
        browseTabITem.add(browseComponent);

        m_component.add(browseTabITem);

        createSearchPanel();
        searchTabITem.setLayout(new FitLayout());
        searchTabITem.setIcon(ToolbarIconProvider.getInstance().getIcon("search"));
        searchTabITem.add(contentSearchForm);
        m_component.add(searchTabITem);

        savedSearchPanel.addListener(Events.Expand, accordionListener);

        m_component.addListener(Events.ContextMenu, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                getLinker().getSelectionContext().refresh(LinkerSelectionContext.MAIN_NODE_ONLY);
            }
        });
    }

    /**
     * Rename Search
     *
     * @param selection
     */
    private void renameSearch(GWTJahiaNode selection) {
        if (selection != null) {
            if (selection.isLocked()) {
                Window.alert(selection.getName() + " is locked");
                return;
            }
            String newName = Window.prompt("Enter the new name for " + (selection.isFile().booleanValue() ? "file " : "folder ") + selection.getName(), selection.getName());
            if (newName != null && newName.length() > 0 && !newName.equals(selection.getName())) {
                service.rename(selection.getPath(), newName, new BaseAsyncCallback<String>() {
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert("Rename failed\n\n" + throwable.getLocalizedMessage());
                    }

                    public void onSuccess(String o) {
                        retrieveSavedSearch();
                    }
                });
            }
        }
    }


    /**
     * ChangeAccordionListener
     *
     * @param <T>
     */
    private class ChangeAccordionListener<T extends ComponentEvent> implements Listener<T> {
        public void handleEvent(T t) {
            if (savedSearchPanel.isExpanded()) {
                retrieveSavedSearch();
            }
            getLinker().onTreeItemSelected();
        }
    }

    public void initWithLinker(ManagerLinker linker) {
        super.initWithLinker(linker);
        for (RepositoryTab tab : repositories) {
            tab.init();
        }
        retrieveSavedSearch();
        contentSearchForm.initWithLinker(linker);
    }

    public void openAndSelectItem(Object item) {
        if (item != null) {
            for (RepositoryTab tab : repositories) {
                tab.openAndSelectItem(item);
            }
        }
    }

    public void refresh(int flag) {
        for (RepositoryTab tab : repositories) {
            tab.refresh(flag);
        }
    }

    private void retrieveSavedSearch() {
        queryList.removeAll();
        service.getSavedSearch(new BaseAsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                for (GWTJahiaNode query : gwtJahiaNodes) {
                    addSavedSearch(query, false);
                }
                if (savedSearchPanel.isExpanded()) {
                    getLinker().onTreeItemSelected();
                }
            }
        });
    }

    public Object getSelectedItem() {
        if (savedSearchPanel.isExpanded()) {
            if (queryList.getSelectedItems().size() == 1) {
                return queryList.getSelectedItem().getData("query");
            } else {
                return null;
            }
        } else {
            for (RepositoryTab tab : repositories) {
                if (tab.isExpanded()) {
                    return tab.getSelectedItem();
                }
            }
        }
        return null;
    }

    public Component getComponent() {
        return m_component;
    }

    public void addSavedSearch(GWTJahiaNode query, boolean expandSearchPanel) {
        DataListItem queryItem = new DataListItem();
        queryItem.setData("query", query);
        queryItem.setText(query.getName());
        queryList.add(queryItem);
        if (expandSearchPanel) {
            savedSearchPanel.setExpanded(true);
        }
    }

    public void createSearchPanel(){
      contentSearchForm = new ContentSearchForm(config);
    }

}