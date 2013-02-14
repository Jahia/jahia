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
package org.jahia.osgi;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.osgi.framework.Bundle;

/**
 * Convenient utilities for Jahia OSGi bundles.
 * 
 * @author Sergiy Shyrkov
 */
public final class BundleUtils {

    /**
     * Creates an instance of the {@link BundleDelegatingClassLoader} baked by the provided bundle and having Jahia root Spring context's
     * class loader as a parent.
     * 
     * @param bundle
     *            the bundle to create class loader for
     * @return an instance of the {@link BundleDelegatingClassLoader} baked by the provided bundle and having Jahia root Spring context's
     *         class loader as a parent
     */
    public static ClassLoader createBundleClassLoader(Bundle bundle) {
        return BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle, SpringContextSingleton.getInstance()
                .getContext().getClassLoader());
    }

    /**
     * Returns the template package that corresponds to the provided OSGi bundle or <code>null</code> if the package is not registered.
     * 
     * @param osgiBundle
     *            the corresponding OSGi bundle
     * @return the template package that corresponds to the provided OSGi bundle or <code>null</code> if the package is not registered
     */
    public static JahiaTemplatesPackage getModuleForBundle(Bundle bundle) {
        return getTemplateManagerService().lookupByBundle(bundle);
    }

    /**
     * Returns an instance of the {@link TemplatePackageRegistry} service.
     * 
     * @return an instance of the {@link TemplatePackageRegistry} service
     */
    private static TemplatePackageRegistry getTemplateManagerService() {
        return ((JahiaTemplateManagerService) SpringContextSingleton.getBean("JahiaTemplateManagerService"))
                .getTemplatePackageRegistry();
    }

    /**
     * Returns <code>true</code> if the provided bundle represents Jahia-related bundle (either a module or a service).
     * 
     * @param bundle
     *            the OSGi bundle to check
     * @return <code>true</code> if the provided bundle represents Jahia-related bundle (either a module or a service)
     */
    public static boolean isJahiaBundle(Bundle bundle) {
        return isJahiaModuleBundle(bundle)
                || StringUtils.defaultString((String) bundle.getHeaders().get("Bundle-Category")).toLowerCase()
                        .contains("jahia");
    }

    /**
     * Returns <code>true</code> if the provided bundle represents Jahia module.
     * 
     * @param bundle
     *            the OSGi bundle to check
     * @return <code>true</code> if the provided bundle represents Jahia module
     */
    public static boolean isJahiaModuleBundle(Bundle bundle) {
        return bundle.getHeaders().get("Jahia-Module-Type") != null;
    }

}