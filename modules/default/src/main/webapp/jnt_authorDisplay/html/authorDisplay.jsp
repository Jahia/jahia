<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="mainresource.css"/>

<c:set var="user" value="${renderContext.mainResource.node.properties['jcr:createdBy'].string}"/>
<c:set var="mainTemplate" value="${currentNode.properties['j:userTemplate'].string}"/>
<c:choose>
    <c:when test="${renderContext.editMode}">
        <div class="authorDisplayArea<c:if test="${not empty currentNode.properties['j:mockupStyle']}"> ${currentNode.properties['j:mockupStyle'].string}</c:if>">
            <c:if test="${not empty currentNode.properties['j:userTemplate'].string}">
                <div class="authorDisplayTemplate">
                    <span>${currentNode.properties['j:userTemplate'].string}</span>
                </div>
            </c:if>
        </div>
    </c:when>
    <c:otherwise>
        <template:module path="/users/${user}" template="${mainTemplate}"/>
    </c:otherwise>
</c:choose>

