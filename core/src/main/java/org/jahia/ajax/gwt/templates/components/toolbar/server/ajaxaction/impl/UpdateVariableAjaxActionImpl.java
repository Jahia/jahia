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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.content.ObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.JahiaPreferencesService;

/**
 * User: jahia
 * Date: 4 ao�t 2008
 * Time: 15:43:32
 */
public class UpdateVariableAjaxActionImpl extends AjaxAction {
    private static final JahiaPreferencesService JAHIA_PREFERENCES_SERVICE = ServicesRegistry.getInstance().getJahiaPreferencesService();
    private static final transient Logger logger = Logger.getLogger(UpdateVariableAjaxActionImpl.class);


    /**
     * Execute ajax action
     *
     * @param jahiaData
     * @param action
     * @param gwtPropertiesMap
     * @return
     */
    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action, Map<String, GWTJahiaProperty> gwtPropertiesMap) {
        GWTJahiaAjaxActionResult result = new GWTJahiaAjaxActionResult();
        if (gwtPropertiesMap != null && jahiaData != null) {
            // get name
            GWTJahiaProperty storageProp = gwtPropertiesMap.get("storage");
            boolean isSession = false;
            if (storageProp != null) {
                String storageValue = storageProp.getValue();
                isSession = storageValue.equalsIgnoreCase("session");
            }

            // get name
            GWTJahiaProperty prefNameProp = gwtPropertiesMap.get("prefName");
            if(prefNameProp == null){
                logger.error("Property 'prefName' not found. --> cancel action");
                return result;
            }
            String preferenceName = prefNameProp.getValue();

            // get value
            GWTJahiaProperty prefValueProp = gwtPropertiesMap.get(AjaxActionJahiaToolItemProvider.SELECTED);
            if(prefValueProp == null){
                logger.error("Property '"+AjaxActionJahiaToolItemProvider.SELECTED+"' not found. --> cancel action");
                return result;
            }
            String preferenceValue = prefValueProp.getValue();

            // update
            if (isSession) {
                setVarInSession(jahiaData, preferenceName, preferenceValue);
            } else {
                setGenericPreferenceValue(jahiaData, preferenceName, preferenceValue);
            }

            // get page cache flush property value
            GWTJahiaProperty flushPageCacheProp = gwtPropertiesMap.get("flushPageCache");
            boolean flushPageCacheActivated = false;
            if (flushPageCacheProp != null) {
                String flushPageCachePropStringValue = flushPageCacheProp.getValue();
                flushPageCacheActivated = flushPageCachePropStringValue.equalsIgnoreCase("true");
            }
            if (flushPageCacheActivated) {
                flushPage(jahiaData);
            }

        }
        return result;
    }


    /**
     * Set generic preference value
     *
     * @param name
     * @param value
     */
    private void setGenericPreferenceValue(JahiaData jahiaData, String name, String value) {
        JAHIA_PREFERENCES_SERVICE.setGenericPreferenceValue(name, value, jahiaData.getProcessingContext()) ;
    }

    private void setVarInSession(JahiaData jahiaData, String name, String value) {
        jahiaData.getProcessingContext().getSessionState().setAttribute(name, value);
    }

    private String flushPage(JahiaData jahiaData) {
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

}
