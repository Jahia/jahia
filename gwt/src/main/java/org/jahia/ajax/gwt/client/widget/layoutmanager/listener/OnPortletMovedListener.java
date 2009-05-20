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

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.PortalEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Component;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;

import java.util.List;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.widget.layoutmanager.portlet.JahiaPortal;
import org.jahia.ajax.gwt.client.widget.layoutmanager.portlet.JahiaPortlet;

/**
 * User: ktlili
 * Date: 28 ao�t 2008
 * Time: 16:32:40
 */
public class OnPortletMovedListener implements Listener<PortalEvent> {
    private JahiaPortal portal;

    public OnPortletMovedListener(JahiaPortal portal) {
        this.portal = portal;
    }

    public void handleEvent(PortalEvent event) {
        final List<GWTJahiaLayoutItem> layoutItemList = new ArrayList<GWTJahiaLayoutItem>();
        Log.debug("OnPortletMovedListener: update old column:  " + event.startColumn);
        Log.debug("OnPortletMovedListener: update new column:  " + event.column);
        // update porlet state of the new column
        int currentRowIndex = 0;

        // portlets of the new columns
        LayoutContainer layoutContainer = portal.getItem(event.column);
        int newColumnIndex = event.column;
        for (Component component : layoutContainer.getItems()) {
            JahiaPortlet portlet = (JahiaPortlet) component;
            // update columns
            portlet.setColumnIndex(newColumnIndex);
            portlet.setColumn(layoutContainer);
            portlet.setRowIndex(currentRowIndex);

            // update portletStates map
            updateLayoutItems(layoutItemList, portlet);
            Log.debug("OnPortletMovedListener: update portlet:  " + portlet.getWindowId() + "," + currentRowIndex);

            currentRowIndex++;
        }

        // portlets of the old columns
        int oldColumnIndex = event.startColumn;
        if (oldColumnIndex != newColumnIndex) {
            LayoutContainer oldLayoutContainer = portal.getItem(oldColumnIndex);
            currentRowIndex = 0;
            Log.debug("OnPortletMovedListener: update " + oldLayoutContainer.getItems() + " portlets.");

            for (Component component : oldLayoutContainer.getItems()) {
                JahiaPortlet portlet = (JahiaPortlet) component;
                // update columns
                portlet.setColumnIndex(oldColumnIndex);
                portlet.setColumn(oldLayoutContainer);
                portlet.setRowIndex(currentRowIndex);

                // update portletStates map
                updateLayoutItems(layoutItemList, portlet);

                Log.debug("OnPortletMovedListener: update portlet:  " + portlet.getWindowId() + "," + currentRowIndex);

                currentRowIndex++;

            }
        }


        // make a call ajax
        LayoutmanagerService.App.getInstance().saveLayoutItems(JahiaPageEntryPoint.getJahiaGWTPage(), layoutItemList, new AsyncCallback() {
            public void onSuccess(Object o) {
                Log.debug("Layout manager updated successfuly.");
            }

            public void onFailure(Throwable t) {
                Window.alert("Unable to execute ajax request " + t.getLocalizedMessage());
                t.printStackTrace();
            }
        });

    }


    /**
     * Update portlets states map
     *
     * @param layoutItems
     * @param portlet
     */
    private void updateLayoutItems(List<GWTJahiaLayoutItem> layoutItems, JahiaPortlet portlet) {
        // get prferences object
        GWTJahiaLayoutItem gwtLayoutItem = portlet.getPorletConfig();
        
        // add to pref list
        layoutItems.add(gwtLayoutItem);

    }
}

