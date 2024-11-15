/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils.i18n;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;

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
        } catch (NoSuchFieldException e) {
            logger.warn("Field cacheList not found on ResourceBundle object, this only works on Oracle JDK : " + e.getMessage());
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
        } else if (bundleName != null) {
            return ResourceBundle.getBundle(bundleName, locale, JahiaResourceBundleControl.getInstance());
        } else {
            return getInternal(locale);
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

    /**
     * Find resource bundle value regarding the key, locale and site for a given Jahia user.
     *
     * @param key to lookup
     * @param locale to use
     * @param site to look at
     * @param jahiaUser to use
     * @return resolved resource bundle value.
     */
    public static String getResources(String key, Locale locale, JCRSiteNode site, JahiaUser jahiaUser) {
        if (key == null || key.length() == 0) {
            return key;
        }
        logger.debug("Resources key: {}", key);
        String baseName = null;
        String value = null;
        if (key.contains("@")) {
            baseName = StringUtils.substringAfter(key, "@");
            key = StringUtils.substringBefore(key, "@");
        }

        value = Messages.get(baseName, site != null ? site.getTemplatePackage() : null, key, locale, null);
        if (value == null || value.length() == 0) {
            value = Messages.getInternal(key, locale);
        }
        logger.debug("Resources value: {}", value);
        if (value.contains("${")) {
            try {
                ScriptEngine scriptEngine = ScriptEngineUtils.getInstance().getEngineByName("velocity");
                ScriptContext scriptContext = new SimpleScriptContext();
                final Bindings bindings = new SimpleBindings();
                bindings.put("currentSite", site);
                bindings.put("currentUser", jahiaUser);
                bindings.put("currentLocale", locale);
                bindings.put("PrincipalViewHelper", PrincipalViewHelper.class);
                scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                scriptContext.setBindings(scriptEngine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
                scriptContext.setWriter(new StringWriter());
                scriptContext.setErrorWriter(new StringWriter());
                scriptEngine.eval(value, scriptContext);
                //String error = scriptContext.getErrorWriter().toString();
                return scriptContext.getWriter().toString().trim();
            } catch (ScriptException e) {
                logger.error("Error while executing script [" + value + "]", e);
            }
        }
        return value;
    }

    private ResourceBundles() {
        super();
    }
}
