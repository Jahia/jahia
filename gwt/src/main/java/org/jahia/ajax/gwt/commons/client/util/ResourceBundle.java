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

package org.jahia.ajax.gwt.commons.client.util;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.i18n.client.Dictionary;

/**
 * User: ktlili
 * Date: 1 oct. 2008
 * Time: 17:35:39
 */
public class ResourceBundle {

    /**
     * Global resource bundle name
     */
    public static final String APPLICATION_RESOURCE_ELEMENT_ID = "application_resource";

    /**
     * Default module type for Resource Bundle
     */
    public static final String RESOURCE_BUNDLE_MODULE_TYPE = "resource_module";

    /**
     *
     * @param jahiaModuleType
     * @param elementId
     * @param key
     * @return
     */
    public static String getResource(String jahiaModuleType, String elementId, String key) {
        try {
            //Log.debug("Dictonary name: " + jahiaModuleType + "_rb_" + elementId);
            Dictionary jahiaParamDictionary = Dictionary.getDictionary(jahiaModuleType.toLowerCase() + "_rb_" + elementId.toLowerCase());
            return jahiaParamDictionary.get(key);
        } catch (Exception e) {
            Log.error("Can't retrieve [" + key + "]", e);
            return key;
        }
    }

    /**
     * Retrieve the resource bundle using <code>RESOURCE_BUNDLE_MODULE_TYPE</code> as default module name
     *
     * @param key
     * @return
     */
    public static String getResource(String key) {
        try {
            //Log.debug("Dictonary name: " + jahiaModuleType + "_rb_" + elementId);
            Dictionary jahiaParamDictionary = Dictionary.getDictionary(RESOURCE_BUNDLE_MODULE_TYPE.toLowerCase()
                    + "_rb_" + APPLICATION_RESOURCE_ELEMENT_ID.toLowerCase());
            return jahiaParamDictionary.get(key);
        } catch (Exception e) {
            Log.error("Can't retrieve [" + key + "]", e);
            return key;
        }
    }

    /**
     * Retrieve the resource bundle using <code>RESOURCE_BUNDLE_MODULE_TYPE</code> as default module type
     * and <code>APPLICATION_RESOURCE_BUNDLE_ID</code> as default element id
     *
     * @param key
     * @param defaultValue if the value is null or empty, return the defaultValue
     * @return
     */
    public static String getNotEmptyResource(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Dictionary jahiaParamDictionary = Dictionary.getDictionary(RESOURCE_BUNDLE_MODULE_TYPE
                    + "_rb_" + APPLICATION_RESOURCE_ELEMENT_ID);
            value = jahiaParamDictionary.get(key);
            if (value == null || "".equals(value.trim())){
                value = defaultValue;
            }
        } catch (Exception e) {
            Log.error("Can't retrieve [" + key + "]", e);
        }
        return value;
    }

}
