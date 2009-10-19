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
package org.jahia.services.toolbar.resolver.impl;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.impl.AdvCompareModeAjaxActionImpl;
import org.jahia.content.ContentPageKey;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.AdvCompareModeSettings;
import org.jahia.params.ProcessingContext;
import org.jahia.services.toolbar.resolver.PropertyResolver;
import org.jahia.services.version.ContentObjectEntryState;


/**
 * User: jahia
 * Date: 10 mars 2008
 * Time: 10:07:57
 */
public class URLPropertyResolver implements PropertyResolver {
    private static final transient Logger logger = Logger.getLogger(URLPropertyResolver.class);

    public static String ADMIN = "admin";


    public static String LIVE = "live";
    public static String EDIT = "edit";
    public static String COMPARE = "compare";
    public static String PREVIEW = "preview";

    public static String MONITOR = "monitor";
    public static String WORKFLOW = "workflow";


    public static String MYSETTINGS = "mysettings";
    public static String PAGE_PROPERTIES = "pageproperties";
    public static String FILEMANAGER = "filemanager";
    public static String CONTENTMANAGER = "contentmanager";
    public static String GWT_WORKFLOWMANAGER = "gwtworkflowmanager";
    public static String GWT_FILEMANAGER = "gwtfilemanager";
    public static String GWT_MASHUPMANAGER = "gwtmashupmanager";
    public static String GWT_CATEGORYMANAGER = "gwtcategorymanager";    
    public static String GWT_SITEMANAGER = "gwtsitemanager";    
    public static String GWT_TAGMANAGER = "gwttagmanager";
    public static String LOGOUT = "logout";

    // cache
    public static String FLUSH_ALL = "flushAll";

    // Edit mode paramete
    public static String TIMEBASEPUBLICHING = "timebasepublishing";
    public static String ACL_DIFF = "acldiff";


