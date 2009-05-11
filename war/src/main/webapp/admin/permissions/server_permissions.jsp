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
                        <internal:aclNameEditor aclId="${curAclName.acl.id}" fieldId="${fieldName}" fieldName="${fieldName}" readonly="${readonly}" allowSiteSelection="true"/>
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
