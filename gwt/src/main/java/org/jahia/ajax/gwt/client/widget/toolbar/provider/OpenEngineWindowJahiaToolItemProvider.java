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

import java.util.Map;

import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.util.EngineOpener;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;

/**
 * Toolbar item provider for opening engine popup windows.
 * 
 * @author Sergiy Shyrkov
 */
public class OpenEngineWindowJahiaToolItemProvider extends
        OpenWindowJahiaToolItemProvider {

    @Override
    public SelectionListener<ComponentEvent> getSelectListener(
            final GWTJahiaToolbarItem gwtToolbarItem) {
        return new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                Map<String, GWTJahiaProperty> preferences = gwtToolbarItem
                        .getProperties();

                EngineOpener.openEngine(preferences.get(ToolbarConstants.URL).getValue(),
                        preferences.get(ToolbarConstants.WINDOW_NAME).getValue(),
                        preferences.get(ToolbarConstants.PARAMETERS).getValue());
            }
        };

    }

}
