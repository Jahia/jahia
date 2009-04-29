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

    private static final String COMPARE = "compare";
    private static final String LIVE = "live";
    private static final String PREVIEW = "preview";
    private static final String EDIT = "edit";
    private static final String ALL = "all";


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

        // guest jahiaUser
        if (!AdvPreviewSettings.isInUserAliasingMode()
                && jahiaUser.getUsername().equalsIgnoreCase(JahiaUserManagerProvider.GUEST_USERNAME)) {
            return false;
        }

        // check edit permission
        boolean editModePermission = isEditModeAllowed(processingContext);
        logger.debug("EditModePermission: " + editModePermission);
        //case of "all op.mode buttons"
        if (type.equalsIgnoreCase(ALL) && !editModePermission) {
            return false;
        }
        if (editModePermission) {
            // check write access
            boolean writeAccess = isWriteAccess(jData);
            logger.debug("WriteAccess: " + writeAccess);
            //case of "all op.mode buttons"
            if (type.equalsIgnoreCase(ALL)) {
                return writeAccess;
            }

            if (writeAccess || isAdminAccess(jData)) {
                //edit
                if (type.equalsIgnoreCase(EDIT)) {
                    return true;
                }

                ContentPage thecontentPage = jData.getProcessingContext().getContentPage();
                String thelocale = processingContext.getLocale().toString();

                if (type.equalsIgnoreCase(LIVE)) {
                    // check if there is active entries
                    boolean hasActiveEntries = hasActiveEntries(jData);
                    logger.debug("hasActiveEntries: " + hasActiveEntries);
                    if (hasActiveEntries) {
                        // ches if page is available
                        boolean isAvailable = isPageAvailabe(jData);
                        logger.debug("isAvailable: " + isAvailable);
                        return isAvailable;
                    }
                }

                // preview or compare
                if ((type.equalsIgnoreCase(COMPARE) || type.equalsIgnoreCase(PREVIEW))) {
                    boolean displayPreview = displayPreview(jData);
                    logger.debug("displayPreview: " + displayPreview);
                    return displayPreview;
                }


            }
        }
        return false;
    }

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

    private boolean isWriteAccess(JahiaData jData) {
        final ProcessingContext processingContext = jData.getProcessingContext();

        // get from attribute for performance reason
        final String attr = "OperationModeVisibilityResolver.isWriteAccess";
        final Boolean aBoolean = getBooleanAttribute(attr, processingContext);
        if (aBoolean != null) {
            return aBoolean.booleanValue();
        }

        final JahiaUser jahiaUser = processingContext.getUser();
        boolean writeAccess = jData.page().checkWriteAccess(jahiaUser, true);

        setBooleanAttribute(processingContext, attr, writeAccess);
        return writeAccess;
    }

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

    private void setBooleanAttribute(ProcessingContext processingContext, String displayPreviewAttr, boolean displayPreview) {
        processingContext.setAttribute(displayPreviewAttr, new Boolean(displayPreview));
    }

    private Boolean getBooleanAttribute(String attr, ProcessingContext processingContext) {
        final Object o = processingContext.getAttribute(attr);
        if (o != null) {
            logger.debug("["+attr+"] found in processinContext");
            return (Boolean) o;
        }
        return null;
    }
}
