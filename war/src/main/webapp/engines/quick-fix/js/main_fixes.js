/* LOTS OF QUICK N DIRTY CODING THAT NEEDS TO BE CLEANED UP ( WILL DO ONCE IT DOES WHAT I WANT IT TO ) */
		var indigoQF = {
			temp: null,
			init: function(){

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

							if(attributeNames == "data-main-node-displayname"){
								indigoQF.updateSelectedItems();

							}

							if(attributeNames == "data-selection-count"){
								indigoQF.updateSelectedItems();

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

				// EDIT MODE
				var pageNameLeft = parseInt($(".x-border-panel.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) > div:nth-child(1)").position().left),
					pageNameWidth = Math.floor($(".x-border-panel.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) > div:nth-child(1)").width()) - 1,
					pageNameRight = pageNameLeft + pageNameWidth,
					fullScreen = $("body").attr("data-indigo-gwt-fullscreen");

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
					$("body[data-selection-count='0'] .x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(5)").css({
						"left": (pageNameLeft + 92) + "px",
						"opacity": 1
					});

				// CONTRIBUTE MODE
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
				var label,
					dataSelectionCount = parseInt($("body").attr("data-selection-count"));

				switch(dataSelectionCount){
					case 0:
						label = $("body").attr("data-main-node-displayname");
						break;

					case 1:
						label = "1 selected item";
						break;

					default:
						label = dataSelectionCount + " selected items";
						break;
				}

				$(".x-current-page-path").attr("data-PAGE-NAME",label);

				// Contribute Path Name (added to Langiage Selector - need to get a class for this)
				$(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div").attr("data-PAGE-NAME",label);

				indigoQF.updatePageMenuPositions();


			},
			JahiaGxtSidePanelTabs: {
				mouseOutTimer: null,
				justBeenClosed: false,
				style: "click"
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


		$(document).ready(function(){

			$("body").on("click", ".x-viewport-editmode .x-toolbar-first > table", function(e){
				// UI Theme toggle (dark / light)
				if($(e.target).hasClass("x-toolbar-ct")){
					var indigoUIColor;

					$("body").attr("data-INDIGO-UI", function(index, attr){
						indigoUIColor = (attr == "light") ? "dark" : "light";

						return indigoUIColor;
					});

					$(this).attr("data-INDIGO-UI", indigoUIColor);
				}



			})

			$("body").on("click", ".editmode-managers-menu",function(){
				// Close side panel if open
				$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "");
			})

			$("body").on("click", ".menu-editmode-managers-menu", function(){
				$(this).fadeOut();
			});

			$("body").on("click", "#JahiaGxtSidePanelTabs > div:nth-child(1) > div:nth-child(2)", function(e){
				// Pin-Toggle Side Panel
				$("body").attr("data-INDIGO-GWT-FULLSCREEN", function(index, attr){
					return (attr == "on") ? "" : "on";
				})

			});


			switch(indigoQF.JahiaGxtSidePanelTabs.style){
				case "click":
					$("body").on("mousedown", "#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab, #JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", function(){
						// Trigger click on Side Panel Tabs on hover
						//$(this).trigger("click");
						var alreadyClicked = $(this).hasClass("x-tab-strip-active");

						if(alreadyClicked && $("body").attr("data-INDIGO-GWT-SIDE-PANEL") == "open"){
							// remove side panel
							$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "");
						} else {
							// open panel
							$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");

						}

					});
					break;
				case "rollover":
					$("body").on("mouseenter", "#JahiaGxtSidePanelTabs", function(){
						// Mouseover Side Panel tabs, so open it.
						if($("body").attr("data-selection-count") == "0"){
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
					break;
			}


			$("body").on("mouseover", ".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(7)", function(){
				// Mouseover Hamburger
				// Problem: When the Side Panel is open and you hover the hamburger the Side Panel closes.
				// Fix: Once the user leaves the Side Panel there is a count down started (100ms). If the user hovers the Hamburger within those 100ms
				//		we reopen it, almost as if it never closed.

				if(indigoQF.JahiaGxtSidePanelTabs.justBeenClosed && $("body").attr("data-selection-count") == "0"){
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
