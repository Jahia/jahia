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
package org.jahia.jaas;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.IdentifierGeneratorFactory;
import org.jahia.api.user.JahiaUserService;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.security.Principal;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 27 févr. 2006
 * Time: 12:00:21
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLoginModule implements LoginModule {
    public static final String SYSTEM = " system ";
    public static final String GUEST = " guest ";

    private static IdentifierGenerator idGen = IdentifierGeneratorFactory.newInstance().uuidVersionFourGenerator();
    private static Map systemPass = new HashMap();

    private static JahiaUserService userService;

    private Subject subject;
    private Principal user = null;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    public boolean login() throws LoginException {
        try {
            Callback[] callbacks = new Callback[] { new NameCallback("name?"), new PasswordCallback("pass?",false) } ;
            callbackHandler.handle(callbacks);

            String name = ((NameCallback)callbacks[0]).getName();
            char[] pass = ((PasswordCallback)callbacks[1]).getPassword();
            if (name != null) {
                if (SYSTEM.equals(name)) {
                    String key = new String(pass);
                    if (key.equals(systemPass.get(name))) {
//                        systemPass.remove(key);
                        user = new JahiaPrincipal(SYSTEM, true, false);
                    }
                } else if (name.startsWith(SYSTEM)) {
                    String key = new String(pass);
                    if (key.equals(systemPass.get(name))) {
//                        systemPass.remove(key);
                        user = new JahiaPrincipal(name.substring(SYSTEM.length()), true, false);
                    }
                } else if (GUEST.equals(name)) {
                    user = new JahiaPrincipal(GUEST, false, true);
                } else {
                    String key = new String(pass);
                    if (key.equals(systemPass.get(name)) || getUserService().checkPassword(name,key)) {
//                        systemPass.remove(key);
                        user = new JahiaPrincipal(name);
                    }
                }
                if (user == null) {
                    throw new FailedLoginException();
                }
            }
        } catch (UnsupportedCallbackException e) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user != null;
    }

    public boolean commit() throws LoginException {
        if (user == null) {
            return false;
        } else {
            // add a principals (authenticated identities) to the Subject
            subject.getPrincipals().add(user);
            return true;
        }
    }

    public boolean abort() throws LoginException {
        if (user == null) {
            return false;
        } else {
            logout();
        }
        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(user);
        user = null;
        return true;
    }

    private static synchronized String getSystemPass(String user) {
        if (systemPass.containsKey(user)) {
            return (String) systemPass.get(user);
        }
        String p = idGen.nextIdentifier().toString();
        systemPass.put( user, p);
        return p;
    }

    public static Credentials getSystemCredentials() {
        return new SimpleCredentials(JahiaLoginModule.SYSTEM, getSystemPass(JahiaLoginModule.SYSTEM).toCharArray());
    }

    public static Credentials getSystemCredentials(String username) {
        if (username == null) {
            return getSystemCredentials();
        }
        return new SimpleCredentials(JahiaLoginModule.SYSTEM + username, getSystemPass(JahiaLoginModule.SYSTEM + username).toCharArray());
    }

    public static Credentials getGuestCredentials() {
        return new SimpleCredentials(JahiaLoginModule.GUEST, new char[0]);
    }

    public static Credentials getCredentials(String username) {
        return new SimpleCredentials(username, getSystemPass(username).toCharArray());
    }

//    public JahiaUserService getJahiaUserService() throws NamingException {
//        String serviceName = "jahia/users";
//        Hashtable env = new Hashtable();
//        InitialContext initctx = new InitialContext(env);
//        try {
//            Context ctx = (Context) initctx.lookup("java:comp/env");
//            return (JahiaUserService) ctx.lookup(serviceName);
//        } catch (NamingException e) {
//            return (JahiaUserService) initctx.lookup("java:jahia/users");
//        }
//    }

    public static JahiaUserService getUserService() {
        return userService;
    }

    public static void setUserService(JahiaUserService userService) {
        JahiaLoginModule.userService = userService;
    }
}
