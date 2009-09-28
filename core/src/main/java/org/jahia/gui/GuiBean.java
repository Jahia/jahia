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
//
//  GuiBean
//  EV      03.11.2000
//	DJ		02.02.2001 - added ACL check for link display
//  JB      16.05.2001
//

package org.jahia.gui;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.restorelivecontainer.RestoreLiveContainer_Engine;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLResource;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.utils.JahiaTools;
import org.jahia.api.Constants;

/**
 * Modified and cleaned by Xavier Lawrence
 */
public class GuiBean {

    private static final transient Logger logger = Logger.getLogger(GuiBean.class);

    private final ProcessingContext processingContext;
    private final HTMLToolBox html;

    protected GuiBean() {
        processingContext = null;
        html = null;
    }

    /**
     * constructor
     * EV    03.11.2000
     */
    public GuiBean(final ProcessingContext jContext) {
        this.processingContext = jContext;
        this.html = new HTMLToolBox(this, processingContext);
    } // end constructor

    public HTMLToolBox html() {
        return html;
    }

    public ProcessingContext params() {
        return processingContext;
    }

    /**
     * drawHttpPath
     * EV    21.11.2000
     */
    public String drawHttpPath() {
        if (processingContext != null) {
            return processingContext.settings().getJahiaTemplatesHttpPath();
        } else {
            return "";
        }
    } // end drawHttpPath


    /**
     * drawHttpPath
     * JB    25.04.2001
     */
    public String drawHttpPath(final String theTemplateFolderName) {
        if (processingContext != null) {
            final StringBuffer buff = new StringBuffer();
            buff.append(processingContext.settings().getJahiaTemplatesHttpPath()).
                    append(theTemplateFolderName);
            return buff.toString();
        } else {
            return "";
        }
    } // end drawHttpPath


    //-------------------------------------------------------------------------
    /**
     * returns the URL of the JSP context
     */
    public String drawHttpJspContext(final HttpServletRequest req) {
        return req.getRequestURI().substring(0, req.getRequestURI().lastIndexOf("/"));
    }

    /**
     * drawPageLink
     * EV    21.11.2000
     */
    public String drawPageLink(final int thePageID)
            throws JahiaException {
        String result = "";

        if (processingContext != null) {
            // Get the current page
            final JahiaPage currentPage = processingContext.getPage();
            if (currentPage != null) {
                result = processingContext.composePageUrl(thePageID);
            }
        }
        return result;
    } // end drawPageLink

    /**
     * Return the current page action url with cache/offonce
     *
     * @return
     * @throws JahiaException
     */
    public String getPageActionURL() throws JahiaException {
        String actionURL = "";
        if (processingContext != null) {
            actionURL = processingContext.composePageUrl(processingContext.getPage().getID());
        }
        return actionURL;
    }

    /**
     * returns the URL allowing to switch between normal mode and edition mode.
     * EV    21.11.2000
     */
    public String drawSwitchModeLink() throws JahiaException {
        String result = "";

        if (processingContext != null) {
            // Get the current page
            final JahiaPage currentPage = processingContext.getPage();
            if (currentPage != null) {
                // Check the write permission on the page
                if (currentPage.checkWriteAccess(processingContext.getUser())) {
                    if (processingContext.getOperationMode().equals(ProcessingContext.NORMAL)
                            || processingContext.getOperationMode().equals(ProcessingContext.COMPARE)) {
                        result = processingContext.composeOperationUrl(ProcessingContext.EDIT, "");
                    } else {
                        result = processingContext.composeOperationUrl(ProcessingContext.NORMAL, "");
                    }
                }
            }
        }
        return result;
    } // end drawSwitchModeLink

    /**
     * Builds an URL to the current page in view mode
     *
     * @return url string
     * @throws JahiaException
     */
    public String drawNormalModeLink() throws JahiaException {
        String result = "";

        if (processingContext != null) {
            // Get the current page
            final JahiaPage currentPage = processingContext.getPage();
            if (currentPage != null) {
                // Check the write permission on the page
                result = processingContext.composeOperationUrl(ProcessingContext.NORMAL, "");
            }
        }
        return result;
    }

    /**
     * Builds an URL to the current page in edit mode, only if the current
     * user has that right. If not an empty string is returned.
     *
     * @return The URL to the current page in edit mode
     * @throws JahiaException
     */
    public String drawEditModeLink() throws JahiaException {
        return drawEditModeLink(ProcessingContext.USERALIASING_MODE_OFF);
    }

    /**
     * @param userAliasingMode
     * @return
     * @throws JahiaException
     */
    public String drawEditModeLink(String userAliasingMode) throws JahiaException {
        String result = "";

        if (processingContext != null) {
            // Get the current page
            final JahiaPage currentPage = processingContext.getPage();
            if (currentPage != null) {
                if (currentPage.checkWriteAccess(processingContext.getUser(), true)) {
                    boolean isInUserAliasingMode = AdvPreviewSettings.isInUserAliasingMode();
                    String params = "";
                    if (isInUserAliasingMode && userAliasingMode != null) {
                        params = ProcessingContext.USERALIASING_MODE_PARAMETER + "=" + userAliasingMode;
                    }
                    result = processingContext.composeOperationUrl(ProcessingContext.EDIT, params);
                } else {
                    result = "";
                }
                //result = processingContext.composeOperationUrl( ProcessingContext.EDIT, "" );
            }
        }
        return result;
    }

