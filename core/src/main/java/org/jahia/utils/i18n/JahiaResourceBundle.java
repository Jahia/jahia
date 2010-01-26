/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils.i18n;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.settings.SettingsBean;

import java.text.MessageFormat;
import java.util.*;

/**
 * Jahia implementation of the resource bundle, which considers template set inheritance.
 * User: rincevent
 * Date: 26 févr. 2009
 * Time: 17:45:02
 */
public class JahiaResourceBundle extends ResourceBundle {
    private transient static Logger logger = Logger.getLogger(JahiaResourceBundle.class);
    private static final Locale EMPTY_LOCALE = new Locale("", "");
    private static Map<ClassLoader, Map<String, Map<Locale, ResourceBundle>>> map = new HashMap<ClassLoader, Map<String, Map<Locale, ResourceBundle>>>();

    private final String basename;
    private final Locale locale;
    private final JahiaTemplatesPackage templatesPackage;
    public static final String JAHIA_INTERNAL_RESOURCES = "JahiaInternalResources";
    private static final String MISSING_RESOURCE = "???";

    private static Map<String, String> resourceBundleMap = new HashMap<String, String>();


    public JahiaResourceBundle(Locale locale, String templatesPackageName) {
        this(null, locale, templatesPackageName, null);
    }

    public JahiaResourceBundle(String basename, Locale locale) {
        this(basename, locale, null, null);
    }

    public JahiaResourceBundle(Locale locale, String templatesPackageName,
                               ClassLoader classLoader) {
        this(null, locale, templatesPackageName, classLoader);
    }

    public JahiaResourceBundle(String basename, Locale locale, String templatesPackageName) {
        this(basename, locale, templatesPackageName, null);
    }

    public JahiaResourceBundle(String basename, Locale locale, String templatesPackageName,
                               ClassLoader classLoader) {
        this.basename = basename;
        this.locale = locale;
        this.templatesPackage = templatesPackageName != null ? ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(templatesPackageName) : null;
    }

    @Override
    public Object handleGetObject(String s) {
        final JahiaTemplatesRBLoader templatesRBLoader = getClassLoader();
        Object o = null;
        if (basename != null) {
            try {
                o = lookupBundle(basename, locale, templatesRBLoader).getString(s);
            } catch (MissingResourceException e) {
                /*logger.debug("Not found '" + s
                        + "' in the base resource bundle '" + basename + "'");*/
            }
        }
        if (o == null && templatesPackage != null) {
            final List<String> stringList = templatesPackage.getResourceBundleHierarchy();
            for (String bundleToLookup : stringList) {
                if (basename != null && basename.equals(bundleToLookup)) {
                    // we did the lookup in this bundle already
                    continue;
                }
                try {
                    o = lookupBundle(bundleToLookup, locale, templatesRBLoader).getString(s);
                    if (o != null) {
                        break;
                    }
                } catch (MissingResourceException e1) {
                    /* logger.debug("Try to find '" + s
           + "' in resource bundle '" + bundleToLookup + "'");*/
                }
            }
        }
        if (o == null) {
            throw new MissingResourceException("Cannot find resource " + s, basename, s);
        }

        if (!resourceBundleMap.containsKey(s)) {
            logger.debug(s + " = " + o);
        }

        resourceBundleMap.put(s, o.toString());
        System.err.println(s + "," + o);

        return o;
    }

    /**
     * Returns an enumeration of the keys.
     *
     * @return an <code>Enumeration</code> of the keys contained in
     *         this <code>ResourceBundle</code> and its parent bundles.
     */
    public Enumeration<String> getKeys() {
        return lookupBundle(basename, locale, getClassLoader()).getKeys();
    }

    /**
     * @return
     */
    private JahiaTemplatesRBLoader getClassLoader() {
        String templatesPackageName = templatesPackage != null ? templatesPackage
                .getName()
                : (Jahia.getThreadParamBean() != null
                && Jahia.getThreadParamBean().getSite() != null ? Jahia
                .getThreadParamBean().getSite()
                .getTemplatePackageName() : null);
        return templatesPackageName != null ? JahiaTemplatesRBLoader
                .getInstance(Thread.currentThread().getContextClassLoader(),
                        templatesPackageName) : null;
    }

    /**
     * Shortcut methods to call a resource key from the engine resource bundle
     *
     * @param key,          the key to search inside the JahiaInternalResources bundle
     * @param locale,       the locale in which we want to find the key
     * @param defaultValue, the defaultValue (surrounded by ???) if not found
     * @return the resource in locale language or defaultValue surrounded by (???)
     */
    public static String getJahiaInternalResource(String key, Locale locale, String defaultValue) {
        final ResourceBundle resourceBundle = lookupBundle(JAHIA_INTERNAL_RESOURCES, locale);
        try {
            String value = resourceBundle.getString(key);
            if (!resourceBundleMap.containsKey(key)) {
               // logger.debug(key + " = " + value);
            }

            resourceBundleMap.put(key, value);
           // System.err.println(key + "," + value);
            return value;
        } catch (MissingResourceException e) {
            return defaultValue != null ? defaultValue : (MISSING_RESOURCE + key + MISSING_RESOURCE);
        }
    }

