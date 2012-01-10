<%@ page import="org.jahia.bin.Jahia" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:set target="${renderContext}" property="contentType" value="application/rss+xml;charset=UTF-8"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:description" var="description"/>
<template:include view="hidden.header" templateType="html"/>
<c:if test="${not empty title}">
    <c:set var="title" value="${title.string}"/>
</c:if>
<c:if test="${empty title}">
    <c:set var="title" value="${currentNode.name}"/>
</c:if>
<c:if test="${not empty description}">
    <c:set var="description" value="${description.string}"/>
</c:if>
<c:if test="${empty description}">
    <c:set var="description" value="${title}"/>
</c:if>
<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
    <channel>
        <title>${fn:escapeXml(title)}</title>
        <link><c:url value="${url.server}${url.base}${currentNode.path}.html" context="/"/></link>
        <description>${fn:escapeXml(description)}</description>
        <generator>Jahia <%= Jahia.VERSION + " r" + Jahia.getBuildNumber() %>, http://www.jahia.org</generator>
        <c:forEach items="${moduleMap.currentList}" var="subchild">
            <c:if test="${!empty subchild.properties['j:view'] && !functions:hasScriptView(subchild,subchild.properties['j:view'].string , renderContext)}">
                <template:module node="${subchild}" editable="false" view="default"/>
            </c:if>
            <c:if test="${!(!empty subchild.properties['j:view'] && !functions:hasScriptView(subchild,subchild.properties['j:view'].string , renderContext))}">
                <template:module node="${subchild}" editable="false"/>
            </c:if>
        </c:forEach>
    </channel>
</rss>
