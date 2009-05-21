<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@include file="/admin/include/header.inc"%>
<%@page import   = "java.util.*,org.jahia.data.templates.*"%>
<%@page import="org.jahia.bin.*"%>
<%

    String theURL = "";
    Iterator packagesList = (Iterator)request.getAttribute("packagesList");

    String requestURI = (String)request.getAttribute("requestURI");
    String contextRoot = (String)request.getContextPath();
    JahiaSite site = (JahiaSite)request.getAttribute("site");

%>

<script type="text/javascript">
    function sendForm() {
        document.mainForm.submit();
    }
</script>

<div id="topTitle">
	<div id="topTitleLogo">
		<img src="<%=URL%>images/icons/admin/briefcase_document.gif" width="48" height="48" />
  </div>
  <h1 id="topTitleLabel"><fmt:message key="org.jahia.admin.manageTemplates.label"/><br><% if ( site!= null ){%><fmt:message key="org.jahia.admin.site.label"/>&nbsp;<%=site.getServerName()%><%}%></h1>
</div>

<div id="adminMainContent">

  <h2>
    <fmt:message key="org.jahia.admin.templates.ManageTemplates.newTemplatesList.label"/>&nbsp;:
  </h2>
  <table border="0" cellpadding="5" cellspacing="0" width="100%" class="evenOddTable">
    <thead>      
      <tr>
          <th width="50%"><fmt:message key="org.jahia.admin.fileOrDirectoryName.label"/></th>
          <th width="25%"><fmt:message key="org.jahia.admin.templates.ManageTemplates.rootFolder.label"/></th>
          <th	style="text-align:right" width="25%"><fmt:message key="org.jahia.admin.moreDetails.label"/></th>
      </tr>
    </thead>
    <tbody>
      <%
          if ( packagesList != null && packagesList.hasNext() ){

              String lineClass = "oddLine";
              while (packagesList.hasNext()){
                  JahiaTemplatesPackage aPackage = (JahiaTemplatesPackage) packagesList.next();
                  if ("oddLine".equals(lineClass)) {
                      lineClass = "evenLine";
                  } else {
                      lineClass = "oddLine";
                  }
                  %>
                  <tr class="<%=lineClass%>">
                      <td><b><a href="<%=requestURI%>?do=templates&sub=details&package_name=<%=aPackage.getFileName()%>"><% if (aPackage.isDirectory()){ %>/<%}%><%=aPackage.getFileName()%></a></b><br><br></td>
                      <td>/<%=aPackage.getRootFolder()%></td>
                      <td style="text-align:right"><a href="<%=requestURI%>?do=templates&sub=details&package_name=<%=aPackage.getFileName()%>" alt="<fmt:message key="org.jahia.admin.showDetails.label"/>"><fmt:message key="org.jahia.admin.details.label"/></a><br><br></td>
                  </tr>
                  <%
              }
          } else {
      %>
      <tr>
          <td colspan="3" style="text-align: center">
            <fmt:message key="org.jahia.admin.templates.ManageTemplates.noTemplateFound.label"/>
          </td>
      </tr>
      <%
          }
      %>
    </tbody>
  </table>
  <div class="buttonList" style="text-align: right; padding-top: 30px; padding-bottom : 20px">
    <div class="button">
      <a class="ico-refresh" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=displaynewlist")%>'><fmt:message key="org.jahia.admin.refresh.label"/></a>
    </div>
  </div>

  <div id="operationMenu">
  	<div id="operationMenuLabel">
			<fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:
		</div>
		<ul id="operationList">
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=display")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.backToTemplatesList.label"/></a>
      </li>     		
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=add")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.manuallyAddNewTemplate.label"/></a>
      </li>     		
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=options")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.templatesManagementOptions.label"/></a>
      </li>     		
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
      </li>     		
    </ul>
  </div>

</div>

<%@include file="/admin/include/footer.inc"%>