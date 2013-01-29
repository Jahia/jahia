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


<template:include view="hidden.load"/>

<c:if test="${not renderContext.editMode}">
    <template:addResources type="javascript" resources="jquery.min.js,jquery.easySlider1.7.js,jquery.easySlider1.7.load.js"/>
    <template:addResources type="css" resources="jquery.easySlider1.7.css"/>
</c:if>

<div id="slider">
    <ul>
        <c:forEach items="${moduleMap.currentList}" var="child" varStatus="status">
            <jcr:node var="image" uuid="${child.properties['j:node'].string}"/>
            <li>
                <c:if test="${!renderContext.editMode}">
                    <c:choose>
                        <c:when test="${jcr:isNodeType(image, 'jmix:thumbnail')}">
                            <c:url value="${image.url}" var="imgUrl" />
                            <img src="${imgUrl}" alt="">
                        </c:when>
                    </c:choose>
                </c:if>
                <c:if test="${renderContext.editMode}">
                    <template:module node="${child}" view="default"/>
                </c:if>
            </li>
        </c:forEach>
        <template:module path="*"/>
    </ul>
</div>
