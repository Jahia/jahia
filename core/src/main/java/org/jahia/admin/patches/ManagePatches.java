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
