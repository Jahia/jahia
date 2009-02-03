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

//

package org.jahia.admin.users;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUserManagerService;

/**
 * desc:  This class is used by the administration to select future group members.
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @version 1.0
 */
public class GroupMembersTool
{
    private static final String CLASS_NAME = GroupMembersTool.class.getName();
    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private JahiaSite jahiaSite;

    public static final String ENGINE_LAUNCHER_NAME = CLASS_NAME;

    /**
     * Default constructor.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session.
     */
    public GroupMembersTool(HttpServletRequest request, HttpServletResponse response,
                            HttpSession session)
    {
        // get the current website. get the jahiaserver if it's null...
        jahiaSite = (JahiaSite)session.getAttribute(ProcessingContext.SESSION_SITE);
        // continue the execution of the request...
        requestDispatcher(request, response, session);
    }

    /**
     * This method is used like a dispatcher for home page tool requests.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session.
     */
    public void requestDispatcher(HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session)
    {
        String operation = request.getParameter("subaction");
        if (operation.equals("display")) {
            displayGroupMembersTool(request, response, session);
        } else if (operation.equals("search")) {
            displayGroupMembersTool(request, response, session);
        }
    }

    /**
     * Forward the servlet request and servlet response objects, using the request
     * dispatcher (from the ServletContext). Note: please be careful, use only
     * context relative path.
     *
     * @param       request             servlet request.
     * @param       response            servlet response.
     * @param       target              target, context-relative path.
     */
    private void doRedirect(HttpServletRequest request,
                            HttpServletResponse response,
                            HttpSession session,
                            String target )
    {
        try {
            JahiaAdministration.doRedirect(request, response, session, target);
        } catch (IOException ie) {
            logger.error("Error", ie);
        } catch (ServletException se) {
            logger.error("Error", se.getRootCause());
        }
    }

    /**
     * Display the group members issuing from the user search engine.
     *
     * @param request
     * @param response
     * @param session
     */
    private void displayGroupMembersTool(HttpServletRequest request,
                                         HttpServletResponse response,
                                         HttpSession session)
    {
        logger.debug("Started");
        JahiaUserManagerService uMgr = ServicesRegistry.getInstance().getJahiaUserManagerService();
        request.setAttribute("providerList", uMgr.getProviderList());
        request.setAttribute("resultList", PrincipalViewHelper.getSearchResult(request, jahiaSite.getID()));
        request.setAttribute("userSearch", JSP_PATH + "user_management/user_search.jsp");
        request.setAttribute("jspSource", JSP_PATH + "group_management/group_members_tool.jsp");
        session.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
        doRedirect(request, response, session, JSP_PATH + "adminpopup.jsp");
    }

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GroupMembersTool.class);

}
