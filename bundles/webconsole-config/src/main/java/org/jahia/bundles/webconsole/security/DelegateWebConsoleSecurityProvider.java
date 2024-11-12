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
package org.jahia.bundles.webconsole.security;

import org.apache.felix.webconsole.WebConsoleSecurityProvider2;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Jahia security provider for the Felix OSGi Web console that delegates the authentication to the Web container and checks that the user
 * has the condigured required role.
 *
 * @author Sergiy Shyrkov
 */
public class DelegateWebConsoleSecurityProvider implements WebConsoleSecurityProvider2 {
    public static final Logger logger = LoggerFactory.getLogger(DelegateWebConsoleSecurityProvider.class);

    private String requiredPermission;

    @Override
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
        try {
            return JCRTemplate.getInstance().doExecute((JahiaUser) request.getUserPrincipal(), null, null, session -> session.getNode("/tools").hasPermission(requiredPermission));
        } catch (RepositoryException e) {
            logger.error("Cannot check permission", e);
        }
        return false;
    }

    @Override
    public Object authenticate(String username, String password) {
        return false;
    }

    @Override
    public boolean authorize(Object user, String role) {
        return false;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }
}
