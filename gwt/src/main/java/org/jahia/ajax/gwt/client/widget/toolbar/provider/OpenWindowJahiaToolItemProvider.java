/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

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
     *
     * @param gwtToolbarItem
     * @return
     */
    public <T extends ComponentEvent> SelectionListener<T> getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem) {
        // add listener
        SelectionListener<T> listener = new SelectionListener<T>() {
            public void componentSelected(T event) {
                Map preferences = gwtToolbarItem.getProperties();
                final GWTJahiaProperty windowUrl = (GWTJahiaProperty) preferences.get(ToolbarConstants.URL);
                if (Log.isDebugEnabled()) {
                    Iterator it = preferences.keySet().iterator();
                    while (it.hasNext()) {
                        Log.debug("Found property: " + it.next());
                    }
                }
                final GWTJahiaProperty windowWidth = (GWTJahiaProperty) preferences.get(ToolbarConstants.WIDTH);
                String wWidth = "";
                if (windowWidth == null) {
                    Log.debug("Warning: width not found - nb. preferences:" + preferences.size());
                    wWidth = ",width=900";
                } else {
                    wWidth = ",width=" + windowWidth.getValue();
                }

                final GWTJahiaProperty windowHeight = (GWTJahiaProperty) preferences.get(ToolbarConstants.HEIGHT);
                String wHeight = "";
                if (windowHeight == null) {
                    wHeight = ",height=600";
                } else {
                    wHeight = ",height=" + windowHeight.getValue();
                }

                final String wOptions = "directories=no,scrollbars=yes,resizable=yes,status=no,location=no" + wWidth + wHeight;
                final String jsUrl = getPropertyValue(gwtToolbarItem, "js.url");
                if (jsUrl != null) {
                    String name = gwtToolbarItem.getTitle().replaceAll(" ", "_");
                    Window.open(JahiaGWTParameters.getParam(jsUrl), name, wOptions);
                } else if (windowUrl != null && windowUrl.getValue() != null) {
                    String name = gwtToolbarItem.getTitle().replaceAll(" ", "_");
                    Window.open(windowUrl.getValue(), name, wOptions);
                }
            }
        };
        return listener;
    }

    /**
     * Create a new tool Item
     *
     * @param gwtToolbarItem
     * @return
     */
    public Component createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        return new Button();
    }

}