    public String getValue(JahiaData jData, String input) {
        String value = "";
        if (jData == null) {
            return value;
        } else {
            try {
                if (input.equalsIgnoreCase(ADMIN)) {
                    value = jData.gui().drawAdministrationLauncher();
                } else if (input.equalsIgnoreCase(LIVE)) {
                    value = jData.gui().drawNormalModeLink();
                } else if (input.equalsIgnoreCase(EDIT)) {
                    value = jData.gui().drawEditModeLink();
                } else if (input.equalsIgnoreCase(COMPARE)) {
                    AdvCompareModeSettings settings = (AdvCompareModeSettings)
                            jData.getProcessingContext().getSessionState()
                                    .getAttribute(ProcessingContext.SESSION_ADV_COMPARE_MODE_SETTINGS);
                    if (settings != null && settings.isEnabled()) {

                        GWTJahiaAjaxActionResult actionResult = new GWTJahiaAjaxActionResult();

                        ContentObjectEntryState version1EntryState = settings.getContentObjectEntryState(settings.getVersion1(),
                                jData.getProcessingContext().getContentPage(), jData.getProcessingContext().getLocale());
                        if (((settings.getVersion1().getDate() > 0 && !settings.getVersion2().isUseVersion())
                                || (settings.getVersion1().getVersion() != null &&
                                settings.getVersion1().isUseVersion()))
                                && (version1EntryState == null || version1EntryState.getWorkflowState() == -1)) {
                            actionResult.addError("Wrong version 1: the page does not exist or is deleted at that date");
                        }

                        ContentObjectEntryState version2EntryState = settings.getContentObjectEntryState(settings.getVersion1(),
                                jData.getProcessingContext().getContentPage(), jData.getProcessingContext().getLocale());
                        if (((settings.getVersion2().getDate() > 0 && !settings.getVersion2().isUseVersion())
                                || (settings.getVersion2().getVersion() != null &&
                                settings.getVersion2().isUseVersion()))
                                && (version1EntryState == null || version1EntryState.getWorkflowState() == -1)) {
                            actionResult.addError("Wrong version 2: the page does not exist or is deleted at that date");
                        }

                        if (version1EntryState != null && version2EntryState != null) {
                            int version1 = AdvCompareModeAjaxActionImpl
                                    .resolveVersionForComparison(settings.getVersion1());
                            int version2 = AdvCompareModeAjaxActionImpl
                                    .resolveVersionForComparison(settings.getVersion2());
                            if (version1 == 0 || version2 == 0) {
                                value = jData.gui().drawRevDifferenceModeLink(1, jData.getProcessingContext().getOperationMode());
                            } else if (version1 == version2) {
                                actionResult.addError("Versions have to be different");
                            } else {
                                value = AdvCompareModeAjaxActionImpl.getComparisonURL(version1, version2, jData, "");
                            }
                        } else {
                            value = jData.gui().drawRevDifferenceModeLink(1, jData.getProcessingContext().getOperationMode());
                        }
                    } else {
                        value = jData.gui().drawRevDifferenceModeLink(1, jData.getProcessingContext().getOperationMode());
                    }
                } else if (input.equalsIgnoreCase(PREVIEW)) {
                    value = jData.gui().drawPreviewModeLink();
                } else if (input.equalsIgnoreCase(MONITOR)) {
                    value = jData.getProcessingContext().getContextPath() + "/processing/processing.jsp";
                } else if (input.equalsIgnoreCase(WORKFLOW)) {
                    final StringBuffer keyBuff = new StringBuffer();
                    keyBuff.append(ContentPageKey.PAGE_TYPE).append("_").append(jData.getProcessingContext().getPage().getID());
                    value = jData.gui().drawWorkflowUrl(keyBuff.toString());
                } else if (input.equalsIgnoreCase(MYSETTINGS)) {
                    value = jData.gui().drawMySettingsUrl();
                } else if (input.equalsIgnoreCase(LOGOUT)) {
                    value = jData.gui().drawLogoutUrl();
                } else if (input.equalsIgnoreCase(GWT_FILEMANAGER)) {
                    value = jData.getProcessingContext().getContextPath() + "/engines/gwtfilemanager/filemanager.jsp";
                } else if (input.equalsIgnoreCase(CONTENTMANAGER)) {
                    value = jData.getProcessingContext().getContextPath() + "/engines/contentmanager/contentmanager.jsp";
                } else if (input.equalsIgnoreCase(GWT_WORKFLOWMANAGER)) {
                    value = jData.getProcessingContext().getContextPath() + "/engines/gwtworkflow/workflow.jsp";
//                    value = jData.getProcessingContext().getContextPath() + "/engines/gwtworkflow/workflow.jsp?startpage=" + jData.getProcessingContext().getContentPage() != null ? jData.getProcessingContext().getContentPage().getObjectKey().getKey() : "";
                } else if (input.equalsIgnoreCase(GWT_MASHUPMANAGER)) {
                    value = jData.getProcessingContext().getContextPath() + "/engines/gwtmashupmanager/mashup.jsp";
                } else if (input.equalsIgnoreCase(GWT_CATEGORYMANAGER)) {
                    value = jData.getProcessingContext().getContextPath() + "/engines/gwtcategorymanager/category.jsp";                    
                } else if (input.equalsIgnoreCase(GWT_TAGMANAGER)) {
                    value = jData.getProcessingContext().getContextPath() + "/engines/gwttagmanager/tag.jsp";                    
                } else if (input.equalsIgnoreCase(GWT_SITEMANAGER)) {
                    value = jData.getProcessingContext().getContextPath() + "/engines/gwtsitemanager/site.jsp";                    
                } else if (input.equalsIgnoreCase(PAGE_PROPERTIES)) {
                    value = jData.gui().drawPagePropertiesUrl();
                } else if (input.equalsIgnoreCase(CacheModeSelectedResolver.CACHE_DEBUG)) {
                    value = jData.getProcessingContext().composePageUrl() + "?containercache=debug";
                } else if (input.equalsIgnoreCase(CacheModeSelectedResolver.CACHE_ON)) {
                    value = jData.getProcessingContext().composePageUrl();
                }
                // input of a realtive url
                else {
                    value = jData.getProcessingContext().getContextPath();
                    if (input.indexOf('/') != 0) {
                        value = value + "/";
                    }
                    value = value + input;
                }

            } catch (JahiaException e) {
                logger.error(e, e);
                return null;
            }
        }
        return value;
    }

}
