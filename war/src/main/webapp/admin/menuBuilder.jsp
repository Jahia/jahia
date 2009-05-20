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
<%@page import="org.jahia.data.JahiaData" %>
<%@page import="org.jahia.bin.*" %>
<% stretcherToOpen = 0; %>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.clipbuilder.title"/></h2>
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
                                        <fmt:message key="org.jahia.admin.clipbuilder.label"/>
                                    </div>
                                </div>
                                <div class="content-item">
                                    <ul class="linkList">
                                        <li>
                                            <a href='<%=JahiaAdministration.composeActionURL(request,response,null,"/manageAction?do=clipbuilder&webClippingAction=init")%>'><fmt:message key="org.jahia.admin.clipbuilder.htmllabel"/></a>
                                        </li>
                                        <li>
                                            <a href='<%=JahiaAdministration.composeActionURL(request,response,null,"/buildSqlPortlet?do=clipbuilder&webClippingAction=init")%>'><fmt:message key="org.jahia.admin.clipbuilder.sqllabel"/></a>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                </td>
            </tr>
    </table>
</div>
<div id="actionBar">
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-back"
         href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu#server","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
    </span>
  </span>
</div>
<%@include file="/admin/include/footer.inc" %>
