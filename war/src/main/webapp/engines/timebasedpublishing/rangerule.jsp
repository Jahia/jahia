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
<%@ page import="org.jahia.services.timebasedpublishing.BaseRetentionRule" %>
<%@ page import="org.jahia.engines.calendar.CalendarHandler" %>
<%@ page import="org.jahia.engines.timebasedpublishing.TimeBasedPublishingEngine" %>
<%@ page import="org.jahia.services.timebasedpublishing.*" %>
<%@page import="org.jahia.services.usermanager.JahiaUser" %>
<%@page import="org.jahia.services.usermanager.JahiaAdminUser" %>
<%@page import="org.jahia.hibernate.manager.JahiaObjectDelegate" %>
<%@page import="org.jahia.content.ObjectKey" %>
<%@page import="org.jahia.registries.ServicesRegistry" %>
<%@page import="org.jahia.services.version.EntryLoadRequest" %>
<%@page import="org.jahia.params.ParamBean" %>
<%@page import="org.jahia.hibernate.manager.SpringContextSingleton" %>
<%@page import="org.jahia.hibernate.manager.JahiaObjectManager" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="ui" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    boolean inherited = false;
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final Map subEngineMap = (Map) engineMap.get(TimeBasedPublishingEngine.SUB_ENGINE_MAP_NAME);
    final CalendarHandler fromDateCalHandler = (CalendarHandler) subEngineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".fromDateCalHandler");
    final CalendarHandler toDateCalHandler = (CalendarHandler) subEngineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".toDateCalHandler");
    BaseRetentionRule currentRule = (BaseRetentionRule) subEngineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".currentRule");
    Boolean enableImmediatePublication = (Boolean) subEngineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".enableImmediatePublication");
    if (currentRule != null) {
        inherited = currentRule.getInherited();
    }
    if (inherited) {
        JahiaObjectDelegate jahiaObjectDelegate = (JahiaObjectDelegate) subEngineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".jahiaObjectDelegate");
        ObjectKey objectKey = jahiaObjectDelegate.getObjectKey();
        JahiaUser adminUser = JahiaAdminUser.getAdminUser(jahiaObjectDelegate.getSiteId().intValue());
        if (objectKey.getIdInType() < 0) {
            JahiaContainer container = (JahiaContainer) engineMap.get("addcontainer.theContainer");
            if (container != null) {
                objectKey = new ContentContainerListKey(container.getListID());
            }
        }
        final TimeBasedPublishingService tbpService = ServicesRegistry.getInstance().getTimeBasedPublishingService();
        ObjectKey currentObjectKey = tbpService.getParentObjectKeyForTimeBasedPublishing(objectKey, adminUser, EntryLoadRequest.STAGED, ParamBean.EDIT, true);
        if (currentObjectKey != null) {
            final JahiaObjectManager jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
            jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(currentObjectKey);
            currentRule = (BaseRetentionRule) tbpService.getRetentionRule(currentObjectKey);
        } else {
            currentRule = new RangeRetentionRule();
        }
        if (currentRule != null && currentRule instanceof RangeRetentionRule) {
            fromDateCalHandler.setDateLong(((RangeRetentionRule) currentRule).getValidFromDate());
            toDateCalHandler.setDateLong(((RangeRetentionRule) currentRule).getEndDate());
        }
    }
    request.setAttribute("currentRule", currentRule);
    request.setAttribute("enableImmediatePublication", enableImmediatePublication);
    List ruleTypes = BaseRetentionRule.ruleTypes;
    request.setAttribute("daysInWeek", currentRule.getDaysInWeek());
    request.setAttribute("inherited", inherited);
    request.setAttribute("fromDateCalHandler", fromDateCalHandler);
    request.setAttribute("toDateCalHandler", toDateCalHandler);
%>
<%@page import="org.jahia.data.containers.JahiaContainer" %>
<%@page import="org.jahia.content.ContentContainerListKey" %>

<div class="head headtop">
    <div class="object-title"><fmt:message key="org.jahia.engines.timebasedpublishing.rule.settings.label"/></div>
</div>

<table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
<tr>
    <th align="left" width="120">
        <fmt:message key="org.jahia.engines.timebasedpublishing.inheritfromparent.label"/>
    </th>
    <td align="left">
        <input class="input" type="radio" name="inherited" value="1" ${inherited ? 'checked="checked"' : ''}
               onclick="handleActionChange('timeBasedPublishing')">
        <fmt:message key="org.jahia.engines.yes.label"/>&nbsp;<input class="input"
                                                                                               type="radio"
                                                                                               name="inherited"
                                                                                               value="0" ${!inherited ? 'checked="checked"' : ''}
                                                                                               onclick="handleActionChange('timeBasedPublishing')">
        <fmt:message key="org.jahia.engines.no.label"/>
    </td>
</tr>
<tr class="ruleSettings">
    <th>
        <fmt:message key="org.jahia.engines.timebasedpublishing.schedulingType.label"/>
    </th>
    <td>
        <input type="hidden" name="oldRuleType" value="<%=currentRule.getRuleType()%>">
        <select class="input" name="ruleType" onchange="handleActionChange('timeBasedPublishing')" ${inherited ? 'disabled="disabled"' : ''}>
            <%
                final Iterator iterator = ruleTypes.iterator();
                while (iterator.hasNext()) {
                    String ruleType = (String) iterator.next();
            %>
            <option class="input" value="<%=ruleType%>" <% if (ruleType.equals(currentRule.getRuleType())){ %>selected="selected"<% } %> >
                <fmt:message key='<%="org.jahia.engines.timebasedpublishing.schedulingType." + ruleType %>'/>
            </option>
                <% } %>
        </select>
    </td>
