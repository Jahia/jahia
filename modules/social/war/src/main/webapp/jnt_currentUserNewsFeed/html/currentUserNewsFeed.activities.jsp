<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>
<template:include view="hidden.header"/>
<template:addCacheDependency flushOnPathMatchingRegexp="${param.user}/activities/.*" />


<c:set var="ps" value="?pagerUrl=${url.mainResource}"/>
<c:if test="${!empty param.pageUrl}">
    <c:set var="ps" value="?pagerUrl=${param.pageUrl}"/>
</c:if>
<c:forEach items="${param}" var="p" varStatus="status">
    <c:if test="${p.key != 'pagerUrl' && p.key != 'jsite'}">
        <c:set var="ps" value="${ps}&${p.key}=${p.value}" />
    </c:if>
</c:forEach>
<c:set target="${moduleMap}" property="pagerUrl" value="${param.pagerUrl}"/>

<template:initPager totalSize="${fn:length(moduleMap.currentList)}" pageSize="${param.pageSize}" id="${renderContext.mainResource.node.identifier}"/>
<template:displayPagination/>

<c:if test="${fn:length(moduleMap.currentList) eq 0}">
    <p><fmt:message key="newsFeed.emptyResults"/></p>
</c:if>
<c:if test="${fn:length(moduleMap.currentList) ne 0}">
    <c:forEach items="${moduleMap.currentList}" var="activity" varStatus="status" begin="${moduleMap.begin}" end="${moduleMap.end}">
        <template:module path="${activity.path}" view="default"/>
    </c:forEach>
</c:if>
