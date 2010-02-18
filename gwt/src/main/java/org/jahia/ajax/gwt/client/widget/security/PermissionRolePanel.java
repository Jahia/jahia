package org.jahia.ajax.gwt.client.widget.security;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTRolesPermissions;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.LinkerComponent;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
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
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Panel for managing permission to role assignment.
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 11:38:47 AM
 */
public class PermissionRolePanel extends LayoutContainer implements LinkerComponent {
    private List<GWTJahiaRole> roles = new ArrayList<GWTJahiaRole>();
    private List<GWTJahiaPermission> permissions = new ArrayList<GWTJahiaPermission>();
    private String siteKey = null;
    private Grid<GWTJahiaPermission> grid;

    private ContentPanel mainPanel = new ContentPanel();
    private final JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();

    public PermissionRolePanel() {
    }

    public PermissionRolePanel(String siteKey) {
        this.siteKey = siteKey;
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
        mainPanel.setSize(600, 470);
        mainPanel.setFrame(true);
        add(mainPanel);
        // refresh data
        refresh();

    }

    /**
     * Refresh data
     */
    public void refresh() {
        contentService.getRolesAndPermissions(siteKey, new AsyncCallback<GWTRolesPermissions>() {
            public void onSuccess(GWTRolesPermissions gwtRolesPermissions) {
                roles = gwtRolesPermissions.getRoles();
                permissions = gwtRolesPermissions.getPermissions();
                refreshUI();
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error while retrieving roles and permissions", throwable);
            }
        });
    }

