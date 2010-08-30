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

package org.jahia.ajax.gwt.client.util.acleditor;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 12, 2008
 * Time: 11:24:32 AM
 */
public class AclEditor {

    private GWTJahiaNodeACL acl;
    private Map<String, GWTJahiaNodeACE> aceMap;
    private GWTJahiaNodeACL originalAcl;
    private final String context;
    private List<String> items;
    private SaveButton saveButton;
    private RestoreButton restoreButton;
    private Grid<ModelData> aclTable;
    private ListStore<ModelData> store;
    private boolean saved = false;
    private boolean canBreakInheritance = false;
    private Button breakinheritanceItem;
    private String aclGroup;
    private boolean readOnly = false;
    private List<String> available;
    private boolean displayInheritanceColumn = true;
    private String addUsersLabel = getResource("org.jahia.engines.users.SelectUG_Engine.newUsers.label");
    private String addGroupsLabel = getResource("org.jahia.engines.users.SelectUG_Engine.newGroups.label");
    private String addRolesLabel = getResource("org.jahia.engines.users.SelectUG_Engine.newRoles.label");

    public AclEditor(GWTJahiaNodeACL acl, String aclContext) {
        this.originalAcl = acl;
        this.context = aclContext;
        this.acl = originalAcl.cloneObject();
        saveButton = new SaveButton();
        restoreButton = new RestoreButton();
        reinitAcl();
    }

