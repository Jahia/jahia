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
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.validation.EngineValidationHelper" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
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
  <h2<% if ("update_container".equals(jspSource)) { %> class="edit"><fmt:message key="org.jahia.engines.updatecontainer.UpdateContainer_Engine.updateContainer.label"/><% } else { %> class="add"><fmt:message key="org.jahia.engines.addcontainer.AddContainer.label"/><% } %></h2>
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
                    <p class="error"><fmt:message key="org.jahia.engines.noLogs.label"/></p>
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