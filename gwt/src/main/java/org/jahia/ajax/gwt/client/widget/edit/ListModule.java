package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListModule extends ContentPanel {

    private HTML html;

    public ListModule(String path, String s) {
//        super(new FitLayout());

        setCollapsible(true);
        setBodyStyleName("pad-text");
        setHeading("Content : "+path);
        setScrollMode(Style.Scroll.AUTO);
        html = new HTML(s);
        add(html);
        ModuleHelper.parse(this,html);
    }

    public HTML getHtml() {
        return html;
    }

}
