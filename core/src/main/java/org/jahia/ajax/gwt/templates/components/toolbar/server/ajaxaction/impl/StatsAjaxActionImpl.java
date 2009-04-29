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
package org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl;

import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.data.JahiaData;

import java.util.Map;

/**
 * User: ktlili
 * Date: 7 nov. 2008
 * Time: 16:53:57
 */
public class StatsAjaxActionImpl extends AjaxAction {
    public static String PAGE = "page";
    public static String SITE = "site";

    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action, Map<String, GWTJahiaProperty> gwtPropertiesMap) {
        GWTJahiaAjaxActionResult gwtAjaxActionResult = new GWTJahiaAjaxActionResult();
        String value = "";
        if (action.equalsIgnoreCase(PAGE)) {
            value = getPageStatistics();
        } else if (action.equalsIgnoreCase(SITE)) {
            value = getSiteStatistics();
        }
        gwtAjaxActionResult.setValue(value);
        return gwtAjaxActionResult;
    }

    private String getPageStatistics() {
        return "<b>page statistics url</b>";
    }

    private String getSiteStatistics() {
        return "<b>Site statistics url</b>";
    }
}
