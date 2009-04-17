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

package org.jahia.ajax.gwt.client.widget.node;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.table.TableItem;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.event.TableListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 23 juin 2008 - 16:15:46
 */
public class FileDetails extends BottomRightComponent {
    private ManagerConfiguration config;
    private ContentPanel m_component;
    private AsyncTabItem infoTabItem;
    private AsyncTabItem propertiesTabItem;
    private AsyncTabItem authorizationsTabItem;
    private AsyncTabItem rolesTabItem;
    private AsyncTabItem modesTabItem;
    private AsyncTabItem usagesTabItem;
    private FlowPanel infoPanel;
    private TabPanel tabs;
    private AclEditor modeAclEditor;
    private AclEditor roleAclEditor;
    private AclEditor authAclEditor;
    private static int ROLES_TAB_ITEM = 1;
    private static int MODES_TAB_ITEM = 2;
    private static int AUTH_TAB_ITEM = 3;

    private List<GWTJahiaNode> selectedNodes = null;


    private final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance();
    private final ContentDefinitionServiceAsync cDefService = ContentDefinitionService.App.getInstance();

    public FileDetails(ManagerConfiguration config) {
        super();
        this.config = config;
        m_component = new ContentPanel(new FitLayout());
        m_component.setBodyBorder(false);
        m_component.setBorders(true);
        tabs = new TabPanel();
        tabs.setBodyBorder(false);
        tabs.setBorders(false);

        // info
        infoPanel = new FlowPanel();
        infoPanel.addStyleName("infoPane");
        infoTabItem = new AsyncTabItem();
        infoTabItem.setText(Messages.getResource("fm_information"));
        infoTabItem.add(infoPanel);

        // properties
        propertiesTabItem = new AsyncTabItem();
        propertiesTabItem.setLayout(new FitLayout());
        propertiesTabItem.setText(Messages.getResource("fm_properties"));

        // roles
        rolesTabItem = new AsyncTabItem();
        rolesTabItem.setLayout(new FitLayout());
        rolesTabItem.setText(Messages.getResource("fm_roles"));

        // modes
        modesTabItem = new AsyncTabItem();
        modesTabItem.setLayout(new FitLayout());
        modesTabItem.setText(Messages.getResource("fm_modes"));

        // authorizations
        authorizationsTabItem = new AsyncTabItem();
        authorizationsTabItem.setLayout(new FitLayout());
        authorizationsTabItem.setText(Messages.getResource("fm_authorizations"));

        // usage
        usagesTabItem = new AsyncTabItem();
        usagesTabItem.setText(Messages.getResource("fm_usages"));
        usagesTabItem.setLayout(new FitLayout());

        // add all tabs
        tabs.add(infoTabItem);
        tabs.add(propertiesTabItem);
        if (config.getTabs().contains(JCRClientUtils.ROLES_ACL)) {
            tabs.add(rolesTabItem);
        }
        if (config.getTabs().contains(JCRClientUtils.MODES_ACL)) {
            tabs.add(modesTabItem);
        }
        tabs.add(authorizationsTabItem);
        tabs.add(usagesTabItem);

        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (selectedNodes != null && selectedNodes.size() > 0) {
                    fillCurrentTab();
                }
            }
        });

        m_component.add(tabs);
    }

    public Component getComponent() {
        return m_component;
    }


    public void clear() {
        m_component.setHeading("&nbsp;");
        infoPanel.clear();
        propertiesTabItem.removeAll();
        rolesTabItem.removeAll();
        modesTabItem.removeAll();
        authorizationsTabItem.removeAll();
        usagesTabItem.removeAll();
        selectedNodes = null;
        infoTabItem.setProcessed(false);
        propertiesTabItem.setProcessed(false);
        rolesTabItem.setProcessed(false);
        modesTabItem.setProcessed(false);
        authorizationsTabItem.setProcessed(false);
        usagesTabItem.setProcessed(false);
    }

    public void fillData(Object selectedItem) {
        clear();
        if (selectedItem != null) {
            if (selectedItem instanceof GWTJahiaNode) {
                selectedNodes = new ArrayList<GWTJahiaNode>() ;
                selectedNodes.add((GWTJahiaNode) selectedItem) ;
            } else {
                selectedNodes = (List<GWTJahiaNode>) selectedItem;
            }
            String heading;
            if (selectedNodes.size() == 0) {
                heading = "&nbsp;";
            } else {
                heading = "";
            }
            for (GWTJahiaNode node : selectedNodes) {
                if (heading.length() + node.getName().length() < 100) {
                    heading += node.getName() + ",";
                } else {
                    heading += "... ";
                    break;
                }
            }
            if (selectedNodes.size() > 0) {
                heading = heading.substring(0, heading.length() - 1);
            }
            m_component.setHeading(heading);

            if (selectedNodes.size() == 1) {
                infoTabItem.setEnabled(true);
                propertiesTabItem.setEnabled(true);
                if (selectedNodes.get(0).isFile()) {
                    modesTabItem.setEnabled(true);
                    rolesTabItem.setEnabled(true);
                } else {
                    modesTabItem.setEnabled(false);
                    rolesTabItem.setEnabled(false);
                }
                authorizationsTabItem.setEnabled(true);
                usagesTabItem.setEnabled(true);
            } else if (selectedNodes.size() > 1) {
                infoTabItem.setEnabled(true);
                propertiesTabItem.setEnabled(true);
                modesTabItem.setEnabled(false);
                rolesTabItem.setEnabled(false);
                authorizationsTabItem.setEnabled(false);
                usagesTabItem.setEnabled(false);
            }


            fillCurrentTab();

        }
    }

    private void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();
        if (!currentTab.isEnabled()) {
            currentTab = infoTabItem;
            tabs.setSelection(currentTab);
        }
        if (currentTab == infoTabItem) {
            displayInfo();
        } else if (currentTab == propertiesTabItem) {
            displayProperties();
        } else if (currentTab == rolesTabItem) {
            displayRoles();
        } else if (currentTab == modesTabItem) {
            displayModes();
        } else if (currentTab == authorizationsTabItem) {
            displayAuthorization();
        } else if (currentTab == usagesTabItem) {
            displayFileUsages();
        }
    }


    // INFO TAB
    public void displayInfo() {
        if (!infoTabItem.isProcessed()) {
            Grid g = new Grid(1, 2);
            g.setCellSpacing(10);
            FlowPanel flowPanel = new FlowPanel();

            if (selectedNodes.size() == 1) {
                final GWTJahiaNode selectedNode = selectedNodes.get(0);

                if (!infoTabItem.isProcessed()) {

                    String preview = selectedNode.getPreview();
                    if (preview != null) {
                        g.setWidget(0, 0, new Image(preview));
                    }
                    String name = selectedNode.getDisplayName();
                    if (name != null) {
                        flowPanel.add(new HTML("<b>" + Messages.getResource("fm_info_name") + " :</b> " + name));
                    }
                    String path = selectedNode.getPath();
                    if (path != null) {
                        flowPanel.add(new HTML("<b>" + Messages.getResource("fm_info_path") + " :</b> " + path));
                    }
                    if (config.isDisplaySize() && selectedNode.isFile()) {
                        Long s = selectedNode.getSize();
                        if (s != null) {
                            flowPanel.add(new HTML("<b>" + Messages.getResource("fm_info_size") + " :</b> " + Formatter.getFormattedSize(s.longValue()) + " (" + s.toString() + " bytes)"));
                        }
                    }
                    if (config.isDisplayDate()) {
                        Date date = selectedNode.getDate();
                        if (date != null) {
                            flowPanel.add(new HTML("<b>" + Messages.getResource("fm_info_lastModif") + " :</b> " + org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate(date, "d/MM/y")));
                        }
                    }
                    if (config.isDisplayLock() && selectedNode.isLocked() && selectedNode.getLockOwner() != null) {
                        flowPanel.add(new HTML("<b>" + Messages.getResource("fm_info_lock") + " :</b> " + selectedNode.getLockOwner()));
                    }
                }
            } else {
                int numberFiles = 0;
                int numberFolders = 0;
                long size = 0;

                for (GWTJahiaNode selectedNode : selectedNodes) {
                    if (selectedNode.isFile()) {
                        numberFiles++;
                        size += selectedNode.getSize();
                    } else {
                        numberFolders++;
                    }
                }
                flowPanel.add(new HTML("<b>" + Messages.getResource("fm_info_nbFiles") + " :</b> " + numberFiles));
                flowPanel.add(new HTML("<b>" + Messages.getResource("fm_info_nbFolders") + " :</b> " + numberFolders));
                flowPanel.add(new HTML("<b>" + Messages.getResource("fm_info_totalSize") + " :</b> " + org.jahia.ajax.gwt.client.util.Formatter.getFormattedSize(size)));
            }
            g.setWidget(0, 1, flowPanel);
            infoPanel.add(g);
            infoTabItem.setProcessed(true);
        }

    }

    public void displayProperties() {
        if (!propertiesTabItem.isProcessed()) {
            if (selectedNodes.size() == 1) {
                final GWTJahiaNode selectedNode = selectedNodes.get(0);
                if (getLinker() != null) {
                    getLinker().loading("collecting properties...");
                }

                service.getProperties(selectedNode.getPath(), new AsyncCallback<GWTJahiaGetPropertiesResult>() {
                    public void onFailure(Throwable throwable) {
                        Log.debug("Cannot get properties", throwable);
                    }

                    public void onSuccess(GWTJahiaGetPropertiesResult result) {
                        final List<GWTJahiaNode> elements = new ArrayList<GWTJahiaNode>();
                        elements.add(selectedNode);

                        List<String> list = new ArrayList<String>();
                        list.add("jcr:content");
                        list.add("j:thumbnail");
                        final PropertiesEditor propertiesEditor = new PropertiesEditor(result.getNodeTypes(), result.getProperties(), false, true, list, null);

                        ToolBar toolBar = (ToolBar) propertiesEditor.getTopComponent();
                        TextToolItem item = new TextToolItem(Messages.getResource("fm_save"), "fm-save");
                        item.setIconStyle("gwt-icons-save");

                        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                JahiaNodeService.App.getInstance().saveProperties(elements, propertiesEditor.getProperties(), new AsyncCallback() {
                                    public void onFailure(Throwable throwable) {
                                        Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                                        Log.error("failed", throwable);
                                    }

                                    public void onSuccess(Object o) {
                                        Info.display("", "Properties saved");
                                        //getLinker().refreshTable();
                                    }
                                });
                            }
                        });
                        toolBar.add(new FillToolItem());
                        toolBar.add(item);
                        item = new TextToolItem(Messages.getResource("fm_restore"), "fm-restore");
                        item.setIconStyle("gwt-icons-restore");
                        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                propertiesEditor.resetForm();
                            }
                        });
                        toolBar.add(item);
                        toolBar.setVisible(true);
                        propertiesTabItem.add(propertiesEditor);

                        m_component.layout();

                        propertiesTabItem.setProcessed(true);
                        if (getLinker() != null) {
                            getLinker().loaded();
                        }
                    }
                });
            } else {
                if (getLinker() != null) {
                    getLinker().loading("collecting properties...");
                }
                List<String> nodeTypes = new ArrayList<String>();
                for (GWTJahiaNode selectedNode : selectedNodes) {
                    for (String nodeType : selectedNode.getNodeTypes()) {
                        if (!nodeTypes.contains(nodeType)) {
                            nodeTypes.add(nodeType);
                        }
                    }
                }
                cDefService.getNodeTypes(nodeTypes, new AsyncCallback<List<GWTJahiaNodeType>>() {
                    public void onFailure(Throwable throwable) {
                        Log.debug("Cannot get properties", throwable);
                    }

                    public void onSuccess(List<GWTJahiaNodeType> gwtJahiaNodeTypes) {
                        final PropertiesEditor propertiesEditor = new PropertiesEditor(gwtJahiaNodeTypes, true, false);

                        ToolBar toolBar = (ToolBar) propertiesEditor.getTopComponent();
                        TextToolItem item = new TextToolItem(Messages.getResource("fm_save"), "fm-save");
                        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                JahiaNodeService.App.getInstance().saveProperties(selectedNodes, propertiesEditor.getProperties(), new AsyncCallback() {
                                    public void onFailure(Throwable throwable) {
                                        Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                                        Log.error("failed", throwable);
                                    }

                                    public void onSuccess(Object o) {
                                        Info.display("", "Properties saved");
                                        //getLinker().refreshTable();
                                    }
                                });
                            }
                        });
                        toolBar.add(item);
                        item = new TextToolItem(Messages.getResource("fm_restore"), "fm-restore");
                        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                propertiesEditor.resetForm();
                            }
                        });
                        toolBar.add(item);
                        toolBar.setVisible(true);

                        propertiesTabItem.add(propertiesEditor);

                        m_component.layout();
                        propertiesTabItem.setProcessed(true);
                        if (getLinker() != null) {
                            getLinker().loaded();
                        }
                    }
                });
            }
        }
    }

    // ACL TAB
    private void renderRoles() {
        if (!roleAclEditor.isEmpty()) {
            rolesTabItem.setEnabled(true);
            rolesTabItem.add(roleAclEditor.renderNewAclPanel());
        } else {
            rolesTabItem.setEnabled(false);
        }
        rolesTabItem.layout();
    }

    public void rerenderRoles() {
        if (roleAclEditor != null) {
            rolesTabItem.add(roleAclEditor.renderNewAclPanel());
            rolesTabItem.layout();
        }
    }

    private void renderModes() {
        if (!modeAclEditor.isEmpty()) {
            modesTabItem.setEnabled(true);
            modesTabItem.add(modeAclEditor.renderNewAclPanel());
        } else {
            modesTabItem.setEnabled(false);
        }
        modesTabItem.layout();
    }

    public void rerenderModes() {
        if (modeAclEditor != null) {
            modesTabItem.add(modeAclEditor.renderNewAclPanel());
            modesTabItem.layout();
        }
    }

    private void renderAuthorization() {
        authorizationsTabItem.add(authAclEditor.renderNewAclPanel());
        authorizationsTabItem.layout();
    }

    public void rerenderAuthorization() {
        if (authAclEditor != null) {
            authorizationsTabItem.removeAll();
            renderAuthorization();
        }
    }


    public void displayRoles() {
        if (selectedNodes.size() == 1) {
            final GWTJahiaNode selectedNode = selectedNodes.get(0);
            if (!rolesTabItem.isProcessed()) {
                if (getLinker() != null) {
                    getLinker().loading("collecting roles acls...");
                }
                service.getACL(selectedNode.getPath(), new AsyncCallback<GWTJahiaNodeACL>() {
                    /**
                     * onsuccess
                     * @param gwtJahiaNodeACL
                     */
                    public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
                        // auth. editor
                        roleAclEditor = new AclEditor(gwtJahiaNodeACL, false, false);
                        roleAclEditor.setAddGroupsLabel(Messages.getNotEmptyResource("fm_addgroup_roles", "Add group-role mapping"));
                        roleAclEditor.setAddUsersLabel(Messages.getNotEmptyResource("fm_adduser_roles", "Add user-role mapping"));
                        roleAclEditor.setAclGroup(JCRClientUtils.ROLES_ACL);
                        roleAclEditor.setCanBreakInheritance(false);
                        roleAclEditor.setReadOnly(!selectedNode.isWriteable());
                        TextToolItem saveButton = roleAclEditor.getSaveButton();
                        saveButton.addSelectionListener(new SaveAclSelectionListener(selectedNode, ROLES_TAB_ITEM));
                        renderRoles();
                        rolesTabItem.setProcessed(true);

                        if (getLinker() != null) {
                            getLinker().loaded();
                        }
                    }

                    /**
                     * On failure
                     * @param throwable
                     */
                    public void onFailure(Throwable throwable) {
                        Log.debug("Cannot retrieve acl", throwable);
                    }

                });
            }
        }
    }

    public void displayModes() {
        if (selectedNodes.size() == 1) {
            final GWTJahiaNode selectedNode = selectedNodes.get(0);
            if (!modesTabItem.isProcessed()) {
                if (getLinker() != null) {
                    getLinker().loading("collecting modes...");
                }
                service.getACL(selectedNode.getPath(), new AsyncCallback<GWTJahiaNodeACL>() {
                    /**
                     * onsuccess
                     * @param gwtJahiaNodeACL
                     */
                    public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
                        // auth. editor
                        modeAclEditor = new AclEditor(gwtJahiaNodeACL, false, false);
                        modeAclEditor.setAddGroupsLabel(Messages.getNotEmptyResource("fm_addgroup_modes", "Add group-mode mapping"));
                        modeAclEditor.setAddUsersLabel(Messages.getNotEmptyResource("fm_adduser_modes", "Add user-mode mapping"));
                        modeAclEditor.setAclGroup(JCRClientUtils.MODES_ACL);
                        modeAclEditor.setCanBreakInheritance(false);
                        modeAclEditor.setReadOnly(!selectedNode.isWriteable());
                        TextToolItem saveButton = modeAclEditor.getSaveButton();
                        saveButton.addSelectionListener(new SaveAclSelectionListener(selectedNode, MODES_TAB_ITEM));
                        renderModes();
                        modesTabItem.setProcessed(true);

                        if (getLinker() != null) {
                            getLinker().loaded();
                        }

                    }

                    /**
                     * On failure
                     * @param throwable
                     */
                    public void onFailure(Throwable throwable) {
                        Log.debug("Cannot retrieve acl", throwable);
                    }

                });
            }
        }
    }

    public void displayAuthorization() {
        if (selectedNodes.size() == 1) {
            final GWTJahiaNode selectedNode = selectedNodes.get(0);
            if (!authorizationsTabItem.isProcessed()) {
                if (getLinker() != null) {
                    getLinker().loading("collecting authorization acls...");
                }
                service.getACL(selectedNode.getPath(), new AsyncCallback<GWTJahiaNodeACL>() {
                    /**
                     * onsuccess
                     * @param gwtJahiaNodeACL
                     */
                    public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
                        // auth. editor
                        authAclEditor = new AclEditor(gwtJahiaNodeACL, false);
                        authAclEditor.setAclGroup(JCRClientUtils.AUTHORIZATIONS_ACL);
                        authAclEditor.setCanBreakInheritance(false);
                        authAclEditor.setReadOnly(!selectedNode.isWriteable());
                        TextToolItem saveButton = authAclEditor.getSaveButton();
                        saveButton.addSelectionListener(new SaveAclSelectionListener(selectedNode, AUTH_TAB_ITEM));
                        renderAuthorization();
                        authorizationsTabItem.setProcessed(true);

                        if (getLinker() != null) {
                            getLinker().loaded();
                        }
                    }

                    /**
                     * On failure
                     * @param throwable
                     */
                    public void onFailure(Throwable throwable) {
                        Log.debug("Cannot retrieve acl", throwable);
                    }

                });
            }
        }
    }

    // FILE USAGES TAB
    public void displayFileUsages() {
        if (!usagesTabItem.isProcessed()) {

            List<TableColumn> columns = new ArrayList<TableColumn>();

            TableColumn col = new TableColumn(Messages.getResource("fm_page"), .30f);
            columns.add(col);
            col = new TableColumn("URL", .50f);
            columns.add(col);
            col = new TableColumn(Messages.getResource("fm_language"), .10f);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            columns.add(col);
            col = new TableColumn(Messages.getResource("fm_workflow"), .10f);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            columns.add(col);

            TableColumnModel cm = new TableColumnModel(columns);
            final Table tbl = new Table(cm);
            usagesTabItem.add(tbl);
            usagesTabItem.setProcessed(true);
            usagesTabItem.layout();

            for (GWTJahiaNode selectedNode : selectedNodes) {
                String path = selectedNode.getPath();

                service.getUsages(path, new AsyncCallback<List<GWTJahiaNodeUsage>>() {
                    public void onFailure(Throwable throwable) {
                        Log.debug("Cannot retrieve usages", throwable);
                    }

                    public void onSuccess(List<GWTJahiaNodeUsage> gwtJahiaNodeUsages) {

                        for (final GWTJahiaNodeUsage gwtJahiaNodeUsage : gwtJahiaNodeUsages) {
                            Object[] values = new Object[4];
                            values[0] = gwtJahiaNodeUsage.getPageTitle();
                            values[1] = gwtJahiaNodeUsage.getUrl();
                            values[2] = gwtJahiaNodeUsage.getLang();
                            String[] ws = new String[]{Messages.getResource("fm_versioned"), Messages.getResource("fm_live"), Messages.getResource("fm_staging"), Messages.getResource("fm_notify")};
                            String[] images = new String[]{"600", "111", "211", "220"};
                            values[3] = "<img src=\"../images/icons/workflow/" + images[gwtJahiaNodeUsage.getWorkflow()] + ".png\">&nbsp;" + ws[gwtJahiaNodeUsage.getWorkflow()];
                            TableItem item = new TableItem(values);
                            tbl.add(item);
                        }
                        tbl.addTableListener(new TableListener() {
                            @Override
                            public void tableRowDoubleClick(TableEvent tableEvent) {
                                Object url = tableEvent.item.getValue(1);
                                if (url != null && url instanceof String) {
                                    Window.open((String) url, "_blank", "");
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    protected class SaveAclSelectionListener extends SelectionListener<ComponentEvent> {
        private GWTJahiaNode selectedNode;
        private GWTJahiaNodeACL acl;
        private int flag;

        private SaveAclSelectionListener(GWTJahiaNode selectedNode, int flag) {
            this.selectedNode = selectedNode;
            this.flag = flag;
            if (flag == ROLES_TAB_ITEM) {
                this.acl = roleAclEditor.getAcl();
            } else if (flag == MODES_TAB_ITEM) {
                this.acl = modeAclEditor.getAcl();
            } else if (flag == AUTH_TAB_ITEM) {
                this.acl = authAclEditor.getAcl();
            }
        }

        public void componentSelected(ComponentEvent event) {
            JahiaNodeService.App.getInstance().setACL(selectedNode.getPath(), acl, new AsyncCallback() {
                public void onSuccess(Object o) {
                    if (flag == ROLES_TAB_ITEM) {
                        roleAclEditor.setSaved();
                    } else if (flag == MODES_TAB_ITEM) {
                        modeAclEditor.setSaved();
                    } else if (flag == AUTH_TAB_ITEM) {
                        authAclEditor.setSaved();
                    }
                    getLinker().getTopRightObject().refresh();
                }

                public void onFailure(Throwable throwable) {
                    Log.error("acl save failed", throwable);
                }


            });

        }
    }
}
