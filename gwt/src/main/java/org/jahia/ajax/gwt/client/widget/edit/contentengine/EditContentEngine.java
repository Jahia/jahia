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
package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.ClassificationEditor;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

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

    private TabPanel tabs;

    private Linker linker = null;
    private GWTJahiaNode parent = null;
    private GWTJahiaNodeType type = null;
    private String targetName = null;
    private boolean createInParentAndMoveBefore = false;

    private ButtonBar buttonBar;
    private Button ok;
    private Button cancel;
    public static final int BUTTON_HEIGHT = 24;
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

        tabs.add(new ContentTabItem(this));
        tabs.add(new LayoutTabItem(this));
        tabs.add(new MetadataTabItem(this));
        tabs.add(new ClassificationTabItem(this));
        tabs.add(new OptionsTabItem(this));
        tabs.add(new RightsTabItem(this));
//        tabs.add(new CreatePageTabItem(this));

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

        if (currentTab instanceof EditEngineTabItem) {
            EditEngineTabItem engineTabItem = (EditEngineTabItem) currentTab;
            if (!engineTabItem.isProcessed()) {
                engineTabItem.create();
            }
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

                fillCurrentTab();
                ok.setEnabled(true);
                //restore.setEnabled(true);
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

    public Linker getLinker() {
        return linker;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public boolean isExistingNode() {
        return existingNode;
    }

    public List<GWTJahiaNodeType> getNodeTypes() {
        return nodeTypes;
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public Map<String, GWTJahiaNodeProperty> getProps() {
        return props;
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
            nodes.add(node);

            // general properties
            final List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();

            // new acl
            GWTJahiaNodeACL newNodeACL = null;

            for (TabItem item : tabs.getItems()) {
                if (item instanceof PropertiesTabItem) {
                    PropertiesEditor pe = ((PropertiesTabItem) item).getPropertiesEditor();
                    if (pe != null) {
                        properties.addAll(pe.getProperties());
                        node.getNodeTypes().removeAll(pe.getRemovedTypes());
                        node.getNodeTypes().addAll(pe.getAddedTypes());
                        node.getNodeTypes().addAll(pe.getTemplateTypes());
                    }
                    if (item instanceof ContentTabItem) {
                        if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                            node.setName(((TextField<?>) ((FormPanel) item.getItem(0)).getItem(0)).getRawValue());
                        }
                    }

                } else if (item instanceof RightsTabItem) {
                    AclEditor acl = ((RightsTabItem) item).getRightsEditor();
                    if (acl != null) {
                        newNodeACL = acl.getAcl();
                    }
                } else if (item instanceof ClassificationTabItem) {
                    updatePropertiesListWithClassificationEditorData(((ClassificationTabItem) item).getClassificationEditor(), properties);
                }
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

        private void updatePropertiesListWithClassificationEditorData(ClassificationEditor classificationEditor, List<GWTJahiaNodeProperty> list) {
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
            save(true);
        }
    }

    private class CreateAndAddNewSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            save(false);
        }
    }

    private void save(final boolean closeAfterSave) {
        String nodeName = targetName;
        List<GWTJahiaNodeProperty> props = new ArrayList<GWTJahiaNodeProperty>();
        List<String> mixin = new ArrayList<String>();


        for (TabItem item : tabs.getItems()) {
            if (item instanceof PropertiesTabItem) {
                PropertiesEditor pe = ((PropertiesTabItem) item).getPropertiesEditor();
                if (pe != null) {
                    props.addAll(pe.getProperties());
                    mixin.addAll(pe.getAddedTypes());
                    mixin.addAll(pe.getTemplateTypes());
                }
                if (item instanceof ContentTabItem) {
                    if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                        nodeName = ((TextField<?>) ((FormPanel) item.getItem(0)).getItem(0)).getRawValue();
                        if (nodeName.equals("Automatically Created (you can type your name here if you want)")) {
                            nodeName = targetName;
                        }
                    }
                }
            } else if (item instanceof RightsTabItem) {
                AclEditor acl = ((RightsTabItem) item).getRightsEditor();
                // ?
            } else if (item instanceof ClassificationTabItem) {
                // ?
            }
        }

        if (createInParentAndMoveBefore) {
            JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(parent.getPath(), nodeName, type.getName(), mixin, props, null, new AsyncCallback<Object>() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display("", "Node created");
                    if (closeAfterSave) {
                        EditContentEngine.this.hide();
                    } else {
                        EditContentEngine.this.removeAll(true);
                        EditContentEngine.this.initTabs();
                        EditContentEngine.this.layout(true);
                    }
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
                    if (closeAfterSave) {
                        Info.display("", "Node " + node.getName() + "created");
                        EditContentEngine.this.hide();
                    } else {
                        EditContentEngine.this.removeAll(true);
                        EditContentEngine.this.initTabs();
                        EditContentEngine.this.layout(true);
                    }

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
