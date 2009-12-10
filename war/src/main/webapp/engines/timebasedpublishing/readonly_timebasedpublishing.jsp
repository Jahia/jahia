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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.bin.Jahia,
                 org.jahia.engines.timebasedpublishing.TimeBasedPublishingEngine,
                 org.jahia.hibernate.manager.JahiaObjectDelegate,
                 org.jahia.services.timebasedpublishing.RetentionRuleDef,
                 java.util.*" %>
<%@ page import="org.jahia.services.timebasedpublishing.RetentionRule"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final Map subEngineMap = (Map) engineMap.get(TimeBasedPublishingEngine.SUB_ENGINE_MAP_NAME);
    final String theScreen = (String) engineMap.get("screen");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }

    final List ruleDefs = (List) subEngineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".ruleDefs");
    final RetentionRuleDef currentRuleDef = (RetentionRuleDef) subEngineMap.get(
            TimeBasedPublishingEngine.ENGINE_NAME + ".currentRuleDef");
    final JahiaObjectDelegate jahiaObjectDelegate = (JahiaObjectDelegate) subEngineMap.get(
            TimeBasedPublishingEngine.ENGINE_NAME + ".jahiaObjectDelegate");
    final String selectedRuleDef;
    if (currentRuleDef != null) {
        selectedRuleDef = String.valueOf(currentRuleDef.getId());
    } else {
        selectedRuleDef = "none";
    }

    final String labelResourceName = "org.jahia.engines.timebasedpublishing.timebpstatus." +
            jahiaObjectDelegate.getTimeBPState() + ".label";

    boolean inherited = false;
    final RetentionRule currentRule = (RetentionRule) subEngineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".currentRule");
    if (currentRule != null) {
        inherited = currentRule.getInherited().booleanValue();
    }
%>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<div class="dex-TabPanelBottom">
  <div class="tabContent">
   <%@ include file="../menu.inc" %>
    <div id="content" class="fit w2">
          <div class="head">
             <div class="object-title"><fmt:message key="org.jahia.engines.timebasedpublishing.status.label"/></div>
          </div>
          <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
            <tr>
              <th width="120">
                <fmt:message key="org.jahia.engines.timebasedpublishing.currentstatus.label"/>
              </th>
              <td>
                <fmt:message key="<%=labelResourceName%>"/>
              </td>
            </tr>
            <tr>
              <th>
                <fmt:message key="org.jahia.engines.servertime.label"/>
              </th>
              <td>
                <span id="serverTime"/>
              </td>
            </tr>
          </table>

      <% if (currentRuleDef != null) { %>
        <jsp:include page="/engines/timebasedpublishing/readonly_rangerule.jsp" flush="true"/>
            <% } else { %>
            <fmt:message key="org.jahia.engines.timebasedpublishing.rule.none.label"/>
            <% } %>

    </div>

   <div class="clearing">&nbsp;</div>
</div>
</div>

<script type="text/javascript">
    var tmpDate = new Date();
    var timeOffSet = tmpDate.getTimezoneOffset();
    getServerTime('<%=Jahia.getContextPath()%>', 'serverTime', 'HH:mm:ss', timeOffSet * 60 * 1000 * -1);
    setInterval("getServerTime('<%=Jahia.getContextPath()%>','serverTime','HH:mm:ss',timeOffSet * 60 * 1000 * -1)", 5000);
</script>

