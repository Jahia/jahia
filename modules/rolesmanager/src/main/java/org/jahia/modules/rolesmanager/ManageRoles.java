/**
 * Enterprise Jahia xCM v6.5
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision of enterprise application
 * convergence - web, search, document, social and portal - unified by the simplicity of web content management.
 * Structured around Jahia Extended Content Management (xCM), the Jahia Studio, Jahiapps social and collaboration
 * capabilities, and based on the powerful Jahia Composite Content Management platform, Jahia brings a unique modularity
 * to the Java development arena. It is the tool of choice for building innovative user experiences for virtually any type
 * of web application. For more information, please visit http://www.jahia.com.
 *
 * This Enterprise Jahia instance must be used in accordance with the terms contained in the
 * Jahia Solutions Group Terms & Conditions as well as the Jahia Sustainable Enterprise License (JSEL).
 *
 * For questions regarding licensing, support, production usage... please contact our team at sales@jahia.com.
 */

package org.jahia.modules.rolesmanager;

import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.bin.JahiaAdministration;
import org.jahia.security.license.LicenseChecker;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.jahia.bin.JahiaAdministration.JSP_PATH;

/**
 * Administration module for managing roles and permissions.
 *
 * @author Serge Huber
 */
public class ManageRoles extends AbstractAdministrationModule {
    public static final String JSP_PATH = "/modules/rolesmanager/admin/";

    private static final String FEATURE = "org.jahia.admin.roles";

    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
            throws Exception {
        String operation = request.getParameter("sub");

        if (operation.equals("display")) {
            display(request, response, request.getSession());
        }
    }

    /**
     * Display the server settings page, using doRedirect().
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    private void display(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session)
            throws IOException, ServletException {

        try {
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "roles.jsp");
        } catch (Exception je) {
            String dspMsg = getMessage("message.generalError");
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }
    }

    @Override
    public final boolean isEnabled(JahiaUser user, String siteKey) {
        return LicenseChecker.isAllowed(FEATURE) && super.isEnabled(user, siteKey);
    }
}
