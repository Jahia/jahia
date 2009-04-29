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
        
<%@ page import="org.jahia.services.acl.JahiaACLManagerService"%>
<%@ page import="org.jahia.registries.ServicesRegistry"%>
<%@ page import="org.jahia.services.acl.JahiaBaseACL"%>
<%@ page import="org.jahia.services.lock.LockPrerequisites"%>
<%@ page import="org.jahia.params.ProcessingContext"%>
<%@ page import="org.jahia.services.lock.LockPrerequisitesResult"%>
<%@ page import="org.jahia.services.lock.LockKey"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
final ProcessingContext paramBean = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
final boolean readOnlyMode;
if (results == null) {
  results = new LockPrerequisitesResult();
  readOnlyMode = false;
} else {
  readOnlyMode = true;
}

Boolean adminAccess = (Boolean) engineMap.get("adminAccess");
if (adminAccess == null) {
    adminAccess = Boolean.FALSE;
}

Boolean enableAuthoring = (Boolean) engineMap.get("enableAuthoring");
if (enableAuthoring == null) {
  enableAuthoring = Boolean.FALSE;
}
if (aclService.getSiteActionPermission(LockPrerequisites.EDIT, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.EDIT)) {
  enableAuthoring = Boolean.FALSE;
}

Boolean enableMetadata = (Boolean) engineMap.get("enableMetadata");
if (enableMetadata == null) {
  enableMetadata = Boolean.FALSE;
}
if (aclService.getSiteActionPermission(LockPrerequisites.METADATA, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.METADATA)) {
  enableMetadata = Boolean.FALSE;
}

Boolean enableRightView = (Boolean) engineMap.get("enableRightView");
if (enableRightView == null) {
  enableRightView = Boolean.FALSE;
}
if (aclService.getSiteActionPermission(LockPrerequisites.RIGHTS, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.RIGHTS)) {
  enableRightView = Boolean.FALSE;
}

Boolean enableTimeBasedPublishing = (Boolean) engineMap.get("enableTimeBasedPublishing");
if (enableTimeBasedPublishing == null) {
  enableTimeBasedPublishing = Boolean.FALSE;
}
if (aclService.getSiteActionPermission(LockPrerequisites.TIME_BASED_PUBLISHING, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.TIME_BASED_PUBLISHING)) {
  enableTimeBasedPublishing = Boolean.FALSE;
}

Boolean doAddEditViewRight = (Boolean) engineMap.get("doAddEditViewRight");
if (doAddEditViewRight == null) {
  doAddEditViewRight = Boolean.FALSE;
}
if (aclService.getSiteActionPermission(LockPrerequisites.FIELD_RIGHTS, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.FIELD_RIGHTS)) {
  doAddEditViewRight = Boolean.FALSE;
}
%>
<!-- leftMenu (start) -->
<div class="leftMenu">
  <ul>
    <% if (enableAuthoring.booleanValue()) { %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('edit'); return false;"<% if (theScreen.equals("edit")) { %> class="open"<%}else{%> class="closed"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.Authoring.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EDIT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %></a>
      </li>
    <% } %>
    <% if (enableMetadata.booleanValue()) { %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('metadata'); return false;"<% if (theScreen.equals("metadata")) { %> class="open"<%}else{%> class="closed"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.Metadata.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.METADATA) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
    <% if (enableRightView.booleanValue() && adminAccess.booleanValue()) { %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('rightsMgmt'); return false;"<% if (theScreen.equals("rightsMgmt")) { %> class="singleOpen"<%}else{%> class="single"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.RightsMgmt.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.RIGHTS) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
    <% if (doAddEditViewRight.booleanValue() && adminAccess.booleanValue()) { %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('ctneditview_rights'); return false;"<% if (theScreen.equals("ctneditview_rights")) { %> class="singleOpen"<%}else{%> class="single"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.FieldsRightsMgmt.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.FIELD_RIGHTS) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
    <% if (enableTimeBasedPublishing.booleanValue()) { %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('timeBasedPublishing'); return false;"<% if (theScreen.equals("timeBasedPublishing")) { %> class="singleOpen"<%}else{%> class="single"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.TimeBasedPublishing.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.TIME_BASED_PUBLISHING) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
  </ul>
</div>
<!-- leftMenu (end) -->