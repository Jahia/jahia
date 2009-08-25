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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit;


import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 24 août 2009
 */
public class EditLinker {
    private ListView<GWTJahiaNodeType> createView;
    private TreePanel<GWTJahiaNode> browseTree;
    private Grid<GWTJahiaNode> displayGrid;
    private Grid<GWTJahiaNodeType> displayTypesGrid;
    private SidePanel.PreviewTabItem previewTabItem;
    private TabItem propertiesTabItem;
    private GWTJahiaNode currentlySelectedNode;
    private String currentrySelectedTemplate;
    private Button deleteButton;
    private final EditManager editManager;
    private Button lockButton;
    private Button editButton;
    private ComboBox<GWTJahiaBasicDataBean> templateBox;
    private Button saveButton;
    private Button createPageButton;

    public EditLinker(EditManager editManager) {
        //To change body of created methods use File | Settings | File Templates.
        this.editManager = editManager;
    }

    public void setCreateView(ListView<GWTJahiaNodeType> createView) {
        this.createView = createView;
    }

    public void setBrowseTree(TreePanel<GWTJahiaNode> browseTree) {
        this.browseTree = browseTree;
    }

    public void setDisplayGrid(Grid<GWTJahiaNode> displayGrid) {
        this.displayGrid = displayGrid;
    }

    public void setDisplayTypesGrid(Grid<GWTJahiaNodeType> displayTypesGrid) {
        this.displayTypesGrid = displayTypesGrid;
    }


    public void setPreviewTabItem(SidePanel.PreviewTabItem previewTabItem) {
        this.previewTabItem = previewTabItem;
    }

    public void setPropertiesTabItem(TabItem propertiesTabItem) {
        this.propertiesTabItem = propertiesTabItem;
    }

    public void setTemplateBox(ComboBox<GWTJahiaBasicDataBean> templateBox) {
        this.templateBox = templateBox;
    }

    public void displaySelection(final GWTJahiaNode node) {
        displayPreview(node, null);
        displayProperties(node);
        currentlySelectedNode = node;
    }

    /**
     * Display the rendered html of the given node in the preview panel
     *
     * @param node the node to render
     */
    private void displayPreview(final GWTJahiaNode node,String template) {
        previewTabItem.setHtml(new HTML());
        previewTabItem.removeAll();
        if (node != null) {
            JahiaContentManagementService.App.getInstance().getRenderedContent(node.getPath(), template, false, new AsyncCallback<String>() {
                public void onSuccess(String result) {
                    HTML html = new HTML(result);
                    previewTabItem.add(html);
                    previewTabItem.setHtml(html);
                    previewTabItem.setNode(node);
                    previewTabItem.layout();

                }

                public void onFailure(Throwable caught) {
                    Log.error("", caught);
                    Window.alert("-->" + caught.getMessage());
                }
            });
        }
    }

