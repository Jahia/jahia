<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>${fn:escapeXml(renderContext.mainResource.node.displayableName)}</title>
    <link rel="stylesheet" type="text/css" href="<c:url value='${url.currentModule}/css/print.css'/>" media="print"/>
</head>
<body id="body">
<div id="bodywrapper">
    <!--start bodywrapper-->
    <div id="topheader" class="noprint">
        <!--start topheader-->
        <div class="container container_16">
            <div class="grid_16">
                <div id="headerPart1">
                    <!--start headerPart1-->
                    <template:area path="header"/>
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <!--stop topheader-->
    <div id="page">
        <!--start page-->
        <div id="bottomheader">
            <!--start bottomheader-->
            <div class="container container_16">
                <h1 class="hide noprint">Nom du site</h1>

                <div class="logotop">
                    <template:area path="logo"/>
                </div>
            </div>
            <div class="container container_16 noprint">
                <template:area path="topMenu"/>
                <!--Include MENU-->
            </div>
            <div class="clear"></div>
        </div>
        <!--stop bottomheader-->
        <div id="content">
            <!--start content-->
            <div class="container container_16">
                <div class="grid_16">
                    <template:area path="pagecontent"/>
                </div>
            </div>
            <!--stop content-->
            <div class="clear"></div>
        </div>
        <div id="footer" class="noprint">
            <!--start footer-->
            <div id="footerPart3">
                <!--start footerPart3-->
                <div class="container container_16">
                    <div class='grid_2'>
                        <!--start grid_2-->
                        <template:area path="logoFooter"/>
                    </div>
                    <!--stop grid_2-->
                    <div class='grid_14'>
                        <!--start grid_14-->
                        <template:area path="footer"/>
                    </div>
                    <!--stop grid_12-->
                    <div class='clear'></div>
                </div>
                <div class="clear"></div>
            </div>
            <!--stop footerPart3-->
            <div class="clear"></div>
        </div>
        <!--stop footer-->
        <div class="clear"></div>
    </div>
    <!--stop page-->
    <div class="clear"></div>
</div>
<!--stop bodywrapper-->
</body>
</html>

<!--ressources-->
<c:if test="${renderContext.editMode}">
    <template:addResources type="css" resources="edit.css"/>
</c:if>
<template:addResources type="css"
                       resources="960.css,01web.css,02mod.css,navigationN1-1.css,navigationN1-2.css,navigationN1-3.css,navigationN1-4.css,navigationN2-1.css,navigationN2-2.css"/>
<template:theme/>