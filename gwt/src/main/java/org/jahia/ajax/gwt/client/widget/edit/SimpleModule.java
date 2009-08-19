package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.fx.Draggable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.core.client.GWT;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:25:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleModule  extends ContentPanel {

    private HTML html;

    public SimpleModule(String path, String s) {
//        super(new FitLayout());
        setHeaderVisible(false);
        setScrollMode(Style.Scroll.AUTO);

        Draggable d = new Draggable(this);

        html = new HTML(s);
        add(html);
        ModuleHelper.parse(this,html);
    }

    public HTML getHtml() {
        return html;
    }

}
