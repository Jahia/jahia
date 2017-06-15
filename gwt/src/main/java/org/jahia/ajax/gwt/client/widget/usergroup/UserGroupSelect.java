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
import org.jahia.ajax.gwt.client.data.GWTJahiaSite;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
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
    private SearchField userSearchField;
    private String lastUserSearchValue;
    private SearchField groupSearchField;
    private String lastGroupSearchValue;
    private ListStore<GWTJahiaSite> sites;
    private String selectedSite;
    private Grid<GWTJahiaNode> userGrid;
    private Grid<GWTJahiaNode> groupGrid;
    private String siteKey;
    private boolean singleSelectionMode;

    public UserGroupSelect (final UserGroupAdder target, int viewMode, String siteKey) {
        this(target, viewMode, siteKey, false);
    }
    
    public UserGroupSelect (final UserGroupAdder target, int viewMode, String siteKey, boolean singleSelectionMode) {
        addStyleName("user-group-select");
        this.siteKey = siteKey;
        this.singleSelectionMode = singleSelectionMode;
        setModal(true);
        setSize(500, 500);
        setLayout(new FitLayout());

        switch (viewMode) {
            case VIEW_TABS:
                ContentPanel userPanel = getUserPanel(target);

                TabItem userTab = new TabItem(Messages.get("label.users", "Users"));
                userTab.setLayout(new FitLayout());
                userTab.add(userPanel);

                ContentPanel groupsPanel = getGroupsPanel(target);

                TabItem groupsTab = new TabItem(Messages.get("label.groups", "Groups"));
                groupsTab.setLayout(new FitLayout());
                groupsTab.add(groupsPanel);

                TabPanel tabs = new TabPanel();
                tabs.add(userTab);
                tabs.add(groupsTab);
                add(tabs);
                break;
            case VIEW_USERS:
                add(getUserPanel(target));
                break;
            case VIEW_GROUPS:
                add(getGroupsPanel(target));
                break;
        }
        Button add = new Button(Messages.get("label.add", "Add"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                if (userGrid != null) {
                    target.addUsersGroups(userGrid.getSelectionModel().getSelectedItems());
                }
                if (groupGrid != null) {
                    target.addUsersGroups(groupGrid.getSelectionModel().getSelectedItems());
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

    private ContentPanel getUserPanel(final UserGroupAdder target) {
        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaNode>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object pageLoaderConfig, AsyncCallback<PagingLoadResult<GWTJahiaNode>> callback) {
                JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
                String newSearch = userSearchField.getText().trim().replace("'","''");

                String query = "select * from [jnt:user] as u where (isdescendantnode(u,'/users/') or isdescendantnode(u,'/sites/"+siteKey.replace("'","''")+"/users/'))";
                if (newSearch.length() > 0) {
                    query += " and (CONTAINS(u.*,'%" + newSearch + "%') OR LOWER(u.[j:nodename]) LIKE '%" + newSearch.toLowerCase() + "%') ";
                }
                query += " ORDER BY u.[j:nodename]";

                // reset offset to 0 if the search value has changed
                int offset = lastUserSearchValue != null && lastUserSearchValue.equals(newSearch) ? ((PagingLoadConfig) pageLoaderConfig).getOffset() : 0;

                service.searchSQL(query, ((PagingLoadConfig) pageLoaderConfig).getLimit(), offset, null, GWTJahiaNode.DEFAULT_USER_FIELDS, false, callback);

                // remember last searched value
                lastUserSearchValue = newSearch;
            }
        };
        final PagingLoader<PagingLoadResult<GWTJahiaNode>> loader = new BasePagingLoader<PagingLoadResult<GWTJahiaNode>>(proxy);
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
        if("siteSelector".equals(siteKey) ){
            ComboBox<GWTJahiaSite> siteMenu = createMenu(loader);
            panel.add(siteMenu);
        }

        ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>(loader);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("displayName", Messages.get("label.username", "User name"), 120));
        columns.add(new ColumnConfig("j:lastName", Messages.get("label.lastName", "Last name"), 140));
        columns.add(new ColumnConfig("j:firstName", Messages.get("label.firstName", "First name"), 140));
//        columns.add(new ColumnConfig("siteName", "Site name", 80));
        columns.add(new ColumnConfig("providerKey", Messages.get("column.provider.label", "Provider"), 80));
//        columns.add(new ColumnConfig("email", "Email", 100));

        ColumnModel cm = new ColumnModel(columns);

        final PagingToolBar toolBar = new PagingToolBar(15);
        toolBar.bind(loader);

        userGrid = new Grid<GWTJahiaNode>(store, cm);
        if (singleSelectionMode) {
            userGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        userGrid.setLoadMask(true);
        userGrid.setBorders(true);
        userGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                target.addUsersGroups(userGrid.getSelectionModel().getSelectedItems());
                if (singleSelectionMode) {
                    hide();
                }
            }
        });

        ContentPanel userPanel = new ContentPanel();
        userPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        userPanel.setHeadingHtml(Messages.get("label.userSelect", "Select users"));
        userPanel.setLayout(new FitLayout());
        userPanel.add(userGrid);
        userPanel.setSize(480, 350);
        userPanel.setBottomComponent(toolBar);
        userPanel.setTopComponent(panel);
        return userPanel;
    }


    private ContentPanel getGroupsPanel(final UserGroupAdder target) {
        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaNode>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object pageLoaderConfig, AsyncCallback<PagingLoadResult<GWTJahiaNode>> callback) {
                String newSearch = groupSearchField.getText().trim().replace("'","''");

                String query = "select * from [jnt:group] as g where ";
                if (!siteKey.equals("systemsite")) {
                    query += "(isdescendantnode(g,'/groups') or isdescendantnode(g,'/sites/"+siteKey.replace("'","''") +"/groups'))";
                } else {
                    query += "isdescendantnode(g,'/groups')";
                }

                if (newSearch.length() > 0) {
                    query += " and (CONTAINS(g.*,'%" + newSearch + "%') OR LOWER(g.[j:nodename]) LIKE '%" + newSearch.toLowerCase() + "%') ";
                }
                query += " ORDER BY g.[j:nodename]";

                // reset offset to 0 if the search value has changed
                int offset = lastGroupSearchValue != null && lastGroupSearchValue.equals(newSearch) ? ((PagingLoadConfig) pageLoaderConfig).getOffset() : 0;

                JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
                service.searchSQL(query, ((PagingLoadConfig) pageLoaderConfig).getLimit(), offset, null, GWTJahiaNode.DEFAULT_USER_FIELDS, false, callback);

                // remember last searched value
                lastGroupSearchValue = newSearch;
            }
        };
        final BasePagingLoader<PagingLoadResult<GWTJahiaNode>> loader = new BasePagingLoader<PagingLoadResult<GWTJahiaNode>>(proxy);

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
        if("siteSelector".equals(siteKey) ){
            ComboBox<GWTJahiaSite> siteMenu = createMenu(loader);
            panel.add(siteMenu);
        }
        ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>(loader);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("displayName", "Group name", 240));
        columns.add(new ColumnConfig("siteKey", "Site name", 120));
        columns.add(new ColumnConfig("providerKey", "Provider", 120));

        ColumnModel cm = new ColumnModel(columns);

        final PagingToolBar toolBar = new PagingToolBar(15);
        toolBar.bind(loader);

        groupGrid = new Grid<GWTJahiaNode>(store, cm);
        if (singleSelectionMode) {
            groupGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        groupGrid.setLoadMask(true);
        groupGrid.setBorders(true);
        groupGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                target.addUsersGroups(groupGrid.getSelectionModel().getSelectedItems());
                if (singleSelectionMode) {
                    hide();
                }
            }
        });

        ContentPanel groupsPanel = new ContentPanel();
        groupsPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        groupsPanel.setHeadingHtml(Messages.get("label.groupSelect", "Select groups"));
        groupsPanel.setLayout(new FitLayout());
        groupsPanel.add(groupGrid);
        groupsPanel.setSize(480, 350);
        groupsPanel.setBottomComponent(toolBar);
        groupsPanel.setTopComponent(panel);
        return groupsPanel;
    }

    protected ComboBox<GWTJahiaSite> createMenu (final PagingLoader loader) {
        if ("siteSelector".equals(siteKey)) {

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
            
            JahiaContentManagementService.App.getInstance().getAvailableSites(new BaseAsyncCallback<List<GWTJahiaSite>>() {
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
