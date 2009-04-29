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
