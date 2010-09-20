<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<html  xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

    <title>${renderContext.mainResource.node.properties['jcr:title'].string}</title>
    <link rel="stylesheet" type="text/css" href="${url.currentModule}/css/print.css" media="print" />
</head>

<body>

<%@ include file="../../common/declarations.jspf" %>
<jcr:nodeProperty var="theme" node="${renderContext.mainResource.node}" name="j:theme" inherited="true"/>
<c:if test="${!empty theme}">
    <c:forEach var="themeFile" items="${jcr:getChildrenOfType(theme.node,'nt:file')}">
        <template:addResources type="css" resources="${themeFile.url}" insert="true"/>
    </c:forEach>
</c:if>
<template:addResources type="css" resources="960.css,01web.css,02mod.css,navigation.css,navigationN1-1.css,navigationN1-2.css,navigationN1-3.css,navigationN1-4.css,navigationN2-1.css,navigationN2-2.css" />
<c:if test="${renderContext.editMode}">
    <template:addResources type="css" resources="edit.css" />
</c:if>
<jcr:node var="rootPage" path="/sites/${renderContext.site.siteKey}/home"/>

<div id="bodywrapper">
    <div id="header" class="colorResource1 imgResource1">
        <div class="container container_16">
            <div class="grid_16">
                <template:area path="header" nodeTypes="jnt:row" />
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <div id="content">
        <div class="container container_16">
            <div class="grid_16">
                <template:area path="wrappercontent"/>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <div id="footer" class="colorResource2 imgResource2 noprint">
        <div class="container container_16">
            <div class="grid_16">
                <template:area path="footer" nodeTypes="jnt:row" />
                <div class="clear"></div></div>
            <div class="clear"></div></div>
    </div>
    <div class="clear"></div>

</div>
</body>
</html>