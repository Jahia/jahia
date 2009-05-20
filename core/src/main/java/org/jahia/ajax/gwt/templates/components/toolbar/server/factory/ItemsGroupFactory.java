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
package org.jahia.ajax.gwt.templates.components.toolbar.server.factory;

import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.data.JahiaData;

import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 5 ao�t 2008
 * Time: 09:17:02
 */
public interface ItemsGroupFactory {
    /**
     * Populate items list depending on the input and properties values.
     * @param gwtToolbarItemsList
     * @param jahiaData
     * @param input
     * @param properties
     * @return
     */
    public List<GWTJahiaToolbarItem> populateItemsList(List<GWTJahiaToolbarItem> gwtToolbarItemsList,JahiaData jahiaData, String input, Map<String, String> properties);
}
