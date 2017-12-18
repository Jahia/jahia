/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
}
