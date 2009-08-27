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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.content.versioning.VersioningPanel;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 23 juin 2008 - 16:15:46
 */
public class ContentDetails extends BottomRightComponent {
    private ManagerConfiguration config;
    private ContentPanel m_component;
    private AsyncTabItem infoTabItem;
    private AsyncTabItem propertiesTabItem;
    private AsyncTabItem portletsTabItem;
    private AsyncTabItem authorizationsTabItem;
    private AsyncTabItem rolesTabItem;
    private AsyncTabItem modesTabItem;
    private AsyncTabItem usagesTabItem;
    private AsyncTabItem versioningTabItem;
    private FlowPanel infoPanel;
    private TabPanel tabs;
    private AclEditor modeAclEditor;
    private AclEditor roleAclEditor;
    private AclEditor authAclEditor;
    private static int ROLES_TAB_ITEM = 1;
    private static int MODES_TAB_ITEM = 2;
    private static int AUTH_TAB_ITEM = 3;

    private List<GWTJahiaNode> selectedNodes = null;


    private final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
    private final JahiaContentDefinitionServiceAsync cDefService = JahiaContentDefinitionService.App.getInstance();

    public ContentDetails(ManagerConfiguration config) {
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
        
        // portlets
        portletsTabItem = new AsyncTabItem();
        portletsTabItem.setLayout(new FitLayout());
        portletsTabItem.setText(Messages.get("fm_portlets", "Portlets"));

        // roles
        rolesTabItem = new AsyncTabItem();
        rolesTabItem.setLayout(new FitLayout());
        rolesTabItem.setText(Messages.getResource("fm_roles"));
        rolesTabItem.setEnabled(false);

        // modes
        modesTabItem = new AsyncTabItem();
        modesTabItem.setLayout(new FitLayout());
        modesTabItem.setText(Messages.getResource("fm_modes"));
        modesTabItem.setEnabled(false);

        // authorizations
        authorizationsTabItem = new AsyncTabItem();
        authorizationsTabItem.setLayout(new FitLayout());
        authorizationsTabItem.setText(Messages.getResource("fm_authorizations"));

        // usage
        usagesTabItem = new AsyncTabItem();
        usagesTabItem.setText(Messages.getResource("fm_usages"));
        usagesTabItem.setLayout(new FitLayout());

        // versions
        versioningTabItem = new AsyncTabItem();
        versioningTabItem.setText("Versioning");
        versioningTabItem.setLayout(new FitLayout());

        // add all tabs
        tabs.add(infoTabItem);
        tabs.add(propertiesTabItem);
        if (config.getTabs().contains(JCRClientUtils.ROLES_ACL)) {
            tabs.add(rolesTabItem);
        }
        if (config.getTabs().contains("portlets")) {
            tabs.add(portletsTabItem);
        }
        if (config.getTabs().contains(JCRClientUtils.MODES_ACL)) {
            tabs.add(modesTabItem);
        }
        tabs.add(authorizationsTabItem);
        tabs.add(usagesTabItem);
        tabs.add(versioningTabItem);

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
        portletsTabItem.removeAll();
        rolesTabItem.removeAll();
        modesTabItem.removeAll();
        authorizationsTabItem.removeAll();
        usagesTabItem.removeAll();
        versioningTabItem.removeAll();
        selectedNodes = null;
        infoTabItem.setProcessed(false);
        propertiesTabItem.setProcessed(false);
        portletsTabItem.setProcessed(false);
        rolesTabItem.setProcessed(false);
        modesTabItem.setProcessed(false);
        authorizationsTabItem.setProcessed(false);
        usagesTabItem.setProcessed(false);
        versioningTabItem.setProcessed(false);
    }

