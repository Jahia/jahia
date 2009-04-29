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
package org.jahia.ajax.sitemap;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.params.ProcessingContext;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sitemap.JahiaSiteMapService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.viewhelper.sitemap.PagesFilter;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.engines.selectpage.SelectPage_Engine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Gets the information of the Root page for the sitemap
 *
 * @author Xavier Lawrence
 */
public class GetChildPagesEntryPoint extends SiteMapAbstractAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetChildPagesEntryPoint.class);

    protected static final String DISPLAY = "display";

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {
            final ProcessingContext jParams = retrieveProcessingContext(request, response);

            final String isVersionning = getParameter(request, DISPLAY_PARAM, "");
            final int mode = (isVersionning.indexOf("versionning") > -1 ? VERSIONNING : NORMAL);

            final List locales = jParams.getSite().getLanguageSettings(true);

            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("GetChildPagesEntryPoint");

            final JahiaUser currentUser = jParams.getUser();
            final int loadFlags;
            if (mode == VERSIONNING) {
                loadFlags = ContentPage.ACTIVE_PAGE_INFOS | ContentPage.STAGING_PAGE_INFOS |
                        ContentPage.ARCHIVED_PAGE_INFOS;
            } else if (! jParams.getOperationMode().equals(ProcessingContext.NORMAL)) {
                loadFlags = ContentPage.ACTIVE_PAGE_INFOS | ContentPage.STAGING_PAGE_INFOS;
            } else {
                loadFlags = ContentPage.ACTIVE_PAGE_INFOS;
            }

            final HttpSession session = request.getSession();
            final String entryPoint = (String) session.getAttribute("Select_Page_Entry");
            final String selectedPageOperation = (String) session.getAttribute("selectedPageOperation");
            final boolean moveOp = SelectPage_Engine.MOVE_OPERATION.equals(selectedPageOperation);
            if (entryPoint != null && moveOp) {
                final List directParents = getDirectParents(ContentPageKey.toObjectKeyString(entryPoint),
                        jParams);
                session.setAttribute("InsertionPointParents", directParents);
            }

            final String[] keys = getMultipleXmlNodeValue(request, KEY);

            Map engineMap = (Map)jParams.getSessionState().getAttribute("jahia_session_engineMap");
            JahiaField theField = engineMap != null ? (JahiaField)engineMap.get("theField") : null;
            PagesFilter pagesFilter = null;
            try {
                pagesFilter = theField != null ? JahiaSiteMapService.getCurrentPagesFilter(theField, jParams) : null;
            } catch (JahiaException e) {
            }

            for (int i = 0, size = keys.length; i < size; i++) {
                String key = keys[i];
                final ContentPage currentObject = (ContentPage)getContentObjectFromString(key);
                if (currentObject == null) {
                    logger.warn("Unable to Get Entry Point ! Object '" + key + "' doesn't exist");
                    throw new JahiaBadRequestException(
                            "Unable to Get Entry Point ! Msg: Object '" + key + "' doesn't exist");
                }
                
                if (pagesFilter == null) {
                    logger.debug("Getting SiteMap Entry Point: " + key);
                    if (currentObject.checkReadAccess(currentUser)) {
                        processPage(currentObject, jParams, session, currentUser, locales, resp, root, loadFlags,
                                currentObject.getID(), mode, pagesFilter);
                    }
                } else {
                    List startPages = pagesFilter.getStartPages(currentObject, jParams.getUser(), loadFlags, locales, jParams);
                    for (Iterator it = startPages.iterator(); it.hasNext();){
                        ContentPage currentPage = (ContentPage)it.next();
                        
                        logger.debug("Getting SiteMap Entry Point: " + key);
                        if (currentPage.checkReadAccess(currentUser)) {
                            if (pagesFilter == null || !pagesFilter.filterForDisplay(currentPage, jParams)) {                            
                                processPage(currentPage, jParams, session, currentUser, locales, resp, root, loadFlags,
                                        currentPage.getID(), mode, pagesFilter);
                            }    
                        }
                    }
                }
                resp.appendChild(root);                
            }            
            sendResponse(resp, response);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    /**
     *
     */
    protected List getDirectParents(final String objectKey,
                                    final ProcessingContext jParams)
            throws ClassNotFoundException, JahiaException {
        final List result = new ArrayList();
        final ContentPage insertionPoint = (ContentPage) getContentObjectFromString(objectKey);

        if (insertionPoint != null) {
            ContentObject parent = insertionPoint.getParent(jParams.getUser(),
                    jParams.getEntryLoadRequest(), jParams.getOpMode());
            while (parent != null &&
                    ! parent.getObjectKey().equals(jParams.getSite().getHomeContentPage().getObjectKey())) {
                if (parent.getObjectKey().getType().equals(ContentPageKey.PAGE_TYPE)) {
                    result.add(parent.getObjectKey().toString());
                }
                parent = parent.getParent(jParams.getUser(), jParams.getEntryLoadRequest(), jParams.getOpMode());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("InsertionPoint parents are: " + result);
        }
        return result;
    }
}
