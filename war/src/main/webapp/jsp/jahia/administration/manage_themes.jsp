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

<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>


<%@include file="/jsp/jahia/administration/include/header.inc" %>
<%@page import="java.util.*,org.jahia.data.templates.*" %>
<%@page import="org.jahia.bin.*" %>
<%@ page import="org.jahia.operations.valves.ThemeValve" %>
<%

    String requestURI = (String) request.getAttribute("requestURI");
    JahiaSite site = (JahiaSite) request.getAttribute("site");

%>

<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.themes.ManageThemes.label"/>
        : <% if (currentSite != null) { %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %>
        &nbsp;&nbsp;<%} %></h2>
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
                            <jsp:param name="mode" value="site"/>
                        </jsp:include>
                        <div id="content" class="fit">
                            <div class="head">
                                <div class="object-title">
                                    <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                            resourceName="org.jahia.admin.themes.ManageThemes.label"/>&nbsp;
                                </div>
                            </div>
                            <div class="content-body">
                                <table class="topAlignedTable" cellpadding="5" cellspacing="0">
                                <form name="jahiathemeSelectorFormsite" method="get" action="/jahia/administration/?do=themes&sub=display">
                                    <tr><td><select name="jahiaThemeSelector" onchange="document.imageTheme.src='<%=URL%>../templates/${templateName}/images/preview_'+this.value+'.gif'">
                                        <c:forEach var="themeBean" items="${themesBean}">
                                            <option name="${themeBean.themeName}" <c:if test="${themeBean.selected}">selected</c:if>>${themeBean.themeName}</option>
                                            <c:if test="${themeBean.selected}">
                                                <c:set var="selectedTheme" value="${themeBean.themeName}"/>
                                            </c:if>
                                        </c:forEach>
                                    </select>
                                    <input type="hidden" name="jahiathemeSelectorScope" value="site">
                                    <input type="hidden" name="do" value="themes">
                                    <input type="hidden" name="sub" value="display">
                                    </td></tr>
                                    <tr><td><img id="imageTheme" src="<%=URL%>../templates/${templateName}/images/preview_${selectedTheme}.gif" width="270" height="141" alt=""></td></tr>
                                    <tr><td><input type="submit" name="<utility:resourceBundle resourceBundle="JahiaInternalResources"
                      resourceName="org.jahia.admin.saveChanges.label"/>" value="<utility:resourceBundle resourceBundle="JahiaInternalResources"
                      resourceName="org.jahia.admin.saveChanges.label"/>"></td></tr>
                                </form>
                                </table>
                            </div>
                            <div class="content-body">
            <%
                if (request.getParameter("jahiaThemeSelector") != null ) {
            %>
                              <span style="padding: 5px;display:block;border-bottom: 1px solid #B7CBD8;">
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.changeTheme'
                                          defaultValue="New theme selected "/> :
                    </span>

                                    <table class="topAlignedTable" cellpadding="5" cellspacing="0">
                                        <tr><td>
                                    <%=request.getParameter("jahiaThemeSelector")%></td></tr>
                                </table>
                                    <%
                }
            %>

            </td>
        </tr>

    </table>
</div>
<div id="actionBar">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-back"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources"
                      resourceName="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#"
                 onclick="document.jahiathemeSelectorFormsite.submit()"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                      resourceName="org.jahia.admin.saveChanges.label"/></a>
            </span>
          </span>
</div>
<%@include file="/jsp/jahia/administration/include/footer.inc" %>