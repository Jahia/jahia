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
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.Window;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;

import java.util.Map;

/**
 * User: ktlili
 * Date: 7 nov. 2008
 * Time: 13:38:20
 */
public class OpenHTMLWindowJahiaToolItemProvider extends AbstractJahiaToolItemProvider {
    /**
     * Get Selectect Listener
     *
     * @param gwtToolbarItem
     * @return
     */
    public SelectionListener<ComponentEvent> getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem) {
        // add listener
        SelectionListener<ComponentEvent> listener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                Map preferences = gwtToolbarItem.getProperties();
                final GWTJahiaProperty htmlProperty = (GWTJahiaProperty) preferences.get(ToolbarConstants.HTML);
                if (htmlProperty != null && htmlProperty.getValue() != null) {
                    Window window = new Window();
                    if (gwtToolbarItem.getTitle() != null) {
                        String title = gwtToolbarItem.getTitle().replaceAll(" ", "_");
                        window.setTitle(title);
                    }
                    window.addText(htmlProperty.getValue());
                    window.setModal(true);
                    window.setResizable(true);
                    window.setClosable(true);
                    window.show();
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
    public ToolItem createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        return new TextToolItem();
    }
}
