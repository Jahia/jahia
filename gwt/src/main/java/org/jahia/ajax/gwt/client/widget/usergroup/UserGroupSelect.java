/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.usergroup;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.service.UserManagerServiceAsync;
import org.jahia.ajax.gwt.client.widget.SearchField;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.*;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaSite;

import java.util.ArrayList;
import java.util.List;

/**
 * User/group selection window.
 * User: toto
 * Date: Nov 5, 2008
 * Time: 2:05:59 PM
 */
public class UserGroupSelect extends Window {
    public static final int VIEW_USERS = 1;
    public static final int VIEW_GROUPS = 2;
    public static final int VIEW_TABS = 3;
    private JahiaServiceAsync service = JahiaService.App.getInstance();
    private SearchField userSearchField;
    private SearchField groupSearchField;
    private ListStore<GWTJahiaSite> sites;
    private String selectedSite;
    private Grid<GWTJahiaUser> userGrid;
    private Grid<GWTJahiaGroup> groupGrid;
    private final String aclContext;
    private boolean singleSelectionMode;

    public UserGroupSelect (final UserGroupAdder target, int viewMode, String aclContext) {
        this(target, viewMode, aclContext, false);
    }
    
    public UserGroupSelect (final UserGroupAdder target, int viewMode, String aclContext, boolean singleSelectionMode) {
        this.aclContext = aclContext;
        this.singleSelectionMode = singleSelectionMode;
        setModal(true);
        setSize(500, 500);
        setLayout(new FitLayout());
        final UserManagerServiceAsync service = UserManagerService.App.getInstance();

        switch (viewMode) {
            case VIEW_TABS:
                ContentPanel userPanel = getUserPanel(target, service);

                TabItem userTab = new TabItem("users");
                userTab.setLayout(new FitLayout());
                userTab.add(userPanel);

                ContentPanel groupsPanel = getGroupsPanel(target, service);

                TabItem groupsTab = new TabItem("groups");
                groupsTab.setLayout(new FitLayout());
                groupsTab.add(groupsPanel);

                TabPanel tabs = new TabPanel();
                tabs.add(userTab);
                tabs.add(groupsTab);
                add(tabs);
                break;
            case VIEW_USERS:
                add(getUserPanel(target, service));
                break;
            case VIEW_GROUPS:
                add(getGroupsPanel(target, service));
                break;
        }
        ButtonBar buttons = new ButtonBar() ;
        Button add = new Button("Add", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                if (userGrid != null) {
                    target.addUsers(userGrid.getSelectionModel().getSelectedItems());
                }
                if (groupGrid != null) {
                    target.addGroups(groupGrid.getSelectionModel().getSelectedItems());
                }
                hide();
            }
        }) ;
        buttons.add(add) ;
        Button cancel = new Button("Cancel", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide();
            }
        });
        buttons.add(cancel) ;        
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setButtonBar(buttons);

        show();
    }

    private ContentPanel getUserPanel(final UserGroupAdder target, final UserManagerServiceAsync service) {
        // data proxy
        RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaUser>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaUser>>() {
            @Override
            protected void load(PagingLoadConfig pageLoaderConfig,
                                AsyncCallback<PagingLoadResult<GWTJahiaUser>> callback) {
                String context = aclContext;
                if ("siteSelector".equals(aclContext)) {
                    context = "site:"+selectedSite;
                }
                if (context != null) {
                    if (userSearchField.getText().length()==0)  {
                        service.searchUsersInContext("*",pageLoaderConfig.getOffset(), pageLoaderConfig.getLimit(),context, callback);
                    } else {
                        service.searchUsersInContext("*"+userSearchField.getText()+"*",pageLoaderConfig.getOffset(), pageLoaderConfig.getLimit(), context, callback);
                    }
                }
            }
        };
        final BasePagingLoader loader = new BasePagingLoader<PagingLoadConfig,
                PagingLoadResult<GWTJahiaUser>>(proxy);
        userSearchField = new SearchField("Search: ", false) {
            public void onFieldValidation(String value) {
                loader.load();
            }

            public void onSaveButtonClicked(String value) {

            }
        };
        userSearchField.setWidth(250) ;
        loader.setLimit(15);
        loader.load();
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(userSearchField);
        if("siteSelector".equals(aclContext) ){
            ComboBox<GWTJahiaSite> siteMenu = createMenu(loader);
            panel.add(siteMenu);
        }

        ListStore<GWTJahiaUser> store = new ListStore<GWTJahiaUser>(loader);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("userName", "User name", 120));
        columns.add(new ColumnConfig("lastname", "Last name", 140));
        columns.add(new ColumnConfig("firstname", "First name", 140));
//        columns.add(new ColumnConfig("siteName", "Site name", 80));
        columns.add(new ColumnConfig("provider", "Provider", 80));
