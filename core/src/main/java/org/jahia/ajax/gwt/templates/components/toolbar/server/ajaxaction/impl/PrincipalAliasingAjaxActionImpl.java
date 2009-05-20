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

import java.util.Map;

import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;

/**
 * User: jahia
 * Date: 11 juil. 2008
 * Time: 10:57:22
 */
public class PrincipalAliasingAjaxActionImpl extends AjaxAction {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PrincipalAliasingAjaxActionImpl.class);

    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action, Map gwtPropertiesMap) {
        logger.debug("***************  PrincipalAliasingAjaxActionImpl");

        GWTJahiaAjaxActionResult result = new GWTJahiaAjaxActionResult();

        GWTJahiaProperty enabledProperty = (GWTJahiaProperty) gwtPropertiesMap.get("enabled");
        GWTJahiaProperty dateProperty = (GWTJahiaProperty) gwtPropertiesMap.get("date");
        GWTJahiaProperty principalProperty = (GWTJahiaProperty) gwtPropertiesMap.get("principalKey");

        boolean previousEnabledState = false;
        JahiaUser previousAliasedUser = null;
        AdvPreviewSettings advPreviewSettings = (AdvPreviewSettings)jahiaData.getProcessingContext().getSessionState()
                .getAttribute(ProcessingContext.SESSION_ADV_PREVIEW_SETTINGS);
        if (advPreviewSettings == null){
            advPreviewSettings = new AdvPreviewSettings();
            advPreviewSettings.setMainUser(jahiaData.getProcessingContext().getUser());
        } else {
            try {
                advPreviewSettings = (AdvPreviewSettings)advPreviewSettings.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        previousEnabledState = advPreviewSettings.isEnabled();
        previousAliasedUser = advPreviewSettings.getAliasedUser();
        advPreviewSettings.setEnabled(enabledProperty != null && "true".equals(enabledProperty.getValue()));

        JahiaUser p = null;
        if (principalProperty != null && principalProperty.getValue() != null) {
            String value = principalProperty.getValue();
            logger.debug("***************  principal value" + value);
            p = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(
                    principalProperty.getValue());
        }
        // retur the url of the page ; See AdvancedPreviewJahiaToolItemProvider for more detail
        try {
            JahiaUser aliasingRootUser = null;
            if (p != null){
                aliasingRootUser = advPreviewSettings.getMainUser();
                if (aliasingRootUser == null){
                    aliasingRootUser = jahiaData.getProcessingContext().getUser();
                }
                if (!p.getUserKey().equals(aliasingRootUser.getUserKey())){
                    advPreviewSettings.setMainUser(aliasingRootUser);
                    advPreviewSettings.setAliasedUser(p);
                } else {
                    advPreviewSettings.setAliasedUser(null);
                }
            } else {
                advPreviewSettings.setAliasedUser(null);
            }

            if (dateProperty == null || dateProperty.getValue() == null || "".equals(dateProperty.getValue().trim())) {
                advPreviewSettings.setPreviewDate(0);
            } else {
                logger.debug("***************  date value" + dateProperty.getValue());
                advPreviewSettings.setPreviewDate(Long.parseLong(dateProperty.getValue()));
            }
            AdvPreviewSettings.setThreadLocalAdvPreviewSettings(advPreviewSettings);
            ParamBean jParams = (ParamBean)jahiaData.getProcessingContext();
            if (advPreviewSettings.isEnabled() && p!= null && !p.getUserKey().equals(aliasingRootUser.getUserKey())){
                jParams.switchUserSession(aliasingRootUser,p);
                jahiaData.getProcessingContext().getSessionState().setAttribute(ProcessingContext.SESSION_USER,
                        aliasingRootUser);
            } else if (previousEnabledState && previousAliasedUser != null){
                jParams.switchUserSession(previousAliasedUser,aliasingRootUser);
                jahiaData.getProcessingContext().getSessionState().setAttribute(ProcessingContext.SESSION_USER,
                        aliasingRootUser);
            }
            String currentMode = jahiaData.getProcessingContext().getOpMode();
            jahiaData.getProcessingContext().setOpMode(ProcessingContext.PREVIEW);
            String actionURL = jahiaData.gui().drawPreviewModeLink();
            jahiaData.getProcessingContext().setOpMode(currentMode);
            jahiaData.getProcessingContext().getSessionState().setAttribute(ProcessingContext.SESSION_ADV_PREVIEW_SETTINGS,
                    advPreviewSettings);
            result.setValue(actionURL);
        } catch (JahiaException e) {
            logger.error(e, e);
        }
        return result;
    }
}
