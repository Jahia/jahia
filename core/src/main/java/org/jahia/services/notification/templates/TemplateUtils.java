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

package org.jahia.services.notification.templates;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.notification.SubscriptionUser;
import org.jahia.services.notification.Subscription;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;

/**
 * Utility class that is used in Groovy-based e-mail templates.
 * 
 * @author Sergiy Shyrkov
 */
public final class TemplateUtils {

    private static Logger logger = Logger.getLogger(TemplateUtils.class);

    public static JahiaSite getSite(int siteId) {
        JahiaSite site = null;
        if (siteId > 0) {
            try {
                site = ServicesRegistry.getInstance().getJahiaSitesService()
                        .getSite(siteId);
            } catch (JahiaException e) {
                logger.warn("Unable to retrieve the site for key '" + siteId
                        + "'", e);
            }
        }

        return site;
    }

    public static JahiaUser getSubscriber(Subscription subscription) {
        return subscription.isUserRegistered() ? ServicesRegistry.getInstance()
                .getJahiaUserManagerService().lookupUser(
                        subscription.getUsername()) : new SubscriptionUser(
                subscription.getUsername(), subscription.getProperties());
    }

    public static String getTemplatesPath() {
        return SettingsBean.getInstance().getTemplatesContext()
                + (SettingsBean.getInstance().getTemplatesContext().endsWith(
                        "/") ? "" : "/");
    }

    public static boolean isResourceAvailable(String resourcePath) {
        boolean available = false;
        try {
            available = Jahia.getStaticServletConfig().getServletContext()
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
     * template set) into the actual path, considering template set inheritance.
     * 
     * @param path
     *            the resource path to resolve
     * @return the resolved path (context related) to the requested resource or
     *         <code>null</code>, if it is not found
     */
    public static String resolvePath(String path, String templatePackageName) {
        String resolvedPath = null;
        String templatesPath = getTemplatesPath();
        if (templatePackageName != null) {
            resolvedPath = ServicesRegistry.getInstance()
                    .getJahiaTemplateManagerService().resolveResourcePath(path,
                            templatePackageName);
        } else {
            path = templatesPath + "default/" + path;
            resolvedPath = isResourceAvailable(path) ? path : null;
        }
        return resolvedPath;
    }

    /**
     * Initializes an instance of this class.
     */
    private TemplateUtils() {
        super();
    }

}
