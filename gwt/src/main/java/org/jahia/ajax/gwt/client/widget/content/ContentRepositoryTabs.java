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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTRepository;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private ListView<GWTJahiaNode> queryList;
    private boolean tabExpanded;

    /**
     * Constructor (UI)
     *
     * @param config the configuration to use (generated in ManagerConfigurationFactory)
     */
    public ContentRepositoryTabs(GWTManagerConfiguration config, final List<String> selectedPaths) {
        m_component = new TabPanel();
        m_component.setBodyBorder(false);
        m_component.setBorders(false);

        browseTabITem = new TabItem(Messages.get("browse.label", "Browse"));
        browseTabITem.setId("CRTbrowseTabItem");
        searchTabITem = new TabItem(Messages.get("label.search", "Search"));
        searchTabITem.setId("CRTsearchTabItem");

        ChangeAccordionListener<ComponentEvent> accordionListener = new ChangeAccordionListener<ComponentEvent>();
        // init main panel and add accordions
        browseComponent = new LayoutContainer(new AccordionLayout());
        browseComponent.setScrollMode(Style.Scroll.NONE);
        browseComponent.setBorders(true);
        for (GWTRepository repo : config.getRepositories()) {
            final RepositoryTab tab = new RepositoryTab(this, repo, selectedPaths, config);
            repositories.add(tab);
            browseComponent.add(tab);
            // if no configured tab to open, open tab first tab
            if ((config.getSelectedAccordion() != null && tab.getRepository().getKey().equals(config.getSelectedAccordion())) ||
                    (config.getSelectedAccordion() == null && repositories.size() == 1)) {
                tab.expand();
            }
            tab.addListener(Events.Expand, accordionListener);
            tab.getHeader().addListener(Events.OnClick, new Listener<BaseEvent>() {
                @Override
                public void handleEvent(BaseEvent be) {
                    if (!tab.isExpanded()) {
                        tabExpanded = true;
                        tab.refresh(null);
                    }
                }
            });
        }
        ////////////////////////////
        // SEARCH PANEL ACCORDION //
        ////////////////////////////
        savedSearchPanel = new ContentPanel(new FitLayout());
        savedSearchPanel.setBodyBorder(false);
        savedSearchPanel.setBorders(false);
        savedSearchPanel.setScrollMode(Style.Scroll.NONE);
        savedSearchPanel.setHeadingHtml(Messages.get("repository.savedSearch.label"));
        savedSearchPanel.getHeader().setIcon(StandardIconsProvider.STANDARD_ICONS.savedSearch());
        savedSearchPanel.getHeader().setBorders(false);

        queryList = new ListView<GWTJahiaNode>();
        queryList.setBorders(false);
        queryList.setHeight("92%");
        queryList.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        queryList.setStore(new ListStore<GWTJahiaNode>());
        queryList.setDisplayProperty("displayName");
        savedSearchPanel.add(queryList, new VBoxLayoutData(new Margins(0, 0, 5, 0)));
        queryList.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                getLinker().getTopRightObject().setContent(queryList.getSelectionModel().getSelectedItem());
            }
        });
        Menu queryMenu = new Menu();
        final MenuItem removeQuery = new MenuItem(Messages.get("label.remove"), new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent event) {
                final GWTJahiaNode item = queryList.getSelectionModel().getSelectedItem();
                if (item != null) {
                    List<String> queryNode = new ArrayList<String>();
                    queryNode.add(item.getPath());
                    service.deletePaths(queryNode, new BaseAsyncCallback<GWTJahiaNode>() {

                        @Override
                        public void onApplicationFailure(Throwable throwable) {
                            Window.alert("Query deletion failed\n\n" + throwable.getLocalizedMessage());
                        }

                        @Override
                        public void onSuccess(GWTJahiaNode o) {
                            queryList.getStore().remove(item);
                            retrieveSavedSearch();
                        }
                    });
                }
            }
        });

        final MenuItem renameQuery = new MenuItem(Messages.get("label.rename"), new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent event) {
                final GWTJahiaNode item = queryList.getSelectionModel().getSelectedItem();
                if (item != null) {
                    renameSearch(item);
                }
            }
        });

        queryMenu.addListener(Events.BeforeShow, new Listener<MenuEvent>() {

            @Override
            public void handleEvent(MenuEvent baseEvent) {
                removeQuery.setEnabled(queryList.getSelectionModel().getSelectedItem() != null);
            }
        });
        queryMenu.add(removeQuery);
        queryMenu.add(renameQuery);
        queryList.setContextMenu(queryMenu);
        savedSearchPanel.getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent event) {
                retrieveSavedSearch();
            }
        }));

        browseComponent.add(savedSearchPanel);
        browseTabITem.setLayout(new FitLayout());
        browseTabITem.add(browseComponent);

        m_component.add(browseTabITem);
        contentSearchForm = new ContentSearchForm(config);
        searchTabITem.setLayout(new FitLayout());
        searchTabITem.setIcon(ToolbarIconProvider.getInstance().getIcon("search"));
        searchTabITem.add(contentSearchForm);
        m_component.add(searchTabITem);

        savedSearchPanel.addListener(Events.Expand, accordionListener);

        m_component.addListener(Events.ContextMenu, new Listener<ComponentEvent>() {

            @Override
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
                service.rename(selection.getPath(), newName, new BaseAsyncCallback<GWTJahiaNode>() {

                    @Override
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert("Rename failed\n\n" + throwable.getLocalizedMessage());
                    }

                    @Override
                    public void onSuccess(GWTJahiaNode o) {
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

        @Override
        public void handleEvent(T t) {
            if (savedSearchPanel.isExpanded()) {
                retrieveSavedSearch();
            }
            getLinker().onTreeItemSelected();
        }
    }

    @Override
    public void initWithLinker(ManagerLinker linker) {
        super.initWithLinker(linker);
        for (RepositoryTab tab : repositories) {
            tab.init();
        }
        contentSearchForm.initWithLinker(linker);
    }

    @Override
    public void openAndSelectItem(Object item) {
        if (item != null) {
            for (RepositoryTab tab : repositories) {
                tab.openAndSelectItem(item);
            }
        }
    }

    @Override
    public void refresh(Map<String, Object> data) {
        for (RepositoryTab tab : repositories) {
            tab.refresh(data);
        }
    }

    private void retrieveSavedSearch() {
        queryList.getStore().removeAll();
        service.getSavedSearch(new BaseAsyncCallback<List<GWTJahiaNode>>() {

            @Override
            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                queryList.getStore().add(gwtJahiaNodes);
            }
        });
    }

    @Override
    public Object getSelectedItem() {
        if (savedSearchPanel.isExpanded()) {
            return queryList.getSelectionModel().getSelectedItem();
        } else {
            for (RepositoryTab tab : repositories) {
                if (tab.isExpanded()) {
                    return tab.getSelectedItem();
                }
            }
        }
        return null;
    }

    /**
     * Expand the selected tab if no tab already expanded
     * @param tab
     */
    public void expandTab(RepositoryTab tab) {
        if (!tabExpanded) {
            tab.expand();
            tabExpanded = true;
        }
    }

    @Override
    public Component getComponent() {
        return m_component;
    }
}
