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

	<script>

		var indigoQF = {
			JahiaGxtSidePanelTabs: {
				mouseOutTimer: null,
				justBeenClosed: false
			}
		};

		$(document).ready(function(){

			$("body").on("click", ".menu-editmode-managers-menu", function(){
				$(this).fadeOut();
			});

			$("body").on("click", "#JahiaGxtSidePanelTabs > div:nth-child(1) > div:nth-child(2)", function(e){
				// Pin-Toggle Side Panel
				$("body").attr("data-INDIGO-GWT-FULLSCREEN", function(index, attr){
					return (attr == "on") ? "" : "on";
				})

			});

			$("body").on("mouseenter", "#JahiaGxtSidePanelTabs", function(){
				// Mouseover Side Panel tabs, so open it.

				$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");
			});

			$("body").on("mouseleave", "#JahiaGxtSidePanelTabs", function(e){
				// Mouse leaves the Side Panel

				if($("body > div.x-menu").length > 0){
					// Use hasnt really left the Side Panel, they have just opened a context menu, so do not close the side panel.

				} else {
					// There is no context menu, so assume that the Side Panel has really been mouseout-ed - close it.

					$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "");

					// Set flag and timer to remove after 100ms.
					indigoQF.JahiaGxtSidePanelTabs.justBeenClosed = true;
					indigoQF.JahiaGxtSidePanelTabs.mouseOutTimer = setTimeout(function(){
						// Mouseou-ed more than 100ms ago, so forget about it.
						indigoQF.JahiaGxtSidePanelTabs.justBeenClosed = false;
					}, 100);
				}

			});

			$("body").on("mouseenter", ".x-panel-body.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) table > tbody > tr > td > div > table > tbody > tr > td:nth-child(1) input[type='text']", function(){
				// Mouseover Site Selector
				// Problem: The Site Selector is displayed as if it is part of the side panel. Only Problem is that it is not a child of Side Panel, so
				//			When the user hovers it the Side Panel is effectivly mouseout-ed.
				// Fix:		Reopen the side panel as soon as the Site Selector is hovered.

				$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");
			})

			$("body").on("mouseover", "#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab, #JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", function(){
				// Trigger click on Side Panel Tabs on hover
				$(this).trigger("click");
			});

			$("body").on("mouseover", ".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(7)", function(){
				// Mouseover Hamburger
				// Problem: When the Side Panel is open and you hover the hamburger the Side Panel closes.
				// Fix: Once the user leaves the Side Panel there is a count down started (100ms). If the user hovers the Hamburger within those 100ms
				//		we reopen it, almost as if it never closed.

				if(indigoQF.JahiaGxtSidePanelTabs.justBeenClosed){
					// Side Panel was open less than 100ms ago, so repopen it.
					$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");

				}
			})

			$("body").on("click", "#JahiaGxtSidePanelTabs .x-tree3-node-text", function(e){
				// Open Context Menu when clicking "More" button.

				var eV = new jQuery.Event("contextmenu");
					eV.clientX = e.pageX;
					eV.clientY = e.pageY;
					$(this).trigger(eV);
			})

			$("body").on("click", "#JahiaGxtFileImagesBrowseTab .thumb-wrap > div:nth-child(1) > div:nth-child(2) div:nth-child(1) b", function(e){
				// Open Context Menu when clicking "More" button.

				var eV = new jQuery.Event("contextmenu");
					eV.clientX = e.pageX;
					eV.clientY = e.pageY;
					$(this).trigger(eV);
			})

		});
	</script>
	<!-- END:::QUICK FIX -->
    </c:if>
</head>

<body>
    <div class="jahia-template-gxt editmode-gxt" jahiatype="editmode" id="editmode" config="${renderContext.editModeConfigName}" path="${currentResource.node.path}" locale="${currentResource.locale}" template="${currentResource.template}" nodetypes="${fn:replace(jcr:getConstraints(renderContext.mainResource.node),',',' ')}"></div>
</body>

</html>
