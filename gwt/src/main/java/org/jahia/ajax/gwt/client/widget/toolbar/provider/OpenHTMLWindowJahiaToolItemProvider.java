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
