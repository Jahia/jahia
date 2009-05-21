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

<%@ include file="../../declarations.jspf" %>
<template:containerList name="lastNewsContainer" id="lastNews"
                        actionMenuNamePostFix="lastnewss" actionMenuNameLabelKey="lastnewss">
    <template:container id="lastNewsContainer" cache="off" actionMenuNamePostFix="lastnews"
                        actionMenuNameLabelKey="lastnews.update">
        <template:field name="filter"display="false" var="newsCategoriesFilter" />
        <c:set var="categoriesFilter" value=""/>
        <c:set var="categoriesDisplay" value=""/>
        <c:forEach items="${newsCategoriesFilter.category}" var="newsCategoryFilter" varStatus="status">
            <c:if test="${status.last}">
                <c:set var="categoriesFilter" value="${categoriesFilter}${newsCategoryFilter.category.key}"/>
                <c:set var="categoriesDisplay" value="${categoriesDisplay}${newsCategoryFilter.title}"/>
            </c:if>
            <c:if test="${!status.last}">
                <c:set var="categoriesFilter" value="${categoriesFilter}${newsCategoryFilter.category.key}$$$"/>
                <c:set var="categoriesDisplay" value="${categoriesDisplay}${newsCategoryFilter.title}, "/>
            </c:if>
        </c:forEach>
        <template:field name="display" var="display" defaultValue="short" display="false"/>
        <template:field name="maxNews" var="maxNews" defaultValue="10" display="false"/>
        <c:if test="${requestScope.currentRequest.editMode}">
        <div class="preferences">    
        <h2><fmt:message key="web_templates_newsList.preferences"/></h2>
        <p class="preference-item"><span class="preference-label"><fmt:message key="web_templates_newsList.categoryFilter"/>: </span><span class="preference-value">${categoriesDisplay} </span></p>
        <p class="preference-item"><span class="preference-label"><fmt:message key="web_templates_newsList.rowsDisplay"/>: </span><span class="preference-value">${maxNews.integer}</span></p>
        <p class="preference-item"><span class="preference-label"><fmt:message key="web_templates_newsList.display"/>: </span><span class="preference-value">${display}</span></p>
        </div>    
        </c:if>
        <c:set var="newsCategoryFilter" value="${newsCategoryFilter}"/>
        <c:set var="maxNews" value="${maxNews}"/>
        <c:set var="display" value="${display}"/>
    </template:container>
</template:containerList>

<template:containerList maxSize="${maxNews.integer}" id="newsList" displayActionMenu="false">

    <query:containerQuery>
        <query:selector nodeTypeName="web_templates:newsContainer" selectorName="newsList"/>
        <c:if test="${!empty categoriesFilter}">
            <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}" value="${categoriesFilter}"
                           multiValue="true" metadata="true"/>
        </c:if>
        <query:descendantNode selectorName="newsList" path="${currentSite.JCRPath}"/>
        <query:setProperty name="${queryConstants.SEARCH_MAX_HITS}" value="${maxNews.integer}" />
        <query:sortBy propertyName="newsDate" order="${queryConstants.ORDER_DESCENDING}"/>
    </query:containerQuery>

    <c:if test="${display == 'small'}">
        <%@ include file="../../../modules/news/smallNewsDisplay.jspf" %>
    </c:if>
    <c:if test="${display == 'medium'}">
        <%@ include file="../../../modules/news/mediumNewsDisplay.jspf" %>
    </c:if>
    <c:if test="${display == 'large'}">
        <%@ include file="../../../modules/news/largeNewsDisplay.jspf" %>
    </c:if>
</template:containerList>
