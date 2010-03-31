package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Interface defining what is a mdoule on a rendered page in edit mode
 * A module could be selected, edited, dragged etc.
 */
public abstract class Module extends LayoutContainer {

    protected String id;
    protected GWTJahiaNode node;
    protected HTML html;
    protected String path;
    protected String template;
    protected String scriptInfo;
    protected Module parentModule;
    protected MainModule mainModule;
    protected String nodeTypes;
    protected boolean isDraggable = false;
    protected int depth;
    protected boolean selectable;
    protected String headerText;
    protected Header head;
    protected boolean locked;

    public Module() {
    }

    public Module(Layout layout) {
        super(layout);
    }

    protected Module(String id, String path, HTML html, String template, String scriptInfo, String nodeTypes, boolean locked, MainModule mainModule) {
        this.id = id;
        this.path = path;
        this.html = html;
        this.template = template;
        this.scriptInfo = scriptInfo;
        this.nodeTypes = nodeTypes;
        this.locked = locked;
        this.mainModule = mainModule;
    }

    public void onParsed() {

    }

    public Module getParentModule() {
        return parentModule;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    public String getModuleId() {
        return id;
    }

    public HTML getHtml() {
        return html;
    }

    public LayoutContainer getContainer() {
        return this;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public void setNode(GWTJahiaNode node) {
        if (mainModule.getConfig().getName().equals("studiomode")) {
            if (node.getNodeTypes().contains("jmix:templateShared") && node.getNodeTypes().contains("jmix:templateLocked")) {
                head.setText(headerText + " (locked & shared)");
            } else if (node.getNodeTypes().contains("jmix:templateShared")) {
                head.setText(headerText + " (shared)");
            } else if (node.getNodeTypes().contains("jmix:templateLocked")) {
                head.setText(headerText + " (locked)");
            }
        }
        this.node = node;
    }

    public String getNodeTypes() {
        return nodeTypes;
    }

    public String getTemplate() {
        return template;
    }

    public void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    public boolean isDraggable() {
        return isDraggable;
    }

}
