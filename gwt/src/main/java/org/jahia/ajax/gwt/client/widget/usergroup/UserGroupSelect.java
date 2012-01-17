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

package org.jahia.ajax.gwt.client.widget.usergroup;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaSite;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.UserManagerService;
import org.jahia.ajax.gwt.client.service.UserManagerServiceAsync;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.SearchField;

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
    public static final int VIEW_ROLES = 4;
    private JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
    private SearchField userSearchField;
    private SearchField groupSearchField;
    private SearchField roleSearchField;
    private ListStore<GWTJahiaSite> sites;
    private String selectedSite;
    private Grid<GWTJahiaUser> userGrid;
    private Grid<GWTJahiaGroup> groupGrid;
    private String aclContext;
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
        Button add = new Button(Messages.get("label.add", "Add"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                if (userGrid != null) {
                    target.addUsers(userGrid.getSelectionModel().getSelectedItems());
                }
                if (groupGrid != null) {
                    target.addGroups(groupGrid.getSelectionModel().getSelectedItems());
                }
                hide();
            }
        }) ;
        addButton(add) ;
        Button cancel = new Button(Messages.get("label.cancel", "Cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        addButton(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);

        show();
    }

    private ContentPanel getUserPanel(final UserGroupAdder target, final UserManagerServiceAsync service) {
        // data proxy
        RpcProxy<BasePagingLoadResult<GWTJahiaUser>> proxy = new RpcProxy<BasePagingLoadResult<GWTJahiaUser>>() {
            @Override
            protected void load(Object pageLoaderConfig, AsyncCallback<BasePagingLoadResult<GWTJahiaUser>> callback) {
                String context = aclContext;
                if ("siteSelector".equals(aclContext)) {
                    context = "site:"+selectedSite;
                }
                if (context != null) {
                    if (userSearchField.getText().length()==0)  {
                        service.searchUsersInContext("*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(),context, callback);
                    } else {
                        service.searchUsersInContext("*"+userSearchField.getText()+"*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(), context, callback);
                    }
                }
            }
        };
        final BasePagingLoader<PagingLoadResult<GWTJahiaUser>> loader = new BasePagingLoader<PagingLoadResult<GWTJahiaUser>>(proxy);
        userSearchField = new SearchField(Messages.get("label.search", "Search: "), false) {
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
        columns.add(new ColumnConfig("display", Messages.get("label.username", "User name"), 120));
        columns.add(new ColumnConfig("j:lastName", Messages.get("org.jahia.admin.lastName.label", "Last name"), 140));
        columns.add(new ColumnConfig("j:firstName", Messages.get("org.jahia.admin.firstName.label", "First name"), 140));
//        columns.add(new ColumnConfig("siteName", "Site name", 80));
        columns.add(new ColumnConfig("provider", Messages.get("column.provider.label", "Provider"), 80));
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
        userPanel.setHeading(Messages.get("label.userSelect", "Select a user"));
        userPanel.setLayout(new FitLayout());
        userPanel.add(userGrid);
        userPanel.setSize(480, 350);
        userPanel.setBottomComponent(toolBar);
        userPanel.setTopComponent(panel);
        return userPanel;
    }


    private ContentPanel getGroupsPanel(final UserGroupAdder target, final UserManagerServiceAsync service) {
        // data proxy
        RpcProxy<BasePagingLoadResult<GWTJahiaGroup>> proxy = new RpcProxy<BasePagingLoadResult<GWTJahiaGroup>>() {
            @Override
            protected void load(Object pageLoaderConfig, AsyncCallback<BasePagingLoadResult<GWTJahiaGroup>> callback) {
                String context = aclContext;
                if ("siteSelector".equals(aclContext)) {
                    context = "site:"+selectedSite;
                }

                if (groupSearchField.getText().length()==0)  {
                    service.searchGroupsInContext("*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(),context, callback);
                } else {
                    service.searchGroupsInContext("*"+groupSearchField.getText()+"*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(), context, callback);
                }
            }
        };
        final BasePagingLoader loader = new BasePagingLoader<PagingLoadResult<GWTJahiaGroup>>(proxy);

        groupSearchField = new SearchField(Messages.get("label.search", "Search: "), false) {
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
        columns.add(new ColumnConfig("display", "Group name", 240));
        columns.add(new ColumnConfig("siteName", "Site name", 120));
        columns.add(new ColumnConfig("provider", "Provider", 120));

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
            
            service.getAvailableSites(new BaseAsyncCallback<List<GWTJahiaSite>>() {
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
