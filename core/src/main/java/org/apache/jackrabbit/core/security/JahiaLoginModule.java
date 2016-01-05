/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.security;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.IdentifierGeneratorFactory;
import org.apache.jackrabbit.core.security.authentication.CredentialsCallback;
import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Digital Experience Manager specific implementation of the login module.
 * 
 * @author toto
 */
public class JahiaLoginModule implements LoginModule {
    private static final Logger logger = LoggerFactory.getLogger(JahiaLoginModule.class);

    public static final String SYSTEM = " system ";
    public static final String GUEST = " guest ";
    public static final String REALM_ATTRIBUTE = "org.jahia.realm";

    private static IdentifierGenerator idGen = IdentifierGeneratorFactory.newInstance().sessionIdGenerator();
    private static Map<String, Token> systemPass = new ConcurrentHashMap<String, Token>();

    private Subject subject;
    private Set<Principal> principals = new HashSet<Principal>();
    private CallbackHandler callbackHandler;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String,?> sharedState, Map<String,?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        try {
            String name = null;
            char[] pass = null;
            String realm = null;
            String impersonatorName = null;
            char[] impersonatorPass = null;
            Callback[] callbacks = new Callback[] { new CredentialsCallback() };
            callbackHandler.handle(callbacks);
            Credentials credentials = ((CredentialsCallback)callbacks[0]).getCredentials();

            if (credentials instanceof SimpleCredentials) {
                SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
                name = simpleCredentials.getUserID();
                pass = simpleCredentials.getPassword();
                realm = (String) simpleCredentials.getAttribute(REALM_ATTRIBUTE);

                SimpleCredentials impersonatorCredentials = (SimpleCredentials) simpleCredentials.getAttribute(
                        SecurityConstants.IMPERSONATOR_ATTRIBUTE);
                if (impersonatorCredentials != null) {
                    // there were impersonator credentials supplied -> will use them
                    impersonatorName = impersonatorCredentials.getUserID();
                    impersonatorPass = impersonatorCredentials.getPassword();
                }
            } else {
                callbacks = new Callback[] { new NameCallback("name?"), new PasswordCallback("pass?", false)};
                callbackHandler.handle(callbacks);

                name = ((NameCallback) callbacks[0]).getName();
                pass = ((PasswordCallback) callbacks[1]).getPassword();
            }
            if (name != null) {
                if (SYSTEM.equals(name)) {
                    String key = new String(pass);
                    Token token = removeToken(name, key);
                    if (token != null) {
                        principals.add(new JahiaPrincipal(SYSTEM, realm, true, false));
                        principals.add(new SystemPrincipal());
                    }
                } else if (name.startsWith(SYSTEM)) {
                    String key = new String(pass);
                    Token token = removeToken(name, key);
                    if (token != null) {
                        principals.add(new JahiaPrincipal(name.substring(SYSTEM.length()), realm, true, false));
                        principals.add(new SystemPrincipal());
                    }
                } else if (GUEST.equals(name)) {
                    principals.add(new JahiaPrincipal(GUEST, null, false, true));
                    principals.add(new AnonymousPrincipal());
                } else {
                    String key = new String(impersonatorPass != null ? impersonatorPass : pass);
                    String lookupUser = impersonatorName != null ? impersonatorName : name;
                    Token token = removeToken(lookupUser, key);

                    boolean ok = token != null;
                    JCRUserNode user = null;
                    if (!ok) {
                        user = JahiaUserManagerService.getInstance().lookupUser(lookupUser);
                        ok = user != null && user.verifyPassword(key);
                    }
                    if (ok && impersonatorName != null) {
                        // ensure the impersonator is root
                        if (user == null) {
                            user = JahiaUserManagerService.getInstance().lookupUser(lookupUser);
                        }
                        if (user == null || !user.isRoot()) {
                            throw new FailedLoginException("Only root user credentials can be used as an impersonator.");
                        }
                        // ensure the user exists
                        if (!JahiaUserManagerService.getInstance().userExists(name)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("User {} is not known, a the guest will be used instead", name,
                                        impersonatorName);
                            }
                            principals.add(new JahiaPrincipal(GUEST, null, false, true));
                            ok = false;
                        }
                    }

                    if (ok) {
                        principals.add(new JahiaPrincipal(name, realm, false, false));
                        if (realm == null) {
                            if (JahiaGroupManagerService.getInstance().isAdminMember(name, null, null)) {
                                principals.add(new AdminPrincipal(name));
                            }
                        }
                    }
                }
                if (principals.isEmpty()) {
                    throw new FailedLoginException();
                }
            }
        } catch (UnsupportedCallbackException e) {
            // ignore
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return !principals.isEmpty();
    }

    public static Token removeToken(String name, String key) {
        if (systemPass.get(key) != null && systemPass.get(key).username.equals(name)) {
            return systemPass.remove(key);
        }
        return null;
    }

    public static Token getToken(String name, String key) {
        if (systemPass.get(key) != null && systemPass.get(key).username.equals(name)) {
            return systemPass.get(key);
        }
        return null;
    }

    @Override
    public boolean commit() throws LoginException {
        if (principals.isEmpty()) {
            return false;
        } else {
            // add a principals (authenticated identities) to the Subject
            subject.getPrincipals().addAll(principals);
            return true;
        }
    }

    @Override
    public boolean abort() throws LoginException {
        if (principals.isEmpty()) {
            return false;
        } else {
            logout();
        }
        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().removeAll(principals);
        principals.clear();
        return true;
    }

    private static String getSystemPass(String user, List<String> deniedPathes) {
        String p = idGen.nextIdentifier().toString();
        systemPass.put(p, new Token(user, deniedPathes));
        return p;
    }

    public static Credentials getSystemCredentials() {
        return getSystemCredentials(null,null,null);
    }

    /**
     * @deprecated Use method with realm
     */
    @Deprecated
    public static Credentials getSystemCredentials(String username) {
        logger.warn("Getting system credentials with empty realm for "+username);
        return getSystemCredentials(username, null, null);
    }

    public static Credentials getSystemCredentials(String username, String realm) {
        return getSystemCredentials(username, realm, null);
    }

    /**
     * @deprecated Use method with realm
     */
    @Deprecated
    public static Credentials getSystemCredentials(String username, List<String> deniedPathes) {
        logger.warn("Getting system credentials with empty realm for "+username);
        return getSystemCredentials(username, null, deniedPathes);
    }

    public static Credentials getSystemCredentials(String username, String realm, List<String> deniedPathes) {
        if (username == null) {
            return new SimpleCredentials(JahiaLoginModule.SYSTEM, getSystemPass(JahiaLoginModule.SYSTEM, deniedPathes).toCharArray());
        }
        String userID = JahiaLoginModule.SYSTEM + username;
        SimpleCredentials credentials = new SimpleCredentials(userID, getSystemPass(userID, deniedPathes).toCharArray());
        credentials.setAttribute(REALM_ATTRIBUTE, realm);
        return credentials;
    }

    public static Credentials getGuestCredentials() {
        return new SimpleCredentials(JahiaLoginModule.GUEST, new char[0]);
    }

    /**
     * @deprecated Use method with realm
     */
    @Deprecated
    public static Credentials getCredentials(String username) {
        logger.warn("Getting credentials with empty realm for "+username);
        return getCredentials(username, null, null);
    }

    public static Credentials getCredentials(String username, String realm) {
        return getCredentials(username, realm, null);
    }

    /**
     * @deprecated Use method with realm
     */
    @Deprecated
    public static Credentials getCredentials(String username, List<String> deniedPathes) {
        logger.warn("Getting credentials with empty realm for "+username);
        return getCredentials(username, null, deniedPathes);
    }

    public static Credentials getCredentials(String username, String realm, List<String> deniedPathes) {
        String userID = username;
        SimpleCredentials credentials = new SimpleCredentials(userID, getSystemPass(userID, deniedPathes).toCharArray());
        credentials.setAttribute(REALM_ATTRIBUTE, realm);
        return credentials;
    }

    public static class Token {
        public String username;
        public List<String> deniedPath;

        Token(String username, List<String> deniedPath) {
            this.username = username;
            this.deniedPath = deniedPath;
        }
    }
}