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
package org.jahia.admin.patches;

import org.apache.log4j.Logger;
import org.jahia.bin.JahiaAdministration;
import org.jahia.version.VersionService;
import org.jahia.admin.AbstractAdministrationModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.SortedMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 29 août 2007
 * Time: 17:37:08
 * To change this template use File | Settings | File Templates.
 */
public class ManagePatches extends AbstractAdministrationModule {
    
    private static final transient Logger logger = Logger.getLogger(ManagePatches.class);
    
    /**
     * @param request
     *            current request
     * @param response
     *            current response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String action = request.getParameter("sub");

        boolean isInit = VersionService.getInstance().isInitialized();

        if ("saveinit".equals(action)) {
            String id = request.getParameter("id");
            try {
                VersionService.getInstance().setBuildNumber(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
            }
        } else if ("install".equals(action)) {
            VersionService.getInstance().installAllPatches();
        } else if (!isInit) {
            JahiaAdministration.doRedirect(request, response, request.getSession(),
                    JahiaAdministration.JSP_PATH + "patch_init.jsp");
            return;
        }

        SortedMap installedPatches = VersionService.getInstance().getInstalledPatchesByVersion();
        request.setAttribute("installedPatches", installedPatches);               

        SortedMap availablePatches = VersionService.getInstance().getPatchesToInstall();
        request.setAttribute("availablePatches",availablePatches);

        request.setAttribute("isPatchesAvailable", Boolean.toString(!availablePatches.isEmpty()));

        JahiaAdministration.doRedirect(request, response, request.getSession(),
                JahiaAdministration.JSP_PATH + "patch_list.jsp");
    }

}
