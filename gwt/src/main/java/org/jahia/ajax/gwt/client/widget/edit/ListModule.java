package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListModule extends ContentPanel implements Module {

    private HTML html;
    private String path;

    public ListModule(String path, String s, EditManager editManager) {
//        super(new FitLayout());
        this.path = path;
        setCollapsible(true);
        setBodyStyleName("pad-text");
        setHeading("Content : "+path);
        setScrollMode(Style.Scroll.AUTO);
        html = new HTML(s);
        add(html);
        ModuleHelper.parse(this,html, editManager);
    }

    public HTML getHtml() {
        return html;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return null;
    }
}
