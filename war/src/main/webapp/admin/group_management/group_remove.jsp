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
<%@page import = "java.util.*" %>
<%@page import="org.jahia.bin.*" %>
<%@page import = "org.jahia.data.JahiaData" %>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="groupErrorMessage" class="java.lang.String" scope="session"/><jsp:useBean id="URL" class="java.lang.String" scope="request"/><%
JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");
String groupName = (String)request.getAttribute("groupName");
int stretcherToOpen   = 1; %>
<!-- Adiministration page position -->
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageGroups.removeGroup.label"/></h2>
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
                    <jsp:param name="mode" value="site"/>
                </jsp:include>
              <div id="content" class="fit">
                <!-- Remove group -->
                <form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=processRemove")%>' method="post">
                  <table border="0" style="width:100%">
                    <tr>
                      <br>
                      <td>
                        <b><fmt:message key="org.jahia.admin.users.ManageGroups.groupName.label"/>&nbsp;:</b>&nbsp;&nbsp;&nbsp;<%= groupName %>
                        <input type="hidden" name="groupName" value="<%= groupName%>">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <br>
                        <br>
                        <fmt:message key="org.jahia.admin.users.ManageGroups.areYouSure.label"/>
                      </td>
                    </tr>
                  </table>
                </form>
              </div><!-- End remove group -->
            </div>
            </td>
          </tr>
          </tbody>
        </table>
        </div>
        <div id="actionBar">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-cancel" href="<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=display")%>
" ><fmt:message key="org.jahia.admin.cancel.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:document.mainForm.submit();"><fmt:message key="org.jahia.admin.ok.label"/></a>
            </span>
          </span>
        </div>