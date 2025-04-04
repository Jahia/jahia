/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jcrcommands.rest.filters;


import org.jahia.params.valves.AuthValveContext;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

/**
 * JAX-RS Filter that filters only users that match the required permission or role.
 */
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final String REQUIRED_PERMISSION = "admin";

    @Context
    HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        AuthValveContext ctx = (AuthValveContext) httpServletRequest.getAttribute(AuthValveContext.class.getName());

        String username = JahiaUserManagerService.GUEST_USERNAME;
        try {
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();
            final JahiaUser jahiaUser = currentUserSession.getUser();
            username = jahiaUser.getUserKey();
            if (currentUserSession.getRootNode().hasPermission(REQUIRED_PERMISSION) && ctx != null && !ctx.isAuthRetrievedFromSession()) {
                requestContext.setSecurityContext(new SecurityContext() {

                    @Override
                    public String getAuthenticationScheme() {
                        return httpServletRequest.getScheme();
                    }

                    @Override
                    public Principal getUserPrincipal() {
                        return jahiaUser;
                    }

                    @Override
                    public boolean isSecure() {
                        return httpServletRequest.isSecure();
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return httpServletRequest.isUserInRole(role);
                    }
                });

                return;
            }
        } catch (RepositoryException e) {
            log.error("An error occurs while accessing a resource", e);
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("an error occured %s (see server log for more detail)",
                            e.getMessage() != null ? e.getMessage() : e))
                    .build());
        }

        log.warn("Unauthorized access to the resource by user {}", username);
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(String.format("User %s is not allowed to access resource %s", username, httpServletRequest.getRequestURI())).build());
    }
}
