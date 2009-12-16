package org.jahia.ajax.gwt.module.edit.client;

import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.widget.content.ContentManager;
import org.jahia.ajax.gwt.client.widget.edit.EditManager;
import org.jahia.ajax.gwt.client.widget.edit.EditPanelViewport;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:53:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditEntryPoint extends CommonEntryPoint {
    public void onModuleLoad() {
        super.onModuleLoad();
        RootPanel panel = RootPanel.get("editmode") ;
        if (panel != null) {
            panel.add(new EditPanelViewport(DOM.getElementAttribute(panel.getElement(), "path"),DOM.getElementAttribute(panel.getElement(), "template"), 
                    DOM.getElementAttribute(panel.getElement(), "locale"))) ;
        }
    }
}
