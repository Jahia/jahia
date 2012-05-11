<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                    <c:if test="${empty currentNode.properties.mode.string}">
                    $('#item-${currentNode.identifier}').load('<c:url value="${url.base}${currentNode.path}.html.ajax?mainResource=${renderContext.mainResource.path}&includeJavascripts=true"/>');
                    </c:if>
                    <c:if test="${currentNode.properties.mode.string eq 'live'}">
                    $('#item-${currentNode.identifier}').load('<c:url value="${url.baseLive}${currentNode.path}.html.ajax?mainResource=${renderContext.mainResource.path}&includeJavascripts=true"/>');
                    </c:if>
                    <c:if test="${currentNode.properties.mode.string eq 'contribute'}">
                    $('#item-${currentNode.identifier}').load('<c:url value="${url.baseContribute}${currentNode.path}.html.ajax?mainResource=${renderContext.mainResource.path}&includeJavascripts=true"/>');
                    </c:if>
                    <c:if test="${currentNode.properties.mode.string eq 'preview'}">
                    $('#item-${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax?mainResource=${renderContext.mainResource.path}&includeJavascripts=true"/>');
                    </c:if>
                    <jcr:node var="root" path="/"/>
                    <c:if test="${currentNode.properties.mode.string eq 'live-or-preview'}">
                    <c:if test="${jcr:hasPermission(root,'jcr:read_default')}">
                        $('#item-${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax?mainResource=${renderContext.mainResource.path}&includeJavascripts=true"/>');
                    </c:if>
                    <c:if test="${not jcr:hasPermission(root,'jcr:read_default')}">
                        $('#item-${currentNode.identifier}').load('<c:url value="${url.baseLive}${currentNode.path}.html.ajax?mainResource=${renderContext.mainResource.path}&includeJavascripts=true"/>');
                    </c:if>
                    </c:if>
                </script>
            </c:otherwise>
        </c:choose>
    </c:if>
</div>
