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
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.j2ee;

import org.apache.jackrabbit.server.BasicCredentialsProvider;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * User: toto
 * Date: 27 déc. 2007
 * Time: 19:30:31
 * 
 */
public class JahiaSessionCredentials extends BasicCredentialsProvider {

    /**
     * Constructs a new JahiaSessionCredentials with {@link BasicCredentialsProvider#EMPTY_DEFAULT_HEADER_VALUE}
     * as the default header value.
     */
    public JahiaSessionCredentials() {
        this(EMPTY_DEFAULT_HEADER_VALUE);
    }

    public JahiaSessionCredentials(String defaultHeaderValue) {
        super(defaultHeaderValue);
    }

    @Override
    public Credentials getCredentials(HttpServletRequest request) throws LoginException, ServletException {
        JahiaUser jahiaUser = (JahiaUser) request.getSession(true).getAttribute("org.jahia.usermanager.jahiauser");
        if (jahiaUser != null) {
            request.setAttribute("isGuest", Boolean.FALSE);
            return JahiaLoginModule.getCredentials(jahiaUser.getName(), jahiaUser.getRealm());
        } else {
            SimpleCredentials c = (SimpleCredentials) super.getCredentials(request);
            if (c != null) {
                return c;
            }            
        }
        request.setAttribute("isGuest", Boolean.TRUE);
        return JahiaLoginModule.getGuestCredentials();
    }

}
