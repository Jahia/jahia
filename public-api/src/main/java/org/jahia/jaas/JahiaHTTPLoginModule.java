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

package org.jahia.jaas;

import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.*;
import java.util.Map;
import java.security.Principal;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

/**
 * TODO Comment me
 *
 * @author toto
 */
public class JahiaHTTPLoginModule  implements LoginModule {

    private Subject subject;
    private Principal user = null;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;

    /**
     * Initialize this LoginModule.
     * <p/>
     * <p> This method is called by the <code>LoginContext</code>
     * after this <code>LoginModule</code> has been instantiated.
     * The purpose of this method is to initialize this
     * <code>LoginModule</code> with the relevant information.
     * If this <code>LoginModule</code> does not understand
     * any of the data stored in <code>sharedState</code> or
     * <code>options</code> parameters, they can be ignored.
     * <p/>
     * <p/>
     *
     * @param subject         the <code>Subject</code> to be authenticated. <p>
     * @param callbackHandler a <code>CallbackHandler</code> for communicating
     *                        with the end user (prompting for usernames and
     *                        passwords, for example). <p>
     * @param sharedState     state shared with other configured LoginModules. <p>
     * @param options         options specified in the login
     *                        <code>Configuration</code> for this particular
     *                        <code>LoginModule</code>.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    /**
     * Method to authenticate a <code>Subject</code> (phase 1).
     * <p/>
     * <p> The implementation of this method authenticates
     * a <code>Subject</code>.  For example, it may prompt for
     * <code>Subject</code> information such
     * as a username and password and then attempt to verify the password.
     * This method saves the result of the authentication attempt
     * as private state within the LoginModule.
     * <p/>
     * <p/>
     *
     * @return true if the authentication succeeded, or false if this
     *         <code>LoginModule</code> should be ignored.
     * @throws javax.security.auth.login.LoginException
     *          if the authentication fails
     */
    public boolean login() throws LoginException {
        try {
            Callback[] callbacks = new Callback[] { new NameCallback("name?"), new PasswordCallback("pass?",false) } ;
            callbackHandler.handle(callbacks);

            final String name = ((NameCallback)callbacks[0]).getName();
            char[] pass = ((PasswordCallback)callbacks[1]).getPassword();
            System.out.println("name="+name+",pass="+new String(pass));

            URL url = new URL(options.get("url")+"?user="+ URLEncoder.encode(name,"UTF-8") + "&pass=" + URLEncoder.encode(new String(pass),"UTF-8"));
            int i = ((HttpURLConnection)url.openConnection()).getResponseCode();
            if (i == 200) {
                user = new Principal() {
                    /**
                     * Returns the name of this principal.
                     *
                     * @return the name of this principal.
                     */
                    public String getName() {
                        return name;
                    }
                };
            }
        } catch (UnsupportedCallbackException e) {
            // ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user != null;

    }

    /**
     * Method to commit the authentication process (phase 2).
     * <p/>
     * <p> This method is called if the LoginContext's
     * overall authentication succeeded
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * succeeded).
     * <p/>
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method associates relevant
     * Principals and Credentials with the <code>Subject</code> located in the
     * <code>LoginModule</code>.  If this LoginModule's own
     * authentication attempted failed, then this method removes/destroys
     * any state that was originally saved.
     * <p/>
     * <p/>
     *
     * @return true if this method succeeded, or false if this
     *         <code>LoginModule</code> should be ignored.
     * @throws javax.security.auth.login.LoginException
     *          if the commit fails
     */
    public boolean commit() throws LoginException {
        if (user == null) {
            return false;
        } else {
            // add a principals (authenticated identities) to the Subject
            subject.getPrincipals().add(user);
            return true;
        }
    }

    /**
     * Method to abort the authentication process (phase 2).
     * <p/>
     * <p> This method is called if the LoginContext's
     * overall authentication failed.
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * did not succeed).
     * <p/>
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method cleans up any state
     * that was originally saved.
     * <p/>
     * <p/>
     *
     * @return true if this method succeeded, or false if this
     *         <code>LoginModule</code> should be ignored.
     * @throws javax.security.auth.login.LoginException
     *          if the abort fails
     */
    public boolean abort() throws LoginException {
        if (user == null) {
            return false;
        } else {
            logout();
        }
        return true;
    }

    /**
     * Method which logs out a <code>Subject</code>.
     * <p/>
     * <p>An implementation of this method might remove/destroy a Subject's
     * Principals and Credentials.
     * <p/>
     * <p/>
     *
     * @return true if this method succeeded, or false if this
     *         <code>LoginModule</code> should be ignored.
     * @throws javax.security.auth.login.LoginException
     *          if the logout fails
     */
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(user);
        user = null;
        return true;
    }
}
