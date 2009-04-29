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
<%@ include file="declarations.jspf" %>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>${currentSite.title}</title>
<template:themeDisplay defaultTheme="default"/>
<!--[if lte IE 6]>
<link href="misc/ie6.css" rel="stylesheet" type="text/css" />
<![endif]-->
<style type="text/css">
<!--
body { behavior: url(<utility:resolvePath value="scripts/csshover.htc"/>); }
img { behavior: url(<utility:resolvePath value="scripts/iepngfix.htc"/>); }
-->
</style>

<%--Filters Settings--%>
<%--add category Filter--%>
<c:if test="${! empty param.addCategory}">
    <c:choose>
    <c:when test="${empty categoryFilter}">
        <c:set scope="session" var="categoryFilter" value="${param.addCategory}"/>
    </c:when>
    <c:otherwise>
        <c:set scope="session" var="categoryFilter" value="${categoryFilter}$$$${param.addCategory}"/>
    </c:otherwise>
    </c:choose>
</c:if>
<%--remove category Filter--%>
<c:if test="${!empty param.removeCategory}">
    <c:set var="categoriesMap" value="${fn:split(categoryFilter, '$$$')}"/>
    <c:set var="categoriestmp" value=""/>
    <c:forEach var="category" items="${categoriesMap}">
        <c:if test="${!category eq param.removeCategory}">
            <c:choose>
            <c:when test="${empty categoriestmp}">
                <c:set var="categoriestmp" value="${category}"/>
            </c:when>
            <c:otherwise>
                <c:set var="categoriestmp" value="${categoriestmp}$$$${category}"/>
            </c:otherwise>
            </c:choose>
        </c:if>
    </c:forEach>
    <c:set var="categoryFilter" value="${categoriestmp}" scope="session"/>
</c:if>
