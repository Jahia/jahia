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

import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.GWTJahiaRevision;
import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.data.JahiaData;
import org.jahia.params.AdvCompareModeSettings;
import org.jahia.params.ProcessingContext;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;

import java.util.Map;

/**
 * User: hollis
 * Date: 11 juil. 2008
 * Time: 10:57:22
 */
public class AdvCompareModeAjaxActionImpl extends AjaxAction {
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(AdvCompareModeAjaxActionImpl.class);

    public GWTJahiaAjaxActionResult execute(JahiaData jahiaData, String action, Map gwtPropertiesMap) {
        logger.debug("***************  AdvCompareModeAjaxActionImpl");

        GWTJahiaProperty enabledProperty = (GWTJahiaProperty) gwtPropertiesMap.get("enabled");

        AdvCompareModeSettings advCompareModeSettings = (AdvCompareModeSettings)jahiaData.getProcessingContext().getSessionState()
                .getAttribute(ProcessingContext.SESSION_ADV_COMPARE_MODE_SETTINGS);
        if (advCompareModeSettings == null){
            advCompareModeSettings = new AdvCompareModeSettings();
            jahiaData.getProcessingContext().getSessionState()
                .setAttribute(ProcessingContext.SESSION_ADV_COMPARE_MODE_SETTINGS,advCompareModeSettings);
        }
        AdvCompareModeSettings.setThreadLocalAdvCompareModeSettings(advCompareModeSettings);
        advCompareModeSettings.setEnabled(enabledProperty != null && "true".equals(enabledProperty.getValue()));

        GWTJahiaAjaxActionResult actionResult = new GWTJahiaAjaxActionResult();

        ContentObjectEntryState version1EntryState = updateVersionSetting(advCompareModeSettings.getVersion1(),
                (GWTJahiaRevision) gwtPropertiesMap.get("version1"),jahiaData);
        if (((advCompareModeSettings.getVersion1().getDate()>0 && !advCompareModeSettings.getVersion1().isUseVersion())
                || (advCompareModeSettings.getVersion1().getVersion() != null &&
                advCompareModeSettings.getVersion1().isUseVersion()))
                && ( version1EntryState == null || version1EntryState.getWorkflowState()== -1)){
            actionResult.addError("Wrong version 1: " + " the page does not exist or is deleted at that date");
        }
        ContentObjectEntryState version2EntryState = updateVersionSetting(advCompareModeSettings.getVersion2(),
                (GWTJahiaRevision) gwtPropertiesMap.get("version2"),jahiaData);
        if (((advCompareModeSettings.getVersion2().getDate()>0 && !advCompareModeSettings.getVersion2().isUseVersion())
                || (advCompareModeSettings.getVersion2().getVersion() != null &&
                advCompareModeSettings.getVersion2().isUseVersion()))
                && ( version2EntryState == null || version2EntryState.getWorkflowState()== -1)){
            actionResult.addError("Wrong version 2: the page does not exist or is deleted at that date");
        }
        String actionURL = null;
        try {
            if (advCompareModeSettings.isEnabled()){
                int version1 = resolveVersionForComparison(advCompareModeSettings.getVersion1());
                int version2 = resolveVersionForComparison(advCompareModeSettings.getVersion2());
                if (version1 == 0 || version2 == 0){
                    actionURL = jahiaData.gui().drawRevDifferenceModeLink(1,
                            jahiaData.getProcessingContext().getOperationMode());
                } else if (version1 == version2) {
                    actionResult.addError("Wrong versions have to be different");
                } else {
                    actionURL = getComparisonURL(version2,version1,jahiaData,"");
                }
            } else {
                actionURL = jahiaData.gui().drawRevDifferenceModeLink(1,
                        jahiaData.getProcessingContext().getOperationMode());
            }
            /*
            if (advCompareModeSettings.isEnabled() && advCompareModeSettings.getVersion1()
                    .getDateOrVersionValue(false)>0){
                int version1 = resolveVersionForComparison(advCompareModeSettings.getVersion1());
                int version2 = resolveVersionForComparison(advCompareModeSettings.getVersion2());
                actionURL = getComparisonURL(version2,version1,jahiaData,"");
            } else {
                actionURL = jahiaData.gui().drawRevDifferenceModeLink(1,
                        jahiaData.getProcessingContext().getOperationMode());
            }*/
        } catch ( Throwable t ){
            actionResult.addError("Unable to process your action");
            logger.debug(t);
        }
        actionResult.setValue(actionURL);
        return actionResult;
    }

    public static int resolveVersionForComparison(AdvCompareModeSettings.VersionSetting version){
        int value = 2;
        if (version.getVersion() != null && version.getVersion().getWorkflowState()
                < EntryLoadRequest.ACTIVE_WORKFLOW_STATE){
            value = new Long(version.getDateOrVersionValue(true)/1000).intValue();
        } else if (version.getVersion() != null && version.getVersion().isLive()) {
            value = ServicesRegistry.getInstance().getJahiaVersionService().getCurrentVersionID();
        } else {
            value = 2;
        }
        return value;
    }

    public static String getComparisonURL(int version1, int version2,
                                          JahiaData jData, String params) throws JahiaException {
        int oldestVersion = version1;
        int newestVersion = version2;
        int currentVersionID = ServicesRegistry.getInstance().getJahiaVersionService()
                .getCurrentVersionID();
        if (version1> EntryLoadRequest.STAGING_WORKFLOW_STATE
                && version2> EntryLoadRequest.STAGING_WORKFLOW_STATE){
            if (version1>version2){
                oldestVersion = version2;
                newestVersion = version1;
            }
        } else if (version1 == EntryLoadRequest.STAGING_WORKFLOW_STATE){
            if (version2 != EntryLoadRequest.STAGING_WORKFLOW_STATE){
                if ( version2 < currentVersionID ){
                    oldestVersion = version2;
                    newestVersion = version1;
                } else {
                    newestVersion = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                }
            }
        } else if (version2 == EntryLoadRequest.STAGING_WORKFLOW_STATE){
            if (version1 != EntryLoadRequest.STAGING_WORKFLOW_STATE){
                if ( version1 > currentVersionID ){
                    oldestVersion = EntryLoadRequest.STAGING_WORKFLOW_STATE;
                }
            }
        }
        return jData.gui().drawRevDifferenceModeLink(newestVersion,oldestVersion,
            jData.getProcessingContext().getOperationMode(),"");
    }

    public static ContentObjectEntryState updateVersionSetting(AdvCompareModeSettings.VersionSetting setting,
                                     GWTJahiaRevision versionProp, JahiaData jahiaData){
        if (versionProp != null){
            setting.setDate(versionProp.getDate());
            setting.setUseVersion(versionProp.isUseVersion());
            setting.setVersion(versionProp.getVersion());
        }
        GWTJahiaVersion version = setting.getVersion();

        ContentObjectEntryState entryState = null;
        if (setting.isUseVersion() && version != null){
            entryState = new ContentObjectEntryState(version.getWorkflowState(),
                Integer.parseInt(String.valueOf(version.getDate()/1000)),
                jahiaData.getProcessingContext().getLocale().toString());
        } else if (!setting.isUseVersion() && setting.getDate()>0){
            entryState = new ContentObjectEntryState(0,
                Integer.parseInt(String.valueOf(setting.getDate()/1000)),
                jahiaData.getProcessingContext().getLocale().toString());
        }
        try {
            entryState = jahiaData.getProcessingContext().getContentPage()
                    .getEntryState(entryState,false,true);
        } catch ( Throwable t ){
            logger.debug(t);
        }
        return entryState;
    }

}