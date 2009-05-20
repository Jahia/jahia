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

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
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

    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action, Map gwtPropertiesMap) {
        GWTJahiaAjaxActionResult result = new GWTJahiaAjaxActionResult();
        // To Do: generate preview link depending on a date value
        GWTJahiaProperty dateProperty = (GWTJahiaProperty) gwtPropertiesMap.get("date");
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
