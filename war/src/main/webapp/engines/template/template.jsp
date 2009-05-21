<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.services.pages.JahiaPageDefinitionTemp" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>

<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String warningMsg = (String) request.getAttribute("Template_Engine.warningMsg");

    final JahiaPageDefinitionTemp theTempoTemplate = (JahiaPageDefinitionTemp) engineMap.get("theTemporaryTemplate");
    pageContext.setAttribute("template", theTempoTemplate);

    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final String fieldForm = (String) engineMap.get(fieldsEditCallingEngineName + "." + "fieldForm");
    final String logForm = (String) engineMap.get("logForm");
    final String engineUrl = (String) engineMap.get("engineUrl");
    final String theScreen = (String) engineMap.get("screen");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final JahiaData jData = (JahiaData) jParams.getRequest().getAttribute("org.jahia.data.JahiaData");

    final int inputSize = jData.gui().isIE() ? 65 : 40;
    final boolean showEditMenu = (theScreen.equals("edit") || theScreen.equals("rightsMgmt"));
    request.setAttribute("showEditMenu", new Boolean(showEditMenu));
%>
<%-- Begin template.jsp --%>
<script type="text/javascript">
    <!--
    function setVisible(who){

        if ( who.checked ){
            document.mainForm.templateAvailable.checked = true;
        }
    }
    //-->
</script>

<div id="header">
  <h1>Jahia</h1>
  <h2 class="template"><fmt:message key="org.jahia.engines.template.Template_Engine.templateSettings.label"/></h2>
  <jsp:include page="../navigation.jsp" flush="true" />
</div>
<div id="mainContent">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left" width="100%">
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
          <% if (theScreen.equals("edit")) { %>
            <div class="dex-TabPanelBottom">
              <div class="tabContent">
                <% if (showEditMenu) { %>
                  <%@ include file="../menu.inc" %>
                <% } else { %>
                  <%@ include file="../tools.inc" %>
                <% } %>
                <div id="content" class="fit w2">
                  <div class="head">
                    <div class="object-title"><fmt:message key="org.jahia.engines.template.Template_Engine.templateSettings.label"/></div>
                  </div>
                  <% if (! warningMsg.equals("")) { %>
                    <p class="errorbold"><fmt:message key="org.jahia.warning.label"/></p>
                    <p class="error"><%=warningMsg%></p>
                  <% } %>

                  <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
                    <tr>
                      <th><fmt:message key="org.jahia.engines.name.label"/></th>
                      <td>
                          <input type="text" name="templateName" value="${template.name}" size="<%=inputSize%>" readonly="readonly">
                      </td>
                    </tr>

                    <tr>
                      <th>
                        <fmt:message key="org.jahia.engines.template.Template_Engine.sourcePath.label"/>
                      </th>
                      <td>
                        <input type="text" name="sourcePath" value="${template.sourcePath}" readonly="readonly" size="100">
                      </td>
                    </tr>
<%-- 
                    <tr>
                      <th>
                        <fmt:message key="org.jahia.engines.template.Template_Engine.relativeTo.label"/>
                      </th>
                      <td class="t22">
                        <%=jParams.settings().getTemplatesContext() + jParams.getSite().getSiteKey() + "/" %>
                      </td>
                    </tr>
--%>                    
                    <tr>
                      <th><label for="templateAvailable"><fmt:message key="org.jahia.engines.available.label"/></label></th>
                      <td>
                        <c:if test="${template.available}">
                            <input type="hidden" name="templateAvailable" value="yes"/>
                        </c:if>
                        <input type="checkbox" id="templateAvailable" name="templateAvailable_hide" value="yes" ${template.available ? 'checked="checked"' : ''} disabled="disabled"/>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <label for="templateDefault"><fmt:message key="org.jahia.engines.template.Template_Engine.isTheDefault.label"/></label>
                      </th>
                      <td>
                        <c:if test="${template.default}">
                            <input type="hidden" name="templateDefault" value="yes"/>
                        </c:if>
                        <input type="checkbox" id="templateDefault" name="templateDefault_hide" value="yes" ${template.default ? 'checked="checked"' : ''} disabled="disabled"/>
                      </td>
                    </tr>
                  </table>
                </div>
              </div>
            </div>
          <% } else if (theScreen.equals("rightsMgmt")) { %>
            <%=fieldForm%>
          <% } else if (theScreen.equals("logs")) { %>
            <%=logForm%>
          <% } %>
        </td>
      </tr>
    </tbody>
  </table>
  <jsp:include page="../buttons.jsp" flush="true" />
</div>
<%-- End template.jsp --%>