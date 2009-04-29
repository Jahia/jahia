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