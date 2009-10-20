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
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeListLoadResult;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.definition.ClassificationEditor;

import java.util.*;

/**
 * Content editing widget.
 *
 * @author Sergiy Shyrkov
 */
public class EditContentEngine extends Window {

    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();

    private boolean existingNode = true;

    private String contentPath;

    private GWTJahiaNode node;
    private List<GWTJahiaNodeType> nodeTypes;
    private Map<String,GWTJahiaNodeProperty> props;

    private GWTJahiaNode referencedNode;
    private List<GWTJahiaNodeType> referencedNodeTypes;
    private Map<String,GWTJahiaNodeProperty> referencedProps;

    private TabPanel tabs;

    private AsyncTabItem contentTab;
    private AsyncTabItem layoutTab;
    private AsyncTabItem metadataTab;
    private AsyncTabItem classificationTab;
    private AsyncTabItem publicationTab;
//    private AsyncTabItem workflowTab;
//    private AsyncTabItem rightsTab;
//    private AsyncTabItem versionsTab;

    private Linker linker = null;
    private GWTJahiaNode parent = null;
    private GWTJahiaNodeType type = null;
    private String targetName = null;
    private boolean createInParentAndMoveBefore = false;
    private boolean isReference = false;

    private PropertiesEditor propertiesEditor;
    private PropertiesEditor layoutEditor;
    private PropertiesEditor metadataEditor;
    private PropertiesEditor publicationEditor;
    private LayoutContainer htmlPreview;

    private Button ok;
    private Button restore;
    private Button cancel;
    private ClassificationEditor classificationEditor;

