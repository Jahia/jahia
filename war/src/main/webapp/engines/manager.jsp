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
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<c:set var="cfgbase" value="${functions:default(param.conf, 'repositoryexplorer')}"/>
<c:set var="cfg" value="${cfgbase}"/>
<c:if test="${fn:indexOf(cfgbase, '-' ) > 0}">
    <c:set var="cfg" value="${fn:substringBefore(cfgbase,'-')}"/>
</c:if>
<c:set var="xUaCompatible" value="${functions:getInternetExplorerCompatibility(pageContext.request)}"/>
<head>
    <c:if test="${not empty xUaCompatible}">
        <meta http-equiv="X-UA-Compatible" content="${xUaCompatible}"/>
    </c:if>
    <meta name="robots" content="noindex, nofollow"/>
    <fmt:message key="label.${fn:escapeXml(cfg)}.title" var="title"/>
    <title>${fn:escapeXml(title)}</title>
    <internal:gwtGenerateDictionary/>
    <internal:gwtInit/>
    <internal:gwtImport module="manager"/>

	<c:if test="${not empty theme}">
		<link rel="stylesheet" type="text/css" href="<c:url value='/engines/${theme}/css/edit${themeLocale}.css'/>"/>
		<%-- Javascript for theme --%>
		<script type="text/javascript" src="<c:url value='/engines/${theme}/js/dist/build/anthracite-min.js'/>"></script>
	</c:if>

</head>
<body>
<internal:contentManager conf="${fn:escapeXml(cfgbase)}" selectedPaths="${fn:escapeXml(param.selectedPaths)}" rootPath="${fn:escapeXml(param.rootPath)}"/>
</body>
</html>
