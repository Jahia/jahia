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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.widget.toolbar.action.AjaxActionActionItem;
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
            GWTJahiaProperty prefValueProp = gwtPropertiesMap.get(AjaxActionActionItem.SELECTED);
            if(prefValueProp == null){
                logger.error("Property '"+AjaxActionActionItem.SELECTED+"' not found. --> cancel action");
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
            return "Page Flushed ";
        } catch (JahiaException e) {
            logger.error("Error while flushing page", e);
        }
        return "Page not flushed";
    }

}
