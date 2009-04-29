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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;

import java.util.Iterator;
import java.util.Map;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 10:44:01
 */
public class OpenWindowJahiaToolItemProvider extends AbstractJahiaToolItemProvider {
    /**
     * Get Selectect Listener
     * @param gwtToolbarItem
     * @return
     */
    public SelectionListener<ComponentEvent> getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem) {
        // add listener
        SelectionListener<ComponentEvent> listener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                Map preferences = gwtToolbarItem.getProperties();
                final GWTJahiaProperty windowUrl = (GWTJahiaProperty) preferences.get(ToolbarConstants.URL);
                if (Log.isDebugEnabled()) {
                    Iterator it = preferences.keySet().iterator();
                    while (it.hasNext()) {
                        Log.debug("Found property: " + it.next());
                    }
                }
                final GWTJahiaProperty windowWidth = (GWTJahiaProperty) preferences.get(ToolbarConstants.WIDTH);
                String wWidth = "" ;
                if (windowWidth == null) {
                    Log.debug("Warning: width not found - nb. preferences:" + preferences.size());
                     wWidth = ",width=900";
                } else {
                    wWidth = ",width=" + windowWidth.getValue() ;
                }

                final GWTJahiaProperty windowHeight = (GWTJahiaProperty) preferences.get(ToolbarConstants.HEIGHT);
                String wHeight = "" ;
                if (windowHeight == null) {
                    wHeight = ",height=600";
                } else {
                    wHeight = ",height=" + windowHeight.getValue() ;
                }

                final String wOptions = "directories=no,scrollbars=yes,resizable=yes,status=no,location=no" + wWidth + wHeight ;
                if (windowUrl != null && windowUrl.getValue() != null) {
                    String name = gwtToolbarItem.getTitle().replaceAll(" ", "_") ;
                    Window.open(windowUrl.getValue(), name, wOptions);
                }
            }
        };
        return listener;
    }

    /**
     * Create a new tool Item
     * @param gwtToolbarItem
     * @return
     */
    public ToolItem createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        return new TextToolItem();
    }

}
