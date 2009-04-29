/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.apache.jackrabbit.core.security;

import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.security.auth.Subject;

import org.apache.jackrabbit.api.security.principal.PrincipalManager;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.AccessManagerConfig;
import org.apache.jackrabbit.core.config.LoginModuleConfig;
import org.apache.jackrabbit.core.config.SecurityConfig;
import org.apache.jackrabbit.core.security.authentication.AuthContext;
import org.apache.jackrabbit.core.security.authentication.AuthContextProvider;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.jaas.JahiaLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 25, 2008
 * Time: 1:50:44 PM
 * To change this template use File | Settings | File Templates.
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
    private RepositoryImpl repository;

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
        this.repository = (RepositoryImpl) repository;

        SecurityConfig config = this.repository.getConfig().getSecurityConfig();
        LoginModuleConfig loginModConf = config.getLoginModuleConfig();

        // build AuthContextProvider based on appName + optional LoginModuleConfig
        authContextProvider = new AuthContextProvider(config.getAppName(), loginModConf);
        if (authContextProvider.isJAAS()) {
            log.info("init: use JAAS login-configuration for " + config.getAppName());
        } else if (authContextProvider.isLocal()) {
            log.info("init: use Repository Login-Configuration for " + config.getAppName());
        } else {
            String msg = "Neither JAAS nor RepositoryConfig contained a valid Configuriation for " + config.getAppName();
            log.error(msg);
            throw new RepositoryException(msg);
        }

        initialized = true;
    }


    public String getUserID(Subject subject) throws RepositoryException {
        if (!subject.getPrincipals(AdminPrincipal.class).isEmpty()) {
            return " system ";
        }

        Set<JahiaPrincipal> s =  subject.getPrincipals(JahiaPrincipal.class);
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
        AccessManagerConfig amConfig = getSecurityConfig().getAccessManagerConfig();
        try {
            AccessManager accessMgr = (AccessManager) amConfig.newInstance();
            accessMgr.init(amContext);
            return accessMgr;
        } catch (AccessDeniedException ade) {
            // re-throw
            throw ade;
        } catch (Exception e) {
            // wrap in RepositoryException
            String msg = "Failed to instantiate AccessManager (" + amConfig.getClassName() + ")";
            log.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    public AuthContext getAuthContext(Credentials credentials, Subject subject) throws RepositoryException {
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
