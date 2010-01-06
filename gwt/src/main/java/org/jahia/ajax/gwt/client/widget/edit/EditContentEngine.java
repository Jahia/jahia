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
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.definition.ClassificationEditor;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.*;

/**
 * Content editing widget.
 *
 * @author Sergiy Shyrkov
 */
public class EditContentEngine extends Window {

    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    private static JahiaContentDefinitionServiceAsync definitionService = JahiaContentDefinitionService.App.getInstance();

    private boolean existingNode = true;

    private String contentPath;

    private GWTJahiaNode node;
    private List<GWTJahiaNodeType> nodeTypes;
    private List<GWTJahiaNodeType> mixin;
    private Map<String, GWTJahiaNodeProperty> props;

    private GWTJahiaNode referencedNode;
    private List<GWTJahiaNodeType> referencedNodeTypes;
    private Map<String, GWTJahiaNodeProperty> referencedProps;

    private TabPanel tabs;

    private AsyncTabItem contentTab;
    private AsyncTabItem layoutTab;
    private AsyncTabItem metadataTab;
    private AsyncTabItem classificationTab;
    private AsyncTabItem optionsTab;
    private AsyncTabItem rightsTab;


    private Linker linker = null;
    private GWTJahiaNode parent = null;
    private GWTJahiaNodeType type = null;
    private String targetName = null;
    private boolean createInParentAndMoveBefore = false;
    private boolean isReference = false;

    private PropertiesEditor propertiesEditor;
    private PropertiesEditor layoutEditor;
    private PropertiesEditor metadataEditor;
    private PropertiesEditor optionsEditor;
    private AclEditor rightsEditor;
    private LayoutContainer htmlPreview;

    private ButtonBar buttonBar;
    private Button ok;
    private Button cancel;
    private ClassificationEditor classificationEditor;
    public static final int BUTTON_HEIGHT = 24;
    private boolean isNodeNameFieldDisplayed = false;
    private String nodeName;


    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public EditContentEngine(GWTJahiaNode node, Linker linker) {
        this.linker = linker;
        contentPath = node.getPath();
        if (node.getNodeTypes().contains("jnt:nodeReference")) {
            isReference = true;
        }
        nodeName = node.getName();
        loadNode();
        initWindowProperties();
        initTabs();
        initFooter();
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
     * @param linker                      The linker
     * @param parent                      The parent node where to create the new node - if createInParentAndMoveBefore, the node is sibling
     * @param type                        The selected node type of the new node
     * @param targetName                  The name of the new node, or null if automatically defined
     * @param createInParentAndMoveBefore
     */
    public EditContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName, boolean createInParentAndMoveBefore) {
        this(linker, parent, type, new HashMap<String, GWTJahiaNodeProperty>(), targetName, createInParentAndMoveBefore);
    }

    /**
     * Open Edit content engine for a new node creation
     *
     * @param linker                      The linker
     * @param parent                      The parent node where to create the new node - if createInParentAndMoveBefore, the node is sibling
     * @param type                        The selected node type of the new node
     * @param props                       initial values for properties
     * @param targetName                  The name of the new node, or null if automatically defined
     * @param createInParentAndMoveBefore
     */
    public EditContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, Map<String, GWTJahiaNodeProperty> props, String targetName, boolean createInParentAndMoveBefore) {
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
        this.props = new HashMap<String, GWTJahiaNodeProperty>(props);

        loadMixin();

        initWindowProperties();
        initTabs();
        initFooter();
        setFooter(true);
        ok.setEnabled(true);
    }

    /**
     * init buttons
     */
    private void initFooter() {
        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        final EditContentEngine editContentEngine = this;
        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        ok = new Button(Messages.getResource("fm_save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setEnabled(false);
        ok.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());
        if (existingNode) {
            ok.addSelectionListener(new SaveSelectionListener());
        } else {
            ok.addSelectionListener(new CreateSelectionListener());
        }

        buttonBar.add(ok);

        if (!existingNode) {
            Button okAndNew = new Button(Messages.getResource("fm_saveAndNew"));
            okAndNew.setHeight(BUTTON_HEIGHT);
            okAndNew.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());

            okAndNew.addSelectionListener(new CreateAndAddNewSelectionListener());
            buttonBar.add(okAndNew);
        }


        /* ToDo: activate restore button in the engine

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
        }*/
        cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                editContentEngine.hide();
            }
        });
        buttonBar.add(cancel);
        buttonsPanel.add(buttonBar);

        // copyrigths
        Text copyright = new Text(Messages.getResource("fm_copyright"));
        ButtonBar container = new ButtonBar();
        container.setAlignment(Style.HorizontalAlignment.CENTER);
        container.add(copyright);
        buttonsPanel.add(container);
        setBottomComponent(buttonsPanel);
    }

    /**
     * Creates and initializes all window tabs.
     */
    private void initTabs() {
        tabs = new TabPanel();

        tabs.setBodyBorder(false);
        tabs.setBorders(true);

        contentTab = new AsyncTabItem(Messages.get("ece_content", "Content"));
        contentTab.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabContent());

        tabs.add(contentTab);

        layoutTab = new AsyncTabItem(Messages.get("ece_layout", "Layout"));
        layoutTab.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabLayout());
        tabs.add(layoutTab);

        metadataTab = new AsyncTabItem(Messages.get("ece_metadata", "Metadata"));
        metadataTab.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabMetadata());
        tabs.add(metadataTab);

        classificationTab = new AsyncTabItem(Messages.get("ece_classification", "Classification"));
        classificationTab.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabClassification());
        tabs.add(classificationTab);

        optionsTab = new AsyncTabItem(Messages.get("ece_options", "Options"));
        optionsTab.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabOption());
        tabs.add(optionsTab);


