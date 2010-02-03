package org.jahia.ajax.gwt.client.widget.security;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTRolesPermissions;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 11:38:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class PermissionRolePanel extends LayoutContainer {
    private List<GWTJahiaRole> roles = new ArrayList<GWTJahiaRole>();
    private List<GWTJahiaPermission> permissions = new ArrayList<GWTJahiaPermission>();

    private boolean site = true;
    private boolean server = false;
    private ContentPanel mainPanel = new ContentPanel();
    private final JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();

    public PermissionRolePanel() {
    }

    public PermissionRolePanel(List<GWTJahiaRole> roles) {
        this.roles = roles;
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setLayout(new FillLayout());
        mainPanel.setHeaderVisible(false);
        mainPanel.setHeaderVisible(false);
        mainPanel.setBodyBorder(false);
        mainPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        mainPanel.setLayout(new FitLayout());
        mainPanel.setSize(600, 500);
        add(mainPanel);
        // refresh data
        refreshData();

    }

    /**
     * Refresh data
     */
    private void refreshData() {
        contentService.getRolesAndPermissions(site, server, new AsyncCallback<GWTRolesPermissions>() {
            public void onSuccess(GWTRolesPermissions gwtRolesPermissions) {
                roles = gwtRolesPermissions.getRoles();
                permissions = gwtRolesPermissions.getPermissions();
                updateUI();
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error while retriving roles and permissions", throwable);
            }
        });
    }

    /**
     * Update ui
     */
    private void updateUI() {
        mainPanel.removeAll();
        final GroupingStore<GWTJahiaPermission> store = new GroupingStore<GWTJahiaPermission>();
        store.add(permissions);
        store.groupBy("group");
        List<ColumnConfig> configs = createColumnsConfig();
        if (configs != null) {
            final ColumnModel cm = new ColumnModel(createColumnsConfig());
            Grid<GWTJahiaPermission> grid = new Grid<GWTJahiaPermission>(store, cm);
            grid.setStyleAttribute("borderTop", "none");
            grid.setBorders(true);

            GroupingView view = new GroupingView();
            view.setShowGroupedColumn(false);
            view.setForceFit(true);
            view.setGroupRenderer(new GridGroupRenderer() {
                public String render(GroupColumnData data) {
                    return data.group;
                }
            });
            grid.setView(view);
            mainPanel.add(grid);

        } else {
            mainPanel.add(new Label("There is no defined roles"));
        }
        mainPanel.layout();
    }

    /**
     * Create columns from roles
     *
     * @return
     */
    private List<ColumnConfig> createColumnsConfig() {
        if (roles == null || roles.isEmpty()) {
            return null;
        }

        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig();
        column.setId("label");
        column.setHeader("");
        column.setWidth(200);
        configs.add(column);


        column = new ColumnConfig();
        column.setHidden(true);
        column.setId("group");
        configs.add(column);

        final int index = configs.size();
        final GridCellRenderer<GWTJahiaPermission> rolePermissionRenderer = new GridCellRenderer<GWTJahiaPermission>() {
            public Object render(final GWTJahiaPermission currentPermission, String property, ColumnData config, final int rowIndex,
                                 final int colIndex, ListStore<GWTJahiaPermission> store, Grid<GWTJahiaPermission> grid) {

                final CheckBox checkbox = new CheckBox();
                final GWTJahiaRole role = roles.get(colIndex - index);
                checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        final List<GWTJahiaPermission> pList = new ArrayList<GWTJahiaPermission>();
                        pList.add(currentPermission);
                        if (checkbox.getValue()) {
                            // adding a permission
                            contentService.addRolePermissions(role, pList, new AsyncCallback() {
                                public void onSuccess(Object o) {
                                    Log.debug("permission added to role");
                                }

                                public void onFailure(Throwable throwable) {
                                    Log.error("Error while adding a permission to a role", throwable);
                                }
                            });
                        } else {
                            // removing a permission
                            contentService.removeRolePermissions(role, pList, new AsyncCallback() {
                                public void onSuccess(Object o) {
                                    Log.debug("permission removed to role");
                                }

                                public void onFailure(Throwable throwable) {
                                    Log.error("Error removing permissin from", throwable);
                                }
                            });
                        }
                    }

                });
                checkbox.setValue(role.hasPermission(currentPermission));
                checkbox.setToolTip(currentPermission.getLabel());
                return checkbox;
            }
        };

        for (GWTJahiaRole role : roles) {
            column = new ColumnConfig();
            column.setRenderer(rolePermissionRenderer);
            column.setId(role.getLabel());
            column.setHeader(role.getLabel());
            column.setWidth(100);
            configs.add(column);
        }
        return configs;
    }
}
