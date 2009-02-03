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

package org.jahia.admin.templates;

import java.util.Collections;

import org.jahia.bin.JahiaAdministration;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates_deployer.JahiaTemplatesDeployerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.data.JahiaData;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Handler for the template sets management dialog.
 * 
 * @author Sergiy Shyrkov
 */
public class ManageTemplateSets extends AbstractAdministrationModule {

    private static void redirect(String jspFile, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        JahiaAdministration.doRedirect(request, response, request
                .getSession(), JahiaAdministration.JSP_PATH + jspFile);

    }

    private JahiaTemplatesDeployerService service;

    private JahiaUser user;

    /**
     * Handles the displayingof the template set list.
     * 
     * @throws Exception
     *             in case of an error
     */
    private void doList(HttpServletRequest request, HttpServletResponse response) throws Exception {

        request.setAttribute("templateSets", Collections.emptyList());
        //((JahiaTemplatesDeployerBaseService) service).test();
        redirect("manage_template_sets.jsp", request, response);
    }

    /**
     * Initializes an instance of this class.
     * 
     *            current context object
     * @throws Exception
     *             in case of an error
     */
    public void service(HttpServletRequest request, HttpServletResponse response) throws Exception {

        JahiaTemplateManagerService service = ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService();

        if (service == null)
            throw new JahiaException("Template set service is unavailable",
                    "Template set service is unavailable",
                    JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY);
        
        service.stop();
        service.start();

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        user = jParams.getUser();
        if (user == null) {
            request.setAttribute(
                            "jahiaDisplayMessage",
                            JahiaResourceBundle
                                    .getAdminResource(
                                            "org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                            jParams, jParams.getLocale()));
            redirect("menu.jsp", request, response);
            return;
        }

        doList(request, response);
    }
}
