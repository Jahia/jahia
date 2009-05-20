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
<%@page import="org.jahia.bin.*" %>
<%@page import = "java.util.*" %>
<%@page import = "org.jahia.data.JahiaData" %>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/><jsp:useBean id="URL" class="java.lang.String" scope="request"/><% // http files path. %>
<%
String groupName = (String)request.getAttribute("groupName");
List groupMembership = (List)request.getAttribute("groupMembership");
JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");
int stretcherToOpen   = 1; %>
<!-- Adiministration page position -->
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.viewGroupMemberships.label"/></h2>
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
            <div class="head">
              <div class="object-title">
                <fmt:message key="org.jahia.admin.membershipsForGroup.label"/>&nbsp;: <%= groupName %>
              </div>
            </div><!-- View group membership on other sites --><!-- For future version : <script language="javascript" src="../search_options.js"></script> -->
            <table border="0" cellspacing="0" class="evenOddTable" style="width:100%">
              <thead>
                <tr>
                  <th>
                    <fmt:message key="org.jahia.admin.username.label"/>
                  </th>
                  <th style="text-align:right">
                    <fmt:message key="org.jahia.admin.homeSite.label"/>&nbsp;&nbsp;
                  </th>
                </tr>
              </thead>
              <tbody>
                <%
                Iterator it = groupMembership.iterator();
                if (!it.hasNext()) { %>
                <tr>
                  <td colspan="2">
                    <fmt:message key="org.jahia.admin.noMembershipFromOtherSites.label"/>
                  </td>
                </tr><%
                } else {
                String lineClass="oddLine";
                while (it.hasNext()) {
                String groupMembers = (String)it.next();
                String memberSite = (String)it.next();
                if ("oddLine".equals(lineClass)) {
                lineClass="evenLine";
                } else {
                lineClass="oddLine";
                } %>
                <tr class="<%=lineClass%>">
                  <td>
                    <%= groupMembers %>
                  </td>
                  <td style="text-align:right">
                    <%= memberSite %>&nbsp;&nbsp;
                  </td>
                </tr>
                <%
                }
                } %>
              </tbody>
            </table>
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
      <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=display")%>'><fmt:message key="org.jahia.admin.backToGroupList.label"/></a>
    </span>
  </span>
</div>
</div>