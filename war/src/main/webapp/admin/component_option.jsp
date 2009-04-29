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
<%@page import   = "java.util.*,org.jahia.data.applications.*" %>
<%@page import="org.jahia.bin.*" %>
<%
String theURL = "";
String requestURI = (String)request.getAttribute("requestURI");
String contextRoot = (String)request.getContextPath();
Integer val = (Integer)request.getAttribute("autoDeploy");
boolean autoDeploy = false;
stretcherToOpen   = 0;
if ( val != null ){
autoDeploy = (val.intValue()==1);
} %>
<script type="text/javascript">
  function sendForm(subAction){
      document.mainForm.subaction.value = subAction;
      document.mainForm.action = "<%=requestURI%>?do=sharecomponents&sub=options";
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
                    <fmt:message key="org.jahia.admin.generalOptions.label"/>
                  </div>
                </div>
                <div class="content-body">
                  <div id="operationMenu">
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-app-add" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=add")%>'><fmt:message key="org.jahia.admin.components.ManageComponents.manuallyAddNewComponent.label"/></a>
                      </span>
                    </span>
                  </div>
                </div>
                <form name="mainForm" action="" method="post">
                  <table border="0" cellpadding="5" cellspacing="0">
                    <tr>
                      <td>
                        <fmt:message key="org.jahia.admin.components.ManageComponents.automaticDeployment.label"/>&nbsp;:
                      </td>
                      <td>
                        <input type="checkbox" name="autoDeploy" value="1"<% if ( autoDeploy ) { %>checked<% } %>>
                      </td>
                    </tr>
                  </table>
                  <div class="buttonList" style="text-align: right; padding-top: 10px">
                    <input type="hidden" name="subaction" value="">
                  </div>
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
              <a class="ico-restore" href="javascript:document.mainForm.reset()"><fmt:message key="org.jahia.admin.resetChanges.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:sendForm('save');"><fmt:message key="org.jahia.admin.save.label"/></a>
            </span>
          </span>
        </div>
      </div>
	  <%@include file="/admin/include/footer.inc" %>