    public void setCanBreakInheritance(boolean canBreakInheritance) {
        this.canBreakInheritance = canBreakInheritance;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setAclGroup(String aclGroup) {
        this.aclGroup = aclGroup;
    }

    public String getAddUsersLabel() {
        if (addUsersLabel == null) {
            return getResource("org.jahia.engines.users.SelectUG_Engine.newUsers.label");
        }
        return addUsersLabel;
    }

    public void setAddUsersLabel(String addUsersLabel) {
        this.addUsersLabel = addUsersLabel;
    }

    public String getAddGroupsLabel() {
        if (addGroupsLabel == null) {
            return getResource("org.jahia.engines.users.SelectUG_Engine.newGroups.label");
        }
        return addGroupsLabel;
    }

    public String getAddRolesLabel() {
        if (addRolesLabel == null) {
            return getResource("org.jahia.engines.users.SelectUG_Engine.newRoles.label");
        }
        return addRolesLabel;
    }

    public void setAddGroupsLabel(String addGroupsLabel) {
        this.addGroupsLabel = addGroupsLabel;
    }

    public void setAddRolesLabel(String addRolesLabel) {
        this.addRolesLabel = addRolesLabel;
    }

    public void addNewAclPanel(final LayoutContainer c) {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {
            }

            public void onSuccess() {
                c.add(renderNewAclPanel());
                c.layout();
            }
        });
    }

    public ContentPanel renderNewAclPanel() {
        final List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        // im. representing the principal
        ColumnConfig col = new ColumnConfig("icon", 30);
        col.setRenderer(new GridCellRenderer() {
            public Object render(final ModelData model, final String perm, ColumnData config, final int rowIndex, final int colIndex,
                                 ListStore listStore, final Grid grid) {
                GWTJahiaNodeACE ace = model.get("ace");
                Html html = new Html("&nbsp;");
                if (ace.getPrincipalType() == 'u') {
                    html.setStyleName("um-user");
                } else if (ace.getPrincipalType() == 'g') {
                    html.setStyleName("um-group");
                } else {
                    html.setStyleName("um-role");
                }
                return html;
            }
        });
        columns.add(col);

        final Map<String, List<String>> map = acl.getAvailablePermissions();
        if (aclGroup == null) {
            available = new ArrayList<String>();
            if(map != null && !map.isEmpty()) {
                for (List<String> l : map.values()) {
                    available.addAll(l);
                }
            }
        } else {
            available = map.get(aclGroup);
        }

        // name of the princial
        col = new ColumnConfig("principal",getResource("label.user"), 140);
        columns.add(col);

        // add a column per available permission
        for (String s : available) {
            final int i = columns.size();
            Map<String, String> permissionLabels = acl.getPermissionLabels();
            final String columnName;
            if (permissionLabels != null) {
                columnName = acl.getPermissionLabels().get(s);
            } else {
                columnName = s;
            }
            col = new ColumnConfig(s, columnName, 80);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            col.setRenderer(new GridCellRenderer() {
                public Object render(final ModelData model, final String perm, ColumnData config, final int rowIndex, final int colIndex,
                                     ListStore listStore, final Grid grid) {
                    final GWTJahiaNodeACE ace = model.get("ace");
                    String permValue = ace.getPermissions().get(perm);
                    CheckBox chb = new CheckBox();
                    chb.setTitle(columnName);
                    chb.setValue("GRANT".equals(permValue));
                    chb.setEnabled(!readOnly);
                    chb.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent sender) {
                            setDirty();
                            boolean checked = ((CheckBox) sender.getSource()).getValue()!=null?((CheckBox) sender.getSource()).getValue():false;
                            ace.getPermissions().put(perm, checked ? "GRANT" : "DENY");
                            if (checked) {
                                grid.getView().getRow(rowIndex);
                                List<String> toCheck = acl.getPermissionsDependencies().get(perm);
                                if (toCheck != null) {
                                    for (String s1 : toCheck) {
                                        CheckBox checkBox = (CheckBox) grid.getView().getWidget(rowIndex,available.indexOf(perm) + 2);
                                        if (checkBox!=null && !checkBox.getValue()) {
                                            checkBox.setValue(true,true);
                                        }
                                    }
                                }
                            } else {
                                Set<String> toCheck = acl.getPermissionsDependencies().keySet();
                                for (String s1 : toCheck) {
                                    if (acl.getPermissionsDependencies().get(s1).contains(perm)) {
                                        CheckBox checkBox = (CheckBox) grid.getView().getWidget(rowIndex,available.indexOf(perm) + 2);
                                        if (checkBox != null && checkBox.getValue()) {
                                            checkBox.setValue(false,true);
                                        }
                                    }
                                }
                            }
                            // update inheritance column
                            if (!ace.getInheritedPermissions().isEmpty()) {
                                if (ace.getPermissions().equals(ace.getInheritedPermissions()) && !acl.isBreakAllInheritance()) {
                                    if (displayInheritanceColumn) {
                                        LayoutContainer ctn = (LayoutContainer) grid.getView().getWidget(rowIndex, available.size() + 2);
                                        ctn.removeAll();
                                        ctn.add(buildInheritanceLabel(ace));
                                        ctn.layout();
                                    }
                                    ace.setInherited(true);
                                } else {
                                    if (ace.isInherited()) {
                                        if (displayInheritanceColumn) {
                                            LayoutContainer ctn = (LayoutContainer) grid.getView().getWidget(rowIndex, available.size() + 2);
                                            ctn.removeAll();
                                            ctn.add(buildLocalRestoreButton(model, ace));
                                            ctn.layout();
                                        }
                                        ace.setInherited(false);
                                    }
                                }
                            }
                        }
                    });
                    return chb;
                }
            });

            columns.add(col);
        }

        // column break in heritance
        if (displayInheritanceColumn) {
            col = new ColumnConfig("inheritance", "", 100);
            col.setAlignment(Style.HorizontalAlignment.LEFT);
            col.setRenderer(new GridCellRenderer<ModelData>() {
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                     ListStore<ModelData> modelDataListStore, Grid<ModelData> modelDataGrid) {
                    final GWTJahiaNodeACE ace = (GWTJahiaNodeACE) model.get("ace");
                    LayoutContainer widget = new LayoutContainer();
                    if (!readOnly) {
                        if (!ace.getInheritedPermissions().isEmpty() && !acl.isBreakAllInheritance()) {
                            if (!ace.getPermissions().equals(ace.getInheritedPermissions())) {
                                widget.add(buildLocalRestoreButton(model, ace));
                            } else {
                                widget.add(buildInheritanceLabel(ace));
                            }
                        } else {
                            widget.add(buildRemoveButton(model, ace));
                        }
                    }
                    return widget;
                }
            });
            columns.add(col);
        }


        // create the table
        store = new ListStore<ModelData>();
        aclTable = new Grid<ModelData>(store, new ColumnModel(columns));

