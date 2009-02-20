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

<%@page language = "java" %>
<%@page import = "org.jahia.utils.JahiaTools"%>
<%@page import = "org.jahia.data.JahiaData"%>
<%@page import = "org.jahia.registries.ServicesRegistry"%>
<%@page import = "org.jahia.services.usermanager.JahiaUser"%>
<%@page import = "java.util.*"%>
<%@page import="org.jahia.bin.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<jsp:useBean id="userErrorMessage" class="java.lang.String" scope="session"/>

<%
JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");    
String selectedUsers = (String)request.getAttribute("selectedUsers");
    JahiaUser selUser = (JahiaUser)ServicesRegistry.getInstance().
    getJahiaUserManagerService().lookupUserByKey(selectedUsers.substring(1));
    int stretcherToOpen   = 1;
    
%>

<!-- Adiministration page position -->
<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageUsers.removeUser.label"/></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
    cellspacing="0">
    <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/jsp/jahia/administration/include/tab_menu.inc"%>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
            <div class="dex-TabPanelBottom">
            <div class="tabContent">
            <jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
                <jsp:param name="mode" value="site"/>
            </jsp:include>
            
            <div id="content" class="fit">
<div class="head">
    <div class="object-title">
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageUsers.removeSelectedUser.label"/>
    </div>
</div>
<div class="content-item"><!-- Remove user -->
<form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=processRemove")%>' method="post">
    <% if (selUser.getProviderName().indexOf("LDAP") == -1) { %>
        <p>
          <b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.username.label"/>&nbsp;:</b>&nbsp;&nbsp;&nbsp;<%= selUser.getUsername()%>
          <input type="hidden" name="username" value="<%= selUser.getUsername()%>">
        </p>
        <p>
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageUsers.definitivelyRemove.label"/>
        </p>
        <p>
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageUsers.areYouSure.label"/>
        </p>
        
        
        
    <% } else { %>
        <p>
          <b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageUsers.userName.label"/>&nbsp;:</b>&nbsp;&nbsp;&nbsp;<%= selUser.getUsername()%>
        </p>
        <p>
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.users.ManageUsers.cannotRemove.label"/>
        </p>
    <% } %>
</form>
</div>
	</div>		
			</td>
		</tr>
	</tbody>
</table>
</div>
  <div id="actionBar">
  	
    <span class="dex-PushButton"> 
      <span class="first-child">
         <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a>
      </span>
     </span>
     <% if (selUser.getProviderName().indexOf("LDAP") == -1) { %>     
     <span class="dex-PushButton"> 
      <span class="first-child">
         <a class="ico-cancel" href='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=display")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.cancel.label"/></a>
      </span>
     </span>
     <span class="dex-PushButton"> 
      <span class="first-child">
         <a class="ico-ok" href="javascript:document.mainForm.submit();" ><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.ok.label"/></a>
      </span>
     </span>      
      <% } %> 	      
  </div>

</div>

