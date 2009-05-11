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
<%@page import="org.jahia.admin.permissions.ManageSitePermissions" %>
<%@page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@ page import="org.jahia.hibernate.model.JahiaAclName" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.security.license.LicenseActionChecker" %>
<%@ page import="org.jahia.services.usermanager.JahiaGroup" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    List aclNameList = (List) request.getAttribute("aclNameList");
    final Integer userNameWidth = new Integer(15);
    request.getSession().setAttribute("userNameWidth", userNameWidth);
    final String selectUsrGrp = (String) request.getAttribute("selectUsrGrp");
    final String curPermissionGroup = (String) session.getAttribute(ManageSitePermissions.class.getName() + ManageSitePermissions.CURGROUP_SESSION_ATTR_NAME); %>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.sitepermissions.title.label"/></h2>
</div>
<internal:gwtImport module="org.jahia.ajax.gwt.module.admin.Admin" />

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
<div class="head headtop">
    <div class="object-title">
        <fmt:message key="org.jahia.admin.sitepermissions.mainMenu.label"/>
    </div>
</div>
<div class="content-item">
<form name="jahiaAdmin"
      action='<%=JahiaAdministration.composeActionURL(request,response,"sitePermissions","&sub=process")%>'
      method="post">
<script type="text/javascript">
    function saveContent() {
        document.jahiaAdmin.submit();
    }
    function changePermissionGroup(newGroup) {
        document.jahiaAdmin.currentGroup.value = newGroup;
        document.jahiaAdmin.submit();
    }
</script>
<input type="hidden" name="currentGroup" value="<%=curPermissionGroup%>"/>

<p>
    <fmt:message key="org.jahia.admin.sitepermissions.introduction.label"/>
</p>

<div class="dex-subTabBar">
    <ul class="dex-subTabBarItem-wrapper">
        <li class="<% if ("administration".equals(curPermissionGroup)) { %>dex-subTabBarItem-selected<% } else { %>dex-subTabBarItem-first<% } %>">
            <div>
                <div>
                              <span>
                                <a href="#"
                                   onclick="changePermissionGroup('administration')"><fmt:message key="org.jahia.admin.sitepermissions.permissionGroup.administration.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("data".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                                <a href="#" onclick="changePermissionGroup('data')"><fmt:message key="org.jahia.admin.sitepermissions.permissionGroup.data.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("tools".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                                <a href="#" onclick="changePermissionGroup('tools')"><fmt:message key="org.jahia.admin.sitepermissions.permissionGroup.tools.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("actions".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                               <a href="#" onclick="changePermissionGroup('actions')"><fmt:message key="org.jahia.admin.sitepermissions.permissionGroup.actions.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("languages".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                               <a href="#" onclick="changePermissionGroup('languages')"><fmt:message key="org.jahia.admin.sitepermissions.permissionGroup.languages.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("toolbars".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                               <a href="#" onclick="changePermissionGroup('toolbars')"><fmt:message key="org.jahia.admin.sitepermissions.permissionGroup.toolbars.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("integrity".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                               <a href="#" onclick="changePermissionGroup('integrity')"><fmt:message key="org.jahia.admin.sitepermissions.permissionGroup.integrity.label"/></a>
                              </span>
                </div>
            </div>
        </li>
    </ul>
</div>
<div style="clear:both">
    <table cellpadding="0" cellspacing="0" border="0" width="100%" class="permissions">
    <c:forEach items="${aclNameList}" var="curAclName" varStatus="status">
        <tr>
            <td>
                <div class="head headtop">
                    <div class="object-title">
                        <c:set var="aclShortName" value="${fn:substringAfter(fn:substringAfter(curAclName.aclName, 'org.jahia.actions.sites.'), '.')}"/>
                        <c:if test="${fn:contains(aclShortName, 'engines.languages.')}" var="languagePermissions">
                            <fmt:message key="org.jahia.admin.sitepermissions.permission.engines.languages.label"/>
                            <%
                            String aclShortName = (String)pageContext.getAttribute("aclShortName");
                            String flagCode = aclShortName.substring(aclShortName.lastIndexOf('.') + 1); 
                            %>
                            &nbsp;<c:set var="curLocale" value="${requestScope['org.jahia.data.JahiaData'].processingContext.locale}"/><%= LanguageCodeConverters.languageCodeToLocale(flagCode).getDisplayName((Locale) pageContext.getAttribute("curLocale")) %>
                            &nbsp;<internal:displayLanguageFlag code="<%=flagCode%>"/>
                        </c:if>
                        <c:if test="${!languagePermissions}">
                            <fmt:message key="org.jahia.admin.sitepermissions.permission.${aclShortName}.label"/>
                        </c:if>
                    </div>
                </div>
                <c:set var="aclName" value="${curAclName.aclName}"/>
                <c:set var="fieldName" value="${fn:replace(aclName, '.', '_')}"/>
                <c:set var="readonly"><%= !LicenseActionChecker.isAuthorizedByLicense((String) pageContext.getAttribute("aclName"), siteID.intValue()) %></c:set>
                <internal:aclNameEditor aclId="${curAclName.acl.id}" fieldId="${fieldName}" fieldName="${fieldName}" readonly="${readonly}"/>
            </td>
        </tr>
    </c:forEach>
    </table>
</div>

</form>
</div>
</div>
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
            <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
        </span>
    </span>
    <span class="dex-PushButton">
        <span class="first-child">
            <a class="ico-ok" href="javascript:saveContent();"><fmt:message key="org.jahia.admin.saveChanges.label"/></a>
        </span>
    </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>
