/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.security;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTRolesPermissions;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.LinkerComponent;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

/**
 * Panel for managing permission to role assignment.
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 11:38:47 AM
 */
public class PermissionRolePanel extends LayoutContainer implements LinkerComponent {
    private List<GWTJahiaRole> roles = new ArrayList<GWTJahiaRole>();
    private List<GWTJahiaPermission> permissions = new ArrayList<GWTJahiaPermission>();

    private final JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    private GWTJahiaRole role;

    public PermissionRolePanel(GWTJahiaRole role) {
        this.role = role;
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setLayout(new FitLayout());
        // refresh data
        refresh();

    }

    /**
     * Refresh data
     */
    public void refresh() {
        contentService.getRolesAndPermissions(new BaseAsyncCallback<GWTRolesPermissions>() {
            public void onSuccess(GWTRolesPermissions gwtRolesPermissions) {
                roles = gwtRolesPermissions.getRoles();
                permissions = gwtRolesPermissions.getPermissions();
                refreshUI();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Error while retrieving roles and permissions", throwable);
            }
        });
    }

    /**
     * Update ui
     */
    public void refreshUI() {
        removeAll();
        final TreeStore<GWTJahiaPermission> store = new TreeStore<GWTJahiaPermission>();
        store.add(permissions, true);
        store.sort("name", SortDir.ASC);
        List<ColumnConfig> configs = createColumnsConfig();
        if (configs != null) {
            final ColumnModel cm = new ColumnModel(configs);
            TreeGrid<GWTJahiaPermission> grid = new TreeGrid<GWTJahiaPermission>(store, cm);
            grid.expandAll();
            grid.setAutoExpandColumn("name");
            grid.setStyleAttribute("borderTop", "none");
            grid.setBorders(true);
            add(grid);

        } else {
            add(new Label("There is no defined roles"));
        }
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
        column.setHeader("");
        column.setRenderer(new TreeGridCellRenderer());
        column.setWidth(350);
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
                checkbox.setValue(role.hasPermission(currentPermission));
                checkbox.setToolTip(currentPermission.getName());
                checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        final List<GWTJahiaPermission> pList = new ArrayList<GWTJahiaPermission>();
                        pList.add(currentPermission);
                        if (checkbox.getValue()) {
                            // adding a permission
                            contentService.addRolePermissions(role, pList, new BaseAsyncCallback() {
                                public void onSuccess(Object o) {
                                    Log.debug("permission added to role");
                                    if (!role.getPermissions().contains(currentPermission)) {
                                        role.getPermissions().add(currentPermission);
                                    }
                                }

                                public void onApplicationFailure(Throwable throwable) {
                                    Log.error("Error while adding a permission to a role", throwable);
                                }
                            });
                        } else {
                            // removing a permission
                            contentService.removeRolePermissions(role, pList, new BaseAsyncCallback() {
                                public void onSuccess(Object o) {
                                    Log.debug("permission removed from role");
                                    role.getPermissions().remove(currentPermission);
                                }

                                public void onApplicationFailure(Throwable throwable) {
                                    Log.error("Error removing permission from", throwable);
                                }
                            });
                        }
                    }

                });
                return checkbox;
            }
        };

        for (GWTJahiaRole role : roles) {
            if (role.getName().equals(this.role.getName())) {
                column = new ColumnConfig();
                column.setRenderer(rolePermissionRenderer);
                column.setId(role.getName());
                column.setHeader(role.getName());
                column.setWidth(100);
                column.setSortable(false);
                column.setGroupable(false);
                configs.add(column);
                this.role = role;
                break;
            }
        }
        return configs;
    }
}
