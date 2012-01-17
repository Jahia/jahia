/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
                value = dict.get(key.contains(".") ? key.replace('.', '_') : key);
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
