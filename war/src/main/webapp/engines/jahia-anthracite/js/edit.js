(function(){
	var indigoQF = {
		init: function(){
			// Attach window listeners
			window.onresize = indigoQF.listeners.windowResize;

			// Setup observers
			indigoQF.observers.body();



			// Setup listeners
			$(document).ready(function(){
				$("body")
					.on("click", ".x-viewport-editmode .x-toolbar-first > table", indigoQF.listeners.toggleThemeMode)
					.on("click", ".editmode-managers-menu", indigoQF.listeners.openManagerMenu)
					.on("click", ".menu-editmode-managers-menu", indigoQF.listeners.closeManagerMenu)
					.on("click", "#JahiaGxtSidePanelTabs > div:nth-child(1) > div:nth-child(2)", indigoQF.listeners.toggleSidePanelDocking)
					.on("mouseover", ".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(7)", indigoQF.listeners.mouseOverHamburger)
					.on("click", "#JahiaGxtSidePanelTabs .x-tree3-node-text", indigoQF.listeners.clickSidePanelMoreOptionsButton)
					.on("click", "#JahiaGxtFileImagesBrowseTab .thumb-wrap > div:nth-child(1) > div:nth-child(2) div:nth-child(1) b", indigoQF.listeners.clickSidePanelFileThumbMoreOptionsButton)
					.on("click", "#JahiaGxtSidePanelTabs .x-grid3-row.sub-level-menu-item", indigoQF.listeners.closeSidePanelSubMenus)
					.on("click", "#JahiaGxtSidePanelTabs .expand-sub-level-menu", function(){
						alert("Expand Window");
					});

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
				style: "click",
				observer: null,
				openedSide: null,
				tabs: {}
			},
			panelMenu: {
				openedJoint: null
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

			panelMenuModifyDOM: function(){
				//console.clear();
				console.log("panelMenuModifyDOM() ::: Used to add class names / attributes to side panel so that it can be correctly displayed with CSS");

				var menu = $("#JahiaGxtSidePanelTabs .x-grid3-row"),
					previousItemLevel = 0;

				menu.each(function(index, menuItem_el){
					var menuItem = $(this),
						indentSpacer = menuItem.find(".x-tree3-el > img:nth-child(1)"), // Sub menus are 'created' by indenting menu items with a transparent GIF spacer in multiples of 18. So a width of 0 is level 1, 18 is level 2, and so on...
						indentSpacerWidth = indentSpacer.width(),
						subMenuJoint = menuItem.find(".x-tree3-el .x-tree3-node-joint"), // If the menu item has a submenu then the joint is visible (has a height).
						subMenuJointHeight = subMenuJoint.attr("style").indexOf("height") > -1,
						hasSubMenu = subMenuJointHeight > 0,
						subMenuAlreadyLoaded = $("#JahiaGxtSidePanelTabs .x-grid3-row[parent-ID='" + index + "']").length > 0,
						menuItemLevel = indentSpacerWidth / 18,
						parentItemLevel,
						parentID = null;

					if(menuItemLevel > 0){
						// Sub menu item, so get ID from previous entry as this is its parent.
						parentItemLevel = menuItemLevel - 1;
						parentID = $(menuItem_el).prevAll("[menu-item-level='" + parentItemLevel + "']").first().attr("menu-id");

					}

					menuItem
						.attr("menu-ID", index)
						.attr("menu-item-level", menuItemLevel)
						.attr("parent-ID", parentID)
						.attr("has-sub-menu", hasSubMenu)
						.attr("sub-menu-available", subMenuAlreadyLoaded);

					console.log(menuItem_el);

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

			// Side Panel Menu loaded
			sidePanelUpdated: function(nodes){
				// REMOVE THE NODE JOINT WHEN NO CHILDREN

				switch(indigoQF.status.currentPage.displayname){
					case "settings":
						var firstMenuItem = $(nodes[0]),
							parentMenu = $(nodes[0].previousSibling),
							parentMenuID = null,
							parentMenuTop = (parentMenu.length > 0) ? parentMenu.position().top - 24 : 0,
							imageSeperator = firstMenuItem.find(".x-tree3-el img:nth-child(1)"),
							imageSeperatorWidth = imageSeperator.width(),
							menuLevel = imageSeperatorWidth / 18,
							className = (menuLevel == 0) ? " top-level-menu-item" : " sub-level-menu-item",
							nCounter = 0,
							nCounterAttr,
							menuLevelAttr,
							parentNCounter,
							hasChildren;


						nodes.forEach(function(node){
							nCounter++;

							$(node)
								.attr("data-n-counter", nCounter)
								.attr("data-menu-level", menuLevel)
								.addClass("n-counter-" + nCounter)
								.addClass(className)
								.css("margin-top", parentMenuTop + "px");

							hasChildren = $(node).find(".x-tree3-node-joint").attr("style").indexOf("height") > -1;

							if(menuLevel == 0 && !hasChildren){
								$(node).find(".x-tree3-node-joint").css("display", "none");
							}

							if(menuLevel > 0){
								// Dealing with a sub menu so attach its parent ID
								parentMenuID = parentMenu.attr("data-n-counter");

								$(node).attr("parent-n-counter", parentMenuID);

							}

							$(node).on("click", function(e){

								var menuItem = $(this).closest(".x-grid3-row"),
									menuItemID = menuItem.attr("data-n-counter"),
									menuLevel = menuItem.attr("data-menu-level");

								if(menuLevel === "0"){
									indigoQF.status.sidePanelTabs.openedSide = menuItemID;

									$("#JahiaGxtSettingsTab").removeClass("disable-node-joint-" + indigoQF.status.sidePanelTabs.openedSide);

									$(".sub-level-menu-item").not("[parent-n-counter='" + indigoQF.status.sidePanelTabs.openedSide + "']")
										.attr("menu-item-visibility", "off")
										.css("display", "none");

									$(".sub-level-menu-item[parent-n-counter='" + indigoQF.status.sidePanelTabs.openedSide + "']")
										.attr("menu-item-visibility", "on")
										.css("display", "block");

									$("#JahiaGxtSidePanelTabs").attr("data-current-tab", indigoQF.status.sidePanelTabs.openedSide);

								}



							})

						});



						$(".sub-level-menu-item")
							.attr("menu-item-visibility", "off")
							.css("display", "none");

						break;
				}


			},

			trackSidePanelSubMenus: function(){
				// Not alway called !!
				var openedMenu = null;

				indigoQF.status.sidePanelTabs.tabs = {};
				$("#JahiaGxtSettingsTab").removeClass("disable-node-joint-1 disable-node-joint-2 disable-node-joint-3 disable-node-joint-4 disable-node-joint-5 disable-node-joint-6 disable-node-joint-7 disable-node-joint-8 disable-node-joint-9");

				$(".sub-level-menu-item").each(function(){
					var menuItem = $(this),
						parentID = menuItem.attr("parent-n-counter"),
						visibility = menuItem.attr("menu-item-visibility") || "on";

					indigoQF.status.sidePanelTabs.tabs[parentID] = visibility;

					if(visibility == "on"){
						openedMenu = parentID;
					}


				});

				if(openedMenu){
				} else {
					$("#JahiaGxtSidePanelTabs").attr("data-current-tab", "");

				}

				for(parentID in indigoQF.status.sidePanelTabs.tabs){
					if(indigoQF.status.sidePanelTabs.tabs[parentID] == "off"){
						// Disable the node joint
						$("#JahiaGxtSettingsTab").addClass("disable-node-joint-" + parentID);


					}
				}
			},

			closeSidePanelSubMenus: function(){
				var menuItem = $(this),
					parentMenuID = menuItem.attr("parent-n-counter");

				$(".sub-level-menu-item[parent-n-counter='" + parentMenuID + "']")
					.attr("menu-item-visibility", "off")
					.css("display", "none");

				indigoQF.listeners.trackSidePanelSubMenus();
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

				switch(indigoQF.status.currentPage.displayname){
					case "settings":
						indigoQF.status.sidePanelTabs.hideInit = true;
						indigoQF.listeners.trackSidePanelSubMenus();

						break;
				}

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

							if(attributeNames == "data-main-node-displayname"){
								var pageName = $("body").attr("data-main-node-displayname");
								indigoQF.status.currentPage.displayname = pageName;

								// Start listening to menu again
								indigoQF.observers.panelMenuObserver();

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
			},
			panelMenuObserver: function(){
				// Find out which menus are already loaded, then I can disable the joint...

				console.log("sidePanelTabs() ::: Used to listen for changes to side menu panel and update CSS accordingly");

				if(!indigoQF.status.sidePanelTabs.observer){
					// Setup new observer


					$("body").on("click", "#JahiaGxtSidePanelTabs .x-grid3-row", function(e){

						var menuItem = $(this),
							menuID = menuItem.attr("menu-ID"),
							hasSubMenu = menuItem.attr("has-sub-menu");

							// console.log("CLICKED ON A MENU ITEM", hasSubMenu);

					});

					$("body").on("mousedown", "#JahiaGxtSidePanelTabs .x-grid3-row", function(e){

						var clickedJoint = $(e.target).hasClass("x-tree3-node-joint"),
							menuItem = $(this),
							menuID = menuItem.attr("menu-ID");

						if(clickedJoint){
							if(indigoQF.status.panelMenu.openedJoint == menuID){
								indigoQF.status.panelMenu.openedJoint = null; // close it
							} else {
								indigoQF.status.panelMenu.openedJoint = menuID; // clicked on a new one

							}

							$("#JahiaGxtSidePanelTabs").attr("current-sub-menu", indigoQF.status.panelMenu.openedJoint);

						}



					})

					var config = {
							attributes: true,
							childList: true,
							characterData: true,
							subtree: true
						},
						target = document.getElementById("JahiaGxtSidePanelTabs");

						indigoQF.status.sidePanelTabs.observer = new MutationObserver(function(mutations){
							var publicationStatus,
								newNodes,
								removedNodes;

							mutations.forEach(function(mutation){
								newNodes = mutation.addedNodes;
								removedNodes = mutation.removedNodes;

								//console.log("mutation", mutation);

								if(newNodes.length > 0){
									// console.log("newNodes:", newNodes);
									if($(newNodes[0]).hasClass("ext-el-mask")){
										// console.log("sidePanelTabs() ::: GWT is modifying side panel");
									}



								}

								if(removedNodes.length > 0){
									// console.log("removedNodes:", removedNodes);
								  if($(removedNodes[0]).hasClass("ext-el-mask") || $(removedNodes[0]).hasClass("x-tree3-node-joint")){
									//   console.log("sidePanelTabs() ::: GWT has finished modifying side panel");
									  indigoQF.listeners.panelMenuModifyDOM();
								  }
								}


							});
						})

						// Pass in the target node, as well as the observer options
						console.log("sidePanelTabs() ::: Attach Mutation Observer to #JahiaGxtSidePanelTabs");
						indigoQF.status.sidePanelTabs.observer.observe(target, config);
				} else {
					console.log("ignore observer, already in place");
				}
			},
			sidePanelTabsBAK: function(){
				var config = {
						attributes: true,
						childList: true,
						characterData: true,
						subtree: true
					},
					target = document.getElementById("JahiaGxtSidePanelTabs");


				indigoQF.status.sidePanelTabs.observer = new MutationObserver(function(mutations){
					var publicationStatus,
						newNodes;

					mutations.forEach(function(mutation){
						newNodes = mutation.addedNodes;
						removedNodes = mutation.removedNodes;

						if(newNodes.length > 0){
						  // Node(s) have been added
						  if(newNodes[0].getAttribute("class").indexOf("x-grid3-row") > -1){
							  // It is a Menu Node, so act upon it
							  indigoQF.listeners.sidePanelUpdated(newNodes);
							  indigoQF.listeners.trackSidePanelSubMenus();

						  }
						}

						if(removedNodes.length > 0){
						  // Node(s) have been added
						  if(removedNodes[0].getAttribute("class").indexOf("x-grid3-row") > -1){
							  // It is a Menu Node, so act upon it
							  indigoQF.listeners.trackSidePanelSubMenus();

						  }
						}


					});
				})

				// Pass in the target node, as well as the observer options
				indigoQF.status.sidePanelTabs.observer.observe(target, config);
			}
		}
	}

	window.onload = indigoQF.init;


}());
