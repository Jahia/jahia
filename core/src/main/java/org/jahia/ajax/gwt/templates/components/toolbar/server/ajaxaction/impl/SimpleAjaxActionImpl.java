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
package org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl;

import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.data.JahiaData;
import org.jahia.engines.EngineMessage;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.*;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

/**
 * User: jahia
 * Date: 4 juil. 2008
 * Time: 10:19:35
 */
public class SimpleAjaxActionImpl extends AjaxAction {
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(SimpleAjaxActionImpl.class);

    private final static String USER_INFO = "userinfo";
    private final static String LICENSE_INFO = "licenseinfo";
    private final static String SEND_EMAIL_INFO = "sendEmail";

    /**
     * Execute the method that corresponds to the action
     *
     * @param jahiaData
     * @param action
     * @param gwtPropertiesMap
     * @return
     */
    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action, Map gwtPropertiesMap) {
        GWTJahiaAjaxActionResult result = new GWTJahiaAjaxActionResult();
        if (action != null) {
            if (action.equalsIgnoreCase(USER_INFO)) {
                result.setValue(userInfo(jahiaData, gwtPropertiesMap));
            } else if (action.equalsIgnoreCase(LICENSE_INFO)) {
                result.setValue(licenseInfo(jahiaData, gwtPropertiesMap));
            } else if (action.equalsIgnoreCase(SEND_EMAIL_INFO)) {
                result.setValue(sendEmail(jahiaData, gwtPropertiesMap));
            }
        }
        result.addError("Error: Action [" + action + "] not found.");
        return result;
    }

    /**
     * Get informatin about the connectged user
     *
     * @param jahiaData
     * @param gwtPropertiesMap
     * @return
     */
    public String userInfo(JahiaData jahiaData, Map gwtPropertiesMap) {
        return "user info";
    }

    /**
     * Get information about the license
     *
     * @param jahiaData
     * @param gwtPropertiesMap
     * @return
     */
    public String licenseInfo(JahiaData jahiaData, Map gwtPropertiesMap) {
        int daysLeft = LicenseManager.getInstance().getJahiaMaxUsageDays();
        if (daysLeft > 0) {
            return getMessage(jahiaData, new EngineMessage("org.jahia.bin.JahiaConfigurationWizard.congratulations.daysLeftInLicense.label", Integer.valueOf(daysLeft)));
        }
        return "unknown";
    }


    /**
     * Send email to the current user
     *
     * @param jahiaData
     * @param gwtPropertiesMap
     * @return
     */
    public String sendEmail(JahiaData jahiaData, Map gwtPropertiesMap) {
        return "send email";
    }


    /**
     * Flusch all Caches
     *
     * @param jahiaData
     * @param gwtPropertiesMap
     * @return
     */
    public String flushAllCaches(JahiaData jahiaData, Map gwtPropertiesMap) {
        GWTJahiaProperty fluschTypeProp = (GWTJahiaProperty) gwtPropertiesMap.get("flush");
        if (fluschTypeProp != null) {
            String cacheType = fluschTypeProp.getValue();
            if (cacheType != null) {

                if (cacheType.equalsIgnoreCase("Locks")) {
                    logger.debug("Flushing Locks");
                    ServicesRegistry.getInstance().getLockService().purgeLocks();
                }

                if (cacheType.equalsIgnoreCase("AllCaches")) {
                    logger.debug("Flushing all caches");
                    ServicesRegistry.getInstance().getCacheService().flushAllCaches();
                    JahiaBatchingClusterCacheHibernateProvider.flushAllCaches();
                }
            }
        }
        return "flushAllCaches";
    }

    /**
     * Get Message that depends on the currentLocale
     *
     * @param jahiaData
     * @param allowedDaysMsg
     * @return
     */
    private String getMessage(JahiaData jahiaData, EngineMessage allowedDaysMsg) {
        final Locale currentLocale = jahiaData.getProcessingContext().getLocale();
        final MessageFormat msgFormat = new MessageFormat(allowedDaysMsg.getKey(), currentLocale);
        return msgFormat.format(allowedDaysMsg.getValues());
    }
}
