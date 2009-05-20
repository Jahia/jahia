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
package org.jahia.ajax.gwt.client.messages;

import java.util.MissingResourceException;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.i18n.client.Dictionary;

/**
 * User: ktlili
 * Date: 1 oct. 2008
 * Time: 17:35:39
 */
public class Messages {

    /**
     * Dictonary name
     */
    public static final String DICTIONARY_NAME = "jahia_gwt_messages";


    /**
     * Retrieve the resource bundle using <code>RESOURCE_BUNDLE_MODULE_TYPE</code> as default module name
     *
     * @param key
     * @return
     */
    public static String getResource(String key) {
        try {
            //Log.debug("Dictionary name: " + jahiaModuleType + "_rb_" + elementId);
            Dictionary jahiaParamDictionary = Dictionary.getDictionary(DICTIONARY_NAME);
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
            Dictionary jahiaParamDictionary = Dictionary.getDictionary(DICTIONARY_NAME);
            value = jahiaParamDictionary.get(key);
            if (value == null || "".equals(value.trim())){
                value = defaultValue;
            }
        } catch (MissingResourceException e) {
            Log.debug("Can't retrieve [" + key + "]", e);
        } catch (Exception e) {
            Log.error("Can't retrieve [" + key + "]", e);
        }
        return value;
    }

}
