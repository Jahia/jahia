(function(){
	var indigoQF = {
		init: function(){
			// Attach window listeners
			window.onresize = indigoQF.listeners.windowResize;

			// Setup observers
			indigoQF.observers.body();


			// Setup listeners
			$(document).ready(function(){
				$("body").on("click", ".x-viewport-editmode .x-toolbar-first > table", indigoQF.listeners.toggleThemeMode)
				$("body").on("click", ".editmode-managers-menu", indigoQF.listeners.openManagerMenu)
				$("body").on("click", ".menu-editmode-managers-menu", indigoQF.listeners.closeManagerMenu);
				$("body").on("click", "#JahiaGxtSidePanelTabs > div:nth-child(1) > div:nth-child(2)", indigoQF.listeners.toggleSidePanelDocking);
				$("body").on("mouseover", ".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(7)", indigoQF.listeners.mouseOverHamburger)
				$("body").on("click", "#JahiaGxtSidePanelTabs .x-tree3-node-text", indigoQF.listeners.clickSidePanelMoreOptionsButton)
				$("body").on("click", "#JahiaGxtFileImagesBrowseTab .thumb-wrap > div:nth-child(1) > div:nth-child(2) div:nth-child(1) b", indigoQF.listeners.clickSidePanelFileThumbMoreOptionsButton)

				switch(indigoQF.status.sidePanelTabs.style){
					case "click":
						$("body").on("mousedown", "#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab, #JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", indigoQF.listeners.clickSidePanelTab);
						break;

					case "rollover":
						$("body").on("mouseenter", "#JahiaGxtSidePanelTabs", indigoQF.listeners.mouseOverSidePanelTab);

						$("body").on("mouseleave", "#JahiaGxtSidePanelTabs", indigoQF.listeners.mouseLeaveSidePanelTabs);

						$("body").on("mouseenter", ".x-panel-body.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) table > tbody > tr > td > div > table > tbody > tr > td:nth-child(1) input[type='text']", indigoQF.listeners.mouseEnterSiteSelector)

						$("body").on("mouseover", "#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab, #JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", indigoQF.listeners.mouseEnterSidePanelTab);
						break;
				}
			});


		},
		status: {
			sidePanelTabs: {
				mouseOutTimer: null,
				justBeenClosed: false,
				style: "click"
			},
			publication: null,
			multiselection: {
				count: 0
			},
			currentPage: {
				displayname: null
			},
			iframeLoaded: null,
			iframeObserver: null
		},
		config: {
			selectors: {
				editModePageName: ".x-border-panel.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) > div:nth-child(1)",
				editModeMoreInfo: "body[data-selection-count='0'] .x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(5)",
				contributeModeLanguageSelector: ".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div input"
			}
		},
		listeners: {
			// Window listeners
			windowLoad: function(){},
			windowResize: function(){
				indigoQF.listeners.updatePageMenuPositions();

			},

			// Body updates
			displaynameChanged: function(){

				var label;


				switch(indigoQF.status.multiselection.count){
					case 0:
						label = indigoQF.status.currentPage.displayname;
						break;

					case 1:
						label = "1 selected item";
						break;

					default:
						label = indigoQF.status.multiselection.count + " selected items";
						break;
				}

				$(".x-current-page-path").attr("data-PAGE-NAME",label);

				// Contribute Path Name (added to Langiage Selector - need to get a class for this)
				$(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div").attr("data-PAGE-NAME",label);

				indigoQF.listeners.updatePageMenuPositions();
			},
			countChanged: function(count){
				indigoQF.status.multiselection.count = count;

				indigoQF.listeners.displaynameChanged();
			},
			publicationStatusChanged: function(status){
				$("body").attr("data-PAGE-PUBLICATION-STATUS", status);

			},

			updatePageMenuPositions: function(){
				// EDIT MODE
				var editMode = {};

					editMode.pageNameLeft = parseInt($(indigoQF.config.selectors.editModePageName).position().left);
					editMode.pageNameWidth = Math.floor($(indigoQF.config.selectors.editModePageName).width()) - 1;
					editMode.pageNameRight = editMode.pageNameLeft + editMode.pageNameWidth;

				$(".edit-menu-view").css({
					"left": (editMode.pageNameRight + 76) + "px",
					"opacity": 1
				});

				$(".edit-menu-publication").css({
					"left": (editMode.pageNameRight + 65) + "px",
					"opacity": 1
				});

				$(indigoQF.config.selectors.editModeMoreInfo).css({
					"left": (editMode.pageNameLeft + 92) + "px",
					"opacity": 1
				});

				// CONTRIBUTE MODE
				var contributeMode = {};

					contributeMode.pageNameWidth = function(){
						var pageNameElement = document.querySelector('.x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div'),
							returnValue = 0;

						if(pageNameElement){
							returnValue = parseInt(window.getComputedStyle(pageNameElement, '::after').getPropertyValue('width'));
						}

						return returnValue;
					}();
					contributeMode.windowWidth = parseInt($("body").width());
					contributeMode.pageNameLeft = (contributeMode.windowWidth / 2) - (contributeMode.pageNameWidth / 2);
					contributeMode.pageNameRight = (contributeMode.windowWidth / 2) + (contributeMode.pageNameWidth / 2) + 20;

				$(indigoQF.config.selectors.contributeModeLanguageSelector).css({
					"margin-left": "-" + (contributeMode.pageNameWidth / 2) + "px"
				});

				$(".contribute-menu-publication").css({
					left: contributeMode.pageNameRight + "px"
				});

				$(".contribute-menu-view").css({
					left: (contributeMode.pageNameRight + 10) + "px"
				});

				// Edit Button
				$(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(4) > table").css({
					left: (contributeMode.pageNameRight) + "px"
				});
			},

			// Button listeners
			toggleThemeMode: function(e){
				// UI Theme toggle (dark / light)
				if($(e.target).hasClass("x-toolbar-ct")){
					var indigoUIColor;

					$("body").attr("data-INDIGO-UI", function(index, attr){
						indigoUIColor = (attr == "light") ? "dark" : "light";

						return indigoUIColor;
					});

					$(this).attr("data-INDIGO-UI", indigoUIColor);
				}
			},
			openManagerMenu: function(){
				// Close side panel if open
				$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "");
			},
			closeManagerMenu: function(){
				$(this).fadeOut();
			},
			toggleSidePanelDocking: function(e){
				// Pin-Toggle Side Panel
				$("body").attr("data-INDIGO-GWT-FULLSCREEN", function(index, attr){
					return (attr == "on") ? "" : "on";
				})

			},
			clickSidePanelTab: function(){
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

			},
			mouseEnterSidePanelTab: function(){
				// Trigger click on Side Panel Tabs on hover
				$(this).trigger("click");
			},
			mouseEnterSiteSelector: function(){
				// Mouseover Site Selector
				// Problem: The Site Selector is displayed as if it is part of the side panel. Only Problem is that it is not a child of Side Panel, so
				//			When the user hovers it the Side Panel is effectivly mouseout-ed.
				// Fix:		Reopen the side panel as soon as the Site Selector is hovered.

				$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");
			},
			mouseOverSidePanelTab: function(){
				// Mouseover Side Panel tabs, so open it.
				if($("body").attr("data-selection-count") == "0"){
					$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");

				}
			},
			mouseLeaveSidePanelTabs: function(e){
				// Mouse leaves the Side Panel

				if($("body > div.x-menu").length > 0){
					// Use hasnt really left the Side Panel, they have just opened a context menu, so do not close the side panel.

				} else {
					// There is no context menu, so assume that the Side Panel has really been mouseout-ed - close it.

					$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "");

					// Set flag and timer to remove after 100ms.
					indigoQF.status.sidePanelTabs.justBeenClosed = true;
					indigoQF.status.sidePanelTabs.mouseOutTimer = setTimeout(function(){
						// Mouseou-ed more than 100ms ago, so forget about it.
						indigoQF.status.sidePanelTabs.justBeenClosed = false;
					}, 100);
				}

			},
			mouseOverHamburger: function(){
				// Mouseover Hamburger
				// Problem: When the Side Panel is open and you hover the hamburger the Side Panel closes.
				// Fix: Once the user leaves the Side Panel there is a count down started (100ms). If the user hovers the Hamburger within those 100ms
				//		we reopen it, almost as if it never closed.

				if(indigoQF.status.sidePanelTabs.justBeenClosed && $("body").attr("data-selection-count") == "0"){
					// Side Panel was open less than 100ms ago, so repopen it.
					$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");

				}
			},
			clickSidePanelMoreOptionsButton: function(e){
				// Open Context Menu when clicking "More" button.

				var eV = new jQuery.Event("contextmenu");
					eV.clientX = e.pageX;
					eV.clientY = e.pageY;
					$(this).trigger(eV);
			},
			clickSidePanelFileThumbMoreOptionsButton: function(e){
				// Open Context Menu when clicking "More" button.

				var eV = new jQuery.Event("contextmenu");
					eV.clientX = e.pageX;
					eV.clientY = e.pageY;
					$(this).trigger(eV);
			},
		},
		observers: {
			body: function(){
				// Configuration of the observer:
				var config = {
						attributes: true,
						childList: true,
						characterData: true
					},
					target = document.body,
					observer = new MutationObserver(function(mutations){
						var publicationStatus,
							newNodes,
							publishSplit,
							publishName,
							friendlyPublishName;

						mutations.forEach(function(mutation){
							newNodes = mutation.addedNodes;
							removedNodes = mutation.removedNodes;
							attributeNames = mutation.attributeName;

							if(attributeNames){
								//console.log("attributeNames:", attributeNames);
							}

							if(attributeNames == "data-main-node-displayname"){
								var pageName = $("body").attr("data-main-node-displayname");
								indigoQF.status.currentPage.displayname = pageName;

								indigoQF.listeners.displaynameChanged();

							}

							if(attributeNames == "data-selection-count"){
								var count = parseInt($("body").attr("data-selection-count"));

								indigoQF.listeners.countChanged(count);

							}

							// Check for changes in document publication STATUS
							if($("body > div:nth-child(1)").attr("config") == "contributemode"){
								publicationStatus = $(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(7) img");

							} else {
								publicationStatus = $(".x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(1) img");

							}

							if(publicationStatus.attr("src") && (publicationStatus.attr("src") !== indigoQF.status.publication)){
								indigoQF.status.publication = publicationStatus.attr("src");

								publishSplit = indigoQF.status.publication.split("/");
								publishName = publishSplit[publishSplit.length - 1];
								friendlyPublishName = publishName.substr(0, publishName.length - 4);

								indigoQF.listeners.publicationStatusChanged(friendlyPublishName);

							}

						});
					});

				// Pass in the target node, as well as the observer options
				observer.observe(target, config);
			}
		}
	}

	window.onload = indigoQF.init;


}());
