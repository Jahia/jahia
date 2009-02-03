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
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.validation.EngineValidationHelper" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>
<%
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
final String fieldForm = (String) engineMap.get(fieldsEditCallingEngineName + ".fieldForm");
final String engineUrl = (String) engineMap.get("engineUrl");    // used by static includes. Don't remove !
final String theScreen = (String) engineMap.get("screen");
final JahiaContainer theEditedContainer = (JahiaContainer) engineMap.get("theContainer");
final String logForm = (String) engineMap.get("logForm");
final EngineValidationHelper evh = (EngineValidationHelper) engineMap.get(JahiaEngine.ENGINE_VALIDATION_HELPER);
final int pageDefinitionID = jParams.getPage().getPageTemplateID();
final boolean showEditMenu = (theScreen.equals("edit") || theScreen.equals("metadata") || theScreen.equals("rightsMgmt") || theScreen.equals("timeBasedPublishing") || theScreen.equals("ctneditview_rights"));
request.setAttribute("showEditMenu", Boolean.valueOf(showEditMenu));
%>
<div id="header">
  <h1>Jahia</h1>
  <h2<% if ("update_container".equals(jspSource)) { %> class="edit"><internal:engineResourceBundle resourceName="org.jahia.engines.updatecontainer.UpdateContainer_Engine.updateContainer.label"/><% } else { %> class="add"><internal:engineResourceBundle resourceName="org.jahia.engines.addcontainer.AddContainer.label"/><% } %></h2>
  <jsp:include page="../navigation.jsp" flush="true" />
</div>
<div id="mainContent">
  <table class="dex-TabPanel" cellpadding="0" cellspacing="0" width="700">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
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
          <% if (theScreen.equals("edit") || theScreen.equals("metadata")) { %>
            <jsp:include page="../containeredit/containeredit.jsp" flush="true" />
          <% } %>
          <input type="hidden" name="addnew" value="false"/>
          <%if (theScreen.equals("logs")) {
            if (logForm != null) { %>
              <%=logForm%>
            <% } else { %>
              <div class="menuwrapper">
                <%@ include file="../tools.inc" %>
                <div class="content">
                  <div id="editor">
                    <p class="error"><internal:engineResourceBundle resourceName="org.jahia.engines.noLogs.label"/></p>
                  </div>
                </div>
                <div class="clearing">&nbsp;</div>
              </div>
            <%}
          } else if (! theScreen.equals("notools") && ! theScreen.equals("edit") && ! theScreen.equals("metadata")) { %>
            <%=fieldForm%>
          <% } %>
        </td>
      </tr>
    </tbody>
  </table>
  <jsp:include page="../buttons.jsp" flush="true" />
</div>