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
<%@ page import="org.jahia.engines.calendar.CalendarHandler" %>
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="ui" %>
<%
    Boolean hiddenCalendar = (Boolean) request.getAttribute("hiddenCalendar");
    if (hiddenCalendar == null) {
        hiddenCalendar = Boolean.FALSE;
    }
    final CalendarHandler calHandler = (CalendarHandler) request.getAttribute("calendarHandler");
    if (!hiddenCalendar) {
%>

<ui:dateSelector fieldName="<%=calHandler.getIdentifier()%>"
                           datePattern="<%=calHandler.getDateFormat()%>"
                           displayTime="true"
                           templateUsage="false"
                           value="<%=calHandler.getFormatedDate()%>"
                           readOnly="<%=hiddenCalendar%>"/>
<%=calHandler.getDateFormat()%>
<% } %>
