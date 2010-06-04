package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 7, 2010
 * Time: 2:23:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatePageContentEngine extends CreateContentEngine {
    protected GWTJahiaNode defaultTemplate;
    private boolean templateToPage = true;

    public CreatePageContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName,
                                   boolean templateToPage) {
        super(linker, parent, type, targetName);
        this.templateToPage = templateToPage;
    }

    public CreatePageContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, Map<String, GWTJahiaNodeProperty> props, GWTJahiaNode defaultTemplate,
                                   String targetName, boolean createInParentAndMoveBefore, boolean templateToPage) {
        super(linker, parent, type, props, targetName, createInParentAndMoveBefore);
        this.defaultTemplate = defaultTemplate;
        this.templateToPage = templateToPage;
    }

    protected void doSave(String nodeName, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<String> mixin, GWTJahiaNodeACL newNodeACL, final boolean closeAfterSave) {
        CreatePageTabItem createPageTab = (CreatePageTabItem) tabs.getItems().get(0);
        if (createPageTab.getTemplate().isEmpty()) {
            super.doSave(nodeName, props, langCodeProperties, mixin, newNodeACL, closeAfterSave);
        } else {
            final GWTJahiaNode template = createPageTab.getTemplate().get(0);
            if (templateToPage) {
                props.add(new GWTJahiaNodeProperty("j:sourceTemplate", new GWTJahiaNodePropertyValue(template.getUUID(), GWTJahiaNodePropertyType.WEAKREFERENCE)));
            }
            contentService.createPage(Arrays.asList(template.getPath()), parentNode.getPath(), mixin , newNodeACL, langCodeProperties, templateToPage, props, new BaseAsyncCallback() {
                public void onApplicationFailure(Throwable caught) {
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

                    linker.refresh(Linker.REFRESH_MAIN+ Linker.REFRESH_PAGES);
                }
            });
        }
    }

    public GWTJahiaNode getDefaultTemplate() {
        return defaultTemplate;
    }
}
