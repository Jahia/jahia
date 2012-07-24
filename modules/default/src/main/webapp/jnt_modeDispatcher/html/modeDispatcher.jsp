<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js"/>
<div id="item-${currentNode.identifier}">
    <c:if test="${renderContext.editMode}">
        Loaded with AJAX call
        <c:if test="${not empty currentNode.properties.mode.string}">
            from mode ${currentNode.properties.mode.string}
        </c:if>
        <c:forEach items="${jcr:getChildrenOfType(currentNode,'jmix:droppableContent')}" var="child">
            <template:module node="${child}"/>
        </c:forEach>
        <template:module path="*"/>
    </c:if>
    <c:if test="${not renderContext.editMode}">
        <c:choose>
            <c:when test="${renderContext.ajaxRequest}">
                <c:set var="modeDispatcherId" value="item-${currentNode.identifier}" scope="request"/>
                <c:forEach items="${jcr:getChildrenOfType(currentNode,'jmix:droppableContent')}" var="child">
                    <template:module node="${child}"/>
                </c:forEach>
                <c:remove var="modeDispatcherId" scope="request"/>
            </c:when>
            <c:otherwise>
                <script type="text/javascript">
                    <c:if test="${empty currentNode.properties['mode'].string}">
                    <c:url value="${url.base}${currentNode.path}.html.ajax" var="theUrl">
                    	<c:param name="mainResource" value="${renderContext.mainResource.path}"/>
                    	<c:param name="includeJavascripts" value="true"/>
                    </c:url>
                    $('#item-${currentNode.identifier}').load("${functions:escapeJavaScript(theUrl)}");
                    </c:if>
                    <c:if test="${not empty currentNode.properties['mode'].string}">
                    <c:url value="/cms/${currentNode.properties['mode'].string}/${currentResource.locale}${currentNode.path}.html.ajax" var="theUrl">
                    	<c:param name="mainResource" value="${renderContext.mainResource.path}"/>
                    	<c:param name="includeJavascripts" value="true"/>
                    </c:url>
                    $('#item-${currentNode.identifier}').load("${functions:escapeJavaScript(theUrl)}");
                    </c:if>
                </script>
            </c:otherwise>
        </c:choose>
    </c:if>
</div>
