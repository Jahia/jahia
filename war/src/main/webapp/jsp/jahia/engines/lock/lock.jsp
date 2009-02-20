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
  <h2 class="lock"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.lock.jahiaLocks.label"/></h2>
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
                  <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.lock.lockPrerequisites.label"/>
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
                            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.lock.resourceLockedForModification.label"/>
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
                                        title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.engines.lock.stealLock.label'/>"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.lock.stealLock.label"/></a>
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