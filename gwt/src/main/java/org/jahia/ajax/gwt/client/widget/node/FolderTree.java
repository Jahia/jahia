/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.node;

import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowCloseListener;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:57:08
 */
public class FolderTree extends LeftComponent {

    // common declarations
    private ContentPanel m_component ;
    private JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance() ;

    // repositories
    private List<RepositoryTab> repositories = new ArrayList<RepositoryTab>() ;

    // my search
    private ContentPanel searchPanel ;
    private DataList queryList ;

    /**
     * Constructor (UI)
     * @param config the configuration to use (generated in ManagerConfigurationFactory)
     */
    public FolderTree(ManagerConfiguration config) {

        for (String repoId: config.getAccordionPanels()) {
            repositories.add(new RepositoryTab(this, service, repoId, Messages.getResource("fm_repository_" + repoId), config)) ;
        }

        ////////////////////////////
        // SEARCH PANEL ACCORDION //
        ////////////////////////////
        searchPanel = new ContentPanel(new FitLayout()) ;
        searchPanel.setBodyBorder(false);
        searchPanel.setBorders(false);
        searchPanel.setScrollMode(Style.Scroll.NONE);
        searchPanel.setHeading(Messages.getResource("fm_repository_savedSearch")) ;
        searchPanel.getHeader().setIconStyle("fm-savedSearch");
        searchPanel.getHeader().setBorders(false);
        queryList = new DataList() ;
        queryList.setBorders(false);
        queryList.setFlatStyle(true);
        queryList.setScrollMode(Style.Scroll.AUTO);
        queryList.setSelectionMode(Style.SelectionMode.SINGLE);
        searchPanel.add(queryList) ;
        queryList.addListener(Events.SelectionChange, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (queryList.getSelectedItems().size() == 1) {
                    getLinker().getTopRightObject().setContent(queryList.getSelectedItem().getData("query"));
                }
            }
        });
        Menu queryMenu = new Menu() ;
        final MenuItem removeQuery = new MenuItem(Messages.getResource("fm_remove"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                final DataListItem item = queryList.getSelectedItem() ;
                if (item != null) {
                    List<String> queryNode = new ArrayList<String>() ;
                    queryNode.add(((GWTJahiaNode) item.getData("query")).getPath()) ;
                    service.deletePaths(queryNode, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Window.alert("Query deletion failed\n\n" + throwable.getLocalizedMessage()) ;
                        }
                        public void onSuccess(Object o) {
                            queryList.remove(item) ;
                            retrieveSavedSearch();
                        }
                    });
                }
            }
        }) ;
        removeQuery.setIconStyle("fm_remove");

        final MenuItem renameQuery = new MenuItem(Messages.getResource("fm_rename"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                final DataListItem item = queryList.getSelectedItem() ;
                if (item != null) {
                    renameSearch(((GWTJahiaNode) item.getData("query")));
                }
            }
        }) ;
        removeQuery.setIconStyle("fm_rename");

        queryMenu.addListener(Events.BeforeShow, new Listener() {
            public void handleEvent(BaseEvent baseEvent) {
                removeQuery.setEnabled(queryList.getSelectedItem() != null) ;
            }
        }) ;
        queryMenu.add(removeQuery) ;
        queryMenu.add(renameQuery) ;
        queryList.setContextMenu(queryMenu);
        searchPanel.getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                retrieveSavedSearch() ;
            }
        }));

        // init main panel and add accordions
        ChangeAccordionListener accordionListener =  new ChangeAccordionListener<ComponentEvent>() ;
        m_component = new ContentPanel(new AccordionLayout()) ;
        m_component.setScrollMode(Style.Scroll.AUTO);
        m_component.getHeader().setBorders(false);
        m_component.setBodyBorder(true);
        m_component.setBorders(true);
        for (RepositoryTab tab: repositories) {
            m_component.add(tab) ;
            if (tab.getRepositoryType().equals(config.getSelectedAccordion())) {
                tab.setExpanded(true);
            }
            tab.addListener(Events.Expand, accordionListener) ;

        }
        m_component.add(searchPanel) ;
        searchPanel.addListener(Events.Expand, accordionListener) ;

        // this is the save opened paths feature
        Window.addWindowCloseListener(new WindowCloseListener() {
            public String onWindowClosing() {
                return null;
            }
            public void onWindowClosed() {
                saveOpenedPaths();
            }
        });
    }

    private void renameSearch(GWTJahiaNode selection) {
        if (selection != null) {
            if (selection.isLocked()) {
                Window.alert(selection.getName() + " is locked") ;
                return ;
            }
            String newName = Window.prompt("Enter the new name for " + (selection.isFile().booleanValue()?"file ":"folder ") + selection.getName(), selection.getName()) ;
            if (newName != null && newName.length() > 0 && !newName.equals(selection.getName())) {
                service.rename(selection.getPath(), newName, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Window.alert("Rename failed\n\n" + throwable.getLocalizedMessage()) ;
                    }
                    public void onSuccess(Object o) {
                        retrieveSavedSearch();
                    }
                });
            }
        }
    }

    private void saveOpenedPaths() {
        Map<String, List<String>> openPathsForRepositoryType = new HashMap<String, List<String>>() ;
        for (RepositoryTab tab: repositories) {
            openPathsForRepositoryType.put(tab.getRepositoryType(), tab.getOpenedPaths());
        }
        service.saveOpenPaths(openPathsForRepositoryType, new AsyncCallback() {
            public void onSuccess(Object o) {
                // nothing here...
            }
            public void onFailure(Throwable throwable) {
                Window.alert("Could not save expanded paths into user preferences:\n\n" + throwable.getLocalizedMessage()) ;
            }
        });
    }

    private class ChangeAccordionListener<T extends ComponentEvent> implements Listener<T> {
        public void handleEvent(T t) {
            if (searchPanel.isExpanded()) {
                retrieveSavedSearch();
            }
            getLinker().onTreeItemSelected();
        }
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        for (RepositoryTab tab: repositories) {
            tab.init();
        }
        retrieveSavedSearch();
    }

    public void openAndSelectItem(Object item) {
        if (item != null) {
            for (RepositoryTab tab: repositories) {
                tab.openAndSelectItem(item);
            }
        }
    }

    public void refresh() {
        for (RepositoryTab tab: repositories) {
            tab.refresh();
        }
    }

    private void retrieveSavedSearch() {
        queryList.removeAll() ;
        service.getSavedSearch(new AsyncCallback<List<GWTJahiaNode>>() {
            public void onFailure(Throwable throwable) {
                // ...
            }
            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                for (GWTJahiaNode query: gwtJahiaNodes) {
                    addSavedSearch(query, false);
                }
                if (searchPanel.isExpanded()) {
                    getLinker().onTreeItemSelected();
                }
            }
        });
    }

    public Object getSelectedItem() {
        if (searchPanel.isExpanded()) {
            if (queryList.getSelectedItems().size() == 1) {
                return queryList.getSelectedItem().getData("query");
            } else {
                return null ;
            }
        } else {
            for (RepositoryTab tab: repositories) {
                if (tab.isExpanded()) {
                    return tab.getSelectedItem() ;
                }
            }
        }
        return null ;
    }

    public Component getComponent() {
        return m_component ;
    }

    public void addSavedSearch(GWTJahiaNode query, boolean expandSearchPanel) {
        DataListItem queryItem = new DataListItem() ;
        queryItem.setData("query", query);
        queryItem.setText(query.getName());
        queryList.add(queryItem);
        if (expandSearchPanel) {
            searchPanel.setExpanded(true);
        }
    }

}