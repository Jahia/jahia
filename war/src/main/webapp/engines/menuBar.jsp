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
<%@page language = "java"%>
<%@page import = "java.util.*"%>
<%@ page import="org.jahia.services.acl.JahiaACLManagerService"%>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ page import="org.jahia.registries.ServicesRegistry"%>
<%@ page import="org.jahia.services.acl.JahiaBaseACL"%>
<%@ page import="org.jahia.services.lock.LockPrerequisites"%>
<%@ page import="org.jahia.services.lock.LockPrerequisitesResult"%>
<%@ page import="org.jahia.services.lock.LockKey"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>

<%
    Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    if (engineMap == null) {
        engineMap = (Map) session.getAttribute("jahia_session_engineMap");
    }
    final boolean showEditMenu = ((Boolean) request.getAttribute("showEditMenu")).booleanValue();
    final boolean readOnly = LockPrerequisites.getInstance().getLockPrerequisitesResult(
            (LockKey) engineMap.get("LockKey")) != null;

%>

  <% if (jspSource.equals("delete_container") || jspSource.equals("filemanager_error")) { %>
  &nbsp;
  <% } else if (jspSource.equals("lock")) { %>
    <ul class="dex-TabBarItem-wrapper" style="width: 100%;">
      <li class="dex-TabBarFirst"><div class="gwt-HTML">&nbsp;</div></li>
      <li class="dex-TabBarItem">
        <div class="display">
          <div>
            <span class="tab-icon ico-data"><fmt:message key="org.jahia.engines.data.label"/></span>
          </div>
        </div>
      </li>
      <li class="dex-TabBarRest"><div class="gwt-HTML">&nbsp;</div></li>
    </ul>
  <% } else { %>
    <ul class="dex-TabBarItem-wrapper" style="width: 100%;">
      <li class="dex-TabBarFirst"><div class="gwt-HTML">&nbsp;</div></li>
      <li class="dex-TabBarItem<% if (showEditMenu) { %>-selected<% } %>">
        <div class="display">
          <div>
            <span class="tab-icon ico-data">
              <% if (showEditMenu) { %>
              <fmt:message key="org.jahia.engines.data.label"/>
              <% } else { %>
              <a href="javascript:handleActionChange('edit')">
                  <fmt:message key="org.jahia.engines.data.label"/>
              </a>
              <% } %>
            </span>
          </div>
        </div>
      </li>
      <% if (! jspSource.startsWith("filemanager")) {
          JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
          final ParamBean paramBean = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
          LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
          if (results == null) {
              results = new LockPrerequisitesResult();
          }
          final String screenName;
          final boolean hasAdminAcess = Boolean.TRUE.equals(engineMap.get("adminAccess"));
          if (Boolean.TRUE.equals(engineMap.get("enableContentPick")) &&
              (aclService.getSiteActionPermission(LockPrerequisites.CONTENT_PICKER,
                      paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) > 0) && !
                   results.getDisabledTabs().contains(LockPrerequisites.CONTENT_PICKER)) {
              screenName = "contentPick";

          } else if (readOnly || (Boolean.TRUE.equals(engineMap.get("enableAdvancedWorkflow")) &&
                 hasAdminAcess &&
                  (aclService.getSiteActionPermission(LockPrerequisites.MANAGE_WORKFLOW,
                          paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) > 0)) && !
                   results.getDisabledTabs().contains(LockPrerequisites.MANAGE_WORKFLOW)) {
              screenName = "workflow";

          } else if (Boolean.TRUE.equals(engineMap.get("enableImport")) &&
                  (aclService.getSiteActionPermission(LockPrerequisites.IMPORT,
                          paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) > 0) && !
                   results.getDisabledTabs().contains(LockPrerequisites.IMPORT)) {
              screenName = "import";

          } else if (Boolean.TRUE.equals(engineMap.get("enableExport")) &&
                  (aclService.getSiteActionPermission(LockPrerequisites.EXPORT,
                          paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) > 0) && !
                   results.getDisabledTabs().contains(LockPrerequisites.EXPORT)) {
              screenName = "export";

           } else if (Boolean.TRUE.equals(engineMap.get("enableVersioning")) &&
                  (aclService.getSiteActionPermission(LockPrerequisites.VERSIONNING,
                          paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) > 0) && !
                   results.getDisabledTabs().contains(LockPrerequisites.VERSIONNING)) {
              screenName = "versioning";

          } else if (hasAdminAcess &&
                  (aclService.getSiteActionPermission(LockPrerequisites.LOGS,
                          paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) > 0) && !
                   results.getDisabledTabs().contains(LockPrerequisites.LOGS)) {
              screenName = "logs";

          } else {
              screenName = "notools";
          }
      %>
      <% if (showEditMenu) { %>
      <%if(!screenName.equalsIgnoreCase("notools")){%>
        <li class="dex-TabBarItem" onclick="handleActionChange('<%=screenName%>');">
          <div class="display">
            <div>
              <span class="tab-icon ico-tool"><fmt:message key="org.jahia.engines.tools.label"/></span>
            </div>
          </div>
        </li>
      <%}%>
      <% } else { %>
          <%if(!screenName.equalsIgnoreCase("notools")){%>
              <li class="dex-TabBarItem-selected">
                <div class="display">
                  <div>
                    <span class="tab-icon ico-tool"><fmt:message key="org.jahia.engines.tools.label"/></span>
                  </div>
                </div>
              </li>
          <%}%>
      <% } %>
      <% } %>
      <li class="dex-TabBarRest"><div class="gwt-HTML">&nbsp;</div></li>
    </ul>
  <% } %>

