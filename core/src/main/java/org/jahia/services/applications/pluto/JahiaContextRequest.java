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
package org.jahia.services.applications.pluto;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

/**
 * 
 * User: Serge Huber
 * Date: 29 juil. 2008
 * Time: 14:48:33
 * 
 */
public class JahiaContextRequest extends HttpServletRequestWrapper {

    private JahiaUser jahiaUser;
    private String workspaceName;
    private String pathInfo = null;
    private String queryString = null;
    private String servletPath = null;
    private EntryPointInstance entryPointInstance;

    public JahiaContextRequest(JahiaUser jahiaUser, HttpServletRequest httpServletRequest, String workspaceName) throws JahiaException {
        super(httpServletRequest);
        this.jahiaUser = jahiaUser;
        this.workspaceName = workspaceName;
        if (Jahia.getServletPath() != null && !Jahia.getServletPath().equals(getServletPath())) {
            setServletPath(Jahia.getServletPath());
            setPathInfo(httpServletRequest.getPathInfo());
            setQueryString(httpServletRequest.getQueryString());
        }
    }

    public String getServletPath() {
        if (servletPath == null) {
            return super.getServletPath();
        } else {
            return servletPath;
        }
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public String getPathInfo() {
        if (pathInfo == null) {
            return super.getPathInfo();
        } else {
            return pathInfo;
        }
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getQueryString() {
        if (queryString == null) {
            return super.getQueryString();
        } else {
            return queryString;
        }
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRequestURI() {
        return getContextPath() + getServletPath() + getPathInfo();
    }

    public String getRemoteUser() {
        return jahiaUser.getUserKey();
    }

    public Principal getUserPrincipal() {
        return jahiaUser;
    }

    public void setEntryPointInstance(EntryPointInstance entryPointInstance) {
        this.entryPointInstance = entryPointInstance;
    }

    public boolean isUserInRole(String role) {
        // This method maps servlet roles on Jahia's groups
        if (entryPointInstance == null) {
            return false;
        }
        return entryPointInstance.isUserInRole(jahiaUser, role, workspaceName);
    }
}
