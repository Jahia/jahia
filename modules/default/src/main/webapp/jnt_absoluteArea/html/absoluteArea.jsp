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
<c:choose>
    <c:when test="${not empty inWrapper and inWrapper eq false}">
        <div class="mainResourceAbsoluteArea<c:if test="${not empty currentNode.properties['j:mockupStyle']}"> ${currentNode.properties['j:mockupStyle'].string}</c:if>">
            <div class="mainResourceAbsoluteAreaTemplate">
                <span>Absolute Area : ${currentNode.name}</span>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <jcr:nodeProperty node="${currentNode}" name="j:allowedTypes" var="restrictions"/>
        <c:if test="${not empty restrictions}">
            <c:forEach items="${restrictions}" var="value">
                <c:if test="${not empty nodeTypes}">
                    <c:set var="nodeTypes" value="${nodeTypes} ${value.string}"/>
                </c:if>
                <c:if test="${empty nodeTypes}">
                    <c:set var="nodeTypes" value="${value.string}"/>
                </c:if>
            </c:forEach>
        </c:if>
        <c:if test="${empty currentNode.properties['j:basenode'].node.path}">
            <c:set var="path" value="${renderContext.site.path}/home"/>
        </c:if>
        <c:if test="${!empty currentNode.properties['j:basenode'].node.path}">
            <c:set var="path" value="${currentNode.properties['j:basenode'].node.path}"/>
        </c:if>

        <template:wrappedContent template="${currentNode.properties['j:referenceTemplate'].string}"
                                 path="${path}/${currentNode.name}"
                                 nodeTypes="${nodeTypes}">
            <c:if test="${not empty currentNode.properties['j:subNodesTemplate'].string}">
                <template:param name="subNodesTemplate" value="${currentNode.properties['j:subNodesTemplate'].string}"/>
            </c:if>
            <c:if test="${not empty currentNode.properties['j:mockupStyle'].string}">
                <template:param name="mockupStyle" value="${currentNode.properties['j:mockupStyle'].string}"/>
            </c:if>
        </template:wrappedContent>
    </c:otherwise>
</c:choose>

