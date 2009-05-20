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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.services.lock.LockPrerequisitesResult" %>
<%@ page import="org.jahia.services.lock.LockRegistry" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ page import="org.jahia.engines.addcontainer.AddContainer_Engine"%>
<%@ page import="org.jahia.engines.updatecontainer.UpdateContainer_Engine"%>
<%@ page import="org.jahia.services.lock.LockKey" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>

<%!
private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("jsp.jahia.engines.lock.lock");
%>
<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
final boolean showEditMenu = false;
request.setAttribute("showEditMenu", Boolean.valueOf(showEditMenu));
final LockPrerequisitesResult lockPrerequisitesResult = (LockPrerequisitesResult) engineMap.get("lockPrerequisitesResult");
final LockKey lockKey = (LockKey) engineMap.get("lockKey");
%>
<div id="header">
  <h1>Jahia</h1>
  <h2 class="lock"><fmt:message key="org.jahia.engines.lock.jahiaLocks.label"/></h2>
  <% if (session.getAttribute("showNavigationInLockEngine") != null) { %>
    <jsp:include page="../navigation.jsp" flush="true" />
  <% } %>
</div>
<div id="mainContent">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left" width="100%">
          <div class="dex-TabBar">
            <jsp:include page="../menuBar.jsp" flush="true" />
          </div>
        </td>
        <td style="vertical-align: top;" align="right" nowrap="nowrap">
          <jsp:include page="../multilanguage_links.jsp" flush="true" />
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%" colspan="2">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
              <div id="content" class="full">
                <% if (lockPrerequisitesResult == null) { %>
                  <fmt:message key="org.jahia.engines.lock.lockPrerequisites.label"/>
                <% } else {
                    final List resultsList = lockPrerequisitesResult.getResultsList();
                  %>
                  <input type="hidden" name="redirectedToLockEngine" value="yes"/>
                  <input type="hidden" name="lockKey" value="<%=lockKey%>"/>
                  <ul>
                    <%
                    final LockRegistry lockRegistry = LockRegistry.getInstance();
                    boolean invertText = false;
                    boolean isAlreadyLocked = false;
                    boolean isAnyCheckBox = false;
                    for (int i = 0; i < resultsList.size(); i++) {
                      invertText = !invertText;
                      final LockKey blockingLockKey = (LockKey) resultsList.get(i);
                      isAlreadyLocked = lockKey.equals(blockingLockKey);
                      boolean canDisplayDetails = true; // Set in the included file "lock_messages.inc"
                      boolean isSameContext = false;
                      %>
                      <li class="noStyle">
                        <span class="celllarge">
                          <% if (isAlreadyLocked) { %>
                            <fmt:message key="org.jahia.engines.lock.resourceLockedForModification.label"/>
                          <% } else { %>
                            <span class="msg-alert-info"><%= LockKey.getFriendlyMessage(blockingLockKey, jParams) %></span>
                          <% } %>
                        </span>
                        <span class="cellsmall">
                          <% if (canDisplayDetails && lockRegistry.hasAdminRights(blockingLockKey, jParams.getUser()) || isSameContext) { isAnyCheckBox = true; %>
                            <input type="hidden" id="<%=blockingLockKey%>" name="<%=blockingLockKey%>"  value="true" disabled="disabled"/>
                            <span class="dex-PushButton">
                                <span class="first-child">
                                    <a class="ico-remove-lock" href="#steal" onclick="javascript:{document.getElementById('<%=blockingLockKey%>').disabled = false; sendFormSave(); return false;}" 
                                        title="<fmt:message key='org.jahia.engines.lock.stealLock.label'/>"><fmt:message key="org.jahia.engines.lock.stealLock.label"/></a>
                                    </span>
                              </span>
                          <% } else { %>
                            &nbsp;
                          <% } %>
                        </span>
                      </li>
                    <% } %>
                  </ul>
                  <div class="clearing">&nbsp;</div>
                <% } %>
              </div>
            </div>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
  <% engineMap.put("noApply", ""); %>
  <jsp:include page="../buttons.jsp" flush="true" />
</div>