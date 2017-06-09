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

	<c:if test="${not empty theme}">
    <!-- CONTAINS CSS TO APPLY QUICK FIX -->
    <link rel="stylesheet" type="text/css" href="<c:url value='/engines/${theme}/edit${themeLocale}.css'/>"/>
	<script>

	/* LOTS OF QUICK N DIRTY CODING THAT NEEDS TO BE CLEANED UP ( WILL DO ONCE IT DOES WHAT I WANT IT TO ) */

		var indigoQF = {
			temp: null,
			init: function(){
				$("body").attr("data-SELECTED-ITEMS", 0);

				var target = document.body,
					observer = new MutationObserver(function(mutations){
						var publicationStatus,
							newNodes,
							publishSplit,
							publishName,
							friendlyPublishName;

						mutations.forEach(function(mutation){
							newNodes = mutation.addedNodes;
							attributeNames = mutation.attributeName;

							if(attributeNames == "data-nodedisplayname"){
								indigoQF.updateSelectedItems();

							}

							if(newNodes.length){
								// New Nodes added, see if iframe is available
								if($("iframe").length > 1){
									if(!indigoQF.iframe.available){
										indigoQF.iframe.available = true;

										$($("iframe")[0]).load(function(){
											indigoQF.listenForIframeDomChanges();

										})
									}
								}

							}

							// Check for changes in document publication STATUS
							if($("body > div:nth-child(1)").attr("config") == "contributemode"){
								publicationStatus = $(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(7) img");

							} else {
								publicationStatus = $(".x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(1) img");

							}

							if(publicationStatus.attr("src") && (publicationStatus.attr("src") !== indigoQF.publication.status)){
								indigoQF.publication.status = publicationStatus.attr("src");

								publishSplit = indigoQF.publication.status.split("/");
								publishName = publishSplit[publishSplit.length - 1];
								friendlyPublishName = publishName.substr(0, publishName.length - 4);

								indigoQF.updatePublicationStatus(friendlyPublishName);


							}

						});
					});

					// Configuration of the observer:
					var config = {
						attributes: true,
						childList: true,
						characterData: true
					};

					// Pass in the target node, as well as the observer options
					observer.observe(target, config);
			},
			updatePublicationStatus: function(status){
				console.log("STATUS: ", status);
				$("body").attr("data-PAGE-PUBLICATION-STATUS", status);

			},
			updatePageMenuPositions: function(){
				console.log("updatePageMenuPositions::: called");
				// Position Flag and Preview Menu accordingly
				// Get Left Position of Page Name
				var pageNameLeft = parseInt($(".x-border-panel.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) > div:nth-child(1)").position().left),
					pageNameWidth = Math.floor($(".x-border-panel.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) > div:nth-child(1)").width()) - 1,
					pageNameRight = pageNameLeft + pageNameWidth;

				// Set Position of View Menu to right hand side of More Options Menu
				$(".edit-menu-view").css({
					"left": (pageNameRight + 76) + "px",
					"opacity": 1
				});

				$(".edit-menu-publication").css({
					"left": (pageNameRight + 65) + "px",
					"opacity": 1
				});



				// Set Position of Flag to just before the Page name
				// Edit Mode
				$("body[data-SELECTED-ITEMS='0'] .x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(5)").css({
					"left": (pageNameLeft + 92) + "px",
					"opacity": 1
				});

				// Contribute Mode
				/*$(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div").css({
					"left": (pageNameLeft + 92) + "px",
					"opacity": 1
				});*/



				var contributePageNameWidth = parseInt(window.getComputedStyle(document.querySelector('.x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div'), '::after').getPropertyValue('width')),
					windowWidth = parseInt($("body").width()),
					contributePageNameLeft = (windowWidth / 2) - (contributePageNameWidth / 2),
					contributePageNameRight = (windowWidth / 2) + (contributePageNameWidth / 2) + 20;

					console.log("contributePageNameLeft:", contributePageNameLeft);



				// Slide Flag / Page Name left
				$(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div input").css({
					"margin-left": "-" + (contributePageNameWidth / 2) + "px"
				});

				$(".contribute-menu-publication").css({
					left: contributePageNameRight + "px"
				});

				$(".contribute-menu-view").css({
					left: (contributePageNameRight + 10) + "px"
				});

				// Edit Button
				$(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(4) > table").css({
					left: (contributePageNameRight) + "px"
				});

			},
			updateSelectedItems: function(){
				console.log("UPDATE SELECTED ITEMS CALLED");
				var label;
				$("body").attr("data-SELECTED-ITEMS", indigoQF.selections.count)

				switch(indigoQF.selections.count){
					case 0:
						label = $("body").attr("data-nodedisplayname");
						break;

					case 1:
						label = "1 selected item";
						break;

					default:
						label = indigoQF.selections.count + " selected items";
						break;
				}

				$(".x-current-page-path").attr("data-PAGE-NAME",label);

				// Contribute Path Name (added to Langiage Selector - need to get a class for this)
				$(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div").attr("data-PAGE-NAME",label);

				indigoQF.updatePageMenuPositions();


			},
			listenForIframeDomChanges: function(){
				// The node to be monitored
				var target = $($("iframe")[0].contentWindow.document).find("body")[0];

				var observer = new MutationObserver(function( mutations ) {
					mutations.forEach(function(mutation) {
						var removedNodes = mutation.removedNodes,
							addedNodes = mutation.addedNodes,
							className;

							if(removedNodes.length > 0){
								$(removedNodes).each(function(){
									className = $(this).attr("class") || "";

									if(className.indexOf("selection-top") > -1){
										//console.log("removed a selection", this);
										indigoQF.selections.count--;
										indigoQF.updateSelectedItems();

									}
								});
							}

							if(addedNodes.length > 0){
								$(addedNodes).each(function(){
									className = $(this).attr("class") || "";

									if(className.indexOf("selection-top") > -1){
										//console.log("added a selection", this);
										indigoQF.selections.count++;
										indigoQF.updateSelectedItems();


									}
								});
							}


					});

				});

				// Configuration of the observer:
				var config = {
					attributes: true,
					childList: true,
					characterData: true
				};

				// Pass in the target node, as well as the observer options
				observer.observe(target, config);
			},
			JahiaGxtSidePanelTabs: {
				mouseOutTimer: null,
				justBeenClosed: false
			},
			selections: {
				count: 0
			},
			pageName: {
				left: null,
				width: null,
			},
			iframe: {
				available: false
			},
			publication: {
				status: null
			}
		};

		$(".x-current-page-path").load(function(){
				console.log("loaded WIN", $("html").html());
		});

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
				if($("body").attr("data-SELECTED-ITEMS") == "0"){
					$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");

				}
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

				if(indigoQF.JahiaGxtSidePanelTabs.justBeenClosed && $("body").attr("data-SELECTED-ITEMS") == "0"){
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

		window.onload = indigoQF.init;
		window.onresize = indigoQF.updatePageMenuPositions;


	</script>
	<!-- END:::QUICK FIX -->
    </c:if>
</head>

<body>
    <div class="jahia-template-gxt editmode-gxt" jahiatype="editmode" id="editmode" config="${renderContext.editModeConfigName}" path="${currentResource.node.path}" locale="${currentResource.locale}" template="${currentResource.template}" nodetypes="${fn:replace(jcr:getConstraints(renderContext.mainResource.node),',',' ')}"></div>
</body>

</html>
