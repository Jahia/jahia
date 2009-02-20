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
<%@ page import="org.jahia.bin.Jahia,
                 org.jahia.engines.timebasedpublishing.TimeBasedPublishingEngine,
                 org.jahia.hibernate.manager.JahiaObjectDelegate,
                 org.jahia.services.timebasedpublishing.RetentionRuleDef,
                 java.util.*" %>
<%@ page import="org.jahia.services.timebasedpublishing.RetentionRule"%>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
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
<div class="menuwrapper">
   <%@ include file="../menu.inc" %>
    <div class="content">
        <div id="editor">
            <h4 class="clock">
                <utility:resourceBundle resourceBundle="JahiaInternalResources"
                        resourceName="org.jahia.engines.include.actionSelector.TimeBasedPublishing.label"/>
            </h4>

            <table>
                <caption>
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.timebasedpublishing.status.label"/>
                </caption>
                <tr>
                    <td class="t1">
                        <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                resourceName="org.jahia.engines.timebasedpublishing.currentstatus.label"/>

                    </td>
                    <td class="t22">
                        <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                resourceName="<%=labelResourceName%>"/>
                    </td>
                </tr>
                <tr>
                    <td class="t1">
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.servertime.label"/>
                    </td>
                    <td class="t22">
                        <span id="serverTime"/>
                    </td>
                </tr>
            </table>

            <table>
                <caption>
                    <utility:resourceBundle resourceBundle="JahiaInternalResources"
                            resourceName="org.jahia.engines.timebasedpublishing.retentionrule.label"/>
                </caption>
                <tr>
                    <td class="t1"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                            resourceName="org.jahia.engines.timebasedpublishing.currentrule.label"/></td>
                    <td class="t22">
                        <% final Iterator iterator = ruleDefs.iterator();
                            while (iterator.hasNext()) {
                                final RetentionRuleDef ruleDef = (RetentionRuleDef) iterator.next();
                                if (String.valueOf(ruleDef.getId().intValue()).equals(selectedRuleDef)) { %>
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="<%=ruleDef.getBundleKey() %>"/>
                        <% } %>
                        <% } %>
                    </td>
                </tr>
            </table>

            <% if (currentRuleDef != null && currentRule != null) { %>
            <%

            %>
            <table>
                <caption><utility:resourceBundle resourceBundle="JahiaInternalResources"
                        resourceName="org.jahia.engines.timebasedpublishing.rule.settings.label"/></caption>
                <tr>
                    <td class="t1"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                            resourceName="org.jahia.engines.timebasedpublishing.inheritfromparent.label"/>
                    </td>
                    <td class="t22">
                        <input class="input" type="radio" disabled="disabled" name="inherited" value="1" <% if ( inherited ){ %>
                               checked="checked"<% } %>>
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.yes.label"/>&nbsp;
                        <input class="input" type="radio" disabled="disabled" name="inherited" value="0" <% if ( !inherited ){ %>
                               checked="checked"<% } %>>
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.no.label"/>
                    </td>
                </tr>
                <% if (!inherited) { %>
                <tr id="rangerule.settings1">
                    <td class="t1">
                        <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                resourceName="org.jahia.engines.timebasedpublishing.rangerule.validFrom.label"/>
                    </td>
                    <td class="t22">
                        &nbsp;
                    </td>
                </tr>
                <tr id="rangerule.settings2">
                    <td class="t1">
                        <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                resourceName="org.jahia.engines.timebasedpublishing.rangerule.validTo.label"/>
                    </td>
                    <td class="t22">
                        &nbsp;
                    </td>
                </tr>
                <% } %>
            </table>
            <% } else { %>
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.timebasedpublishing.rule.none.label"/>
            <% } %>

        </div>
    </div>

   <div class="clearing">&nbsp;</div>
</div>

<script type="text/javascript">
    var tmpDate = new Date();
    var timeOffSet = tmpDate.getTimezoneOffset();
    getServerTime('<%=Jahia.getContextPath()%>', 'serverTime', 'HH:mm:ss', timeOffSet * 60 * 1000 * -1);
    setInterval("getServerTime('<%=Jahia.getContextPath()%>','serverTime','HH:mm:ss',timeOffSet * 60 * 1000 * -1)", 5000);
</script>

