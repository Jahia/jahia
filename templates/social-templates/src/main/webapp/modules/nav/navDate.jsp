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

<%@ include file="../../common/declarations.jspf" %>

<template:cache cacheKey="fullListInOneEntry">
<div class="archives">
    <h3><fmt:message key="blog.nav.archives"/> </h3>
<c:set var="previousYear" value="0000"/>
<c:set var="previousMonth" value="00"/>
<c:set var="counter" value="0"/>
<template:containerList name="blogEntries" id="blogEntriesDateNav" sortByField="date" sortOrder="asc" displayExtensions="false" displayActionMenu="false">
    <template:addDependency bean="${blogEntriesDateNav}"/>
    <template:container cacheKey="dateNavigation" cache="off" id="blogEntry" displayActionMenu="false" displayExtensions="false">
        <template:addDependency bean="${blogEntry}"/>    
        <template:field name="date" display="false" var="date"/>
        <fmt:formatDate pattern="yyyy" value="${date.date}" var="currentYear"/>
        <fmt:formatDate pattern="MMMMM" value="${date.date}" var="currentMonth"/>
        <fmt:formatDate pattern="MM" value="${date.date}" var="currentMonthShort"/>
        <c:if test="${((previousMonth != currentMonth) || (previousYear != currentYear)) && (previousMonth !='00') }">
                <li><a href="${currentPage.url}?year=${currentYear}&month=${previousMonthShort}">${previousMonth}</a> (${counter})</li>
                <c:set var="counter" value="1"/>
        </c:if>
        <c:if test="${previousMonth == currentMonth && previousYear == currentYear}">
            <c:set var="counter" value="${counter + 1}"/>
        </c:if>
        <c:if test="${previousYear != currentYear}">
                <c:if test="${previousYear != '0000'}">
                    </ul>
                </c:if>
                <h4>${currentYear}</h4>
                <ul>
                <c:set var="counter" value="1"/>
        </c:if>
        <c:set var="previousYear" value="${currentYear}"/>
        <c:set var="previousMonth" value="${currentMonth}"/>
        <c:set var="previousMonthShort" value="${currentMonthShort}"/>
    </template:container>
</template:containerList>
    <li><a href="${currentPage.url}?year=${currentYear}&month=${previousMonthShort}">${previousMonth}</a> (${counter})</li>
    </ul>
</div>
</template:cache>