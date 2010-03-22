package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 7, 2010
 * Time: 2:23:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatePageContentEngine extends CreateContentEngine {
    protected CreatePageTabItem createPageTab;

    public CreatePageContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName) {
        super(linker, parent, type, targetName);
    }

    public CreatePageContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName, boolean createInParentAndMoveBefore) {
        super(linker, parent, type, targetName, createInParentAndMoveBefore);
    }

    public CreatePageContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, Map<String, GWTJahiaNodeProperty> props, String targetName, boolean createInParentAndMoveBefore) {
        super(linker, parent, type, props, targetName, createInParentAndMoveBefore);
    }

    @Override
    protected void initTabs() {
        createPageTab = new CreatePageTabItem(this);
        tabs.add(createPageTab);
//        tabs.add(new ContentTabItem(this));
//        tabs.add(new LayoutTabItem(this));
        tabs.add(new MetadataTabItem(this));
        tabs.add(new ClassificationTabItem(this));
        tabs.add(new OptionsTabItem(this));
        tabs.add(new RightsTabItem(this));
    }

    @Override
    protected void save(final boolean closeAfterSave) {
        if (createPageTab.getTemplate().isEmpty()) {
            super.save(closeAfterSave);
        } else {
            String nodeName = null;
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
                        langCodeProperties.putAll(propertiesTabItem.getLangPropertiesMap(false));
                        if (pe != null) {
                            props.addAll(pe.getProperties(false, true, false));
                        }
                    } else {
                        if (pe != null) {
                            props.addAll(pe.getProperties());
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

            final GWTJahiaNode gwtJahiaNode = createPageTab.getTemplate().get(0);
            contentService.copyAndSaveProperties(Arrays.asList(gwtJahiaNode.getPath()), parentNode.getPath(), mixin , newNodeACL, langCodeProperties, props, new AsyncCallback() {
                public void onFailure(Throwable caught) {
                    Window.alert("error1: "+caught);
                }

                public void onSuccess(Object result) {
                    if (closeAfterSave) {
                        Info.display("", "Page created");
                        CreatePageContentEngine.this.hide();
                    } else {
                        CreatePageContentEngine.this.tabs.removeAll();
                        CreatePageContentEngine.this.initTabs();
                        CreatePageContentEngine.this.layout(true);
                    }

                    linker.refreshMainComponent();
                    linker.refreshLeftPanel(EditLinker.REFRESH_PAGES);
                }
            });
        }
    }
}
