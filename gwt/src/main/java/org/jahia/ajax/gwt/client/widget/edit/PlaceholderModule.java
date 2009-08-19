package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.HTML;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaceholderModule extends LayoutContainer {

    public PlaceholderModule(String path) {
        super(new FlowLayout());
        setBorders(true);
        setWidth("80%");
        HTML html = new HTML("--placehodler--");
        add(html);
//        ModuleHelper.parse(this,html);
    }



}
