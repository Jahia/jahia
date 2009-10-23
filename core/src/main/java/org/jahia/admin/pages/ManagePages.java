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
// $Id$
//
//  ManagePages
//
//  01.04.2001  AK  added in jahia.
//  23.05.2001  NK  Lot of change mainly to display the page's template when the user choose another page in the page selectbox.
//  13.16.2001  MJ  Avoid crash when pointing to a page referencing a deleted template.
//

package org.jahia.admin.pages;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.data.events.*;
import org.jahia.admin.AbstractAdministrationModule;

/**
 * desc:  This class is used by the administration to manage
 * pages settings. For each page on the site, you can choose wich template
 * you want. It's a very good tool when you use the template swapping and
 * the template doesn't work correctly.
 * <p/>
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @version 1.0
 */
public class ManagePages extends AbstractAdministrationModule {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManagePages.class);

    private static final String CLASS_NAME = JahiaAdministration.CLASS_NAME;
    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;
    private JahiaSite site;


    /**
     * Default constructor.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request,
                       HttpServletResponse response)
            throws Exception {
        // get the current website. get the jahiaserver if it's null...
        site = (JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE);
        if (site == null) {
            JahiaSitesService sitesService = ServicesRegistry.getInstance().getJahiaSitesService();
            site = sitesService.getSite(0);
            request.getSession().setAttribute(ProcessingContext.SESSION_SITE, site);
        }

        try {
        userRequestDispatcher(request, response, request.getSession());
        } catch (Exception e) {
            logger.error(e, e);
        }
    } // end constructor


    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    private void userRequestDispatcher(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws Exception {
        String operation = request.getParameter("sub");

        if (operation.equals("display")) {
            displayPagesSettings(request, response, session);
        } else if (operation.equals("process")) {
            processPagesSettings(request, response, session);
        }
    } // userRequestDispatcher


    /**
     * Display the pages settings view, using doRedirect(). It's on this
     * page where you can change the template for each page on your Jahia
     * portal.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    private void displayPagesSettings(HttpServletRequest request,
                                      HttpServletResponse response,
                                      HttpSession session)
            throws IOException, ServletException {
        try {

            // get homepage id for the current site...
            Integer homePageID = new Integer(site.getHomePageID());

            // retrieve previous form values...
            Integer basePageID = (Integer) request.getAttribute(CLASS_NAME + "basePageID");
            Integer baseTemplateID = (Integer) request.getAttribute(CLASS_NAME + "baseTemplateID");

            logger.debug("displayPagesSettings basePageID: " + basePageID + ", baseTemplateID: " + baseTemplateID);

            // get only visible templates
            Iterator allTemplatesIterator = ServicesRegistry.getInstance().getJahiaPageTemplateService().getPageTemplates(site.getID(), true);

            // set default values...
            if (basePageID == null) {
                basePageID = new Integer(0);
            }
            if (baseTemplateID == null) {
                baseTemplateID = new Integer(0);
            }

            // set all pages infos into an Iterator and redirect...
            request.setAttribute("homePageID", homePageID);
            request.setAttribute("basePageID", basePageID);
            request.setAttribute("baseTemplateID", baseTemplateID);
            request.setAttribute("allTemplatesIterator", allTemplatesIterator);

            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "pages_settings.jsp");

            // reset message...
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);
        } catch (JahiaException je) {
            logger.error(je, je);
        }
    } // end displayPagesSettings


    /**
     * Process and determine which template the user want for wich
     * page. It's a simple process, but check if the user has choosed
     * a page and a template in select boxs.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    protected void processPagesSettings(HttpServletRequest request,
                                        HttpServletResponse response,
                                        HttpSession session)
            throws IOException, ServletException {
        int idPage = 0;
        int idTemplate = 0;
        JahiaPage page = null;
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        try {

            JahiaPageService pageServ = ServicesRegistry.getInstance().getJahiaPageService();

            if (pageServ == null) {
                throw new JahiaException("Unavailable Services",
                        "Unavailable Services",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }


            String subAction = request.getParameter("subaction");

            // get form values...
            String idPageString = request.getParameter("pageid");

            logger.debug("processPagesSettings pageid: " + idPageString + ", subaction: " + subAction);

            try {
                idPage = Integer.parseInt(idPageString);
                if (idPage != 0) {
                    page = pageServ.lookupPage(idPage, jParams);
                    if (page != null) {

                        JahiaPageDefinition pageDef = page.getPageTemplate();
                        if (pageDef != null) {
                            idTemplate = pageDef.getID();
                        }
                    }
                }
            } catch (NumberFormatException nfe) {
                logger.debug(nfe, nfe);
                throw new JahiaException("ManagePages",
                        "page not found",
                        JahiaException.DATA_ERROR,
                        JahiaException.ERROR_SEVERITY, nfe);
                // Mik - 13.06.2001
            } catch (JahiaTemplateNotFoundException jtnfe) {
                // idTemplate remains 0 ... the template referenced by this page has been deleted
                // so we want to set a new template.
                logger.debug(jtnfe, jtnfe);
            }

            if (subAction.equals("save")) {

                // check form validity...
                if (idPageString.equals("0")) {
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.choicePage.label",
                            jParams.getLocale());
                    session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    logger.debug("processPagesSettings: " + dspMsg);
                    idTemplate = 0;
                } else if (page != null) {
                    logger.debug("processPagesSettings page != null");
                    String idTemplateString = request.getParameter("templateid");
                    logger.debug("processPagesSettings templateid: " + idTemplateString);

                    try {
                        idTemplate = Integer.parseInt(idTemplateString);

                        if (idTemplate != 0) {
                            // set new settings for this JahiaPage...
                            page.setPageTemplateID(idTemplate);
                            page.commitChanges(true, jParams.getUser());
                            ContentPage contentPage = ContentPage.getPage(page.getID());
                            JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, contentPage);
                            ServicesRegistry.getInstance().getJahiaEventService()
                                    .fireContentObjectUpdated(objectCreatedEvent);

                            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.changeUpdated.label",
                                    jParams.getLocale());
                            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                        } else {
                            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.choiceTemplate.label",
                                    jParams.getLocale());
                            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                        }
                    } catch (NumberFormatException nfe) {

                        throw new JahiaException("ManagePages",
                                nfe.getMessage(),
                                JahiaException.DATA_ERROR,
                                JahiaException.ERROR_SEVERITY, nfe);

                    }
                } else {
                    logger.debug("Page not found: " + idPage);
                    throw new JahiaException("ManagePages",
                            "Page not found [" + idPage + "]",
                            JahiaException.DATA_ERROR,
                            JahiaException.ERROR_SEVERITY);
                }
            }

            request.setAttribute(CLASS_NAME + "basePageID", new Integer(idPage));
            request.setAttribute(CLASS_NAME + "baseTemplateID", new Integer(idTemplate));
            displayPagesSettings(request, response, session);

        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }


    } // end processPagesSettings


} // end ManagePages
