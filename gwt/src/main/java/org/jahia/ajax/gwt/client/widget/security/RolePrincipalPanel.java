package org.jahia.ajax.gwt.client.widget.security;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 2:51:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class RolePrincipalPanel extends LayoutContainer {
    private List<GWTJahiaRole> roles = new ArrayList<GWTJahiaRole>();
    private boolean site = true;
    private boolean server = true;
    private String principalKey;
    private final JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();

    public RolePrincipalPanel(boolean site, boolean server, String principalKey) {
        this.site = site;
        this.server = server;
        this.principalKey = principalKey;
    }

    public RolePrincipalPanel() {
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        //setLayout(new FitLayout());
        JahiaContentManagementService.App.getInstance().getRoles(principalKey, new AsyncCallback<List<GWTJahiaRole>>() {
            public void onSuccess(List<GWTJahiaRole> gwtRoles) {
                roles = gwtRoles;
                updateUI();
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error while retrieving roles", throwable);
            }
        });


    }

    /**
     * Update uui
     */
    private void updateUI() {
        final ListStore<GWTJahiaRole> store = new ListStore<GWTJahiaRole>();
        // prepare data
        for (GWTJahiaRole r : roles) {
            // prepare data
            List<GWTJahiaPermission> permissions = r.getPermissions();
            String permLabels = "";
            for (int j = 0; j < permissions.size(); j++) {
                permLabels += permissions.get(j).getLabel();
                if (j < permissions.size()) {
                    permLabels += "<br/>";
                }
            }
            r.set("permLabels", permLabels);

        }


        store.add(roles);
        final XTemplate tpl = XTemplate.create("<p><b>Permissions:</b> <br/><p>{permLabels}</p>");
        final RowExpander expander = new RowExpander();
        expander.setTemplate(tpl);
        final List<ColumnConfig> configs = createColumnsConfig(expander);
        if (configs != null) {

            final ColumnModel cm = new ColumnModel(configs);
            ContentPanel cp = new ContentPanel();
            cp.setBorders(false);
            cp.setBodyBorder(false);
            cp.setHeading("Roles mapping");
            cp.setButtonAlign(Style.HorizontalAlignment.CENTER);
            cp.setLayout(new FitLayout());
            cp.setSize(700, 300);

            Grid<GWTJahiaRole> grid = new Grid<GWTJahiaRole>(store, cm);
            grid.setStyleAttribute("borderTop", "none");
            grid.addPlugin(expander);
            grid.setBorders(false);
            cp.add(grid);

            add(cp);
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
    private List<ColumnConfig> createColumnsConfig(RowExpander expander) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }

        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(expander);

        ColumnConfig column = new ColumnConfig();
        column.setId("label");
        column.setHeader("Role name");
        column.setWidth(550);
        configs.add(column);


        final int index = configs.size();
        final GridCellRenderer<GWTJahiaRole> rolePermissionRenderer = new GridCellRenderer<GWTJahiaRole>() {
            public Object render(final GWTJahiaRole currentRole, String property, ColumnData config, final int rowIndex,
                                 final int colIndex, ListStore<GWTJahiaRole> store, Grid<GWTJahiaRole> grid) {

                final CheckBox checkbox = new CheckBox();
                final GWTJahiaRole role = roles.get(colIndex - index);
                checkbox.setValue(true);
                checkbox.setToolTip(currentRole.getLabel());
                checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        if (checkbox.getValue()) {
                            contentService.grantRoleToUser(role,principalKey, new AsyncCallback(){
                                public void onSuccess(Object o) {
                                    Log.debug("role granted");
                                }

                                public void onFailure(Throwable throwable) {
                                    Log.error("Error while granting role to user",throwable);
                                }
                            });
                        } else {
                            contentService.removeRoleToPrincipal(role,principalKey, new AsyncCallback(){
                                public void onSuccess(Object o) {
                                    Log.debug("role granted");
                                }

                                public void onFailure(Throwable throwable) {
                                    Log.error("Error while removing role to user",throwable);
                                }
                            });
                        }
                    }

                });
                return checkbox;
            }
        };

        column = new ColumnConfig();
        column.setRenderer(rolePermissionRenderer);
        column.setId("user");
        column.setHeader("Grant");
        column.setWidth(100);
        configs.add(column);

        return configs;
    }


}

