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
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final String engineURL = (String) engineMap.get("engineUrl");
final String objectTypeName = (String) engineMap.get("objectTypeName");
final String objectIDStr = engineMap.get("objectIDObj").toString();
final String objectName = (String) engineMap.get("objectName");
final String URLSep = (engineURL.indexOf("?") == -1) ? "?" : "&";
final String theScreen = (String) engineMap.get("screen");
%>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title"><fmt:message key="org.jahia.engines.EngineToolBox.flushLogEntries.label"/></div>
      </div>
      <div class="content-body">
        <div id="operationMenu">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:submittedCount++;window.location.href='<%=engineURL%><%=URLSep%>screen=logs&flush=2'">
                  <fmt:message key="org.jahia.engines.yes.label"/>
              </a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-cancel" href="javascript:submittedCount++;window.location.href='<%=engineURL%><%=URLSep%>screen=logs'">
                  <fmt:message key="org.jahia.engines.no.label"/>
              </a>
            </span>
          </span>
        </div>
      </div>
      <div class="content-body padded">
        <p align="left">
          <fmt:message key="org.jahia.engines.EngineToolBox.areYouSureDelete.label"/>&nbsp;<%=objectTypeName%>:
          <b><%=objectName%></b> ?
        </p>
        <input type="hidden" name="oid" value="<%=objectIDStr%>">
      </div>
    </div>
  </div>
</div>