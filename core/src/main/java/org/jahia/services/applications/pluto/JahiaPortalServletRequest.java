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

package org.jahia.services.applications.pluto;

import java.security.Principal;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.data.applications.EntryPointInstance;
import org.apache.pluto.container.PortletWindow;

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
