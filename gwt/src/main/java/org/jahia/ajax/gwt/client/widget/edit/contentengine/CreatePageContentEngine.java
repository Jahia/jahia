package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 7, 2010
 * Time: 2:23:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatePageContentEngine extends CreateContentEngine {
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
//        tabs.add(new CreatePageTabItem(this));
        tabs.add(new ContentTabItem(this));
        tabs.add(new LayoutTabItem(this));
        tabs.add(new MetadataTabItem(this));
        tabs.add(new ClassificationTabItem(this));
        tabs.add(new OptionsTabItem(this));
        tabs.add(new RightsTabItem(this));
    }
}
