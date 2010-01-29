package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:55:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateContentEngine extends AbstractContentEngine {

    protected GWTJahiaNodeType type = null;
    protected String targetName = null;
    protected boolean createInParentAndMoveBefore = false;


    /**
     * Open Edit content engine for a new node creation
     *
     * @param linker
     * @param parent
     * @param type
     * @param targetName
     */
    public CreateContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName) {
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
    public CreateContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName, boolean createInParentAndMoveBefore) {
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
    public CreateContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, Map<String, GWTJahiaNodeProperty> props, String targetName, boolean createInParentAndMoveBefore) {
        super(linker);
        this.existingNode = false;
        this.parentNode = parent;
        this.type = type;
        if (!"*".equals(targetName)) {
            this.targetName = targetName;
        }
        this.createInParentAndMoveBefore = createInParentAndMoveBefore;

        nodeTypes = new ArrayList<GWTJahiaNodeType>(1);
        nodeTypes.add(type);
        properties = new HashMap<String, GWTJahiaNodeProperty>(props);
        heading = "Create " + type.getName();
        loadMixin();

        init();
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        tabs.add(new ContentTabItem(this));
        tabs.add(new LayoutTabItem(this));
        tabs.add(new MetadataTabItem(this));
        tabs.add(new ClassificationTabItem(this));
        tabs.add(new OptionsTabItem(this));
        tabs.add(new RightsTabItem(this));
//        tabs.add(new CreatePageTabItem(this));
    }


    /**
     * init buttons
     */
    protected void initFooter() {
        Button ok = new Button(Messages.getResource("fm_save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setEnabled(true);
        ok.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());
        ok.addSelectionListener(new CreateSelectionListener());

        buttonBar.add(ok);

        Button okAndNew = new Button(Messages.getResource("fm_saveAndNew"));
        okAndNew.setHeight(BUTTON_HEIGHT);
        okAndNew.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());

        okAndNew.addSelectionListener(new CreateAndAddNewSelectionListener());
        buttonBar.add(okAndNew);

        Button cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                CreateContentEngine.this.hide();
            }
        });
        buttonBar.add(cancel);
    }

    /**
     * on language chnage, fill currentAzble
     */
    protected void onLanguageChange() {
        fillCurrentTab();
    }


    /**
     * load mixin
     */
    private void loadMixin() {
        definitionService.getAvailableMixin(nodeTypes.iterator().next(), new AsyncCallback<List<GWTJahiaNodeType>>() {
            public void onSuccess(List<GWTJahiaNodeType> result) {
                mixin = result;
                contentService.getSiteLanguages(new AsyncCallback<List<GWTJahiaLanguage>>() {
                    public void onSuccess(List<GWTJahiaLanguage> gwtJahiaLanguages) {
                        if (gwtJahiaLanguages != null && !gwtJahiaLanguages.isEmpty()) {
                            for (GWTJahiaLanguage gwtJahiaLanguage : gwtJahiaLanguages) {
                                if (gwtJahiaLanguage.isCurrent()) {
                                    defaultLanguageBean = gwtJahiaLanguage;
                                    break;
                                }
                            }
                            setAvailableLanguages(gwtJahiaLanguages);                            
                        }
                        fillCurrentTab();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to load avalibale mixin", throwable);
                    }
                });

            }

            public void onFailure(Throwable caught) {
                Log.error("Unable to load avalibale mixin", caught);
            }
        });
    }


    protected class CreateSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            save(true);
        }
    }

    protected class CreateAndAddNewSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            save(false);
        }
    }

    protected void save(final boolean closeAfterSave) {
        String nodeName = targetName;
        final List<GWTJahiaNodeProperty> props = new ArrayList<GWTJahiaNodeProperty>();
        final Map<String, List<GWTJahiaNodeProperty>> langCodeProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();
        final List<String> mixin = new ArrayList<String>();

        // new acl
        GWTJahiaNodeACL newNodeACL = null;

        for (TabItem item : tabs.getItems()) {
            if (item instanceof PropertiesTabItem) {
                PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                PropertiesEditor pe = ((PropertiesTabItem) item).getPropertiesEditor();
                if (pe != null) {
                    // props.addAll(pe.getProperties());
                    mixin.addAll(pe.getAddedTypes());
                    mixin.addAll(pe.getTemplateTypes());
                }

                // handle multilang
                if (propertiesTabItem.isMultiLang()) {
                    // for now only contentTabItem  has multilang. properties
                    langCodeProperties.putAll(propertiesTabItem.getLangPropertiesMap());
                    if (pe != null) {
                        props.addAll(pe.getProperties(false, true));
                    }
                } else {
                    if (pe != null) {
                        props.addAll(pe.getProperties());
                    }
                }

                if (item instanceof ContentTabItem) {
                    if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                        String nodeNameValue = ((ContentTabItem) item).getName().getValue();
                        nodeName = "Automatically Created (you can type your name here if you want)".equals(nodeNameValue) ? targetName : nodeNameValue;
                    }
                }
            } else if (item instanceof RightsTabItem) {
                AclEditor acl = ((RightsTabItem) item).getRightsEditor();
                if (acl != null) {
                    newNodeACL = acl.getAcl();
                }
            } else if (item instanceof ClassificationTabItem) {
                ((ClassificationTabItem) item).updatePropertiesListWithClassificationEditorData(((ClassificationTabItem) item).getClassificationEditor(), props, mixin);
            }
        }

        if (createInParentAndMoveBefore) {
            JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(parentNode.getPath(), nodeName, type.getName(), mixin, newNodeACL, props,langCodeProperties, null, new AsyncCallback<Object>() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display("", "Node created");
                    if (closeAfterSave) {
                        CreateContentEngine.this.hide();
                    } else {
                        CreateContentEngine.this.tabs.removeAll();
                        CreateContentEngine.this.initTabs();
                        CreateContentEngine.this.layout(true);
                    }
                    linker.refreshMainComponent();
                }
            });
        } else {
            JahiaContentManagementService.App.getInstance().createNode(parentNode.getPath(), nodeName, type.getName(), mixin, newNodeACL, props,langCodeProperties, null, new AsyncCallback<GWTJahiaNode>() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(GWTJahiaNode node) {
                    if (closeAfterSave) {
                        Info.display("", "Node " + node.getName() + "created");
                        CreateContentEngine.this.hide();
                    } else {
                        CreateContentEngine.this.tabs.removeAll();
                        CreateContentEngine.this.initTabs();
                        CreateContentEngine.this.layout(true);
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
