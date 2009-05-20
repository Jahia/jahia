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
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final List logData = (List) engineMap.get("logData");
final String engineURL = (String) engineMap.get("engineUrl");
final Integer deletedRows = (Integer) engineMap.get("deletedRows");
int areaSize = 70;
final String theScreen = (String) engineMap.get("screen");
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
if (elh != null) {
    jParams.setCurrentLocale(elh.getCurrentLocale());
}
final String URLSep = (engineURL.indexOf("?") == -1) ? "?" : "&";
String userAgent = request.getHeader("user-agent");
if (userAgent != null) {
    if (userAgent.indexOf("MSIE") != -1) {
        areaSize = 85;
    }
}

%>
<utility:setBundle basename="JahiaInternalResources"/>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title"><fmt:message key="org.jahia.engines.EngineToolBox.administrativeAuditLog.label"/></div>
      </div>
      <% if (!(deletedRows.intValue() > 0)) { %>
        <% if (! (readOnlyMode && (results.getReadOnlyTabs().contains(LockPrerequisites.LOGS) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)))) { %>
          <div class="content-body">
            <div id="operationMenu">
              <span class="dex-PushButton">
                <span class="first-child">
                  <a class="ico-flush" href="javascript:submittedCount++;window.location.href='<%=engineURL%><%=URLSep%>screen=logs&flush=1'" title="<fmt:message key="org.jahia.engines.EngineToolBox.flush.label"/>"><fmt:message key="org.jahia.engines.EngineToolBox.flush.label"/></a>
                </span>
              </span>
            </div>
          </div>
        <% } %>
      <% } %>
      <div class="padded">
        <textarea class="logs" name="objectLogs" cols="<%=areaSize%>" rows="18">
          <%
          Long start = new Long(0);
          for(int i=0; i < logData.size(); i++) {
              final Map logRecord = (Map) logData.get(i);
              final Long tempStart = (Long) logRecord.get("starttime");
            if (tempStart != null && !start.equals(tempStart)) {
              out.println("");
              start = tempStart;
            }
          %><%= logRecord.get("timeStr")   %>  <%=
                logRecord.get("username")  %>  <%=
                logRecord.get("operation") %> : <%=
                logRecord.get("objectname")%> (<%=
                logRecord.get("objectid")  %>) to <%= logRecord.get("parenttype")%> <%= logRecord.get("parentname") %> (<%= logRecord.get("parentid")%>)
          <% } %>
        </textarea>
        <% if (deletedRows.intValue() > 0) { %>
          <p class="error">
            <%=deletedRows%>
            <% if (deletedRows.intValue() > 1) {%>
              <fmt:message key="org.jahia.engines.EngineToolBox.logDeletedPlural.label"/>
            <%} else {%>
              <fmt:message key="org.jahia.engines.EngineToolBox.logDeletedSingular.label"/>
            <% } %>
          </p>
        <% } %>
      </div>
    </div>
  </div>
</div>