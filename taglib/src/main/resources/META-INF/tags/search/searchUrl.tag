<%@ tag body-content="empty" description="Renders the search URL built from current search request parameters."%>
<%@ attribute name="exclude" required="false" type="java.lang.String" description="Comma-separated list of parameter names to exclude from the final search URL. None is excluded by default."%>
<%@ attribute name="url" required="false" type="org.jahia.services.render.URLGenerator" description="Current URL Generator if available"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib"%>

<c:set var="urlBase" value="${pageContext.request.requestURI}"/>
<c:if test="${not empty url}">
    <c:set var="urlBase" value="${url.mainResource}"/>
</c:if>
<template:url value="${urlBase}" excludeParams="${exclude}" paramIncludeRegex="src_.*" useRequestParams="true"/>