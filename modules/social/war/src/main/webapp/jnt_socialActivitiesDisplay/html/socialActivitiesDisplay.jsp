<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:nodeTypeFilter" var="nodeTypeFilter"/>
<jcr:nodeProperty node="${currentNode}" name="j:recommendationLimit" var="recommendationLimit"/>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
    <div class="metrics">
        <social:get-acl-connections var="aclConnections" path="${bindedComponent.path}"/>
        <social:get-activities var="activities" sourcePaths="${aclConnections}" pathFilter="${bindedComponent.path}"/>
        <c:if test="${empty activities}">
            No activities found.
        </c:if>
        <c:if test="${not empty activities}">
            <c:forEach items="${activities}" var="activity">
                <template:module path="${activity.path}"/>
            </c:forEach>
        </c:if></div>
</c:if>
<template:linker property="j:bindedComponent"/>


