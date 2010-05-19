package org.jahia.ajax.gwt.module.edit.client;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngine;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.edit.EditPanelViewport;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:53:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditEntryPoint extends CommonEntryPoint {
    private JahiaContentManagementServiceAsync async;
    public void onModuleLoad() {
        @SuppressWarnings("unused")
        Layout junk = new AnchorLayout();
        checkSession();
        final RootPanel panel = RootPanel.get("editmode");
        if (panel != null) {
            JahiaContentManagementService.App.getInstance().getEditConfiguration(DOM.getElementAttribute(panel.getElement(), "config"), new BaseAsyncCallback<GWTEditConfiguration>() {
                public void onSuccess(GWTEditConfiguration gwtEditConfiguration) {
                    panel.add(new EditPanelViewport(DOM.getInnerHTML(panel.getElement()),
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

    public void checkSession() {
        Timer t = new Timer() {
            public void run() {
                async = JahiaContentManagementService.App.getInstance();
                try {
                    async.isValidSession(new AsyncCallback<Integer>() {
                        public void onFailure(Throwable throwable) {
                            Log.error("Error checking active session", throwable);
                        }

                        public void onSuccess(Integer val) {
                            if (val > 0) {
                                scheduleRepeating(val);
                                //Window.alert("your session awake");
                            } else if (val == 0) {
                               Window.Location.reload();
                            }
                        }
                    });
                } catch (GWTJahiaServiceException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        };
        t.run();
    }
}
