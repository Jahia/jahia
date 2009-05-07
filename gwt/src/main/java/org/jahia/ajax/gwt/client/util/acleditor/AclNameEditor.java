/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util.acleditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jahia.ajax.gwt.client.messages.Messages;
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
    protected static String aclContext = "currentSite";

    public static void initACLNameEditors() {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final RootPanel panel = RootPanel.get("gwtaclnameeditor-" + i);
            if (panel != null) {
                String heightAttribute = DOM.getElementAttribute(panel.getElement(), "height");
                final String height = heightAttribute.length() > 0 ? heightAttribute : "200px";
                final int aclId = Integer.parseInt(DOM.getElementAttribute(panel.getElement(), "aclid"));
                final String acl = DOM.getElementAttribute(panel.getElement(), "aclContext");
                if (acl != null && acl.length()>0) {
                    aclContext = acl;
                }
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

    private static String getMessage(String key) {
        return Messages.getResource(key);
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

        TableColumn col = new TableColumn("", .10f);
        columns.add(col);

        col = new TableColumn(getMessage("ae_principal"), .75f);
        columns.add(col);

        col = new TableColumn("local", "", .15f);
        col.setRenderer(new CellRenderer<TableItem>() {
            public String render(final TableItem item, final String property, final Object value) {
                final GWTJahiaNodeACE ace = (GWTJahiaNodeACE) value;
                Button button = new Button(getMessage("ae_remove"));
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
        TextToolItem addUser = new TextToolItem(getMessage("um_adduser"), "um-adduser");
        addUser.setEnabled(!readonly);
        toolBar.add(addUser);
        TextToolItem addGroup = new TextToolItem(getMessage("um_addgroup"), "um-addgroup");
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
                    new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_USERS, aclContext);
                }
            });
            addGroup.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    new UserGroupSelect(userGroupAdder, UserGroupSelect.VIEW_GROUPS, aclContext);
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