    /**
     * Builds an URL to the current page in compare mode, only if the current
     * user has that right. If not an empty string is returned.
     *
     * @param revisionDiffID 0 to compare with staging.
     * @param operationMode
     * @return
     * @throws JahiaException
     */
    public String drawRevDifferenceModeLink(final int revisionDiffID,
                                            final String operationMode) throws JahiaException {
        String result = "";

        if (processingContext != null) {
            // Get the current page
            final JahiaPage currentPage = processingContext.getPage();
            if (currentPage != null) {
                /* NK : EDIT_MODE_PAGE_ACCESS_ISSUE */
                // Check the write permission on the page
                if (currentPage.checkWriteAccess(processingContext.getUser(), true)
                        || currentPage.checkAdminAccess(processingContext.getUser(), true)) {
                    result = processingContext.composeRevDifferenceUrl(revisionDiffID, operationMode, "");
                } else {
                    result = "";
                }
                //result = processingContext.composeRevDifferenceUrl(revisionDiffID,operationMode,"");
            }
        }
        return result;
    }

    /**
     * Generate a page url in compare mode between entryStateVersion version and revisionDiffVersion version.
     *
     * @param entryStateVersion
     * @param revisionDiffVersion
     * @param operationMode
     * @param params
     * @return
     * @throws JahiaException
     */
    public String drawRevDifferenceModeLink(final int entryStateVersion,
                                            final int revisionDiffVersion, final String operationMode,
                                            final String params) throws JahiaException {
        String result = "";

        if (processingContext != null) {
            // Get the current page
            final JahiaPage currentPage = processingContext.getPage();
            if (currentPage != null) {
                if (currentPage.checkWriteAccess(processingContext.getUser(), true)
                        || currentPage.checkAdminAccess(processingContext.getUser(), true)) {
                    result = processingContext.composeRevDifferenceUrl(entryStateVersion, revisionDiffVersion, operationMode, params);
                } else {
                    result = "";
                }
            }
        }
        return result;
    }

    /**
     * Builds an URL to the current page in staging preview  mode, only if
     * the current user has that right. If not an empty string is returned.
     *
     * @return
     * @throws JahiaException
     */
    public String drawPreviewModeLink() throws JahiaException {
        return drawPreviewModeLink(ProcessingContext.USERALIASING_MODE_ON);
    }

    /**
     * Builds an URL to the current page in staging preview  mode, only if
     * the current user has that right. If not an empty string is returned.
     *
     * @return
     * @throws JahiaException
     */
    public String drawPreviewModeLink(String userAliasingMode) throws JahiaException {
        String result = "";

        if (processingContext != null) {
            // Get the current page
            final JahiaPage currentPage = processingContext.getPage();
            if (currentPage != null) {
                if (currentPage.checkWriteAccess(processingContext.getUser(), true)
                        || currentPage.checkAdminAccess(processingContext.getUser(), true)) {
                    String params = "";
                    AdvPreviewSettings advPreviewSettings = AdvPreviewSettings.getThreadLocaleInstance();
                    boolean appendUserAliasingMode = advPreviewSettings != null && advPreviewSettings.isEnabled();
                    if (appendUserAliasingMode) {
                        params = ProcessingContext.USERALIASING_MODE_PARAMETER + "=" + userAliasingMode;
                    }
                    result = processingContext.composeOperationUrl(ProcessingContext.PREVIEW, params);
                } else {
                    result = "";
                }
                //result = processingContext.composeOperationUrl( ProcessingContext.PREVIEW, "" );
            }
        }
        return result;
    }

    /**
     * Build an URL containing the language code the displayed page should
     * switch to.
     *
     * @param code The iso639 language code
     * @return The URL string composed
     * @throws JahiaException
     */
    public String drawPageLanguageSwitch(final String code) throws JahiaException {
        String result = "";
        if (processingContext != null) {
            // Get the current page
            final JahiaPage currentPage = processingContext.getPage();
            if (currentPage != null) {
                result = processingContext.composeLanguageURL(code);
            }
        }
        return result;
    }

    /**
     * Build an URL containing the language code the given page should
     * switch to.
     *
     * @param code The iso639 language code
     * @return The URL string composed
     * @throws JahiaException
     */
    public String drawPageLanguageSwitch(final String code, final int pid) throws JahiaException {
        String result = "";
        if (processingContext != null) {
            result = processingContext.composeLanguageURL(code, pid);
        }
        return result;
    }

