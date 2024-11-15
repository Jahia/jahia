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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.params.valves;

import org.jahia.services.content.JCRSessionFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: toto
 * Date: May 14, 2010
 * Time: 11:18:26 AM
 */
public class AuthValveContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JCRSessionFactory sessionFactory;
    private boolean authRetrievedFromSession = false;
    private boolean shouldStoreAuthInSession = true;

    public AuthValveContext(HttpServletRequest request, HttpServletResponse response, JCRSessionFactory sessionFactory) {
        this.request = request;
        this.response = response;
        this.sessionFactory = sessionFactory;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * This method will return true if the auth was indeed retrieved by the session.
     * It is the responsability of the valve to set this value using the setAuthRetrievedFromSession method
     * @return true if the authentifcation was retrieved from the HttpSession object, false otherwise
     */
    public boolean isAuthRetrievedFromSession() {
        return authRetrievedFromSession;
    }

    /**
     * A valve that resolves the authentication from an HttpSession attribute should set this value to true.
     * This value will then be used by the JCRSessionFilter and possibly other classes to avoid setting the session
     * attribute once again if the value was read from the session.
     * @param authRetrievedFromSession set to true if the authentication was read from the session, false otherwise.
     *                                 if not yet the default value is false.
     */
    public void setAuthRetrievedFromSession(boolean authRetrievedFromSession) {
        this.authRetrievedFromSession = authRetrievedFromSession;
    }

    public boolean isShouldStoreAuthInSession() {
        return shouldStoreAuthInSession;
    }

    public void setShouldStoreAuthInSession(boolean shouldStoreAuthInSession) {
        this.shouldStoreAuthInSession = shouldStoreAuthInSession;
    }
}
