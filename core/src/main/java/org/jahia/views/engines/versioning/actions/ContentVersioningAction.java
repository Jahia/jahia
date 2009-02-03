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

package org.jahia.views.engines.versioning.actions;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JTree;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.jahia.bin.AdminAction;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.ContentTreeRevisionsVisitor;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.PageRevisionEntrySet;
import org.jahia.services.version.RestoreVersionNodeTestResult;
import org.jahia.services.version.RestoreVersionStateModificationContext;
import org.jahia.services.version.RestoreVersionTestResults;
import org.jahia.services.version.RevisionEntrySet;
import org.jahia.services.version.RevisionsTreeTools;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.version.UndoStagingContentTreeVisitor;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.views.engines.JahiaEngineButtonsHelper;
import org.jahia.views.engines.JahiaEngineCommonData;
import org.jahia.views.engines.JahiaEngineViewHelper;
import org.jahia.views.engines.versioning.ContainerVersioningViewHelper;
import org.jahia.views.engines.versioning.ContentVersioningViewHelper;
import org.jahia.views.engines.versioning.pages.PagesVersioningViewHelper;
import org.jahia.views.engines.versioning.revisionsdetail.actions.RevisionEntrySetDetailAction;

/**
 * <p>Title: Content Versioning Dispatch Action</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Jahia</p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class ContentVersioningAction extends AdminAction {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentVersioningAction.class);

    private static final String ENGINE_TITLE = "Content Versioning";

    public static final String SESSION_VERSIONING_LOCK_LIST = "org.jahia.views.engines.versioning.pages.Locks";

    /**
     * Init Engine Data
     *
     * @param request
     */
    protected void init(ActionMapping mapping, HttpServletRequest request)
            throws JahiaException {

        try {

            // test whether the data already exists in session or should be created
            String engineView = request.getParameter("engineview");
            String reloaded = request.getParameter("reloaded");

            Boolean allowReadAccess = (Boolean) request.getAttribute("versioningAllowReadAccess");
            if (allowReadAccess == null) {
                allowReadAccess = Boolean.FALSE;
            }
            // engine view Helper
            ContentVersioningViewHelper engineViewHelper = null;
            if (engineView != null && !"yes".equals(reloaded)) {
                // try to retrieve engine data from session
                engineViewHelper =
                        (ContentVersioningViewHelper) request.getSession()
                                .getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            }
            // engine common data
            JahiaEngineCommonData engineCommonData =
                    new JahiaEngineCommonData(request);
            engineCommonData.setEngineTitle(ContentVersioningAction.ENGINE_TITLE);

            // Prepare engine buttons helper
            JahiaEngineButtonsHelper jahiaEngineButtonsHelper =
                    new JahiaEngineButtonsHelper();
            jahiaEngineButtonsHelper.addCloseButton();

            jahiaEngineButtonsHelper.addAuthoringButton();
            if (engineCommonData.getParamBean().getPage()
                    .checkAdminAccess(engineCommonData.getParamBean().getUser())) {
                jahiaEngineButtonsHelper.addRightsButton();
                jahiaEngineButtonsHelper.addLogsButton();
            }

            if (engineCommonData.getParamBean().getPage()
                    .checkAdminAccess(engineCommonData.getParamBean().getUser()) ||
                    engineCommonData.getParamBean().getPage()
                            .checkWriteAccess(engineCommonData.getParamBean().getUser())) {
                jahiaEngineButtonsHelper.addVersioningButton();
            }

            request.setAttribute(JahiaEngineButtonsHelper.JAHIA_ENGINE_BUTTONS_HELPER,
                    jahiaEngineButtonsHelper);

            final ContentObject contentObject;

            if (engineViewHelper == null) {
                // Prepage a new engine view helper
                String objectKey;
                objectKey = request.getParameter("objectKey");
                contentObject = ContentObject.getContentObjectInstance(ObjectKey.getInstance(objectKey));
                CalendarHandler cal = this.getCalHandler("restoreDateCalendar", 0,
                        engineCommonData.getParamBean());
                engineViewHelper = ContentVersioningViewHelper.getInstance(contentObject, cal);
                cal = this.getCalHandler("fromRevisionDateCalendar", 0,
                        engineCommonData.getParamBean());
                engineViewHelper.setFromRevisionDateCalendar(cal);
                cal = this.getCalHandler("toRevisionDateCalendar", 0,
                        engineCommonData.getParamBean());
                engineViewHelper.setToRevisionDateCalendar(cal);

                // store engine data in session
                request.getSession().setAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER,
                        engineViewHelper);
            } else {
                contentObject = engineViewHelper.getContentObject();
            }

            // check permission
            ContentVersioningAction.logger.info("Logged in User :" + engineCommonData.getParamBean().getUser().getUsername());
            if (!contentObject.checkWriteAccess(engineCommonData.getParamBean().getUser())) {
                if (!allowReadAccess.booleanValue()) {
                    throw new JahiaForbiddenAccessException();
                } else {
                    if (!contentObject.checkReadAccess(engineCommonData.getParamBean().getUser())) {
                        throw new JahiaForbiddenAccessException();
                    }
                }
            }

            request.setAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA, engineCommonData);

            request.setAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER, engineViewHelper);

            // JSP attributes
            String contentTitle = null;
            if (contentObject instanceof ContentPage) {
                contentTitle = contentObject.getDisplayName(engineCommonData.getParamBean());
            } else if (contentObject instanceof ContentContainer) {
                JahiaContainerDefinition containerDefinition = (JahiaContainerDefinition) JahiaContainerDefinition
                        .getContentDefinitionInstance(contentObject.getDefinitionKey(EntryLoadRequest.STAGED));
                contentTitle = containerDefinition.getDisplayName(engineCommonData.getParamBean());
            } else if (contentObject instanceof ContentContainerList) {
                JahiaContainerDefinition containerDefinition = (JahiaContainerDefinition) JahiaContainerDefinition
                        .getContentDefinitionInstance(contentObject.getDefinitionKey(EntryLoadRequest.STAGED));
                contentTitle = containerDefinition.getDisplayName(engineCommonData.getParamBean());
            }
            if (contentTitle == null) {
                contentTitle = "No title for content object [" + engineViewHelper.getContentObject().getObjectKey()
                        + "] in lang : " + engineCommonData.getParamBean()
                        .getEntryLoadRequest().getFirstLocale(true).getDisplayName();
            }
            request.setAttribute("contentVersioning.contentTitle", contentTitle);
            request.setAttribute("contentVersioning.objectKey", String.valueOf(contentObject.getObjectKey().toString()));

            // Prepare the Action URL for this Action Dispatcher
            Properties props = new Properties();
            String actionURL = ContentVersioningAction.composeActionURL(contentObject,
                    engineCommonData.getParamBean(), mapping.getPath(), props, null);
            request.setAttribute("ContentVersioning.ActionURL", actionURL);

            // BACKWARD COMPATIBILITY WITH OLD ENGINE
            // Shared engine map for keeping compatibility with old engine system
            Map engineMap = (Map) request
                    .getSession().getAttribute("jahia_session_engineMap");
            if (engineMap != null) {
                engineMap.put("screen", "versioning");
                request.setAttribute("jahia_session_engineMap", engineMap);
                // use the engineURL to keep compatibility
                engineCommonData.setEngineURL((String) engineMap.get("engineUrl"));

                final EngineLanguageHelper elh = (EngineLanguageHelper)
                        engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
                elh.update(engineCommonData.getParamBean());
                if (elh != null &&
                        !engineViewHelper.getLanguagesToRestore().contains(elh.getCurrentLocale())) {
                    engineViewHelper.getLanguagesToRestore().add(elh.getCurrentLocale().clone());
                }
            }

        } catch (Exception t) {
            ContentVersioningAction.logger.debug("Error occurred", t);
            throw new JahiaException("Exception occured initializing engine's objects",
                    "Exception occured initializing engine's objects",
                    JahiaException.ENGINE_ERROR,
                    JahiaException.ENGINE_ERROR, t);
        }
    }

    /**
     * Display the operation choices view.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward showOperationChoices(ActionMapping mapping,
                                              ActionForm form,
                                              HttpServletRequest request,
                                              HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = mapping.findForward("operationChoices");
        ActionErrors errors = new ActionErrors();
        JahiaEngineCommonData engineCommonData;
        try {
            init(mapping, request);
            getRevisionsListFormData(mapping, form, request, response);
            getSiteMapFormData(mapping, form, request, response);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);

            // before we go any further, let's try to acquire all the locks
            // on the page sub tree.
            ActionMessages actionMessages = new ActionMessages();
            boolean lockingSuccessful = acquireTreeLocks(engineCommonData.getParamBean(), actionMessages);
            if (!lockingSuccessful) {
                forward = mapping.findForward("pageTreeLocked");
                saveMessages(request, actionMessages);
            }

        } catch (Exception t) {
            logger.error(t.getMessage(), t);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error preparing Operation Choices view"));
        }
        // set engine screen
        request.setAttribute("engineView", "operationChoices");
        request.setAttribute("DisableButtons", "DisableButtons");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Display the main screen containing the revisions list
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward showRevisionsList(ActionMapping mapping,
                                           ActionForm form,
                                           HttpServletRequest request,
                                           HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = mapping.getInputForward();
        ActionErrors errors = new ActionErrors();
        ContentVersioningViewHelper versViewHelper = null;
        JahiaEngineCommonData engineCommonData;
        try {
            init(mapping, request);
            versViewHelper =
                    (ContentVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            if (versViewHelper.isRestoringContainer() || versViewHelper.isRestoringContainerList()) {
                // before we go any further, let's try to acquire all the locks
                // on the page sub tree.
                ActionMessages messages = new ActionMessages();
                boolean lockingSuccessful = acquireTreeLocks(engineCommonData.getParamBean(), messages);
                if (!lockingSuccessful) {
                    forward = mapping.findForward("pageTreeLocked");
                    saveMessages(request, messages);
                } else {
                    getRevisionsListFormData(mapping, form, request, response);
                    loadRevisions(mapping, form, request, response);
                }
            } else {
                getRevisionsListFormData(mapping, form, request, response);
                loadRevisions(mapping, form, request, response);
            }
        } catch (Exception t) {
            logger.error(t.getMessage(), t);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Exception occured when processing content object ["
                            + versViewHelper.getContentObject().getObjectKey() + "]"));
        }
        // set engine screen
        request.setAttribute("engineView", "revisionsList");
        request.setAttribute("DisableButtons", "DisableButtons");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void loadRevisions(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
            throws IOException, ServletException, JahiaException {

        ContentVersioningViewHelper versViewHelper = (ContentVersioningViewHelper)
                request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
        JahiaEngineCommonData engineCommonData = (JahiaEngineCommonData)
                request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);

        long toDate = versViewHelper.getToRevisionDateCalendar().getDateLong().longValue();
        if (toDate == 0) {
            toDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        }
        toDate += versViewHelper.getToRevisionDateCalendar().getTimeZoneOffSet().longValue()
                + versViewHelper.getToRevisionDateCalendar().getServerClientTimeDiff().longValue();

        EntryLoadRequest loadRequest =
                new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                        (int) (toDate / 1000),
                        engineCommonData.getParamBean().getEntryLoadRequest().getLocales());

        // prepare Revisions List
        ContentTreeRevisionsVisitor revisionsVisitor =
                versViewHelper.getContentTreeRevisionsVisitor(
                        engineCommonData.getParamBean().getUser(),
                        loadRequest, ProcessingContext.EDIT);

        revisionsVisitor.setRevisionEntryType(versViewHelper.getContentOrMetadataRevisions());
        revisionsVisitor.setWithDeletedContent(true);
        if (versViewHelper.isRestoringPage()) {
            int pageLevel = ((PagesVersioningViewHelper) versViewHelper).getPageLevel().intValue();
            if (pageLevel > 0) {
                pageLevel -= 1;
            }
            revisionsVisitor.setDescendingPageLevel(pageLevel);
        } else if (versViewHelper.isRestoringContainer()) {
            revisionsVisitor.setWithStagingRevisions(true);
        }
        long fromDate = versViewHelper.getFromRevisionDateCalendar().getDateLong().longValue();
        if (fromDate > 0) {
            fromDate += versViewHelper.getFromRevisionDateCalendar().getTimeZoneOffSet().longValue()
                    + versViewHelper.getFromRevisionDateCalendar().getServerClientTimeDiff().longValue();
        }
        revisionsVisitor.setFromRevisionDate(fromDate);
        revisionsVisitor.setToRevisionDate(toDate);

        revisionsVisitor.loadRevisions(false);
        revisionsVisitor.sortRevisions(
                engineCommonData.getParamBean().getLocale().toString(),
                versViewHelper.getSortAttribute(),
                versViewHelper.getSortOrder(), false);

        List revisions = revisionsVisitor.getRevisions();
        List filteredRevisions = new ArrayList();
        int size = revisions.size();
        // remove deleted revisions, we don't want to restore at deleted revision
        RevisionEntrySet revEntrySet;
        if ((versViewHelper.isRestoringPage() || versViewHelper.isRestoringContainer())) {
            for (int i = 0; i < size; i++) {
                revEntrySet = (RevisionEntrySet) revisions.get(i);
                if ((revEntrySet.getWorkflowState() !=
                        EntryLoadRequest.DELETED_WORKFLOW_STATE)) {
                    filteredRevisions.add(revEntrySet);
                }
            }
        } else {
            filteredRevisions = revisions;
        }
        if (filteredRevisions == null) {
            filteredRevisions = new ArrayList();
        }
        request.setAttribute("revisions", filteredRevisions);
    }

    /**
     * Display the site map with the selected page to restore.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward showSiteMap(ActionMapping mapping,
                                     ActionForm form,
                                     HttpServletRequest request,
                                     HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = mapping.findForward("sitemap");
        ActionErrors errors = new ActionErrors();
        PagesVersioningViewHelper pagesVersViewHelper;
        JahiaEngineCommonData engineCommonData;
        try {
            init(mapping, request);
            getRevisionsListFormData(mapping, form, request, response);
            getSiteMapFormData(mapping, form, request, response);
            pagesVersViewHelper = (PagesVersioningViewHelper)
                    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            pagesVersViewHelper.loadSiteMapViewHelper(
                    engineCommonData.getParamBean().getUser(), request);
            pagesVersViewHelper.getRestoreDateCalendar().update(engineCommonData.getParamBean());

            // retrieve the requested operation
            String paramVal = request.getParameter("operationType");
            ContentVersioningAction.logger.debug("showSiteMap: " + paramVal);
            if (paramVal != null) {
                try {
                    pagesVersViewHelper.setOperationType(Integer.parseInt(paramVal));
                } catch (Exception t) {
                    ContentVersioningAction.logger.error("Unable to set the operation", t);
                }
            }

            // select the current page if none selected and operation type = restore archived
            if (pagesVersViewHelper.getSelectedPages().size() == 0 &&
                    pagesVersViewHelper.getOperationType() == ContentVersioningViewHelper.RESTORE_ARCHIVE_CONTENT_OPERATION)
            {
                pagesVersViewHelper.getSelectedPages().add(pagesVersViewHelper.getPage().getObjectKey());
            }

            String siteMapParam = request.getParameter("sitemap");
            if (siteMapParam != null) {
                ServicesRegistry.getInstance().getJahiaSiteMapService()
                        .invokeTreeSiteMapViewHelperMethod(engineCommonData.getParamBean().getUser(),
                                pagesVersViewHelper.getPage(), engineCommonData.getParamBean().getSessionID(),
                                ContentPage.ARCHIVED_PAGE_INFOS,
                                null, siteMapParam, true, null, null);
            }

        } catch (Exception t) {
            logger.error(t.getMessage(), t);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error preparing sitemap view"));
        }
        // set engine screen
        request.setAttribute("engineView", "sitemap");
        request.setAttribute("DisableButtons", "DisableButtons");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Display the confirm restore view.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward showConfirmRestore(ActionMapping mapping,
                                            ActionForm form,
                                            HttpServletRequest request,
                                            HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = null;
        ActionErrors errors = new ActionErrors();
        ContentVersioningViewHelper versViewHelper;
        JahiaEngineCommonData engineCommonData;
        try {
            init(mapping, request);
            versViewHelper = (ContentVersioningViewHelper)
                    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            boolean pageVersioning = versViewHelper.isRestoringPage();
            if (pageVersioning) {
                getSiteMapFormData(mapping, form, request, response);
                // store pages to restore
                ((PagesVersioningViewHelper) versViewHelper)
                        .setPagesToRestore(getPagesToRestore(request));
            } else if (versViewHelper.isRestoringContainer()) {
                versViewHelper.setOperationType(ContentVersioningViewHelper.RESTORE_ARCHIVE_CONTENT_OPERATION);
                getRevisionsListFormData(mapping, form, request, response);
            }

            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);

            ContentVersioningAction.logger.debug("showConfirmRestore: " + versViewHelper.getOperationType());

            switch (versViewHelper.getOperationType()) {
                case ContentVersioningViewHelper.UNDO_STAGING_OPERATION : {
                    forward = mapping.findForward("confirmUndoStaging");
                    break;
                }
                case ContentVersioningViewHelper.RESTORE_ARCHIVE_CONTENT_OPERATION : {
                    forward = mapping.findForward("confirmRestore");
                    break;
                }
                case ContentVersioningViewHelper.UNDELETE_OPERATION : {
                    forward = mapping.findForward("confirmUndelete");
                    break;
                }
            }

            if (pageVersioning && ((PagesVersioningViewHelper) versViewHelper).getPagesToRestore().size() == 0) {
                forward = this.showSiteMap(mapping, form, request, response);
                // set engine screen
                request.setAttribute("engineView", "sitemap");
                return continueForward(mapping, request, errors, forward);
            } else
            if (versViewHelper.getOperationType() == ContentVersioningViewHelper.RESTORE_ARCHIVE_CONTENT_OPERATION
                    && versViewHelper.getRevisionEntrySet() == null) {
                forward = this.showRevisionsList(mapping, form, request, response);
                request.setAttribute("engineView", "revisionsList");
                return continueForward(mapping, request, errors, forward);
            }
            // Prepare engine buttons helper
            JahiaEngineButtonsHelper jahiaEngineButtonsHelper =
                    new JahiaEngineButtonsHelper();

            jahiaEngineButtonsHelper.addOkButton();
            jahiaEngineButtonsHelper.addApplyButton();
            jahiaEngineButtonsHelper.addCloseButton();

            jahiaEngineButtonsHelper.addAuthoringButton();
            if (engineCommonData.getParamBean().getPage()
                    .checkAdminAccess(engineCommonData.getParamBean().getUser())) {
                jahiaEngineButtonsHelper.addRightsButton();
                jahiaEngineButtonsHelper.addVersioningButton();
                jahiaEngineButtonsHelper.addLogsButton();
            }

            request.setAttribute(JahiaEngineButtonsHelper.JAHIA_ENGINE_BUTTONS_HELPER,
                    jahiaEngineButtonsHelper);

            // JSP output
            Date d = new Date(
                    versViewHelper.getRestoreDateCalendar().getDateLong().longValue()
                            + versViewHelper.getRestoreDateCalendar().getTimeZoneOffSet().longValue()
                            + versViewHelper.getRestoreDateCalendar().getServerClientTimeDiff().longValue());
            request.setAttribute("contentVersioning.full_restore_date",
                    DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM,
                            engineCommonData.getParamBean().getLocale()).format(d));
            if (pageVersioning) {
                PagesVersioningViewHelper pagesVersViewHelper = (PagesVersioningViewHelper) versViewHelper;
                request.setAttribute("contentVersioning.nb_pages_to_restore",
                        new Integer(pagesVersViewHelper.getPagesToRestore().size() / 2));
                request.setAttribute("contentVersioning.restore_exact",
                        pagesVersViewHelper.exactRestore() ? "yes" : "no");
            }
        } catch (Exception t) {
            logger.error(t.getMessage(), t);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error preparing confirm restore view"));
        }
        // set engine screen
        request.setAttribute("engineView", "confirmRestore");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Display the confirm undo staging container view.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward showConfirmContainerUndoStaging(ActionMapping mapping,
                                                         ActionForm form,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = null;
        ActionErrors errors = new ActionErrors();
        ContentVersioningViewHelper versViewHelper;
        JahiaEngineCommonData engineCommonData;
        try {
            init(mapping, request);
            versViewHelper = (ContentVersioningViewHelper)
                    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            versViewHelper.setOperationType(ContentVersioningViewHelper.UNDO_STAGING_OPERATION);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            forward = mapping.findForward("confirmUndoStaging");
            // Prepare engine buttons helper
            JahiaEngineButtonsHelper jahiaEngineButtonsHelper =
                    new JahiaEngineButtonsHelper();

            jahiaEngineButtonsHelper.addOkButton();
            jahiaEngineButtonsHelper.addApplyButton();
            jahiaEngineButtonsHelper.addCloseButton();

            jahiaEngineButtonsHelper.addAuthoringButton();
            if (engineCommonData.getParamBean().getPage()
                    .checkAdminAccess(engineCommonData.getParamBean().getUser())) {
                jahiaEngineButtonsHelper.addRightsButton();
                jahiaEngineButtonsHelper.addVersioningButton();
                jahiaEngineButtonsHelper.addLogsButton();
            }
            request.setAttribute(JahiaEngineButtonsHelper.JAHIA_ENGINE_BUTTONS_HELPER,
                    jahiaEngineButtonsHelper);
        } catch (Exception t) {
            logger.error(t.getMessage(), t);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error preparing confirm undo view"));
        }
        // set engine screen
        request.setAttribute("engineView", "confirmRestore");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Restore .
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward restoreApply(ActionMapping mapping,
                                      ActionForm form,
                                      HttpServletRequest request,
                                      HttpServletResponse response)
            throws IOException, ServletException {
        return restoreSave(mapping, form, request, response);
    }

    /**
     * Restore .
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward restoreSave(ActionMapping mapping,
                                     ActionForm form,
                                     HttpServletRequest request,
                                     HttpServletResponse response)
            throws IOException, ServletException {

        ContentVersioningViewHelper versViewHelper;
        ActionErrors errors = new ActionErrors();
        try {
            init(mapping, request);
            versViewHelper = (ContentVersioningViewHelper)
                    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

            versViewHelper.getLanguagesToRestore().clear();
            String[] languagesToRestore = request.getParameterValues("languageToRestore");
            if (languagesToRestore != null) {
                Locale loc = null;
                for (int i = 0; i < languagesToRestore.length; i++) {
                    loc = LanguageCodeConverters.languageCodeToLocale(languagesToRestore[i]);
                    if (loc != null) {
                        versViewHelper.getLanguagesToRestore().add(loc);
                    }
                }
            }

            String restoreMode = request.getParameter("restoreMode");
            if (restoreMode != null){
                try {
                    versViewHelper.setRestoreMode(Integer.parseInt(restoreMode));
                } catch (Exception t){
                }
            }

            if (versViewHelper.isRestoringPage()) {
                return restorePageSave(mapping, form, request, response);
            } else if (versViewHelper.isRestoringContainer()) {
                return restoreContainerSave(mapping, form, request, response);
            } else if (versViewHelper.isRestoringContainerList()) {
                return restoreContainerListSave(mapping, form, request, response);
            }
        } catch (Exception t) {
            handleException(t, request, response);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error processing restore save action"));
        }
        return null;
    }

    /**
     * Restore.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    protected ActionForward restorePageSave(ActionMapping mapping,
                                            ActionForm form,
                                            HttpServletRequest request,
                                            HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = mapping.findForward("sitemap");
        ActionErrors errors = new ActionErrors();
        PagesVersioningViewHelper pagesVersViewHelper;
        JahiaEngineCommonData engineCommonData;
        try {
            pagesVersViewHelper = (PagesVersioningViewHelper)
                    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            if (pagesVersViewHelper.getPagesToRestore().size() == 0) {
                forward = this.showSiteMap(mapping, form, request, response);
            } else {
                engineCommonData = (JahiaEngineCommonData)
                        request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
                boolean activeRestoredPages =
                        "yes".equals(request.getParameter("activate_pages_after_restore"));
                boolean applyPageMoveWhenRestore =
                        "yes".equals(request.getParameter("apply_page_move_when_restore"));

                pagesVersViewHelper.setApplyPageMoveWhenRestore(applyPageMoveWhenRestore);

                List pagesToRestoreArray = pagesVersViewHelper.getPagesToRestore();
                Map pagesToRestore = new HashMap();
                int size = pagesToRestoreArray.size();
                for (int i = 0; i < size - 1; i += 2) {
                    String lang = (String) pagesToRestoreArray.get(i);
                    Integer pageID = (Integer) pagesToRestoreArray.get(i + 1);
                    List langs = (List) pagesToRestore.get(pageID);
                    if (langs == null) {
                        langs = new ArrayList();
                    }
                    langs.add(lang);
                    pagesToRestore.put(pageID, langs);
                }

                switch (pagesVersViewHelper.getOperationType()) {
                    case ContentVersioningViewHelper.UNDO_STAGING_OPERATION : {
                        this.pagesUndoStaging(engineCommonData.getParamBean(),
                                request, pagesToRestore);
                        break;
                    }
                    case ContentVersioningViewHelper.RESTORE_ARCHIVE_CONTENT_OPERATION : {
                        this.pagesRestore(engineCommonData.getParamBean(), request,
                                pagesToRestore,
                                engineCommonData.getParamBean().getUser(),
                                pagesVersViewHelper.getRestoreDateCalendar().getDateLong().longValue(),
                                pagesVersViewHelper.exactRestore(),
                                activeRestoredPages);
                        break;
                    }
                    case ContentVersioningViewHelper.UNDELETE_OPERATION :
                        this.pagesRestore(engineCommonData.getParamBean(), request,
                                pagesToRestore,
                                engineCommonData.getParamBean().getUser(),
                                pagesVersViewHelper.getRestoreDateCalendar().getDateLong().longValue(),
                                pagesVersViewHelper.exactRestore(),
                                activeRestoredPages);
                        break;

                }

                // reload revisions
                if (pagesVersViewHelper.getContentTreeRevisionsVisitor() != null) {
                    pagesVersViewHelper.getContentTreeRevisionsVisitor().loadRevisions(true);
                }

                String method = request.getParameter(mapping.getParameter());
                if (method.equals("restoreSave") || method.equals("restoreApply")) {
                    releaseTreeLocks(engineCommonData.getParamBean());
                    request.setAttribute("engines.close.refresh-opener", "yes");
                    request.setAttribute("engines.close.opener-url-params", "&reloaded=yes");
                    forward = mapping.findForward("EnginesCloseAnyPopup");
                    /*
                    } else if (method.equals("restoreApply")) {

                        Properties params = new Properties();
                        params.put("method", "showOperationChoices");
                        params.put("engineview", "restore");
                        String requestURL = ContentVersioningAction.composeActionURL(pagesVersViewHelper.getContentObject(),
                                engineCommonData.getParamBean(), mapping.getPath(), params, null);
                        request.setAttribute("engines.apply.new-url", requestURL);
                        request.setAttribute("engines.apply.refresh-opener", "yes");
                        request.setAttribute("engines.apply.opener-url-params", "&reloaded=yes");
                        forward = mapping.findForward("EnginesApplyAnyPopup");
                    */
                } else {
                    forward = this.showRevisionsList(mapping, form, request, response);
                }
            }
        } catch (Exception t) {
            handleException(t, request, response);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error processing page restore"));
        } finally {
            ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();
            ServicesRegistry.getInstance().getCacheService().syncClusterNow();
            JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();
        }
        // set engine screen
        request.setAttribute("engineView", "sitemap");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Restore.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    protected ActionForward restoreContainerSave(ActionMapping mapping,
                                                 ActionForm form,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = mapping.findForward("revisionDetails");
        ActionErrors errors = new ActionErrors();
        ContentVersioningViewHelper versViewHelper;
        JahiaEngineCommonData engineCommonData;
        try {
            versViewHelper = (ContentVersioningViewHelper)
                    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            switch (versViewHelper.getOperationType()) {
                case ContentVersioningViewHelper.UNDO_STAGING_OPERATION : {
                    this.containerUndoStaging(engineCommonData.getParamBean(),
                            request);
                    break;
                }
                case ContentVersioningViewHelper.RESTORE_ARCHIVE_CONTENT_OPERATION : {
                    this.containerRestore(engineCommonData.getParamBean(), request);
                    break;
                }
            }

            // reload revisions
            if (versViewHelper.getContentTreeRevisionsVisitor() != null) {
                versViewHelper.getContentTreeRevisionsVisitor().loadRevisions(true);
            }
            String method = request.getParameter(mapping.getParameter());
            if (method.equals("restoreSave")) {
                releaseTreeLocks(engineCommonData.getParamBean());
                request.setAttribute("engines.close.refresh-opener", "yes");
                request.setAttribute("engines.close.opener-url-params", "&reloaded=yes");
                forward = mapping.findForward("EnginesCloseAnyPopup");
            } else if (method.equals("restoreApply")) {
                Properties params = new Properties();
                params.put("method", "showRevisionsList");
                params.put("engineview", "restore");
                String requestURL = ContentVersioningAction.composeActionURL(versViewHelper.getContentObject(),
                        engineCommonData.getParamBean(), mapping.getPath(), params, null);
                request.setAttribute("engines.apply.new-url", requestURL);
                request.setAttribute("engines.apply.refresh-opener", "yes");
                request.setAttribute("engines.apply.opener-url-params", "&reloaded=yes");
                forward = mapping.findForward("EnginesApplyAnyPopup");
            } else {
                forward = this.showRevisionsList(mapping, form, request, response);
            }
        } catch (Exception t) {
            handleException(t, request, response);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error processing container restore"));
        } finally {
            ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();
            ServicesRegistry.getInstance().getCacheService().syncClusterNow();
            JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();
        }
        // set engine screen
        request.setAttribute("engineView", "sitemap");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Must be overriden for container list restore version
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    protected ActionForward restoreContainerListSave(ActionMapping mapping,
                                                     ActionForm form,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response)
            throws IOException, ServletException {
        return null;
    }

    /**
     * Forward to revision detail.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward revisionsDetail(ActionMapping mapping,
                                         ActionForm form,
                                         HttpServletRequest request,
                                         HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = mapping.findForward("revisionsDetail");
        ActionErrors errors = new ActionErrors();
        PagesVersioningViewHelper pagesVersViewHelper = null;
        try {
            init(mapping, request);
            pagesVersViewHelper = (PagesVersioningViewHelper)
                    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);            
            String revisionEntryParam = request.getParameter("revisionEntrySet");
            RevisionEntrySet revisionEntrySet = getPageRevisionEntrySet(revisionEntryParam);

            JTree tree = null;
            ContentTreeRevisionsVisitor revisionsVisitor =
                    pagesVersViewHelper.getContentTreeRevisionsVisitor();
            List revisions = revisionsVisitor.getRevisions();
            if (revisions.size() > 0) {
                int index = revisions.indexOf(revisionEntrySet);
                if (index != -1) {
                    revisionEntrySet = (RevisionEntrySet) revisions.get(index);
                    tree = RevisionsTreeTools.getTreeOfPageRevisions(
                            (PageRevisionEntrySet) revisionEntrySet,
                            revisionsVisitor.getUser(),
                            revisionsVisitor.getEntryLoadRequest(),
                            revisionsVisitor.getOperationMode());
                }
            }

            // For forwarded action
            request.setAttribute(RevisionEntrySetDetailAction.REVISIONS_TREE,
                    tree);
            request.setAttribute(RevisionEntrySetDetailAction.REVISIONENTRYSET,
                    revisionEntrySet);

        } catch (Exception t) {
            handleException(t, request, response);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Exception processing show restore version test result"));
        }
        request.setAttribute("engineView", "revisionsDetail");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Closes the window, liberating all the locked resources.
     *
     * @param mapping  ActionMapping
     * @param form     ActionForm
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return ActionForward
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward close(ActionMapping mapping,
                               ActionForm form,
                               HttpServletRequest request,
                               HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = mapping.findForward("close");
        ActionErrors errors = new ActionErrors();
        ContentVersioningViewHelper versViewHelper = null;
        JahiaEngineCommonData engineCommonData = null;
        try {
            init(mapping, request);
            getRevisionsListFormData(mapping, form, request, response);
            getSiteMapFormData(mapping, form, request, response);
            versViewHelper = (ContentVersioningViewHelper)
                    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            if (versViewHelper.isRestoringPage()) {
                releaseTreeLocks(engineCommonData.getParamBean());
            }
        } catch (Exception t) {
            handleException(t, request, response);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error preparing Operation Choices view"));
        } finally {
            if (versViewHelper != null && versViewHelper.isRestoringPage()) {
                try {
                    releaseTreeLocks(engineCommonData.getParamBean());
                } catch (Exception t) {
                }
            }
            try {
                releaseActionLock(engineCommonData.getParamBean());
            } catch (Exception t) {
            }
        }
        // set engine screen
        request.setAttribute("engineView", "close");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Closes the window, liberating all the locked resources.
     *
     * @param mapping  ActionMapping
     * @param form     ActionForm
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return ActionForward
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward cancel(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                HttpServletResponse response)
            throws IOException, ServletException {
        ActionForward forward = mapping.findForward("close");
        ActionErrors errors = new ActionErrors();
        JahiaEngineCommonData engineCommonData = null;
        try {
            init(mapping, request);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
        } catch (Exception t) {
            handleException(t, request, response);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error preparing Operation Choices view"));
        } finally {
            try {
                releaseTreeLocks(engineCommonData.getParamBean());
                // this is required in the case that we never locked the tree
                releaseActionLock(engineCommonData.getParamBean());
            } catch (Exception t) {
                ContentVersioningAction.logger.debug("Fail releasing locks from versioning engine", t);
            }
        }
        // set engine screen
        request.setAttribute("engineView", "close");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * @param mapping
     * @param form
     * @param request
     * @param response
     */
    protected void getRevisionsListFormData(ActionMapping mapping,
                                            ActionForm form,
                                            HttpServletRequest request,
                                            HttpServletResponse response)
            throws JahiaSessionExpirationException {

        ContentVersioningViewHelper versViewHelper =
                (ContentVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
        JahiaEngineCommonData engineCommonData = (JahiaEngineCommonData)
                request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);

        versViewHelper.getFromRevisionDateCalendar().update(engineCommonData.getParamBean());
        versViewHelper.getToRevisionDateCalendar().update(engineCommonData.getParamBean());

        if (versViewHelper.isRestoringPage()) {
            // Choosed Revision entry
            String revisionEntryParam = request.getParameter("revisionEntrySet");
            String useRevisionEntryParam = request.getParameter("useRevisionEntry");
            RevisionEntrySet revisionEntrySet = getPageRevisionEntrySet(revisionEntryParam);
            if (revisionEntrySet != null) {
                versViewHelper.setRevisionEntrySet(revisionEntrySet);
                if (useRevisionEntryParam.equals("yes")) {
                    versViewHelper.getRestoreDateCalendar().setDateLong(new Long(revisionEntrySet.getVersionID() * 1000L));
                }
            }
        } else if (versViewHelper.isRestoringContainer()) {
            String revisionEntryParam = request.getParameter("revisionEntrySetToUse");
            RevisionEntrySet revisionEntrySet = null;
            if (versViewHelper.getContentTreeRevisionsVisitor() != null) {
                revisionEntrySet = getContainerRevisionEntrySet(revisionEntryParam,
                        versViewHelper.getContentTreeRevisionsVisitor().getRevisions());
            }
            if (revisionEntrySet != null) {
                versViewHelper.setRevisionEntrySet(revisionEntrySet);
                versViewHelper.getRestoreDateCalendar().setDateLong(new Long(revisionEntrySet.getVersionID() * 1000L));
            }
        }

        // Choosed type of revision
        String paramVal = request.getParameter("rev_type");
        if (paramVal != null) {
            try {
                versViewHelper.setTypeOfRevisions(Integer.parseInt(paramVal));
            } catch (Exception t) {
            }
        }

        // Choosed content and/or metadata revisions
        paramVal = request.getParameter("typeOfRevisions");
        if (paramVal != null) {
            try {
                versViewHelper.setContentOrMetadataRevisions(Integer.parseInt(paramVal));
            } catch (Exception t) {
            }
        }

        // display all revisions of not
        paramVal = request.getParameter("displayAllRevisions");
        versViewHelper.setDisplayAllRevisions(!"0".equals(paramVal));

        // Choosed page level
        paramVal = request.getParameter("level");
        if (paramVal != null) {
            try {
                ((PagesVersioningViewHelper) versViewHelper).setPageLevel(Integer.parseInt(paramVal));
            } catch (Exception t) {
            }
        }

        // Choosed nb max of revisions
        paramVal = request.getParameter("nbmax_rev");
        if (paramVal != null) {
            try {
                versViewHelper.setNbMaxOfRevisions(Integer.parseInt(paramVal));
            } catch (Exception t) {
            }
        }

        // Choosed sortAttribute
        paramVal = request.getParameter("sortAttribute");
        if (paramVal != null) {
            try {
                versViewHelper.setSortAttribute(Integer.parseInt(paramVal));
            } catch (Exception t) {
            }
        }

        // Choosed sortOrder
        paramVal = request.getParameter("sortOrder");
        if (paramVal != null) {
            try {
                versViewHelper.setSortOrder(Integer.parseInt(paramVal));
            } catch (Exception t) {
            }
        }
    }

    /**
     * @param mapping
     * @param form
     * @param request
     * @param response
     */
    protected void getSiteMapFormData(ActionMapping mapping,
                                      ActionForm form,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {

        try {
            PagesVersioningViewHelper pagesVersViewHelper =
                    (PagesVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            JahiaEngineCommonData engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            ProcessingContext jParams = engineCommonData.getParamBean();

            String siteMapParam = request.getParameter("sitemap");
            if (siteMapParam != null) {
                ServicesRegistry.getInstance().getJahiaSiteMapService()
                        .invokeTreeSiteMapViewHelperMethod(jParams.getUser(),
                                pagesVersViewHelper.getPage(), jParams.getSessionID(),
                                ContentPage.ARCHIVED_PAGE_INFOS,
                                null, siteMapParam, true, null, null);
            }

            // Choosed nb max of revisions
            String paramVal = request.getParameter("exact_restore");
            if (paramVal != null) {
                pagesVersViewHelper.setExactRestore("yes".equals(paramVal));
            }
        } catch (Exception t) {
            ContentVersioningAction.logger.debug("Error occurred", t);
        }
    }

    /**
     * Restore pages
     *
     * @param pagesToRestore         an array containing alternatively the language to restore
     *                               following by the content page to restore
     * @param jParams
     * @param request
     * @param pagesToRestore
     * @param user
     * @param restoreDate
     * @param removeMoreRecentActive
     * @param activeRestoredPages
     * @throws org.jahia.exceptions.JahiaException
     *
     */
    protected void pagesRestore(ProcessingContext jParams,
                                HttpServletRequest request,
                                Map pagesToRestore,
                                JahiaUser user,
                                long restoreDate,
                                boolean removeMoreRecentActive,
                                boolean activeRestoredPages) throws JahiaException {

        PagesVersioningViewHelper pagesVersViewHelper =
                (PagesVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

        // Build the list of pages to restore ( both page attributes and content ).
        List selectedPages = new ArrayList();
        Iterator iterator = pagesToRestore.keySet().iterator();
        while (iterator.hasNext()) {
            Integer pageID = (Integer) iterator.next();
            if (pageID != null) {
                selectedPages.add(pageID);
            }
        }

        // sort pages to undo staging , childs first
        List sortedPages = ServicesRegistry.getInstance().getJahiaPageService().
                sortPages(selectedPages, jParams.getEntryLoadRequest(), jParams.getUser(), jParams.getOperationMode());
        Iterator sortedPageEnum = sortedPages.iterator();

        // Before we first start restore a page, we need to check if its parent page
        // should be restored too. It should be the case if at the archive date,
        // its parent page is it child !! If we don't restore both of them,
        // an infinite loop occurs.
        /*
        if ( (pagesVersViewHelper.getOperationType() ==
                 PagesVersioningViewHelper.RESTORE_ARCHIVE_CONTENT_OPERATION) ){
            checkPagesToRestore(pagesToRestore,jParams,restoreDate);
        }*/

        List processedPages = new ArrayList();
        while (sortedPageEnum.hasNext()) {
            Integer pageID = (Integer) sortedPageEnum.next();
            List langsToRestore = (List) pagesToRestore.get(pageID);
            String lang = (String) langsToRestore.get(0);
            if (processedPages.contains(pageID)) {
                continue;
            }
            ContentPage contentPage = ContentPage.getPage(pageID.intValue());

            Set langs = new HashSet();
            langs.add(lang);
            ContentObjectEntryState entryState =
                    new ContentObjectEntryState(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                            (int) (restoreDate / 1000), lang);

            boolean undeletePage = false;

            // Are we doing an undelete ?
            if (pagesVersViewHelper.getOperationType() ==
                    ContentVersioningViewHelper.UNDELETE_OPERATION) {
                // Check if we are restoring a currently deleted page.
                // If so and if the restore date is more recent,
                // use the date before the deletion of the page.

                // check if we are processing a deleted page
                if (contentPage.isStagedEntryMarkedForDeletion(lang)) {
                    // undo staging
                    Map pages = new HashMap();
                    langs.add(lang);
                    pages.put(pageID, langs);
                    this.pagesUndoStaging(jParams, request, pages);
                    entryState = null; // to skip restore
                } else {
                    int deleteVersionID = contentPage.getDeleteVersionID();
                    if (deleteVersionID != -1) {
                        undeletePage = true;
                        //a page deleted for real
                        //use the closest entry state before the delete date
                        //entryState = contentPage.getClosestVersionedEntryState(entryState,true);
                        entryState = new ContentObjectEntryState(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                                (deleteVersionID), lang);
                        //use the closest entry state before the delete date
                        //entryState = contentPage.getClosestVersionedEntryState(entryState,true);
                    }
                }
            }
            if (entryState != null) {
                RestoreVersionStateModificationContext stateModificationContext =
                        new RestoreVersionStateModificationContext(contentPage.getObjectKey(), langs, entryState, undeletePage);
                stateModificationContext.setDescendingInSubPages(false);


                // 1. restore current page
                RestoreVersionTestResults results = contentPage.restoreVersion(user, ProcessingContext.EDIT, entryState,
                        (removeMoreRecentActive && !undeletePage),
                        selectedPages.contains(new Integer(contentPage.getID())),
                        stateModificationContext);

                // 2. apply restore on other languages
                List langsArray = (List) pagesToRestore
                        .get(new Integer(contentPage.getID()));
                langsArray.remove(lang);
                Iterator it = langsArray.iterator();
                while (it.hasNext()) {
                    String lng = (String) it.next();
                    entryState = new ContentObjectEntryState(entryState.getWorkflowState(),
                            entryState.getVersionID(), lng);
                    stateModificationContext.getLanguageCodes().clear();
                    stateModificationContext.getLanguageCodes().add(lng);

                    results.merge(contentPage.restoreVersion(user, ProcessingContext.EDIT, entryState,
                            (removeMoreRecentActive && !undeletePage),
                            selectedPages.contains(new Integer(contentPage.getID())),
                            stateModificationContext));
                }

                processedPages.add(pageID);

                // 3. remove more recent page recursively on page childs
                if (!undeletePage && removeMoreRecentActive) {
                    this.removeMoreRecentActivePages(contentPage, user, ProcessingContext.EDIT,
                            entryState, stateModificationContext,
                            new RestoreVersionTestResults(),
                            pagesToRestore, selectedPages, processedPages,
                            jParams);

                }

                if (results != null){
                    if ((pagesVersViewHelper.getRestoreMode() & ContentVersioningViewHelper.RESTORE_METADATA) != 0){
                        results.merge(restoreMetadatas(jParams,pagesVersViewHelper,results.getRestoredPages(),
                                results.getRestoredContainers(),stateModificationContext));
                    }
                }
            }
        }
    }

    /**
     * Undo Staging for pages
     *
     * @param jParams
     * @param request
     * @param pagesToRestore key = pageID, value = List of lang codes
     * @throws org.jahia.exceptions.JahiaException
     *
     */
    protected void pagesUndoStaging(ProcessingContext jParams,
                                    HttpServletRequest request,
                                    Map pagesToRestore) throws JahiaException {

        PagesVersioningViewHelper pagesVersViewHelper =
                (PagesVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

        List processedPages = new ArrayList();
        EntryLoadRequest loadRequest =
                (EntryLoadRequest) jParams.getEntryLoadRequest().clone();
        loadRequest.setWithMarkedForDeletion(true);
        Iterator iterator = pagesToRestore.keySet().iterator();

        // sort pages to undo staging , childs first
        List pageIDs = new ArrayList();
        while (iterator.hasNext()) {
            try {
                Integer pageID = (Integer) iterator.next();
                if (pageID != null) {
                    pageIDs.add(pageID);
                }
            } catch (Exception t) {
                ContentVersioningAction.logger.debug(t);
            }
        }
        List sortedPages = ServicesRegistry.getInstance().getJahiaPageService()
                .sortPages(pageIDs, jParams.getEntryLoadRequest(),
                        jParams.getUser(), jParams.getOperationMode());
        Iterator sortedPageEnum = sortedPages.iterator();

        while (sortedPageEnum.hasNext()) {
            Integer pageID = (Integer) sortedPageEnum.next();
            if (!processedPages.contains(pageID)) {
                ContentPage contentPage = ContentPage.getPage(pageID.intValue());
                UndoStagingContentTreeVisitor visitor =
                        new UndoStagingContentTreeVisitor(contentPage, jParams.getUser(),
                                loadRequest, jParams.getOperationMode(), jParams);
                visitor.undoStaging();
                processedPages.add(new Integer(pageID.intValue()));
                // reset page cache
                contentPage.commitChanges(true, jParams);
            }
        }
        // reset site map cache
        ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();
        pagesVersViewHelper.loadSiteMapViewHelper(jParams.getUser(), request);
    }

    /**
     * Container undo staging
     *
     * @param jParams
     * @param request
     * @throws org.jahia.exceptions.JahiaException
     *
     */
    protected void containerUndoStaging(ProcessingContext jParams,
                                        HttpServletRequest request) throws JahiaException {

        ContentVersioningViewHelper versViewHelper =
                (ContentVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
        EntryLoadRequest loadRequest = (EntryLoadRequest) jParams.getEntryLoadRequest().clone();
        loadRequest.setWithMarkedForDeletion(true);
        UndoStagingContentTreeVisitor visitor = new UndoStagingContentTreeVisitor(versViewHelper.getContentObject(),
                jParams.getUser(), loadRequest, jParams.getOperationMode(), jParams);
        visitor.undoStaging();
    }

    /**
     * @param jParams
     * @param request
     * @throws JahiaException
     */
    protected void containerRestore(ProcessingContext jParams,
                                    HttpServletRequest request)
            throws JahiaException {

        ContentVersioningViewHelper versViewHelper =
                (ContentVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
        ContentObject contentObject = versViewHelper.getContentObject();
        Set langs = new HashSet();
        Locale loc = null;
        Iterator iterator = versViewHelper.getLanguagesToRestore().iterator();
        ContentObjectEntryState entryState = null;
        RestoreVersionStateModificationContext stateModificationContext = null;
        RestoreVersionTestResults results = null;
        while (iterator.hasNext()) {
            loc = (Locale) iterator.next();
            langs.add(loc.toString());
            entryState = new ContentObjectEntryState(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                    (int) (versViewHelper.getRestoreDateCalendar().getDateLong().longValue() / 1000), loc.toString());
            stateModificationContext =
                    new RestoreVersionStateModificationContext(contentObject.getObjectKey(), langs, entryState);
            stateModificationContext.pushAllLanguages(true);
            results = contentObject.restoreVersion(jParams.getUser(), ProcessingContext.EDIT,
                    entryState, true, stateModificationContext);
            if (results != null){
                if ((versViewHelper.getRestoreMode() & ContentVersioningViewHelper.RESTORE_METADATA) != 0){
                    results.merge(restoreMetadatas(jParams,versViewHelper,results.getRestoredPages(),
                            results.getRestoredContainers(),stateModificationContext));
                }
            }
        }
    }

    /**
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @throws JahiaException
     * @throws ServletException 
     * @throws IOException 
     */
    public ActionForward containerVersionCompare(ActionMapping mapping,
                                                 ActionForm form,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response)
            throws JahiaException, IOException, ServletException {

        ActionForward forward = mapping.findForward("containerVersionCompare");
        ActionErrors errors = new ActionErrors();
        ContainerVersioningViewHelper versViewHelper = null;
        JahiaEngineCommonData engineCommonData = null;
        try {
            request.setAttribute("versioningAllowReadAccess", Boolean.TRUE);
            init(mapping, request);
            versViewHelper = (ContainerVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            if (versViewHelper.getContentTreeRevisionsVisitor() == null) {
                loadRevisions(mapping, form, request, response);
            }
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            String version1 = request.getParameter("version1");
            String version2 = request.getParameter("version2");
            Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
            String languageCode = engineCommonData.getParamBean().getLocale().toString();
            EngineLanguageHelper elh = null;
            if (engineMap != null) {
                elh = (EngineLanguageHelper)
                        engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
                if (elh != null) {
                    languageCode = elh.getCurrentLanguageCode();
                }
            }
            versViewHelper.handlebuilFieldVersionCompares(engineCommonData, request, version1, version2, languageCode);
        } catch (Exception t) {
            handleException(t, request, response);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Exception processing container version compare"));
        }
        request.setAttribute("DisableButtons", "DisableButtons");
        request.setAttribute("engineView", "containerVersionCompare");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Forward to errors if any or to continueForward
     *
     * @param mapping
     * @param request
     * @param errors
     * @param continueForward
     */
    public ActionForward continueForward(
            ActionMapping mapping,
            HttpServletRequest request,
            ActionMessages errors,
            ActionForward continueForward) {

        if (errors != null && !errors.isEmpty()) {
            saveErrors(request, errors);
            return mapping.findForward("EnginesErrors");
        }
        return continueForward;
    }

    /**
     * Generate a valid url for this Struts Action
     *
     * @param contentObject
     * @param jParams
     * @param strutsAction
     * @param properties
     * @param params
     * @return
     * @throws org.jahia.exceptions.JahiaException
     *
     */
    public static String composeActionURL(ContentObject contentObject,
                                          ProcessingContext jParams,
                                          String strutsAction,
                                          Properties properties,
                                          String params)
            throws JahiaException {
        // Prepare this struts action Mapping url
        if (properties == null) {
            properties = new Properties();
        }
        properties.put("objectKey", String.valueOf(contentObject.getObjectKey()));
        return jParams.composeStrutsUrl(strutsAction, properties, params);
    }

    /**
     * Returns an array where a[0] is the language to restore and a[1] is
     * the page id of the page to restore.
     *
     * @param pageParamStr
     */
    protected List getPageToRestore(String pageParamStr) {

        List array = new ArrayList();

        try {
            StringTokenizer strToken = new StringTokenizer(pageParamStr, "_");
            String id;
            String lang = "";

            if (strToken.countTokens() > 4) {
                strToken.nextToken();
                strToken.nextToken();
                id = strToken.nextToken();
                strToken.nextToken();
                while (strToken.hasMoreTokens()) {
                    lang += strToken.nextToken();
                    if (strToken.hasMoreTokens()) {
                        lang += "_";
                    }
                }
                array.add(lang);
                array.add(new Integer(id));
            }
        } catch (Exception t) {
            ContentVersioningAction.logger.debug("Error occurred", t);
        }
        return array;
    }

    /**
     * Retrieve pages to restore
     * Returns an List containing alternatively the language followed by
     * the content page to restore.
     *
     * @param request
     */
    protected List getPagesToRestore(HttpServletRequest request) {
        List pages = new ArrayList();
        Iterator paramNames = new EnumerationIterator(request.getParameterNames());
        String paramName;
        while (paramNames.hasNext()) {
            paramName = (String) paramNames.next();
            if (paramName.startsWith("checkbox_ContentPage_")) {
                List array = getPageToRestore(paramName);
                pages.addAll(array);
            }
        }
        return pages;
    }

    /**
     * @param revisionEntrySetKey
     */
    protected RevisionEntrySet getPageRevisionEntrySet(String revisionEntrySetKey) {

        PageRevisionEntrySet revisionEntrySet = null;
        try {
            StringTokenizer strToken = new StringTokenizer(revisionEntrySetKey, "_");
            String objType;
            String objID;
            String v;

            if (strToken.countTokens() == 4) {
                objType = strToken.nextToken();
                objID = strToken.nextToken();
                strToken.nextToken();
                v = strToken.nextToken();
                revisionEntrySet = new PageRevisionEntrySet(Integer.parseInt(v),
                        ObjectKey.getInstance(objType + "_" + objID));
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
        return revisionEntrySet;
    }

    /**
     * @param revisionEntrySetKey
     * @param revisionsList
     */
    protected RevisionEntrySet getContainerRevisionEntrySet(String revisionEntrySetKey, List revisionsList) {

        if (revisionsList == null || revisionsList.size() == 0) {
            return null;
        }
        Iterator iterator = revisionsList.iterator();
        RevisionEntrySet revSet = null;
        while (iterator.hasNext()) {
            revSet = (RevisionEntrySet) iterator.next();
            if (revSet.toString().equals(revisionEntrySetKey)) {
                return revSet;
            }
        }
        return null;
    }

    /**
     * @param contentPage
     * @param user
     * @param operationMode
     * @param entryState
     * @param stateModificationContext
     * @param result
     * @throws org.jahia.exceptions.JahiaException
     *
     */
    protected void removeMoreRecentActivePages(ContentPage contentPage,
                                               JahiaUser user,
                                               String operationMode,
                                               ContentObjectEntryState entryState,
                                               StateModificationContext stateModificationContext,
                                               RestoreVersionTestResults result,
                                               Map pagesToRestore,
                                               List selectedPages,
                                               List processedPages,
                                               ProcessingContext jParams) throws JahiaException {
        List<JahiaPage> childs = contentPage.getChildPages(jParams);
        for (JahiaPage child : childs) {            
            ContentPage contentPageChild = ContentPage.getPage(child.getID());
            if (processedPages.contains(new Integer(child.getID()))) {
                continue;
            }
            if (!contentPageChild.hasActiveEntries() &&
                    !contentPageChild.hasStagingEntries()) {
                // as this page is actually deleted, no need
                // to try to mark it for delete again.
                continue;
            }

            // check if the page is not moved
            if (contentPageChild.getParentID(EntryLoadRequest.STAGED)
                    != contentPage.getID()) {
                continue; // could be a moved page
            }

            if (child.getPageType() == ContentPage.TYPE_DIRECT
                    && contentPageChild.isDeletedOrDoesNotExist(entryState.getVersionID())) {
                // the page was deleted or doesn't exist so remove
                // all languages ( delete the page )

                ContentObjectEntryState removeEntryState =
                        new ContentObjectEntryState(entryState.getWorkflowState(),
                                entryState.getVersionID(),
                                ContentObject.SHARED_LANGUAGE);
                RestoreVersionStateModificationContext smc =
                        new RestoreVersionStateModificationContext(contentPageChild.getObjectKey(),
                                stateModificationContext.getLanguageCodes(), removeEntryState);

                smc.getLanguageCodes().add(ContentObject.SHARED_LANGUAGE);

                result.merge(contentPageChild.restoreVersion(user, operationMode,
                        removeEntryState, true,
                        selectedPages.contains(new Integer(contentPageChild.getID())),
                        smc));
                /*
                // apply restore on other languages
                List langsArray = (List) pagesToRestore
                        .get(new Integer(contentPage.getID()));
                if (langsArray != null) {
                    langsArray.remove(entryState.getLanguageCode());
                    Iterator it = langsArray.iterator();
                    while (it.hasNext()) {
                        String lng = (String) it.next();
                        removeEntryState = new ContentObjectEntryState(
                                removeEntryState.getWorkflowState(),
                                removeEntryState.getVersionID(), lng);
                        smc.getLanguageCodes().clear();
                        smc.getLanguageCodes().add(lng);
                        result.merge(contentPageChild.restoreVersion(user,
                                operationMode,
                                removeEntryState, true,
                                selectedPages.contains(new Integer(contentPageChild.getID())),
                                smc));
                    }
                    pagesToRestore.put(new Integer(contentPageChild.getID()),
                            langsArray.clone());
                }*/

                processedPages.add(new Integer(contentPageChild.getID()));
                // recurse on childs
                removeMoreRecentActivePages(contentPageChild, user,
                        operationMode, entryState, stateModificationContext, result,
                        pagesToRestore, selectedPages, processedPages,
                        jParams);
            }
        }
    }

    // #ifdef LOCK

    /**
     * Tries to acquire all the locks for all the sub pages starting at the
     * current page
     *
     * @param jParams ProcessingContext
     * @return boolean true if all the sub pages could be locked, false
     *         otherwise.
     * @throws org.jahia.exceptions.JahiaException
     *
     */
    protected boolean acquireTreeLocks(ProcessingContext jParams, ActionMessages actionMessages)
            throws JahiaException {
        //@todo: have locks optimized or remove them.
        return true;
        /*
        List acquiredPageLocks = new ArrayList();
        if (jParams.settings().areLocksActivated()) {
            JahiaUser user = jParams.getUser();
            HtmlCache htmlCache = ServicesRegistry.getInstance().getCacheService().getHtmlCacheInstance();
            ContentPage contentPage = ServicesRegistry.getInstance().
                    getJahiaPageService().lookupContentPage(jParams.getPageID(), false);
            JahiaSiteMapService siteMapService = ServicesRegistry.getInstance().
                    getJahiaSiteMapService();
            // let's make sure the whole tree is expanded before we start
            // liberating page locks.
            int pageInfosFlag = ContentPage.ACTIVE_PAGE_INFOS | ContentPage.STAGING_PAGE_INFOS;
            siteMapService.invokeTreeSiteMapViewHelperMethod(jParams.getUser(),
                    contentPage, jParams.getSessionID(), pageInfosFlag,
                    null, "expandall|0", true, null, null);

            // here below we set the page level to maximum because we want
            // to free all the sub pages locks.
            SiteMapViewHelper treeSiteMapViewHelper = siteMapService.
                    getTreeSiteMapViewHelper(jParams.getUser(), contentPage,
                            jParams.getSessionID(), ContentPage.ACTIVE_PAGE_INFOS |
                            ContentPage.STAGING_PAGE_INFOS,
                            null, Integer.MAX_VALUE);

            // Lock all page site if possible.
            LockService lockRegistry = ServicesRegistry.getInstance().getLockService();

            // first let's test if we can acquire ALL the sub pages locks
            // before we start locking anything, otherwise we would end up
            // with half locked trees.
            for (int i = 0; i < treeSiteMapViewHelper.size(); i++) {
                ContentPage siteMapContentPage = treeSiteMapViewHelper.getContentPage(i);
                if (siteMapContentPage != null) {
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE,
                            siteMapContentPage.getID(), siteMapContentPage.getID());
                    if (!lockRegistry.isAcquireable(lockKey, user, user.getUserKey())) {
                        // acquiring of all locks was not successfull, we
                        // exit immediately.
                        actionMessages.add(ActionMessages.GLOBAL_MESSAGE,
                                new ActionMessage("message.org.jahia.views.engines.versioning.pages.cannotLockPage", new Integer(siteMapContentPage.getID())));
                        return false;
                    }
                }
            }

            for (int i = 0; i < treeSiteMapViewHelper.size(); i++) {
                ContentPage siteMapContentPage = treeSiteMapViewHelper.getContentPage(i);
                if (siteMapContentPage != null) {
                    LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE,
                            siteMapContentPage.getID(), siteMapContentPage.getID());
                    if (lockRegistry.acquire(lockKey, user, user.getUserKey(),
                            jParams.getSessionState().getMaxInactiveInterval())) {
                        htmlCache.invalidatePageEntries(Integer.toString(siteMapContentPage.getID()));
                        ContentVersioningAction.logger.debug(
                                "Lock acquired for page " +
                                        siteMapContentPage.getTitles(true));
                        acquiredPageLocks.add(new Integer(siteMapContentPage.getID()));
                    } else {
                        // this case really shouldn't happen since we have
                        // already previously tested lock availability, but
                        // might happen under loads when the two operations
                        // are not perfectly atomic.
                        ContentVersioningAction.logger.warn("Warning, page tree was partly locked for updating, you might want to clear the locks manually.");
                        return false;
                    }
                }
            }
        }
        jParams.getSessionState().setAttribute(ContentVersioningAction.SESSION_VERSIONING_LOCK_LIST, acquiredPageLocks);
        return true;
        */
    }

    protected void releaseTreeLocks(ProcessingContext jParams)
            throws JahiaException {
        //@todo: have locks optimized or remove it
        if (jParams.settings().areLocksActivated()) {
            final Set locks = (Set) jParams.getSessionState().getAttribute("VersionningLocks");
            if (locks != null) {
                final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
                final JahiaUser user = jParams.getUser();
                synchronized (locks) {
                    final Iterator iterator = locks.iterator();
                    while (iterator.hasNext()) {
                        final LockKey lockKey = (LockKey) iterator.next();
                        lockRegistry.release(lockKey, user, user.getUserKey());
                    }
                }
            }
        }
        /*
        if (jParams.settings().areLocksActivated()) {
            List acquiredPageLocks = (List) jParams.getSessionState().getAttribute(ContentVersioningAction.SESSION_VERSIONING_LOCK_LIST);
            if (acquiredPageLocks == null) {
                return;
            }
            HtmlCache htmlCache = ServicesRegistry.getInstance().getCacheService().getHtmlCacheInstance();

            // Lock all page site if possible.
            LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            Iterator acquiredPageIter = acquiredPageLocks.iterator();
            while (acquiredPageIter.hasNext()) {
                int curPageID = ((Integer) acquiredPageIter.next()).intValue();
                LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE,
                        curPageID, curPageID);
                final JahiaUser user = jParams.getUser();
                lockRegistry.release(lockKey, user, user.getUserKey());
                htmlCache.invalidatePageEntries(Integer.toString(curPageID));
            }
            jParams.getSessionState().removeAttribute(ContentVersioningAction.SESSION_VERSIONING_LOCK_LIST);
        }*/
    }

    protected void releaseActionLock(ProcessingContext jParams)
            throws JahiaException {
        if (jParams.settings().areLocksActivated()) {
            LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE,
                    jParams.getPageID());
            final JahiaUser user = jParams.getUser();
            lockRegistry.release(lockKey, user, user.getUserKey());

            Map engineMap = (Map) jParams.getSessionState().getAttribute("jahia_session_engineMap");
            if (engineMap != null) {
                lockKey = (LockKey) engineMap.get("lock");
                if (lockKey != null) {
                    lockRegistry.release(lockKey, user, user.getUserKey());
                }
            }
        }
    }
    // #endif

    protected CalendarHandler getCalHandler(String calIdentifier,
                                            long initialDate,
                                            ProcessingContext jParams) {
        Long date = new Long(initialDate);
        CalendarHandler calHandler =
                new CalendarHandler(jParams.settings().getJahiaEnginesHttpPath(),
                        calIdentifier,
                        CalendarHandler.DEFAULT_DATE_FORMAT,
                        date,
                        jParams.getLocale(),
                        new Long(0));
        return calHandler;
    }

    /**
     * Perform metadata restore
     *
     * @param jParams
     * @param versViewHelper
     * @param restoredPages
     * @param restoredContainers
     * @param stateModificationContext
     * @return
     */
    protected RestoreVersionTestResults restoreMetadatas(ProcessingContext jParams,
                                    ContentVersioningViewHelper versViewHelper,
                                    Set restoredPages, Set restoredContainers,
                                    RestoreVersionStateModificationContext stateModificationContext){
        RestoreVersionTestResults results = new RestoreVersionTestResults();
        if (restoredPages == null && restoredContainers == null){
            return results;
        }
        ObjectKey key = null;
        if (restoredContainers != null){
            for (Iterator it=restoredContainers.iterator(); it.hasNext();){
                key = (ObjectKey)it.next();
                try {
                    results.merge(restoreMetadatas(key,jParams,versViewHelper,stateModificationContext));
                } catch ( Exception t){
                    results.appendError(new RestoreVersionNodeTestResult(
                                        key, stateModificationContext.getEntryState().getLanguageCode(),
                                        "Error restoring metadatas for content"));
                }
            }
        }
        if (restoredPages != null){
            for (Iterator it=restoredPages.iterator(); it.hasNext();){
                key = (ObjectKey)it.next();
                try {
                    results.merge(restoreMetadatas(key,jParams,versViewHelper,stateModificationContext));
                } catch ( Exception t){
                    results.appendError(new RestoreVersionNodeTestResult(
                                        key, stateModificationContext.getEntryState().getLanguageCode(),
                                        "Error restoring metadatas for content"));
                }
            }
        }
        return results;
    }

    protected RestoreVersionTestResults restoreMetadatas(ObjectKey objectKey,ProcessingContext jParams,
                                    ContentVersioningViewHelper versViewHelper,
                                    RestoreVersionStateModificationContext stateModificationContext)
    throws JahiaException {
        RestoreVersionTestResults result = new RestoreVersionTestResults();
        ContentObject contentObject = null;
        try {
            contentObject = ContentContainer.getContentObjectInstance(objectKey);
        } catch ( Exception t){
            logger.debug("Exception occured restoring metadatas for content : " + objectKey, t);
            result.appendError(new RestoreVersionNodeTestResult(
                                objectKey, stateModificationContext.getEntryState().getLanguageCode(),
                                "Cannot restore metadatas for content"));
            return result;
        }
        if (contentObject == null){
            return result;
        }
        result.merge(contentObject.metadataRestoreVersion(jParams.getUser(),jParams.getOperationMode(),
                stateModificationContext.getEntryState(),versViewHelper.isExactRestore(),
                stateModificationContext));
        return result;
    }

}
