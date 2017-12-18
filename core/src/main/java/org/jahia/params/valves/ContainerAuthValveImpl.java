/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.params.valves;

import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * This valve retrieves the authentification that was done on the container, if there is one.
 * User: Serge Huber
 * Date: Aug 10, 2005
 * Time: 7:03:55 PM
 * Copyright (C) Jahia Inc.
 */
public class ContainerAuthValveImpl extends BaseAuthValve {

    private static final Logger logger = LoggerFactory.getLogger(ContainerAuthValveImpl.class);

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }
        
        AuthValveContext authContext = (AuthValveContext) context;
        HttpServletRequest request = authContext.getRequest();
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found user " + principal.getName() +
                        "  already in HttpServletRequest, using it to try to login...(Principal.toString=" + principal);
            }
            try {
                JCRUserNode userNode = JahiaUserManagerService.getInstance().lookupUser(principal.getName());
                if (userNode != null) {
                    if (userNode.isAccountLocked()) {
                        logger.debug("Login failed. Account is locked for user " + principal.getName());
                        return;
                    }
                    authContext.getSessionFactory().setCurrentUser(userNode.getJahiaUser());
                    return;
                }
            } catch (Exception e) {
            }
        }
        valveContext.invokeNext(context);
    }

}
