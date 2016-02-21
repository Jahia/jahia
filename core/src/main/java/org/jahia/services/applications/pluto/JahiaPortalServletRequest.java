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

import org.apache.pluto.container.PortletWindow;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;
import java.util.Locale;

/**
 * 
 * User: Serge Huber
 * Date: 30 juil. 2008
 * Time: 12:27:46
 * 
 */
public class JahiaPortalServletRequest extends HttpServletRequestWrapper {
    
    private JahiaUser jahiaUser;
    private EntryPointInstance entryPointInstance;
    private String id;
    private String workspaceName;
    private Locale locale;

    public JahiaPortalServletRequest(EntryPointInstance entryPointInstance,JahiaUser jahiaUser, HttpServletRequest request, PortletWindow window, String workspaceName) {
        super(request);
        this.jahiaUser = jahiaUser;
        this.entryPointInstance = entryPointInstance;
        this.id = window.getId().getStringId();
        this.workspaceName = workspaceName;
        RenderContext ctx = (RenderContext) request.getAttribute("renderContext");
        this.locale = ctx != null && ctx.getMainResource() != null ? ctx.getMainResourceLocale() : null; 
    }

    public String getRemoteUser() {
        return jahiaUser.getUserKey();
    }

    public Principal getUserPrincipal() {
        return jahiaUser;
    }

    public String getId() {
        return id;
    }

    public boolean isUserInRole(String role) {
        // This method maps servlet roles on Jahia's groups
        if (entryPointInstance == null) {
            return false;
        }
        return entryPointInstance.isUserInRole(jahiaUser, role, workspaceName);
    }
    
    @Override
    public Locale getLocale() {
        return locale != null ? locale : super.getLocale();
    }
}
