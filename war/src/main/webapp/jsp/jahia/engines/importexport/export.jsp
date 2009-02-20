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
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
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
         <div class="object-title"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.Export.label"/></div>
      </div>

      <% if ((readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EXPORT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) && ! ((Boolean) engineMap.get("hasActiveEntries")).booleanValue()) { %>
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.unavailable.label"/>
      <% } else { %>
        <div class="content-body padded">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-export" href="#" onclick="doExport(); return false;" title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.Export.label"/>"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.Export.label"/></a>
            </span>
          </span>
        </div>
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <tr>
            <th><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.type.label"/></th>
            <td>
              <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EXPORT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %>
                 <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.completeLiveContent.label"/>
              <% } else { %>
                <select name="exporttype" class="input">
                  <% if (((Boolean) engineMap.get("hasActiveEntries")).booleanValue()) { %>
                    <option value="complete"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.completeLiveContent.label"/></option>
                  <% } %>
                  <option value="staging"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.completeStagingContent.label"/></option>
                  <option value="diff"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.Differential.label"/></option>
                </select>
              <% } %>
            </td>
          </tr>
          <tr>
            <th><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.format.label"/></th>
            <td class="t22">
              <select name="exportformat" class="input">
                <option value="xml"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.xml.label"/></option>
                <option value="zipnofiles"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.zipnofiles.label"/></option>
                <option value="zipfiles"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.importexport.export.zipfiles.label"/></option>
              </select>
            </td>
          </tr>
        </table>
      <% } %>
    </div>
  </div>
</div>