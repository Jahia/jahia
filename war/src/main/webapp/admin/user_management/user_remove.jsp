<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
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
<h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageUsers.removeUser.label"/></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
    cellspacing="0">
    <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc"%>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
            <div class="dex-TabPanelBottom">
            <div class="tabContent">
            <jsp:include page="/admin/include/left_menu.jsp">
                <jsp:param name="mode" value="site"/>
            </jsp:include>
            
            <div id="content" class="fit">
<div class="head">
    <div class="object-title">
        <fmt:message key="org.jahia.admin.users.ManageUsers.removeSelectedUser.label"/>
    </div>
</div>
<div class="content-item"><!-- Remove user -->
<form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=processRemove")%>' method="post">
    <% if (selUser.getProviderName().indexOf("LDAP") == -1) { %>
        <p>
          <b><fmt:message key="org.jahia.admin.username.label"/>&nbsp;:</b>&nbsp;&nbsp;&nbsp;<%= selUser.getUsername()%>
          <input type="hidden" name="username" value="<%= selUser.getUsername()%>">
        </p>
        <p>
          <fmt:message key="org.jahia.admin.users.ManageUsers.definitivelyRemove.label"/>
        </p>
        <p>
          <fmt:message key="org.jahia.admin.users.ManageUsers.areYouSure.label"/>
        </p>
        
        
        
    <% } else { %>
        <p>
          <b><fmt:message key="org.jahia.admin.users.ManageUsers.userName.label"/>&nbsp;:</b>&nbsp;&nbsp;&nbsp;<%= selUser.getUsername()%>
        </p>
        <p>
          <fmt:message key="org.jahia.admin.users.ManageUsers.cannotRemove.label"/>
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
         <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
      </span>
     </span>
     <% if (selUser.getProviderName().indexOf("LDAP") == -1) { %>     
     <span class="dex-PushButton"> 
      <span class="first-child">
         <a class="ico-cancel" href='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=display")%>'><fmt:message key="org.jahia.admin.cancel.label"/></a>
      </span>
     </span>
     <span class="dex-PushButton"> 
      <span class="first-child">
         <a class="ico-ok" href="javascript:document.mainForm.submit();" ><fmt:message key="org.jahia.admin.ok.label"/></a>
      </span>
     </span>      
      <% } %> 	      
  </div>

</div>

