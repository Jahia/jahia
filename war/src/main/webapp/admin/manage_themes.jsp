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
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>


<%@include file="/admin/include/header.inc" %>
<%@page import="java.util.*,org.jahia.data.templates.*" %>
<%@page import="org.jahia.bin.*" %>
<%@ page import="org.jahia.operations.valves.ThemeValve" %>
<%

    String requestURI = (String) request.getAttribute("requestURI");
    JahiaSite site = (JahiaSite) request.getAttribute("site");

%>

<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.themes.ManageThemes.label"/>
        : <% if (currentSite != null) { %><fmt:message key="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %>
        &nbsp;&nbsp;<%} %></h2>
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
                                    <fmt:message key="org.jahia.admin.themes.ManageThemes.label"/>&nbsp;
                                </div>
                            </div>

                           <script type="text/javascript">
                                    function swapImage(imgId,imgToSwitch){
                                        var image = document.getElementById(imgId);
                                        var dropd = document.getElementById(imgToSwitch);
                                        var themePreview = '${templatePkg.thumbnail}';
                                        var themePreviewBegin = themePreview.substr(0,themePreview.lastIndexOf("."));
                                        var themePreviewEnd = themePreview.substr(themePreview.lastIndexOf("."),themePreview.length);
                                        image.src = '<%=URL%>../${templatePkg.rootFolderPath}/' + themePreviewBegin + '_' + dropd.value + themePreviewEnd;
                                    };
                                    </script>
                            <div class="content-body">
                                <table class="topAlignedTable" cellpadding="5" cellspacing="0">
                                    <tr><td><table class="evenOddTable">
                                        <tr><th><fmt:message key='org.jahia.admin.themes.list.label'/></th><th><fmt:message key='org.jahia.admin.themes.default.label'/></th></tr>
                                        <c:forEach var="themeBean" items="${themesBean}">
                                            <tr>
                                                <td>${themeBean.themeName}</td>
                                                <td><c:if test="${themeBean.selected}"><img src="<%=URL%>images/icons/workflow/accept.gif" width="10"  height="10" border="0"/></c:if></td>
                                            </tr>
                                        </c:forEach>
                                    </table></td></tr>
                                </table>
                                <div class="head">
                                       <div class="object-title">
                                           <fmt:message key="org.jahia.admin.themes.choose.label"/>&nbsp;
                                       </div>
                                   </div>

                                <table class="topAlignedTable" cellpadding="5" cellspacing="0">
                                    <form name="jahiathemeSelectorFormsite" action='<%=JahiaAdministration.composeActionURL(request,response,"themes","&sub=display")%>'>
                                        <tr><td><select id="switchTheme" name="jahiaThemeSelector" onchange="swapImage('imageTheme','switchTheme')">
                                        <c:forEach var="themeBean" items="${themesBean}">
                                            <option value="${themeBean.themeName}" <c:if test="${themeBean.selected}">selected</c:if>>${themeBean.themeName}</option>
                                            <c:if test="${themeBean.selected}">
                                                <c:set var="selectedTheme" value="${themeBean.themeName}"/>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                    <input type="hidden" name="jahiathemeSelectorScope" value="site">
                                    <input type="hidden" name="do" value="themes">
                                    <input type="hidden" name="sub" value="display">
                                    </td></tr>

                                    <tr><td><img id="imageTheme" src="#" width="270" height="141" alt=""></td></tr>
                                     <script type="text/javascript">
                                        swapImage('imageTheme','switchTheme')
                                    </script>
                                    </form>
                                </table>
                            </div>
                            </div>
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
                 href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#"
                 onclick="document.jahiathemeSelectorFormsite.submit()"><fmt:message key="org.jahia.admin.saveChanges.label"/></a>
            </span>
          </span>
</div>
<%@include file="/admin/include/footer.inc" %>