    public void fillData(Object selectedItem) {
        clear();
        if (selectedItem != null) {
            if (selectedItem instanceof GWTJahiaNode) {
                selectedNodes = new ArrayList<GWTJahiaNode>();
                selectedNodes.add((GWTJahiaNode) selectedItem);
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
                portletsTabItem.setEnabled(true);
                if (selectedNodes.get(0).isPortlet()) {
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
                portletsTabItem.setEnabled(false);
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
        } else if (currentTab == portletsTabItem) {
            displayPortlets();
        } else if (currentTab == rolesTabItem) {
            displayRoles();
        } else if (currentTab == modesTabItem) {
            displayModes();
        } else if (currentTab == authorizationsTabItem) {
            displayAuthorization();
        } else if (currentTab == usagesTabItem) {
            displayFileUsages();
        } else if (currentTab == versioningTabItem) {
            displayVersioning();
        }
    }


    private void displayPortlets() {
        if (!portletsTabItem.isProcessed() && selectedNodes.size() == 1) {

            final ListStore<GWTJahiaPortletDefinition> store = new ListStore<GWTJahiaPortletDefinition>();
            JahiaContentManagementService.App.getInstance().searchPortlets(selectedNodes.get(0).getUUID(), new AsyncCallback<List<GWTJahiaPortletDefinition>>() {
                public void onSuccess(List<GWTJahiaPortletDefinition> result) {
                    store.add(result);
                }

                public void onFailure(Throwable caught) {
                	// do nothing
                }
            });

            List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
            columns.add(new ColumnConfig("displayName", Messages.getNotEmptyResource("mw_name", "Name"), 170));
            columns.add(new ColumnConfig("description", Messages.getNotEmptyResource("mw_description", "Description"), 330));

            ColumnModel cm = new ColumnModel(columns);
            com.extjs.gxt.ui.client.widget.grid.Grid<GWTJahiaPortletDefinition> grid = new com.extjs.gxt.ui.client.widget.grid.Grid<GWTJahiaPortletDefinition>(store, cm);
            grid.setBorders(true);

            ContentPanel panel = new ContentPanel();
            panel.setLayout(new FitLayout());
            panel.setHeaderVisible(false);
            panel.setBodyBorder(false);
            panel.setBorders(false);
            panel.setFrame(false);
            panel.setCollapsible(false);
            panel.setButtonAlign(Style.HorizontalAlignment.CENTER);
            panel.add(grid);
            portletsTabItem.add(panel);
            portletsTabItem.setProcessed(true);
            portletsTabItem.layout();
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
                        final PropertiesEditor propertiesEditor = new PropertiesEditor(result.getNodeTypes(), result.getProperties(), false, true, null, list, null);

                        ToolBar toolBar = (ToolBar) propertiesEditor.getTopComponent();
                        Button item = new Button(Messages.getResource("fm_save"));
                        item.setIconStyle("gwt-icons-save");
                        item.setEnabled(selectedNode.isWriteable() && !selectedNode.isLocked());
                        item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            public void componentSelected(ButtonEvent event) {
                                JahiaContentManagementService.App.getInstance().saveProperties(elements, propertiesEditor.getProperties(), new AsyncCallback() {
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
                        item = new Button(Messages.getResource("fm_restore"));
                        item.setIconStyle("gwt-icons-restore");
                        item.setEnabled(selectedNode.isWriteable() && !selectedNode.isLocked());

                        item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            public void componentSelected(ButtonEvent event) {
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

                boolean writeable = true;
                for (GWTJahiaNode selectedNode : selectedNodes) {
                    for (String nodeType : selectedNode.getNodeTypes()) {
                        if (!nodeTypes.contains(nodeType)) {
                            nodeTypes.add(nodeType);
                        }
                    }
                    writeable &= selectedNode.isWriteable() && !selectedNode.isLocked();
                }
                final boolean w = writeable;
                cDefService.getNodeTypes(nodeTypes, new AsyncCallback<List<GWTJahiaNodeType>>() {
                    public void onFailure(Throwable throwable) {
                        Log.debug("Cannot get properties", throwable);
                    }

                    public void onSuccess(List<GWTJahiaNodeType> gwtJahiaNodeTypes) {
                        final PropertiesEditor propertiesEditor = new PropertiesEditor(gwtJahiaNodeTypes, true, true);

                        ToolBar toolBar = (ToolBar) propertiesEditor.getTopComponent();
                        Button item = new Button(Messages.getResource("fm_save"));
                        item.setEnabled(w);
                        item.setIconStyle("fm-save");
                        item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            public void componentSelected(ButtonEvent event) {
                                JahiaContentManagementService.App.getInstance().saveProperties(selectedNodes, propertiesEditor.getProperties(), new AsyncCallback() {
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
                        item = new Button(Messages.getResource("fm_restore"));
                        item.setEnabled(w);
                        item.setIconStyle("fm-restore");
                        item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            public void componentSelected(ButtonEvent event) {
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
                        roleAclEditor = new AclEditor(gwtJahiaNodeACL, selectedNode.getAclContext(), false);
                        roleAclEditor.setAddGroupsLabel(Messages.getNotEmptyResource("fm_addgroup_roles", "Add group-role mapping"));
                        roleAclEditor.setAddUsersLabel(Messages.getNotEmptyResource("fm_adduser_roles", "Add user-role mapping"));
                        roleAclEditor.setAclGroup(JCRClientUtils.ROLES_ACL);
                        roleAclEditor.setCanBreakInheritance(false);
                        roleAclEditor.setReadOnly(!selectedNode.isWriteable() || selectedNode.isLocked());
                        Button saveButton = roleAclEditor.getSaveButton();
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
                        modeAclEditor = new AclEditor(gwtJahiaNodeACL, selectedNode.getAclContext(), false);
                        modeAclEditor.setAddGroupsLabel(Messages.getNotEmptyResource("fm_addgroup_modes", "Add group-mode mapping"));
                        modeAclEditor.setAddUsersLabel(Messages.getNotEmptyResource("fm_adduser_modes", "Add user-mode mapping"));
                        modeAclEditor.setAclGroup(JCRClientUtils.MODES_ACL);
                        modeAclEditor.setCanBreakInheritance(false);
                        modeAclEditor.setReadOnly(!selectedNode.isWriteable() || selectedNode.isLocked());
                        Button saveButton = modeAclEditor.getSaveButton();
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
                        authAclEditor = new AclEditor(gwtJahiaNodeACL, selectedNode.getAclContext());
                        authAclEditor.setAclGroup(JCRClientUtils.AUTHORIZATIONS_ACL);
                        authAclEditor.setCanBreakInheritance(false);
                        if(!(selectedNode.getProviderKey().equals("default")||selectedNode.getProviderKey().equals("jahia"))) {
                            authAclEditor.setReadOnly(true);
                        } else {
                            authAclEditor.setReadOnly(!selectedNode.isWriteable() || selectedNode.isLocked());
                        }
                        Button saveButton = authAclEditor.getSaveButton();
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

            List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

            ColumnConfig col = new ColumnConfig("pageTitle", Messages.getResource("fm_page"), 100);
            columns.add(col);
            col = new ColumnConfig("url", "URL", 150);
            columns.add(col);
            col = new ColumnConfig("lang", Messages.getResource("fm_language"), 30);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            columns.add(col);
            col = new ColumnConfig(Messages.getResource("fm_workflow"), 30);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            col.setRenderer(new GridCellRenderer<GWTJahiaNodeUsage>() {
                public Object render(GWTJahiaNodeUsage gwtJahiaNodeUsage, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNodeUsage> gwtJahiaNodeUsageListStore, com.extjs.gxt.ui.client.widget.grid.Grid<GWTJahiaNodeUsage> gwtJahiaNodeUsageGrid) {
                    String[] ws = new String[]{Messages.getResource("fm_versioned"), Messages.getResource("fm_live"), Messages.getResource("fm_staging"), Messages.getResource("fm_notify")};
                    String[] images = new String[]{"600", "111", "121", "130"};
                    return "<img src=\"../images/icons/workflow/" + images[gwtJahiaNodeUsage.getWorkflow()] + ".png\">&nbsp;" + ws[gwtJahiaNodeUsage.getWorkflow()];
                }
            });
            columns.add(col);
            col = new ColumnConfig("versionName", Messages.getResource("fm_version"), 30);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            columns.add(col);

            ColumnModel cm = new ColumnModel(columns);
            final ListStore<GWTJahiaNodeUsage> usageStore = new ListStore<GWTJahiaNodeUsage>();
            final com.extjs.gxt.ui.client.widget.grid.Grid<GWTJahiaNodeUsage> tbl = new com.extjs.gxt.ui.client.widget.grid.Grid<GWTJahiaNodeUsage>(usageStore, cm);
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
                        usageStore.add(gwtJahiaNodeUsages);
                        tbl.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
                            public void handleEvent(GridEvent tableEvent) {
                                Object url = tableEvent.getModel().get("url");
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

    protected class SaveAclSelectionListener extends SelectionListener<ButtonEvent> {
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

        public void componentSelected(ButtonEvent event) {
            JahiaContentManagementService.App.getInstance().setACL(selectedNode.getPath(), acl, new AsyncCallback() {
                public void onSuccess(Object o) {
                    if (flag == ROLES_TAB_ITEM) {
                        roleAclEditor.setSaved();
                    } else if (flag == MODES_TAB_ITEM) {
                        modeAclEditor.setSaved();
                    } else if (flag == AUTH_TAB_ITEM) {
                        authAclEditor.setSaved();
                    }
                    //getLinker().getTopRightObject().refresh(); // refresh should not be needed
                }

                public void onFailure(Throwable throwable) {
                    Log.error("acl save failed", throwable);
                }


            });

        }
    }

    /**
     * Display versioning
     */
    public void displayVersioning() {
        if (selectedNodes.size() == 1) {
            final GWTJahiaNode selectedNode = selectedNodes.get(0);
            if (!versioningTabItem.isProcessed()) {
                if (selectedNode.getNodeTypes().contains("mix:versionable") || selectedNode.getNodeTypes().contains("mix:simpleVersionable")) {
                    VersioningPanel versionPanel = new VersioningPanel(selectedNode, config.isEnableFileDoubleClick()) {
                        @Override
                        public void onRowDoubleClick(GWTJahiaNodeVersion version) {
                            if (config.isEnableFileDoubleClick()) {
                                if (getSelectedNode().isDisplayable()) {
                                    ImagePopup.popImage(version.getNode());
                                } else {
                                    ContentActions.download(getLinker(), version.getNode(), version.getNode().getUrl());
                                }
                            }
                        }


                        @Override
                        public void afterRestore() {
                            getLinker().refreshTable();
                        }
                    };

                    versioningTabItem.add(versionPanel);
                } else {
                    versioningTabItem.setLayout(new FlowLayout());
                    versioningTabItem.add(new Text("File is not versioned yet"));
                    com.extjs.gxt.ui.client.widget.button.Button button = new com.extjs.gxt.ui.client.widget.button.Button("Activate versioning");

                    button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                        public void componentSelected(ButtonEvent event) {
                            List<String> list = new ArrayList<String>();
                            list.add(selectedNode.getPath());
                            service.activateVersioning(list, new AsyncCallback() {
                                public void onFailure(Throwable caught) {
                                    Window.alert("failure");
                                }

                                public void onSuccess(Object result) {
                                    selectedNode.getNodeTypes().add("mix:versionable");
                                    versioningTabItem.removeAll();
                                    versioningTabItem.setProcessed(false);
                                    versioningTabItem.setLayout(new FitLayout());
                                    TabItem currentTab = tabs.getSelectedItem();
                                    if (currentTab == versioningTabItem) {
                                        displayVersioning();
                                    }
                                }
                            });
                        }
                    });
                    versioningTabItem.add(button);
                }
                versioningTabItem.setProcessed(true);
                versioningTabItem.layout();
            }
        }
    }


}
