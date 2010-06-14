package org.jahia.ajax.gwt.module.edit.client;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.LoginBox;
import org.jahia.ajax.gwt.client.widget.edit.EditPanelViewport;

/**
 * Edit mode GWT entry point.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:53:34 PM
 */
public class EditEntryPoint extends CommonEntryPoint {
    public void onModuleLoad() {
        @SuppressWarnings("unused")
        Layout junk = new AnchorLayout();
        checkSession();
        final RootPanel panel = RootPanel.get("editmode");
        if (panel != null) {
            JahiaContentManagementService.App.getInstance().getEditConfiguration(DOM.getElementAttribute(panel.getElement(), "config"), new BaseAsyncCallback<GWTEditConfiguration>() {
                public void onSuccess(GWTEditConfiguration gwtEditConfiguration) {
                    final String s = DOM.getInnerHTML(panel.getElement());
                    DOM.setInnerHTML(panel.getElement(), "");
                    panel.add(new EditPanelViewport(s,
                            DOM.getElementAttribute(panel.getElement(), "path"),
                            DOM.getElementAttribute(panel.getElement(), "template"),
                            DOM.getElementAttribute(panel.getElement(), "locale"), gwtEditConfiguration));
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error when loading EditConfiguration", throwable);
                }
            });
        }
    }

}
