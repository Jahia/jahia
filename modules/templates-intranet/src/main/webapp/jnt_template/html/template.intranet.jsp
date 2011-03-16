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
<link rel="stylesheet" type="text/css" href="<c:url value='${url.currentModule}/css/print.css'/>" media="print" />
</head>
<body id="body">
<%@ include file="../../common/declarations.jspf" %>
<jcr:node var="rootPage" path="/sites/${renderContext.site.siteKey}/home"/>
<div id="bodywrapper">
  <!--start bodywrapper-->
  <div id="topheader">
    <!--start topheader-->
    <div class="container container_16">
      <div class="grid_16">
        <div class="logotop"><a href="#">
          <template:area path="logo"/>
          </a></div>
      </div>
    </div>
    <div class="clear"></div>
  </div>
  <!--stop topheader-->
  <div id="bottomheader" class="noprint">
    <!--start bottomheader-->
    <div class="container container_16">
      <div class="grid_10">
        <div id="banner">
          <!--start banner-->
          <template:area path="pagetitle"> Put your page title element here </template:area>
        </div>
        <div class="clear"></div>
      </div>
      <div class="grid_6">
        <div id="search-bar">
          <template:area path="simpleSearch"/>
        </div>
        <div class="clear"></div>
      </div>
      <div class="grid_16">
        <template:area path="topMenu"/>
      </div>
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
    <div class="clear"></div>
  </div>
  <!--stop content-->
  <div id="topfooter" class="noprint">
    <!--start topfooter-->
    <div class="container container_16">
      <!--start container_16-->
      <div class="grid_16">
        <template:area path="topfooter" nodeTypes="jnt:row" > Put your footer here </template:area>
        <div class="clear"></div>
      </div>
      <div class="clear"></div>
    </div>
    <!--stop container_16-->
    <div class="clear"></div>
  </div>
  <!--stop topfooter-->
  <div id="bottomfooter" class="noprint">
    <!--start bottomfooter-->
    <div class="container container_16">
      <!--start container_16-->
      <div class="grid_16">
        <template:area path="footer" nodeTypes="jnt:row" />
        <div class="clear"></div>
      </div>
      <div class="clear"></div>
    </div>
    <!--stop container_16-->
  </div>
  <!--stop bottomfooter-->
  <div class="clear"></div>
</div>
<!--stop bodywrapper-->
</body>
</html>

<!--ressources-->
<c:if test="${renderContext.editMode}">
  <template:addResources type="css" resources="edit.css" />
</c:if>
<template:addResources>
  <!--[if IE]><link rel="stylesheet" type="text/css" href="<c:url value='${url.currentModule}/css/ie.css'/>" media="screen" /><![endif]-->
  <!--[if lte IE 6]>
<link rel="stylesheet" type="text/css" href="<c:url value='${url.currentModule}/css/ie6.css'/>" media="screen" />
    <style type="text/css">
    body { behavior: url(<c:url value='${url.currentModule}/scripts/csshover3.htc'/>); }
    </style>
<![endif]-->
</template:addResources>
<template:addResources type="css" resources="960.css,01web.css,02mod.css,navigation.css,navigationN1-1.css,navigationN1-2.css,navigationN1-3.css,navigationN1-4.css,navigationN2-1.css,navigationN2-2.css" />
<template:theme/>