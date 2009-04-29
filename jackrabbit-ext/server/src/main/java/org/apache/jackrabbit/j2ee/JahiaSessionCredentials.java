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
package org.apache.jackrabbit.j2ee;

import org.apache.jackrabbit.server.BasicCredentialsProvider;
import org.jahia.jaas.JahiaLoginModule;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 27 déc. 2007
 * Time: 19:30:31
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSessionCredentials extends BasicCredentialsProvider {
    public JahiaSessionCredentials(String s) {
        super(s);
    }

    public Credentials getCredentials(HttpServletRequest request) throws LoginException, ServletException {
        SimpleCredentials c = (SimpleCredentials) super.getCredentials(request);

        if (c != null) {
            return c;
        }

        Principal jahiaUser = (Principal) request.getSession(true).getAttribute("org.jahia.usermanager.jahiauser");
        if (jahiaUser != null) {
            String n = jahiaUser.getName();
            // principal.getName should return username, but returns userkey now
            if (n.startsWith("{")) {
                n = n.substring(n.indexOf('}')+1);
            } else if (n.contains(":")) {
                n = n.substring(0, n.indexOf(':'));
            }
            request.setAttribute("isGuest", Boolean.FALSE);
            return JahiaLoginModule.getCredentials(n);
        }
        request.setAttribute("isGuest", Boolean.TRUE);
        return JahiaLoginModule.getGuestCredentials();
    }

}
