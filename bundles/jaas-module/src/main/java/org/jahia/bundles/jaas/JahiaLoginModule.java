/*
 * JAHIA DUAL LICENSING IMPORTANT INFORMATION
 * ----------------------------------------------------------
 *
 * This program was developed exclusively for Jahia, the next-generation Open Source CMS.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS PROGRAM IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ----------------------------------------------------------
 *
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "This program is free software; you can redistribute it and/or
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
 * http://www.jahia.com/license"
 *
 * The GPL license is available in the LICENSE folder of this program.
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ----------------------------------------------------------
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained
 * in a separate written agreement between you and Jahia Solutions Group SA."
 *
 * The JSEL license is available in the LICENSE folder of this program.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bundles.jaas;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;
import org.jahia.services.pwd.PasswordService;
import org.jahia.settings.SettingsBean;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

/**
 * Login module checks against jahia tools user
 */
public class JahiaLoginModule extends AbstractKarafLoginModule {
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, options);
    }

    @Override
    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[]{new NameCallback("Username: "), new PasswordCallback("Password: ", false)};
        if(this.callbackHandler != null) {
            try {
                this.callbackHandler.handle(callbacks);
            } catch (IOException var13) {
                throw new LoginException(var13.getMessage());
            } catch (UnsupportedCallbackException var14) {
                throw new LoginException(var14.getMessage() + " not available to obtain information from user");
            }
        }

        if(((NameCallback)callbacks[0]).getName() == null) {
            throw new LoginException("Username can not be null");
        } else {
            this.user = ((NameCallback)callbacks[0]).getName();
            if(((PasswordCallback)callbacks[1]).getPassword() == null) {
                throw new LoginException("Password can not be null");
            } else {
                String password = new String(((PasswordCallback) callbacks[1]).getPassword());
                boolean userCheck = user.equals(SettingsBean.getInstance().getPropertiesFile().getProperty("jahiaToolManagerUsername"));
                if (!userCheck) {
                    throw new FailedLoginException((new StringBuilder()).append("User ").append(user).append(" does not exist").toString());
                }
                boolean passwordCheck = PasswordService.getInstance().matches(password, SettingsBean.getInstance().getPropertiesFile().getProperty("jahiaToolManagerPassword"));
                if (!passwordCheck) {
                    throw new FailedLoginException((new StringBuilder()).append("Password for ").append(user).append(" does not match").toString());
                }
                principals = new HashSet<>();
                principals.add(new UserPrincipal(user));
                principals.add(new GroupPrincipal("admingroup"));
                principals.add(new RolePrincipal("group"));
                principals.add(new RolePrincipal("admin"));
                principals.add(new RolePrincipal("manager"));
                principals.add(new RolePrincipal("viewer"));
                principals.add(new RolePrincipal("systembundles"));
            }
        }
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        this.clear();
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        this.subject.getPrincipals().removeAll(this.principals);
        this.principals.clear();
        return true;
    }
}
