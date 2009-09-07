package org.jahia.ajax.gwt.module.edit.client;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
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
public class EditEntryPoint {
    public void onModuleLoad() {
        RootPanel panel = RootPanel.get("editmode") ;
        if (panel != null) {
            panel.add(new EditPanelViewport(DOM.getElementAttribute(panel.getElement(), "path"),DOM.getElementAttribute(panel.getElement(), "template"), 
                    DOM.getElementAttribute(panel.getElement(), "locale"))) ;
        }
    }
}
