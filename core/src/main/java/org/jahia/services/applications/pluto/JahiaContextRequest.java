/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
