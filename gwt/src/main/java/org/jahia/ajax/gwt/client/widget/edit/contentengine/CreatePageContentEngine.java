package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        tabs.add(new LayoutTabItem(this));
        tabs.add(new MetadataTabItem(this));
        tabs.add(new ClassificationTabItem(this));
        tabs.add(new OptionsTabItem(this));
        tabs.add(new RightsTabItem(this));
    }

    @Override
    protected void save(final boolean closeAfterSave) {
        if (createPageTab.getReusableComponent().isEmpty()) {
            super.save(closeAfterSave);
        } else {
            String nodeName = targetName;
            final List<GWTJahiaNodeProperty> props = new ArrayList<GWTJahiaNodeProperty>();
            final List<String> mixin = new ArrayList<String>();


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
                } else if (item instanceof CreatePageTabItem) {
                    props.add(new GWTJahiaNodeProperty("jcr:title",new GWTJahiaNodePropertyValue(((CreatePageTabItem) item).getContentTitle(),  GWTJahiaNodePropertyType.STRING)));
                }
            }

            final GWTJahiaNode gwtJahiaNode = createPageTab.getReusableComponent().get(0);
            contentService.pasteAndSaveProperties(Arrays.asList(gwtJahiaNode.getPath()+ "/j:target"), parent.getPath() , gwtJahiaNode.getName(),false, null, props, new AsyncCallback() {
                public void onFailure(Throwable caught) {
                    Window.alert("error1: "+caught);
                }

                public void onSuccess(Object result) {
                    if (closeAfterSave) {
                        Info.display("", "Page " + node.getName() + "created");
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
