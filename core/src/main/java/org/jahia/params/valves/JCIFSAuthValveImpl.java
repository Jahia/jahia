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
