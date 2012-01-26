/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;

import java.util.*;

/**
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
    private RestoreButton restoreButton;
    private Grid<ModelData> grid;
    private ListStore<ModelData> store;
    private boolean canBreakInheritance = false;
    private Button breakinheritanceItem;
    private boolean readOnly = false;
    private List<String> displayedRoles;
    private boolean displayInheritanceColumn = true;
    private String addUsersLabel = getResource("newUsers.label");
    private String addGroupsLabel = getResource("newGroups.label");

    public AclEditor(GWTJahiaNodeACL acl, String aclContext, Set<String> roles, Set<String> roleGroups) {
        this.originalAcl = acl;
        this.context = aclContext;
        final Map<String, List<String>> map = acl.getAvailablePermissions();
        if ((roleGroups == null || roleGroups.isEmpty()) && (roles == null || roles.isEmpty())) {
            displayedRoles = new ArrayList<String>();
            if (map != null && !map.isEmpty()) {
                for (List<String> l : map.values()) {
                    displayedRoles.addAll(l);
                }
            }
        } else {
            displayedRoles = new ArrayList<String>();
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    if ((roleGroups != null && roleGroups.contains(entry.getKey()))) {
                        displayedRoles.addAll(entry.getValue());
                    } else if (roles != null && !roles.isEmpty()) {
                        for (String s : entry.getValue()) {
                            if (roles.contains(s)) {
                                displayedRoles.add(s);
                            }
                        }
                    }
                }
            }
        }

        restoreButton = new RestoreButton();
        reinitAcl();
    }

    public void setCanBreakInheritance(boolean canBreakInheritance) {
        this.canBreakInheritance = canBreakInheritance;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getAddUsersLabel() {
        if (addUsersLabel == null) {
            return getResource("newUsers.label");
        }
        return addUsersLabel;
    }

    public void setAddUsersLabel(String addUsersLabel) {
        this.addUsersLabel = addUsersLabel;
    }

    public String getAddGroupsLabel() {
        if (addGroupsLabel == null) {
            return getResource("newGroups.label");
        }
        return addGroupsLabel;
    }

    public void setAddGroupsLabel(String addGroupsLabel) {
        this.addGroupsLabel = addGroupsLabel;
    }

    public void addNewAclPanel(final LayoutContainer c) {
        c.add(renderNewAclPanel());
        c.layout();
    }

    public ContentPanel renderNewAclPanel() {
        final List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        // im. representing the principal
        ColumnConfig col = new ColumnConfig("icon", 30);
        col.setSortable(false);
        col.setFixed(true);
        col.setRenderer(new GridCellRenderer<ModelData>() {
            public Object render(final ModelData model, final String perm, ColumnData config, final int rowIndex, final int colIndex,
                                 ListStore<ModelData> listStore, final Grid<ModelData> grid) {
                GWTJahiaNodeACE ace = model.get("ace");
                Image html;
                if (ace.getPrincipalType() == 'u') {
                    html = StandardIconsProvider.STANDARD_ICONS.user().createImage();
                } else {
                    html = StandardIconsProvider.STANDARD_ICONS.group().createImage();
                }
                return html;
            }
        });
        columns.add(col);

        // name of the princial
        col = new ColumnConfig("principal", Messages.get("label.user", "User") + " / " + Messages.get("label.group", "Group"), 200);
        columns.add(col);
        // column break inheritance
        if (displayInheritanceColumn) {
            col = new ColumnConfig("inheritance", Messages.get("label.inherited"), 300);
            col.setAlignment(Style.HorizontalAlignment.LEFT);
            col.setRenderer(new GridCellRenderer<ModelData>() {
                public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                     ListStore<ModelData> modelDataListStore, Grid<ModelData> modelDataGrid) {
                    final GWTJahiaNodeACE ace = (GWTJahiaNodeACE) model.get("ace");
                    LayoutContainer widget = new LayoutContainer();
                    if (!readOnly && !ace.isHidden()) {
                        if (!ace.getInheritedPermissions().isEmpty() && !acl.isBreakAllInheritance()) {
                            if (!ace.getPermissions().isEmpty()) {
                                widget.add(buildLocalRestoreInheritanceButton(model, ace));
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
        // add a column per available permission
        for (String s : displayedRoles) {
            final String columnName;
            if (acl.getPermissionLabels() != null) {
                columnName = acl.getPermissionLabels().get(s);
            } else {
                columnName = s;
            }
            col = new ColumnConfig(s, columnName, 80);

            if (acl.getPermissionTooltips() != null) {
                String tooltip = acl.getPermissionTooltips().get(s);
                if (tooltip != null && tooltip.length() > 0) {
                    col.setToolTip(tooltip);
                }
            }
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            col.setSortable(false);
            col.setRenderer(new GridCellRenderer<ModelData>() {
                public Object render(final ModelData model, final String perm, ColumnData config, final int rowIndex, final int colIndex,
                                     ListStore<ModelData> listStore, final Grid<ModelData> grid) {
                    final GWTJahiaNodeACE ace = model.get("ace");
                    Boolean permValue = ace.getPermissions().get(perm);
                    Boolean inPermValue = Boolean.TRUE.equals(ace.getInheritedPermissions().get(perm)) && !acl.isBreakAllInheritance();
                    CheckBox chb = new CheckBox();
                    chb.setTitle(columnName);
                    chb.setValue(Boolean.TRUE.equals(permValue) || (permValue == null && Boolean.TRUE.equals(inPermValue)));
                    chb.setEnabled(!readOnly && !ace.isHidden());
                    chb.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent sender) {
                            setDirty();
                            boolean checked = ((CheckBox) sender.getSource()).getValue() != null ? ((CheckBox) sender.getSource()).getValue() : false;
                            ace.getPermissions().put(perm, checked ? Boolean.TRUE : Boolean.FALSE);
                            if (checked) {
                                grid.getView().getRow(rowIndex);
                                List<String> toCheck = acl.getPermissionsDependencies().get(perm);
                                if (toCheck != null) {
                                    for (String s1 : toCheck) {
                                        CheckBox checkBox = (CheckBox) grid.getView().getWidget(rowIndex, displayedRoles.indexOf(s1) + 2);
                                        if (checkBox != null && !checkBox.getValue()) {
                                            checkBox.setValue(true, true);
                                        }
                                    }
                                }
                            } else {
                                Set<String> toCheck = acl.getPermissionsDependencies().keySet();
                                for (String s1 : toCheck) {
                                    if (acl.getPermissionsDependencies().get(s1).contains(perm)) {
                                        CheckBox checkBox = (CheckBox) grid.getView().getWidget(rowIndex, displayedRoles.indexOf(s1) + 2);
                                        if (checkBox != null && checkBox.getValue()) {
                                            checkBox.setValue(false, true);
                                        }
                                    }
                                }
                            }
                            // update inheritance column
                            if (!ace.getInheritedPermissions().isEmpty()) {
                                if (ace.getPermissions().equals(ace.getInheritedPermissions()) && !acl.isBreakAllInheritance()) {
                                    if (displayInheritanceColumn) {
                                        LayoutContainer ctn = (LayoutContainer) grid.getView().getWidget(rowIndex, 2);
                                        ctn.removeAll();
                                        ctn.add(buildInheritanceLabel(ace));
                                        ctn.layout();
                                    }
                                    ace.setInherited(true);
                                } else {
                                    if (ace.isInherited()) {
                                        if (displayInheritanceColumn) {
                                            LayoutContainer ctn = (LayoutContainer) grid.getView().getWidget(rowIndex, 2);
                                            ctn.removeAll();
                                            ctn.add(buildLocalRestoreInheritanceButton(model, ace));
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


        // create the table
        store = new ListStore<ModelData>();
        ColumnModel cm = new ColumnModel(columns);
        grid = new Grid<ModelData>(store, cm);
        final BufferView bufferView = new BufferView();
        bufferView.setRowHeight(28);
        grid.setAutoExpandColumn("inheritance");
        grid.setAutoExpandMax(300);
        grid.setView(bufferView);
//        aclTable.setBulkRender(false);
        store.sort("name", Style.SortDir.ASC);
        items = new ArrayList<String>();
        aceMap = new HashMap<String, GWTJahiaNodeACE>();
        List<GWTJahiaNodeACE> l = acl.getAce();

        // populate table
        for (GWTJahiaNodeACE ace : l) {
            addTableItem(store, ace, displayedRoles);
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
                    addTableItem(store, ace, displayedRoles);
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
        panel.add(grid);

        final UserGroupAdder userGroupAdder = new UserGroupAdder() {
            public void addUsers(List<GWTJahiaUser> users) {
                for (GWTJahiaUser user : users) {
                    GWTJahiaNodeACE ace = aceMap.get('u' + user.getUserKey());
                    if (ace == null) {
                        ace = new GWTJahiaNodeACE();
                        ace.setPrincipalType('u');
                        ace.setPrincipal(user.getDisplay());
                        ace.setPrincipalKey(user.getUserKey());
                        ace.setPermissions(new HashMap<String, Boolean>());
                        ace.setInheritedPermissions(new HashMap<String, Boolean>());
                        ace.setInherited(false);
                        acl.getAce().add(ace);
                        aceMap.put('u' + user.getUserKey(), ace);
                    } else {
                        if (acl.isBreakAllInheritance()) {
                            ace.setInherited(false);
                        }
                    }
                    boolean first = true;
                    for (String s : displayedRoles) {
                        ace.getPermissions().put(s, first ? Boolean.TRUE : Boolean.FALSE);
                        first = false;
                    }
                    setDirty();
                    addTableItem(store, ace, displayedRoles);
                }
            }

            public void addGroups(List<GWTJahiaGroup> groups) {
                for (GWTJahiaGroup group : groups) {
                    GWTJahiaNodeACE ace = aceMap.get('g' + group.getGroupKey());
                    if (ace == null) {
                        ace = new GWTJahiaNodeACE();
                        ace.setPrincipalType('g');
                        ace.setPrincipal(group.getDisplay());
                        ace.setPrincipalKey(group.getGroupKey());
                        ace.setPermissions(new HashMap<String, Boolean>());
                        ace.setInheritedPermissions(new HashMap<String, Boolean>());
                        ace.setInherited(false);
                        acl.getAce().add(ace);
                        aceMap.put('g' + group.getGroupKey(), ace);
                    } else {
                        if (acl.isBreakAllInheritance()) {
                            ace.setInherited(false);
                        }
                    }
                    boolean first = true;
                    for (String s : displayedRoles) {
                        ace.getPermissions().put(s, first ? Boolean.TRUE : Boolean.FALSE);
                        first = false;
                    }
                    setDirty();
                    addTableItem(store, ace, displayedRoles);
                }
            }

        };

        ToolBar toolBar = new ToolBar();
        Button addUsersToolItem = new Button(getAddUsersLabel());
        addUsersToolItem.setIcon(StandardIconsProvider.STANDARD_ICONS.user());
        addUsersToolItem.setEnabled(!readOnly);
        addUsersToolItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_USERS, context);
            }
        });
        toolBar.add(addUsersToolItem);

        addUsersToolItem = new Button(getAddGroupsLabel());
        addUsersToolItem.setIcon(StandardIconsProvider.STANDARD_ICONS.group());
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
                    store.removeAll();
                    items.clear();
                    List<GWTJahiaNodeACE> list = new ArrayList<GWTJahiaNodeACE>(acl.getAce());
                    for (GWTJahiaNodeACE ace : list) {
                        if (!acl.isBreakAllInheritance()) {
                            if (ace.getPermissions().equals(ace.getInheritedPermissions())) {
                                ace.setInherited(true);
                            }
                        }
                        addTableItem(store, ace, displayedRoles);
                    }
                }
            });
            toolBar.add(breakinheritanceItem);
        }
        toolBar.add(new FillToolItem());
        toolBar.add(restoreButton);
        panel.setTopComponent(toolBar);

        return panel;
    }

    /**
     * Build local remove button
     *
     * @param item
     * @param ace
     * @return
     */
    private Button buildRemoveButton(final ModelData item, final GWTJahiaNodeACE ace) {
        Button button = new Button();
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.delete());
        button.setBorders(false);
        button.setToolTip(getResource("label.remove"));
        button.setEnabled(!readOnly && !ace.isHidden());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                setDirty();
                String o = ace.getPrincipalType() + ace.getPrincipalKey();
                items.remove(o);
                store.remove(item);

                ace.getPermissions().clear();
                ace.setInherited(true);
