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
package org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
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
    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action, Map gwtPropertiesMap) {
        GWTJahiaAjaxActionResult result = new GWTJahiaAjaxActionResult();
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
