package org.jahia.ajax.gwt.module.edit.client;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.widget.edit.EditPanelViewport;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:53:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditEntryPoint extends CommonEntryPoint {
    public void afterPermissionsLoad() {
        super.afterPermissionsLoad();
        RootPanel panel = RootPanel.get("editmode") ;
        if (panel != null) {
            panel.add(new EditPanelViewport(DOM.getInnerHTML(panel.getElement()),DOM.getElementAttribute(panel.getElement(), "path"),DOM.getElementAttribute(panel.getElement(), "template"), 
                    DOM.getElementAttribute(panel.getElement(), "locale"))) ;
        }
    }
}
