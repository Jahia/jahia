/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jahia resource bundle utility class.
 * 
 * @author Sergiy Shyrkov
 */
public final class ResourceBundles {

    private static final Logger logger = LoggerFactory.getLogger(ResourceBundles.class);

    public static final String JAHIA_INTERNAL_RESOURCES = "JahiaInternalResources";

    public static final String JAHIA_TYPES_RESOURCES = "JahiaTypesResources";

    /**
     * Triggers the clean up of the {@link ResourceBundle} caches.
     */
    public static void flushCache() {
        try {
            Field cacheList = ResourceBundle.class.getDeclaredField("cacheList");
            cacheList.setAccessible(true);
            ((Map<?, ?>) cacheList.get(ResourceBundle.class)).clear();
        } catch (Exception e) {
            logger.warn("Unable to flush resource bundle cache", e);
        }

        JahiaTemplatesRBLoader.clearCache();
    }

    /**
     * Use the resource bundle lookup hierarchy of the provided template package.
     * 
     * @param pkg
     *            the template package to use resources bundle lookup for
     * @param locale
     *            the target locale
     * @return a resource bundle that is baked by the resource bundle lookup chain of a provided template package
     */
    public static ResourceBundle get(JahiaTemplatesPackage pkg, Locale locale) {
        return get(pkg.getResourceBundleHierarchy(), locale);
    }

    protected static ResourceBundle get(List<String> bundleLookupChain, Locale locale) {
        if (bundleLookupChain == null || bundleLookupChain.isEmpty()) {
            throw new IllegalArgumentException("ResourceBundle lookup chain is empty");
        }

        return bundleLookupChain.size() > 1 ? new HierarchicalResourceBundle(bundleLookupChain, locale)
                : get(bundleLookupChain.get(0), locale);
    }

    /**
     * Use the resource bundle lookup hierarchy of the provided template package, but first check for the specified bundle name.
     * 
     * @param primaryBundleName
     *            the bundle name to peform lookup for in first turn
     * @param pkg
     *            the template package to use resources bundle lookup for
     * @param locale
     *            the target locale
     * @return a resource bundle that is baked by the resource bundle lookup chain of a provided template package, considering specified
     *         primary bundle name
     */
    public static ResourceBundle get(String primaryBundleName, JahiaTemplatesPackage pkg, Locale locale) {
        if (pkg == null) {
            return get(primaryBundleName, locale);
        }
        if (primaryBundleName == null
                || (!pkg.getResourceBundleHierarchy().isEmpty() && pkg.getResourceBundleHierarchy().get(0)
                        .equals(primaryBundleName))) {
            return get(pkg, locale);
        }

        List<String> lookup = new LinkedList<String>();
        lookup.add(primaryBundleName);
        lookup.addAll(pkg.getResourceBundleHierarchy());

        return get(lookup, locale);
    }

    /**
     * Looks up a resource bundle for the specified locale
     * 
     * @param bundleName
     *            the name of the bundle to look up
     * @param locale
     *            the locale to look the bundle up
     * @return a resource bundle for the specified locale
     */
    public static ResourceBundle get(String bundleName, Locale locale) {
        JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().getPackageForResourceBundle(bundleName);
        if (aPackage != null && aPackage.getClassLoader() != null) {
            return ResourceBundle.getBundle(bundleName, locale, aPackage.getClassLoader(), JahiaResourceBundleControl.getInstance());
        } else {
            return ResourceBundle.getBundle(bundleName, locale, JahiaResourceBundleControl.getInstance());
        }
    }

    /**
     * Looks up the JahiaInternalResources bundle for the specified locale
     * 
     * @param locale
     *            the locale to look the bundle up
     * @return the JahiaInternalResources bundle for the specified locale
     */
    public static ResourceBundle getInternal(Locale locale) {
        return get(JAHIA_INTERNAL_RESOURCES, locale);
    }
    
    private ResourceBundles() {
        super();
    }
}
