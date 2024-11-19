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
package org.apache.jackrabbit.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.util.Base64;
import org.apache.jackrabbit.webdav.DavConstants;

/**
 * Credentials provider that extracts the credentials information from the "Authorization" header (basic authentication type) and also
 * supports impersonation.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaBasicCredentialsProvider extends BasicCredentialsProvider {

    public static final String IMPERSONATOR = " impersonator ";

    public JahiaBasicCredentialsProvider(String defaultHeaderValue) {
        super(defaultHeaderValue);
    }

    @Override
    public Credentials getCredentials(HttpServletRequest request) throws LoginException, ServletException {
        String authHeader = request.getHeader(DavConstants.HEADER_AUTHORIZATION);
        if (authHeader != null) {
            try {
                String[] authStr = authHeader.split(" ");
                if (authStr.length >= 2 && authStr[0].equalsIgnoreCase(HttpServletRequest.BASIC_AUTH)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Base64.decode(authStr[1].toCharArray(), out);
                    String decAuthStr = out.toString("ISO-8859-1");
                    int pos = decAuthStr.indexOf(':');
                    String userid = decAuthStr.substring(0, pos);
                    String passwd = decAuthStr.substring(pos + 1);
                    return createCredentials(userid, passwd.toCharArray());
                }
                throw new ServletException("Unable to decode authorization.");
            } catch (IOException e) {
                throw new ServletException("Unable to decode authorization: " + e.toString());
            }
        }

        return super.getCredentials(request);
    }

    /**
     * Creates the {@link SimpleCredentials} object for the provided username and password considering the impersonation case.
     *
     * @param user
     *            the received username
     * @param password
     *            the user password
     * @return the {@link SimpleCredentials} object for the provided username and password considering the impersonation case
     */
    protected Credentials createCredentials(String user, char[] password) {
        SimpleCredentials credentials = null;
        if (user != null && user.contains(IMPERSONATOR)) {
            credentials = new SimpleCredentials(StringUtils.substringBefore(user, IMPERSONATOR),
                    ArrayUtils.EMPTY_CHAR_ARRAY);

            credentials.setAttribute(SecurityConstants.IMPERSONATOR_ATTRIBUTE,
                    new SimpleCredentials(StringUtils.substringAfter(user, IMPERSONATOR), password));
        } else {
            credentials = new SimpleCredentials(user, password);
        }

        return credentials;
    }

}
