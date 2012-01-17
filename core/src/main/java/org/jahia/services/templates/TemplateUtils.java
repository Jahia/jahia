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

package org.jahia.services.templates;

import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.settings.SettingsBean;

/**
 * Utility class that is used in template related operations.
 * 
 * @author Sergiy Shyrkov
 */
public class TemplateUtils {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TemplateUtils.class);

    public static String getTemplatesPath() {
        return SettingsBean.getInstance().getTemplatesContext()
                + (SettingsBean.getInstance().getTemplatesContext().endsWith(
                        "/") ? "" : "/");
    }

    public static boolean isResourceAvailable(String resourcePath) {
        boolean available = false;
        try {
            available = JahiaContextLoaderListener.getServletContext()
                    .getResource(resourcePath) != null;
        } catch (MalformedURLException e) {
            logger.debug(e.getMessage(), e);
        }
        return available;
    }

    public static String lookupTemplate(String templatePackageName,
            String... filePathToTry) {
        String templatePath = null;
        for (String path : filePathToTry) {
            if (path == null) {
                continue;
            }
            templatePath = resolvePath(path, templatePackageName);
            if (templatePath != null) {
                break;
            }
        }

        return templatePath;
    }

    /**
     * Resolves the specified path (which is related to the root folder of the
     * template set) into the actual path.
     * 
     * @param path
     *            the resource path to resolve
     * @param templatePackageName the template package, where the template belongs to
     * @return the resolved path (context related) to the requested resource or
     *         <code>null</code>, if it is not found
     */
    public static String resolvePath(String path, String templatePackageName) {
        return resolvePath(path, templatePackageName != null ? ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService().getTemplatePackage(templatePackageName) : null);
    }

    /**
     * Resolves the specified path (which is related to the root folder of the
     * template set) into the actual path.
     * 
     * @param path
     *            the resource path to resolve
     * @param templatePackage the template package, where the template belongs to
     * @return the resolved path (context related) to the requested resource or
     *         <code>null</code>, if it is not found
     */
    public static String resolvePath(String path, JahiaTemplatesPackage templatePackage) {
        String resolvedPath = null;
        String templatesPath = getTemplatesPath();
        if (templatePackage != null) {
            resolvedPath = templatePackage.getRootFolderPath() + path;
            resolvedPath = isResourceAvailable(resolvedPath) ? resolvedPath : null;
        }
        if (path == null) {
            path = templatesPath + "default/" + path;
            resolvedPath = isResourceAvailable(path) ? path : null;
        }
        return resolvedPath;
    }

    /**
     * Initializes an instance of this class.
     */
    protected TemplateUtils() {
        super();
    }

}