    public String drawPopupLoginUrl() throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        return drawUrl("login", buff.append("/pid/").append(processingContext.getPageID()).toString());
    }

    public String drawPopupLoginUrl(final int destinationPageID)
            throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        return drawUrl("login", buff.append("/pid/").append(destinationPageID).toString());
    }

    public String drawLoginUrl() throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        return drawUrl("login", buff.append("/pid/").append(processingContext.getPageID()).
                append("?screen=save").toString());
    }

    public String drawLoginUrl(final int destinationPageID)
            throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        return drawUrl("login", buff.append("/pid/").append(destinationPageID).
                append("?screen=save").toString());
    }
    // end drawLoginUrl

    // this draw logout is called by the other ones too

    public String drawPopupLogoutUrl(final int destinationPageID)
            throws JahiaException {
        if (!processingContext.getUser().getUsername().equals("guest")) {
            return drawUrl("logout", new Integer(destinationPageID));
        } else {
            return "";
        }
    }

    public String drawPopupLogoutUrl() throws JahiaException {
        return drawPopupLogoutUrl(processingContext.getPageID());
    }

    public String drawLogoutUrl(final int destinationPageID)
            throws JahiaException {
        return drawPopupLogoutUrl(destinationPageID);
    }

    public String drawLogoutUrl() throws JahiaException {
        return drawPopupLogoutUrl(processingContext.getPageID());
    }

    /**
     * @param theField
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawUpdateFieldUrl(final JahiaField theField)
            throws JahiaException {
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        if (aclService.getSiteActionPermission("engines.actions.update",
                processingContext.getUser(), JahiaBaseACL.READ_RIGHTS,
                processingContext.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                        processingContext.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        processingContext.getSiteID()) > 0) {
            final ContentField contentField = theField.getContentField();
            return drawUrlCheckWriteAccess("updatefield", contentField);
        } else {
            return "";
        }
    }

    /**
     * @param contentField
     * @return
     * @throws JahiaException
     */
    public String drawUpdateFieldUrl(final ContentField contentField)
            throws JahiaException {
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        if (aclService.getSiteActionPermission("engines.actions.update",
                processingContext.getUser(), JahiaBaseACL.READ_RIGHTS,
                processingContext.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                        processingContext.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        processingContext.getSiteID()) > 0) {
            return drawUrlCheckWriteAccess("updatefield", contentField);
        } else {
            return "";
        }
    }

    /**
     * @param jahiaContainerList
     * @return
     * @throws JahiaException
     */
    public String drawAddContainerUrl(final JahiaContainerList jahiaContainerList)
            throws JahiaException {
        if (jahiaContainerList != null) {
            final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
            if (aclService.getSiteActionPermission("engines.actions.add",
                    processingContext.getUser(), JahiaBaseACL.READ_RIGHTS,
                    processingContext.getSiteID()) > 0 &&
                    aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                            processingContext.getUser(),
                            JahiaBaseACL.READ_RIGHTS,
                            processingContext.getSiteID()) > 0) {
                return drawUrlCheckWriteAccess("addcontainer", jahiaContainerList);
            } else {
                return "";
            }
        } else {
            final JahiaException je = new JahiaException("Accessing non-existing data",
                    "GuiBean : Trying to access to non-existing containerlist (addContainerUrl)",
                    JahiaException.DATA_ERROR, JahiaException.WARNING_SEVERITY);
            logger.warn("Trying to generate URL for null container list", je);
            return "";
        }
    }

    /**
     * @param theContainer
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawUpdateContainerUrl(final JahiaContainer theContainer)
            throws JahiaException {
        final ContentContainer contentContainer = theContainer.getContentContainer();
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        if (aclService.getSiteActionPermission("engines.actions.update",
                processingContext.getUser(), JahiaBaseACL.READ_RIGHTS,
                processingContext.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                        processingContext.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        processingContext.getSiteID()) > 0) {
            return drawUrlCheckWriteAccess("updatecontainer", contentContainer);
        } else {
            return "";
        }
    }

    /**
     * @param contentContainer
     * @return
     * @throws JahiaException
     */
    public String drawUpdateContainerUrl(final ContentContainer contentContainer)
            throws JahiaException {
        return drawUpdateContainerUrl(contentContainer, 0);
    }

    /**
     * @param contentContainer
     * @param focusedFieldId
     * @return
     * @throws JahiaException
     */
    public String drawUpdateContainerUrl(final ContentContainer contentContainer, int focusedFieldId)
            throws JahiaException {
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        String url = "";
        if (aclService.getSiteActionPermission("engines.actions.update",
                processingContext.getUser(), JahiaBaseACL.READ_RIGHTS,
                processingContext.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                        processingContext.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        processingContext.getSiteID()) > 0) {
            url = drawUrlCheckWriteAccess("updatecontainer", contentContainer);
        }
        if (focusedFieldId > 0 && url.length() > 0) {
            url = new StringBuffer(url.length() + 16).append(url).append(
                    "&fid=").append(focusedFieldId).toString();
        }

        return url;
    }


    /**
     * @param theContainer
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawDeleteContainerUrl(final JahiaContainer theContainer)
            throws JahiaException {
        final ContentContainer contentContainer = theContainer.getContentContainer();
        return drawUrlCheckWriteAccess("deletecontainer", contentContainer);
    }

    /**
     * @param contentContainer
     * @return
     * @throws JahiaException
     */
    public String drawDeleteContainerUrl(final ContentContainer contentContainer)
            throws JahiaException {
        if (ServicesRegistry.getInstance().getJahiaACLManagerService().
                getSiteActionPermission("engines.actions.delete",
                        processingContext.getUser(), JahiaBaseACL.READ_RIGHTS,
                        processingContext.getSiteID()) > 0) {
            /** todo we removed this version of the action check because it was dreadfully
             * slow to check the rights on the whole sub tree, which could be very large.
             * We might want to do this check when opening the engine instead, or use AJAX
             * to indicate the background process is running.
             */
            // return drawUrlCheckWriteAccess( "deletecontainer", contentContainer,true,true);
            return drawUrlCheckWriteAccess("deletecontainer", contentContainer);
        } else {
            return "";
        }
    }

    public String drawWorkflowUrl(final String key)
            throws JahiaException {

        return drawUrl("workflow", key);
    }

    public String drawLockUrl(final LockKey lockKey)
            throws JahiaException {
        return drawUrl("lock", lockKey);
    }

    public String drawPagePropertiesUrl() throws JahiaException {
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        if (aclService.getSiteActionPermission("engines.actions.update",
                processingContext.getUser(), JahiaBaseACL.READ_RIGHTS,
                processingContext.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                        processingContext.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        processingContext.getSiteID()) > 0) {
            return drawUrlCheckWriteAccess("pageproperties", processingContext.getPage());
        } else {
            return "";
        }
    }

    public String drawPagePropertiesUrl(final ContentPage page) throws JahiaException {
        return drawPagePropertiesUrl(page.getID());
    }

    public String drawPagePropertiesUrl(final int pageID) throws JahiaException {
        final int oldPageID = processingContext.getPageID();
        if (oldPageID != pageID) processingContext.changePage(pageID);
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        final String result;
        if (aclService.getSiteActionPermission("engines.actions.update",
                processingContext.getUser(), JahiaBaseACL.READ_RIGHTS,
                processingContext.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                        processingContext.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        processingContext.getSiteID()) > 0) {
            result = drawUrlCheckWriteAccess("pageproperties", processingContext.getPage());
        } else {
            result = "";
        }
        if (oldPageID != pageID) processingContext.changePage(oldPageID);
        return result;
    }

    public String drawUpdateTemplateUrl(final JahiaPageDefinition theTemplate)
            throws JahiaException {
        return drawUrlCheckWriteAccess("template", theTemplate);
    }

    public String drawUpdateCategoryUrl(final Category category)
            throws JahiaException {
        return drawUrl("categoryEdit", category);
    }

    public String drawAddSubCategoryUrl(final String parentCategoryKey)
            throws JahiaException {
        return drawUrl("categoryEdit", parentCategoryKey);
    }

    public String drawUpdateCategoryUrl(final Category category, final int nodeIndex)
            throws JahiaException {
        return drawUrl("categoryEdit", category);
    }

    public String drawAddSubCategoryUrl(final String parentCategoryKey, final int nodeIndex)
            throws JahiaException {
        return drawUrl("categoryEdit", parentCategoryKey);
    }

    /**
     * @param theContainerList
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawContainerListPropertiesUrl(final JahiaContainerList theContainerList)
            throws JahiaException {
        final ContentContainerList contentContainerList = theContainerList.getContentContainerList();
        return drawUrlCheckWriteAccess("containerlistproperties", contentContainerList);
    }

    /**
     * @param contentContainerList
     * @return
     * @throws JahiaException
     */
    public String drawContainerListPropertiesUrl(final ContentContainerList contentContainerList)
            throws JahiaException {
        if (contentContainerList != null) {
            return drawUrlCheckWriteAccess("containerlistproperties",
                    contentContainerList);
        } else {
            return "";
        }
    }

    /**
     * Generates an URL for a NEXT WINDOW on a containerlist.
     *
     * @param theContainerList   the containerList for which to generate the URL
     * @param pageStep           the number of pages to skip through.
     * @param newWindowSize      the size of the window (optional). -1 to keep
     *                           current window size.
     * @param scrollingValueOnly if true, return only the scrolling
     *                           value , i.e. '5_10' , instead of the full url.
     * @return the URL for the operation specified.
     * @throws JahiaException on error accessing containerList definitions either
     *                        in memory or in the database.
     */
    public String drawContainerListNextWindowPageURL(final JahiaContainerList theContainerList,
                                                     final int pageStep,
                                                     final int newWindowSize,
                                                     final boolean scrollingValueOnly,
                                                     final String listViewId)

            throws JahiaException {
        return drawContainerListWindowPageURL(theContainerList, pageStep,
                newWindowSize,
                scrollingValueOnly, listViewId);
    }

    /**
     * Generates an URL for a PREVIOUS WINDOW on a containerlist.
     *
     * @param theContainerList   the containerList for which to generate the URL
     * @param pageStep           the number of pages to skip through.
     * @param newWindowSize      the size of the window (optional). -1 to keep
     *                           current window size.
     * @param scrollingValueOnly if true, return only the scrolling
     *                           value , i.e. '5_10' , instead of the full url.
     * @return the URL for the operation specified.
     * @throws JahiaException on error accessing containerList definitions either
     *                        in memory or in the database.
     */
    public String drawContainerListPreviousWindowPageURL(final JahiaContainerList theContainerList,
                                                         final int pageStep,
                                                         final int newWindowSize,
                                                         final boolean scrollingValueOnly,
                                                         final String listViewId)
            throws JahiaException {
        return drawContainerListWindowPageURL(theContainerList, -pageStep,
                newWindowSize,
                scrollingValueOnly, listViewId);
    }

    /**
     * Generates an URL that allows navigation within a scrollable container
     * list.
     *
     * @param theContainerList   the containerList for which to generate the URL
     * @param pageStep           the number of pages to switch, this may be positive or
     *                           negative, going both ways.
     * @param newWindowSize      the size of the window, optional (set to -1 if deactivated).
     * @param scrollingValueOnly if true, return only the scrolling value,
     *                           i.e. '5_10' , instead of the full url.
     * @return the URL generated for this operation. Size and offset are correct
     *         to match containerList size.
     * @throws JahiaException on error accessing containerList definitions either
     *                        in memory or in the database.
     */
    public String drawContainerListWindowPageURL(final JahiaContainerList theContainerList,
                                                 final int pageStep,
                                                 final int newWindowSize,
                                                 final boolean scrollingValueOnly,
                                                 final String listViewId)
            throws JahiaException {

        if (theContainerList == null) {
            return "";
        }

        JahiaContainerListPagination cListPagination = theContainerList.getCtnListPagination();
        if (cListPagination == null || (newWindowSize != -1 && (cListPagination.getWindowSize() != newWindowSize))) {
            cListPagination = new JahiaContainerListPagination(theContainerList, processingContext, newWindowSize);
            theContainerList.setCtnListPagination(cListPagination);
        }

        // For next & previous button check
        if (!cListPagination.isValid() || (cListPagination.getNbPages() < 2)) {
            // feature is deactivated.
            return "";
        }

        // For previous button check
        if ((cListPagination.getCurrentPageIndex() == 1) && (pageStep < 0)) {
            // feature is deactivated.
            return "";
        }

        // For next button check
        final int nextPage = cListPagination.getCurrentPageIndex() + 1;
        if ((nextPage > cListPagination.getNbPages()) && (pageStep > 0)) {
            // feature is deactivated.
            return "";
        }

        final int windowSize = cListPagination.getWindowSize();
        int windowOffset = cListPagination.getWindowOffset();
        windowOffset += pageStep * windowSize;

        // we now have all the correct values, let's build the URL...

        final StringBuffer paramName = new StringBuffer();
        paramName.append(ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER)
                .append(listViewId != null ? listViewId + "_" : "")
                .append(theContainerList.getDefinition().getName());
        final StringBuffer paramValue = new StringBuffer();
        paramValue.append(Integer.toString(windowSize)).append("_").
                append(Integer.toString(windowOffset));

        if (scrollingValueOnly) {
            return paramValue.toString();
        }
        final StringBuffer curPageURL = new StringBuffer();
        curPageURL.append(processingContext.composePageUrl(processingContext.getPageID()));
        curPageURL.append("?").append(paramName).append("=").append(paramValue);
        // curPageURL.append("/").append(paramName).append("/").append(paramValue);
        return curPageURL.toString();
    }

    /**
     * Generates an URL that allows navigation within a scrollable container
     * list.
     *
     * @param containerList      the container list with a valid JahiaContainerListPagination bean.
     * @param pageStep           must be bigger than 0.
     * @param scrollingValueOnly if true, return only the scrolling value , i.e. '5_10' , instead of the full url.
     * @return the URL generated for this operation.
     * @throws JahiaException on error accessing containerList definitions either
     *                        in memory or in the database.
     */
    public String drawContainerListWindowPageURL(final JahiaContainerList containerList,
                                                 final int pageStep,
                                                 final boolean scrollingValueOnly,
                                                 final String listViewId)

            throws JahiaException {

        if (containerList == null || containerList.getCtnListPagination() == null ||
                !containerList.getCtnListPagination().isValid() || (pageStep <= 0)) {
            return null;
        }

        final StringBuffer paramName = new StringBuffer();
        paramName.append(ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER)
                .append(listViewId != null ? listViewId + "_" : "")
                .append(containerList.getDefinition().getName());
        final StringBuffer paramValue = new StringBuffer();
        paramValue.append(Integer.toString(containerList.getCtnListPagination().getWindowSize())).
                append("_").append(Integer.toString((pageStep - 1) * containerList.getCtnListPagination().
                getWindowSize()));

        if (scrollingValueOnly) {
            return paramValue.toString();
        }
        final StringBuffer curPageURL = new StringBuffer();
        curPageURL.append(processingContext.composePageUrl(processingContext.getPageID()));
        curPageURL.append("?").append(paramName).append("=").append(paramValue);
        // curPageURL.append("/").append(paramName).append("/").append(paramValue);
        return curPageURL.toString();
    }

    public String drawSearchUrl() throws JahiaException {
        return drawUrl("search", null);
    }
    // end drawSearchUrl

    public String drawMySettingsUrl() throws JahiaException {
        return drawUrl("mysettings", null);
    }

    public String drawMySettingsUrl(Object theObj) throws JahiaException {
        return drawUrl("mysettings", theObj);
    }

    public String drawNewUserRegistrationUrl() throws JahiaException {
        return drawUrl("newuserregistration", null);
    }

    public String drawNewUserRegistrationUrl(Object theObj) throws JahiaException {
        return drawUrl("newuserregistration", theObj);
    }

    public String drawSiteMapUrl() throws JahiaException {
        return drawUrl("sitemap", null);
    }
    // end drawSiteMapUrl

    /**
     * drawAdministrationLauncher
     * MJ    21.03.2001
     */
    public String drawAdministrationLauncher() throws JahiaException {
        final StringBuffer url = new StringBuffer();
        url.append(processingContext.getContextPath()).append(Jahia.getInitAdminServletPath()).
                append("?do=passthru");
        return url.toString();
    }
    // end drawAdministrationLauncher


    /**
     * drawUrl
     * EV    15.12.2000
     */
    private String drawUrl(final String engineName, final Object theObj)
            throws JahiaException {
        String htmlResult = "";
        final JahiaEngine theEngine = (JahiaEngine) EnginesRegistry.getInstance().
                getEngine(engineName);
        if (theEngine.authoriseRender(processingContext)) {
            htmlResult = theEngine.renderLink(processingContext, theObj);
        }
        return htmlResult;
    }
    // end drawUrl

    private String drawUrlCheckWriteAccess(final String engineName, final Object anObject)
            throws JahiaException {
        return drawUrlCheckWriteAccess(engineName, anObject, false, false);
    }

    private String drawUrlCheckWriteAccess(final String engineName, final Object anObject, boolean checkChilds, boolean forceChilds)
            throws JahiaException {
        final JahiaEngine theEngine = (JahiaEngine) EnginesRegistry.getInstance().getEngine(engineName);
        if (theEngine.authoriseRender(processingContext)) {
            if (anObject instanceof ContentObject) {
                final ContentObject contentObject = (ContentObject) anObject;
                final ContentObject parent = contentObject.getParent(null);
                if (parent != null && ServicesRegistry.getInstance().getImportExportService().isPicker(parent)) {
                    return "";
                }
                if (contentObject.checkWriteAccess(getUser(), checkChilds, forceChilds)) {
                    return theEngine.renderLink(processingContext, anObject);
                }
            } else if (anObject instanceof ACLResourceInterface) {
                if (anObject instanceof JahiaField) {
                    if (ServicesRegistry.getInstance().getImportExportService().isPicker(((JahiaField) anObject).getContentField())) {
                        return "";
                    }
                }
                if (ACLResource.checkWriteAccess(null, (ACLResourceInterface) anObject, getUser())) {
                    if (anObject instanceof JahiaPage) {
                        if (ServicesRegistry.getInstance().getImportExportService().isPicker(((JahiaPage) anObject).getContentPage())) {
                            return "";
                        }
                    }
                    return theEngine.renderLink(processingContext, anObject);
                }
            } else if (anObject instanceof JahiaContainerList) {
                // this is mostly used to generate the add container URL when
                // a container list doesn't yet exist (created at the same time
                // as the first container).
                final JahiaContainerList containerList = (JahiaContainerList) anObject;
                if (containerList.getID() == 0) {
                    if (ServicesRegistry.getInstance().getImportExportService().isPicker(processingContext.getPage().getContentPage())) {
                        return "";
                    }
                } else {
                    if (ServicesRegistry.getInstance().getImportExportService().isPicker(containerList.getContentContainerList())) {
                        return "";
                    }
                }

                    // Add Container
                    if (containerList.getID() == 0 &&
                            processingContext.getPage().checkWriteAccess(getUser(), true) ||
                            containerList.getID() != 0 &&
                                    containerList.checkWriteAccess(getUser())) {
                        return theEngine.renderLink(processingContext, anObject);
                    }
            } else if (anObject instanceof ApplicationBean) {
                ApplicationBean bean = (ApplicationBean) anObject;
                if(JCRContentUtils.hasPermission(getUser(), Constants.JCR_WRITE_RIGHTS,bean.getID())) {
                    return theEngine.renderLink(processingContext, anObject);
                }
            }

        }
        return "";
    }

    /**
     * returns the current user name.
     * JB   25.04.2001
     */
    public String drawUsername() {
        return drawUsername(true);
    } // end drawUsername

    /**
     * @param allowUserAliasing if true, return the username of the aliased user when applicable ( in preview mode and
     *                          aliased user enabled )
     * @return
     */
    public String drawUsername(boolean allowUserAliasing) {
        if (processingContext != null) {
            if (!allowUserAliasing) {
                return getUser().getUsername();
            } else {
                JahiaUser user = AdvPreviewSettings.getAliasedUser(getUser());
                String s = getUser().getUsername();
                if (!s.equals(user.getUsername())) s = s + " ( " + user.getUsername() + " )";
                return s;
            }
        } else {
            return "";
        }
    } // end drawUsername

    /**
     * tests if Jahia is in edition mode
     * JB   25.04.2001
     */
    public boolean isEditMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.EDIT);
        } else {
            return false;
        }
    } // end isEditMode

    public boolean isNormalMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.NORMAL);
        } else {
            return false;
        }
    }

    public boolean isCompareMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.COMPARE);
        } else {
            return false;
        }
    }

    public boolean isPreviewMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.PREVIEW);
        } else {
            return false;
        }
    }

    /**
     * tests if the current user is logged
     * JB   25.04.2001
     */
    public boolean isLogged() {
        if (processingContext != null) {
            final String theUserName = getUser().getUsername();
            if (!theUserName.equals("guest")) {
                return true;
            }
        }
        return false;
    } // end isLogged


    /**
     * tests if a page belongs to the current page path
     * JB   25.04.2001
     */
    public boolean isPageInPath(final int destPageID)
            throws JahiaException {
        if (processingContext != null) {

            final Iterator<ContentPage> thePath = getContentPage().getContentPagePath(
                    processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), getUser());
            boolean foundTarget = false;
            while (thePath.hasNext()) {
                final ContentPage aPage = thePath.next();
                if (!foundTarget) {
                    foundTarget = (aPage.getID() == getPage().getID());
                }
                if (aPage.getID() == destPageID) {
                    return true;
                }
                if (foundTarget) {
                    break;
                }
            }
        }
        return false;
    } // end isPageInPath

    /**
     * isPageInPath
     * JB   25.04.2001
     */
    public boolean isPageInPath(final int destPageID, final int levels)
            throws JahiaException {
        if (processingContext != null) {
            final Iterator<ContentPage> thePath = getContentPage()
                    .getContentPagePath(levels, processingContext.getEntryLoadRequest(),
                            processingContext.getOperationMode(), getUser(),
                            JahiaPageService.PAGEPATH_SHOW_ALL);
            boolean foundTarget = false;
            while (thePath.hasNext()) {
                final ContentPage aPage = thePath.next();
                if (!foundTarget) {
                    foundTarget = (aPage.getID() == getPage().getID());
                }
                if (aPage.getID() == destPageID) {
                    return true;
                }
                if (foundTarget) {
                    break;
                }
            }
        }
        return false;
    } // end isPageInPath


    /**
     * tests if the browser is Netscape
     * JB   16.05.2001
     */
    public boolean isNS() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mozilla") != -1) {
                if (userAgent.indexOf("MSIE") == -1) {
                    return true;
                }
            }
        }
        return false;
    } // end isNS


    /**
     * tests if the browser is Netscape 4.x.
     * JB   16.05.2001
     */
    public boolean isNS4() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mozilla/4") != -1) {
                if (userAgent.indexOf("MSIE") == -1) {
                    return true;
                }
            }
        }
        return false;
    } // end isNS4


    /**
     * tests if the browser is Netscape 6.x.
     * JB   16.05.2001
     */
    public boolean isNS6() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mozilla/5") != -1) {
                return true;
            }
        }
        return false;
    } // end isNS6

    /**
     * tests if the browser is Internet Explorer
     * JB   16.05.2001
     */
    public boolean isIE() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE

    /**
     * tests if the browser is Internet Explorer
     * JB   16.05.2001
     */
    public static boolean isIe(final HttpServletRequest req) {
        final String userAgent;
        final Enumeration<?> userAgentValues = req.getHeaders("user-agent");
        if (userAgentValues.hasMoreElements()) {
            // we only take the first value.
            userAgent = (String) userAgentValues.nextElement();
        } else {
            userAgent = null;
        }

        if (userAgent != null) {
            if (userAgent.indexOf("MSIE") != -1) {
                return true;
            }
        }
        return false;
    } // end isIe


    /**
     * tests if the browser is Internet Explorer 4.x.
     * JB   16.05.2001
     */
    public boolean isIE4() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 4") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE4


    /**
     * tests if the browser is Internet Explorer 5.x.
     * JB   16.05.2001
     */
    public boolean isIE5() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 5") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE5


    /**
     * isIE6
     * JB   16.05.2001
     */
    public boolean isIE6() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 6") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE6

    public boolean isIE7() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 7") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE7

    /**
     * isOpera
     */
    public boolean isOpera() {
        String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            if (userAgent.indexOf("opera") != -1) {
                return true;
            }
        }
        return false;
    } // end isOpera

    /**
     * isWindow
     * JB   13.11.2001
     */
    public boolean isWindow() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Win") != -1) {
                return true;
            }
        }
        return false;
    } // end isWindow


    /**
     * isUnix
     * JB   13.11.2001
     */
    public boolean isUnix() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("X11") != -1) {
                return true;
            }
        }
        return false;
    } // end isUnix


    /**
     * isMac
     * JB   13.11.2001
     */
    public boolean isMac() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mac") != -1) {
                return true;
            }
        }
        return false;
    } // end isMac


    /**
     * checks if the current user has write access on the current page
     * JB   25.04.2001
     */
    public boolean checkWriteAccess() {
        return getPage().checkWriteAccess(getUser());
    } // end checkWriteAccess

    /**
     * Cuts and prepares a String for display at a specified length, by
     * appending "..." characters at the end and encoding the string for
     * HTML output (by replacing all non ISO-8859-1 characters with &#XX;
     * encoding).
     *
     * @param in  the String to cut and prepare for output.
     * @param len the length at which to cut it. If the string is shorter than
     *            the length then the string will be returned unmodified.
     * @return the cut string with "..." at the end if it was cut, and encoded
     *         for HTML output.
     */
    public static String glueTitle(String in, final int len) {
        if (in == null) {
            return null;
        }
        in = JahiaTools.html2text(in);
        if ((in.length()) > len && (len > 2)) {
            in = in.substring(0, len - 3) + "...";
        }
        return in;
    } // end glueTitle


    /**
     * Returns the page ID of the page that is at the specific level in the
     * page page of the current page. So if we have the following path :
     * page1 -> page2 -> page3 (current page)
     * <p/>
     * level 0 = -1 (too low, will always return -1)
     * level 1 = page1 (root page ID)
     * level 2 = page2
     * level 3 = page3
     * level 4 = -1 (doesn't exist)
     *
     * @param level the offset from the root page, specifying the number of
     *              levels to go down in the tree, 1-based.
     * @return an integer specifying the page ID of the desired level in the
     *         page path, or -1 if level was too big.
     * @throws JahiaException thrown if we have trouble retrieving the page's
     *                        path.
     */
    public int getLevelID(final int level)
            throws JahiaException {
        final Iterator<ContentPage> thePath = getContentPage().getContentPagePath(
                processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), getUser());
        int count_loop = 0;
        while (thePath.hasNext()) {
            final ContentPage aPage = (ContentPage) thePath.next();
            count_loop++;
            if (count_loop == (level)) {
                return aPage.getID();
            }
        }
        return -1;
    } // end getLevelID


    /**
     * Returns the depth of the current page.
     *
     * @return an integer specifying the depth in the page tree of the current
     *         page, or -1 in the case of an error.
     * @throws JahiaException thrown if we had trouble retrieving the page's
     *                        path.
     */
    public int getLevel() throws JahiaException {
        final Iterator<ContentPage> thePath = getContentPage().getContentPagePath(
                processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), getUser());
        int count_loop = 0;
        while (thePath.hasNext()) {
            final ContentPage aPage = (ContentPage) thePath.next();
            count_loop++;
            if (aPage.getID() == getPage().getID()) {
                return count_loop;
            }
        }
        return -1;
    } // end getLevel


    /**
     * getHomePage, return the site's home page
     *
     * @deprecated, use getContentHomePage
     */
    public JahiaPage getHomePage() throws JahiaException {
        if (processingContext.getSite() == null)
            return null;

        final JahiaPageService pageService = ServicesRegistry.getInstance().
                getJahiaPageService();

        // finds origin page
        return pageService.lookupPage(processingContext.getSite().getHomePageID(),
                processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), processingContext.getUser(), false);

    } // end getHomePage

    /**
     * getHomePage, return the site's home page as ContentPage
     */
    public ContentPage getContentHomePage() throws JahiaException {
        if (processingContext.getSite() == null)
            return null;
        return ContentPage.getPage(processingContext.getSite().getHomePageID());

    } // end getHomePage

    public String drawUpdateAppplicationUrl(ApplicationBean applicationBean) throws JahiaException {
        return drawUrlCheckWriteAccess("application", applicationBean);
    }

    /**
     * @param contentContainer
     * @return
     * @throws JahiaException
     */
    public String drawRestoreContainerUrl(final ContentContainer contentContainer)
            throws JahiaException {
        return drawUrlCheckWriteAccess(RestoreLiveContainer_Engine.ENGINE_NAME, contentContainer);
    }

    

    private JahiaUser getUser() {
        return processingContext != null ? processingContext.getUser() : null;
    }

    private JahiaPage getPage() {
        return processingContext != null ? processingContext.getPage() : null;
    }

    private ContentPage getContentPage() {
        return processingContext != null ? processingContext.getContentPage() : null;
    }
}