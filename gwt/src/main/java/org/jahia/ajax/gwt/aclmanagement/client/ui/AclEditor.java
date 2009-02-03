/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.aclmanagement.client.ui;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.*;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToggleToolItem;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaGroup;
import org.jahia.ajax.gwt.aclmanagement.client.model.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.aclmanagement.client.model.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaUser;
import org.jahia.ajax.gwt.commons.client.util.ResourceBundle;
import org.jahia.ajax.gwt.usergroupmanagement.client.UserGroupAdder;
import org.jahia.ajax.gwt.usergroupmanagement.client.ui.UserGroupSelect;

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
    private final boolean showSiteSelector;
    private List<String> items;
    private SaveButton saveButton;
    private RestoreButton restoreButton;
    private Table tbl;
    private boolean saved = false;
    private boolean canBreakInheritance = false;
    private ToggleToolItem breakinheritanceItem;
    private String aclGroup;
    private boolean readOnly = false;
    private List<String> available;

    public AclEditor (GWTJahiaNodeACL acl, boolean showSiteSelector) {
        this.originalAcl = acl;
        this.showSiteSelector = showSiteSelector;
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

    public ContentPanel renderNewAclPanel() {
        final List<TableColumn> columns = new ArrayList<TableColumn>();

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

        col = new TableColumn(getResource("ae_principal"), .70f-.05f*available.size());
        columns.add(col);

        for (String s : available) {
            final int i = columns.size();
            Map<String, String> permissionLabels = acl.getPermissionLabels();
            String columnName = s;
            if (permissionLabels != null) {
                columnName = acl.getPermissionLabels().get(s);
            }
            col = new TableColumn(s, columnName, .05f);
            col.setRenderer(new CellRenderer<TableItem>() {
                public String render(final TableItem item, final String perm, final Object value) {
                    final GWTJahiaNodeACE ace = (GWTJahiaNodeACE) value;
                    String v = ace.getPermissions().get(perm);
                    CheckBox chb = new CheckBox();
                    chb.setChecked("GRANT".equals(v));
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
                            if (!ace.getInheritedPermissions().isEmpty()) {
                                if (ace.getPermissions().equals(ace.getInheritedPermissions()) && !acl.isBreakAllInheritance()) {
                                    ((Button) item.getValue(available.size() + 2)).removeFromParent();
                                    item.setValue(available.size() + 2, buildInheritanceLabel(ace));
                                    ace.setInherited(true);
                                } else {
                                    if (ace.isInherited()) {
                                        Button button = buildRestoreButton(item, ace);
                                        item.setValue(available.size() + 2, button);
//                                    ((Button) item.getValue(available.size() + 2)).setVisible(true);
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

        col = new TableColumn("local", "", .25f);
        col.setRenderer(new CellRenderer<TableItem>() {
            public String render(final TableItem item, final String property, final Object value) {
                final GWTJahiaNodeACE ace = (GWTJahiaNodeACE) value;

                if (!ace.getInheritedPermissions().isEmpty() && !acl.isBreakAllInheritance()) {
                    if (!ace.getPermissions().equals(ace.getInheritedPermissions())) {
                        item.setWidget(available.size() + 2, buildRestoreButton(item, ace));
                    } else {
                        item.setValue(available.size() + 2, buildInheritanceLabel(ace));
                    }
                } else {
                    Button button = new Button();
                    button.setText(getResource("ae_remove"));
                    button.setEnabled(!readOnly);
                    button.addSelectionListener(new SelectionListener<ComponentEvent>() {
                        public void componentSelected(ComponentEvent event) {
                            setDirty();
                            String o = ace.getPrincipalType() + ace.getPrincipal();
                            items.remove(o);
                            tbl.remove(item);
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
                    item.setWidget(available.size() + 2, button);
                }
                return "";
            }
        });

        columns.add(col);


        TableColumnModel cm = new TableColumnModel(columns);
        tbl = new Table(cm);
        tbl.setBulkRender(false);
        tbl.sort(1, Style.SortDir.ASC);
        items = new ArrayList<String>();
        aceMap = new HashMap<String, GWTJahiaNodeACE>();
        List<GWTJahiaNodeACE> l = acl.getAce();
        for (GWTJahiaNodeACE ace : l) {
            addTableItem(tbl, ace, available);
            aceMap.put(ace.getPrincipalType() + ace.getPrincipalKey(), ace);
        }

        restoreButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                reinitAcl();
                tbl.removeAll();
                items.clear();
                aceMap.clear();
                List<GWTJahiaNodeACE> l = acl.getAce();
                for (GWTJahiaNodeACE ace : l) {
                    addTableItem(tbl, ace, available);
                    aceMap.put(ace.getPrincipalType() + ace.getPrincipalKey(), ace);
                }
                breakinheritanceItem.toggle(acl.isBreakAllInheritance());
                setBreakInheritanceLabel();

            }
        });

        ContentPanel panel = new ContentPanel();
        panel.setLayout(new FitLayout());
        panel.setCollapsible(false);
        panel.setFrame(false);
        panel.setAnimCollapse(false);
        panel.setBorders(false);
        panel.add(tbl);

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
                            ace.getPermissions().put(s, first?"GRANT":"DENY");
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
                    addTableItem(tbl, ace, available);
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
                            ace.getPermissions().put(s, first?"GRANT":"DENY");
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
                    addTableItem(tbl, ace, available);
                }
            }
        };

        ToolBar toolBar = new ToolBar();
        TextToolItem item = new TextToolItem(getResource("um_adduser"), "um-adduser");
        item.setEnabled(!readOnly);
        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                Log.debug("add");
                new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_USERS, showSiteSelector);
            }
        });
        toolBar.add(item);
        item = new TextToolItem(getResource("um_addgroup"), "um-addgroup");
        item.setEnabled(!readOnly);
        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                Log.debug("add");
                new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_GROUPS, showSiteSelector);
            }
        });
        toolBar.add(item);
        breakinheritanceItem = new ToggleToolItem();
        if (canBreakInheritance) {
            setBreakInheritanceLabel();

            breakinheritanceItem.pressed = acl.isBreakAllInheritance();
            breakinheritanceItem.setEnabled(!readOnly);
            breakinheritanceItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    Log.debug("toggle");
                    acl.setBreakAllInheritance(breakinheritanceItem.isPressed());
                    setDirty();
                    setBreakInheritanceLabel();
                    tbl.removeAll();
                    items.clear();
                    List<GWTJahiaNodeACE> list = new ArrayList<GWTJahiaNodeACE>(acl.getAce());
                    for (GWTJahiaNodeACE ace : list) {
                        if (!breakinheritanceItem.isPressed()) {
                            if (ace.getPermissions().equals(ace.getInheritedPermissions())) {
                                ace.setInherited(true);
                            }
                        }
                        addTableItem(tbl, ace, available);
                    }
                }
            });
            toolBar.add(breakinheritanceItem);
        }
        panel.setTopComponent(toolBar);

        panel.addButton(saveButton);
        panel.addButton(restoreButton);
        panel.setHeaderVisible(false);

        return panel;
    }

    private Text buildInheritanceLabel(GWTJahiaNodeACE ace) {
        Text text = new Text(getResource("ae_inherited_from")+" : "+ace.getInheritedFrom());
        text.setStyleName("my-tbl-item-cell-text");
        return text;
    }

    private Button buildRestoreButton(final TableItem item, final GWTJahiaNodeACE ace) {
        Button button = new Button();
        button.setText(getResource("ae_restore_inheritance"));
        button.setEnabled(!readOnly);
        button.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                setDirty();
                Log.debug("restore" + ace.getPermissions());
                ace.getPermissions().clear();
                ace.getPermissions().putAll(ace.getInheritedPermissions());
                Object[] o = item.getValues();
                for (int i = 2; i < o.length - 1; i++) {
                    CheckBox chb = (CheckBox) o[i];
                    String perm = tbl.getColumnModel().getColumn(i).getId();
                    String v = ace.getPermissions().get(perm);
                    chb.setChecked("GRANT".equals(v));
                }
                ace.setInherited(true);
                event.component.removeFromParent();
                item.setValue(available.size() + 2, buildInheritanceLabel(ace));
            }
        });
        return button;
    }

    private void setBreakInheritanceLabel() {
        if (acl.isBreakAllInheritance()) {
            breakinheritanceItem.setText(getResource("ae_restore_all_inheritance"));
        } else {
            breakinheritanceItem.setText(getResource("ae_break_all_inheritance"));
        }
    }

    private void reinitAcl() {
        this.saved = true;
        saveButton.setEnabled(false);
        restoreButton.setEnabled(false);

        acl = originalAcl.cloneObject();
        for (GWTJahiaNodeACE ace : acl.getAce()) {
            if (ace.isInherited()) ace.getPermissions().putAll(ace.getInheritedPermissions());
        }
    }

    public void setSaved() {
        this.originalAcl = acl.cloneObject();
        reinitAcl();
    }

    public void setDirty() {
        this.saved = false;
        saveButton.setEnabled(true);
        restoreButton.setEnabled(true);
    }


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

    public SaveButton getSaveButton() {
        return saveButton;
    }

    public void setSaveButton(SaveButton saveButton) {
        this.saveButton = saveButton;
    }

    public RestoreButton getRestoreButton() {
        return restoreButton;
    }

    public void setRestoreButton(RestoreButton restoreButton) {
        this.restoreButton = restoreButton;
    }


    public GWTJahiaNodeACL getAcl() {
        return acl;
    }

    /**
     * Save button, takes care of sending modified properties to server.
     */
    private class SaveButton extends Button {
        public SaveButton() {
            super(getResource("ae_save"));
        }
    }

    /**
     * Restore the properties retrieved by the last server call.
     */
    private class RestoreButton extends Button {
        public RestoreButton() {
            super(getResource("ae_restore"));
        }
    }

    public String getResource(String key) {
        return ResourceBundle.getResource("gwtacleditor", "gwtacleditor", key);
    }

}