//                for (String role : displayedRoles) {
//                    ace.getPermissions().put(role, false);
//                }

//                if (ace.getInheritedPermissions().isEmpty()) {
//                    aceMap.remove(ace.getPrincipalType() + ace.getPrincipalKey());
//                    acl.getAce().remove(ace);
//                } else {
//                    ace.getPermissions().clear();
//                    ace.setInherited(true);
//                }
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
            label = getResource("label.inheritedFrom") + " : " + ace.getInheritedFrom();
        } else {
            label = getResource("label.inherited");
        }

        Text text = new Text(label);
        return text;
    }

    /**
     * Create local restore button
     *
     * @param item
     * @param ace
     * @return
     */
    private Button buildLocalRestoreInheritanceButton(final ModelData item, final GWTJahiaNodeACE ace) {
        Button button = new Button();
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.restore());
        button.setToolTip(getResource("org.jahia.engines.rights.ManageRights.restoreInheritance.label"));
        button.setEnabled(!readOnly && !ace.isHidden());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                setDirty();
                Log.debug("restore" + ace.getPermissions());
                ace.getPermissions().clear();
//                ace.getPermissions().putAll(ace.getInheritedPermissions());
                int row = store.indexOf(item);
                for (int i = 3; i < displayedRoles.size() + 3; i++) {
                    CheckBox chb = (CheckBox) grid.getView().getWidget(row, i);
                    String perm = grid.getColumnModel().getColumn(i).getId();
                    Boolean v = ace.getInheritedPermissions().get(perm);
                    chb.setValue(Boolean.TRUE.equals(v));
                }
                ace.setInherited(true);
                LayoutContainer ctn = (LayoutContainer) grid.getView().getWidget(row, 2);
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
        restoreButton.setEnabled(false);
        acl = originalAcl.cloneObject();

        // Clean ACL from roles that are not displayed here
        List<GWTJahiaNodeACE> aces = new ArrayList<GWTJahiaNodeACE>(acl.getAce());
        for (GWTJahiaNodeACE ace : aces) {
            ace.getPermissions().keySet().retainAll(displayedRoles);
            ace.getInheritedPermissions().keySet().retainAll(displayedRoles);
            if (ace.getPermissions().isEmpty()) {
                if (ace.getInheritedPermissions().isEmpty()) {
                    acl.getAce().remove(ace);
                } else {
                    ace.setInherited(true);
                }
            }
        }
    }

    /**
     * Set dirty
     */
    public void setDirty() {
        restoreButton.setEnabled(true);
    }

    public List<String> getDisplayedRoles() {
        return displayedRoles;
    }

    /**
     * Add ace to acl table
     *
     * @param store
     * @param ace
     * @param available
     */
    private void addTableItem(ListStore<ModelData> store, GWTJahiaNodeACE ace, List<String> available) {
        if (ace.getPermissions().isEmpty() && !ace.getInheritedPermissions().isEmpty() && acl.isBreakAllInheritance()) {
            return;
        }
        if (ace.getPermissions().isEmpty() && ace.getInheritedPermissions().isEmpty()) {
            return;
        }
        BaseModelData value = new BaseModelData();//Object[3 + available.size()];
        value.set("principal", ace.getPrincipalDisplayName());

        String o = ace.getPrincipalType() + ace.getPrincipalKey();
        if (!items.contains(o)) {
            items.add(o);
            value.set("ace", ace);

            store.add(value);
            store.sort("principal", Style.SortDir.ASC);
        }
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
     *
     * @return
     */
    public boolean isEmpty() {
        return getAcl() == null || getAcl().getAvailablePermissions() == null || getAcl().getAvailablePermissions().isEmpty();
    }

    /**
     * Restore the properties retrieved by the last server call.
     */
    private class RestoreButton extends Button {
        public RestoreButton() {
            super(getResource("label.restore"));
            setIcon(StandardIconsProvider.STANDARD_ICONS.restore());
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
