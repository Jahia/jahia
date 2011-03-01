<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>

<social:get-connections var="userConnections" path="${param.user}" />
<social:get-activities var="activities" sourcePaths="${userConnections}" />
<template:addCacheDependency flushOnPathMatchingRegexp="${param.user}/activities/.*" />
<c:if test="${empty activities}">
    <fmt:message key="message.noActivitiesFound"/>
</c:if>
<c:if test="${not empty activities}">
<c:forEach items="${activities}" var="activity">
    <template:module path="${activity.path}" />
</c:forEach>
</c:if>