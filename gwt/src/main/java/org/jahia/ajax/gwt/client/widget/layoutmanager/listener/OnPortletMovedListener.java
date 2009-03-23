/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

