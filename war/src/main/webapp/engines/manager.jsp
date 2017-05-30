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

	<!-- START:::QUICK FIX-->
	<!-- REM : Once Development has finished REMOVE unused Raleway Fonts ... -->
	<link href="https://fonts.googleapis.com/css?family=Raleway:100,100i,200,200i,300,300i,400,400i,500,500i,600,600i,700,700i,800,800i,900,900i" rel="stylesheet">

<<<<<<< HEAD
    <c:if test="${useNewTheme}">
	<!-- CONTAINS CSS TO APPLY QUICK FIX -->
    <link rel="stylesheet" type="text/css" href="<c:url value='/engines/quick-fix/manager_${newThemeLocale}.css'/>"/>
=======
	<!-- CONTAINS CSS TO APPLY QUICK FIX -->
	<link rel="stylesheet" type="text/css" href="/engines/quick-fix/manager.css"/>

>>>>>>> INDIGO-41: No Ticket
	<!-- ADDS ATTRIBUTE TO BODY TAG WHEN USER HAS CLICKED ON MANAGERS MENU ITEM -->
	<script>
		$(document).ready(function(){

			$("body").on("click", "#JahiaGxtManagerBottomTabs .x-panel-footer", function(){
				$("body").attr("data-INDIGO-INLINE-EDIT-ENGINE", function(index, attr){
					return (attr == "on") ? "" : "on";
				});
			})


			$("body").on("click", "#JahiaGxtManagerLeftTree .x-tab-panel-header", function(){
				$("body").attr("data-INDIGO-SEARCH", "on");
				$(this).find("li:nth-child(2) em").trigger("click");
			})

			$("body").on("click", "#JahiaGxtManagerLeftTree .x-panel > div.x-accordion-hd", function(){
				$("body").attr("data-INDIGO-SEARCH", "");
			})



		});
	</script>
	<!-- END:::QUICK FIX -->
<<<<<<< HEAD
    </c:if>
=======
>>>>>>> INDIGO-41: No Ticket
</head>
<body onload="window.focus()">
<internal:contentManager conf="${fn:escapeXml(cfg)}" selectedPaths="${fn:escapeXml(param.selectedPaths)}" rootPath="${fn:escapeXml(param.rootPath)}"/>
</body>
</html>
