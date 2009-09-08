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
package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Content editing widget.
 *
 * @author Sergiy Shyrkov
 */
public class EditContentEngine extends Window {

    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();

    private String contentPath;

    private ContentPanel contentPanel;

    private TabPanel tabs;

    private AsyncTabItem contentTab;

    private AsyncTabItem metadataTab;

    private AsyncTabItem rightsTab;

    private AsyncTabItem versionsTab;

    private AsyncTabItem workflowTab;
    private EditLinker editLinker = null;
    private GWTJahiaNode parent = null;
    private GWTJahiaNodeType type = null;
    private String targetPath = null;
    private boolean createInParentAndMoveOnTop = false;
    private boolean showMetadataTitleInContentTab = false;
    private boolean isReference = false;

    /**
     * Initializes an instance of this class.
     *
     * @param node the content object to be edited
     */
    public EditContentEngine(GWTJahiaNode node) {
        contentPath = node.getPath();
        if (node.getNodeTypes().contains("jnt:nodeReference")) {
            isReference = true;
        }
        initWindowProperties();
        initTabs(true);
    }

    public EditContentEngine(EditLinker editLinker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetPath) {
        this(editLinker, parent, type, targetPath, false, false);

    }

    public EditContentEngine(EditLinker editLinker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetPath, boolean createInParentAndMoveOnTop, boolean showMetadataTitleInContentTab) {
        this.editLinker = editLinker;
        this.parent = parent;
        this.type = type;
        if (!"*".equals(targetPath)) {
            this.targetPath = targetPath;
        }
        this.createInParentAndMoveOnTop = createInParentAndMoveOnTop;
        this.showMetadataTitleInContentTab = showMetadataTitleInContentTab;
        initWindowProperties();
        initTabs(false);
    }

    /**
     * Creates and initializes all window tabs.
     */
    private void initTabs(boolean fullMode) {
        tabs = new TabPanel();
        tabs.setBodyBorder(false);
        tabs.setBorders(false);

        contentTab = new AsyncTabItem(Messages.get("ece_content", "Content"));
        contentTab.setLayout(new FitLayout());
        tabs.add(contentTab);
        if (fullMode) {
            metadataTab = new AsyncTabItem(Messages.get("ece_metadata", "Metadata"));
            metadataTab.setLayout(new FitLayout());
            tabs.add(metadataTab);

            rightsTab = new AsyncTabItem(Messages.get("ece_rights", "Rights"));
            rightsTab.setLayout(new FitLayout());
            tabs.add(rightsTab);

            workflowTab = new AsyncTabItem(Messages.get("ece_workflow", "Workflow"));
            workflowTab.setLayout(new FitLayout());
            tabs.add(workflowTab);

            versionsTab = new AsyncTabItem(Messages.get("ece_versions", "Versions"));
            versionsTab.setLayout(new FitLayout());
            tabs.add(versionsTab);
        }
        add(tabs);
        createContentTab(fullMode);
    }

