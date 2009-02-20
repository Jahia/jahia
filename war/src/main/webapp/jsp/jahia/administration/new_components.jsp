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
<%@page import   = "java.util.*,org.jahia.data.applications.*,org.jahia.data.webapps.*" %>
<%@page import="org.jahia.bin.*" %>
<%
String theURL = "";
Iterator packagesList = (Iterator)request.getAttribute("packagesList");
String requestURI = (String)request.getAttribute("requestURI");
String contextRoot = (String)request.getContextPath();
stretcherToOpen   = 0; %>
<script type="text/javascript">
  
  function sendForm(){
      document.mainForm.submit();
  }
  
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageComponents.label"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/jsp/jahia/administration/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
            <jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
                <jsp:param name="mode" value="server"/>
            </jsp:include>
              <div id="content" class="fit">
                <div class="head">
                  <div class="object-title">
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.newApplicationsList.label"/>
                  </div>
                </div>
                <div class="content-body">
                  <div id="operationMenu">
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-refresh" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=displaynewlist")%>' onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('refreshdir','','${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.refreshOn.button"/>',1)" title='<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.refresh.label"/>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.refresh.label"/></a>
                      </span>
                    </span>
                  </div>
                  <form name="mainForm" action="<%=requestURI%>?do=sharecomponents&sub=visibility" method="post">
                    <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                      <thead>
                        <tr>
                          <th width="50%">
                            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.fileOrDirectoryName.label"/>
                          </th>
                          <th width="25%">
                            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.context.label"/>
                          </th>
                          <th class="lastCol" style="text-align:right" width="25%">
                            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.moreDetails.label"/>
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        <%
                        if ( packagesList != null && packagesList.hasNext() ){
                        int lineCounter = 0;
                        while (packagesList.hasNext()){
                        JahiaWebAppsPackage aPackage = (JahiaWebAppsPackage) packagesList.next();
                        String lineClass = "oddLine";
                        if (lineCounter % 2 == 0) {
                        lineClass = "evenLine";
                        }
                        lineCounter++; %>
                        <tr class="<%=lineClass%>">
                          <td>
                            <a href="<%=requestURI%>?do=sharecomponents&sub=details&package_name=<%=aPackage.getFileName()%>"><% if (aPackage.isDirectory()){ %>/<%} %><%=aPackage.getFileName() %></a>
                            <br>
                            <br>
                          </td>
                          <td>
                            /<%=aPackage.getContextRoot() %>
                          </td>
                          <td class="lastCol" style="text-align:right">
                            <a href="<%=requestURI%>?do=sharecomponents&sub=details&package_name=<%=aPackage.getFileName()%>" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.showDetails.label"/>"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.details.label"/></a>
                            <br>
                            <br>
                          </td>
                        </tr>
                        <%
                        }
                        } else { %>
                        <tr>
                          <td colspan="3" align="center" class="lastCol">
                            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.noApplicationsFound.label"/>
                          </td>
                        </tr><%
                        } %>
                      </tbody>
                    </table>
                  </form>
                </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
          <div id="actionBar">
            <span class="dex-PushButton">
              <span class="first-child">
                <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.backToComponentsList.label"/></a>
              </span>
            </span>
          </div>
          </div><%@include file="/jsp/jahia/administration/include/footer.inc" %>