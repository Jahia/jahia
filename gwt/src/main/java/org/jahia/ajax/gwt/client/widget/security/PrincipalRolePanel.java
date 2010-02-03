package org.jahia.ajax.gwt.client.widget.security;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaPrincipal;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 2:14:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrincipalRolePanel extends LayoutContainer {
    private List<GWTJahiaPrincipal> principals = new ArrayList<GWTJahiaPrincipal>();
    private GWTJahiaRole role;
    private final JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();


    public PrincipalRolePanel(GWTJahiaRole role) {
        this.role = role;
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        //setLayout(new FitLayout());
        contentService.getPrincipalsInRole(role, new AsyncCallback<List<GWTJahiaPrincipal>>() {
            public void onSuccess(List<GWTJahiaPrincipal> p) {
                principals = p;
                updateUI();
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error while retriving roles", throwable);
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
        final List<ColumnConfig> configs = createColumnsConfig();

        final ColumnModel cm = new ColumnModel(configs);
        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBorders(false);
        cp.setBodyBorder(false);
        ToolBar bar = new ToolBar();
        Button addUser = new Button("Add user");
        Button addGroup = new Button("Add Group");
        bar.add(addUser);
        bar.add(addGroup);
        final UserGroupAdder userGroupAdder = new UserGroupAdder() {
            public void addGroups(List<GWTJahiaGroup> groups) {
                final List<GWTJahiaPrincipal> pList = new ArrayList<GWTJahiaPrincipal>();
                pList.addAll(groups);
                contentService.grantRoleToPrincipals(role, pList, new AsyncCallback() {
                    public void onSuccess(Object o) {
                        Log.debug("Grant role to groups");
                        store.add(pList);
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error("Error while granting role to groups", throwable);
                    }
                });
            }

            public void addUsers(List<GWTJahiaUser> users) {
                final List<GWTJahiaPrincipal> pList = new ArrayList<GWTJahiaPrincipal>();
                pList.addAll(users);
                contentService.grantRoleToPrincipals(role, pList, new AsyncCallback() {
                    public void onSuccess(Object o) {
                        Log.debug("Grant role to users");
                        store.add(pList);
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error("Error while granting role to users", throwable);
                    }
                });
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
        cp.setButtonAlign(Style.HorizontalAlignment.CENTER);
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
    private List<ColumnConfig> createColumnsConfig() {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig();
        column.setId("name");
        column.setHeader("Name");
        column.setWidth(550);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("siteName");
        column.setHeader("siteName");
        column.setWidth(550);
        configs.add(column);

        return configs;
    }


}