//        columns.add(new ColumnConfig("email", "Email", 100));

        ColumnModel cm = new ColumnModel(columns);

        final PagingToolBar toolBar = new PagingToolBar(15);
        toolBar.bind(loader);

        userGrid = new Grid<GWTJahiaUser>(store, cm);
        if (singleSelectionMode) {
            userGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        userGrid.setLoadMask(true);
        userGrid.setBorders(true);
        userGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                target.addUsers(userGrid.getSelectionModel().getSelectedItems());
                if (singleSelectionMode) {
                    hide();
                }
            }
        });

        ContentPanel userPanel = new ContentPanel();
        userPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        userPanel.setIconStyle("icon-table");
        userPanel.setHeading("Select a user");
        userPanel.setLayout(new FitLayout());
        userPanel.add(userGrid);
        userPanel.setSize(480, 350);
        userPanel.setBottomComponent(toolBar);
        userPanel.setTopComponent(panel);
        return userPanel;
    }


    private ContentPanel getGroupsPanel(final UserGroupAdder target, final UserManagerServiceAsync service) {
        // data proxy
        RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaGroup>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaGroup>>() {
            @Override
            protected void load(PagingLoadConfig pageLoaderConfig,
                                AsyncCallback<PagingLoadResult<GWTJahiaGroup>> callback) {
                String context = aclContext;
                if ("siteSelector".equals(aclContext)) {
                    context = "site:"+selectedSite;
                }
                service.searchGroupsInContext("*"+groupSearchField.getText()+"*",pageLoaderConfig.getOffset(), pageLoaderConfig.getLimit(), context, callback);
            }
        };
        final BasePagingLoader loader = new BasePagingLoader<PagingLoadConfig,
                PagingLoadResult<GWTJahiaGroup>>(proxy);

        groupSearchField = new SearchField("Search: ", false) {
            public void onFieldValidation(String value) {
                loader.load();
            }

            public void onSaveButtonClicked(String value) {

            }
        };
        groupSearchField.setWidth(250) ;

        loader.setLimit(15);
        loader.load();
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(groupSearchField);
        if("siteSelector".equals(aclContext) ){
            ComboBox<GWTJahiaSite> siteMenu = createMenu(loader);
            panel.add(siteMenu);
        }
        ListStore<GWTJahiaGroup> store = new ListStore<GWTJahiaGroup>(loader);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("groupname", "Group name", 120));
        columns.add(new ColumnConfig("displaymembers", "Members", 200));
        columns.add(new ColumnConfig("siteName", "Site name", 80));
        columns.add(new ColumnConfig("provider", "Provider", 80));

        ColumnModel cm = new ColumnModel(columns);

        final PagingToolBar toolBar = new PagingToolBar(15);
        toolBar.bind(loader);

        groupGrid = new Grid<GWTJahiaGroup>(store, cm);
        if (singleSelectionMode) {
            groupGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        groupGrid.setLoadMask(true);
        groupGrid.setBorders(true);
        groupGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                target.addGroups(groupGrid.getSelectionModel().getSelectedItems());
                if (singleSelectionMode) {
                    hide();
                }
            }
        });

        ContentPanel groupsPanel = new ContentPanel();
        groupsPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        groupsPanel.setIconStyle("icon-table");
        groupsPanel.setHeading("Select a group");
        groupsPanel.setLayout(new FitLayout());
        groupsPanel.add(groupGrid);
        groupsPanel.setSize(480, 350);
        groupsPanel.setBottomComponent(toolBar);
        groupsPanel.setTopComponent(panel);
        return groupsPanel;
    }

    protected ComboBox<GWTJahiaSite> createMenu (final BasePagingLoader loader) {
        if ("siteSelector".equals(aclContext)) {

            sites = new ListStore<GWTJahiaSite>();

            final ComboBox<GWTJahiaSite> siteMenu = new ComboBox<GWTJahiaSite>();
            siteMenu.setEmptyText("Select a site...");
            siteMenu.setDisplayField("siteKey");
            siteMenu.setStore(sites);
            siteMenu.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaSite>() {
            	public void selectionChanged(SelectionChangedEvent<GWTJahiaSite> se) {
				    selectedSite = se.getSelectedItem().getSiteKey();
                    loader.load();
                }
            });
            
            service.getAvailableSites(new AsyncCallback<List<GWTJahiaSite>>() {
                public void onFailure (Throwable throwable) {
                    Log.error("Err",throwable);
                }
    
                public void onSuccess (List<GWTJahiaSite> gwtJahiaSites) {
                    sites.add(gwtJahiaSites);
                    if (gwtJahiaSites.size() > 0) {
                        siteMenu.setValue(gwtJahiaSites.get(0));
                    }

                }
            });
            return siteMenu;
        }
        return null;
    }
}
