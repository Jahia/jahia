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
package org.jahia.utils.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.utils.Patterns;

/**
 * Jahia i18n message utilities.
 * 
 * @author Sergiy Shyrkov
 */
public final class Messages {

    private static final String MISSING_RESOURCE = "???";

    private static final Pattern RB_MACRO = Pattern.compile("##resourceBundle\\((.*)\\)##");
    
    /**
     * Returns the formatted messages with placeholders substituted by argument values.
     * 
     * @param text
     *            the message text with placeholders
     * @param arguments
     *            an array of arguments to be used for substitution
     * @return the formatted messages with placeholders substituted by argument values
     */
    public static String format(String text, Object... arguments) {
        if (text == null || arguments.length == 0) {
            return text;
        }
        return MessageFormat.format(StringUtils.replace(text, "'", "''"), arguments);
    }

    public static String get(JahiaTemplatesPackage pkg, String key, Locale locale) {
        return ResourceBundles.get(pkg, locale).getString(key);
    }

    public static String get(JahiaTemplatesPackage pkg, String key, Locale locale, String defaultValue) {
        String message;
        try {
            message = ResourceBundles.get(pkg, locale).getString(key);
        } catch (MissingResourceException e) {
            message = defaultValue;
        }
        return message;
    }

    public static String get(ResourceBundle bundle, String key, String defaultValue) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    public static String get(String primaryBundleName, JahiaTemplatesPackage pkg, String key, Locale locale) {
        return ResourceBundles.get(primaryBundleName, pkg, locale).getString(key);
    }

    public static String get(String primaryBundleName, JahiaTemplatesPackage pkg, String key, Locale locale,
            String defaultValue) {
        try {
            return get(primaryBundleName, pkg, key, locale);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    public static String get(String bundle, String key, Locale locale) throws MissingResourceException {
        return ResourceBundles.get(bundle, locale).getString(key);
    }

    public static String get(String bundle, String key, Locale locale, String defaultValue) {
        try {
            return ResourceBundles.get(bundle, locale).getString(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    public static String getInternal(String key, Locale locale) {
        return getNonEmpty(ResourceBundles.JAHIA_INTERNAL_RESOURCES, key, locale);
    }

    public static String getInternal(String key, Locale locale, String defaultValue) {
        return get(ResourceBundles.JAHIA_INTERNAL_RESOURCES, key, locale, defaultValue);
    }

    public static String getInternalWithArguments(String key, Locale locale, Object... args) {
        return format(get(ResourceBundles.JAHIA_INTERNAL_RESOURCES, key, locale), args);
    }

    public static String getNonEmpty(String bundle, String key, Locale locale) {
        String value = get(bundle, key, locale, null);
        return value != null ? value : (MISSING_RESOURCE + key + MISSING_RESOURCE);
    }

    public static String getTypes(String key, Locale locale, String defaultValue) {
        return get(ResourceBundles.JAHIA_TYPES_RESOURCES, key, locale, defaultValue);
    }

    public static String interpolateResourceBundleMacro(String input, Locale locale, JahiaTemplatesPackage module) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }

        String result = input;
        Matcher m = RB_MACRO.matcher(input);
        if (m.matches()) {
            String params = m.group(1);
            if (StringUtils.isNotEmpty(params)) {
                String bundle = null;
                String key = null;
                if (params.indexOf('"') != -1) {
                    params = Patterns.DOUBLE_QUOTE.matcher(params).replaceAll(StringUtils.EMPTY);
                }
                if (params.indexOf('\'') != -1) {
                    params = Patterns.SINGLE_QUOTE.matcher(params).replaceAll(StringUtils.EMPTY);
                }
                if (params.indexOf(',') != -1) {
                    String[] paramArray = Patterns.COMMA.split(params);
                    if (paramArray.length > 0) {
                        key = paramArray[0];
                        bundle = paramArray.length > 1 ? paramArray[1] : null;
                    }
                } else {
                    key = params;
                }
                if (StringUtils.isNotEmpty(key)) {
                    String replacement = get(bundle, module, key, locale, key);
                    result = StringUtils.replace(input, m.group(), replacement);
                }
            }
        }

        return result;
    }
}
