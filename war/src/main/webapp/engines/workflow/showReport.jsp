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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
%>
<!-- Begin showReport.jsp -->
<style type="text/css">
    table.validationResults tr.header td {
        font-weight: bold;
    }

    table.validationResults tr td {
        text-align: center !important;
    }

    table.validationResults img {
        border: none;
    }
</style>
<script type="text/javascript">
    window.onunload = null;
    function refreshTree() {
        window.opener.refreshTree();
    }
</script>

<div id="header">
  <h1>Jahia</h1>
  <h2 class="workflow"><fmt:message key="org.jahia.engines.workflow.WorkflowEngine.workflowreport.label"/></h2>
</div>
<div id="mainContent">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom-full">
            <div class="tabContent">
              <div id="content" class="full">
                <c:forEach items="${viewHelper.resultGroups}" var="group">
                  <div class="head">
                    <div class="object-title">
                      <bean:define id="labelKey" name="group" property="labelKey" type="java.lang.String"/>
                      <span class="<c:out value='${group.warningSeverity}'/>" style="font-weight: bold">${fn:length(group.results)}&nbsp;<fmt:message key="<%=labelKey %>"/></span>
                    </div>
                  </div>
                  <table class="evenOddTable bordered" border="0" cellpadding="5" cellspacing="0" width="100%">
                    <thead>
                      <tr class="header">
                        <th>&nbsp;</th>
                        <th>
                          <fmt:message key="org.jahia.engines.deletecontainer.DeleteContainer_Engine.type.label"/>
                        </th>
                        <th>
                          <fmt:message key="org.jahia.engines.deletecontainer.DeleteContainer_Engine.id.label"/>
                        </th>
                        <th>
                          <fmt:message key="org.jahia.engines.deletecontainer.DeleteContainer_Engine.title.label"/>
                        </th>
                        <th>
                          <fmt:message key="org.jahia.engines.deletecontainer.DeleteContainer_Engine.pageId.label"/>
                        </th>
                        <th>
                          <fmt:message key="org.jahia.engines.deletecontainer.DeleteContainer_Engine.pageTitle.label"/>
                        </th>
                        <th>
                          <fmt:message key="org.jahia.engines.shared.Page_Field.languages.label"/>
                        </th>
                        <th class="lastCol">
                          <fmt:message key="org.jahia.engines.version.warning"/>
                        </th>
                      </tr>
                    </thead>
                    <c:forEach items="${group.results}" var="obj">
                      <bean:define id="targetPageId" name="obj" property="pageId" type="java.lang.String"/>
                      <% String targetPageUrl = jData.gui().drawPageLink(Integer.parseInt(targetPageId)); %>
                      <c:set var="lineCss" value="${lineCss == 'oddLine' ? 'evenLine' : 'oddLine'}" />
                      <tr class="<c:out value="${lineCss}"/>">
                        <td>
                          <a onclick="window.onunload=refreshTree;" href="javascript:<c:out value='${obj.url}'/>" title="<fmt:message key='org.jahia.engines.title.UpdateContainer'/>">
                            <img src="<%=request.getContextPath() %>/engines/images/filemanager/edit_hover.gif" border="0" alt="link" title="<fmt:message key='org.jahia.engines.title.UpdateContainer'/>"/></a>
                        </td>
                        <td>
                          <c:out value="${obj.objectType}"/>
                        </td>
                        <td>
                          <c:out value="${obj.objectId}"/>
                        </td>
                        <td>
                          <c:out value="${obj.title}"/>
                        </td>
                        <td>
                          <a href="<%= targetPageUrl %>" target="_blank"><c:out value="${obj.pageId}"/></a>
                        </td>
                        <td>
                          <a href="<%= targetPageUrl %>" target="_blank"><c:out value="${obj.pageTitle}"/></a>
                        </td>
                        <td>
                          <c:out value="${obj.language}"/>
                        </td>
                        <td style="text-align: left !important;" class="lastCol">
                          <c:out value="${obj.message}" escapeXml="false"/>
                        </td>
                      </tr>
                    </c:forEach>
                  </table>
                </c:forEach>
              </div>
            </div>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
  <div id="actionBar">
    <span class="dex-PushButton">
      <span class="first-child">
        <a class="ico-delete" href="javascript:window.close();" title="<fmt:message key="org.jahia.close.button"/>"><fmt:message key="org.jahia.close.button"/></a>
      </span>
    </span>
</div>
</div>

<!-- End showReport.jsp -->