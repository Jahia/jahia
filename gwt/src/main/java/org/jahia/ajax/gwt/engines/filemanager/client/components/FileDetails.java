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

package org.jahia.ajax.gwt.engines.filemanager.client.components;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.button.Button;
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

import org.jahia.ajax.gwt.aclmanagement.client.model.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.definitions.client.model.GWTJahiaNodeType;
import org.jahia.ajax.gwt.definitions.client.ContentDefinitionService;
import org.jahia.ajax.gwt.definitions.client.ContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.definitions.client.ui.PropertiesEditor;
import org.jahia.ajax.gwt.commons.client.ui.AsyncTabItem;
import org.jahia.ajax.gwt.commons.client.util.Formatter;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeService;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaNode;
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.filemanagement.client.util.Resources;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.BottomRightComponent;
import org.jahia.ajax.gwt.aclmanagement.client.ui.AclEditor;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 23 juin 2008 - 16:15:46
 */
public class FileDetails extends BottomRightComponent {

    private ContentPanel m_component;
    private AsyncTabItem info;
    private AsyncTabItem properties;
    private AsyncTabItem acl;
    private AsyncTabItem usages;
//    private AsyncTabItem image;
    private FlowPanel infoPanel;
//    private FlowPanel imagePanel;
    private TabPanel tabs;
    private AclEditor aclEditor;

    private List<GWTJahiaNode> selectedNodes = null;


    private final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance();
    private final ContentDefinitionServiceAsync cDefService = ContentDefinitionService.App.getInstance();