    /**
     * Get message by key and local
     *
     * @param key
     * @param locale
     * @return
     */
    public static String getJahiaInternalResource(String key, Locale locale) {
        return getJahiaInternalResource(key, locale, null);
    }

    /**
     * Get message depending on the key
     *
     * @param bundle
     * @param key
     * @param locale
     * @param templatePackageName
     * @return
     */
    public static String getString(String bundle, String key, Locale locale, String templatePackageName) {
        return new JahiaResourceBundle(bundle, locale, templatePackageName, Thread.currentThread().getContextClassLoader()).getString(key);
    }

    /**
     * Get message depending on the key
     *
     * @param bundle
     * @param key
     * @param locale
     * @param templatePackageName
     * @param loader
     * @return
     */
    public static String getString(String bundle, String key, Locale locale, String templatePackageName, ClassLoader loader) {
        return new JahiaResourceBundle(bundle, locale, templatePackageName, loader).getString(key);
    }

    /**
     * find  ResourceBundle dependinng on a baseName
     *
     * @param baseName
     * @param locale
     * @return
     */
    private static ResourceBundle lookupBundle(String baseName, Locale locale) {
        return lookupBundle(baseName, locale, null);
    }

    /**
     * find  ResourceBundle dependinng on a baseName
     *
     * @param baseName
     * @param preferredLocale
     * @param loader
     * @return
     */
    private static ResourceBundle lookupBundle(String baseName,
                                               Locale preferredLocale, ClassLoader loader) {

        if (map.get(loader) == null) {
            map.put(loader, new HashMap<String, Map<Locale, ResourceBundle>>());
        }
        if (map.get(loader).get(baseName) == null) {
            map.get(loader).put(baseName, new HashMap<Locale, ResourceBundle>());
        }

        ResourceBundle bundle;
        if (map.get(loader).get(baseName).containsKey(preferredLocale)) {
            bundle = map.get(loader).get(baseName).get(preferredLocale);
        } else {
            bundle = loader != null ? ResourceBundle.getBundle(
                    baseName, preferredLocale, loader) : ResourceBundle.getBundle(
                    baseName, preferredLocale);
            map.get(loader).get(baseName).put(preferredLocale, bundle);
        }

        ResourceBundle match = null;

        if (!SettingsBean.getInstance().isConsiderDefaultJVMLocale()) {
            Locale availableLocale = bundle.getLocale();
            if (availableLocale.equals(preferredLocale)) {
                match = bundle;
            } else if (preferredLocale.getLanguage().equals(
                    availableLocale.getLanguage())
                    && (availableLocale.getCountry().length() == 0 || preferredLocale
                    .getCountry().equals(availableLocale.getCountry()))) {
                match = bundle;
            }
            if (match == null && !EMPTY_LOCALE.equals(preferredLocale)) {
                match = lookupBundle(baseName, EMPTY_LOCALE, loader);
            }
        } else {
            match = bundle;
        }

        return match;
    }

    /**
     * Get message depending on a key. If not found, return the default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String get(String key, String defaultValue) {
        return getString(key, defaultValue);
    }

    /**
     * Get formateed message
     *
     * @param key
     * @param defaultValue
     * @param arguments
     * @return
     */
    public String getFormatted(String key, String defaultValue, Object... arguments) {
        String value = MessageFormat.format(get(key, defaultValue), arguments);
        if (!resourceBundleMap.containsKey(key)) {
            logger.debug(key + " = " + value);
        }
        System.err.println(key + "," + value);

        return value;
    }

    /**
     * Get message depending on a key. If not found, return the default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue) {
        String message;
        try {
            message = getString(key);
            if (!resourceBundleMap.containsKey(key)) {
                logger.debug(key + " = " + message);
            }
            System.err.println(key + "," + message);

        } catch (MissingResourceException e) {
            message = defaultValue;
        }
        return message;
    }

    /**
     * Returns names of bundles where the key is looked up.
     *
     * @return list of bundles where the key is looked up
     */
    public List<String> getLookupBundles() {
        List<String> bundles = new LinkedList<String>();
        if (basename != null) {
            bundles.add(basename);
        }
        if (templatesPackage != null) {
            List<String> bundleHierarchy = templatesPackage.getResourceBundleHierarchy();
            for (String name : bundleHierarchy) {
                if (basename == null || !basename.equals(name)) {
                    bundles.add(name);
                }
            }
        }
        return bundles;
    }


}