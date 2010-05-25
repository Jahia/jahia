package org.jahia.ajax.gwt.client.core;

import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;

import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;



/**
 * Common entry point for all our custom entry points.
 * Use this class to share behavior between all our entry points.
 *
 * @author Serge Huber.
 * Date: Dec 16, 2009
 * Time: 10:39:39 AM
 */
public class CommonEntryPoint implements EntryPoint {
    
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
        Timer t = new Timer() {
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
        t.run();
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
}

