/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.core.security;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.jackrabbit.core.config.LoginModuleConfig;
import org.apache.jackrabbit.core.config.SecurityConfig;
import org.apache.jackrabbit.core.security.authentication.AuthContext;
import org.apache.jackrabbit.core.security.authentication.AuthContextProvider;
import org.jahia.jaas.JahiaPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.security.auth.Subject;
import java.util.Set;

/**
 * Jahia specific implementation for Jackrabbit security manager.
 *
 * @author Thomas Draier
 *         Date: Jul 25, 2008
 *         Time: 1:50:44 PM
 */
public class JahiaSecurityManager implements JackrabbitSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(JahiaSecurityManager.class);

    /**
     *
     */
    private boolean initialized;

    /**
     * the repository implementation
     */
    private JahiaRepositoryImpl repository;

    /**
     * session on the system workspace.
     */
    private Session securitySession;

    /**
     * factory for login-context {@see Repository#login())
     */
    private AuthContextProvider authContextProvider;

    //------------------------------------------< JackrabbitSecurityManager >---

    /**
     * @see JackrabbitSecurityManager#init(Repository, Session)
     */
    public synchronized void init(Repository repository, Session systemSession) throws RepositoryException {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }

        securitySession = systemSession;
        this.repository = (JahiaRepositoryImpl) repository;

        SecurityConfig config = this.repository.getConfig().getSecurityConfig();
        LoginModuleConfig loginModConf = config.getLoginModuleConfig();

        // build AuthContextProvider based on appName + optional LoginModuleConfig
        authContextProvider = new JahiaAuthContextProvider(config.getAppName(), loginModConf);
        if (authContextProvider.isLocal()) {
            log.info("init: use Repository Login-Configuration for " + config.getAppName());
        } else if (authContextProvider.isJAAS()) {
            log.info("init: use JAAS login-configuration for " + config.getAppName());
        } else {
            String msg = "Neither JAAS nor RepositoryConfig contained a valid Configuriation for " + config.getAppName();
            log.error(msg);
            throw new RepositoryException(msg);
        }

        initialized = true;
    }

    public String getUserID(Subject subject, String workspace) throws RepositoryException {
        /*if (!subject.getPrincipals(SystemPrincipal.class).isEmpty()) {
            return " system ";
        }*/

        Set<JahiaPrincipal> s = subject.getPrincipals(JahiaPrincipal.class);
        for (JahiaPrincipal jahiaPrincipal : s) {
            return jahiaPrincipal.getName();
        }
        return null;
    }

    public UserManager getUserManager(Session session) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException("UserManager not supported.");
    }

    public PrincipalManager getPrincipalManager(Session session) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException("PrincipalManager not supported.");
    }

    public AccessManager getAccessManager(Session session, AMContext amContext) throws RepositoryException {
        checkInitialized();
        try {
            if (session instanceof JahiaSystemSession) {
                return ((JahiaSystemSession) session).createAccessManager(((JahiaSystemSession) session).getSubject());
            }
            JahiaAccessManager accessMgr = new JahiaAccessManager();
            accessMgr.init(amContext, null, null, repository.getContext(), repository.getConfig().getWorkspaceConfig(session.getWorkspace().getName()));
            return accessMgr;
        } catch (AccessDeniedException ade) {
            // re-throw
            throw ade;
        } catch (Exception e) {
            // wrap in RepositoryException
            String msg = "Failed to instantiate JahiaAccessManager";
            log.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    public AuthContext getAuthContext(Credentials credentials, Subject subject, String workspace) throws RepositoryException {
        checkInitialized();
        return authContextProvider.getAuthContext(credentials, subject, securitySession, null, JahiaLoginModule.SYSTEM, JahiaLoginModule.GUEST);
    }

    public SecurityConfig getSecurityConfig() throws RepositoryException {
        return repository.getConfig().getSecurityConfig();
    }

    public void close() {
        log.info("close");
    }

    public void dispose(String s) {
        log.info("dispose");
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Not initialized");
        }
    }


}
