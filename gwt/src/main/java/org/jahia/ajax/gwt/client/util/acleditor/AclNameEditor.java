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

package org.jahia.ajax.gwt.client.util.acleditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jahia.ajax.gwt.client.service.acl.ACLService;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.CellRenderer;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.table.TableItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Used for editing permissions by adding users/groups into the list of allowed
 * principals.
 * 
 * @author Sergiy Shyrkov
 */
public class AclNameEditor {

    private GWTJahiaNodeACL acl;
    private String fieldId;
    private List<String> items;
    private Table tbl;
    private boolean readonly;

    public static void initACLNameEditors() {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final RootPanel panel = RootPanel.get("gwtaclnameeditor-" + i);
            if (panel != null) {
                String heightAttribute = DOM.getElementAttribute(panel.getElement(), "height");
                final String height = heightAttribute.length() > 0 ? heightAttribute : "200px";
                final int aclId = Integer.parseInt(DOM.getElementAttribute(panel.getElement(), "aclid"));
                ACLService.App.getInstance().getACL(aclId, new AsyncCallback<GWTJahiaNodeACL>() {
                    public void onFailure(Throwable caught) {
                        Log.error("ACL load failed for the ID '" + aclId + "'", caught);
                    }

                    public void onSuccess(GWTJahiaNodeACL result) {
                        ContentPanel aclPanel = new AclNameEditor(result, DOM.getElementAttribute(panel.getElement(), "fieldid"), "true".equals(DOM.getElementAttribute(panel.getElement(), "readonly"))).renderNewAclPanel();
                        aclPanel.setHeight(height);
                        panel.add(aclPanel);
                    }
                });
            } else {
                break;
            }
        }
    }

    public AclNameEditor(GWTJahiaNodeACL acl, String fieldId) {
        this(acl, fieldId, false);
    }

    public AclNameEditor(GWTJahiaNodeACL acl, String fieldId, boolean readonly) {
        super();
        this.acl = acl;
        this.fieldId = fieldId;
        this.readonly = readonly;
    }

    private void addTableItem(Table tbl, GWTJahiaNodeACE ace) {
        if (ace.isInherited() && acl.isBreakAllInheritance()) {
            return;
        }
        Object[] values = new Object[3];
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
            updateField();
            values[2] = ace;
            TableItem item = new TableItem(values);
            tbl.add(item);
        }
    }

    public ContentPanel renderNewAclPanel() {
        final List<TableColumn> columns = new ArrayList<TableColumn>();

        TableColumn col = new TableColumn("Type", .10f);
        columns.add(col);

        col = new TableColumn("Principal", .75f);
        columns.add(col);

        col = new TableColumn("local", "", .15f);
        col.setRenderer(new CellRenderer<TableItem>() {
            public String render(final TableItem item, final String property, final Object value) {
                final GWTJahiaNodeACE ace = (GWTJahiaNodeACE) value;
                Button button = new Button("remove");
                button.setEnabled(!readonly);
                if (!readonly) {
                    button.addSelectionListener(new SelectionListener<ComponentEvent>() {
                        public void componentSelected(ComponentEvent event) {
                            String o = ace.getPrincipalType() + ace.getPrincipal();
                            items.remove(o);
                            updateField();
                            tbl.remove(item);
                            acl.getAce().remove(ace);
                        }
                    });
                }
                item.setWidget(2, button);
                return "";
            }
        });

        columns.add(col);

        TableColumnModel cm = new TableColumnModel(columns);
        tbl = new Table(cm);
        tbl.setBulkRender(false);
        tbl.sort(1, Style.SortDir.ASC);
        items = new ArrayList<String>();

        List<GWTJahiaNodeACE> l = acl.getAce();
        for (GWTJahiaNodeACE ace : l) {
            addTableItem(tbl, ace);
        }

        ContentPanel panel = new ContentPanel();
        panel.setLayout(new FitLayout());
        panel.setCollapsible(false);
        panel.setFrame(false);
        panel.setAnimCollapse(false);
        panel.setBorders(false);
        panel.add(tbl);

        ToolBar toolBar = new ToolBar();
        TextToolItem addUser = new TextToolItem("Add user", "um-adduser");
        addUser.setEnabled(!readonly);
        toolBar.add(addUser);
        TextToolItem addGroup = new TextToolItem("Add group", "um-addgroup");
        addGroup.setEnabled(!readonly);
        toolBar.add(addGroup);
        
        if (!readonly) {
            final UserGroupAdder userGroupAdder = new UserGroupAdder() {
                public void addGroups(List<GWTJahiaGroup> groups) {
                    for (GWTJahiaGroup group : groups) {
                        GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
                        ace.setPrincipalType('g');
                        ace.setPrincipal(group.getGroupname());
                        ace.setPermissions(new HashMap<String, String>());
                        ace.setInheritedPermissions(new HashMap<String, String>());
                        ace.setInherited(false);
                        acl.getAce().add(ace);
                        addTableItem(tbl, ace);
                    }
                }
    
                public void addUsers(List<GWTJahiaUser> users) {
                    for (GWTJahiaUser user : users) {
                        GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
                        ace.setPrincipalType('u');
                        ace.setPrincipal(user.getUsername());
                        ace.setPermissions(new HashMap<String, String>());
                        ace.setInheritedPermissions(new HashMap<String, String>());
                        ace.setInherited(false);
                        acl.getAce().add(ace);
                        addTableItem(tbl, ace);
                    }
                }
            };
            addUser.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    Log.debug("add");
                    new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_USERS, false);
                }
            });
            addGroup.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    Log.debug("add");
                    new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_GROUPS, false);
                }
            });
        }
        panel.setTopComponent(toolBar);

        panel.setHeaderVisible(false);
        return panel;
    }

    private void updateField() {
        if (fieldId.length() == 0) {
            return;
        }
        Element field = DOM.getElementById(fieldId);
        if (field != null) {
            StringBuffer value = new StringBuffer(64);
            for (String item : items) {
                value.append(item).append(",");
            }
            if (items.size() > 0) {
                value.deleteCharAt(value.length() - 1);
            }
            field.setAttribute("value", value.toString());
        }
    }
}
