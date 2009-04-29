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
 package org.jahia.params.valves;

import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.registries.ServicesRegistry;

import javax.servlet.http.HttpServletRequest;

import java.security.Principal;

/**
 * This valve retrieves the authentification that was done on the container, if there is one.
 * User: Serge Huber
 * Date: Aug 10, 2005
 * Time: 7:03:55 PM
 * Copyright (C) Jahia Inc.
 */
public class ContainerAuthValveImpl implements Valve {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContainerAuthValveImpl.class);

    public ContainerAuthValveImpl() {
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        ProcessingContext processingContext = (ProcessingContext) context;
        HttpServletRequest request = ((ParamBean)processingContext).getRequest();
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found user "+principal.getName()+ "  already in HttpServletRequest, using it to try to login...(Principal.toString=" + principal);
            }
            try {
                JahiaSite site = (JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE);
                JahiaUser jahiaUser = null;
                    jahiaUser = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(site.getID(), principal.getName());
                    if (jahiaUser != null) {
                        processingContext.setTheUser(jahiaUser);
                        return;
                    }
            } catch (Exception e) {
            }
        }
        valveContext.invokeNext(context);
    }

    public void initialize() {
    }
}
