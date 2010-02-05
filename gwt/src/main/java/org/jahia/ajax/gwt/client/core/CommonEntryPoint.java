package org.jahia.ajax.gwt.client.core;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;

import java.util.List;

/**
 * Common entry point for all our custom entry points. Use this class to share behavior between all our entry points.
 *
 * @author Serge Huber.
 *         Date: Dec 16, 2009
 *         Time: 10:39:39 AM
 */
public class CommonEntryPoint implements EntryPoint {
    private boolean loadPermisson = true;

    public void onModuleLoad() {
        /* todo The following two lines are a hack to get development mode to work on Mac OS X, should be removed once this
           problem is fixed.
         */
        @SuppressWarnings("unused")
        Layout junk = new AnchorLayout();
        /* End of GWT hack */
        if (loadPermisson) {
            loadPermissions();
        }

    }

    /**
     * load permission
     */
    public void loadPermissions() {
        JahiaContentManagementService.App.getInstance().getGrantedPermissions(new AsyncCallback<List<GWTJahiaPermission>>() {
            public void onSuccess(List<GWTJahiaPermission> gwtJahiaPermissions) {
                Log.debug("Permissions loaded");
                PermissionsUtils.loadPermissions(gwtJahiaPermissions);
                afterPermissionsLoad();
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error while loading user permission", throwable);
                afterPermissionsLoad();
            }
        });
    }

    /**
     * to be overriden by subClasses
     */
    public void afterPermissionsLoad() {

    }

}

