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
<%@ page import="org.jahia.params.ParamBean" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final String engineUrl = (String) engineMap.get("engineUrl");
final String selectUGEngine = (String) engineMap.get("selectUGEngine");
final String theScreen = (String) engineMap.get("screen");
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
%>
<!-- FIXME : The following javascript file path are hardcoded. -->
<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/selectbox.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/help.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/checkbox.js"></script>
<script type="text/javascript">
  var defaultAction = "searchUsers";
  window.onunload = null;

  function sendForm(action) {
    defaultAction = action;
    document.mainForm.screen.value = action;
    // document.mainForm.action = "<%=engineUrl%>" + action;
    teleportCaptainFlam(document.mainForm);
  }

  function pasteSelection() {
    if (window.opener.closed) {
        //alert('ACL entries closed');
    } else {
      for (i = 0; i < document.mainForm.selectedUG.length; i++) {
        if ((document.mainForm.selectedUG.options[i].selected) && (document.mainForm.selectedUG.options[i].value != "null")) {
          window.opener.addOptions(document.mainForm.selectedUG.options[i].text, document.mainForm.selectedUG.options[i].value);
          document.mainForm.selectedUG.options[i].selected = false;
        }
      }
      window.opener.addOptionsBalance();
    }
  }

  function pasteSelectionClose() {
    pasteSelection();
    document.mainForm.screen.value = "save";
    teleportCaptainFlam(document.mainForm);
    window.close();
  }
</script>

<div id="header">
  <h1>Jahia</h1>
  <h2>
    <% if (selectUGEngine.equals("selectUsers")) { %>
      <fmt:message key="org.jahia.engines.users.SelectUG_Engine.newUsers.label"/>
    <% } else { %>
      <fmt:message key="org.jahia.engines.users.SelectUG_Engine.newGroups.label"/>
    <% } %>
  </h2>
</div>
<div id="mainContent">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left" width="100%">
          <div class="dex-TabBar">
            <!-- Menubar -->
            <ul class="dex-TabBarItem-wrapper" style="width: 100%;">
              <li class="dex-TabBarFirst"><div class="gwt-HTML">&nbsp;</div></li>
              <li class="dex-TabBarItem<% if(selectUGEngine.equals("selectGroups")) { %>-selected<%}%>">
                <div class="display">
                  <div>
                    <span class="tab-icon ico-data">
                      <a href="javascript:handleActionChange('searchGroups')">
                        <fmt:message key="org.jahia.engines.users.SelectUG_Engine.groupList.label"/></a>
                    </span>
                  </div>
                </div>
              </li>
              <li class="dex-TabBarItem<% if(selectUGEngine.equals("selectUsers")) { %>-selected<%}%>">
                <div class="display">
                  <div>
                    <span class="tab-icon ico-data">
                      <a href="javascript:handleActionChange('searchUsers')">
                        <fmt:message key="org.jahia.engines.users.SelectUG_Engine.userList.label"/></a>
                    </span>
                  </div>
                </div>
              </li>
              <li class="dex-TabBarRest"><div class="gwt-HTML">&nbsp;</div></li>
            </ul>
            <!-- End Menubar -->
          </div>
        </td>
        <td style="vertical-align: top;" align="right" nowrap="nowrap">
          <!-- jsp:include page="../multilanguage_links.jsp" flush="true" / -->
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%" colspan="2">
          <div class="dex-TabPanelBottom-full">
            <div class="tabContent">
              <div id="content">
                <div class="content-body-noborder">
                  <% if (selectUGEngine.equals("selectUsers")) { %>
                    <%@include file="selectusers.inc"%>
                  <% } else { %>
                    <%@include file="selectgroups.inc"%>
                  <% } %>
                  <input type="hidden" name="subaction" value=""/>
                </div>
              </div>
            </div>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
  <!-- Buttons -->
  <div id="actionBar">
    <span class="dex-PushButton">
      <span class="first-child">
        <a class="ico-ok" href="javascript:pasteSelectionClose();" title="<fmt:message key="org.jahia.engines.users.SelectUG_Engine.pasteAndClose.label"/>">
          <fmt:message key="org.jahia.button.ok"/></a>
      </span>
    </span>
    <span class="dex-PushButton">
      <span class="first-child">
        <a class="ico-add" href="javascript:pasteSelection();" title="<fmt:message key="org.jahia.engines.users.SelectUG_Engine.pasteWithoutClose.label"/>">
          <fmt:message key="org.jahia.button.add"/></a>
      </span>
    </span>
    <span class="dex-PushButton">
      <span class="first-child">
        <a class="ico-cancel" href="javascript:window.close();" title="<fmt:message key="org.jahia.altCloseWithoutSave.label"/>">
          <fmt:message key="org.jahia.button.cancel"/></a>
      </span>
    </span>
  </div>
  <!-- End Buttons -->
</div>
