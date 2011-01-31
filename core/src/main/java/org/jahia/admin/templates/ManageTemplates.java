/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.admin.templates;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.ajax.gwt.client.core.SessionExpirationException;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.helper.ContentManagerHelper;
import org.jahia.api.Constants;

public class ManageTemplates extends AbstractAdministrationModule {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ManageTemplates.class);

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    // private static ServletContext context;

    private JahiaSite site;

    private JahiaUser user;

    private ServicesRegistry sReg;

    private ContentManagerHelper contentManager;

    /**
     * Default constructor.
     * 
     * @author Alexandre Kraft
     * 
     * @param request
     *            Servlet request.
     * @param response
     *            Servlet response.
     */
    public void service(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        userRequestDispatcher(request, response, request.getSession());
    } // end constructor

    /**
     * This method is used like a dispatcher for user requests.
     * 
     * @author Alexandre Kraft
     * 
     * @param request
     *            Servlet request.
     * @param response
     *            Servlet response.
     * @param session
     *            Servlet session for the current user.
     */
    private void userRequestDispatcher(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws Exception {
        String operation = request.getParameter("sub");

        sReg = ServicesRegistry.getInstance();

        // check if the user has really admin access to this site...
        user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        if (site != null && user != null && sReg != null) {

            // set the new site id to administrate...
            request.setAttribute("site", site);

            if (operation.equals("display")) {
                displayMainView(request, response, session);
            } else if (operation.equals("synchronize")) {
                processSynchronize(request, response, session, jParams.getLocale());
            }
        } else {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label", jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "menu.jsp");
        }

    } // userRequestDispatcher

    // -------------------------------------------------------------------------
    /**
     * Display a list of the templates.
     * 
     * @author NK
     * @param request
     *            Servlet request.
     * @param response
     *            Servlet response.
     * @param session
     *            HttpSession object.
     */
    private void displayMainView(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws Exception {
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_templates.jsp");
    } // end displayTemplateList

    /**
     * Process requested template swapping to remove references to a given
     * template from a page before deleting the template.
     * 
     * @param request
     *            Servlet request.
     * @param response
     *            Servlet response.
     * @param session
     *            Servlet session for the current user.
     */
    protected void processSynchronize(HttpServletRequest request, HttpServletResponse response, HttpSession session, Locale locale)
            throws Exception {
        String nodePath = "/templateSets/" + site.getTemplateFolder();

        try {
            getContentManagementService().deployTemplates(nodePath, site.getJCRLocalPath(), retrieveCurrentSession(request, locale));

            request.setAttribute("processedSynchronize", new Integer(1));
            session.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", getMessage("org.jahia.admin.site.ManageTemplates.templatesDeployed"));            
        } catch (Exception e) {
            logger.error("Error during template deployment", e);
            session.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", getMessage("org.jahia.admin.site.ManageTemplates.deploymentError"));
        }
        
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_templates.jsp");        
    } // end processSynchronize

    public ContentManagerHelper getContentManagementService() {
        return contentManager;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }
    
    /**
     * Retrieve current session by locale
     *
     * @param locale
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession(HttpServletRequest request, Locale locale) throws GWTJahiaServiceException {
        checkSession(request);
        try {
            return JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, locale, null);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot open user session");
        }
    }

    private void checkSession(HttpServletRequest request) throws SessionExpirationException {
        if (request.getSession(false) == null) {
            throw new SessionExpirationException();
        }
    }    
} // end ManageTemplates