    public FileDetails() {
        super();
        m_component = new ContentPanel(new FitLayout());
        m_component.setBodyBorder(false);
        m_component.setBorders(true);
        tabs = new TabPanel();
        tabs.setBodyBorder(false);
        tabs.setBorders(false);
        info = new AsyncTabItem();
//        image = new AsyncTabItem();
        properties = new AsyncTabItem();
        acl = new AsyncTabItem();
        usages = new AsyncTabItem();
        infoPanel = new FlowPanel();
        infoPanel.addStyleName("infoPane");
//        imagePanel = new FlowPanel();
//        imagePanel.addStyleName("infoPane");
        info.setText(Resources.getResource("fm_information"));
        info.add(infoPanel);
//        image.setText("Image");
//        image.add(imagePanel);
        properties.setLayout(new FitLayout());
        properties.setText(Resources.getResource("fm_properties"));
        acl.setLayout(new FitLayout());
        acl.setText(Resources.getResource("fm_authorizations"));
        usages.setText(Resources.getResource("fm_usages"));
        usages.setLayout(new FitLayout());
        tabs.add(info);
        tabs.add(properties);
        tabs.add(acl);
        tabs.add(usages);
//        tabs.add(image);
        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (selectedNodes != null && selectedNodes.size() > 0) {
                    fillCurrentTab();
                }
            }
        });

        m_component.add(tabs);
    }


    public void clear() {
        m_component.setHeading("&nbsp;");
        infoPanel.clear();
//        imagePanel.clear();
        properties.removeAll();
        acl.removeAll();
        usages.removeAll();
        selectedNodes = null;
        info.setProcessed(false);
//        image.setProcessed(false);
        properties.setProcessed(false);
        acl.setProcessed(false);
        usages.setProcessed(false);
    }

    public void fillData(Object selectedItem) {
        clear();
        if (selectedItem != null) {
            selectedNodes = (List<GWTJahiaNode>) selectedItem;
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
                info.setEnabled(true);
                properties.setEnabled(true);
                acl.setEnabled(true);
                usages.setEnabled(true);
//                image.setEnabled(selectedNodes.get(0).getNodeTypes().contains("jmix:image"));
            } else if (selectedNodes.size() > 1) {
                info.setEnabled(true);
                properties.setEnabled(true);
                acl.setEnabled(false);
                usages.setEnabled(false);
//                image.setEnabled(false);
            }

            fillCurrentTab();

        }
    }

    private void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();
        if (!currentTab.isEnabled()) {
            currentTab = info;
            tabs.setSelection(currentTab);
        }
        if (currentTab == info) {
            displayInfo();
        } else if (currentTab == properties) {
            displayProperties();
        } else if (currentTab == acl) {
            displayAcl();
        } else if (currentTab == usages) {
            displayFileUsages();
//        } else if (currentTab == image) {
//            displayImageCrop();
        }
    }

    public Component getComponent() {
        return m_component;
    }

    // INFO TAB

    public void displayInfo() {
        if (!info.isProcessed()) {
            Grid g = new Grid(1, 2);
            g.setCellSpacing(10);
            FlowPanel flowPanel = new FlowPanel();

            if (selectedNodes.size() == 1) {
                final GWTJahiaNode selectedNode = selectedNodes.get(0);

                if (!info.isProcessed()) {

                    String preview = selectedNode.getPreview();
                    if (preview != null) {
                        g.setWidget(0, 0, new Image(preview));
                    }
                    String name = selectedNode.getName();
                    if (name != null) {
                        flowPanel.add(new HTML("<b>" + Resources.getResource("fm_info_name") + " :</b> " + name));
                    }
                    String path = selectedNode.getPath();
                    if (path != null) {
                        flowPanel.add(new HTML("<b>" + Resources.getResource("fm_info_path") + " :</b> " + path));
                    }
                    if (selectedNode.isFile()) {
                        Long s = selectedNode.getSize();
                        if (s != null) {
                            flowPanel.add(new HTML("<b>" + Resources.getResource("fm_info_size") + " :</b> " + Formatter.getFormattedSize(s.longValue()) + " (" + s.toString() + " bytes)"));
                        }
                    }
                    Date date = selectedNode.getDate();
                    if (date != null) {
                        flowPanel.add(new HTML("<b>" + Resources.getResource("fm_info_lastModif") + " :</b> " + Formatter.getFormattedDate(date, "d/MM/y")));
                    }
                    if (selectedNode.isLocked() && selectedNode.getLockOwner() != null) {
                        flowPanel.add(new HTML("<b>" + Resources.getResource("fm_info_lock") + " :</b> " + selectedNode.getLockOwner()));
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
                flowPanel.add(new HTML("<b>" + Resources.getResource("fm_info_nbFiles") + " :</b> " + numberFiles));
                flowPanel.add(new HTML("<b>" + Resources.getResource("fm_info_nbFolders") + " :</b> " + numberFolders));
                flowPanel.add(new HTML("<b>" + Resources.getResource("fm_info_totalSize") + " :</b> " + Formatter.getFormattedSize(size)));
            }
            g.setWidget(0, 1, flowPanel);
            infoPanel.add(g);
            info.setProcessed(true);
        }

    }
    // image Crop TAB

//    public void displayImageCrop() {
//        if (!image.isProcessed()) {
//            final GWTJahiaNode selectedNode = selectedNodes.get(0);
//            imagePanel.add(new Button("Crop", new SelectionListener<ComponentEvent>() {
//                public void componentSelected(ComponentEvent event) {
//                    new ImageCrop(getLinker(),selectedNode).show();
//                }
//            }));
//            imagePanel.add(new Button("Resize", new SelectionListener<ComponentEvent>() {
//                public void componentSelected(ComponentEvent event) {
//                    new ImageResize(getLinker(),selectedNode).show();
//                }
//            }));
//            imagePanel.add(new Button("Rotate", new SelectionListener<ComponentEvent>() {
//                public void componentSelected(ComponentEvent event) {
//                    new ImageRotate(getLinker(),selectedNode).show();
//                }
//            }));
//            image.setProcessed(true);
//        }
//
//    }
    // PROPERTIES TAB

    public void displayProperties() {


        if (!properties.isProcessed()) {
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
                        TextToolItem item = new TextToolItem(Resources.getResource("fm_save"), "fm-save");
                        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                JahiaNodeService.App.getInstance().saveProperties(elements, propertiesEditor.getProperties(), new AsyncCallback() {
                                    public void onFailure(Throwable throwable) {
                                        Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage()) ;
                                        Log.error("failed",throwable);
                                    }

                                    public void onSuccess(Object o) {
                                             getLinker().refreshTable();
                                    }
                                });
                            }
                        });
                        toolBar.add(item);
                        item = new TextToolItem(Resources.getResource("fm_restore"), "fm-restore");
                        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                propertiesEditor.resetForm();
                            }
                        });
                        toolBar.add(item);
                        toolBar.setVisible(true);
                        properties.add(propertiesEditor);

                        m_component.layout();

                        properties.setProcessed(true);
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
                        TextToolItem item = new TextToolItem(Resources.getResource("fm_save"), "fm-save");
                        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                JahiaNodeService.App.getInstance().saveProperties(selectedNodes, propertiesEditor.getProperties(), new AsyncCallback() {
                                    public void onFailure(Throwable throwable) {
                                        Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage()) ;
                                        Log.error("failed",throwable);
                                    }

                                    public void onSuccess(Object o) {

                                    }
                                });
                            }
                        });
                        toolBar.add(item);
                        item = new TextToolItem(Resources.getResource("fm_restore"), "fm-restore");
                        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                propertiesEditor.resetForm();
                            }
                        });
                        toolBar.add(item);
                        toolBar.setVisible(true);

                        properties.add(propertiesEditor);

                        m_component.layout();
                        properties.setProcessed(true);
                        if (getLinker() != null) {
                            getLinker().loaded();
                        }
                    }
                });
            }
        }
    }

    // ACL TAB
    private void renderAcl() {
        acl.add(aclEditor.renderNewAclPanel());
        acl.layout();
    }

    public void rerenderAcl() {
        if (aclEditor != null) {
            acl.removeAll();
            renderAcl();
        }
    }

    public void displayAcl() {
        if (selectedNodes.size() == 1) {
            final GWTJahiaNode selectedNode = selectedNodes.get(0);
            if (!acl.isProcessed()) {
//                service.getFileManagerUrl(new AsyncCallback<String>() {
//                    public void onFailure(Throwable throwable) {
//                        Log.debug("Cannot retrieve acl", throwable);
//                    }
//
//                    public void onSuccess(String s) {
//                        acl.add(new Frame(s + "?screen=info&tab=rightsMgmt&path=" + selectedNode.getPath() + "&source=fileManager"));
//                        acl.layout();
//                        acl.setProcessed(true);
//                    }
//                });
//
                if (getLinker() != null) {
                    getLinker().loading("collecting acls...");
                }
                service.getACL(selectedNode.getPath(), new AsyncCallback<GWTJahiaNodeACL>() {

                    public void onFailure(Throwable throwable) {
                        Log.debug("Cannot retrieve acl", throwable);
                    }

                    public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
                        aclEditor = new AclEditor(gwtJahiaNodeACL, false);
                        aclEditor.setCanBreakInheritance(false);
                        aclEditor.setReadOnly(!selectedNode.isWriteable());
                        Button saveButton = aclEditor.getSaveButton();

                        // add selection lister on save button
                        saveButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                Log.debug("save");
                                JahiaNodeService.App.getInstance().setACL(selectedNode.getPath(), aclEditor.getAcl(), new AsyncCallback() {
                                    public void onFailure(Throwable throwable) {
                                        Log.error("acl save failed", throwable);
                                    }

                                    public void onSuccess(Object o) {
                                        aclEditor.setSaved();
                                        getLinker().getTopRightObject().refresh();
                                    }
                                });

                            }
                        });

                        renderAcl();
                        acl.setProcessed(true);
                        if (getLinker() != null) {
                            getLinker().loaded();
                        }
                    }
                });
            }
        }
    }

    // FILE USAGES TAB
    public void displayFileUsages() {
        if (!usages.isProcessed()) {

            List<TableColumn> columns = new ArrayList<TableColumn>();

            TableColumn col = new TableColumn(Resources.getResource("fm_page"), .30f);
            columns.add(col);
            col = new TableColumn("URL", .50f);
            columns.add(col);
            col = new TableColumn(Resources.getResource("fm_language"), .10f);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            columns.add(col);
            col = new TableColumn(Resources.getResource("fm_workflow"), .10f);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            columns.add(col);

            TableColumnModel cm = new TableColumnModel(columns);
            final Table tbl = new Table(cm);
            usages.add(tbl);
            usages.setProcessed(true);
            usages.layout();

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
                            String [] ws = new String[] {Resources.getResource("fm_versioned"),Resources.getResource("fm_live"),Resources.getResource("fm_staging"), Resources.getResource("fm_notify")};
                            String [] images = new String[] {"600", "111", "211", "220"};
                            values[3] = "<img src=\"../images/icons/workflow/" + images[gwtJahiaNodeUsage.getWorkflow()] + ".png\">&nbsp;"+ws[gwtJahiaNodeUsage.getWorkflow()] ;
                            TableItem item = new TableItem(values);
                            tbl.add(item);
                        }
                        tbl.addTableListener(new TableListener() {
                            @Override
                            public void tableRowDoubleClick(TableEvent tableEvent) {
                                Object url = tableEvent.item.getValue(1) ;
                                if (url != null && url instanceof String) {
                                    Window.open((String)url, "_blank", "") ;
                                }
                            }
                        });
                    }
                });
            }
        }
    }

}