//        publicationTab = new AsyncTabItem(Messages.get("ece_publication", "Publication"));
//        publicationTab.setScrollMode(Style.Scroll.AUTO);
//        tabs.add(publicationTab);

        rightsTab = new AsyncTabItem(Messages.get("ece_rights", "Rights"));
        tabs.add(rightsTab);

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

    /**
     * fill current tab
     */
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
//        }  else if (currentTab == workflowTab) {
//        } else if (currentTab == versionsTab) {
        } else if (currentTab == classificationTab) {
            createClassificationTab();
        } else if (currentTab == optionsTab) {
            createOptionsTab();
        } else if (currentTab == rightsTab) {
            createRightsTab();
        }
    }

    /**
     * Create classification tab
     */
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

    /**
     * Create content tab
     */
    private void createContentTab() {
        if (!contentTab.isProcessed()) {
            if (mixin != null && !isReference) {
                contentTab.setProcessed(true);
                contentTab.setStyleName("x-panel-mc");


                propertiesEditor = new PropertiesEditor(nodeTypes, mixin, props, false, true, GWTJahiaItemDefinition.CONTENT, null, null, !existingNode || node.isWriteable(), true);
                propertiesEditor.setHeight(504);
                if (!propertiesEditor.getFieldsMap().containsKey("jcr:title")) {
                    FormPanel formPanel = getNamePanel();
                    contentTab.add(formPanel);
                    isNodeNameFieldDisplayed = true;
                }
                contentTab.add(propertiesEditor);
                contentTab.layout();
            } else if (isReference && referencedNode != null) {
                contentTab.setProcessed(true);
                propertiesEditor = new PropertiesEditor(referencedNodeTypes, referencedProps, false, true, GWTJahiaItemDefinition.CONTENT, null, null, referencedNode.isWriteable(), true);
                propertiesEditor.setHeight(504);

                if (!propertiesEditor.getFieldsMap().containsKey("jcr:title")) {
                    FormPanel formPanel = getNamePanel();
                    contentTab.add(formPanel);
                    isNodeNameFieldDisplayed = true;
                }

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
            contentTab.setData("NodeName", node.getName());
        } else {
            name.setValue("Automatically Created (you can type your name here if you want)");
        }
        formPanel.add(name);
        return formPanel;
    }

    /**
     * create layout tab
     */
    private void createLayoutTab() {
        if (!layoutTab.isProcessed()) {
            if (mixin != null) {
                layoutTab.setProcessed(true);
                layoutTab.setStyleName("x-panel-mc");
                layoutEditor = new PropertiesEditor(this.nodeTypes, mixin, this.props, false, true, GWTJahiaItemDefinition.LAYOUT, null, null, !existingNode || node.isWriteable(), true);
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
                            updatePreview((templateField != null && templateField.getValue() != null) ? templateField.getValue().getValue() : null, contextParams);
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
                    //htmlPreview.setHeight(250);
                    htmlPreview.addStyleName("x-panel");
                    htmlPreview.setScrollMode(Style.Scroll.AUTO);
                    layoutTab.add(htmlPreview);
                }
                layout();
            }
        }
    }

    /**
     * Create metadata tab
     */
    private void createMetadataTab() {
        if (!metadataTab.isProcessed()) {
            if (mixin != null) {
                metadataTab.setProcessed(true);
                metadataTab.setStyleName("x-panel-mc");
                metadataEditor = new PropertiesEditor(nodeTypes, mixin, props, false, true, GWTJahiaItemDefinition.METADATA, null, Arrays.asList("jmix:categorized", "jmix:tagged"), !existingNode || node.isWriteable(), true);
                metadataEditor.setHeight(504);
                metadataTab.add(metadataEditor);
                layout();
            }
        }
    }

    /**
     * Create option tab
     */
    private void createOptionsTab() {
        if (!optionsTab.isProcessed()) {
            if (mixin != null) {
                optionsTab.setProcessed(true);
                optionsTab.setStyleName("x-panel-mc");
                optionsEditor = new PropertiesEditor(nodeTypes, mixin, props, false, true, GWTJahiaItemDefinition.OPTIONS, null, null, !existingNode || node.isWriteable(), true);
                optionsTab.setHeight(504);
                optionsTab.add(optionsEditor);
                layout();
            }
        }


    }

    /**
     * create rights tab
     */
    public void createRightsTab() {
        if (!rightsTab.isProcessed()) {
            contentService.getACL(node.getPath(), new AsyncCallback<GWTJahiaNodeACL>() {
                /**
                 * onsuccess
                 * @param gwtJahiaNodeACL
                 */
                public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
                    // auth. editor
                    rightsEditor = new AclEditor(gwtJahiaNodeACL, node.getAclContext());
                    rightsEditor.setAclGroup(JCRClientUtils.AUTHORIZATIONS_ACL);
                    rightsEditor.setCanBreakInheritance(false);
                    if (!(node.getProviderKey().equals("default") || node.getProviderKey().equals("jahia"))) {
                        rightsEditor.setReadOnly(true);
                    } else {
                        rightsEditor.setReadOnly(!node.isWriteable() || node.isLocked());
                    }
                    Button saveButton = rightsEditor.getSaveButton();
                    saveButton.setVisible(false);
                    //saveButton.addSelectionListener(new SaveAclSelectionListener(selectedNode, AUTH_TAB_ITEM));
                    rightsTab.setLayout(new FitLayout());
                    rightsTab.add(rightsEditor.renderNewAclPanel());
                    rightsTab.layout();
                    rightsTab.setProcessed(true);

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

    /**
     * load node
     */
    private void loadNode() {
        contentService.getProperties(contentPath, new AsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
            }

            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                props = result.getProperties();

                loadMixin();

                if (referencedNode != null || !isReference) {
                    fillCurrentTab();
                    ok.setEnabled(true);
                    //restore.setEnabled(true);
                } else {
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
                                //restore.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });

    }

    /**
     * load mixin
     */
    private void loadMixin() {
        if (node == null) {
            definitionService.getAvailableMixin(nodeTypes.iterator().next(), new AsyncCallback<List<GWTJahiaNodeType>>() {
                public void onSuccess(List<GWTJahiaNodeType> result) {
                    mixin = result;
                    fillCurrentTab();
                }

                public void onFailure(Throwable caught) {

                }
            });
        } else {
            definitionService.getAvailableMixin(node, new AsyncCallback<List<GWTJahiaNodeType>>() {
                public void onSuccess(List<GWTJahiaNodeType> result) {
                    mixin = result;
                    fillCurrentTab();
                }

                public void onFailure(Throwable caught) {

                }
            });
        }
    }

    /**
     * Update preview
     *
     * @param template
     * @param contextParams
     */
    private void updatePreview(String template, Map<String, String> contextParams) {
        if (node != null) {
            JahiaContentManagementService.App.getInstance().getRenderedContent(node.getPath(), null, null, template, "wrapper.previewwrapper", contextParams, false, new AsyncCallback<String>() {
                public void onSuccess(String result) {
                    HTML html = new HTML(result);
                    setHTML(html);
                    layout();
                }

                public void onFailure(Throwable caught) {
                    Log.error("", caught);
                    com.google.gwt.user.client.Window.alert("-update preview->" + caught.getMessage());
                }
            });
        } else {
            setHTML(null);
        }
    }

    /**
     * set preview HTML
     *
     * @param html
     */
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
        setLayout(new FillLayout());
        setBodyBorder(false);
        setSize(950, 750);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineLogoJahia());
        if (existingNode) {
            setHeading("Edit " + nodeName);
            //setHeading("Edit " + contentPath);
        } else {
            setHeading("Create " + type.getName());
            //setHeading("Create " + type.getName() + " in " + contentPath);
        }
    }

    /**
     * Save selection listener
     */
    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        public SaveSelectionListener() {
        }

        public void componentSelected(ButtonEvent event) {
            // node
            final List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            if (isNodeNameFieldDisplayed) {
                node.setName(((TextField<?>) ((FormPanel) contentTab.getItem(0)).getItem(0)).getRawValue());
            }
            nodes.add(node);


            // general properties
            final List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
            if (propertiesEditor != null) {
                properties.addAll(propertiesEditor.getProperties());
                node.getNodeTypes().removeAll(propertiesEditor.getRemovedTypes());
                node.getNodeTypes().addAll(propertiesEditor.getAddedTypes());
            }

            // reference
            if (isReference) {
                List<GWTJahiaNode> refNodes = new ArrayList<GWTJahiaNode>();
                refNodes.add(referencedNode);
                JahiaContentManagementService.App.getInstance().saveProperties(refNodes, properties, new AsyncCallback<Object>() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(Object o) {
                    }
                });
                properties.clear();
            }

            // layout properties
            if (layoutEditor != null) {
                properties.addAll(layoutEditor.getProperties());
                node.getNodeTypes().removeAll(layoutEditor.getRemovedTypes());
                node.getNodeTypes().addAll(layoutEditor.getAddedTypes());
                node.getNodeTypes().addAll(layoutEditor.getTemplateTypes());
            }

            // meta properties
            if (metadataEditor != null) {
                properties.addAll(metadataEditor.getProperties());
                node.getNodeTypes().removeAll(metadataEditor.getRemovedTypes());
                node.getNodeTypes().addAll(metadataEditor.getAddedTypes());
            }

            // option properties
            if (optionsEditor != null) {
                properties.addAll(optionsEditor.getProperties());
                node.getNodeTypes().removeAll(optionsEditor.getRemovedTypes());
                node.getNodeTypes().addAll(optionsEditor.getAddedTypes());
            }

            // classification
            if (classificationEditor != null) {
                updatePropertiesListWithClassificationEditorData(properties);
            }

            // new acl
            final GWTJahiaNodeACL newNodeACL;
            if (rightsEditor != null) {
                newNodeACL = rightsEditor.getAcl();
            } else {
                newNodeACL = null;
            }

            // Ajax call to update values
            JahiaContentManagementService.App.getInstance().savePropertiesAndACL(nodes, newNodeACL, properties, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert(Messages.get("saved_prop_failed", "Properties save failed\n\n") + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display("", Messages.get("saved_prop", "Properties saved\n\n"));
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
            if (node.getProperties().containsKey("j:defaultCategory")) {
                if (values.isEmpty()) {
                    node.getNodeTypes().remove("jmix:categorized");
                } else {
                    list.add(gwtJahiaNodeProperty);
                }
            } else {
                if (!values.isEmpty()) {
                    node.getNodeTypes().add("jmix:categorized");
                    list.add(gwtJahiaNodeProperty);
                }
            }

            gwtJahiaNodes = classificationEditor.getTagStore().getAllItems();
            values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
            for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
                values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode, GWTJahiaNodePropertyType.WEAKREFERENCE));
            }
            gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
            gwtJahiaNodeProperty.setMultiple(true);
            gwtJahiaNodeProperty.setValues(values);
            gwtJahiaNodeProperty.setName("j:tags");
            if (node.getProperties().containsKey("j:tags")) {
                if (values.isEmpty()) {
                    node.getNodeTypes().remove("jmix:tagged");
                } else {
                    list.add(gwtJahiaNodeProperty);
                }
            } else {
                if (!values.isEmpty()) {
                    node.getNodeTypes().add("jmix:tagged");
                    list.add(gwtJahiaNodeProperty);
                }
            }
        }
    }

    private class CreateSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            String nodeName = targetName;
            if (isNodeNameFieldDisplayed) {
                nodeName = ((TextField<?>) ((FormPanel) contentTab.getItem(0)).getItem(0)).getRawValue();
                if (nodeName.equals("Automatically Created (you can type your name here if you want)")) {
                    nodeName = targetName;
                }
            }
            List<GWTJahiaNodeProperty> props = new ArrayList<GWTJahiaNodeProperty>();
            List<String> mixin = new ArrayList<String>();
            if (propertiesEditor != null) {
                props.addAll(propertiesEditor.getProperties());
                mixin.addAll(propertiesEditor.getAddedTypes());
            }
            if (layoutEditor != null) {
                props.addAll(layoutEditor.getProperties());
                mixin.addAll(layoutEditor.getAddedTypes());
                mixin.addAll(layoutEditor.getTemplateTypes());
            }
            if (metadataEditor != null) {
                props.addAll(metadataEditor.getProperties());
                mixin.addAll(metadataEditor.getAddedTypes());
            }
            if (optionsEditor != null) {
                props.addAll(optionsEditor.getProperties());
                mixin.addAll(optionsEditor.getAddedTypes());
            }

            if (createInParentAndMoveBefore) {
                JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(parent.getPath(), nodeName, type.getName(), mixin, props, null, new AsyncCallback<Object>() {
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
                JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), nodeName, type.getName(), mixin, props, null, new AsyncCallback<GWTJahiaNode>() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(GWTJahiaNode node) {
                        Info.display("", "Node " + node.getName() + "created");
                        EditContentEngine.this.hide();
                        linker.refreshMainComponent();
                        if (node.isPage()) {
                            linker.refreshLeftPanel(EditLinker.REFRESH_PAGES);
                        }
                        if (node.getNodeTypes().contains("jnt:reusableComponent")) {
                            linker.refreshLeftPanel();
                        }
                    }
                });
            }
        }


    }

    private class CreateAndAddNewSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            String nodeName = targetName;
            if (isNodeNameFieldDisplayed) {
                nodeName = ((TextField<?>) ((FormPanel) contentTab.getItem(0)).getItem(0)).getRawValue();
                if (nodeName.equals("Automatically Created (you can type your name here if you want)")) {
                    nodeName = targetName;
                }
            }
            List<GWTJahiaNodeProperty> props = new ArrayList<GWTJahiaNodeProperty>();
            List<String> mixin = new ArrayList<String>();
            if (propertiesEditor != null) {
                props.addAll(propertiesEditor.getProperties());
                mixin.addAll(propertiesEditor.getAddedTypes());
            }
            if (layoutEditor != null) {
                props.addAll(layoutEditor.getProperties());
                mixin.addAll(layoutEditor.getAddedTypes());
                mixin.addAll(layoutEditor.getTemplateTypes());
            }
            if (metadataEditor != null) {
                props.addAll(metadataEditor.getProperties());
                mixin.addAll(metadataEditor.getAddedTypes());
            }
            if (optionsEditor != null) {
                props.addAll(optionsEditor.getProperties());
                mixin.addAll(optionsEditor.getAddedTypes());
            }

            if (createInParentAndMoveBefore) {
                JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(parent.getPath(), nodeName, type.getName(), mixin, props, null, new AsyncCallback<Object>() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(Object o) {
                        Info.display("", "Node created");
                        linker.refreshMainComponent();
                        EditContentEngine.this.removeAll(true);
                        EditContentEngine.this.initTabs();
                        EditContentEngine.this.layout(true);
                    }
                });
            } else {
                JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), nodeName, type.getName(), mixin, props, null, new AsyncCallback<GWTJahiaNode>() {
                    public void onFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                    }

                    public void onSuccess(GWTJahiaNode node) {
                        Info.display("", "Node created");
                        linker.refreshMainComponent();
                        if (node.getNodeTypes().contains("jnt:reusableComponent")) {
                            linker.refreshLeftPanel();
                        }
                        EditContentEngine.this.removeAll(true);
                        EditContentEngine.this.initTabs();
                        EditContentEngine.this.layout(true);
                    }
                });
            }
        }
    }
}
