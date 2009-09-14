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

import org.jahia.data.JahiaData;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.toolbar.resolver.VisibilityResolver;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;

/**
 * User: jahia
 * Date: 8 juil. 2008
 * Time: 11:04:03
 */
public class OperationModeVisibilityResolver implements VisibilityResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(OperationModeVisibilityResolver.class);

    public static final String COMPARE = "compare";
    public static final String LIVE = "live";
    public static final String PREVIEW = "preview";
    public static final String EDIT = "edit";
    public static final String ALL = "all";


    /**
     * Chech visibility of the current type
     * @param jData
     * @param type
     * @return
     */
    public boolean isVisible(JahiaData jData, String type) {
        // type unknown
        if (type == null || jData == null) {
            return false;
        }

        final ProcessingContext processingContext = jData.getProcessingContext();
        final JahiaUser jahiaUser = processingContext.getUser();

        if (jahiaUser == null){
            return false;
        }

        return true;
//
//        // guest jahiaUser
//        if (!AdvPreviewSettings.isInUserAliasingMode()
//                && jahiaUser.getUsername().equalsIgnoreCase(JahiaUserManagerProvider.GUEST_USERNAME)) {
//            return false;
//        }
//
//        // check edit permission
//        boolean editModePermission = isEditModeAllowed(processingContext);
//        logger.debug("EditModePermission: " + editModePermission);
//        //case of "all op.mode buttons"
//        if (type.equalsIgnoreCase(ALL) && !editModePermission) {
//            return false;
//        }
//        if (editModePermission) {
//            // check write access
//            boolean writeAccess = isWriteAccess(jData);
//            logger.debug("WriteAccess: " + writeAccess);
//            //case of "all op.mode buttons"
//            if (type.equalsIgnoreCase(ALL)) {
//                return writeAccess;
//            }
//
//            if (writeAccess || isAdminAccess(jData)) {
//                //edit
//                if (type.equalsIgnoreCase(EDIT)) {
//                    return true;
//                }
//
//                ContentPage thecontentPage = jData.getProcessingContext().getContentPage();
//                String thelocale = processingContext.getLocale().toString();
//
//                if (type.equalsIgnoreCase(LIVE)) {
//                    // check if there is active entries
//                    boolean hasActiveEntries = hasActiveEntries(jData);
//                    logger.debug("hasActiveEntries: " + hasActiveEntries);
//                    if (hasActiveEntries) {
//                        // ches if page is available
//                        boolean isAvailable = isPageAvailabe(jData);
//                        logger.debug("isAvailable: " + isAvailable);
//                        return isAvailable;
//                    }
//                }
//
//                // preview or compare
//                if ((type.equalsIgnoreCase(COMPARE) || type.equalsIgnoreCase(PREVIEW))) {
//                    boolean displayPreview = displayPreview(jData);
//                    logger.debug("displayPreview: " + displayPreview);
//                    return displayPreview;
//                }
//
//
//            }
//        }
//        return false;
    }


    /**
     * Check if preview link can be displayed
     * @param jData
     * @return
     */
    private boolean displayPreview(JahiaData jData) {
        final ProcessingContext processingContext = jData.getProcessingContext();

        // get from attribute for performance reason
        final String attr = "OperationModeVisibilityResolver.displayPreview";
        final Boolean aBoolean = getBooleanAttribute(attr, processingContext);
        if (aBoolean != null) {
            return aBoolean.booleanValue();
        }

        // value not present --> compute
        final ContentPage thecontentPage = jData.getProcessingContext().getContentPage();
        final String thelocale = processingContext.getLocale().toString();
        boolean displayPreview = (!thecontentPage.isStagedEntryMarkedForDeletion(thelocale));

        // set it in attr
        setBooleanAttribute(processingContext, attr, displayPreview);
        return displayPreview;
    }


    /**
     * Check if page is available
     * @param jData
     * @return
     */
    private boolean isPageAvailabe(JahiaData jData) {
        final ProcessingContext processingContext = jData.getProcessingContext();

        // get from attribute for performance reason
        final String attr = "OperationModeVisibilityResolver.isPageAvailabe";
        final Boolean aBoolean = getBooleanAttribute(attr, processingContext);
        if (aBoolean != null) {
            return aBoolean.booleanValue();
        }

        final ContentPage thecontentPage = jData.getProcessingContext().getContentPage();
        boolean isAvailable = thecontentPage.isAvailable();

        setBooleanAttribute(processingContext, attr, isAvailable );
        return isAvailable;
    }

    /**
     * Check if current content page gas active entries
     * @param jData
     * @return
     */
    private boolean hasActiveEntries(JahiaData jData) {
        final ProcessingContext processingContext = jData.getProcessingContext();

        // get from attribute for performance reason
        final String attr = "OperationModeVisibilityResolver.hasActiveEntries";
        final Boolean aBoolean = getBooleanAttribute(attr, processingContext);
        if (aBoolean != null) {
            return aBoolean.booleanValue();
        }

        final ContentPage thecontentPage = jData.getProcessingContext().getContentPage();
        final String thelocale = processingContext.getLocale().toString();
        boolean hasActiveEntries = thecontentPage.hasEntries(ContentPage.ACTIVE_PAGE_INFOS, thelocale);

        setBooleanAttribute(processingContext, attr, hasActiveEntries);
        return hasActiveEntries;
    }

    /**
     * Check admin access
     * @param jData
     * @return
     */
    private boolean isAdminAccess(JahiaData jData) {
        final ProcessingContext processingContext = jData.getProcessingContext();

        // get from attribute for performance reason
        final String attr = "OperationModeVisibilityResolver.isAdminAccess";
        final Boolean aBoolean = getBooleanAttribute(attr, processingContext);
        if (aBoolean != null) {
            return aBoolean.booleanValue();
        }

        final JahiaUser jahiaUser = processingContext.getUser();

        boolean isAdminAccess = jData.page().checkAdminAccess(jahiaUser, true);
        setBooleanAttribute(processingContext, attr, isAdminAccess);
        return isAdminAccess;
    }

    /**
     * Check write access
     * @param jData
     * @return
     */
    private boolean isWriteAccess(JahiaData jData) {
        final ProcessingContext processingContext = jData.getProcessingContext();

        // get from attribute for performance reason
        final String attr = "OperationModeVisibilityResolver.isWriteAccess";
        final Boolean aBoolean = getBooleanAttribute(attr, processingContext);
        if (aBoolean != null) {
            return aBoolean.booleanValue();
        }

        final JahiaUser jahiaUser = processingContext.getUser();

        boolean writeAccess = jData.page() != null && jData.page().checkWriteAccess(jahiaUser, true);

        setBooleanAttribute(processingContext, attr, writeAccess);
        return writeAccess;
    }

    /**
     * Check if edit mode permission
     * @param processingContext
     * @return
     */
    private boolean isEditModeAllowed(ProcessingContext processingContext) {
        // get from attribute for performance reason
        final String attr = "OperationModeVisibilityResolver.isEditModeAllowed";
        final Boolean aBoolean = getBooleanAttribute(attr, processingContext);
        if (aBoolean != null) {
            return aBoolean.booleanValue();
        }

        boolean editModePermission = ServicesRegistry.getInstance().getJahiaACLManagerService()
                .getSiteActionPermission("engines.actions.editMode", processingContext.getUser(), 
                        org.jahia.services.acl.JahiaBaseACL.READ_RIGHTS, processingContext.getSiteID()) > 0;

        setBooleanAttribute(processingContext, attr, editModePermission);
        return editModePermission;
    }

    /**
     * Set attribute as a boolean
     * @param processingContext
     * @param displayPreviewAttr
     * @param displayPreview
     */
    private void setBooleanAttribute(ProcessingContext processingContext, String displayPreviewAttr, boolean displayPreview) {
        processingContext.setAttribute(displayPreviewAttr, new Boolean(displayPreview));
    }

    /**
     * Get attrinute as a boolean
     * @param attr
     * @param processingContext
     * @return
     */
    private Boolean getBooleanAttribute(String attr, ProcessingContext processingContext) {
        final Object o = processingContext.getAttribute(attr);
        if (o != null) {
            logger.debug("["+attr+"] found in processinContext");
            return (Boolean) o;
        }
        return null;
    }
}
