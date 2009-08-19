package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.ui.HTML;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListModule extends ContentPanel {

    private HTML html;

    public ListModule(String path, String s, EditManager editManager) {
//        super(new FitLayout());

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

}