    /**
     * Clear the properties panel and display the current node properties
     *
     * @param node the current node
     */
    private void displayProperties(final GWTJahiaNode node) {
        propertiesTabItem.removeAll();
        if (node != null) {
            JahiaContentManagementService.App.getInstance().getProperties(node.getPath(), new AsyncCallback<GWTJahiaGetPropertiesResult>() {
                public void onFailure(Throwable throwable) {
                    Log.debug("Cannot get properties", throwable);
                }

                public void onSuccess(GWTJahiaGetPropertiesResult result) {
                    final List<GWTJahiaNode> elements = new ArrayList<GWTJahiaNode>();
                    elements.add(node);

                    final PropertiesEditor propertiesEditor = new PropertiesEditor(result.getNodeTypes(), result.getProperties(), false, true, GWTJahiaItemDefinition.METADATA, null, null);

                    ToolBar toolBar = (ToolBar) propertiesEditor.getTopComponent();
                    Button item = new Button(Messages.getResource("fm_save"));
                    item.setIconStyle("gwt-icons-save");
                    item.setEnabled(node.isWriteable() && !node.isLocked());
                    item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                        public void componentSelected(ButtonEvent event) {
                            JahiaContentManagementService.App.getInstance().saveProperties(elements, propertiesEditor.getProperties(), new AsyncCallback() {
                                public void onFailure(Throwable throwable) {
                                    com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
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
                    item.setEnabled(node.isWriteable() && !node.isLocked());

                    item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                        public void componentSelected(ButtonEvent event) {
                            propertiesEditor.resetForm();
                        }
                    });
                    toolBar.add(item);
                    toolBar.setVisible(true);
                    propertiesTabItem.add(propertiesEditor);
                    propertiesTabItem.layout();
                }
            });
        }
    }

    public void onBrowseTreeSelection(GWTJahiaNode node) {
        updateButtonsState(node);
        updateTemplateBox(node);
        displaySelection(node);
        if (currentlySelectedNode.getInheritedNodeTypes().contains("jnt:page") ||
            currentlySelectedNode.getNodeTypes().contains("jnt:page")) {
            createPageButton.setEnabled(true);
        }
    }

    private void updateButtonsState(GWTJahiaNode node) {
        if (node.isLockable()) {
            lockButton.setEnabled(true);
        }
        if (!node.isLocked()) {
            if (node.isWriteable()) {
                deleteButton.setEnabled(true);
                editButton.setEnabled(true);
            }
        } else {
            showUnlockButton();
        }
        createPageButton.setEnabled(false);
    }

    public void onCreateGridSelection(GWTJahiaNodeType selected) {
        deleteButton.setEnabled(false);
        lockButton.setEnabled(false);
        editButton.setEnabled(false);
        saveButton.setEnabled(false);
        createPageButton.setEnabled(false);
        displaySelection(null);
    }

    public void onSimpleModuleSelection(GWTJahiaNode node) {
        updateButtonsState(node);

        createView.getSelectionModel().deselectAll();
        displayGrid.getSelectionModel().deselectAll();
        displayTypesGrid.getSelectionModel().deselectAll();
        browseTree.getSelectionModel().deselectAll();
        updateTemplateBox(node);
        displaySelection(node);
    }

    public SelectionListener<ButtonEvent> getDeleteButtonListener(Button delete) {
        deleteButton = delete;
        return new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                if (currentlySelectedNode != null) {
                    List<String> paths = new ArrayList<String>(1);
                    paths.add(currentlySelectedNode.getPath());
                    JahiaContentManagementService.App.getInstance().deletePaths(paths, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Log.error("", throwable);
                            Window.alert("-->" + throwable.getMessage());
                        }

                        public void onSuccess(Object o) {
                            editManager.getMainModule().refresh();
                            browseTree.getStore().remove(currentlySelectedNode);
                            displayGrid.getStore().remove(currentlySelectedNode);
                        }
                    });
                }
            }
        };
    }

    public SelectionListener<ButtonEvent> getLockButtonListener(Button lock) {
        lockButton = lock;
        return new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                if (currentlySelectedNode != null) {
                    List<String> paths = new ArrayList<String>(1);
                    paths.add(currentlySelectedNode.getPath());
                    final boolean isLock = lockButton.getToolTip().getToolTipConfig().getText().equals("lock");
                    JahiaContentManagementService.App.getInstance().setLock(paths, isLock, new AsyncCallback() {
                        public void onFailure(Throwable throwable) {
                            Log.error("", throwable);
                            Window.alert("-->" + throwable.getMessage());
                        }

                        public void onSuccess(Object o) {
                            if (isLock) {
                                showUnlockButton();
                            } else {
                                showLockButton();
                            }
                            editManager.getMainModule().refresh();
                        }
                    });
                }
            }
        };
    }

    private void showLockButton() {
        lockButton.setToolTip("lock");
        lockButton.setIcon(SidePanel.ACTION_ICONS.lock());
    }

    private void showUnlockButton() {
        lockButton.setToolTip("unlock");
        lockButton.setIcon(SidePanel.ACTION_ICONS.unlock());
    }

    public SelectionListener<ButtonEvent> getEditButtonListener(Button edit) {
        editButton = edit;
        return new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                if (currentlySelectedNode != null) {
                    new EditContentEngine(currentlySelectedNode.getPath()).show();
                }
            }
        };
    }

    public void onDisplayGridSelection(GWTJahiaNode selectedItem) {
        onBrowseTreeSelection(selectedItem);
    }
    
    /**
     * This will update the template conbo box based on the page selected item
     *
     * @param node the selected node (item on the page)
     */
    public void updateTemplateBox(GWTJahiaNode node) {
        templateBox.getStore().removeAll();
        templateBox.clearSelections();
        JahiaContentManagementService.App.getInstance().getTemplatesPath(node.getPath(),new AsyncCallback<List<String[]>>() {
            public void onFailure(Throwable throwable) {
                Log.error("", throwable);
                Window.alert("-->" + throwable.getMessage());
            }
            public void onSuccess(List<String[]> strings) {
                for(String[] template:strings) {
                    templateBox.getStore().add(new GWTJahiaBasicDataBean(template[0], template[1]));
                }
            }
        });
    }

    public void onTemplateBoxSelection(GWTJahiaBasicDataBean selectedItem) {
         currentrySelectedTemplate = selectedItem.getValue();
         displayPreview(currentlySelectedNode, selectedItem.getValue());
         saveButton.setEnabled(true);
    }

    public SelectionListener<ButtonEvent> getSaveButtonListener(Button save) {
        saveButton = save;
        return new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                JahiaContentManagementService.App.getInstance().saveNodeTemplate(currentlySelectedNode.getPath() , currentrySelectedTemplate,new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Log.error("", throwable);
                        Window.alert("-->" + throwable.getMessage());

                    }
                    public void onSuccess(Object o) {
                        editManager.getMainModule().refresh();
                    }
                });
            }
        };
    }
    
    public SelectionListener<ButtonEvent> getCreatePageButtonListener(Button createPage) {
        createPageButton = createPage;
        return new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                if (currentlySelectedNode != null) {
                    JahiaContentDefinitionService.App.getInstance().getNodeType("jnt:page", new AsyncCallback<GWTJahiaNodeType>() {
                        public void onFailure(Throwable throwable) {
                            Log.error("", throwable);
                            Window.alert("-->" + throwable.getMessage());
                        }

                        public void onSuccess(GWTJahiaNodeType gwtJahiaNodeType) {
                            new EditContentEngine(editManager, currentlySelectedNode, gwtJahiaNodeType).show();
                        }
                    });
                }
            }
        };
    }
}
