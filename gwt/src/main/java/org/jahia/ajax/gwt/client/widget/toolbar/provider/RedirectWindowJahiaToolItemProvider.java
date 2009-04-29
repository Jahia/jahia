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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToggleToolItem;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;

import java.util.Map;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 10:45:02
 */
public class RedirectWindowJahiaToolItemProvider extends AbstractJahiaToolItemProvider {
    public SelectionListener<ComponentEvent> getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem) {
        // add listener
        SelectionListener<ComponentEvent> listener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent be) {
                Map preferences = gwtToolbarItem.getProperties();
                final GWTJahiaProperty windowUrl = (GWTJahiaProperty) preferences.get(ToolbarConstants.URL);
                if (windowUrl != null && windowUrl.getValue() != null) {
                    Window.Location.replace(windowUrl.getValue());
                } else {
                    Window.Location.replace("http://www.google.com");
                }
            }
        };
        return listener;
    }

    public ToolItem createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        return new ToggleToolItem();
    }
}
