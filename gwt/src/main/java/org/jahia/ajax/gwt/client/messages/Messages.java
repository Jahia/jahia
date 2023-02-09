/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.messages;

import java.util.MissingResourceException;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.i18n.client.Dictionary;

/**
 * Provider of the I18N text in the GWT components.
 *
 * @author Khaled Tlili
 * Date: 1 oct. 2008
 * Time: 17:35:39
 */
public class Messages {

    /**
     * Dictonary name
     */
    public static final String DICTIONARY_NAME = "jahia_gwt_messages";


    /**
     * Retrieves the value for the specified key from the resource bundle.
     *
     * @param key the key of the message to look up
     * @return the found localized message or a key if the message was not found
     */
    public static String get(String key) {
        return get(key, key);
    }

    /**
     * Retrieves the value for the specified key from the resource bundle.
     *
     * @param key the key of the message to look up
     * @param defaultValue if the value is null or empty, return the defaultValue
     * @return the found localized message or the provided default value if the message was not found
     */
    public static String get(String key, String defaultValue) {
        String value = defaultValue;
        if (key != null) {
            try {
                Dictionary dict = Dictionary.getDictionary(DICTIONARY_NAME);
                value = dict.get(normalizeKey(key));
            } catch (MissingResourceException e) {
                if (Log.isDebugEnabled()) {
                    Log.debug("Can't retrieve [" + key + "]. Using default value: " + defaultValue, e);
                }
            } catch (Exception e) {
                Log.error("Can't retrieve [" + key + "]. Using default value: " + defaultValue + ". Cause: " + e.getMessage(), e);
            }
        } else {
            if (Log.isDebugEnabled()) {
                Log.debug("Provided key is null. Using default value: " + defaultValue);
            }
        }
        return value;
    }

    private static String normalizeKey(String key) {
        String normalized = key.indexOf('.') != -1 ? key.replace('.', '_') : key;
        if (normalized.indexOf('-') != -1) {
            normalized = normalized.replace('-', '_');
        }
        return normalized;
    }

    /**
     * Retrieves the value for the specified key from the resource bundle replacing placeholders if available.
     *
     * @param key the key of the message to look up
     * @param defaultValue if the value is null or empty, return the defaultValue
     * @return the found localized message or the provided default value if the message was not found
     */
    public static String getWithArgs(String key, String defaultValue, Object[] args) {
        String msg = get(key, defaultValue);
        if (msg != null && msg.contains("{0}") && args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String placeholder = "{" + i + "}";
                if (msg.contains(placeholder)) {
                    msg = msg.replace(placeholder, String.valueOf(args[i]));
                }
            }
        }

       return msg;
    }

}
