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

package org.jahia.ajax.sitemap;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.viewhelper.sitemap.PagesFilter;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sitemap.JahiaSiteMapService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.comparator.JahiaPageComparator;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Struts AJAX based Action that collects all the direct child pages from a given parent page. This action would typically be used for a
 * basic and simple sitemap
 *
 * @author Xavier Lawrence
 */
public class GetChildPages extends SiteMapAbstractAction {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(GetChildPages.class);

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {
            ProcessingContext jParams = retrieveProcessingContext(request, response);

            final JahiaUser currentUser = jParams.getUser();

            final String key = getXmlNodeValue(request, KEY);
            final String isVersionning = getParameter(request, DISPLAY_PARAM, "");
            final int mode = (isVersionning.indexOf("versionning") > -1 ? VERSIONNING : NORMAL);
            final ContentPage currentObject = (ContentPage) getContentObjectFromString(key);
            if (currentObject == null) {
                logger.warn("Unable to Get Child Pages ! Object '" + key + "' doesn't exist");
                throw new JahiaBadRequestException("Unable to Get Child Pages ! Msg: Page '" + key + "' doesn't exist");
            }

            // Get all active languages
            final List locales = jParams.getSite().getLanguageSettings(true);

            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("GetChildPagesResp");

            final int loadFlags;
            if (mode == VERSIONNING) {
                loadFlags = ContentPage.ACTIVE_PAGE_INFOS
                        | ContentPage.STAGING_PAGE_INFOS
                        | ContentPage.ARCHIVED_PAGE_INFOS;
            } else if (!jParams.getOperationMode().equals(
                    ProcessingContext.NORMAL)) {
                loadFlags = ContentPage.ACTIVE_PAGE_INFOS
                        | ContentPage.STAGING_PAGE_INFOS;
            } else {
                loadFlags = ContentPage.ACTIVE_PAGE_INFOS;
            }

            
            Map engineMap = (Map)jParams.getSessionState().getAttribute("jahia_session_engineMap");
            JahiaField theField = engineMap != null ? (JahiaField)engineMap.get("theField") : null;
            PagesFilter pagesFilter = null;
            try {
                pagesFilter = theField != null ? JahiaSiteMapService.getCurrentPagesFilter(theField, jParams) : null;
            } catch (JahiaException e) {
            }

            final boolean directPagesOnly = (pagesFilter == null ? true : pagesFilter.isDirectPagesOnly());
            List childPages = ServicesRegistry.getInstance().getJahiaPageService()
                .getContentPageChilds(currentObject.getID(), currentUser, loadFlags,
                null, directPagesOnly);
            if (pagesFilter != null) {
                childPages = pagesFilter.filterChildren(currentObject, childPages, jParams);
            }
            final Iterator children = childPages.iterator();
            final TreeSet orderedPage = new TreeSet(new JahiaPageComparator(jParams, new HashMap()));
            while (children.hasNext()) {
                final ContentPage child = (ContentPage) children.next();
                if (getParameter(request, "noPickers", "false").equalsIgnoreCase("false")
                        || child.getPickedObject() == null) {
                    if (child.checkReadAccess(currentUser))
                        orderedPage.add(child);
                }
            }

            final Iterator ite = orderedPage.iterator();
            final HttpSession session = request.getSession();
            while (ite.hasNext()) {
                final ContentPage child = (ContentPage) ite.next();
                if (pagesFilter == null || !pagesFilter.filterForDisplay(child, jParams)) {
                    processPage(child, jParams, session, currentUser, locales, resp, root, loadFlags,
                            currentObject.getID(), mode, pagesFilter);
                }   
            }

            resp.appendChild(root);
            sendResponse(resp, response);
        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }
}
