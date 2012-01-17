/**
 * This file is part of the Enterprise Jahia software.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This Enteprise Jahia software must be used in accordance with the terms contained in the
 * Jahia Solutions Group Terms & Conditions as well as the
 * Jahia Sustainable Enterprise License (JSEL). You may not use this software except
 * in compliance with the Jahia Solutions Group Terms & Conditions and the JSEL.
 * See the license for the rights, obligations and limitations governing use
 * of the contents of the software. For questions regarding licensing, support, production usage,
 * please contact our team at sales@jahia.com or go to: http://www.jahia.com/license
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
