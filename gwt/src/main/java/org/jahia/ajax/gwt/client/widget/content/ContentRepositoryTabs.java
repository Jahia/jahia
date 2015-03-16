/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
        savedSearchPanel.setHeadingHtml(Messages.get("repository.savedSearch.label"));
        savedSearchPanel.getHeader().setIcon(StandardIconsProvider.STANDARD_ICONS.savedSearch());
        savedSearchPanel.getHeader().setBorders(false);

        queryList = new ListView<GWTJahiaNode>();
        queryList.setBorders(false);
        queryList.setHeight("92%");
        queryList.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        queryList.setStore(new ListStore<GWTJahiaNode>());
        queryList.setDisplayProperty("displayName");
        savedSearchPanel.add(queryList,new VBoxLayoutData(new Margins(0, 0, 5, 0)));
        queryList.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                getLinker().getTopRightObject().setContent(queryList.getSelectionModel().getSelectedItem());
            }
        });
        Menu queryMenu = new Menu();
        final MenuItem removeQuery = new MenuItem(Messages.get("label.remove"), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent event) {
                final GWTJahiaNode item = queryList.getSelectionModel().getSelectedItem();
                if (item != null) {
                    List<String> queryNode = new ArrayList<String>();
                    queryNode.add(item.getPath());
                    service.deletePaths(queryNode, new BaseAsyncCallback() {
                        public void onApplicationFailure(Throwable throwable) {
                            Window.alert("Query deletion failed\n\n" + throwable.getLocalizedMessage());
                        }

                        public void onSuccess(Object o) {
                            queryList.getStore().remove(item);
                            retrieveSavedSearch();
                        }
                    });
                }
            }
        });

        final MenuItem renameQuery = new MenuItem(Messages.get("label.rename"), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent event) {
                final GWTJahiaNode item = queryList.getSelectionModel().getSelectedItem();
                if (item != null) {
                    renameSearch(item);
                }
            }
        });

        queryMenu.addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                removeQuery.setEnabled(queryList.getSelectionModel().getSelectedItem() != null);
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
        for (final RepositoryTab tab : repositories) {
            browseComponent.add(tab);
            if (tab.getRepository().getKey().equals(config.getSelectedAccordion())) {
                tab.setExpanded(true);
            }
            tab.addListener(Events.Expand, accordionListener);
            tab.getHeader().addListener(Events.OnClick, new Listener<BaseEvent>() {
                @Override
                public void handleEvent(BaseEvent be) {
                    if (!tab.isExpanded()) {
                        tab.refresh(null);
                    }
                }
            });
        }
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
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert("Rename failed\n\n" + throwable.getLocalizedMessage());
                    }

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
        contentSearchForm.initWithLinker(linker);
    }

    public void openAndSelectItem(Object item) {
        if (item != null) {
            for (RepositoryTab tab : repositories) {
                tab.openAndSelectItem(item);
            }
        }
    }

    public void refresh(Map<String, Object> data) {
        for (RepositoryTab tab : repositories) {
            tab.refresh(data);
        }
    }

    private void retrieveSavedSearch() {
        queryList.getStore().removeAll();
        service.getSavedSearch(new BaseAsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                queryList.getStore().add(gwtJahiaNodes);
            }
        });
    }

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

    public Component getComponent() {
        return m_component;
    }
}