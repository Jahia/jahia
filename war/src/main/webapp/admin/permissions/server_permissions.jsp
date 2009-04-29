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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@page language = "java" %>
<%@page import = "org.jahia.bin.*" %>
<%@page import = "java.util.*" %>
<%@ page import="org.jahia.hibernate.model.JahiaAclName" %>
<%@ page import="org.jahia.admin.permissions.ManageServerPermissions" %>
<%@ page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.usermanager.JahiaGroup" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="org.jahia.services.acl.JahiaACLManagerService" %>
<%@ page import="org.jahia.security.license.LicenseActionChecker" %>
<% List aclNameList = (List) request.getAttribute("aclNameList");
final Integer userNameWidth = new Integer(15);
request.getSession().setAttribute("userNameWidth", userNameWidth);
final String selectUsrGrp = (String) request.getAttribute("selectUsrGrp"); %>
<%@include file="/admin/include/header.inc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%
stretcherToOpen   = 0; %>
<internal:gwtImport module="org.jahia.ajax.gwt.module.admin.Admin" />
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.serverpermissions.title.label"/></h2>
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
                       <fmt:message key="org.jahia.admin.serverpermissions.mainMenu.label"/>
                  </div>
              </div>
              <div class="content-item-noborder">
                <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"serverPermissions","&sub=process")%>' method="post">
                <p>
                  &nbsp;&nbsp;<fmt:message key="org.jahia.admin.serverpermissions.introduction.label"/>
                </p>
                <table cellpadding="0" cellspacing="0" border="0" width="100%" class="permissions">
                <c:forEach items="${aclNameList}" var="curAclName" varStatus="status">
                  <tr>
                    <td>
                      <div class="head headtop">
                        <div class="object-title">
                          <fmt:message key="org.jahia.admin.serverpermissions.permission.${fn:substringAfter(curAclName.aclName, 'org.jahia.actions.server.')}.label"/>
                        </div>
                      </div>
                        <c:set var="aclName" value="${curAclName.aclName}"/>
                        <c:set var="fieldName" value="${fn:replace(aclName, '.', '_')}"/>
                        <c:set var="readonly"><%= !LicenseActionChecker.isAuthorizedByLicense((String) pageContext.getAttribute("aclName"), siteID.intValue()) %></c:set>
                        <input type="hidden" name="${fieldName}" id="${fieldName}" value=""/>
                        <span id="gwtaclnameeditor-${status.index}" aclid="${curAclName.acl.id}" height="120px" fieldId="${fieldName}" readonly="${readonly}"></span>
                    </td>
                  </tr>
                </c:forEach>
                </table>

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
              <a  class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="#ok" onclick="document.jahiaAdmin.submit(); return false;"><fmt:message key="org.jahia.admin.saveChanges.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>
