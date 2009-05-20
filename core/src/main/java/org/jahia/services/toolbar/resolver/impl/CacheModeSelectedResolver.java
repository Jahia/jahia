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

import org.jahia.data.JahiaData;
import org.jahia.services.toolbar.resolver.SelectedResolver;

/**
 * User: jahia
 * Date: 4 juil. 2008
 * Time: 15:31:36
 */
public class CacheModeSelectedResolver implements SelectedResolver {
    // cache
    public static String CACHE_DEBUG = "cache_debug";
    public static String CACHE_ON = "cache_on";

    public boolean isSelected(JahiaData jData,String type) {
        if (type.equalsIgnoreCase(CACHE_ON)) {
            return true;
        } else if (type.equalsIgnoreCase(CACHE_DEBUG)) {
            return true;
        } else {
            return false;
        }
    }

}
