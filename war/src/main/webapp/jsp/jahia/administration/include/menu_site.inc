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
    Integer menuSiteID = (Integer) request.getAttribute("siteID");
    if (menuSiteID == null) {
        menuSiteID = new Integer(-1);
    }
%>
<div class="leftMenu">
<ul>
    <li class="section first"><span>Main Menu</span></li>
    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.pages.ManagePages", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=JahiaAdministration.composeActionURL(request,response,"pages","&sub=display")%>"
           class="set-site ico-site-page<% if ("pages".equals(request.getParameter("do"))) { %> selected<% } %>">
            <internal:adminResourceBundle resourceName="org.jahia.admin.pageSettings.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-page"><internal:adminResourceBundle
                resourceName="org.jahia.admin.pageSettings.label"/></span>
        <% } %>
    </li>
    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.users.ManageUsers", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=display")%>"
           class="set-site ico-site-user<% if ("users".equals(request.getParameter("do"))) { %> selected<% } %>">
            <internal:adminResourceBundle resourceName="org.jahia.admin.manageUsers.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-user"><internal:adminResourceBundle
                resourceName="org.jahia.admin.manageUsers.label"/></span>
        <% } %>
    </li>
    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.users.ManageGroups", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=display")%>"
           class="set-site ico-site-group<% if ("groups".equals(request.getParameter("do"))) { %> selected<% } %>">
            <internal:adminResourceBundle resourceName="org.jahia.admin.manageGroups.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-group"><internal:adminResourceBundle
                resourceName="org.jahia.admin.manageGroups.label"/></span>
        <% } %>
    </li>
    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.templates.ManageTemplates", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=display")%>"
           class="set-site ico-site-template<% if ("templates".equals(request.getParameter("do"))) { %> selected<% } %>">
            <internal:adminResourceBundle resourceName="org.jahia.admin.manageTemplates.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-template"><internal:adminResourceBundle
                resourceName="org.jahia.admin.manageTemplates.label"/></span>
        <% } %>
    </li>

    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.search.ManageSearch", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=JahiaAdministration.composeActionURL(request,response,"search","&sub=display")%>"
           class="set-site ico-site-search<% if ("search".equals(request.getParameter("do"))) { %> selected<% } %>">
            <internal:adminResourceBundle resourceName="org.jahia.admin.manageSearchEngine.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-search"><internal:adminResourceBundle
                resourceName="org.jahia.admin.manageSearchEngine.label"/></span>
        <% } %>
    </li>
    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.languages.ManageSiteLanguages", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=display")%>"
           class="set-site ico-site-lang<% if ("siteLanguages".equals(request.getParameter("do"))) { %> selected<% } %>">
            <internal:adminResourceBundle resourceName="org.jahia.admin.manageLanguages.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-lang"><internal:adminResourceBundle
                resourceName="org.jahia.admin.manageLanguages.label"/></span>
        <% } %>
    </li>

    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.production.ManageSiteProduction", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=JahiaAdministration.composeActionURL(request,response,"productionManager","&sub=display")%>"
           class="set-site ico-site-export<% if ("productionManager".equals(request.getParameter("do"))) { %> selected<% } %>">
            <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-export"><internal:adminResourceBundle
                resourceName="org.jahia.admin.productionManager.label"/></span>
        <% } %>
    </li>

    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.permissions.ManageSitePermissions", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=JahiaAdministration.composeActionURL(request,response,"sitePermissions","&sub=display")%>"
           class="set-site ico-site-permission<% if ("sitePermissions".equals(request.getParameter("do"))) { %> selected<% } %>">
            <internal:adminResourceBundle resourceName="org.jahia.admin.sitepermissions.mainMenu.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-permission"><internal:adminResourceBundle
                resourceName="org.jahia.admin.sitepermissions.mainMenu.label"/></span>
        <% } %>
    </li>

    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.htmleditors.HtmlEditorsAdmin", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=jData.getProcessingContext().composeStrutsUrl("HtmlSettings","&group=htmlsettings&method=display")%>"
           class="set-site ico-site-html ${param.group == 'htmlsettings' ? 'selected' : ''}">
            <internal:adminResourceBundle resourceName="org.jahia.admin.htmlsettings.label" defaultValue="org.jahia.admin.htmlsettings.label"/>
        </a>
        <% } else { %>

        <span class="set-site-disabled ico-site-html"><internal:adminResourceBundle
                resourceName="org.jahia.admin.htmlsettings.label" defaultValue="org.jahia.admin.htmlsettings.label"/></span>
        <% } %>
    </li>

    <li class="item">
        <% if (aclService.getSiteActionPermission("admin.htmleditors.HtmlEditorsAdmin", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=jData.getProcessingContext().composeStrutsUrl("IntegrityChecks","&group=integrity&method=display")%>"
           class="set-site ico-site-integrity ${param.group == 'integrity' ? 'selected' : ''}">
            <internal:adminResourceBundle resourceName="org.jahia.admin.integrity.label" defaultValue="org.jahia.admin.integrity.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-integrity"><internal:adminResourceBundle
                resourceName="org.jahia.admin.integrity.label" defaultValue="org.jahia.admin.integrity.label"/></span>
        <% } %>
    </li>

     <li class="item">
        <% if (aclService.getSiteActionPermission("admin.analytics.ManageAnalytics", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
        <a href="<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=display")%>"
           class="set-site ico-site-analytics<% if ("analytics".equals(request.getParameter("do"))) { %> selected<% } %>">
            <internal:adminResourceBundle resourceName="org.jahia.admin.analytics.ManageAnalytics.label"/>
        </a>
        <% } else { %>
        <span class="set-site-disabled ico-site-analytics"><internal:adminResourceBundle
                resourceName="org.jahia.admin.analytics.ManageAnalytics.label"/></span>
        <% } %>
    </li>

    <li class="item">
       <% if (aclService.getSiteActionPermission("admin.analytics.ManageThemes", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
       <a href="<%=JahiaAdministration.composeActionURL(request,response,"themes","&sub=display")%>"
          class="set-site ico-site-themes<% if ("themes".equals(request.getParameter("do"))) { %> selected<% } %>">
           <internal:adminResourceBundle resourceName="org.jahia.admin.themes.ManageThemes.label"/>
       </a>
       <% } else { %>
       <span class="set-site-disabled ico-site-themes"><internal:adminResourceBundle
               resourceName="org.jahia.admin.themes.ManageThemes.label"/></span>
       <% } %>
   </li>


</ul>
</div>