/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.*;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
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
    private Table aclTable;
    private boolean saved = false;
    private boolean canBreakInheritance = false;
    private Button breakinheritanceItem;
    private String aclGroup;
    private boolean readOnly = false;
    private List<String> available;
    private boolean displayInheritanceColumn = true;
    private String addUsersLabel = getResource("um_adduser");
    private String addGroupsLabel = getResource("um_addgroup");

    public AclEditor(GWTJahiaNodeACL acl, String aclContext) {
        this.originalAcl = acl;
        this.context = aclContext;
        this.acl = originalAcl.cloneObject();
        saveButton = new SaveButton();
        restoreButton = new RestoreButton();
        reinitAcl();
    }


    public AclEditor(GWTJahiaNodeACL acl, String aclContext, boolean displayInheritanceColumn) {
        this(acl, aclContext);
        this.displayInheritanceColumn = displayInheritanceColumn;
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
            return getResource("um_adduser");
        }
        return addUsersLabel;
    }

    public void setAddUsersLabel(String addUsersLabel) {
        this.addUsersLabel = addUsersLabel;
    }

    public String getAddGroupsLabel() {
        if (addGroupsLabel == null) {
            return getResource("um_addgroup");
        }
        return addGroupsLabel;
    }

    public void setAddGroupsLabel(String addGroupsLabel) {
        this.addGroupsLabel = addGroupsLabel;
    }

    public ContentPanel renderNewAclPanel() {
        final List<TableColumn> columns = new ArrayList<TableColumn>();

        // im. representing the principal
        TableColumn col = new TableColumn("", .05f);
        columns.add(col);

        if (aclGroup == null) {
            available = new ArrayList<String>();
            for (List<String> l : acl.getAvailablePermissions().values()) {
                available.addAll(l);
            }
        } else {
            available = acl.getAvailablePermissions().get(aclGroup);
        }

        // name of the princial
        col = new TableColumn(getResource("ae_principal"), .2f);
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
            float size = columnName.length() * .02f;
            col = new TableColumn(s, columnName, size);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            col.setRenderer(new CellRenderer<TableItem>() {
                public String render(final TableItem item, final String perm, final Object value) {
                    final GWTJahiaNodeACE ace = (GWTJahiaNodeACE) value;
                    String permValue = ace.getPermissions().get(perm);
                    CheckBox chb = new CheckBox();
                    chb.setTitle(columnName);
                    chb.setChecked("GRANT".equals(permValue));
                    chb.setEnabled(!readOnly);
                    chb.addClickListener(new ClickListener() {
                        public void onClick(Widget sender) {
                            setDirty();
                            boolean checked = ((CheckBox) sender).isChecked();
                            ace.getPermissions().put(perm, checked ? "GRANT" : "DENY");
                            if (checked) {
                                List<String> toCheck = acl.getAclDependencies().get(perm);
                                if (toCheck != null) {
                                    for (String s1 : toCheck) {
                                        CheckBox checkBox = (CheckBox) item.getValue(available.indexOf(s1) + 2);
                                        if (!checkBox.isChecked()) {
                                            checkBox.setChecked(true);
                                            checkBox.onBrowserEvent(Event.getCurrentEvent());
                                        }
                                    }
                                }
                            } else {
                                Set<String> toCheck = acl.getAclDependencies().keySet();
                                for (String s1 : toCheck) {
                                    if (acl.getAclDependencies().get(s1).contains(perm)) {
                                        CheckBox checkBox = (CheckBox) item.getValue(available.indexOf(s1) + 2);
                                        if (checkBox.isChecked()) {
                                            checkBox.setChecked(false);
                                            checkBox.onBrowserEvent(Event.getCurrentEvent());
                                        }
                                    }
                                }

                            }
                            // update inheritance column
                            if (!ace.getInheritedPermissions().isEmpty()) {
                                if (ace.getPermissions().equals(ace.getInheritedPermissions()) && !acl.isBreakAllInheritance()) {
                                    if (displayInheritanceColumn) {
                                        ((Widget) item.getValue(available.size() + 2)).removeFromParent();
                                        item.setValue(available.size() + 2, buildInheritanceLabel(ace));
                                    }
                                    ace.setInherited(true);
                                } else {
                                    if (ace.isInherited()) {
                                        if (displayInheritanceColumn) {
                                            item.setValue(available.size() + 2, buildLocalRestoreButton(item, ace));
                                        }
                                        ace.setInherited(false);
                                    }
                                }
                            }
                        }
                    });
                    item.setWidget(i, chb);
                    return "";
                }
            });

            columns.add(col);
        }

        // column break in heritance
        if (displayInheritanceColumn) {
            col = new TableColumn("local", "", 0.4f);
            col.setAlignment(Style.HorizontalAlignment.LEFT);
            col.setRenderer(new CellRenderer<TableItem>() {
                public String render(final TableItem item, final String property, final Object value) {
                    final GWTJahiaNodeACE ace = (GWTJahiaNodeACE) value;
                    if (!readOnly) {
                        if (!ace.getInheritedPermissions().isEmpty() && !acl.isBreakAllInheritance()) {
                            if (!ace.getPermissions().equals(ace.getInheritedPermissions())) {
                                item.setWidget(available.size() + 2, buildLocalRestoreButton(item, ace));
                            } else {
                                item.setValue(available.size() + 2, buildInheritanceLabel(ace));
                            }
                        } else {
                            item.setWidget(available.size() + 2, buildRemoveButton(item, ace));
                        }
                    }
                    return "";
                }
            });
            columns.add(col);
        }


        // create the table
        aclTable = new Table(new TableColumnModel(columns));
        aclTable.setBulkRender(false);
        aclTable.sort(1, Style.SortDir.ASC);
        items = new ArrayList<String>();
        aceMap = new HashMap<String, GWTJahiaNodeACE>();
        List<GWTJahiaNodeACE> l = acl.getAce();

        // populate table
        for (GWTJahiaNodeACE ace : l) {
            addTableItem(aclTable, ace, available);
            aceMap.put(ace.getPrincipalType() + ace.getPrincipalKey(), ace);
        }

        restoreButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                reinitAcl();
                aclTable.removeAll();
                items.clear();
                aceMap.clear();
                List<GWTJahiaNodeACE> l = acl.getAce();
                for (GWTJahiaNodeACE ace : l) {
                    addTableItem(aclTable, ace, available);
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
                    addTableItem(aclTable, ace, available);
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
                    addTableItem(aclTable, ace, available);
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
        breakinheritanceItem = new Button();
        if (canBreakInheritance) {
            setBreakInheritanceLabel();

            breakinheritanceItem.setEnabled(!readOnly);
            breakinheritanceItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    acl.setBreakAllInheritance(!acl.isBreakAllInheritance());
                    setDirty();
                    setBreakInheritanceLabel();
                    aclTable.removeAll();
                    items.clear();
                    List<GWTJahiaNodeACE> list = new ArrayList<GWTJahiaNodeACE>(acl.getAce());
                    for (GWTJahiaNodeACE ace : list) {
                        if (!acl.isBreakAllInheritance()) {
                            if (ace.getPermissions().equals(ace.getInheritedPermissions())) {
                                ace.setInherited(true);
                            }
                        }
                        addTableItem(aclTable, ace, available);
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
    private Button buildRemoveButton(final TableItem item, final GWTJahiaNodeACE ace) {
        Button button = new Button();
        button.setIconStyle("gwt-icons-delete");
        button.setBorders(false);
        button.setToolTip(getResource("ae_remove"));
        button.setEnabled(!readOnly);
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                setDirty();
                String o = ace.getPrincipalType() + ace.getPrincipal();
                items.remove(o);
                aclTable.remove(item);
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
            label = getResource("ae_inherited_from") + " : " + ace.getInheritedFrom();
        } else {
            label = getResource("ae_inherited");
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
    private Button buildLocalRestoreButton(final TableItem item, final GWTJahiaNodeACE ace) {
        Button button = new Button();
        button.setIconStyle("gwt-icons-restore");
        button.setToolTip(getResource("ae_restore_inheritance"));
        button.setEnabled(!readOnly);
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                setDirty();
                Log.debug("restore" + ace.getPermissions());
                ace.getPermissions().clear();
                ace.getPermissions().putAll(ace.getInheritedPermissions());
                Object[] o = item.getValues();
                for (int i = 2; i < o.length - 1; i++) {
                    CheckBox chb = (CheckBox) o[i];
                    String perm = aclTable.getColumnModel().getColumn(i).getId();
                    String v = ace.getPermissions().get(perm);
                    chb.setChecked("GRANT".equals(v));
                }
                ace.setInherited(true);
                //event.getComponent().removeFromParent(); // TODO verify
                item.setValue(available.size() + 2, buildInheritanceLabel(ace));
            }
        });
        return button;
    }

    /**
     * Set break inheritance label
     */
    private void setBreakInheritanceLabel() {
        if (acl.isBreakAllInheritance()) {
            breakinheritanceItem.setText(getResource("ae_restore_all_inheritance"));
        } else {
            breakinheritanceItem.setText(getResource("ae_break_all_inheritance"));
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
        aclTable.removeAll();
        items.clear();
        aceMap.clear();
        List<GWTJahiaNodeACE> l = acl.getAce();
        for (GWTJahiaNodeACE ace : l) {
            addTableItem(aclTable, ace, available);
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
     * @param tbl
     * @param ace
     * @param available
     */
    private void addTableItem(Table tbl, GWTJahiaNodeACE ace, List<String> available) {
        if (ace.isInherited() && acl.isBreakAllInheritance()) {
            return;
        }
        Object[] values = new Object[3 + available.size()];
        Html html = new Html("&nbsp;");
        if (ace.getPrincipalType() == 'u') {
            html.setStyleName("um-user");
        } else {
            html.setStyleName("um-group");
        }
        values[0] = html;
        values[1] = ace.getPrincipal();

        String o = ace.getPrincipalType() + ace.getPrincipal();
        if (!items.contains(o)) {
            items.add(o);

            int i = 2;
            for (String s : available) {
                values[i++] = ace;
            }
            values[i] = ace;

            TableItem item = new TableItem(values);
            tbl.add(item);
            tbl.sort(1, Style.SortDir.ASC);
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
            super(getResource("ae_save"));
            setIconStyle("gwt-icons-save");
        }
    }

    /**
     * Restore the properties retrieved by the last server call.
     */
    private class RestoreButton extends Button {
        public RestoreButton() {
            super(getResource("ae_restore"));
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
        return Messages.getResource(key);
    }

}
