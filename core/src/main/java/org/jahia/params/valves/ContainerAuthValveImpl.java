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
