<%@ include file="../../common/declarations.jspf" %>

<template:cache cacheKey="fullListInOneEntry">
<div class="archives">
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