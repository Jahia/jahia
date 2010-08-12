package org.jahia.ajax.gwt.client.widget.security;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaPrincipal;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;

import java.util.ArrayList;
import java.util.List;

/**
 * GWT panel that allows management of role-to-principal relationship.
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 2:14:43 PM
 */
public class PrincipalRolePanel extends LayoutContainer {
    private List<GWTJahiaPrincipal> principals = new ArrayList<GWTJahiaPrincipal>();
    private GWTJahiaRole role;
    private final JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();


    public PrincipalRolePanel(GWTJahiaRole role) {
        this.role = role;
        setLayout(new FitLayout());
        contentService.getPrincipalsInRole(role, new BaseAsyncCallback<List<GWTJahiaPrincipal>>() {
            public void onSuccess(List<GWTJahiaPrincipal> p) {
                principals = p;
                updateUI();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Error while retrieving roles", throwable);
            }
        });
    }

    /**
     * Update ui
     */
    private void updateUI() {
        final ListStore<GWTJahiaPrincipal> store = new ListStore<GWTJahiaPrincipal>();
        Log.debug("update ui");

        store.add(principals);
        final List<ColumnConfig> configs = createColumnsConfig(store);

        final ColumnModel cm = new ColumnModel(configs);
        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBorders(false);
        cp.setBodyBorder(false);
        ToolBar bar = new ToolBar();
        Button addUser = new Button(Messages.get("label.addUsersToRole"));
        addUser.setIconStyle("um-adduser");
        Button addGroup = new Button(Messages.get("label.addGroupsToRole"));
        addGroup.setIconStyle("um-addgroup");
        bar.add(new FillToolItem());
        bar.setAlignment(HorizontalAlignment.LEFT);
        bar.add(addUser);
        bar.add(addGroup);
        final UserGroupAdder userGroupAdder = new UserGroupAdder() {
            public void addGroups(List<GWTJahiaGroup> groups) {
                final List<GWTJahiaPrincipal> pList = new ArrayList<GWTJahiaPrincipal>();
                for (GWTJahiaPrincipal p : groups) {
                    if (!store.contains(p)) {
                        pList.add(p);
                    }
                }
                if (pList.size() > 0) {
                    contentService.grantRoleToPrincipals(role, pList, new BaseAsyncCallback<Object>() {
                        public void onSuccess(Object o) {
                            Log.debug("Grant role to groups");
                            for (GWTJahiaPrincipal p : pList) {
                                if (!store.contains(p)) {
                                    store.add(p);
                                }
                            }
                        }
    
                        public void onApplicationFailure(Throwable throwable) {
                            Log.error("Error while granting role to groups", throwable);
                        }
                    });
                }
            }

            public void addRoles(List<GWTJahiaRole> roles) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void addUsers(List<GWTJahiaUser> users) {
                final List<GWTJahiaPrincipal> pList = new ArrayList<GWTJahiaPrincipal>();
                for (GWTJahiaPrincipal p : users) {
                    if (!store.contains(p)) {
                        pList.add(p);
                    }
                }
                if (pList.size() > 0) {
                    contentService.grantRoleToPrincipals(role, pList, new BaseAsyncCallback<Object>() {
                        public void onSuccess(Object o) {
                            Log.debug("Grant role to users");
                            for (GWTJahiaPrincipal p : pList) {
                                if (!store.contains(p)) {
                                    store.add(p);
                                }
                            }
                        }
    
                        public void onApplicationFailure(Throwable throwable) {
                            Log.error("Error while granting role to users", throwable);
                        }
                    });
                }
            }
        };
        addUser.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_USERS, "currentSite");
            }
        });
        addGroup.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_GROUPS, "currentSite");
            }
        });

        cp.setTopComponent(bar);
        cp.setButtonAlign(Style.HorizontalAlignment.LEFT);
        cp.setLayout(new FitLayout());
        cp.setSize(700, 300);

        Grid<GWTJahiaPrincipal> grid = new Grid<GWTJahiaPrincipal>(store, cm);
        grid.setStyleAttribute("borderTop", "none");
        grid.setBorders(false);
        cp.add(grid);

        add(cp);
        layout();
    }

    /**
     * Create columns from roles
     *
     * @return
     */
    private List<ColumnConfig> createColumnsConfig(final ListStore<GWTJahiaPrincipal> store) {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig();
        column.setId("name");
        column.setHeader(Messages.get("label.name", "Name"));
        column.setWidth(350);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("siteName");
        column.setHeader(Messages.get("org.jahia.admin.site.ManageSites.siteServerName.label", "Site"));
        column.setWidth(250);
        configs.add(column);

        ColumnConfig action = new ColumnConfig("action", "", 100);
        action.setAlignment(Style.HorizontalAlignment.RIGHT);
        action.setRenderer(new GridCellRenderer<GWTJahiaPrincipal>() {
            public Object render(GWTJahiaPrincipal modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore<GWTJahiaPrincipal> listStore, Grid<GWTJahiaPrincipal> grid) {
                Button button = new Button(Messages.get("label.remove", "Remove"), new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        final GWTJahiaPrincipal principal = (GWTJahiaPrincipal) buttonEvent.getButton().getData("associatedNode");
                        final List<GWTJahiaPrincipal> principalList = new ArrayList<GWTJahiaPrincipal>(1);
                        principalList.add(principal);
                        contentService.removeRoleToPrincipals(role, principalList, new BaseAsyncCallback<Object>() {
                            public void onSuccess(Object o) {
                                Log.debug("Revoke role from principal " + principal.getKey());
                                store.remove(principal);
                            }

                            public void onApplicationFailure(Throwable throwable) {
                                Log.error("Error while revoking role from principal", throwable);
                            }
                        });
                    }
                });
                button.setData("associatedNode", modelData);
                button.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
                return button;
            }
        });
        action.setFixed(true);
        configs.add(action);

        return configs;
    }


}


