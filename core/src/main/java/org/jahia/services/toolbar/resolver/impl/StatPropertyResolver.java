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
package org.jahia.services.toolbar.resolver.impl;

import org.jahia.services.toolbar.resolver.PropertyResolver;
import org.jahia.data.JahiaData;

/**
 * User: ktlili
 * Date: 7 nov. 2008
 * Time: 12:40:16
 */
public class StatPropertyResolver implements PropertyResolver {
    public static String PAGE = "page";
    public static String SITE = "site";

    public String getValue(JahiaData jData, String input) {
        String value = "";
        if (input.equalsIgnoreCase(PAGE)) {
            value = getPageStatistics();
        } else if (input.equalsIgnoreCase(SITE)) {
            value = getSiteStatistics();
        }
        return value;
    }

    private String getPageStatistics() {
        return "<b>page statistics url</b>";
    }

    private String getSiteStatistics() {
        return "<b>Site statistics url</b>";
    }
}
