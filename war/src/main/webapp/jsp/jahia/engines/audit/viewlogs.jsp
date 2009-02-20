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
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>

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
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.EngineToolBox.administrativeAuditLog.label"/></div>
      </div>
      <% if (!(deletedRows.intValue() > 0)) { %>
        <% if (! (readOnlyMode && (results.getReadOnlyTabs().contains(LockPrerequisites.LOGS) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)))) { %>
          <div class="content-body">
            <div id="operationMenu">
              <span class="dex-PushButton">
                <span class="first-child">
                  <a class="ico-flush" href="javascript:submittedCount++;window.location.href='<%=engineURL%><%=URLSep%>screen=logs&flush=1'" title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.EngineToolBox.flush.label"/>"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.EngineToolBox.flush.label"/></a>
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
              <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.EngineToolBox.logDeletedPlural.label"/>
            <%} else {%>
              <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.EngineToolBox.logDeletedSingular.label"/>
            <% } %>
          </p>
        <% } %>
      </div>
    </div>
  </div>
</div>