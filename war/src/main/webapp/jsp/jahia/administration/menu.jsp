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

<%@include file="/jsp/jahia/administration/include/header.inc" %>
<%@page import="java.util.*" %>
<%@page import="org.jahia.bin.*" %>
<%@page import="org.jahia.utils.JahiaTools" %>
<%@page import="org.jahia.services.sites.JahiaSite" %>
<%@page import="org.jahia.data.JahiaData" %>
<%@page import="org.jahia.data.beans.MenuItem" %>
<%@page import="org.jahia.services.acl.JahiaACLManagerService" %>
<%@page import="org.jahia.registries.ServicesRegistry" %>
<%@page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@page import="org.jahia.params.ParamBean" %>
<%@page import="org.jahia.settings.SettingsBean" %>
<%@page import="org.jahia.services.usermanager.JahiaUser" %>
<%!
    private static JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
    private boolean isServerActionAllowed(String action, JahiaUser user, int currentSiteID) {
        return aclService.getServerActionPermission(action, user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0;
    }
    private boolean isSiteActionAllowed(String action, JahiaUser user, int currentSiteID) {
        return aclService.getSiteActionPermission(action, user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0;
    }
%>
<%

    Boolean isSuperAdmin = (Boolean) request.getAttribute("isSuperAdmin");
    if (isSuperAdmin == null) {
        isSuperAdmin = new Boolean(false);
    }
    Boolean configJahia = (Boolean) request.getAttribute("configJahia");
    List sitesList = (List) request.getAttribute("sitesList");

    String sub = (String) request.getParameter("sub");

    if (sitesList == null) {
        sitesList = new ArrayList();
    }
    Iterator sitesEnum = sitesList.iterator();
    Iterator sitesJavaScript = sitesList.iterator();

    if (sub != null && !"".equals(sub)) {
        if (sub.equals("server")) {
            stretcherToOpen = 0;
        } else {
            stretcherToOpen = 1;
        }

    } else {
        stretcherToOpen = configJahia != null && configJahia.booleanValue() ? 0 : 1;
    }

    ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final JahiaUser user = jData.getProcessingContext().getUser();
    final int currentSiteID = jData.getProcessingContext().getSiteID();
%>
<script type="text/javascript" src="<%=URL%>../javascript/moo/prototype.lite.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/moo/moo.fx.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/moo/moo.fx.pack.js"></script>


<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.jahiaAdministration.label"/></h2>

</div>

<table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td style="vertical-align: top;" align="left">
    <%@ include file="include/tab_menu.inc"%>
    </td>
</tr>
<tr>
<td style="vertical-align: top;" align="left" height="100%">
<div class="dex-TabPanelBottom">

<% if (stretcherToOpen == 0) { %>
<%@include file="include/main_menu_server.inc"%>
<%} %>

<% if (stretcherToOpen > 0) { %>

    <%@include file="include/main_menu_site.inc"%> 

<%
}
%>


</td>
</tr>
</tbody>
</table>
</div>

<%@include file="/jsp/jahia/administration/include/footer.inc" %>