//        aclTable.setBulkRender(false);
        store.sort("name", Style.SortDir.ASC);
        items = new ArrayList<String>();
        aceMap = new HashMap<String, GWTJahiaNodeACE>();
        List<GWTJahiaNodeACE> l = acl.getAce();

        // populate table
        for (GWTJahiaNodeACE ace : l) {
            addTableItem(store, ace, available);
            aceMap.put(ace.getPrincipalType() + ace.getPrincipalKey(), ace);
        }

        restoreButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                reinitAcl();
//                aclTable.removeAll();
                store.removeAll();
                items.clear();
                aceMap.clear();
                List<GWTJahiaNodeACE> l = acl.getAce();
                for (GWTJahiaNodeACE ace : l) {
                    addTableItem(store, ace, available);
                    aceMap.put(ace.getPrincipalType() + ace.getPrincipalKey(), ace);
                }
                setBreakInheritanceLabel();

            }
        });

        ContentPanel panel = new ContentPanel();
        panel.setLayout(new FitLayout());
        panel.setCollapsible(false);
        panel.setFrame(false);
        panel.setAnimCollapse(false);
        panel.setBorders(false);
        panel.setBodyBorder(false);
        panel.setHeaderVisible(false);
        panel.add(aclTable);

        final UserGroupAdder userGroupAdder = new UserGroupAdder() {
            public void addUsers(List<GWTJahiaUser> users) {
                for (GWTJahiaUser user : users) {
                    GWTJahiaNodeACE ace = aceMap.get('u' + user.getUserKey());
                    if (ace == null) {
                        ace = new GWTJahiaNodeACE();
                        ace.setPrincipalType('u');
                        ace.setPrincipal(user.getUsername());
                        ace.setPrincipalKey(user.getUserKey());
                        ace.setPermissions(new HashMap<String, String>());
                        boolean first = true;
                        for (String s : available) {
                            ace.getPermissions().put(s, first ? "GRANT" : "DENY");
                            first = false;
                        }
                        ace.setInheritedPermissions(new HashMap<String, String>());
                        ace.setInherited(false);
                        acl.getAce().add(ace);
                        aceMap.put('u' + user.getUserKey(), ace);
                    } else {
                        if (acl.isBreakAllInheritance()) {
                            ace.setInherited(false);
                        }
                    }
                    setDirty();
                    addTableItem(store, ace, available);
                }
            }

            public void addGroups(List<GWTJahiaGroup> groups) {
                for (GWTJahiaGroup group : groups) {
                    GWTJahiaNodeACE ace = aceMap.get('g' + group.getGroupKey());
                    if (ace == null) {
                        ace = new GWTJahiaNodeACE();
                        ace.setPrincipalType('g');
                        ace.setPrincipal(group.getGroupname());
                        ace.setPrincipalKey(group.getGroupKey());
                        ace.setPermissions(new HashMap<String, String>());
                        boolean first = true;
                        for (String s : available) {
                            ace.getPermissions().put(s, first ? "GRANT" : "DENY");
                            first = false;
                        }
                        ace.setInheritedPermissions(new HashMap<String, String>());
                        ace.setInherited(false);
                        acl.getAce().add(ace);
                        aceMap.put('g' + group.getGroupKey(), ace);
                    } else {
                        if (acl.isBreakAllInheritance()) {
                            ace.setInherited(false);
                        }
                    }
                    setDirty();
                    addTableItem(store, ace, available);
                }
            }

            public void addRoles(List<GWTJahiaRole> roles) {
                for (GWTJahiaRole role : roles) {
                    GWTJahiaNodeACE ace = aceMap.get('r' + role.getName()+":"+role.getSite());
                    if (ace == null) {
                        ace = new GWTJahiaNodeACE();
                        ace.setPrincipalType('r');
                        ace.setPrincipal(role.getName());
                        ace.setPrincipalKey(role.getName()+":"+role.getSite());
                        ace.setPermissions(new HashMap<String, String>());
                        boolean first = true;
                        for (String s : available) {
                            ace.getPermissions().put(s, first ? "GRANT" : "DENY");
                            first = false;
                        }
                        ace.setInheritedPermissions(new HashMap<String, String>());
                        ace.setInherited(false);
                        acl.getAce().add(ace);
                        aceMap.put('r' + role.getName()+":"+role.getSite(), ace);
                    } else {
                        if (acl.isBreakAllInheritance()) {
                            ace.setInherited(false);
                        }
                    }
                    setDirty();
                    addTableItem(store, ace, available);
                }
            }
        };

        ToolBar toolBar = new ToolBar();
        Button addUsersToolItem = new Button(getAddUsersLabel());
        addUsersToolItem.setIconStyle("um-adduser");
        addUsersToolItem.setEnabled(!readOnly);
        addUsersToolItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_USERS, context);
            }
        });
        toolBar.add(addUsersToolItem);

        addUsersToolItem = new Button(getAddGroupsLabel());
        addUsersToolItem.setIconStyle("um-addgroup");
        addUsersToolItem.setEnabled(!readOnly);
        addUsersToolItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_GROUPS, context);
            }
        });
        toolBar.add(addUsersToolItem);
        addUsersToolItem = new Button(getAddRolesLabel());
        addUsersToolItem.setIconStyle("um-addrole");
        addUsersToolItem.setEnabled(!readOnly);
        addUsersToolItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_ROLES, context);
            }
        });
        toolBar.add(addUsersToolItem);
        breakinheritanceItem = new Button();
        if (canBreakInheritance) {
            setBreakInheritanceLabel();

            breakinheritanceItem.setEnabled(!readOnly);
            breakinheritanceItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    acl.setBreakAllInheritance(!acl.isBreakAllInheritance());
                    setDirty();
                    setBreakInheritanceLabel();
                    store.removeAll();
                    items.clear();
                    List<GWTJahiaNodeACE> list = new ArrayList<GWTJahiaNodeACE>(acl.getAce());
                    for (GWTJahiaNodeACE ace : list) {
                        if (!acl.isBreakAllInheritance()) {
                            if (ace.getPermissions().equals(ace.getInheritedPermissions())) {
                                ace.setInherited(true);
                            }
                        }
                        addTableItem(store, ace, available);
                    }
                }
            });
            toolBar.add(breakinheritanceItem);
        }
        toolBar.add(new FillToolItem());
        toolBar.add(saveButton);
        toolBar.add(restoreButton);
        panel.setTopComponent(toolBar);

        return panel;
    }

    /**
     * Build loacl remove button
     *
     * @param item
     * @param ace
     * @return
     */
    private Button buildRemoveButton(final ModelData item, final GWTJahiaNodeACE ace) {
        Button button = new Button();
        button.setIconStyle("gwt-icons-delete");
        button.setBorders(false);
        button.setToolTip(getResource("label.remove"));
        button.setEnabled(!readOnly);
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                setDirty();
                String o = ace.getPrincipalType() + ace.getPrincipal();
                items.remove(o);
                store.remove(item);
                if (ace.getInheritedPermissions().isEmpty()) {
                    aceMap.remove(ace.getPrincipalType() + ace.getPrincipalKey());
                    acl.getAce().remove(ace);
                } else {
                    ace.getPermissions().clear();
                    ace.getPermissions().putAll(ace.getInheritedPermissions());
                    ace.setInherited(true);
                }
            }
        });
        return button;
    }

    /**
     * Create inheritance label
     *
     * @param ace
     * @return
     */
    private Text buildInheritanceLabel(GWTJahiaNodeACE ace) {
        String label;
        if (ace.getInheritedFrom() != null) {
            label = getResource("org.jahia.engines.rights.ManageRights.inheritedFrom.label") + " : " + ace.getInheritedFrom();
        } else {
            label = getResource("org.jahia.engines.rights.ManageRights.inherited.label");
        }

        Text text = new Text(label);
        text.setToolTip(label);
        text.setStyleName("my-tbl-item-cell-text");
        return text;
    }

    /**
     * Create local restore button
     *
     * @param item
     * @param ace
     * @return
     */
    private Button buildLocalRestoreButton(final ModelData item, final GWTJahiaNodeACE ace) {
        Button button = new Button();
        button.setIconStyle("gwt-icons-restore");
        button.setToolTip(getResource("org.jahia.engines.rights.ManageRights.restoreInheritance.label"));
        button.setEnabled(!readOnly);
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                setDirty();
                Log.debug("restore" + ace.getPermissions());
                ace.getPermissions().clear();
                ace.getPermissions().putAll(ace.getInheritedPermissions());
                int row = store.indexOf(item);
                for (int i = 2; i < available.size() + 2; i++) {
                    CheckBox chb = (CheckBox) aclTable.getView().getWidget(row,i);
                    String perm = aclTable.getColumnModel().getColumn(i).getId();
                    String v = ace.getPermissions().get(perm);
                    chb.setChecked("GRANT".equals(v));
                }
                ace.setInherited(true);
                LayoutContainer ctn = (LayoutContainer) aclTable.getView().getWidget(row,available.size() + 2);
                ctn.removeAll();
                ctn.add(buildInheritanceLabel(ace));
                ctn.layout();
            }
        });
        return button;
    }

    /**
     * Set break inheritance label
     */
    private void setBreakInheritanceLabel() {
        if (acl.isBreakAllInheritance()) {
            breakinheritanceItem.setText(getResource("org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"));
        } else {
            breakinheritanceItem.setText(getResource("org.jahia.engines.rights.ManageRights.breakAllInheritance.label"));
        }
    }

    /**
     * init acl
     */
    private void reinitAcl() {
        this.saved = true;
        saveButton.setEnabled(false);
        restoreButton.setEnabled(false);

        acl = originalAcl.cloneObject();
        for (GWTJahiaNodeACE ace : acl.getAce()) {
            if (ace.isInherited()) {
                ace.getPermissions().putAll(ace.getInheritedPermissions());
            }
        }
    }

    /**
     * Update save flag
     */
    public void setSaved() {
        this.originalAcl = acl.cloneObject();
        reinitAcl();
        store.removeAll();
        items.clear();
        aceMap.clear();
        List<GWTJahiaNodeACE> l = acl.getAce();
        for (GWTJahiaNodeACE ace : l) {
            addTableItem(store, ace, available);
            aceMap.put(ace.getPrincipalType() + ace.getPrincipalKey(), ace);
        }
        setBreakInheritanceLabel();
        
    }

    /**
     * Set dirty
     */
    public void setDirty() {
        this.saved = false;
        saveButton.setEnabled(true);
        restoreButton.setEnabled(true);
    }


    /**
     * Add ace to acl table
     *
     * @param store
     * @param ace
     * @param available
     */
    private void addTableItem(ListStore<ModelData> store, GWTJahiaNodeACE ace, List<String> available) {
        if (ace.isInherited() && acl.isBreakAllInheritance()) {
            return;
        }
        BaseModelData value = new BaseModelData();//Object[3 + available.size()];
        value.set("principal", ace.getPrincipal());

        String o = ace.getPrincipalType() + ace.getPrincipal();
        if (!items.contains(o)) {
            items.add(o);
            value.set("ace",ace);

            store.add(value);
            store.sort("principal", Style.SortDir.ASC);
        }
    }

    /**
     * Get Save button
     *
     * @return
     */
    public SaveButton getSaveButton() {
        return saveButton;
    }

    /**
     * Set save button
     *
     * @param saveButton
     */
    public void setSaveButton(SaveButton saveButton) {
        this.saveButton = saveButton;
    }

    /**
     * Get restore button
     *
     * @return
     */
    public RestoreButton getRestoreButton() {
        return restoreButton;
    }

    /**
     * Set restore button
     *
     * @param restoreButton
     */
    public void setRestoreButton(RestoreButton restoreButton) {
        this.restoreButton = restoreButton;
    }

    /**
     * Get ACL object
     *
     * @return
     */
    public GWTJahiaNodeACL getAcl() {
        return acl;
    }

    /**
     * return true if ACL permissions are empty
     * @return
     */
    public boolean isEmpty(){
       return getAcl() == null || getAcl().getAvailablePermissions() == null || getAcl().getAvailablePermissions().isEmpty();
    }

    /**
     * Save button, takes care of sending modified properties to server.
     */
    private class SaveButton extends Button {
        public SaveButton() {
            super(getResource("label.save"));
            setIconStyle("gwt-icons-save");
        }
    }

    /**
     * Restore the properties retrieved by the last server call.
     */
    private class RestoreButton extends Button {
        public RestoreButton() {
            super(getResource("label.restore"));
            setIconStyle("gwt-icons-restore");
        }
    }

    /**
     * Get resource bundle
     *
     * @param key
     * @return
     */
    public String getResource(String key) {
        return Messages.get(key);
    }

}
