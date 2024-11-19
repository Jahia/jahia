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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.security;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.apache.jackrabbit.core.config.LoginModuleConfig;
import org.apache.jackrabbit.core.security.authentication.AuthContext;
import org.apache.jackrabbit.core.security.authentication.AuthContextProvider;
import org.apache.jackrabbit.core.security.authentication.CallbackHandlerImpl;
import org.apache.jackrabbit.core.security.authentication.JAASAuthContext;
import org.apache.jackrabbit.core.security.authentication.LocalAuthContext;
import org.apache.jackrabbit.core.security.principal.PrincipalProviderRegistry;

/**
 * Jahia authentication provider. Introduced to work around the bug https://issues.apache.org/jira/browse/JCR-1977 <br>
 * in order to prefer local login configuration (if available) over JAAS. <br>
 * The problem was fixed in Jackrabbit 1.6 (revision 791815).
 * After upgrade to 1.6 this class should be dropped.
 *
 * @author Sergiy Shyrkov
 *
 * @deprecated Use {@link org.apache.jackrabbit.core.security.authentication.AuthContextProvider} instead
 */
@Deprecated
public class JahiaAuthContextProvider extends AuthContextProvider {

    public static class JahiaJAASAuthContext extends JAASAuthContext {
        protected JahiaJAASAuthContext(String appName,
                CallbackHandler cbHandler, Subject subject) {
            super(appName, cbHandler, subject);
        }
    }

    public static class JahiaLocalAuthContext extends LocalAuthContext {
        protected JahiaLocalAuthContext(LoginModuleConfig config,
                CallbackHandler cbHandler, Subject subject) {
            super(config, cbHandler, subject);
        }
    }

    private String applicationName;
    private LoginModuleConfig configuration;

    /**
     * Initializes an instance of this class.
     *
     * @param appName
     * @param config
     */
    public JahiaAuthContextProvider(String appName, LoginModuleConfig config) {
        super(appName, config);
        applicationName = appName;
        configuration = config;
    }

    @Override
    public AuthContext getAuthContext(Credentials credentials, Subject subject,
            Session session,
            PrincipalProviderRegistry principalProviderRegistry,
            String adminId, String anonymousId) throws RepositoryException {

        CallbackHandler cbHandler = new CallbackHandlerImpl(credentials, session, principalProviderRegistry, adminId, anonymousId);

        if (isLocal()){
            return new JahiaLocalAuthContext(configuration, cbHandler, subject);
        } else if (isJAAS()) {
            return new JahiaJAASAuthContext(applicationName, cbHandler, subject);
        } else {
            throw new RepositoryException("No Login-Configuration");
        }
    }

    @Override
    public boolean isJAAS() {
        return !isLocal() && super.isJAAS();
    }

    @Override
    public boolean isLocal() {
        return configuration != null;
    }
}
