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
<%@page import="org.jahia.params.ParamBean,
                    org.jahia.params.ProcessingContext,
                    org.jahia.services.sites.JahiaSite,
                    org.jahia.services.sites.JahiaSitesService,
                    org.jahia.registries.ServicesRegistry" 
%><%
    JahiaSitesService siteService = ServicesRegistry.getInstance().getJahiaSitesService();
    JahiaSite theSite = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);
    if (theSite != null && theSite.getID() == -1) {
        theSite = null;
    }
    Integer siteID = null;
    if (theSite == null) {
        siteID = jData.getProcessingContext().getSiteID();
        if (siteID != null && siteID.intValue() > 0) {
            theSite = siteService.getSite(siteID.intValue());
        }
    }
        
    if (theSite == null && siteService.getNbSites() > 0) {
        theSite = siteService.getDefaultSite();
    }
    siteID = theSite != null ? theSite.getID() : null;
    
    //Boolean configJahia = (Boolean) request.getAttribute("configJahia");
    Integer daysLeft = (Integer) request.getAttribute("daysLeft");
%>
<div class="dex-TabBar">
    <ul class="dex-TabBarItem-wrapper" style="width: 70%;">
        <li class="dex-TabBarFirst">
            <div>
                &nbsp;
            </div>
        </li>
        <li class="dex-TabBarItem<% if (stretcherToOpen == 0) { %>-selected<% } %>">
            <div>
                <div>
          <span class="tab-icon ico-server">
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","&sub=server")%>"><fmt:message key="org.jahia.admin.serverSettings.label"/></a>
          </span>
                </div>
            </div>
        </li>
        <% if (theSite != null) {
            String siteName = theSite.getTitle();;
        %>
        <li class="dex-TabBarItem<% if (stretcherToOpen == 1) { %>-selected<% } %>">
            <div>
                <div>
          <span class="tab-icon ico-site">
            <a href="<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","&sub=site")%>"><fmt:message key="org.jahia.admin.siteSettings.label"/>: <span id="siteName"><%=siteName%></span></a>
          </span>
                </div>
            </div>
        </li>

        <li class="dex-TabBarItem tabExit">
            <div>
                <div>
                    <span class="tab-icon ico-quit"><a href='<%=Jahia.getContextPath() + Jahia.getServletPath() + "/" + ParamBean.SITE_KEY_PARAMETER + "/" + theSite.getSiteKey()%>'
                       title="<fmt:message key='org.jahia.admin.exit.label'/>" ><fmt:message key="org.jahia.admin.exit.label"/></a></span>
                </div>
            </div>
        </li>
        <% } %>
        <li class="dex-TabBarRest">
            <div>
                &nbsp;
            </div>
        </li>
    </ul>
    <!-- License expiration message -->
    <ul class="dex-license-wrapper" style="width: 30%;">
        <li>
            <% if (daysLeft != null) { %>
            <fmt:message key="org.jahia.admin.daysLeftWarning.label"/>&nbsp;:&nbsp;<%=daysLeft %>
            <% } else { %>
            &nbsp;
            <% } %>
        </li>
    </ul>
</div>
