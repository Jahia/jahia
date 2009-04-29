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
package org.jahia.admin.templates;

import java.util.Collections;

import org.jahia.bin.JahiaAdministration;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
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
                                    .getJahiaInternalResource(
                                            "org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                            jParams.getLocale()));
            redirect("menu.jsp", request, response);
            return;
        }

        doList(request, response);
    }
}
