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
package org.jahia.utils.i18n;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 26 févr. 2009
 * Time: 17:45:02
 * To change this template use File | Settings | File Templates.
 */
public class JahiaResourceBundle extends ResourceBundle {
    private transient static Logger logger = Logger.getLogger(JahiaResourceBundle.class);
    private final String basename;
    private final Locale locale;
    private final JahiaTemplatesPackage templatesPackage;
    public static final String JAHIA_INTERNAL_RESOURCES = "JahiaInternalResources";
    private static final String MISSING_RESOURCE = "???";
    public static final String JAHIA_MESSAGE_RESOURCES = "JahiaMessageResources";

    public JahiaResourceBundle(String basename, Locale locale,
                               ClassLoader classLoader, JahiaTemplatesPackage templatesPackage) {
        this.basename = basename;
        this.locale = locale;
        this.templatesPackage = templatesPackage;
        ResourceBundle.getBundle(basename, locale, classLoader);
    }

    public JahiaResourceBundle(String basename, Locale locale) {
        this.basename = basename;
        this.locale = locale;
        this.templatesPackage = null;
        ResourceBundle.getBundle(basename, locale);
    }


    @Override
    public Object handleGetObject(String s) {
        final JahiaTemplatesRBLoader templatesRBLoader = getClassLoader();
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(basename, locale, templatesRBLoader);
        Object o;
        try {
            o = resourceBundle.getString(s);
        } catch (MissingResourceException e) {
            if (templatesPackage != null) {
                final List<String> stringList = templatesPackage.getResourceBundleHierarchy();
                for (String s1 : stringList) {
                    ResourceBundle parentResourceBundle = ResourceBundle.getBundle(s1, locale, templatesRBLoader);
                    try {
                        o = parentResourceBundle.getString(s);
                        if (o != null) {
                            return o;
                        }
                    } catch (MissingResourceException e1) {
                        logger.debug("Try to find " + s + " in resource bundle hierarchy");
                    }
                }
            }
            throw new MissingResourceException("Cannot find resource "+s, basename, s);
        }
        return o;
    }

    /**
     * Returns an enumeration of the keys.
     *
     * @return an <code>Enumeration</code> of the keys contained in
     *         this <code>ResourceBundle</code> and its parent bundles.
     */
    public Enumeration<String> getKeys() {
        final JahiaTemplatesRBLoader templatesRBLoader = getClassLoader();
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(basename, locale, templatesRBLoader);
        return resourceBundle.getKeys();
    }

    private JahiaTemplatesRBLoader getClassLoader() {
        final JahiaTemplatesRBLoader templatesRBLoader;
        if (templatesPackage != null) {
            templatesRBLoader = new JahiaTemplatesRBLoader(Thread.currentThread().getContextClassLoader(), templatesPackage.getName());
        } else {
            templatesRBLoader = new JahiaTemplatesRBLoader(Thread.currentThread().getContextClassLoader(), Jahia.getThreadParamBean().getSiteID());
        }
        return templatesRBLoader;
    }

    /**
     * Shortcut methods to call a resource key from the engine resource bundle
     * @param key, the key to search inside the JahiaInternalResources bundle
     * @param locale, the locale in which we want to find the key
     * @param defaultValue, the defaultValue (surrounded by ???) if not found
     * @return the resource in locale langauge or defaultValue surrounded by (???)
     */
    public static String getJahiaInternalResource(String key, Locale locale, String defaultValue) {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(JAHIA_INTERNAL_RESOURCES, locale);
        try{
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return MISSING_RESOURCE +defaultValue!=null?defaultValue:key+ MISSING_RESOURCE;
        }
    }

    public static String getJahiaInternalResource(String key, Locale locale) {
        return getJahiaInternalResource(key, locale,null);
    }

    public static String getMessageResource(String key, Locale locale,String defaultValue) {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(JAHIA_MESSAGE_RESOURCES, locale);
        try{
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return MISSING_RESOURCE +defaultValue!=null?defaultValue:key+ MISSING_RESOURCE;
        }
    }

    public static String getMessageResource(String key, Locale locale) {
        return getMessageResource(key, locale,null);
    }

    public static String getString(String bundle, String key, Locale locale, int siteID) {
        final JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(siteID);
        final JahiaTemplatesRBLoader templatesRBLoader = new JahiaTemplatesRBLoader(Thread.currentThread().getContextClassLoader(), siteID);
        JahiaResourceBundle resourceBundle = new JahiaResourceBundle(bundle,locale, templatesRBLoader, aPackage);
        return resourceBundle.getString(key);
    }
}
