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
import org.jahia.security.license.LicenseActionChecker;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * User: Serge Huber
 * Date: 22 ao�t 2005
 * Time: 18:59:12
 * Copyright (C) Jahia Inc.
 */
public class JCIFSAuthValveImpl implements Valve {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JCIFSAuthValveImpl.class);

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        if (!LicenseActionChecker.isAuthorizedByLicense("org.jahia.params.valves.JCIFSAuthValve", 0)) {
            valveContext.invokeNext(context);            
        }

        ProcessingContext processingContext = (ProcessingContext) context;
        HttpServletRequest request = ((ParamBean)processingContext).getRequest();
        if (request.getAttribute("ntlmAuthType") != null) {
            Principal principal = (Principal) request.getAttribute("ntlmPrincipal");
        if (principal != null) {
            // JCIFS delivers the Principal name under the form DOMAIN\Username. We
            // will now truncate to only keep the username.
            String userName = principal.getName();
            int backslashPos = principal.getName().lastIndexOf("\\");
            if (backslashPos != -1) {
                userName = principal.getName().substring(backslashPos+1);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Found user "+userName+ "  passed by JCIFS filter, using it to try to login...(Principal.toString=" + principal);
            }
            try {
                JahiaSite site = (JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE);
                JahiaUser jahiaUser = null;
                    jahiaUser = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(site.getID(), userName);
                    if (jahiaUser != null) {
                        processingContext.setTheUser(jahiaUser);
                        return;
                    }
            } catch (Exception e) {
            }
        }
       }
        valveContext.invokeNext(context);
    }

    public void initialize() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
