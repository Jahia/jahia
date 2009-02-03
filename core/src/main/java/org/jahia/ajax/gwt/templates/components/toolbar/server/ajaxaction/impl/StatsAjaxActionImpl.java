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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl;

import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.ajax.gwt.commons.client.beans.GWTProperty;
import org.jahia.ajax.gwt.commons.client.beans.GWTAjaxActionResult;
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

    public GWTAjaxActionResult execute(JahiaData jahiaData, String action, Map<String, GWTProperty> gwtPropertiesMap) {
        GWTAjaxActionResult gwtAjaxActionResult = new GWTAjaxActionResult();
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
