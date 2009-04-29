/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.layoutmanager.listener;

import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.util.layoutmanager.JahiaPropertyHelper;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.widget.layoutmanager.JahiaPortalManager;
import org.jahia.ajax.gwt.client.widget.layoutmanager.portlet.JahiaPortlet;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * User: ktlili
 * Date: 17 nov. 2008
 * Time: 21:34:51
 */
public class OnPortletStatusChanged implements Listener<ComponentEvent> {
    private JahiaPortlet jahiaPortlet;
    private String newStatus;

    public OnPortletStatusChanged(JahiaPortlet jahiaPortlet) {
        this.jahiaPortlet = jahiaPortlet;
    }

    public OnPortletStatusChanged(JahiaPortlet jahiaPortlet, String newStatus) {
        this.jahiaPortlet = jahiaPortlet;
        this.newStatus = newStatus;
    }

    public void handleEvent(ComponentEvent event) {
        // create properties list
        final String oldStatus = jahiaPortlet.getStatus();
        final String status;
        if (newStatus != null) {
            status = newStatus;
        } else {
            if (JahiaPropertyHelper.isStatusMinimized(jahiaPortlet.getPorletConfig().getStatus())) {
                status = JahiaPropertyHelper.getStatusMaximizedValue();
            } else {
                status = JahiaPropertyHelper.getStatusMinimizedValue();
            }
        }
        Log.debug("JahiaPortlet[" + jahiaPortlet.getWindowId() + "] "+status+".");

        GWTJahiaLayoutItem gwtJahiaLayoutItem = jahiaPortlet.getPorletConfig();
        gwtJahiaLayoutItem.setStatus(status);

        // make a call ajax
        LayoutmanagerService.App.getInstance().saveLayoutItem(JahiaPageEntryPoint.getJahiaGWTPage(), gwtJahiaLayoutItem, new AsyncCallback() {
            public void onSuccess(Object o) {
                Log.debug("old status: "+oldStatus);
                if (JahiaPropertyHelper.isStatusFullScreen(oldStatus)) {
                   JahiaPortalManager.getInstance().refreshPortal();
                }
                jahiaPortlet.setStatus(status);
                jahiaPortlet.refreshStatus();
                // getJahiaDndArea().addToMaximizedPanel(draggableWidget);
            }

            public void onFailure(Throwable t) {
                // handle failure
                Window.alert(t.getLocalizedMessage());
            }
        });
    }
}
