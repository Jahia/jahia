/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
