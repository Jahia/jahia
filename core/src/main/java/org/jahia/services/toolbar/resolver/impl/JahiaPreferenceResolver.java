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

package org.jahia.services.toolbar.resolver.impl;

import org.jahia.data.JahiaData;
import org.jahia.services.toolbar.resolver.SelectedResolver;
import org.jahia.services.toolbar.resolver.VisibilityResolver;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.registries.ServicesRegistry;
import org.apache.log4j.Logger;

/**
 * User: jahia
 * Date: 4 ao�t 2008
 * Time: 15:23:25
 */
public class JahiaPreferenceResolver implements SelectedResolver, VisibilityResolver {
    private static final transient Logger logger = Logger.getLogger(JahiaPreferenceResolver.class);

    private static transient final JahiaPreferencesService JAHIA_PREFERENCES_SERVICE = ServicesRegistry.getInstance().getJahiaPreferencesService();

    /**
     * Return true is preference with name ${type} == true
     *
     * @param jData
     * @param type
     * @return
     */
    public boolean isSelected(JahiaData jData, String type) {
        return getPreferenceValueAsBoolean(jData, type);
    }

    /**
     * Return true is preference with name ${name} == true
     *
     * @param jData
     * @param name
     * @return
     */
    private boolean getPreferenceValueAsBoolean(JahiaData jData, String name) {
        String prefValue = getGenericPreferenceValue(jData, name);
        boolean isSelected = false;
        try {
            isSelected = Boolean.parseBoolean(prefValue);
        } catch (Exception e) {
            logger.error("Preference [" + name + "] is not a boolean");
        }
        
        return isSelected;
    }

    /**
     * Return true is preference with name ${type} == true
     *
     * @param jData
     * @param type
     * @return
     */
    public boolean isVisible(JahiaData jData, String type) {
        return getPreferenceValueAsBoolean(jData, type);
    }

   /**
     * Get the generic preference provider
     *
     * @param jahiaData
     * @param name
     * @return
     */
    private String getGenericPreferenceValue(JahiaData jahiaData, String name) {
        return JAHIA_PREFERENCES_SERVICE.getGenericPreferenceValue(name, jahiaData.getProcessingContext());
    }
}
