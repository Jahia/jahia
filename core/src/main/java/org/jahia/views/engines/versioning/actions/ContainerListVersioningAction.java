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
package org.jahia.views.engines.versioning.actions;

import org.apache.struts.action.*;
import org.jahia.content.ContentObject;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.JahiaEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.version.*;
import org.jahia.views.engines.JahiaEngineButtonsHelper;
import org.jahia.views.engines.JahiaEngineCommonData;
import org.jahia.views.engines.JahiaEngineViewHelper;
import org.jahia.views.engines.versioning.ContainerCompareBean;
import org.jahia.views.engines.versioning.ContainerListVersioningViewHelper;
import org.jahia.views.engines.versioning.ContainerVersioningBean;
import org.jahia.views.engines.versioning.ContentVersioningViewHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * <p>Title: Container List Versioning Dispatch Action</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Jahia</p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class ContainerListVersioningAction extends ContentVersioningAction {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContainerListVersioningAction.class);

    /**
     * Display the list of containers
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public ActionForward showContainersList(ActionMapping mapping,
                                            ActionForm form,
                                            HttpServletRequest request,
                                            HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = mapping.getInputForward();
        ActionMessages errors = new ActionMessages();
        try {
            init(mapping, request);
            loadContainers(mapping,form,request,response,true);
        } catch (Exception t) {
        	handleException(t, request, response);
            ContentVersioningViewHelper versViewHelper = (ContentVersioningViewHelper)
            request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            
            errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("Exception occured when processing content object ["
                            + (versViewHelper != null && versViewHelper.getContentObject() != null ? versViewHelper.getContentObject().getObjectKey() : null) + "]"));
        }
        // set engine screen
        request.setAttribute("engineView", "containersList");
        request.setAttribute("DisableButtons", "DisableButtons");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Display the confirm undelete view.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws ServletException
     */
    public ActionForward showConfirmUndelete(ActionMapping mapping,
                                             ActionForm form,
                                             HttpServletRequest request,
                                             HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = null;
        ActionMessages errors = new ActionMessages();
        ContainerListVersioningViewHelper versViewHelper;
        JahiaEngineCommonData engineCommonData;
        try {
            init(mapping, request);
            versViewHelper = (ContainerListVersioningViewHelper)
                request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            versViewHelper.setOperationType(ContentVersioningViewHelper.UNDELETE_OPERATION);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            versViewHelper.setSelectedContainer(0);
            try {
                String containerId = request.getParameter("containerId");
                versViewHelper.setSelectedContainer(Integer.parseInt(containerId));
            } catch ( Exception t ){
            }
            if ( versViewHelper.getSelectedContainer() > 0 ){
                forward = mapping.findForward("confirmUndelete");
            } else {
                forward = showContainersList(mapping, form, request, response);
                request.setAttribute("engineView", "containersList");
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
        } catch (Exception t) {
            handleException(t, request, response);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Error preparing confirm restore view"));
        }
        // set engine screen
        request.setAttribute("engineView", "confirmRestore");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @throws JahiaException
     * @throws ServletException 
     * @throws IOException 
     */
    public ActionForward containerVersionDetail( ActionMapping mapping,
                                                 ActionForm form,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response )
    throws JahiaException, IOException, ServletException {

        ActionForward forward = mapping.findForward("containerVersionDetail");
        ActionMessages errors = new ActionMessages();
        ContainerListVersioningViewHelper versViewHelper = null;
        JahiaEngineCommonData engineCommonData = null;
        try {
            request.setAttribute("versioningAllowReadAccess",Boolean.TRUE);
            init(mapping, request);
            versViewHelper = (ContainerListVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            try {
                String containerId = request.getParameter("containerId");
                versViewHelper.setSelectedContainer(Integer.parseInt(containerId));
            } catch ( Exception t ){
            }
            Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
            String languageCode = engineCommonData.getParamBean().getLocale().toString();
            EngineLanguageHelper elh = null;
            if ( engineMap != null ){
                elh = (EngineLanguageHelper)
                    engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
                if ( elh != null ){
                    languageCode = elh.getCurrentLanguageCode();
                }
            }
            ContentContainer contentContainer = ContentContainer.getContainer(versViewHelper.getSelectedContainer());
            List revisions = ContainerCompareBean
                    .getContentRevisions(contentContainer,
                            engineCommonData.getParamBean(),
                            engineCommonData.getParamBean().getUser(),
                            engineCommonData.getParamBean().getOperationMode(),
                            0,0,0,0,0,
                            RevisionEntrySetComparator.SORT_BY_DATE,
                            RevisionEntrySetComparator.ASC_ORDER,null);
            RevisionEntrySet firstEntrySet = null;
            if ( revisions != null && !revisions.isEmpty() ){
                firstEntrySet = (RevisionEntrySet)revisions.iterator().next();
                ContainerCompareBean containerCompareBean = ContainerCompareBean
                        .getInstance(contentContainer,engineCommonData.getParamBean(),
                                firstEntrySet.toString(),null,
                                ContainerCompareBean.DISPLAY_OLD_VALUE,false, languageCode,revisions);
                versViewHelper.setContainerCompareBean(containerCompareBean);
            }
        } catch (Exception t) {
            handleException(t, request, response);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("Exception processing container version detail"));
        }
        request.setAttribute("DisableButtons", "DisableButtons");
        request.setAttribute("engineView", "containerVersionDetail");
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
     * @throws ServletException
     */
    protected ActionForward restoreContainerListSave(ActionMapping mapping,
                                                     ActionForm form,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = mapping.findForward("containersList");
        ActionMessages errors = new ActionMessages();
        ContainerListVersioningViewHelper versViewHelper;
        JahiaEngineCommonData engineCommonData;
        try {
            versViewHelper = (ContainerListVersioningViewHelper)
                    request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
            engineCommonData = (JahiaEngineCommonData)
                    request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
            switch (versViewHelper.getOperationType()) {
                case ContentVersioningViewHelper.UNDO_STAGING_OPERATION : {
                    this.containerListUndoStaging(engineCommonData.getParamBean(),
                            request);
                    break;
                }
                case ContentVersioningViewHelper.RESTORE_ARCHIVE_CONTENT_OPERATION : {
                    this.containerListRestore(engineCommonData.getParamBean(), request);
                    break;
                }
                case ContentVersioningViewHelper.UNDELETE_OPERATION : {
                    this.containerUndelete(engineCommonData.getParamBean(), request);
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
                params.put("method", "showContainersList");
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
                    new ActionMessage("Error processing container list restore"));
        }
        // set engine screen
        request.setAttribute("engineView", "sitemap");
        return continueForward(mapping, request, errors, forward);
    }

    /**
     * Container List undo staging
     *
     * @param jParams
     * @param request
     * @throws org.jahia.exceptions.JahiaException
     */
    protected void containerListUndoStaging(  ProcessingContext jParams,
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
     *
     * @param jParams
     * @param request
     * @throws JahiaException
     */
    protected void containerListRestore(ProcessingContext jParams,
                                        HttpServletRequest request)
    throws JahiaException {

        ContentVersioningViewHelper versViewHelper =
                (ContentVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
        ContentObject contentObject = versViewHelper.getContentObject();
        String lang = ContentObject.SHARED_LANGUAGE;
        Set<String> langs = new HashSet<String>();
        langs.add(lang);
        ContentObjectEntryState entryState = new ContentObjectEntryState(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
            (int) (versViewHelper.getRestoreDateCalendar().getDateLong().longValue() / 1000), lang);
        RestoreVersionStateModificationContext stateModificationContext =
            new RestoreVersionStateModificationContext(contentObject.getObjectKey(), langs, entryState);

        RestoreVersionTestResults results = contentObject.restoreVersion(jParams.getUser(), ProcessingContext.EDIT,
                entryState, true, stateModificationContext);
        if (results != null){
            if ((versViewHelper.getRestoreMode() & ContentVersioningViewHelper.RESTORE_METADATA) != 0){
                results.merge(restoreMetadatas(jParams,versViewHelper,results.getRestoredPages(),
                        results.getRestoredContainers(),stateModificationContext));
            }
        }
    }

    /**
     *
     * @param jParams
     * @param request
     * @throws JahiaException
     */
    protected void containerUndelete(   ProcessingContext jParams,
                                        HttpServletRequest request)
    throws JahiaException {

        ContainerListVersioningViewHelper versViewHelper =
                (ContainerListVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
        ContentContainer contentContainer = ContentContainer
            .getContainer(versViewHelper.getSelectedContainer());
        if ( contentContainer.isMarkedForDelete() ){
            EntryLoadRequest loadRequest = (EntryLoadRequest) jParams.getEntryLoadRequest().clone();
            loadRequest.setWithMarkedForDeletion(true);
            /*
            UndoStagingContentTreeVisitor visitor = new UndoStagingContentTreeVisitor(contentContainer,
                jParams.getUser(), loadRequest, jParams.getOperationMode(), jParams);
            visitor.undoStaging();
            */
            this.containerUndelete(contentContainer,jParams,loadRequest, true);
        } else if ( contentContainer.isDeleted(ServicesRegistry
                .getInstance().getJahiaVersionService().getCurrentVersionID())){
            int deleteVersionID = contentContainer.getDeleteVersionID();
            if (deleteVersionID != -1) {
                Set langs = new HashSet();
                JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService()
                        .getSite(contentContainer.getSiteID());
                List v = site.getLanguageSettings(true);
                SiteLanguageSettings langSettings = null;
                for ( int i=0; i<v.size(); i++ ){
                    langSettings = (SiteLanguageSettings)v.get(i);
                    langs.clear();
                    langs.add(langSettings.getCode());
                    ContentObjectEntryState entryState =
                        new ContentObjectEntryState(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                                (deleteVersionID), langSettings.getCode());
                    entryState = contentContainer.getEntryState(entryState,true,false);
                    if (entryState != null) {
                        entryState = new ContentObjectEntryState(entryState.getWorkflowState(),
                                entryState.getVersionID(),langSettings.getCode());
                        RestoreVersionStateModificationContext stateModificationContext =
                                new RestoreVersionStateModificationContext(contentContainer.getObjectKey(),
                                        langs, entryState, true);
                        stateModificationContext.setDescendingInSubPages(false);
                        contentContainer.restoreVersion(jParams.getUser(),
                                                        ProcessingContext.EDIT, entryState,false,
                                stateModificationContext);
                    }
                }
                EntryLoadRequest loadRequest = (EntryLoadRequest)EntryLoadRequest.VERSIONED.clone();
                loadRequest.setVersionID(deleteVersionID);
                loadRequest.setWithDeleted(true);
                loadRequest.setWithMarkedForDeletion(true);
                this.containerUndelete(contentContainer,jParams,loadRequest, false);
            }
        }
    }

    protected void containerUndelete(ContentContainer contentContainer,
                                   ProcessingContext jParams, EntryLoadRequest loadRequest, boolean undoStaging)
    throws JahiaException {
        List childs = contentContainer.getChilds(jParams.getUser(),loadRequest, JahiaContainerStructure.JAHIA_FIELD);
        Iterator iterator = childs.iterator();
        Object child = null;
        ContentPageField pageField = null;
        ContentPage subPage = null;
        String fieldValue = null;
        boolean pageRestore = false;
        while ( iterator.hasNext() ){
            child = iterator.next();
            if ( child instanceof ContentPageField ){
                pageField = (ContentPageField)child;
                fieldValue = pageField.getValue(jParams,loadRequest);
                if ( fieldValue != null ){
                    try {
                        int pageId = Integer.parseInt(fieldValue);
                        if ( pageId > 0){
                            subPage = ContentPage.getPage(pageId);
                            if ( subPage != null &&
                                    subPage.getPageType(EntryLoadRequest.STAGED) == ContentPage.TYPE_DIRECT ){
                                if ( subPage.isMarkedForDelete() ){
                                    pageRestore = true;
                                    this.pageUndelete(subPage,jParams);
                                } else if (!subPage.hasStagingEntries()
                                    && subPage.isDeleted(ServicesRegistry.getInstance().getJahiaVersionService()
                                    .getCurrentVersionID()) ){
                                    pageRestore = true;
                                    this.pageUndelete(subPage,jParams);
                                }
                            }
                        }
                    } catch ( Exception t ){
                        logger.debug("Error restoring sub page when undeleting parent container "
                                + contentContainer.getID(),t);
                    }
                }
            }
        }
        if ( undoStaging && !pageRestore ){
            UndoStagingContentTreeVisitor visitor = new UndoStagingContentTreeVisitor(contentContainer,
                jParams.getUser(), loadRequest, jParams.getOperationMode(), jParams);
            visitor.undoStaging();
        }
    }

    protected void pageUndoMarkedForDelete(ContentPage page,
                                           ProcessingContext jParams)
    throws JahiaException {
        if ( page == null ){
            return;
        }
        EntryLoadRequest loadRequest = (EntryLoadRequest) jParams.getEntryLoadRequest().clone();
        loadRequest.setWithMarkedForDeletion(true);
        UndoStagingContentTreeVisitor visitor = new UndoStagingContentTreeVisitor(page,
            jParams.getUser(), loadRequest, jParams.getOperationMode(), jParams);
        visitor.undoStaging();
        // reset page cache
        page.commitChanges(true, jParams);
        // reset site map cache
        ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();
        Iterator childPages = page
                .getDirectContentPageChilds(jParams.getUser(),ContentPage.STAGING_PAGE_INFOS,ContentObject.SHARED_LANGUAGE);
        ContentPage childPage = null;
        List processedPages = new ArrayList();
        while ( childPages.hasNext() ){
            childPage = (ContentPage)childPages.next();
            if ( !processedPages.contains(new Integer(childPage.getID())) ){
                processedPages.add(new Integer(childPage.getID()));
                pageUndoMarkedForDelete(childPage, jParams);
            }
        }
    }

    protected void pageUndelete(   ContentPage page,
                                   ProcessingContext jParams)
    throws JahiaException {
        if ( page == null ){
            return;
        }
        int versionId = -1;
        boolean undeletePage = false;
        if ( page.isMarkedForDelete() ){
            versionId = page.getActiveVersionID();
        } else {
            versionId = page.getDeleteVersionID();
            undeletePage = true;
        }
        if (versionId != -1) {
            JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService()
                    .getSite(page.getSiteID());
            List v = site.getLanguageSettings(true);
            SiteLanguageSettings langSettings = null;
            String restoreLangCode = null;
            ContentObjectEntryState entryState = null;
            for ( int i=0; i<v.size(); i++ ){
                langSettings = (SiteLanguageSettings)v.get(i);
                Set langs = new HashSet();
                //a page deleted for real
                //use the closest entry state before the delete date
                //entryState = contentPage.getClosestVersionedEntryState(entryState,true);
                entryState =
                        new ContentObjectEntryState(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                                (versionId), langSettings.getCode());
                if (entryState != null) {
                    restoreLangCode = langSettings.getCode();
                    langs.add(langSettings.getCode());
                    RestoreVersionStateModificationContext stateModificationContext =
                            new RestoreVersionStateModificationContext(page.getObjectKey(),
                                    langs, entryState, undeletePage);
                    stateModificationContext.setDescendingInSubPages(false);

                    // 1. restore current page
                    page.restoreVersion(jParams.getUser(), ProcessingContext.EDIT, entryState,
                            false, true, stateModificationContext);
                    break;
                }
            }
            if ( entryState != null ){
                for ( int i=0; i<v.size(); i++ ){
                    langSettings = (SiteLanguageSettings)v.get(i);
                    if ( !langSettings.getCode().equals(restoreLangCode) ){
                        // 2. apply restore on other languages
                        Set langs = new HashSet();
                        entryState = new ContentObjectEntryState(entryState.getWorkflowState(),
                                entryState.getVersionID(), langSettings.getCode());

                        RestoreVersionStateModificationContext stateModificationContext =
                                new RestoreVersionStateModificationContext(page.getObjectKey(),
                                        langs, entryState, undeletePage);
                        stateModificationContext.setDescendingInSubPages(false);
                        page.restoreVersion(jParams.getUser(), ProcessingContext.EDIT, entryState,
                                false, true, stateModificationContext);
                    }
                }
            }
            Iterator childPages = page
                    .getDirectContentPageChilds(jParams.getUser(),ContentPage.ARCHIVED_PAGE_INFOS,ContentObject.SHARED_LANGUAGE);
            ContentPage childPage = null;
            List processedPages = new ArrayList();
            while ( childPages.hasNext() ){
                childPage = (ContentPage)childPages.next();
                if ( !processedPages.contains(new Integer(childPage.getID())) ){
                    processedPages.add(new Integer(childPage.getID()));
                    pageUndelete(childPage, jParams);
                }
            }
        }
    }

    /**
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    protected void loadContainers(  ActionMapping mapping,
                                    ActionForm form,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    boolean deletedContainerOnly)
    throws IOException, ServletException, JahiaException {

        ContentVersioningViewHelper versViewHelper = (ContentVersioningViewHelper)
                request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);
        JahiaEngineCommonData engineCommonData = (JahiaEngineCommonData)
                request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
        List containersList = new ArrayList();
        if ( versViewHelper.getContentObject() != null ){
            List<Integer> ids = ServicesRegistry.getInstance().getJahiaContainersService()
                    .getctnidsInList(versViewHelper.getContentObject().getID(),null);
            int size = ids.size();
            Integer id = null;
            ContentContainer container = null;
            ContainerVersioningBean containerVB = null;
            Locale locale = engineCommonData.getParamBean().getLocale();
            int now = ServicesRegistry.getInstance().getJahiaVersionService().getCurrentVersionID();
            for ( int i=0; i<size; i++ ){
                containerVB = null;
                id = (Integer)ids.get(i);
                try {
                    container = ContentContainer.getContainer(id.intValue());
                    if ( container.checkWriteAccess(engineCommonData.getParamBean().getUser()) ){
                        if ( !deletedContainerOnly ){
                            containerVB = ContainerVersioningBean
                                .getInstance(container,engineCommonData.getParamBean(),locale,null);
                        } else {
                            if ( container.isMarkedForDelete() ||
                                !container.hasStagingEntryIgnoreLanguageCase(ContentObject.SHARED_LANGUAGE)
                                        && container.isDeleted(now) ){
                                containerVB = ContainerVersioningBean
                                    .getInstance(container,engineCommonData.getParamBean(),locale,null);
                            }
                        }
                        if ( containerVB != null ){
                            containersList.add(containerVB);
                        }
                    }
                } catch ( Exception t ){
                    logger.debug("Error loading container [" + id.intValue() + "]", t);
                }
            }
        }

        request.setAttribute("containersList", containersList);
    }

}
