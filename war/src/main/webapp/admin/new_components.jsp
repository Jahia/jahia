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
<%@include file="/admin/include/header.inc" %>
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
  <h2 class="edit"><fmt:message key="org.jahia.admin.manageComponents.label"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/admin/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
            <jsp:include page="/admin/include/left_menu.jsp">
                <jsp:param name="mode" value="server"/>
            </jsp:include>
              <div id="content" class="fit">
                <div class="head">
                  <div class="object-title">
                    <fmt:message key="org.jahia.admin.components.ManageComponents.newApplicationsList.label"/>
                  </div>
                </div>
                <div class="content-body">
                  <div id="operationMenu">
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-refresh" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=displaynewlist")%>' onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('refreshdir','','${pageContext.request.contextPath}<fmt:message key="org.jahia.refreshOn.button"/>',1)" title='<fmt:message key="org.jahia.admin.refresh.label"/>'><fmt:message key="org.jahia.admin.refresh.label"/></a>
                      </span>
                    </span>
                  </div>
                  <form name="mainForm" action="<%=requestURI%>?do=sharecomponents&sub=visibility" method="post">
                    <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                      <thead>
                        <tr>
                          <th width="50%">
                            <fmt:message key="org.jahia.admin.fileOrDirectoryName.label"/>
                          </th>
                          <th width="25%">
                            <fmt:message key="org.jahia.admin.components.ManageComponents.context.label"/>
                          </th>
                          <th class="lastCol" style="text-align:right" width="25%">
                            <fmt:message key="org.jahia.admin.moreDetails.label"/>
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
                            <a href="<%=requestURI%>?do=sharecomponents&sub=details&package_name=<%=aPackage.getFileName()%>" alt="<fmt:message key="org.jahia.admin.showDetails.label"/>"><fmt:message key="org.jahia.admin.details.label"/></a>
                            <br>
                            <br>
                          </td>
                        </tr>
                        <%
                        }
                        } else { %>
                        <tr>
                          <td colspan="3" align="center" class="lastCol">
                            <fmt:message key="org.jahia.admin.components.ManageComponents.noApplicationsFound.label"/>
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
                <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>'><fmt:message key="org.jahia.admin.components.ManageComponents.backToComponentsList.label"/></a>
              </span>
            </span>
          </div>
          </div><%@include file="/admin/include/footer.inc" %>