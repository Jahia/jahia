(function(){
	var indigoQF = {
		init: function(){

			if(indigoQF.status.quickMenu.active){
				console.log("ITS ACTIVE");
				$("body")
					.attr("data-quick-menu", "on")
					.prepend("<div id='quick-menu'><iframe scrolling='no' id='quick-menu-contents'></iframe></div>");
				$('#quick-menu-contents').attr("src", '/cms/dashboardframe/default/en/users/root.projects.html');



				console.log(indigoQF.status.user);

			}




			// Attach window listeners
			window.onresize = indigoQF.listeners.windowResize;

			// Setup observers
			indigoQF.observers.body();

			// Setup listeners
			$(document).ready(function(){
				$(window).on("blur", function(){
					// Window has lost focus, so presume that the user has clicked in the iframe.
					// If the side panel is open, then close it

					if($("body").attr("data-INDIGO-GWT-SIDE-PANEL") == "open"){
						indigoQF.listeners.closeSidePanel()

					}

				});

				$("body")
					.on("click", ".x-viewport-editmode .x-toolbar-first > table", indigoQF.listeners.toggleThemeMode)
					.on("click", ".editmode-managers-menu", indigoQF.listeners.openManagerMenu)
					.on("click", ".menu-editmode-managers-menu", indigoQF.listeners.closeManagerMenu)
					.on("mousedown", ".menu-edit-menu-mode, .menu-edit-menu-user", indigoQF.listeners.closeManagerMenu)
					.on("click", "#JahiaGxtSidePanelTabs > div:nth-child(1) > div:nth-child(2)", indigoQF.listeners.toggleSidePanelDocking)
					.on("mouseover", ".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(7)", indigoQF.listeners.mouseOverHamburger)
					.on("click", "#JahiaGxtSidePanelTabs .x-grid3-td-displayName", indigoQF.listeners.clickSidePanelMoreOptionsButton)
					.on("click", "#JahiaGxtFileImagesBrowseTab .thumb-wrap > div:nth-child(1) > div:nth-child(2) div:nth-child(1) b", indigoQF.listeners.clickSidePanelFileThumbMoreOptionsButton)
					.on("click", ".x-current-page-path", indigoQF.listeners.clearMultiSelection)
					.on("click", "#JahiaGxtSidePanelTabs .x-grid3-row", indigoQF.listeners.addPageToHistory);

				// Setup side panel listeners accordingly to naviagtion style (rollover or click) as defined in indigoQF.status.panelMenu.style
				switch(indigoQF.status.panelMenu.style){
					case "click":
						$("body").on("mousedown", "#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab", indigoQF.listeners.clickSidePanelTab);
						$("body").on("mousedown", "#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", indigoQF.listeners.clickSidePanelSettingsTab);
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
				justBeenClosed: false
			},
			panelMenu: {
				style: "click",
				openedJoint: null,
				mouseOutTimer: null,
				mouseOutTimeValue: 200,
				allowClickToCloseSubMenu: false,
				autoHideOnMouseOut: true,
				observer: null
			},
			publication: null,
			multiselection: {
				count: 0
			},
			currentPage: {
				displayname: null
			},
			iframeLoaded: null,
			iframeObserver: null,
			user: {},
			quickMenu: {
				active: false
			}
		},
		config: {
			selectors: { // Ask Thomas for classes where possible
				editModePageName: ".x-border-panel.x-border-layout-ct > div:nth-child(1) > div:nth-child(1) > div:nth-child(1)",
				editModeMoreInfo: "body[data-selection-count='0'] .x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(5)",
				contributeModeLanguageSelector: ".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div input",
				closeSidePanelCapture: "[data-INDIGO-GWT-SIDE-PANEL='open'] .x-panel-body.x-border-layout-ct > div:nth-child(1) > div:nth-child(2) > div:nth-child(2)",
				closeSidePanelCapture: "[data-INDIGO-GWT-SIDE-PANEL='open'] .gwt-body-edit"
			}
		},
		listeners: {
			// Window listeners
			windowLoad: function(){},
			windowResize: function(){
				indigoQF.listeners.updatePageMenuPositions();

			},

			// Clear Multi select
			clearMultiSelection: function(e){
				$("iframe").trigger("click");
			},

			panelMenuModifyDOM: function(){
				/* PROBLEM ::: CONTENT OF GRID ROW IS EMPTY AT THE TIME OF PROCESSING */
				// panelMenuModifyDOM() ::: Used to add class names / attributes to side panel so that it can be correctly displayed with CSS"
				console.log("panelMenuModifyDOM() ::: recode DOM");
				console.log("tab_serverSettings", $("#JahiaGxtSidePanelTabs .tab_serverSettings .x-grid3-row").length);
				console.log("tab_systemSiteSettings", $("#JahiaGxtSidePanelTabs .tab_systemSiteSettings .x-grid3-row").length);


				var menu = $("#JahiaGxtSidePanelTabs .tab_serverSettings .x-grid3-row"),
					previousItemLevel = 0,
					relPosCounter = 0,
					parentCounter = 0;
					previousParentID = null;

				menu.each(function(index, menuItem_el){
					var menuItem = $(this),
						indentSpacer = menuItem.find(".x-tree3-el > img:nth-child(1)"), // Sub menus are 'created' by indenting menu items with a transparent GIF spacer in multiples of 18. So a width of 0 is level 1, 18 is level 2, and so on...
						indentSpacerWidth = indentSpacer.width(),
						subMenuJoint = menuItem.find(".x-tree3-el .x-tree3-node-joint"), // If the menu item has a submenu then the joint is visible (has a height).
						subMenuJointHeight = (subMenuJoint.length > 0) ? subMenuJoint.attr("style").indexOf("height") > -1 : 0,
						hasSubMenu = subMenuJointHeight > 0,
						subMenuAlreadyLoaded = $("#JahiaGxtSidePanelTabs .x-grid3-row[parent-ID='" + index + "']").length > 0,
						menuItemLevel = indentSpacerWidth / 18,
						parentItemLevel,
						parentID = null;

					if(menuItemLevel > 0){
						// Sub menu item, so get ID from previous entry as this is its parent.
						parentItemLevel = menuItemLevel - 1;
						parentID = $(menuItem_el).prevAll("[menu-item-level='" + parentItemLevel + "']").first().attr("menu-id");

						if(parentID == previousParentID){
							// Part of same sub menu
							relPosCounter++;

						} else {
							relPosCounter = 0;
						}

						previousParentID = parentID;
					} else {
						relPosCounter = null;
						parentCounter++;
					}

					menuItem
						.attr("menu-ID", index)
						.attr("menu-rel-ID", relPosCounter)
						.attr("menu-item-level", menuItemLevel)
						.attr("parent-ID", parentID)
						.attr("parent-rel-ID", parentCounter)
						.attr("has-sub-menu", hasSubMenu)
						.attr("sub-menu-available", subMenuAlreadyLoaded);


				});





				var menu = $("#JahiaGxtSidePanelTabs .tab_systemSiteSettings .x-grid3-row"),
					previousItemLevel = 0,
					relPosCounter = 0,
					parentCounter = 0;
					previousParentID = null,
					systemSiteSettingsMenuItem = true;

				menu.each(function(index, menuItem_el){
					var menuItem = $(this),
						indentSpacer = menuItem.find(".x-tree3-el > img:nth-child(1)"), // Sub menus are 'created' by indenting menu items with a transparent GIF spacer in multiples of 18. So a width of 0 is level 1, 18 is level 2, and so on...
						indentSpacerWidth = indentSpacer.width(),
						subMenuJoint = menuItem.find(".x-tree3-el .x-tree3-node-joint"), // If the menu item has a submenu then the joint is visible (has a height).
						subMenuJointHeight = (subMenuJoint.length > 0) ? subMenuJoint.attr("style").indexOf("height") > -1 : 0,
						hasSubMenu = subMenuJointHeight > 0,
						subMenuAlreadyLoaded = $("#JahiaGxtSidePanelTabs .x-grid3-row[parent-ID='" + index + "']").length > 0,
						menuItemLevel = indentSpacerWidth / 18,
						menuItemLevel = 1,
						parentItemLevel;
						relPosCounter = null;
						parentCounter++;

					menuItem
						.attr("menu-ID", index)
						.attr("menu-rel-ID", relPosCounter)
						.attr("menu-item-level", menuItemLevel)
						.attr("menu-system-site-settings", systemSiteSettingsMenuItem)
						.attr("parent-ID", "50")
						.attr("parent-rel-ID", parentCounter)
						.attr("has-sub-menu", hasSubMenu)
						.attr("sub-menu-available", subMenuAlreadyLoaded);

				});







				// Add sub menu IDs (0, 1, 2, ...)
				var previousParentID = 0,
					counter = 0;

				menu.closest("[parent-id]").each(function(index, menuItem_el){
					var parentID = $(menuItem_el).attr("parent-ID");

					if(parentID == previousParentID){
						counter++;
					} else {
						counter = 0;
					}

					$(menuItem_el).attr("sub-menu-ID", counter);

					previousParentID = parentID;
				});

			},

			// Body updates
			displaynameChanged: function(){

				var pageTitle,
					multiselect = "off";

				switch(indigoQF.status.currentPage.displayname){
					case "settings":
					case "System Site":
						console.log("trigger the clicks");
						// Need to trigger a click on Settings tabs to make sure that the menus are loaded in advance.
						$("#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab").trigger("click");

						// Attach an observer to the Side Panel Menu
						indigoQF.observers.panelMenuObserver();
						break;

					default:
						// Presumably in Edit Mode or Contribute Mode, in which case we need to set the page title
						switch(indigoQF.status.multiselection.count){
							case 0:
								pageTitle = indigoQF.status.currentPage.displayname;
								break;

							case 1:
								pageTitle = "1 selected item";
								pageTitle = $("body").attr("data-singleselection-node-displayname");
								multiselect = "on";
								break;

							default:
								pageTitle = indigoQF.status.multiselection.count + " selected items";
								multiselect = "on";
								break;
						}

						// Set multiselect status in body attribute...
						$("body").attr("data-multiselect", multiselect);

						// QUICKFIX::: CLose Side Panel If Open ( user has selected an element )
						if($("body").attr("data-INDIGO-GWT-SIDE-PANEL") == "open"){
							indigoQF.listeners.closeSidePanel();

						}
						// END QUICKFIX

						// Page Title in Edit Made
						$(".x-current-page-path").attr("data-PAGE-NAME",pageTitle);

						// Page Title in Contribute Made
						$(".x-viewport-contributemode .toolbar-itemsgroup-languageswitcher").attr("data-PAGE-NAME",pageTitle);

						// Page Titles need centering
						indigoQF.listeners.updatePageMenuPositions();

						// Remove Mutation Observer used in Settings pages (if attached)
						if(indigoQF.status.panelMenu.observer){
							indigoQF.status.panelMenu.observer.disconnect();
							indigoQF.status.panelMenu.observer = null;

						}
				}


			},
			countChanged: function(count){
				// Multiple Items have been selected (in Edit Mode)
				indigoQF.status.multiselection.count = count;

				// Refresh the title of the page accordingly
				indigoQF.listeners.displaynameChanged();

				setTimeout(function(){
					$(".editModeContextMenu .x-menu-list").attr("data-selected-name", $("body").attr("data-singleselection-node-displayname"));
				}, 50);
			},
			publicationStatusChanged: function(status){
				// Publication status of the current page has changed (in edit or contribute mode). Update status accordingly.
				$("body").attr("data-PAGE-PUBLICATION-STATUS", status);

			},

			updatePageMenuPositions: function(){
				// Center title to page and move surrounding menus to right and left.
				// Ask Thomas for a body attribute to distinguish EDIT and CONTRIBUTE modes.

				// EDIT MODE page title positions
				var editMode = {};
					editMode.pageNameLeft = parseInt($(indigoQF.config.selectors.editModePageName).position().left);
					editMode.pageNameWidth = Math.floor($(indigoQF.config.selectors.editModePageName).width()) - 1;
					editMode.pageNameRight = editMode.pageNameLeft + editMode.pageNameWidth;

				// Preview Menu
				$(".edit-menu-view").css({
					"left": (editMode.pageNameRight + 76) + "px",
					"opacity": 1
				});

				// Publication Menu
				$(".edit-menu-publication").css({
					"left": (editMode.pageNameRight + 65) + "px",
					"opacity": 1
				});

				// More Info Menu (previously labeled as Edit )
				$(indigoQF.config.selectors.editModeMoreInfo).css({
					"left": (editMode.pageNameLeft + 92) + "px",
					"opacity": 1
				});

				// CONTRIBUTE MODE page title positions
				var contributeMode = {};
					contributeMode.pageNameWidth = function(){
						/* Because the Page Title is an ::after we can not access it via Jquery, have to get the computed width of the pseudo element ... */
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

				// Language Selector
				$(indigoQF.config.selectors.contributeModeLanguageSelector).css({
					"margin-left": "-" + (contributeMode.pageNameWidth / 2) + "px"
				});

				// Publication Menu
				$(".contribute-menu-publication").css({
					left: contributeMode.pageNameRight + "px"
				});

				// Preview Menu
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
				// Toggle the UI Theme by changing the body attribute accordingly.

				/* The button firing this event is actually a pseudo element atached to a table.
				// The tables CSS has been set to ignore all pointer events EXCEPT the pseudo element who accepts pointer events.
				// This allows us to capture a click on the pseudo element, but we have to check that it a child of the table want the one that was clicked */

				if($(e.target).hasClass("x-toolbar-ct")){
					$("body").attr("data-INDIGO-UI", function(index, attr){
						return (attr == "light") ? "dark" : "light";
					});

				}
			},
			openManagerMenu: function(){
				// Manager Menu has been opened.

				if(indigoQF.status.quickMenu.active){
					// Get admin mode / studio mode buttons if available
					$("body").attr("data-quick-menu-open", "open");
					$(".edit-menu-mode").trigger("click");
					$(".contribute-menu-mode").trigger("click");

					$(".edit-menu-user").trigger("click");
					$(".contribute-menu-user").trigger("click");

					$(".menu-edit-menu-user").attr("data-username", indigoQF.status.user.userName);

					// Store values
					//indigoQF.status.user.adminMode = $(".toolbar-item-admin").length;
					//indigoQF.status.user.studioMode = $(".toolbar-item-studio").length;

					// Remove duplucate stuff from mode menu
					$(".toolbar-item-live").hide();
					$(".toolbar-item-preview").hide();
					$(".toolbar-item-contribute").hide();

					// Get rid off menu
					//$("body").trigger("click");

					$('#quick-menu-contents').fadeIn("slow");
					$("#quick-menu").addClass("open");
				}



				// Close the side panel if it is open.
				indigoQF.listeners.closeSidePanel()
			},
			closeManagerMenu: function(){
				// Manager Menu has been closed by clicking on the X.
				// Can not remove the actual DOM node as it causes problems with GWT, so just hide it instead.
				if(indigoQF.status.quickMenu.active){
					$("body").attr("data-quick-menu-open", "");

					$("#quick-menu").removeClass("open");
					$('#quick-menu-contents').fadeOut();
					$(".menu-contribute-menu-mode").fadeOut();
					$(".menu-contribute-menu-user").fadeOut();
					$(".menu-edit-menu-mode").fadeOut();
					$(".menu-edit-menu-user").fadeOut();
				}

				$(".menu-editmode-managers-menu").fadeOut();
			},
			toggleSidePanelDocking: function(e){
				// This listener has a dual purpose depending on where it was called from.
				// If called from the Edit Mode then it toggles the Side Panel Menu as PINNED and FLOATING
				// If it is called from the settings window then it acts as a close button, closing the settings and returning to the Edit Mode.

				var windowStyle = $("body").attr("data-edit-window-style");

				switch(windowStyle){
					case "settings":
						// SETTINGS MODE: Button acts as a button that closed the Settings Overlay Page

						$("body").attr("data-edit-window-style", "default");
						indigoQF.listeners.closeSidePanel()

						// Load the last page displayed in the Edit Mode. Technically this should never be NULL. However, need to assign a value on first window load as it is currently only assigned when a user clicks a page in the Page Tree.
						if(indigoQF.status.lastOpenedPage){
							indigoQF.status.lastOpenedPage.trigger("mousedown");

						} else {
							// Could not find a page in the history so select the first page in the tree.
							// Note that this probably will never work, because by default the tree is collapsed and item #2 is not yet loaded, so a trigger click wont work as it is not there.
							// Also, we can not click the first element in the tree because it isnt actually a clickable page.
							// To solve this problem, if there isnt a nth-child(2), then the first child first needs expanding (clicking).
							$("#JahiaGxtPagesTab .x-grid3-row:nth-child(2)").trigger("mousedown");
						}

						break;
					default:
						// EDIT MODE: Button acts as a toggle for the side panel
						$("body").attr("data-INDIGO-GWT-FULLSCREEN", function(index, attr){
							return (attr == "on") ? "" : "on";
						})
						break;
				}



			},
			clickSidePanelSettingsTab: function(forceClick){
				console.log("Side panel settings");
				// User has clicke the Settings Tab Button.
				if(indigoQF.status.currentPage.displayname != "settings" && ($("body").attr("data-sitesettings") == "false" || forceClick)){
					$("body").attr("data-edit-window-style", "settings");

					indigoQF.listeners.openSidePanel()

					if(indigoQF.status.lastSettingsPage){
						// Found settings page in history so open it
						indigoQF.status.lastSettingsPage.trigger("click");
					} else {
						// Need to check for loaded elements ...Not found a settings page in history, so open first
						//$("#JahiaGxtSidePanelTabs #JahiaGxtSettingsTab .x-grid3-row:nth-child(1)").trigger("click");

					}
				}


			},
			clickSidePanelTab: function(){
				// User has clicked on one of the side panel tabs (except for Settings Tab which calls indigoQF.listeners.clickSidePanelSettingsTab)

				// Menus for the Tabs that call this listener require a normal side panel display
				$("body").attr("data-edit-window-style", "default");

				var tabMenuActive = $(this).hasClass("x-tab-strip-active"),
					sidePanelOpen = $("body").attr("data-INDIGO-GWT-SIDE-PANEL") == "open";

				if(tabMenuActive && sidePanelOpen){
					// CLOSE SIDE PANEL: Already open for current Tab Menu
					indigoQF.listeners.closeSidePanel()
				} else {
					// OPEN SIDE PANEL.
					indigoQF.listeners.openSidePanel()

				}



			},
			closeSidePanel: function(){
				$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "");
				console.log("CLOSE IT");

			},
			addPageToHistory: function(){
				if($("body").attr("data-sitesettings") != "true" && $("body").attr("data-main-node-displayname") != "settings"){
					var openedPage = $(this).closest("#JahiaGxtPagesTab").length > 0,
						openedSettings = $(this).closest("#JahiaGxtSettingsTab").length > 0;

						if(openedPage){
							indigoQF.status.lastOpenedPage = $(this);
						} else if(openedSettings){
							indigoQF.status.lastSettingsPage = $(this);
						}

				}


			},
			openSidePanel: function(){
				$("body").attr("data-INDIGO-GWT-SIDE-PANEL", "open");

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

				indigoQF.listeners.openSidePanel()
			},
			mouseOverSidePanelTab: function(){
				// Mouseover Side Panel tabs, so open it.
				if($("body").attr("data-selection-count") == "0"){
					indigoQF.listeners.openSidePanel()

				}
			},
			mouseLeaveSidePanelTabs: function(e){
				// CHECK if the user has actually left the Side Panel OR if they have just opened a context menu, in which case keep the Side Panel Open.
				// Note that this only applies when the Side Panel is activated on mouse over.

				if($("body > div.x-menu").length > 0){
					// A Context Menu has been found, so do not close the Side Panel.

				} else {
					// No Context menu found, so assume that the Side Panel has really been mouseout-ed - close it.

					indigoQF.listeners.closeSidePanel()

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
					indigoQF.listeners.openSidePanel()

				}
			},
			clickSidePanelMoreOptionsButton: function(e){
				// Open Context Menu when clicking "More" button.
				console.log(e.target);
				var clickedElement = $(e.target),
					acceptClick = clickedElement.hasClass("x-grid3-td-displayName");

				if(acceptClick){
					console.log("VALID CLICK FOR MORE OPTIONS");
					var eV = new jQuery.Event("contextmenu");
						eV.clientX = e.pageX;
						eV.clientY = e.pageY;
						$(this).trigger(eV);
				}


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
				/*
					Mutation Observer attached to BODY tag.
					Listening for changes in attributes to determine if page has changed OR if the user has started a multiple selection.
				*/


				var // Configuration of the observer:
					config = {
						attributes: true,
						childList: true,
						characterData: true
						//subtree: true
					},

					// Attach Mutation Observer to the BODY tag
					target = document.body,

					// Mutation Observer
					observer = new MutationObserver(function(mutations){
						var publicationStatus,
							publishSplit,
							publishName,
							friendlyPublishName;

						// Loop through all mutations in BODY tag
						mutations.forEach(function(mutation){

							if(mutation.attributeName == "class"){
								if($("body").hasClass("x-dd-cursor")){
									// x-dd-cursor class is what GWT uses to say that a drag and drop has started.
									indigoQF.listeners.closeSidePanel();

								}

							}

							if(mutation.attributeName == "data-currentuser"){
								if(indigoQF.status.quickMenu.active){
									indigoQF.status.user.userName = $("body").attr("data-currentuser");
									$("#quick-menu-user-menu").html(indigoQF.status.user.userName);
								}

							}

							if(mutation.attributeName == "data-sitesettings"){
								console.log("DATA SITE SETTINGS CHANGE ...", $("body").attr("data-sitesettings"));

								if($("body").attr("data-sitesettings") == "true" && $("body").attr("data-edit-window-style") != "settings"){
									console.log("Gona trigger the click", $("#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab"));
									indigoQF.listeners.clickSidePanelSettingsTab(true);
								}
							}

							// Check if Page has been changed
							if(mutation.attributeName == "data-main-node-displayname"){
								indigoQF.status.currentPage.displayname = $("body").attr("data-main-node-displayname");

								// Start listening to menu again
								indigoQF.listeners.displaynameChanged();

							}

							// Check if multiple selection has been initiated
							if(mutation.attributeName == "data-selection-count"){
								var count = parseInt($("body").attr("data-selection-count"));

								indigoQF.listeners.countChanged(count);

							}

							/* START MESSY */
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
							/* END MESSY */

						});
					});

				// Pass in the target node, as well as the observer options
				observer.observe(target, config);
			},
			panelMenuObserver: function(){
				//Used to listen for changes to side menu panel and update CSS accordingly
				if(!indigoQF.status.panelMenu.observer){
					// The Observer hasnt already been attached, so do it now.

					// Used to control side panel menu by CLICK
					$("body")
						.off("mouseenter", "#JahiaGxtSidePanelTabs ul.x-tab-strip li:nth-child(2)")
						.on("mouseenter", "#JahiaGxtSidePanelTabs ul.x-tab-strip li:nth-child(2)", function(){
							indigoQF.status.panelMenu.openedJoint = 50;
							$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint)
						})

					// FOR THE TRASH ?!
					$("body")
						.off("click", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row")
						.on("click", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row", function(e){

						if(indigoQF.status.panelMenu.allowClickToCloseSubMenu){
							var menuItem = $(this),
								menuID = menuItem.attr("menu-ID"),
								currentMenu = $("#JahiaGxtSidePanelTabs").attr("current-sub-menu"),
								topLevelMenuItem = menuItem.attr("menu-item-level") == 0;

							if(topLevelMenuItem){
								if(currentMenu == menuID){
									// Already open, so hide it
									indigoQF.status.panelMenu.openedJoint = null;
									$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
								} else {
									// Open it
									indigoQF.status.panelMenu.openedJoint = menuID;
									$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
								}

							}
						}


					});

					// Used to control left panel menu by MOUSE OVER
					$("body")
						.off("mouseenter", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row")
						.on("mouseenter", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row", function(e){
						var menuItem = $(this),
							menuID = menuItem.attr("menu-ID"),
							subMenuAvailable = menuItem.attr("sub-menu-available") == "true",
							hasSubMenu = menuItem.attr("has-sub-menu") == "true",
							currentMenu = $("#JahiaGxtSidePanelTabs").attr("current-sub-menu"),
							topLevelMenuItem = menuItem.attr("menu-item-level") == 0;

						if(hasSubMenu){
							if(currentMenu == menuID){
								// Menu already visible so hide it
								indigoQF.status.panelMenu.openedJoint = null;
								$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);

							} else {
								// Open this sub menuID
								indigoQF.status.panelMenu.openedJoint = menuID;
								$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);

								if(!subMenuAvailable){
									// Sub menu hasnt been loaded into the DOM, so trigger click the joint that will load it from GWT
									menuItem.find(".x-tree3-node-joint").trigger("click");
								}

							}
						} else {
							if(topLevelMenuItem) {
								// Mouse enter top level menu item with no sub menu, so just close any open sub menus
								indigoQF.status.panelMenu.openedJoint = null;
								$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
							} else {
								// Mouse over sub level menu
								if(indigoQF.status.panelMenu.autoHideOnMouseOut){
									clearTimeout(indigoQF.status.panelMenu.mouseOutTimer);
								}

							}
						}
					});

					$("body")
						.off("mouseleave", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row")
						.on("mouseleave", ".x-viewport-adminmode #JahiaGxtSidePanelTabs .x-grid3-row", function(e){

						if(indigoQF.status.panelMenu.autoHideOnMouseOut){
							var menuItem = $(this),
								subLevelMenuItem = menuItem.attr("menu-item-level") > 0;

							if(subLevelMenuItem){

								indigoQF.status.panelMenu.mouseOutTimer = setTimeout(function(){
									indigoQF.status.panelMenu.openedJoint = null;
									$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
									clearTimeout(indigoQF.status.panelMenu.mouseOutTimer);
								}, indigoQF.status.panelMenu.mouseOutTimeValue);


							}
						}



					});

					var config = {
							attributes: true,
							childList: true,
							characterData: true,
							subtree: true
						},
						target = document.getElementById("JahiaGxtSidePanelTabs");


						indigoQF.status.panelMenu.observer = new MutationObserver(function(mutations){
							var removedNodes;

							mutations.forEach(function(mutation){
								// Knowing when the Side Panel is loaded is a real PAIN. There is no feedback.
								// Nodes are added at a will and not even in one go, so we have to execute the callback a few times.
								// The callback is executed when the LOADING MASK has been removed from the Menu.

								removedNodes = mutation.removedNodes;

								if(removedNodes.length > 0){
								  if($(removedNodes[0]).hasClass("ext-el-mask") || $(removedNodes[0]).hasClass("x-tree3-node-joint")){
									  indigoQF.listeners.panelMenuModifyDOM();
								  }
								}


							});
						})

						// Pass in the target node, as well as the observer options
						indigoQF.status.panelMenu.observer.observe(target, config);
				} else {
					// Close Sub Menus if still open (after clicking a link)
					indigoQF.status.panelMenu.openedJoint = null;
					$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);
				}
			}
		}
	}

	// Page is ready, so start the ball rolling ...
	window.onload = indigoQF.init;


}());
