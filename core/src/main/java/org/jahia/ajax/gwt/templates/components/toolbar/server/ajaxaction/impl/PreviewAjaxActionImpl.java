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

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.commons.client.beans.GWTAjaxActionResult;
import org.jahia.ajax.gwt.commons.client.beans.GWTProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;

import java.util.Date;
import java.util.Map;

/**
 * User: jahia
 * Date: 11 juil. 2008
 * Time: 10:50:08
 */
public class PreviewAjaxActionImpl extends AjaxAction {
    private static final transient Logger logger = Logger.getLogger(PreviewAjaxActionImpl.class);

    public GWTAjaxActionResult execute(JahiaData jahiaData, String action, Map gwtPropertiesMap) {
        GWTAjaxActionResult result = new GWTAjaxActionResult();
        // To Do: generate preview link depending on a date value
        GWTProperty dateProperty = (GWTProperty) gwtPropertiesMap.get("date");
        if (dateProperty != null) {
            String dateLongValue = dateProperty.getValue();
            if (dateLongValue != null) {
                try {
                    long dateLong = Long.parseLong(dateLongValue);
                    logger.debug("Found date: " + new Date(dateLong));
                    result.setValue(jahiaData.gui().drawPreviewModeLink());
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
        // No date
        logger.debug("No date specified.");
        try {
            result.setValue(jahiaData.gui().drawPreviewModeLink());
        } catch (JahiaException e) {
            logger.error(e, e);
        }
        return result;
    }
}