</tr>
<% if (RetentionRule.RULE_START_AND_END_DATE.equals(currentRule.getRuleType())) {%>
<tr class="ruleSettings">
    <th>
        <fmt:message key="org.jahia.engines.timebasedpublishing.rangerule.validFrom.label"/>
    </th>
    <td nowrap>
        <ui:dateSelector fieldName="${fromDateCalHandler.identifier}"
                                   datePattern="${fromDateCalHandler.dateFormat}"
                                   displayTime="true"
                                   templateUsage="false"
                                   value="${fromDateCalHandler.formatedDate}"
                                   readOnly="${inherited}"/>
        ${fromDateCalHandler.dateFormat}
    </td>
</tr>
<tr class="ruleSettings">
    <th>
        <fmt:message key="org.jahia.engines.timebasedpublishing.rangerule.validTo.label"/>
    </th>
    <td>
        <ui:dateSelector fieldName="${toDateCalHandler.identifier}"
                                   datePattern="${toDateCalHandler.dateFormat}"
                                   displayTime="true"
                                   templateUsage="false"
                                   value="${toDateCalHandler.formatedDate}"
                                   readOnly="${inherited}"/>
        ${toDateCalHandler.dateFormat}
    </td>
</tr>
<% } else if (RetentionRule.RULE_DAILY.equals(currentRule.getRuleType())) {%>
<tr class="ruleSettings">
    <th>
        <fmt:message key="org.jahia.engines.timebasedpublishing.from"/>
    </th>
    <td nowrap>
        <%
            request.setAttribute("inputNamePrefix", "from");
            request.setAttribute("hours", String.valueOf(currentRule.getDailyFromHours()));
            request.setAttribute("minutes", String.valueOf(currentRule.getDailyFromMinutes()));
        %>
        <jsp:include page="hours_input.jsp" flush="true"/>
    </td>
</tr>
<tr class="ruleSettings">
    <th>
        <fmt:message key="org.jahia.engines.timebasedpublishing.until"/>
    </th>
    <td>
        <%
            request.setAttribute("inputNamePrefix", "to");
            request.setAttribute("hours", String.valueOf(currentRule.getDailyToHours()));
            request.setAttribute("minutes", String.valueOf(currentRule.getDailyToMinutes()));
        %>
        <jsp:include page="hours_input.jsp" flush="true"/>
        <br/>
        <br/>
    </td>
</tr>
<tr class="ruleSettings">
    <th>
        <fmt:message key="org.jahia.engines.timebasedpublishing.firstOccurence"/>
    </th>
    <td>
        <jsp:include page="enable_immediatepublication.jsp" flush="true"/>
    </td>
</tr>
<% } else if (RetentionRule.RULE_XDAYINWEEK.equals(currentRule.getRuleType())) {%>
<tr>
    <td colspan="2">
        <fmt:message key="org.jahia.engines.timebasedpublishing.rangerule.daysInWeek.label"/>
        <table>
            <c:forEach var="dayItem" items="${requestScope['daysInWeek']}">
                <c:set scope="page" var="dayBean" value="${dayItem}"/>
                <%
                    DayInWeekBean dayBean = (DayInWeekBean) pageContext.getAttribute("dayBean");
                %>
                <tr>
                    <td>
                        <input class="input" type="checkbox" id="daysInWeek${dayItem.day}" name="daysInWeek" value="${dayItem.day}" ${dayItem.selected ? 'checked="checked"' : ''} ${inherited ? 'disabled="disabled"' : ''}>
                        <label for="daysInWeek${dayItem.day}">
                        <fmt:message key='<%="org.jahia.engines.timebasedpublishing.days."+dayBean.getDay()%>'/></label>
                    </td>
                    <td style="width:50px;padding-left:20px;padding-right:5px;"><fmt:message key="org.jahia.engines.timebasedpublishing.from"/>
                    </td>
                    <td>
                        <%
                            request.setAttribute("inputNamePrefix", "from_" + dayBean.getDay());
                            request.setAttribute("hours", String.valueOf(dayBean.getFromHours()));
                            request.setAttribute("hoursMax", 24);
                            request.setAttribute("minutes", String.valueOf(dayBean.getFromMinutes()));
                        %>
                        <jsp:include page="hours_input.jsp" flush="true"/>
                    </td>
                    <td style="width:50px;padding-left:15px;padding-right:5px;">
                        <fmt:message key="org.jahia.engines.timebasedpublishing.until"/>
                    </td>
                    <td>
                        <%
                            request.setAttribute("inputNamePrefix", "to_" + dayBean.getDay());
                            request.setAttribute("hours", String.valueOf(dayBean.getToHours()));
                            request.setAttribute("hoursMax", 25);
                            request.setAttribute("minutes", String.valueOf(dayBean.getToMinutes()));
                        %>
                        <jsp:include page="hours_input.jsp" flush="true"/>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </td>
</tr>
<tr class="ruleSettings">
    <th>
        <fmt:message key="org.jahia.engines.timebasedpublishing.firstOccurence"/>
    </th>
    <td>
        <jsp:include page="enable_immediatepublication.jsp" flush="true"/>
    </td>
</tr>
<% } %>
</table>