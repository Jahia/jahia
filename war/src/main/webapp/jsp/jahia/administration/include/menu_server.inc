<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@page language="java" %>
<%@page import="java.util.*" %>
<%@page import="org.jahia.services.acl.JahiaACLManagerService" %>
<%@page import="org.jahia.params.ParamBean" %>
<%@page import="org.jahia.registries.ServicesRegistry" %>
<%@page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@page import="org.jahia.services.lock.LockPrerequisites" %>
<%@page import="org.jahia.services.lock.LockPrerequisitesResult" %>
<%@page import="org.jahia.services.lock.LockKey" %>
<%@page import="org.jahia.services.usermanager.JahiaUser" %>
<%
    final JahiaUser user = jData.getProcessingContext().getUser();
    final int currentSiteID = jData.getProcessingContext().getSiteID();
    JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
    ParamBean mnujParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    Integer menuSiteID = (Integer) request.getAttribute("siteID");
    if (menuSiteID == null) {
        menuSiteID = new Integer(-1);
    }
%>
<div class="leftMenu">
    <ul>
        <li class="section first">
            <span>Main Menu</span>
        </li>
        <li class="item selected">
            <% if (aclService.getServerActionPermission("admin.sites.ManageSites", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>"
               class="set-server ico-server-site<% if ("sites".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.virtualSitesManagement.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-site"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.virtualSitesManagement.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.categories.ManageCategories", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"categories","&sub=display")%>"
               class="set-server ico-server-category<% if ("categories".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.manageCategories.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-category"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.manageCategories.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.server.ManageServer", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"server","&sub=display")%>"
               class="set-server ico-server-setting<% if ("server".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.emailSettings.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-setting"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.emailSettings.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.components.ManageShareComponents", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>"
               class="set-server ico-server-component<% if ("sharecomponents".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.manageComponents.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-component"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.manageComponents.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.audit.ManageLogs", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"logs","&sub=settings")%>"
               class="set-server ico-server-log<% if ("logs".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.administrativeAuditLog.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-log"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.administrativeAuditLog.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.users.ManageSuperAdminProps", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=superAdminProps")%>"
               class="set-server ico-server-admin<% if ("users".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.editSuperAdminProperties.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-admin"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.editSuperAdminProperties.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.status.ManageStatus", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"status","&sub=display")%>"
               class="set-server ico-server-cache<% if ("status".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.serverStatus.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-cache"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.serverStatus.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.permissions.ManageServerPermissions", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"serverPermissions","&sub=display")%>"
               class="set-server ico-server-permissions<% if ("serverPermissions".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.serverpermissions.mainMenu.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-permissions"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.serverpermissions.mainMenu.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.pwdpolicy.ManagePasswordPolicies", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"passwordPolicies","&sub=display")%>"
               class="set-server ico-server-password<% if ("passwordPolicies".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.passwordPolicies.mainMenu.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-password"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.passwordPolicies.mainMenu.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.server.ViewPatches", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"patches","")%>"
               class="set-server ico-server-patch<% if ("patches".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.patchmanagement.mainMenu.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-patch"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.patchmanagement.mainMenu.label"/></span><% } %>
        </li>
        <li class="item">
            <% if (aclService.getServerActionPermission("admin.info.LicenseInfo", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"info","")%>"
               class="set-server ico-server-info<% if ("info".equals(request.getParameter("do"))) { %> selected<% } %>"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.aboutJahia.label"/></a>
            <% } else { %>
            <span class="set-server-disabled ico-server-info"><internal:adminResourceBundle
                    resourceName="org.jahia.admin.aboutJahia.label"/></span><% } %>
        </li>

    </ul>
</div>