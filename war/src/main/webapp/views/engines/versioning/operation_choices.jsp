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

<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.views.engines.*" %>
<%@ page import="org.jahia.views.engines.versioning.ContentVersioningViewHelper" %>
<%@ include file="/views/engines/common/taglibs.jsp" %>
<%

    String actionURL = (String)request.getAttribute("ContentVersioning.ActionURL");
    String engineView = (String)request.getAttribute("engineView");
    final ContentVersioningViewHelper versViewHelper =
            (ContentVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

    final Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
    final String theScreen = (String) engineMap.get("screen");
%>
<!-- operation_choices.jsp (start) -->

<%@include file="common-javascript.inc" %>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../../../engines/tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title">
          <c:if test="${requestScope.jahiaEngineViewHelper.restoringPage}">
            <fmt:message key="org.jahia.engines.include.actionSelector.PageVersioning.label"/>
          </c:if>
          <c:if test="${requestScope.jahiaEngineViewHelper.restoringContainer}">
            <fmt:message key="org.jahia.engines.include.actionSelector.ContainerVersioning.label"/>
          </c:if>
          <c:if test="${requestScope.jahiaEngineViewHelper.restoringContainerList}">
            <fmt:message key="org.jahia.engines.include.actionSelector.ContainerListVersioning.label"/>
          </c:if>
          &nbsp;-&nbsp;<fmt:message key="org.jahia.engines.version.stepOneOfThree"/>
        </div>
      </div>
      <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
        <tr>
          <td colspan="2">
            <strong><fmt:message key="org.jahia.engines.version.selectTheTaskToPerform"/></strong>.
          </td>
        </tr>
        <tr>
          <th valign="top" width="100">
            <fmt:message key="org.jahia.engines.version.availableTasks"/>:
          </th>
          <td>
            <ul class="noStyle">
              <li>
                <input type="radio" name="operationType" value="1" <%if(versViewHelper.getOperationType()==1){%> checked="checked"<%}%>>
                A)&nbsp;<fmt:message key="org.jahia.engines.version.undoStagingModification"/>
              </li>
              <li>
                <input type="radio" name="operationType" value="2" <%if(versViewHelper.getOperationType()==2){%> checked=checked<%}%>>
                B)&nbsp;<fmt:message key="org.jahia.engines.version.restoreArchivedContent"/>
              </li>
              <c:if test="${!empty versioningUndeletePage }">
                <li>
                  <input type="radio" name="operationType" value="3" <%if(versViewHelper.getOperationType()==3){%> checked=checked<%}%>>
                  C)&nbsp;<fmt:message key="org.jahia.engines.version.restoreDeletedPages"/>
                </li>
              </c:if>
              <c:if test="${!empty versioningUndeleteContainer }">
                <li>
                  <input type="radio" name="operationType" value="3" <%if(versViewHelper.getOperationType()==3){%> checked=checked<%}%>>
                  C)&nbsp;<fmt:message key="org.jahia.engines.version.restoreDeletedContainer"/>
                </li>
              </c:if>
            </ul>
          </td>
        </tr>
      </table>
    </div>
  </div>
</div>
<!-- operation_choices.jsp (end) -->
