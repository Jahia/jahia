package org.jahia.ajax.gwt.client.core;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.xhr.client.XMLHttpRequest;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;

import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;
import org.jahia.ajax.gwt.client.widget.LoginBox;


/**
 * Common entry point for all our custom entry points.
 * Use this class to share behavior between all our entry points.
 *
 * @author Serge Huber.
 * Date: Dec 16, 2009
 * Time: 10:39:39 AM
 */
public class CommonEntryPoint implements EntryPoint {

    private static Timer sessionCheckTimer;

    JahiaContentManagementServiceAsync contentManagementService;

    public void onModuleLoad() {
        /* todo The following two lines are a hack to get development mode to work on Mac OS X, should be removed once this
           problem is fixed.
         */
        @SuppressWarnings("unused")
        Layout junk = new AnchorLayout();
        /* End of GWT hack */
    }
    
    protected void checkSession() {
        sessionCheckTimer = new Timer() {
            public void run() {
                try {
                    getContentManagementService().isValidSession(new BaseAsyncCallback<Integer>() {
                        public void onSuccess(Integer val) {
                            if (val > 0) {
                               scheduleRepeating(val);
                            } else if (val == 0) {
                               cancel();
                               handleSessionExpired(this);
                            }
                        }
                    });
                } catch (GWTJahiaServiceException e) {
                    e.printStackTrace();
                }
            }
        };
        sessionCheckTimer.run();
    }

    protected JahiaContentManagementServiceAsync getContentManagementService() {
        if (contentManagementService == null) {
            contentManagementService = JahiaContentManagementService.App.getInstance();
        }
        
        return contentManagementService;
    }
    
    protected void handleSessionExpired(BaseAsyncCallback<?> callback) {
        callback.onSessionExpired();
    }

    public static Timer getSessionCheckTimer() {
        return sessionCheckTimer;
    }

}

