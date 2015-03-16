/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
 */
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
