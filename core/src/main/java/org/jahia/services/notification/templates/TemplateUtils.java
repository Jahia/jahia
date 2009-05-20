/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
