/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jaas;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;
import org.jahia.api.usermanager.JahiaUserManagerService;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

/**
 * Login module checks against Jahia tools user.
 */
public class JahiaLoginModule extends AbstractKarafLoginModule {

    private JahiaUserManagerService userManagerService;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, options);
        this.userManagerService = bundleContext.getService(bundleContext.getServiceReference(JahiaUserManagerService.class));
    }

    @Override
    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[]{new NameCallback("Username: "), new PasswordCallback("Password: ", false)};
        if(this.callbackHandler != null) {
            try {
                this.callbackHandler.handle(callbacks);
            } catch (IOException e) {
                throw new LoginException(e.getMessage());
            } catch (UnsupportedCallbackException e) {
                throw new LoginException(e.getMessage() + " not available to obtain information from user");
            }
        }

        if(((NameCallback)callbacks[0]).getName() == null) {
            throw new LoginException("Username can not be null");
        } else {
            JCRUserNode jahiaUser = checkUser(callbacks);
            checkPermissions(jahiaUser);
        }
        this.succeeded = true;
        return true;
    }

    private JCRUserNode checkUser(Callback[] callbacks) throws LoginException {
        this.user = ((NameCallback)callbacks[0]).getName();
        if(((PasswordCallback)callbacks[1]).getPassword() == null) {
            throw new LoginException("Password can not be null");
        } else {
            String password = new String(((PasswordCallback) callbacks[1]).getPassword());
            JCRUserNode jahiaUser = userManagerService.lookup(user);
            boolean userCheck = jahiaUser != null;
            if (!userCheck) {
                throw new FailedLoginException("User " + user + " does not exist");
            }
            boolean passwordCheck = jahiaUser.verifyPassword(password);
            if (!passwordCheck) {
                throw new FailedLoginException("Password for " + user + " does not match");
            }
            return jahiaUser;
        }
    }

    private void checkPermissions(JCRUserNode jahiaUser) throws FailedLoginException {
        principals = new HashSet<>();
        principals.add(new UserPrincipal(user));
        try {
            boolean permissionCheck = JCRTemplate.getInstance().doExecute(jahiaUser.getJahiaUser(), null, null, session -> session.getNode("/tools").hasPermission("jcr:write"));
            if (permissionCheck) {
                principals.add(new GroupPrincipal("admingroup"));
                principals.add(new RolePrincipal("group"));
                principals.add(new RolePrincipal("admin"));
                principals.add(new RolePrincipal("manager"));
                principals.add(new RolePrincipal("viewer"));
                principals.add(new RolePrincipal("ssh"));
                principals.add(new RolePrincipal("systembundles"));
            }
        } catch (PathNotFoundException e) {
            // Node not readable, ignore
        } catch (RepositoryException e) {
            throw new FailedLoginException("Cannot check permission : " + e.getMessage());
        }
    }
}
