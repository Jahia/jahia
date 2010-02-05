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
package org.jahia.admin.roles;

import org.jahia.bin.JahiaAdministration;
import org.jahia.params.ProcessingContext;
import org.jahia.data.JahiaData;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.admin.AbstractAdministrationModule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * User: Serge Huber
 * Date: 27 d�c. 2005
 * Time: 14:16:23
 * Copyright (C) Jahia Inc.
 */
public class ManageServerRoles extends AbstractAdministrationModule {

    public static final String SERVER_PERMISSIONS_PREFIX = "org.jahia.actions.server.";

    /**
     * logging
     */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageServerRoles.class);

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

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

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        try {
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "permissions/server_permissions.jsp");
        } catch (Exception je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }
    }


}
