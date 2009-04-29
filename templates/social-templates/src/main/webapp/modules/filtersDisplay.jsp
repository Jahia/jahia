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
<%@ include file="../common/declarations.jspf" %>
<c:if test="${!empty categoryFilter}">
<div class="filterList">
<h3><fmt:message key="currentFilters"/> </h3>
<h4><fmt:message key="currentFilters.categories"/> </h4>

<c:set var="categoriesMap" value="${fn:split(categoryFilter, '$$$')}"/>

<ul>
    <c:forEach var="category" items="${categoriesMap}">
	<li><a href="${currentPage.url}?removeCategory=${category}" title="delete"><ui:displayCategoryTitle categoryKeys="${category}"/>
        <img src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/delete.png'/>" alt="delete" />
    </a></li>
    </c:forEach>
</ul>

    <div class="clear"></div>
<p class="filterListDeleteAll"><a title="#" href="#"><fmt:message key="currentFilters.deleteAll"/></a></p>
<div class="clear"></div>
</div>
</c:if>
