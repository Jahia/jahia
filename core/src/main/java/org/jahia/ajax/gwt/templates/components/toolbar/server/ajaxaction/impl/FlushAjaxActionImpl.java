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

package org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.ajax.gwt.commons.client.beans.GWTAjaxActionResult;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.content.ObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.engines.EngineMessage;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.registries.ServicesRegistry;

/**
 * User: jahia
 * Date: 8 juil. 2008
 * Time: 09:28:09
 */
public class FlushAjaxActionImpl extends AjaxAction {
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(FlushAjaxActionImpl.class);


    /**
     * Execute the method that correponds to the action
     *
     * @param jahiaData
     * @param action
     * @param gwtPropertiesMap
     * @return
     */
    public GWTAjaxActionResult execute(JahiaData jahiaData, String action, Map gwtPropertiesMap) {
        GWTAjaxActionResult result = new GWTAjaxActionResult();
        if (action != null) {

            if (action.equalsIgnoreCase("flushLocks")) {
                result.setValue(flushLocks(jahiaData, gwtPropertiesMap));
            }

            else if (action.equalsIgnoreCase("flushAllCaches")) {
                result.setValue(flushAllCaches(jahiaData, gwtPropertiesMap));
            }
            
            else if (action.equalsIgnoreCase("flushSite")) {
                result.setValue(flushSite(jahiaData, gwtPropertiesMap));
            }
            else if (action.equalsIgnoreCase("flushPage")) {
                result.setValue(flushPage(jahiaData, gwtPropertiesMap));
            }
        } else {
            result.addError("Error: Action [" + action + "] not found.");
        }
        return result;
    }

    public String flushPage(JahiaData jahiaData, Map gwtPropertiesMap) {
       logger.debug("Flush Page "+jahiaData.getProcessingContext().getPageID());
        try {
            final ObjectKey key = jahiaData.getProcessingContext().getPage().getContentPage().getObjectKey();
            final List<Locale> localeArrayList = jahiaData.getProcessingContext().getSite().getLanguageSettingsAsLocales(false);
            ServicesRegistry.getInstance().getCacheService().getSkeletonCacheInstance().flushPage(key, localeArrayList);
            ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance().flushPage(key,localeArrayList);
            return "Page Flushed ";
        } catch (JahiaException e) {
            logger.error(e);
        }
        return "Page not flushed";
    }

    public String flushSite(JahiaData jahiaData, Map gwtPropertiesMap) {
        final int id = jahiaData.getProcessingContext().getSiteID();
        logger.debug("Flush Site "+ id);
        try {
            ServicesRegistry.getInstance().getCacheService().getSkeletonCacheInstance().flushSkeletonsForSite(id);
            ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance().flushContainersForSite(id);
            return "Site Flushed ";
        } catch (JahiaException e) {
            logger.error(e);
        }
        return "Site not flushed";
    }

    /**
     * Flush All Caches
     *
     * @param jahiaData
     * @param gwtPropertiesMap
     * @return
     */
    public String flushAllCaches(JahiaData jahiaData, Map gwtPropertiesMap) {
        logger.debug("Flushing all caches");
        ServicesRegistry.getInstance().getCacheService().flushAllCaches();
        JahiaBatchingClusterCacheHibernateProvider.flushAllCaches();
        return "flushAllCaches";
    }

    /**
     * Flusch locks
     *
     * @param jahiaData
     * @param gwtPropertiesMap
     * @return
     */
    public String flushLocks(JahiaData jahiaData, Map gwtPropertiesMap) {
        logger.debug("Flushing Locks");
        ServicesRegistry.getInstance().getLockService().purgeLocks();
        return "Flushing Locks";
    }

    /**
     * Get Message that depends on the currentLocale
     *
     * @param jahiaData
     * @param allowedDaysMsg
     * @return
     */
    public String getMessage(JahiaData jahiaData, EngineMessage allowedDaysMsg) {
        final Locale currentLocale = jahiaData.getProcessingContext().getLocale();
        final MessageFormat msgFormat = new MessageFormat(allowedDaysMsg.getKey(), currentLocale);
        return msgFormat.format(allowedDaysMsg.getValues());
    }
}
