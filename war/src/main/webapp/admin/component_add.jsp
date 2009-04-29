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
String appPath		= (String)request.getAttribute("appPath");
String warningMsg	= (String)request.getAttribute("warningMsg");
String appName		= "";
String appDescr		= "";
JahiaWebAppsPackage aPackage = null;
if (request.getAttribute("aPackage") != null){
aPackage = (JahiaWebAppsPackage)request.getAttribute("aPackage");
appName = (String)request.getAttribute("appName");
appDescr = (String)request.getAttribute("appDescr");
}
String requestURI 	= (String)request.getAttribute("requestURI");
String contextRoot 	= (String)request.getContextPath();
stretcherToOpen   = 0; %>
<script type="text/javascript">
  
  function sendForm(subAction){
      document.mainForm.subaction.value = subAction;
      document.mainForm.action = "<%=requestURI%>?do=sharecomponents&sub=add";
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
                    <fmt:message key="org.jahia.admin.components.ManageComponents.manuallyRegister.label"/>
                  </div>
                </div>
                <form name="mainForm" action="<%=requestURI%>?do=sharecomponents&sub=add" method="post">
                  <%
                  if ( warningMsg.length()>0 ){ %>
                  <p class="error">
                    <%=warningMsg %>
                  </p>
                  <% } %>
                  <table border="0" cellpadding="5" width="100%">
                    <tr>
                      <td>
                        <p>
                          <fmt:message key="org.jahia.admin.components.ManageComponents.manuallyAddComponent.label"/>
                        </p>
                        <p>
                          <fmt:message key="org.jahia.admin.components.ManageComponents.note.label"/>&nbsp;:<fmt:message key="org.jahia.admin.components.ManageComponents.onlyFiles.label"/>
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td colspan="2">
                        <fmt:message key="org.jahia.admin.components.ManageComponents.fullPath.label"/>&nbsp;:<input class="input" type="text" name="appPath" value="<%=appPath%>" size="<%=inputSize%>">
                      </td>
                    </tr>
                  </table><% if ( aPackage != null ){ %>
                  <div class="head headtop">
                    <div class="object-title">
                      <fmt:message key="org.jahia.admin.components.ManageComponents.componentDetails.label"/>
                    </div>
                  </div><h4>&nbsp;&nbsp;<fmt:message key="org.jahia.admin.components.ManageComponents.listWebApplications.label"/>&nbsp;:</h4>
                  <table border="0" cellpadding="5" cellspacing="0" width="100%">
                    <%
                    JahiaWebAppDef webApp = null;
                    List webApps = aPackage.getWebApps();
                    int size = webApps.size();
                    int count = 0;
                    if ( size>0 ){
                    for ( int i=0 ; i<size ; i++ ) {
                    webApp = (JahiaWebAppDef)webApps.get(i);
                    if ( webApp != null ){
                    count +=1; %>
                    <tr>
                      <td>
                        <fmt:message key="org.jahia.admin.components.ManageComponents.webAppName.label"/>&nbsp;:
                      </td>
                      <td>
                        &nbsp;<input class="input" type="text" name="appName" value="<%=appName%>" size="<%=inputSize%>">
                        <br>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <fmt:message key="org.jahia.admin.components.ManageComponents.webAppContext.label"/>&nbsp;:
                        <br>
                      </td>
                      <td>
                        &nbsp;<%=webApp.getContextRoot() %>
                        <br>
                      </td>
                    </tr>
                    <tr>
                      <td style="vertical-align: top;">
                        <fmt:message key="org.jahia.admin.description.label"/>&nbsp;:
                      </td>
                      <td>
                        &nbsp;
                        <textarea class="input" name="appDescr" rows="10" cols="<%=inputSize%>">
                          <%=appDescr %>
                        </textarea>
                        <br>
                      </td>
                    </tr>
                    <tr>
                      <td style="vertical-align : top">
                        <fmt:message key="org.jahia.admin.components.ManageComponents.listServelts.label"/>&nbsp;:
                      </td>
                      <td>
                        <table width="100%" cellspacing="0" border="0" cellpadding="5">
                          <%
                          List servlets = webApp.getServlets();
                          Servlet_Element servlet = null;
                          int nbServlet = 0;
                          if ( servlets != null ){
                          nbServlet = servlets.size();
                          }
                          for ( int j=0 ; j<nbServlet ; j++ ){
                          servlet = (Servlet_Element)servlets.get(j); %>
                          <tr>
                            <td>
                              <fmt:message key="org.jahia.admin.name.label"/>&nbsp;:
                            </td>
                            <td>
                              <%=servlet.getName() %>
                            </td>
                          </tr>
                          <tr>
                            <td>
                              <fmt:message key="org.jahia.admin.components.ManageComponents.source.label"/>&nbsp;:
                            </td>
                            <td>
                              <%=servlet.getSource() %>
                            </td>
                          </tr>
                          <tr>
                            <td>
                              <fmt:message key="org.jahia.admin.type.label"/>&nbsp;:
                            </td>
                            <td>
                              <%=servlet.getTypeLabel() %>
                            </td>
                          </tr>
                          <tr>
                            <td>
                              <fmt:message key="org.jahia.admin.description.label"/>&nbsp;:
                            </td>
                            <td>
                              <%=servlet.getdesc() %>
                            </td>
                          </tr>
                          <% } %>
                        </table>
                      </td>
                    </tr>
                    <% }
                    }
                    } %>
                  </table>
                  <% if ( count==0 ){ %>
                  <div style="text-align : right">
                    <b><fmt:message key="org.jahia.admin.components.ManageComponents.noWebComponents.label"/></b>
                  </div><% } %>
                  <% } %>
                  <input type="hidden" name="subaction" value="">
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>'><fmt:message key="org.jahia.admin.components.ManageComponents.backToComponentsList.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-app-scan" href="javascript:sendForm('scanDir');"><fmt:message key="org.jahia.admin.components.ManageComponents.scanComponent.label"/></a>
            </span>
          </span><% if ( aPackage != null ){ %>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:sendForm('deploy');"><fmt:message key="org.jahia.admin.deploy.label"/></a>
            </span>
          </span><% } %>
        </div>
      </div>
<%@include file="/admin/include/footer.inc" %>
