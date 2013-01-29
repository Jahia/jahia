<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>

<template:addResources type="css" resources="sibling-navigation.css" />

<c:set var="mainNode" value="${renderContext.mainResource.node}"/>
<c:set var="nodeType" value="${mainNode.primaryNodeTypeName}"/>
<c:set var="parent" value="${mainNode.parent}"/>
<c:set var="sibblings" value="${jcr:getChildrenOfType(parent, nodeType)}"/>
<c:forEach items="${sibblings}" var="sibbling" varStatus="status">
    <c:if test="${mainNode == sibbling}">
        <c:if test="${not status.first}">
            <c:set var="previousNode" value="${sibblings[status.index - 1]}"/>
        </c:if>
        <c:if test="${not status.last}">
            <c:set var="nextNode" value="${sibblings[status.index + 1]}"/>
        </c:if>
    </c:if>
</c:forEach>
<c:set var="upperNode" value="${currentNode.properties['displayLinktoParent'].boolean?jcr:findDisplayableNode(parent, renderContext):''}"/>

<ul class="sibling-navigation-list">
    <c:if test="${not empty previousNode}">
        <li><a class="previousNode" title="${previousNode.displayableName}" href="<c:url value='${url.base}${previousNode.path}.html'/>"><span><fmt:message key="siblings.previous"/></span></a></li>
    </c:if>
    <c:if test="${not empty upperNode}">
        <li><a class="upperNode" title="${upperNode.displayableName}" href="<c:url value='${url.base}${upperNode.path}.html'/>"><span><fmt:message key="siblings.up"/></span></a></li>
    </c:if>
    <c:if test="${not empty nextNode}">
        <li><a class="nextNode" title="${nextNode.displayableName}" href="<c:url value='${url.base}${nextNode.path}.html'/>"><span><fmt:message key="siblings.next"/></span></a></li>
    </c:if>
</ul>