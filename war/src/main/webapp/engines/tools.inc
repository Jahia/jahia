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
<%@ page import="org.jahia.services.acl.JahiaACLManagerService"%>
<%@ page import="org.jahia.registries.ServicesRegistry"%>
<%@ page import="org.jahia.services.acl.JahiaBaseACL"%>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ page import="org.jahia.services.lock.LockPrerequisites"%>
<%@ page import="org.jahia.services.lock.LockPrerequisitesResult"%>
<%@ page import="org.jahia.services.lock.LockKey"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    int tabCounter = 0;

    final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
    final ParamBean paramBean = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
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

    Boolean enableContentPick = (Boolean) engineMap.get("enableContentPick");
    if (enableContentPick == null) {
        enableContentPick = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.CONTENT_PICKER, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 ||
            results.getDisabledTabs().contains(LockPrerequisites.CONTENT_PICKER)) {
        enableContentPick = Boolean.FALSE;
    }

    Boolean enableVersioning = (Boolean) engineMap.get("enableVersioning");
    if (enableVersioning == null) {
        enableVersioning = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.VERSIONNING, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 ||
            results.getDisabledTabs().contains(LockPrerequisites.VERSIONNING)) {
        enableVersioning = Boolean.FALSE;
    }

    Boolean enableImport = (Boolean) engineMap.get("enableImport");
    if (enableImport == null) {
        enableImport = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.IMPORT, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 ||
            results.getDisabledTabs().contains(LockPrerequisites.IMPORT)) {
        enableImport = Boolean.FALSE;
    }

    Boolean enableExport = (Boolean) engineMap.get("enableExport");
    if (enableExport == null) {
        enableExport = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.EXPORT, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 ||
            results.getDisabledTabs().contains(LockPrerequisites.EXPORT)) {
        enableExport = Boolean.FALSE;
    }

    Boolean enableLogs = engineMap.containsKey("enableLogs") ? (Boolean) engineMap.get("enableLogs") : Boolean.TRUE;

    if (aclService.getSiteActionPermission(LockPrerequisites.LOGS, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 ||
            results.getDisabledTabs().contains(LockPrerequisites.LOGS)) {
        enableLogs = Boolean.FALSE;
    }

    Boolean enableAdvancedWorkflow = (Boolean) engineMap.get("enableAdvancedWorkflow");
    if (enableAdvancedWorkflow == null) {
        enableAdvancedWorkflow = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.MANAGE_WORKFLOW, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 ||
            results.getDisabledTabs().contains(LockPrerequisites.MANAGE_WORKFLOW)) {
        enableAdvancedWorkflow = Boolean.FALSE;
    }
%>

<div class="leftMenu">
  <ul>
    <% if (enableContentPick.booleanValue()) {
        tabCounter++;
    %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('contentPick'); return false;"<% if (theScreen.equals("contentPick")) { %> class="singleOpen"<%}else{%> class="single"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.ContentPick.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.CONTENT_PICKER) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
    <% if ((enableAdvancedWorkflow.booleanValue() && adminAccess.booleanValue()) || readOnlyMode) {
        tabCounter++;
    %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('workflow'); return false;"<% if (theScreen.equals("workflow")) { %> class="singleOpen"<%}else{%> class="single"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.Workflow.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.MANAGE_WORKFLOW) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
    <% if (enableImport.booleanValue()) {
        tabCounter++;
    %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('import'); return false;"<% if (theScreen.equals("import")) { %> class="singleOpen"<%}else{%> class="single"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.Import.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.IMPORT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
    <% if (enableExport.booleanValue()) {
        tabCounter++;
    %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('export'); return false;"<% if (theScreen.equals("export")) { %> class="singleOpen"<%}else{%> class="single"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.Export.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EXPORT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
    <% if (enableVersioning.booleanValue()) {
        tabCounter++;
    %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('versioning'); return false;"<% if (theScreen.equals("versioning")) { %> class="singleOpen"<%}else{%> class="single"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.Versioning.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.VERSIONNING) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
    <% if (adminAccess.booleanValue() && enableLogs.booleanValue()) {
        tabCounter++;
    %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('logs'); return false;"<% if (theScreen.equals("logs")) { %> class="singleOpen"<%}else{%> class="single"<%}%>>
          <fmt:message key="org.jahia.engines.include.actionSelector.Logs.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.LOGS) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
    <% if (tabCounter == 0) { %>
      <li class="section first">
        <a href="#" onclick=" return false;" class="singleOpen">
          <fmt:message key="org.jahia.engines.noTools.label"/></a>
      </li>
    <% } %>
  </ul>
</div>