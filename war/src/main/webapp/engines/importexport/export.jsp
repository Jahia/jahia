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
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final String theScreen = (String) engineMap.get("screen");
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
final String exportUrl = (String) engineMap.get("exportUrl");
final String objectKey = (String) engineMap.get("objectKey");
EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
if (elh != null) {
    jParams.setCurrentLocale(elh.getCurrentLocale());
}
%>
<utility:setBundle basename="JahiaInternalResources"/>
<script type="text/javascript">
  var myWin = null;
  function doExport() {
      if (document.mainForm.exportformat.value == "xml") {
          params = "status=no";
          myWin = window.open("<%=exportUrl%>/export_<%=objectKey%>.xml?key=<%=objectKey%>&exporttype=" + document.mainForm.exporttype.value + "&exportformat=" + document.mainForm.exportformat.value, "Export", params);
      } else {
          window.location.href = "<%=exportUrl%>/export_<%=objectKey%>.zip?key=<%=objectKey%>&exporttype=" + document.mainForm.exporttype.value + "&exportformat=" + document.mainForm.exportformat.value;
      }
  }
</script>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../tools.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
         <div class="object-title"><fmt:message key="org.jahia.engines.include.actionSelector.Export.label"/></div>
      </div>

      <% if ((readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EXPORT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) && ! ((Boolean) engineMap.get("hasActiveEntries")).booleanValue()) { %>
        <fmt:message key="org.jahia.engines.importexport.export.unavailable.label"/>
      <% } else { %>
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <tr>
            <th><fmt:message key="org.jahia.engines.importexport.export.type.label"/></th>
            <td>
              <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EXPORT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %>
                 <fmt:message key="org.jahia.engines.importexport.export.completeLiveContent.label"/>
              <% } else { %>
                <select name="exporttype" class="input">
                  <% if (((Boolean) engineMap.get("hasActiveEntries")).booleanValue()) { %>
                    <option value="complete"><fmt:message key="org.jahia.engines.importexport.export.completeLiveContent.label"/></option>
                  <% } %>
                  <option value="staging"><fmt:message key="org.jahia.engines.importexport.export.completeStagingContent.label"/></option>
                  <option value="diff"><fmt:message key="org.jahia.engines.importexport.export.Differential.label"/></option>
                </select>
              <% } %>
            </td>
          </tr>
          <tr>
            <th><fmt:message key="org.jahia.engines.importexport.export.format.label"/></th>
            <td class="t22">
              <select name="exportformat" class="input">
                <option value="xml"><fmt:message key="org.jahia.engines.importexport.export.xml.label"/></option>
                <option value="zipnofiles"><fmt:message key="org.jahia.engines.importexport.export.zipnofiles.label"/></option>
                <option value="zipfiles"><fmt:message key="org.jahia.engines.importexport.export.zipfiles.label"/></option>
              </select>
            </td>
          </tr>
        </table>
        <div class="content-body padded">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-export" href="#" onclick="doExport(); return false;" title="<fmt:message key="org.jahia.engines.include.actionSelector.Export.label"/>"><fmt:message key="org.jahia.engines.include.actionSelector.Export.label"/></a>
            </span>
          </span>
        </div>        
      <% } %>
    </div>
  </div>
</div>