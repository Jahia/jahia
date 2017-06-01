<%@ page contentType="text/html;charset=UTF-8" language="java"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="org.jahia.settings.SettingsBean" %>
<% pageContext.setAttribute("xUaCompatible", SettingsBean.getInstance().getInternetExplorerCompatibility()); %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <c:if test="${not empty xUaCompatible}">
        <meta http-equiv="X-UA-Compatible" content="${xUaCompatible}"/>
    </c:if>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="author" content="system" />
    <title>Edit</title>

    <internal:gwtGenerateDictionary/>
    <internal:gwtInit />
    <internal:gwtImport module="edit"/>
    <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/modules/assets/css/jquery.Jcrop.min.css"/>
    <script type="text/javascript" src="<c:url value='/modules/jquery/javascript/jquery.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.Jcrop.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/clippy/jquery.clippy.min.js'/>"></script>

	<!-- START:::QUICK FIX-->
	<!-- REM : Once Development has finished REMOVE unused Raleway Fonts ... -->
	<link href="https://fonts.googleapis.com/css?family=Raleway:100,100i,200,200i,300,300i,400,400i,500,500i,600,600i,700,700i,800,800i,900,900i" rel="stylesheet">


	<c:if test="${useNewTheme}">
    <!-- CONTAINS CSS TO APPLY QUICK FIX -->
    <link rel="stylesheet" type="text/css" href="<c:url value='/engines/quick-fix/edit_${newThemeLocale}.css'/>"/>

	<!-- ADDS ATTRIBUTE TO BODY TAG WHEN USER HAS CLICKED ON MANAGERS MENU ITEM -->
	<script>
		$(document).ready(function(){

			/* ON CLICK ::: MANAGERS BUTTON */
			$("body").on("click", ".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(7), .x-viewport-contributemode .x-toolbar-first .x-toolbar-cell:nth-child(11)", function(){
				$("body").attr("data-INDIGO-GWT-FORMATTER", "managers-modal");
			});

			/* ON CLOSE ::: MANAGERS MENU */
			$("body").on("click", ".x-menu", function(){
				$("body").attr("data-INDIGO-GWT-FORMATTER", "");
				$("body > div.x-menu").hide();
			})

			//data-INDIGO-GWT-FULLSCREEN="on"]

			$("body").on("click", "#JahiaGxtSidePanelTabs > div:nth-child(1) > div:nth-child(2)", function(e){
				$("body").attr("data-INDIGO-GWT-FULLSCREEN", function(index, attr){
					return (attr == "on") ? "" : "on";
				})

			});

		});
	</script>
	<!-- END:::QUICK FIX -->
    </c:if>
</head>

<body>
    <div class="jahia-template-gxt editmode-gxt" jahiatype="editmode" id="editmode" config="${renderContext.editModeConfigName}" path="${currentResource.node.path}" locale="${currentResource.locale}" template="${currentResource.template}" nodetypes="${fn:replace(jcr:getConstraints(renderContext.mainResource.node),',',' ')}"></div>
</body>

</html>