    /**
     * Initializes an instance of this class.
     *
     * @param node the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public EditContentEngine(GWTJahiaNode node, Linker linker) {
        this.linker = linker;
        contentPath = node.getPath();
        if (node.getNodeTypes().contains("jnt:nodeReference")) {
            isReference = true;
        }

        loadNode();

        initWindowProperties();
        initTabs();
        initButtons();
    }

    /**
     * Open Edit content engine for a new node creation
     *
     * @param linker
     * @param parent
     * @param type
     * @param targetName
     */
    public EditContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName) {
        this(linker, parent, type, targetName, false);

    }

    /**
     * Open Edit content engine for a new node creation
     *
     * @param linker The linker
     * @param parent The parent node where to create the new node - if createInParentAndMoveBefore, the node is sibling
     * @param type The selected node type of the new node
     * @param targetName The name of the new node, or null if automatically defined
     * @param createInParentAndMoveBefore
     */
    public EditContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName, boolean createInParentAndMoveBefore) {
        this.linker = linker;
        this.existingNode = false;
        this.parent = parent;
        this.type = type;
        if (!"*".equals(targetName)) {
            this.targetName = targetName;
        }
        this.createInParentAndMoveBefore = createInParentAndMoveBefore;

        nodeTypes = new ArrayList<GWTJahiaNodeType>(1);
        nodeTypes.add(type);
        props = new HashMap<String, GWTJahiaNodeProperty>();

        initWindowProperties();
        initTabs();
        initButtons();
        ok.setEnabled(true);
    }

    private void initButtons() {

        setButtonAlign(Style.HorizontalAlignment.CENTER);

        final EditContentEngine editContentEngine = this;

        ok = new Button(Messages.getResource("fm_save"));
        ok.setEnabled(false);
        ok.setIconStyle("gwt-icons-save");
        if (existingNode) {
            ok.addSelectionListener(new SaveSelectionListener());
        } else {
            ok.addSelectionListener(new CreateSelectionListener());
        }

        addButton(this.ok);

        restore = new Button(Messages.getResource("fm_restore"));
        restore.setIconStyle("gwt-icons-restore");
        restore.setEnabled(false);

        if (existingNode) {
            restore.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    propertiesEditor.resetForm();
                }
            });
            addButton(this.restore);
        }
        cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setIconStyle("gwt-icons-cancel");
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                editContentEngine.hide();
            }
        });
        addButton(this.cancel);

    }

    /**
     * Creates and initializes all window tabs.
     */
    private void initTabs() {
        tabs = new TabPanel();
        tabs.setBodyBorder(false);
        tabs.setBorders(false);
        tabs.setAutoHeight(true);

        contentTab = new AsyncTabItem(Messages.get("ece_content", "Content"));
        tabs.add(contentTab);

        layoutTab = new AsyncTabItem(Messages.get("ece_layout", "Layout"));
        tabs.add(layoutTab);

        metadataTab = new AsyncTabItem(Messages.get("ece_metadata", "Metadata"));
        tabs.add(metadataTab);

        classificationTab = new AsyncTabItem(Messages.get("ece_classification", "Classification"));
        tabs.add(classificationTab);

//        publicationTab = new AsyncTabItem(Messages.get("ece_publication", "Publication"));
//        publicationTab.setScrollMode(Style.Scroll.AUTO);
//        tabs.add(publicationTab);

//        rightsTab = new AsyncTabItem(Messages.get("ece_rights", "Rights"));
//        tabs.add(rightsTab);

//        workflowTab = new AsyncTabItem(Messages.get("ece_workflow", "Workflow"));
//        tabs.add(workflowTab);

//        versionsTab = new AsyncTabItem(Messages.get("ece_versions", "Versions"));
//        tabs.add(versionsTab);

        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                fillCurrentTab();
            }
        });

        add(tabs);
    }

    private void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();

        if (currentTab == contentTab) {
            createContentTab();
        } else if (currentTab == layoutTab) {
            createLayoutTab();
        } else if (currentTab == metadataTab) {
            createMetadataTab();
//        } else if (currentTab == publicationTab) {
//            createPublicationTab();
//        } else if (currentTab == rightsTab) {
//        } else if (currentTab == workflowTab) {
//        } else if (currentTab == versionsTab) {
        } else if (currentTab == classificationTab) {
            createClassificationTab();
        }
    }

    private void createClassificationTab() {
        if (!classificationTab.isProcessed()) {
            if (!existingNode || (node != null)) {
                classificationTab.setProcessed(true);
                classificationEditor = new ClassificationEditor(node);
                classificationTab.add(classificationEditor);

            }
            layout();
        }
    }

    private void createContentTab() {
        if (!contentTab.isProcessed()) {
            if (!existingNode || (!isReference && node != null)) {
                contentTab.setProcessed(true);
                contentTab.setStyleName("x-panel-mc");

                propertiesEditor = new PropertiesEditor(nodeTypes, props, false, true, GWTJahiaItemDefinition.CONTENT, null, null, !existingNode || node.isWriteable(), true);
                propertiesEditor.setHeight(504);

                FormPanel formPanel = getNamePanel();
                contentTab.add(formPanel);

                contentTab.add(propertiesEditor);
                contentTab.layout();
            } else if (isReference && referencedNode != null) {
                contentTab.setProcessed(true);
                propertiesEditor = new PropertiesEditor(referencedNodeTypes, referencedProps, false, true, GWTJahiaItemDefinition.CONTENT, null, null, referencedNode.isWriteable(), true);
                propertiesEditor.setHeight(504);

                FormPanel formPanel = getNamePanel();
                contentTab.add(formPanel);

                contentTab.add(propertiesEditor);
                contentTab.layout();
            }
            layout();
        }
    }

    private FormPanel getNamePanel() {
        FormPanel formPanel = new FormPanel();
        formPanel.setFieldWidth(550);
        formPanel.setLabelWidth(180);
        formPanel.setFrame(false);
        formPanel.setBorders(false);
        formPanel.setBodyBorder(false);
        formPanel.setHeaderVisible(false);
        TextField<String> name = new TextField<String>();
        name.setFieldLabel("Name");
        name.setName("name");
        if (existingNode) {
            name.setValue(node.getName());
            contentTab.setData("NodeName",node.getName());
        } else {
            name.setValue("Automatically Created (you can type your name here if you want)");
        }
        formPanel.add(name);
        return formPanel;
    }

    private void createLayoutTab() {
        if (!layoutTab.isProcessed()) {
            if (!existingNode || (node != null)) {
                layoutTab.setProcessed(true);
                layoutTab.setStyleName("x-panel-mc");
                layoutEditor = new PropertiesEditor(this.nodeTypes, this.props, false, true, GWTJahiaItemDefinition.LAYOUT, null, null, !existingNode || node.isWriteable(), true);
                layoutEditor.setHeight(254);
                layoutTab.add(layoutEditor);

                if (node != null) {
                    final ComboBox<GWTJahiaValueDisplayBean> templateField = (ComboBox<GWTJahiaValueDisplayBean>) layoutEditor.getFieldsMap().get("j:template");
                    final ComboBox<GWTJahiaValueDisplayBean> skinField = (ComboBox<GWTJahiaValueDisplayBean>) layoutEditor.getFieldsMap().get("j:skin");
                    final ComboBox<GWTJahiaValueDisplayBean> subNodesTemplateField = (ComboBox<GWTJahiaValueDisplayBean>) layoutEditor.getFieldsMap().get("j:subNodesTemplate");
                    SelectionChangedListener<GWTJahiaValueDisplayBean> listener = new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                        public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> se) {
                            Map<String, String> contextParams = new HashMap<String, String>();
                            if (skinField != null && skinField.getValue() != null) {
                                contextParams.put("forcedSkin", skinField.getValue().getValue());
                            }
                            if (subNodesTemplateField != null && subNodesTemplateField.getValue() != null) {
                                contextParams.put("forcedSubNodesTemplate", subNodesTemplateField.getValue().getValue());
                            }
                            updatePreview((templateField != null && templateField.getValue() != null)? templateField.getValue().getValue():null, contextParams);
                        }
                    };
                    if (templateField != null) {
                        templateField.addSelectionChangedListener(listener);
                    }
                    if (skinField != null) {
                        skinField.addSelectionChangedListener(listener);
                    }
                    if (subNodesTemplateField != null) {
                        subNodesTemplateField.addSelectionChangedListener(listener);
                    }
                    htmlPreview = new LayoutContainer(new FitLayout());
                    htmlPreview.setHeight(250);
                    htmlPreview.addStyleName("x-panel");
                    htmlPreview.setScrollMode(Style.Scroll.AUTO);
                    layoutTab.add(htmlPreview);
                }
//            layoutTab.layout();
                layout();
            }
        }
    }

    private void createMetadataTab() {
        if (!metadataTab.isProcessed()) {
            if (!existingNode || (node != null)) {
                metadataTab.setProcessed(true);
                metadataTab.setStyleName("x-panel-mc");
                metadataEditor = new PropertiesEditor(nodeTypes, props, false, true, GWTJahiaItemDefinition.METADATA, null, Arrays.asList("jmix:categorized","jmix:tagged"), !existingNode || node.isWriteable(), true);
                metadataEditor.setHeight(504);
                metadataTab.add(metadataEditor);
                layout();
            }
        }
    }

    private void createPublicationTab() {
        if (!publicationTab.isProcessed()) {
            if (!existingNode || (node != null)) {
                publicationTab.setProcessed(true);
                publicationEditor = new PropertiesEditor(nodeTypes, props, false, true, GWTJahiaItemDefinition.PUBLICATION, null, null, !existingNode || node.isWriteable(), true);
                publicationTab.add(publicationEditor);
//            metadataTab.layout();
                publicationTab.setHeight("400px");
                layout();
            }
        }


    }

    private void loadNode() {
        contentService.getProperties(contentPath, new AsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
            }

            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                props = result.getProperties();

                if (referencedNode != null || !isReference) {
                    fillCurrentTab();
                    ok.setEnabled(true);
                    restore.setEnabled(true);
                }
            }
        });

        if (isReference) {
            contentService.getProperties(node.getReferencedNode().getPath(), new AsyncCallback<GWTJahiaGetPropertiesResult>() {
                public void onFailure(Throwable throwable) {
                    Log.debug("Cannot get properties", throwable);
                }
                public void onSuccess(GWTJahiaGetPropertiesResult result) {
                    referencedNode = result.getNode();
                    referencedNodeTypes = result.getNodeTypes();
                    referencedProps = result.getProperties();

                    if (node != null) {
                        fillCurrentTab();
                        ok.setEnabled(true);
                        restore.setEnabled(true);
                    }
                }
            });
        } 
    }

    private void updatePreview(String template, Map<String,String> contextParams) {
        if (node != null) {
            JahiaContentManagementService.App.getInstance().getRenderedContent(node.getPath(), null, null, template, "wrapper.previewwrapper", contextParams, false, new AsyncCallback<String>() {
                public void onSuccess(String result) {
                    HTML html = new HTML(result);
                    setHTML(html);
                    layout();
                }

                public void onFailure(Throwable caught) {
                    Log.error("", caught);
                    com.google.gwt.user.client.Window.alert("-->" + caught.getMessage());
                }
            });
        } else {
            setHTML(null);
        }
    }

    public void setHTML(HTML html) {
        htmlPreview.removeAll();
        if (html != null) {
            htmlPreview.add(html);
        }
        htmlPreview.layout();
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
        if (existingNode) {
            setHeading("Edit " + contentPath);
        } else {
            setHeading("Create " + type.getName() + " in " + contentPath);
        }
    }

    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        public SaveSelectionListener() {
        }

        public void componentSelected(ButtonEvent event) {
            List elements = new ArrayList<GWTJahiaNode>();
            node.setName(((TextField)((FormPanel)contentTab.getItem(0)).getItem(0)).getRawValue());
            elements.add(node);
            List<GWTJahiaNodeProperty> list = new ArrayList<GWTJahiaNodeProperty>();
            if (propertiesEditor != null) {
                list.addAll(propertiesEditor.getProperties());
            }

            if (isReference) {
                List refelements = new ArrayList<GWTJahiaNode>();
                refelements.add(referencedNode);
                JahiaContentManagementService.App.getInstance().saveProperties(refelements, list, new AsyncCallback<Object>() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(Object o) {
                    }
                });
                list.clear();
            }
            if (layoutEditor != null) {
                list.addAll(layoutEditor.getProperties());
            }
            if (metadataEditor != null) {
                list.addAll(metadataEditor.getProperties());
            }
            if (classificationEditor!=null) {
                updatePropertiesListWithClassificationEditorData(list);
            }
            JahiaContentManagementService.App.getInstance().saveProperties(elements, list, new AsyncCallback<Object>() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display("", "Properties saved");
                    EditContentEngine.this.hide();
                    linker.refreshMainComponent();
                }
            });
        }

        private void updatePropertiesListWithClassificationEditorData(List<GWTJahiaNodeProperty> list) {
            List<GWTJahiaNode> gwtJahiaNodes = classificationEditor.getCatStore().getAllItems();
            List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
            for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
                values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode));
            }
            GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
            gwtJahiaNodeProperty.setMultiple(true);
            gwtJahiaNodeProperty.setValues(values);
            gwtJahiaNodeProperty.setName("j:defaultCategory");
            list.add(gwtJahiaNodeProperty);
            gwtJahiaNodes = classificationEditor.getTagStore().getAllItems();
            values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
            for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
                values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode));
            }
            gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
            gwtJahiaNodeProperty.setMultiple(true);
            gwtJahiaNodeProperty.setValues(values);
            gwtJahiaNodeProperty.setName("j:tags");
            list.add(gwtJahiaNodeProperty);
        }
    }

    private class CreateSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            String nodeName = ((TextField)((FormPanel)contentTab.getItem(0)).getItem(0)).getRawValue();
            if(nodeName.equals("Automatically Created (you can type your name here if you want)")) {
                nodeName = targetName;
            }
            if (createInParentAndMoveBefore) {
                JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(parent.getPath(), nodeName, type.getName(), propertiesEditor.getProperties(), null, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(Object o) {
                        Info.display("", "Node created");
                        EditContentEngine.this.hide();
                        linker.refreshMainComponent();
                    }
                });
            } else {
                JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), nodeName, type.getName(), propertiesEditor.getProperties(), null, new AsyncCallback<GWTJahiaNode>() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(GWTJahiaNode node) {
                        Info.display("", "Node created");
                        EditContentEngine.this.hide();
                        linker.refreshMainComponent();
                    }
                });
            }
        }
    }
}
