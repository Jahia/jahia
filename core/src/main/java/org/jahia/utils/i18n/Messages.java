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

    /**
     * Looks up the resource bundle key considering locale and current module.
     * 
     * @param pkg
     *            the current module
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @return the label for the requested key
     */
    public static String get(JahiaTemplatesPackage pkg, String key, Locale locale) {
        return ResourceBundles.get(pkg, locale).getString(key);
    }

    /**
     * Looks up the resource bundle key considering locale and current module. If not found the specified default value is returned.
     * 
     * @param pkg
     *            the current module
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @param defaultValue
     *            the default value to return if the lookup has not found anything
     * @return the label for the requested key; if not found the provided default value it returned
     */
    public static String get(JahiaTemplatesPackage pkg, String key, Locale locale, String defaultValue) {
        String message;
        try {
            message = ResourceBundles.get(pkg, locale).getString(key);
        } catch (MissingResourceException e) {
            message = defaultValue;
        }
        return message;
    }

    /**
     * Looks up the resource bundle key considering locale and specified {@link ResourceBundle} instance. If not found the specified default
     * value is returned.
     * 
     * @param bundle
     *            the resource bundle key to lookup the key
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @param defaultValue
     *            the default value to return if the lookup has not found anything
     * @return the label for the requested key; if not found the provided default value it returned
     */
    public static String get(ResourceBundle bundle, String key, String defaultValue) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    /**
     * Looks up the resource bundle key considering locale, specified bundle name and current module.
     * 
     * @param primaryBundleName
     *            the resource bundle name to perform lookup for the first turn
     * @param pkg
     *            the current module
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @param defaultValue
     *            the default value to return if the lookup has not found anything
     * @return the label for the requested key
     */
    public static String get(String primaryBundleName, JahiaTemplatesPackage pkg, String key, Locale locale) {
        return ResourceBundles.get(primaryBundleName, pkg, locale).getString(key);
    }

    /**
     * Looks up the resource bundle key considering locale, specified bundle name and current module. If not found the specified default
     * value is returned.
     * 
     * @param primaryBundleName
     *            the resource bundle name to perform lookup for the first turn
     * @param pkg
     *            the current module
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @param defaultValue
     *            the default value to return if the lookup has not found anything
     * @return the label for the requested key; if not found the provided default value it returned
     */
    public static String get(String primaryBundleName, JahiaTemplatesPackage pkg, String key, Locale locale,
            String defaultValue) {
        try {
            return get(primaryBundleName, pkg, key, locale);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    /**
     * Looks up the resource bundle key considering locale and specified bundle name.
     * 
     * @param bundle
     *            the bundle name to perform the lookup
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @return the label for the requested key
     * @throws MissingResourceException
     *             in case the key is not found in the specified bundle
     */
    public static String get(String bundle, String key, Locale locale) throws MissingResourceException {
        return ResourceBundles.get(bundle, locale).getString(key);
    }

    /**
     * Looks up the resource bundle key considering locale and specified bundle name. If not found the specified default value is returned.
     * 
     * @param bundle
     *            the bundle name to perform the lookup
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @param defaultValue
     *            the default value to return if the lookup has not found anything
     * @return the label for the requested key; if not found the provided default value it returned
     */
    public static String get(String bundle, String key, Locale locale, String defaultValue) {
        try {
            return ResourceBundles.get(bundle, locale).getString(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    /**
     * Looks up the resource bundle key in the {@link ResourceBundles#JAHIA_INTERNAL_RESOURCES} bundle, considering locale. This method does
     * not throw {@link MissingResourceException} in case the key is not found, but rather returns the key itself.
     * 
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @return the label for the requested key
     * 
     * @see #getNonEmpty(String, String, Locale)
     */
    public static String getInternal(String key, Locale locale) {
        return getNonEmpty(ResourceBundles.JAHIA_INTERNAL_RESOURCES, key, locale);
    }

    /**
     * Looks up the resource bundle key in the {@link ResourceBundles#JAHIA_INTERNAL_RESOURCES} bundle, considering locale. If not found the
     * specified default value is returned.
     * 
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @param defaultValue
     *            the default value to return if the lookup has not found anything
     * @return the label for the requested key; if not found the provided default value it returned
     */
    public static String getInternal(String key, Locale locale, String defaultValue) {
        return get(ResourceBundles.JAHIA_INTERNAL_RESOURCES, key, locale, defaultValue);
    }

    /**
     * Looks up the resource bundle key in the {@link ResourceBundles#JAHIA_INTERNAL_RESOURCES} bundle, considering locale. Additionally
     * placeholders are replaced with the provided arguments.
     * 
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @param args
     *            the arguments to replace placeholders with
     * @return the label for the requested key
     */
    public static String getInternalWithArguments(String key, Locale locale, Object... args) {
        return format(get(ResourceBundles.JAHIA_INTERNAL_RESOURCES, key, locale), args);
    }

    /**
     * Looks up the resource bundle key in the specified bundle, considering locale. This method does not throw
     * {@link MissingResourceException} in case the key is not found, but rather returns the key itself.
     * 
     * @param bundle
     *            the bundle name to perform the lookup
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @return the label for the requested key
     */
    public static String getNonEmpty(String bundle, String key, Locale locale) {
        String value = get(bundle, key, locale, null);
        return value != null ? value : (MISSING_RESOURCE + key + MISSING_RESOURCE);
    }

    /**
     * Looks up the resource bundle key in the {@link ResourceBundles#JAHIA_TYPES_RESOURCES} bundle, considering locale. If not found the
     * specified default value is returned.
     * 
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @param defaultValue
     *            the default value to return if the lookup has not found anything
     * @return the label for the requested key; if not found the provided default value it returned
     */
    public static String getTypes(String key, Locale locale, String defaultValue) {
        return get(ResourceBundles.JAHIA_TYPES_RESOURCES, key, locale, defaultValue);
    }

    /**
     * Looks up the resource bundle key in the specified bundle, considering locale. Additionally placeholders are replaced with the
     * provided arguments.
     * 
     * @param bundle
     *            the bundle name to perform the lookup
     * @param key
     *            the key to perform lookup for
     * @param locale
     *            current locale
     * @param args
     *            the arguments to replace placeholders with
     * @return the label for the requested key
     */
    public static String getWithArgs(String bundle, String key, Locale locale, Object... arguments)
            throws MissingResourceException {
        return format(ResourceBundles.get(bundle, locale).getString(key), arguments);
    }

    /**
     * Performs the interpolation (evaluation) of the resource bundle macro in the provided input.
     * 
     * @param input
     *            the text to be interpolated
     * @param locale
     *            current local
     * @param module
     *            current module
     * @return the text after evaluation of the resource bundle macros
     */
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

    private Messages() {
        super();
    }
}
