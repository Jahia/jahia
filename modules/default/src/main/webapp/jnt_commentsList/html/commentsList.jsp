<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="commentable.css"/>
<c:if test="${renderContext.editMode}">
    <fmt:message key="label.comments.list"/>
</c:if>
<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty boundComponent}">
    <c:choose>
        <c:when test="${not renderContext.liveMode}">
            <template:addResources type="javascript" resources="jquery.min.js"/>
            <div id="commentsList${boundComponent.identifier}"></div>
            <script type="text/javascript">
                $('#commentsList${boundComponent.identifier}').load('<c:url value="${url.baseLive}${boundComponent.path}/comments.hidden.commentsoutoflivemode.html.ajax"/>');
            </script>
        </c:when>
        <c:otherwise>
            <jcr:node var="comments" path="${boundComponent.path}/comments"/>
            <c:if test="${not empty comments}">
                <template:addCacheDependency node="${comments}"/>
                <template:module node="${comments}" />
            </c:if>
            <c:if test="${empty comments}">
                <template:addCacheDependency node="${boundComponent}"/>
            </c:if>
        </c:otherwise>
    </c:choose>
</c:if>
