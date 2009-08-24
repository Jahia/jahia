package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListModule extends ContentPanel implements Module {

    private GWTJahiaNode node;
    private HTML html;
    private String path;
    private Module parentModule;
    private EditManager editManager;

    public ListModule(String path, String s, EditManager editManager) {
//        super(new FitLayout());
        this.path = path;
        this.editManager = editManager;
        setCollapsible(true);
        setBodyStyleName("pad-text");
        setHeading("Content : "+path);
        setScrollMode(Style.Scroll.AUTO);
        html = new HTML(s);
        add(html);
    }

    public void parse() {
        Map<Element, Module> m = ModuleHelper.parse(this);
    }

    public HTML getHtml() {
        return html;
    }

    public LayoutContainer getContainer() {
        return this;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
    }

    public Module getParentModule() {
        return parentModule;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }
}