    /**
     * Update ui
     */
    public void refreshUI() {
        mainPanel.removeAll();
        
        final GroupingStore<GWTJahiaPermission> store = new GroupingStore<GWTJahiaPermission>();
        store.add(permissions);
        store.groupBy("group");
        store.sort("name", SortDir.ASC);
        List<ColumnConfig> configs = createColumnsConfig();
        if (configs != null) {
            final ColumnModel cm = new ColumnModel(configs);
            cm.addHeaderGroup(0, 0, new HeaderGroupConfig("", 1, 1));
            cm.addHeaderGroup(0, 1, new HeaderGroupConfig("", 1, 1));
            for (int i = 2; i < configs.size(); i++) {
                cm.addHeaderGroup(0, i, new HeaderGroupConfig(createGrantAllCheckbox(i - 2), 1, 1));
            }
            
            grid = new Grid<GWTJahiaPermission>(store, cm);
            grid.setStyleAttribute("borderTop", "none");
            grid.setBorders(true);
            
            GroupingView view = new GroupingView();
            view.setShowGroupedColumn(false);
            view.setForceFit(false);
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

    private Button createAddPermissionButton() {
        final String label = Messages.get("label.newPermission", "Add permission");
        Button add = new Button(label);
        add.setIconStyle("gwt-toolbar-icon-addContent");
        add.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                final Dialog dialog = new Dialog();
                dialog.setSize(300, 200);
                dialog.setHeading(label);  
                dialog.setButtons(Dialog.OKCANCEL);
                dialog.setHideOnButtonClick(true);
                LayoutContainer panel = new LayoutContainer(new FormLayout(LabelAlign.LEFT));
                panel.setStyleAttribute("padding", "10px 30px 10px 10px");
                
                final TextField<String> name = new TextField<String>();  
                name.setFieldLabel(Messages.get("label.name", "Name"));  
                name.setAllowBlank(false);
                panel.add(name, new FormData("100%"));                 
                
                final ListStore<GWTJahiaBasicDataBean> store = new ListStore<GWTJahiaBasicDataBean>();
                for (GWTJahiaPermission perm : permissions) {
                    GWTJahiaBasicDataBean permGroup = new GWTJahiaBasicDataBean(perm.getGroup(), perm.getGroup()); 
                    if (!store.contains(permGroup)) {
                        store.add(permGroup);
                    }
                }

                final ComboBox<GWTJahiaBasicDataBean> groupSelect = new ComboBox<GWTJahiaBasicDataBean>();
                groupSelect.setDisplayField(GWTJahiaBasicDataBean.DISPLAY_NAME);
                groupSelect.setStore(store);
                groupSelect.setTypeAhead(true);
                groupSelect.setTriggerAction(ComboBox.TriggerAction.ALL);
                groupSelect.setFieldLabel(Messages.get("label.group", "Group"));

                panel.add(groupSelect, new FormData("100%"));
                
                dialog.add(panel);
                dialog.addListener(Events.Hide, new Listener<WindowEvent>() {
                    public void handleEvent(WindowEvent be) {
                        if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.OK)) {
                            contentService.createPermission(name.getValue(), groupSelect.getRawValue(), siteKey, new AsyncCallback<GWTJahiaPermission>() {
                                public void onSuccess(GWTJahiaPermission perm) {
                                    permissions.add(perm);
                                    grid.getStore().add(perm);
                                    for (int i = 0; i < roles.size(); i++) {
                                        updateGrantAllCheckboxState(i);
                                    }
                                }
                                public void onFailure(Throwable throwable) {
                                    Log.error("Error while creating permission " + name.getValue(), throwable);
                                }
                            });
                        }
                    }
                });
                
                dialog.show();
            }
        });
        
        return add;
    }

    private void updateGrantAllCheckboxState(final int roleIndex) {
        final CheckBox cb = (CheckBox) grid.getColumnModel().getHeaderGroups().get(roleIndex + 2).getWidget();
        boolean oldState = cb.getValue();
        boolean state = roleHasAllPemrissions(roleIndex);
        if (oldState != state) {
            cb.setFireChangeEventOnSetValue(false);
            cb.setValue(state);
            cb.setFireChangeEventOnSetValue(true);
        }
    }
    
    private boolean roleHasAllPemrissions(final int roleIndex) {
        final GWTJahiaRole role = roles.get(roleIndex); 
        boolean state = true;
        for (GWTJahiaPermission perm : permissions) {
            state = role.hasPermission(perm);
            if (!state) {
                break;
            }
        }
        return state;
    }
    
    private CheckBox createGrantAllCheckbox(final int roleIndex) {
        final GWTJahiaRole role = roles.get(roleIndex); 
        final CheckBox cb = new CheckBox();
        cb.setValue(roleHasAllPemrissions(roleIndex));
        
        cb.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (cb.getValue()) {
                    // adding a permission
                    contentService.addRolePermissions(role, permissions, new AsyncCallback<Object>() {
                        public void onSuccess(Object o) {
                            Log.debug("permissions added to role " + role.getName());
                            role.getPermissions().clear();
                            role.getPermissions().addAll(permissions);
                            updateState();
                        }
                        public void onFailure(Throwable throwable) {
                            Log.error("Error while adding a permissions to a role " + role.getName(), throwable);
                        }
                    });
                } else {
                    // removing a permission
                    contentService.removeRolePermissions(role, permissions, new AsyncCallback<Object>() {
                        public void onSuccess(Object o) {
                            Log.debug("permissions revoked from role " + role.getName());
                            role.getPermissions().clear();
                            updateState();
                        }
                        public void onFailure(Throwable throwable) {
                            Log.error("Error revoking permissions from role " + role.getName(), throwable);
                        }
                    });
                }
            }

            private void updateState() {
                for (int i = 0; i < permissions.size(); i++) {
                    CheckBox rolePemissionCheckBox = (CheckBox) grid.getView().getWidget(i, roleIndex + 2);
                    rolePemissionCheckBox.setFireChangeEventOnSetValue(false);
                    rolePemissionCheckBox.setValue(cb.getValue());
                    rolePemissionCheckBox.setFireChangeEventOnSetValue(true);
                }
            }
        });
        
        return cb;
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
                checkbox.setValue(role.hasPermission(currentPermission));
                checkbox.setToolTip(currentPermission.getName());
                checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        final List<GWTJahiaPermission> pList = new ArrayList<GWTJahiaPermission>();
                        pList.add(currentPermission);
                        if (checkbox.getValue()) {
                            // adding a permission
                            contentService.addRolePermissions(role, pList, new AsyncCallback() {
                                public void onSuccess(Object o) {
                                    Log.debug("permission added to role");
                                    if (!role.getPermissions().contains(currentPermission)) {
                                        role.getPermissions().add(currentPermission);
                                    }
                                    updateGrantAllCheckboxState(colIndex - index);
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
                                    role.getPermissions().remove(currentPermission);
                                    updateGrantAllCheckboxState(colIndex - index);
                                }

                                public void onFailure(Throwable throwable) {
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
            column = new ColumnConfig();
            column.setRenderer(rolePermissionRenderer);
            column.setId(role.getName());
            column.setHeader(role.getName());
            column.setWidth(100);
            column.setSortable(false);
            column.setGroupable(false);
            configs.add(column);
        }
        return configs;
    }
}
