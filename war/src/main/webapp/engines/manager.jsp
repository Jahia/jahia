<%@ page contentType="text/html;charset=UTF-8" language="java"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility"%>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ page import="org.jahia.settings.SettingsBean" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<c:set var="cfg" value="${functions:default(param.conf, 'repositoryexplorer')}"/>
<% pageContext.setAttribute("xUaCompatible", SettingsBean.getInstance().getInternetExplorerCompatibility()); %>
<head>
    <c:if test="${not empty xUaCompatible}">
        <meta http-equiv="X-UA-Compatible" content="${xUaCompatible}"/>
    </c:if>
    <meta name="robots" content="noindex, nofollow"/>
    <fmt:message key="label.${fn:escapeXml(cfg)}" var="title"/>
    <title>${fn:escapeXml(title)}</title>
    <internal:gwtGenerateDictionary/>
    <internal:gwtInit/>
    <internal:gwtImport module="manager"/>
    <c:if test="${cfg == 'filemanager' || cfg == 'repositoryexplorer' || cfg == 'editorialcontentmanager'}">
        <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/modules/assets/css/jquery.Jcrop.min.css"/>
        <script type="text/javascript" src="<c:url value='/modules/jquery/javascript/jquery.min.js'/>"></script>
        <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.Jcrop.min.js'/>"></script>
        <script type="text/javascript" src="<c:url value='/modules/assets/javascript/clippy/jquery.clippy.min.js'/>"></script>
        <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.jahia.js'/>"></script>
    </c:if>

    <c:if test="${not empty theme}">
        <link rel="stylesheet" type="text/css" href="<c:url value='/engines/${theme}/manager${themeLocale}.css'/>"/>
        <!-- Javascript for theme -->
        <script type="text/javascript" src="<c:url value='/engines/${theme}/js/manager.js'/>"></script>
    </c:if>
</head>
<body onload="window.focus()">
<internal:contentManager conf="${fn:escapeXml(cfg)}" selectedPaths="${fn:escapeXml(param.selectedPaths)}" rootPath="${fn:escapeXml(param.rootPath)}"/>
</body>
</html>
