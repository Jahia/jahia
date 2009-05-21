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
<%@ tag body-content="empty"
        description="Renders a date control to query any date field of a content object. It is used internally by created, lastModified and documentProperty tags." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false" type="java.lang.String"
              description="Initial date value. Supported values are: anytime, today, last_week, last_month, last_three_months, last_six_months, range. In case of range value, the from and to attributes can be provided." %>
<%@ attribute name="from" required="false" type="java.lang.String"
              description="Initial value for date from in case of the range date type." %>
<%@ attribute name="to" required="false" type="java.lang.String"
              description="Initial value for date to in case of the range date type." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="display" value="${h:default(display, true)}"/>
<c:set var="valueParamName" value="${attributes.name}.type"/>
<c:set var="value" value="${h:default(param[valueParamName], value)}"/>
<c:if test="${empty requestScope['org.apache.jsp.tag.web.search.dateTag.included']}">
    <c:set var="org.apache.jsp.tag.web.search.dateTag.included" value="true" scope="request"/>
    <script type="text/javascript">
        /* <![CDATA[ */
        function searchDateTypeToggle(dateType) {
            var range = dateType.nextSibling;
            if (dateType.value == 'range' && range.style.display != 'none' || dateType.value != 'range' && range.style.display == 'none') {
                return;
            }
            range.style.display = dateType.value == 'range' ? '' : 'none';
            for (var i = 0; i < range.childNodes.length; i++) {
                if (range.childNodes[i].nodeName.toLowerCase() == 'input') {
                    range.childNodes[i].disabled = dateType.value != 'range';
                }
            }
        }
        /* ]]> */
    </script>
</c:if>
<c:if test="${display}">
    <select name="${valueParamName}" onchange="searchDateTypeToggle(this);">
        <option value="anytime" ${value == 'anytime' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.anytime"/></option>
        <option value="today" ${value == 'today' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.today"/></option>
        <option value="last_week" ${value == 'last_week' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.lastWeek"/></option>
        <option value="last_month" ${value == 'last_month' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.lastMonth"/></option>
        <option value="last_three_months" ${value == 'last_three_months' ? 'selected="selected"' : ''}>
            <fmt:message key="searchForm.date.lastThreeMonths"/></option>
        <option value="last_six_months" ${value == 'last_six_months' ? 'selected="selected"' : ''}>
            <fmt:message key="searchForm.date.lastSixMonths"/></option>
        <option value="range" ${value == 'range' ? 'selected="selected"' : ''}><fmt:message key="searchForm.date.range"/></option>
    </select><div ${value != 'range' ? 'style="display:none"' : ''} class="dateRange">
            <c:set var="valueParamName" value="${attributes.name}.from"/>
            <fmt:message key="searchForm.date.from"/>:&nbsp;
            <ui:dateSelector fieldName="${valueParamName}" value="${h:default(param[valueParamName], from)}"/>
            <c:set var="valueParamName" value="${attributes.name}.to"/>
            <fmt:message key="searchForm.date.to"/>:&nbsp;
            <ui:dateSelector fieldName="${valueParamName}" value="${h:default(param[valueParamName], to)}"/>
    </div>
</c:if>
<c:if test="${!display}"><input type="hidden" name="${valueParamName}" value="${value}"/></c:if>