    private AsyncTabItem createContentTab(final boolean fullMode) {
        if (fullMode) {
            contentService.getProperties(contentPath, new AsyncCallback<GWTJahiaGetPropertiesResult>() {
                public void onFailure(Throwable throwable) {
                    Log.debug("Cannot get properties", throwable);
                }

                public void onSuccess(GWTJahiaGetPropertiesResult result) {
                    if (isReference) {
                        GWTJahiaNodeProperty ref = result.getProperties().get("j:node");
                        GWTJahiaNodePropertyValue v = ref.getValues().iterator().next();
                        
                        contentService.getProperties(v.getNode().getPath(), new AsyncCallback<GWTJahiaGetPropertiesResult>() {
                            public void onFailure(Throwable throwable) {
                                Log.debug("Cannot get properties", throwable);
                            }
                            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                                PropertiesEditor propertiesEditor = createPropertiesEditor(result, GWTJahiaItemDefinition.CONTENT, fullMode, null);
                                contentTab.add(propertiesEditor);
                                contentTab.setProcessed(true);
                                contentTab.layout();
                            }
                        });
                    } else {
                        PropertiesEditor propertiesEditor = createPropertiesEditor(result, GWTJahiaItemDefinition.CONTENT, fullMode, null);
                        contentTab.add(propertiesEditor);
                        contentTab.setProcessed(true);
                        contentTab.layout();
                    }
                    PropertiesEditor metadataEditor = createPropertiesEditor(result, GWTJahiaItemDefinition.METADATA, fullMode, null);
                    metadataTab.add(metadataEditor);
                    metadataTab.setProcessed(true);
                    metadataTab.layout();
                }
            });

            return contentTab;
        } else {
            List<GWTJahiaNodeType> nodeTypes = new ArrayList<GWTJahiaNodeType>(1);
            nodeTypes.add(type);
            GWTJahiaGetPropertiesResult result = new GWTJahiaGetPropertiesResult(nodeTypes, new HashMap<String, GWTJahiaNodeProperty>());
            result.setNode(parent);
            if (showMetadataTitleInContentTab) {
                List<String> excludedItems = new ArrayList<String>();
                for (GWTJahiaItemDefinition definition : type.getInheritedItems()) {
                    if (!GWTJahiaItemDefinition.CONTENT.equals(definition.getDataType()) &&
                        !("jcr:title".equals(definition.getName()))) {
                        excludedItems.add(definition.getName());
                    }
                }
                contentTab.add(createPropertiesEditor(result, null, fullMode, excludedItems));
            } else {
                contentTab.add(createPropertiesEditor(result, GWTJahiaItemDefinition.CONTENT, fullMode, null));
            }
            contentTab.setProcessed(true);
            contentTab.layout();
            return contentTab;
        }
    }

    private PropertiesEditor createPropertiesEditor(GWTJahiaGetPropertiesResult result, String datatype, boolean fullMode, List<String> excludedItems) {
        GWTJahiaNode selectedNode = result.getNode();
        final List<GWTJahiaNode> elements = new ArrayList<GWTJahiaNode>();
        elements.add(selectedNode);

        final PropertiesEditor propertiesEditor = new PropertiesEditor(result.getNodeTypes(), result.getProperties(), false, true, datatype, excludedItems, null,selectedNode.isWriteable());
        final EditContentEngine editContentEngine = this;
        ToolBar toolBar = (ToolBar) propertiesEditor.getTopComponent();
        Button item = new Button(Messages.getResource("fm_save"));
        item.setIconStyle("gwt-icons-save");
        item.setEnabled(selectedNode.isWriteable() && !selectedNode.isLocked());
        if (fullMode) {
            item.addSelectionListener(new SaveSelectionListener(elements, propertiesEditor, this));
        } else {
            item.addSelectionListener(new CreateSelectionListener(propertiesEditor, this));
        }
        toolBar.add(new FillToolItem());
        toolBar.add(item);
        if (fullMode) {
            item = new Button(Messages.getResource("fm_restore"));
            item.setIconStyle("gwt-icons-restore");
            item.setEnabled(selectedNode.isWriteable() && !selectedNode.isLocked());

            item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    propertiesEditor.resetForm();
                }
            });
            toolBar.add(item);
        }
        item = new Button(Messages.getResource("fm_cancel"));
        item.setIconStyle("gwt-icons-cancel");
        item.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                editContentEngine.hide();
            }
        });
        toolBar.add(item);
        toolBar.setVisible(true);
        return propertiesEditor;
    }

    /**
     * Initializes basic window properties: size, state and title.
     */
    private void initWindowProperties() {
        setSize(800, 600);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setHeading(contentPath);
    }

    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        private final List<GWTJahiaNode> elements;
        private final PropertiesEditor propertiesEditor;
        private final EditContentEngine editContentEngine;

        public SaveSelectionListener(List<GWTJahiaNode> elements, PropertiesEditor propertiesEditor, EditContentEngine editContentEngine) {
            this.elements = elements;
            this.propertiesEditor = propertiesEditor;
            this.editContentEngine = editContentEngine;
        }

        public void componentSelected(ButtonEvent event) {
            JahiaContentManagementService.App.getInstance().saveProperties(elements, propertiesEditor.getProperties(), new AsyncCallback<Object>() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display("", "Properties saved");
                    editContentEngine.hide();
                    editLinker.getMainModule().refresh();
                }
            });
        }
    }

    private class CreateSelectionListener extends SelectionListener<ButtonEvent> {
        private final PropertiesEditor propertiesEditor;
        private final EditContentEngine editContentEngine;

        public CreateSelectionListener(PropertiesEditor propertiesEditor, EditContentEngine editContentEngine) {
            this.propertiesEditor = propertiesEditor;
            this.editContentEngine = editContentEngine;
        }


        public void componentSelected(ButtonEvent event) {
            if (createInParentAndMoveOnTop) {
                JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(parent.getPath(), targetPath, type.getName(), propertiesEditor.getProperties(), null, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(Object o) {
                        Info.display("", "Node created");
                        editContentEngine.hide();
                        editLinker.getMainModule().refresh();
                    }
                });
            } else {
                JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), targetPath, type.getName(), propertiesEditor.getProperties(), null, new AsyncCallback<GWTJahiaNode>() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(GWTJahiaNode node) {
                        Info.display("", "Node created");
                        editContentEngine.hide();
                        editLinker.getMainModule().refresh();
                    }
                });
            }
        }
    }
}
