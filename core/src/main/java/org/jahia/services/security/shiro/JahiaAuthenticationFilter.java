/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.security.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.jahia.api.Constants;
import org.jahia.params.valves.AuthValveContext;
import org.jahia.pipelines.Pipeline;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class JahiaAuthenticationFilter extends AuthenticatingFilter {
    private Pipeline authPipeline;
    private JCRSessionFactory sessionFactory;

    public void setAuthPipeline(Pipeline authPipeline) {
        this.authPipeline = authPipeline;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        return new JahiaUserToken((JahiaUser) WebUtils.toHttp(request).getAttribute(Constants.SESSION_USER));
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        AuthValveContext authValveContext = new AuthValveContext(WebUtils.toHttp(request), WebUtils.toHttp(response), sessionFactory);
        authPipeline.invoke(authValveContext);

        JahiaUser jahiaUser = sessionFactory.getCurrentUser();
        boolean userSet = jahiaUser != null;

        if (userSet) {
            request.setAttribute(Constants.SESSION_USER, jahiaUser);
            executeLogin(request, response);
        } else {
            saveRequestAndRedirectToLogin(request, response);
        }

        return userSet;
    }

    @Override
    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        String loginUrl = getLoginUrl() + "?redirect=" + WebUtils.toHttp(request).getRequestURI();
        WebUtils.issueRedirect(request, response, loginUrl);
    }

}
