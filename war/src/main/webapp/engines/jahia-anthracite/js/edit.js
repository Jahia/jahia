(function(exposeAs){

    var Dex = function(selector){

        // Return Dex object
        return new Dex.fn.init(selector);

    };

    Dex.fn = Dex.prototype = {
        init: function(selector){
            this.selector = selector;

            return this;
        }

    };

    // CONSTANTS
    var UNDEFINED = "undefined";

    // HIDDEN VARIABLES
    var observers = {},
        matches = {},
        queue = {},
        observerConfig = {
            attributes: true,
            childList: true,
            characterData: false,
            subtree: true
        },
        mutationObserverCallback = function(mutation){

            // check for onClose
            if (mutation.removedNodes.length > 0) {
                var removedNode = mutation.removedNodes[0];
                // Skip text nodes, check if getAttribute is present
                if (removedNode.getAttribute) {
                    for (removedID in queue["close"]) {
                        if (matches[removedID](removedNode)) {
                            Dex(removedID).trigger({
                                eventType: "close",
                                nodes: removedNode
                            });
                        }
                    }
                }
            }

            // check for onOpen
            if (mutation.addedNodes.length > 0) {
                var addedNode = mutation.addedNodes[0];
                // Skip text nodes, check if getAttribute is present
                if (addedNode.getAttribute) {
                    for (selector in queue["open"]) {
                        if (matches[selector](addedNode)) {
                            Dex(selector).trigger({
                                eventType: "open",
                                nodes: addedNode
                            });
                        }
                    }

                    // Handle tree change
                    if (addedNode.classList.contains("x-grid3-row")) {
                        for (selector in queue["treechange"]) {

                            // Find matching ancestor
                            var treeNode = addedNode;
                            while ((treeNode = treeNode.parentElement) && !matches[selector](treeNode)) ;
                            if (treeNode) {
                                Dex(selector).trigger({
                                    eventType: "treechange",
                                    nodes: mutation.addedNodes
                                });
                            }
                        }
                    }
                }
            }

            // Check for onAttr
            if(mutation.attributeName) {
                for(selector in queue["attribute"]){
                    if (matches[selector](mutation.target)) {
                        // Matched selector
                        for(var n = 0; n < queue["attribute"][selector].length; n++){
                            if(queue["attribute"][selector][n].attribute === mutation.attributeName){
                                Dex(selector).trigger({
                                    eventType: "attribute",
                                    attribute: {
                                        name: queue["attribute"][selector][n].attribute,
                                        value: mutation.target.attributes[mutation.attributeName].value
                                    }
                                });
                            }
                        }
                    }
                }
            }
        };

    // EXPOSED SELECTOR FUNCTIONS
    Dex.fn.init.prototype = {

        // Attach Observer
        attach: function(callback){
            // See if array exists for this selector, if not add
            if(!observers[this.selector]){
                observers[this.selector] = [];
            }

            // Get last position in array
            var index = observers[this.selector].length,
                target = $(this.selector)[0];

            // Create new Mutation Observer
            observers[this.selector][index] = new MutationObserver(function(mutations){
                mutations.forEach(callback);
            });


            // Start Observer
            observers[this.selector][index].observe(target, observerConfig);
        },

        // SHORT HAND CALLS

        onceTreeChange: function(callback){
            this.on({
                eventType: "treechange",
                callback: callback,
                persistant: false
            });
        },
        onTreeChange: function(callback){
            this.on({
                eventType: "treechange",
                callback: callback
            })

        },

        onceOpen: function(callback){
            this.on({
                eventType: "open",
                callback: callback,
                persistant: false
            })
        },
        onOpen: function(callback){
            this.on({
                eventType: "open",
                callback: callback
            })

        },

        onClose: function(callback){
            this.on({
                eventType: "close",
                callback: callback
            })

        },
        onceClose: function(callback){
            this.on({
                eventType: "close",
                callback: callback,
                persistant: false
            })

        },

        onceAttr: function(attribute, callback){
            this.on({
                eventType: "attribute",
                attribute: attribute,
                callback: callback,
                persistant: false
            })

        },
        onAttr: function(attribute, callback){
            this.on({
                eventType: "attribute",
                attribute: attribute,
                callback: callback
            })

        },


        on: function(params){
            var eventType = params.eventType,
                attribute = params.attribute,
                callback = params.callback,
                persistant = params.persistant;

            if (typeof persistant == UNDEFINED) {
                persistant = true;
            }

            if (!matches[this.selector]) {
                if (this.selector.indexOf("#")===0) {
                    var id = this.selector.substring(1);
                    matches[this.selector] = function(target) {
                        return target.getAttribute("id") === id
                    }
                } else if (this.selector.indexOf(".")===0) {
                    var cl = this.selector.substring(1)
                    matches[this.selector] = function(target) {
                        return target.classList.contains(cl);
                    }
                } else {
                    var tagName = this.selector.toUpperCase();
                    matches[this.selector] = function(target) {
                        return target.tagName === tagName;
                    }
                }
            }

            if(!queue[eventType]){
                // No eventType for this selector, add entry
                queue[eventType] = {}
            }

            if(!queue[eventType][this.selector]){
                queue[eventType][this.selector] = []
            }

            queue[eventType][this.selector].push({
                callback: callback,
                persistant: persistant,
                attribute: attribute
            })

            return this;
        },
        trigger: function(params){
            var eventType = params.eventType,
                nodes = params.nodes,
                attribute = params.attribute,
                persistantFunctions = [];

            if(	queue[eventType] &&
                queue[eventType][this.selector]){

                // FOUND ENTRY IN QUEUE ...
                while(queue[eventType][this.selector].length > 0){
                    queueEntry = queue[eventType][this.selector].pop();

                    if(attribute){
                        if(queueEntry.attribute === attribute.name){
                            queueEntry.callback(attribute.value, nodes);
                        }
                    } else {
                        queueEntry.callback(nodes);
                    }

                    if(queueEntry.persistant){
                        persistantFunctions.push(queueEntry);
                    }
                }

                queue[eventType][this.selector] = queue[eventType][this.selector].concat(persistantFunctions);

            } else {
                // NO ENTRIES
            }
        }
    };

    // EXPOSED BASE FUNCTIONS
    Dex.init = function(){
        Dex("body").attach(mutationObserverCallback);
    };

    if(exposeAs){
        window[exposeAs] = Dex;
    }

})("Dex");

(function(exposeAs){

    // MOUSE CONTROLLER
    var mouse = {
        trigger: function(node, eventType){
            if(!node) return false;

            var clickEvent = document.createEvent("MouseEvents");
            clickEvent.initEvent(eventType, true, true);
            node.dispatchEvent(clickEvent);

        }
    };

	var app = {
		data: {
			currentApp: null
		},
		onChange: function(mode){
			mode.split(" ").forEach(function(cl) {
                if (cl.indexOf("x-viewport") == 0) {

                    switch (cl) {
                        case "x-viewport-editmode":
							app.switch("edit");

                            break;
                        case "x-viewport-adminmode":
							app.switch("admin");

                            break;
                        case "x-viewport-dashboardmode":
							app.switch("dashboard");

                            break;
                        case "x-viewport-studiomode":
							app.switch("studio");

                            break;
                        case "x-viewport-contributemode":
							app.switch("contribute");

                            break;
                    }

                }
            })
		},
		onResize: function(){
			if(app.data.currentApp == "edit"){
				app.edit.topbar.reposition();
			}

			if(app.data.currentApp == "contribute"){
				app.contribute.topbar.reposition();
			}
		},
		onBlur: function(){
			// Window has lost focus, so presume that the user has clicked in the iframe.
            // If the side panel is open, then close it
            if(app.data.body.getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open"){
                app.edit.sidepanel.close();
            }
		},
		onClick: function(e){
			var inSidePanel = $(e.target).closest("#JahiaGxtSidePanelTabs, .edit-menu-sites, .window-side-panel #JahiaGxtRefreshSidePanelButton");

	        if(inSidePanel.length == 0){
                app.edit.sidepanel.close();
            }
		},
		switch: function(appID){
			if(app.data.currentApp == appID){
				return false;

			}

			app.data.currentApp = appID;

			app[appID].onOpen();

		},
		contextMenus: {
			managerMenu: {
				onOpen: function(){},
				onClose: function(){
					// Manager Menu has been closed by clicking on the X.
		            // Can not remove the actual DOM node as it causes problems with GWT, so just hide it instead.
		            $(".menu-editmode-managers-menu").fadeOut();
				}
			}
		},
		theme: {
			data: {
				skin: "dark",
				enabled: true,
				storedCSS: null
			},
			onToggle: function(e){
				// Toggle the UI Theme by changing the body attribute accordingly.

	            /* The button firing this event is actually a pseudo element atached to a table.
	            // The tables CSS has been set to ignore all pointer events EXCEPT the pseudo element who accepts pointer events.
	            // This allows us to capture a click on the pseudo element, but we have to check that it a child of the table want the one that was clicked */
	            if($(e.target).hasClass("x-toolbar-ct")){

					if(app.theme.data.skin == "dark"){
						app.theme.data.skin = "light";
					} else {
						app.theme.data.skin = "dark";
					}

					$("body").attr("data-INDIGO-UI", app.theme.data.skin);

	            }
			},
			on: function(changeSkin){
				if(changeSkin){
					app.theme.data.skin = changeSkin;
				}

				if(!app.theme.data.enabled){
					// Anthracite CSS has been removed, so plug it back in
                    $("head").append(app.theme.data.storedCSS);
				}
			},
			off: function(){
				if(app.theme.data.enabled){
					// Remove Anthracite CSS style sheet
	               $('link[rel=stylesheet][href$="edit_en.css"]').remove();

	               // Register the fact that it has been removed
	               app.theme.data.enabled = false;
				}
			},
		},
		picker: {
			data: {
				currentItem: null,
				title: null
			},
			onOpen: function(){
				app.data.body.setAttribute("data-INDIGO-PICKER-SEARCH", "");
				app.data.body.setAttribute("data-INDIGO-PICKER", "open");
				app.data.body.setAttribute("indigo-PICKER-DISPLAY", "thumbs");
			},
			onClose: function(){
				app.data.body.setAttribute("data-INDIGO-PICKER", "");

			},
			onClick: function(){
				app.data.body.setAttribute("data-INDIGO-PICKER-SOURCE-PANEL", "");

			},
			onListView: function(){
				app.data.body.setAttribute("indigo-PICKER-DISPLAY", "list");

			},
			onThumbView: function(){
				app.data.body.setAttribute("indigo-PICKER-DISPLAY", "thumbs");

			},
			row: {
				onClick: function(){
					$(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");

				},
				onMouseOver: function(e){
					// Position the preview button next to the file whilst hovering
		            app.picker.previewButton.reposition(e, {
		                left: -58,
		                top: 0
		            });

					app.picker.data.currentItem = $(this)[0];
		            app.picker.data.title = $(this).find(".x-grid3-col-name").html();

		            if($(this).hasClass("x-grid3-row-selected")){
		                $(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");

		            } else {
		                $(".toolbar-item-filepreview").attr("indigo-preview-button-state", "unselected");

		            }

		            $(".toolbar-item-filepreview").attr("indigo-preview-button", "show");
				},
				onContext: function(e){
					// Open Context Menu when clicking "More" button.
					// if matchClass is passed, then the click is ONLY accepted if the clicked element has that class.
					// if matchClass is not passed then it is accepted.
					var acceptClick = $(e.target).hasClass("x-tree3-el");

					if(acceptClick){
						$(e.target).trigger({
							type: 'mousedown',
							button: 2,
							which:3,
							clientX: e.pageX,
							clientY: e.pageY
						}).trigger({
							type:"contextmenu",
							clientX: e.pageX,
							clientY: e.pageY
						});
					}
				}
			},
			thumb: {
				onClick: function(){
					$(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");

				},
				onMouseOver: function(e){
					// Position the preview button next to the file whilst hovering
		            app.picker.previewButton.reposition(e, {
		                left: -52,
		                top: 0
		            });

					app.picker.data.currentItem = $(this)[0];
		            app.picker.data.title = $(this).attr("id");

		            if($(this).hasClass("x-view-item-sel")){
		                $(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");

		            } else {
		                $(".toolbar-item-filepreview").attr("indigo-preview-button-state", "unselected");

		            }

		            $(".toolbar-item-filepreview").attr("indigo-preview-button", "show");
				},
				onContext: function(e){
					// Open Context Menu when clicking "More" button.
					$(e.target).trigger({
						type: 'mousedown',
						button: 2,
						which:3,
						clientX: e.pageX,
						clientY: e.pageY
					}).trigger({
						type:"contextmenu",
						clientX: e.pageX,
						clientY: e.pageY
					});

				}

			},
			previewButton: {
				onMouseOver: function(){
					$(app.picker.data.currentItem)
		                .addClass("x-view-over")
		                .addClass("x-grid3-row-over");
				},
				onMouseOut: function(){
					$(app.picker.data.currentItem)
		                .removeClass("x-view-over")
		                .removeClass("x-grid3-row-over");
				},
				onClick: function(e, secondClick){
					mouse.trigger(app.picker.data.currentItem, "mousedown");
		            mouse.trigger(app.picker.data.currentItem, "mouseup");

		            if(!secondClick){
		                $("#JahiaGxtImagePopup").remove(); // remove OLD preview
		                $(this).trigger("click", [true]); // Reopen with new preview
		                $("#JahiaGxtImagePopup .x-window-bwrap").attr("data-file-name", app.picker.data.title);

		            }

		            $(".toolbar-item-filepreview").attr("indigo-preview-button", "hide");
				},
				reposition: function(e, offset){
					var offset = offset || {
		                    left: 0,
		                    top: 0
		                },
		                file = $(e.currentTarget),
		                box = file[0].getBoundingClientRect(),
		                left = box.left,
		                top = box.top,
		                width = box.width;

		            $("#JahiaGxtManagerToolbar .toolbar-item-filepreview")
		                .css({
		                    top: (top + (offset.top)) + "px",
		                    left: ((left + width) + offset.left) + "px"
		                })
		                .addClass("indigo-show-button");
				},
			},

			source: {
				onChange: function(){},
				onMouseOver: function(){
					// USER HAS ROLLED OVER THE COMBO TRIGGER
		            if(app.data.body.getAttribute("data-indigo-picker-source-panel") != "open"){
		                $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").addClass("indigo-hover");
		            }
				},
				onMouseOut: function(){
					// USER HAS ROLLED OUT OF THE COMBO TRIGGER
		            $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");
				},
				close: function(){
					// CHANGE SOURCE
		            // The user has changed SOURCE, so we just need to hide the combo...
		            app.data.body.setAttribute("data-INDIGO-PICKER-SOURCE-PANEL", "");
				},
				open: function(){},
				toggle: function(e){
					// USER HAS CLICKED THE COMBO TRIGGER
		            e.stopPropagation();

		            $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");

		            /// Toggle the attribute in body tag
		            $("body").attr("data-INDIGO-PICKER-SOURCE-PANEL", function(id, label){
		                return (label == "open") ? "" : "open";
		            });
				}
			},
			search: {
				open: function(){
					// OPEN SEARCH PANEL

		            // Close source picker if open
		            app.picker.onClose();


		            // Display the search panel
		            app.data.body.setAttribute("data-INDIGO-PICKER-SEARCH", "open");

		            // Put the results in LIST mode
		            $("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview").trigger("click");

		            // Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
		            $("#CRTbrowseTabItem").addClass("x-hide-display");


		            // Remove the directory listing ( gives the search panel an empty start)
		            setTimeout(function(){
		                $("#JahiaGxtManagerTobTable .x-grid3 .x-grid3-row").remove();
		            }, 250);
				},
				close: function(){
					// CLOSE SEARCH PANEL

		            // Hide the search panel
		            app.data.body.setAttribute("data-INDIGO-PICKER-SEARCH", "");

		            // Display the BROWSE panels
		            $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)").removeClass("x-hide-display");

		            // Get the refresh button
		            var refreshButton = $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel").not(".x-panel-collapsed").find(".x-tool-refresh")[0];

		            // CLick on the refresh button to reload the content of the directory
		            mouse.trigger(refreshButton, "click");
				},

				onContext: function(e){
					// Open Context Menu when clicking "More" button.
					$(e.target).trigger({
						type: 'mousedown',
						button: 2,
						which:3,
						clientX: e.pageX,
						clientY: e.pageY
					}).trigger({
						type:"contextmenu",
						clientX: e.pageX,
						clientY: e.pageY
					});
				}
			}
		},
		imagePreview: {
			onOpen: function(){
				app.data.body.setAttribute("data-INDIGO-IMAGE-PREVIEW", "open");

				// Attribute used to display the friendly name in edit panel
				$(".engine-panel > div.x-panel-header .x-panel-header-text").attr("data-friendly-name", "nodeDisplayName");
			},
			onClose: function(){
				app.data.body.setAttribute("data-INDIGO-IMAGE-PREVIEW", "");

			}
		},
		engine: {
			onOpen: function(){
				console.log("::: APP ::: ENGINE ::: OPEN");
				var nodeDisplayName = app.data.body.getAttribute("data-singleselection-node-displayname");

				app.data.body.setAttribute("data-INDIGO-EDIT-ENGINE", "open");

				// Attribute used to display the friendly name in edit panel
				$(".engine-panel > div.x-panel-header .x-panel-header-text").attr("data-friendly-name", nodeDisplayName);
			},
			onClose: function(){
				console.log("::: APP ::: ENGINE ::: CLOSE");
				app.iframe.clearSelection();
				app.data.body.setAttribute("data-INDIGO-EDIT-ENGINE", "");

			},
		},
		workflow: {
			dashboard: {
				onOpen: function(){
					mouse.trigger($(".workflow-dashboard-engine .x-tool-maximize")[0],"click")

				}
			}
		},
		iframe: {
			data: {
				previousUrl: -1,
				currentUrl: null,
				displayName: null,
				selectionCount: 0,
				bodyStyle: null
			},
			// Event Handlers
			onChangeSRC: function(value){
				console.log("::: APP ::: IFRAME ::: ONCHANGESRC");

				app.iframe.data.previousUrl = app.iframe.data.currentUrl;
				app.iframe.data.currentUrl = value;

                // TEMP BLIND
                // $(".window-iframe").hide();

                var elements = {
                    iframe: document.getElementsByClassName("window-iframe")[0],
                    title: document.getElementsByClassName("x-current-page-path")[0],
                    publishButton: document.getElementsByClassName("edit-menu-publication")[0],
                    refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                    previewButton: document.getElementsByClassName("edit-menu-view")[0],
                    moreInfo: document.getElementsByClassName("edit-menu-edit")[0],
                };

                elements.iframe.style.opacity = 0;
                elements.title.style.opacity = 0;
                elements.publishButton.style.opacity = 0;
                elements.refreshButton.style.opacity = 0;
                elements.previewButton.style.opacity = 0;
                elements.moreInfo.style.opacity = 0;





			},
			onChange: function(value){
				console.log("::: APP ::: IFRAME ::: ONCHANGE", value);

				if(app.iframe.data.displayName == value){
					return false;
				}

				app.iframe.data.displayName = value;

				switch(app.data.currentApp){
					case "edit":
						// Need to update the header bar
						app.edit.topbar.build();

						if(app.edit.sidepanel.isOpen()){
							app.iframe.disableClicks();
						}

						break;

					case "contribute":
						// Need to update the header bar
						app.contribute.topbar.build();

						break;
				}


			},
			onSelect: function(value){
				console.log("::: APP ::: IFRAME ::: ONSELECT");
				var count = parseInt(value);

				// Multiple Items have been selected (in Edit Mode) or removed
				// Check if value is different
				if(app.iframe.data.selectionCount == count){
					// return false;

				}

				app.iframe.data.selectionCount = count;

				// Refresh the title of the page accordingly
				app.edit.topbar.build();

				// MOVE TO A DEX LISTENER ON MENU ITEM
				// setTimeout(function(){
				// 	$(".editModeContextMenu .x-menu-list").attr("data-selected-name", app.data.body.getAttribute("data-singleselection-node-displayname"));
				// }, 50);
			},

			clearSelection: function(){
				console.log("::: APP ::: IFRAME ::: CLEARSELECTION");

				mouse.trigger(document.getElementsByClassName("window-iframe")[0], "click");

			},

			disableClicks: function(){
				console.log("::: APP ::: IFRAME ::: DISABLECLICKS");

				if(app.data.body.getAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL") == "yes" && app.data.body.getAttribute("data-sitesettings") == "false"){
	                // SAVE the curent style properties of the iframes body tag so we can revert to it once the side panel is closed.
	                var iframeBody = $(".window-iframe").contents().find("body");
	                app.iframe.data.bodyStyle = iframeBody.attr("style") || "";

	                // Remove pointer events from the iframes body, which means that once a user clicks on the iframe to exit the side panel, the content is not automatically selected.
	                iframeBody.attr("style", app.iframe.data.bodyStyle + " pointer-events: none !important");
	            }
			}
		},
		admin: {
			// Event Handlers
			onOpen: function(){
				console.log("::: APP ::: ADMIN ::: OPENED");

				var systemSettingsTabs = document.querySelectorAll(".tab_systemSiteSettings")[0],
                    serverSettingsTabs = document.querySelectorAll(".tab_serverSettings")[0];

                if(systemSettingsTabs){
                    if(window.getComputedStyle(systemSettingsTabs).display == "none"){
                        // System Settings Tabs have not been loaded, so trigger click to open them
                        mouse.trigger(document.querySelectorAll("#JahiaGxtSidePanelTabs li")[0], "click");
                    }

                }

                if(serverSettingsTabs){
                    if(window.getComputedStyle(serverSettingsTabs).display == "none"){
                        // Server Settings Tabs have not been loaded, so trigger click to open them
                        mouse.trigger(document.querySelectorAll("#JahiaGxtSidePanelTabs li")[1], "click");
                    }

                }

                // Use Anthracite CSS
				app.theme.on();

                // Set attributes to be used by CSS
                app.data.body.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "no");
                app.data.body.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");
			},
			onClose: function(){},

			// Controls

		},
		edit: {
			// Data
			data: {
				history: {
					settingspage: null,
					editpage: null,
				}
			},
			// Event Handlers
			onOpen: function(){
				console.log("::: APP ::: EDIT ::: OPENED");

				// Reset History
				app.edit.history.reset();

				// Reset to force reload of settings menu via triggering click on refresh button (later on)
				app.edit.sidepanel.data.firstRun = true;
				app.edit.sidepanel.data.firstRunSettings = true;
				app.edit.sidepanel.data.firstRunPages = true;

				app.edit.topbar.build();

				// Use Anthracite CSS
				app.theme.on();

                // Set attributes to be used by CSS
                app.data.body.setAttribute("data-edit-window-style", "default");
                app.data.body.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "");
                app.data.body.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "yes");
			},
			onClose: function(){},

			onNav: function(){

				if(app.edit.settings.data.opened){
					// CLicked on a settings page
					app.edit.sidepanel.data.firstRun = false;

					app.edit.history.add("settingspage", this);
				} else {
					app.edit.history.add("editpage", this);

				}

			},

			// Controls
			history: {
				data: {},
				add: function(type, node){
					app.edit.history.data[type] = node;

				},
				get: function(type){

					var returnResult = null,
						stillInVisibleDOM;

					if(app.edit.history.data[type]){
						stillInVisibleDOM = document.body.contains(app.edit.history.data[type]);

						if(stillInVisibleDOM){
							returnResult = app.edit.history.data[type];
						}
					}

					return returnResult;

				},
				reset: function(){
					app.edit.history.data = {
						settingspage: null,
						editpage: null,
					};
				},
			},

			topbar: {
				build: function(){
					console.log("::: APP ::: EDIT ::: TOPBAR ::: BUILD");

                    // TEMP BLIND
                    // $(".window-iframe").fadeIn("fast");

                    var elements = {
                        iframe: document.getElementsByClassName("window-iframe")[0],
                        title: document.getElementsByClassName("x-current-page-path")[0],
                        publishButton: document.getElementsByClassName("edit-menu-publication")[0],
                        refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                        previewButton: document.getElementsByClassName("edit-menu-view")[0],
                        moreInfo: document.getElementsByClassName("edit-menu-edit")[0],
                    };



                    elements.iframe.style.opacity = 1;
                    elements.title.style.opacity = 1;
                    elements.publishButton.style.opacity = 1;
                    elements.refreshButton.style.opacity = 1;
                    elements.previewButton.style.opacity = 1;
                    elements.moreInfo.style.opacity = 1;

					var pageTitle,
                        selectType = "none",
						multiselect = "off",
                        publicationStatus = document.querySelectorAll(".toolbar-item-publicationstatuswithtext .gwt-Image")[0],

                        extractStatus = function(url){
                            var urlSplit = url.split("/"),
                                fileName = urlSplit[urlSplit.length-1],
                                statusSplit = fileName.split(".png"),
                                status = statusSplit[0];

                            return status
                        };

					// Presumably in Edit Mode or Contribute Mode, in which case we need to set the page title
					switch(app.iframe.data.selectionCount){
						case 0:
							pageTitle = app.iframe.data.displayName;
                            selectType = "none";
							break;

						case 1:
							pageTitle = "1 selected item";
							pageTitle = app.data.body.getAttribute("data-singleselection-node-displayname");
							multiselect = "on";
                            selectType = "single";


							break;

						default:
							pageTitle = app.iframe.data.selectionCount + " selected items";
							multiselect = "on";
                            selectType = "multiple";
							break;
					}

					// Set multiselect status in body attribute...
                    app.data.body.setAttribute("data-multiselect", multiselect);
                    app.data.body.setAttribute("data-select-type", selectType);

					// Page Title in Edit Made
					$(".x-current-page-path").attr("data-PAGE-NAME",pageTitle);
                    $(".node-path-text-inner").html(app.iframe.data.displayName);

                    // Determine publication status
                    if(publicationStatus){
                        app.iframe.data.publication = {
                            status: extractStatus(publicationStatus.getAttribute("src")),
                            label: publicationStatus.getAttribute("title")
                        };
                    } else {
                        app.iframe.data.publication = {
                            status: null,
                            label: null
                        };
                    }

                    console.log("::: app.iframe.data.publication.status ", app.iframe.data.publication.status);

					// Page Titles need centering
					app.edit.topbar.reposition();


				},
				reposition: function(e){
					console.log("::: APP ::: EDIT ::: TOPBAR ::: REPOSITION");
					// Center title to page and move surrounding menus to right and left.

					if(document.getElementsByClassName("x-current-page-path").length > 0){


                        if(document.getElementsByClassName("x-current-page-path")[0].getAttribute("data-page-name") != null){
                            console.log("FOUND PAGE TITLE, SO SET UP POSITIONING ...");
                            document.getElementsByClassName("edit-menu-publication")[0].style.display = "block"

                            var elements = {
                                    body: document.getElementsByTagName("body")[0],
                                    title: document.getElementsByClassName("x-current-page-path")[0],
                                    innerTitle: document.getElementsByClassName("node-path-text-inner")[0],
                                    publishButton: document.getElementsByClassName("edit-menu-publication")[0],
                                    refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                                    previewButton: document.getElementsByClassName("edit-menu-view")[0],
                                    moreInfo: document.getElementsByClassName("edit-menu-edit")[0],
                                },

                                boxes = {
                                    body: elements.body.getBoundingClientRect(),
                                    title: elements.title.getBoundingClientRect()
                                };


                                // Center Page Title
                                elements.title.style.left = ((boxes.body.width / 2) - (boxes.title.width / 2)) + "px";

                                // Get Inner title bunding box
                                boxes.innerTitle = elements.innerTitle.getBoundingClientRect();

                                // Center Inner title bounding box
                                elements.innerTitle.style.left = ((boxes.body.width / 2) - (boxes.innerTitle.width / 2)) + 25 + "px";

                                // Refresh bounding box for title as it has moved
                                boxes.title = elements.title.getBoundingClientRect();

                                if(app.iframe.data.selectionCount > 0){
                                    // Multiselect, so display differently
                                    elements.publishButton.style.left = (boxes.title.left - 20) + "px";
                                    // elements.refreshButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                                    elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                                    elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 40) + "px";
                                } else {
                                    // No Select
                                    elements.publishButton.style.left = (boxes.title.left - 20) + "px";
                                    elements.refreshButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                                    elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 40) + "px";
                                    elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 70) + "px";
                                }

                                // Make sure correct class is added to publication button
                                elements.publishButton.setAttribute("data-publication-status", app.iframe.data.publication.status)




                        } else {
                            document.getElementsByClassName("edit-menu-publication")[0].style.display = "none"
                        }





                        //

			            // var editMode = {};
			            // editMode.pageNameLeft = parseInt($(".mainmodule-head-container .toolbar-left-container").position().left);
			            // editMode.pageNameWidth = Math.floor($(".mainmodule-head-container .toolbar-left-container").width()) - 1;
			            // editMode.pageNameRight = editMode.pageNameLeft + editMode.pageNameWidth;
                        //
			            // // Preview Menu
			            // $(".edit-menu-view").css({
			            //     "left": (editMode.pageNameRight + 76) + "px",
			            //     "opacity": 1
			            // });
                        //
			            // // Publication Menu
			            // $(".edit-menu-publication").css({
			            //     "left": (editMode.pageNameRight + 65) + "px",
			            //     "opacity": 1
			            // });
                        //
			            // // More Info Menu (previously labeled as Edit )
			            // $("body[data-selection-count='0'] .x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(5)").css({
			            //     "left": (editMode.pageNameLeft + 92) + "px",
			            //     "opacity": 1
			            // });

			            // More Language Menu (previously labeled as Edit )
			            // $(".mainmodule-head-container .toolbar-itemsgroup-languageswitcher").attr("style", "left: " + (editMode.pageNameLeft + 92) + "px !important; opacity: 1");

					}



				}
			},

			sidepanel: {
				data: {
					firstRun: true,
					firstRunPages: true,
					firstRunSettings: true,
					open: false
				},
				onStartDrag: function(){
					console.log("::: APP ::: EDIT ::: SIDEPANEL ::: ONSTARTDRAG");
					app.edit.sidepanel.close();

				},
				onStopDrag: function(){
					console.log("::: APP ::: EDIT ::: SIDEPANEL ::: ONSTOPDRAG");

				},
				open: function(isSettings){
					console.log("::: APP ::: EDIT ::: SIDEPANEL ::: OPEN", isSettings);

					app.data.body.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");
					app.edit.sidepanel.data.open = true;

					// GWT has problems populating the site page tree when the side panel is hidden.
					// Solution: When the side panel is opened for the FIRST TIME ONLY, the refresh button is triggered and the sites page tree is populated correctly.
					if(app.data.body.getAttribute("data-sitesettings") == "false"){

						if(isSettings){
							if(app.edit.sidepanel.data.firstRunSettings){
								mouse.trigger(document.getElementById("JahiaGxtRefreshSidePanelButton"), "click");

								app.edit.sidepanel.data.firstRunSettings = false;

								mouse.trigger($(".tab_siteSettings .x-grid3-row:nth-child(1)")[0], "mousedown");
								mouse.trigger($(".tab_siteSettings .x-grid3-row:nth-child(1)")[0], "click");
							}
						} else {
							if(app.edit.sidepanel.data.firstRunPages){
								mouse.trigger(document.getElementById("JahiaGxtRefreshSidePanelButton"), "click");
								app.edit.sidepanel.data.firstRunPages = false;
							}
						}



						app.iframe.disableClicks();
					}
				},
				close: function(){
					console.log("::: APP ::: EDIT ::: SIDEPANEL ::: CLOSE");
					if(app.data.body.getAttribute("data-edit-window-style") !== "settings" && app.data.body.getAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL") == "yes"){
		                app.data.body.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "");

		                // Revert iframes body style attribute to what it was originally
		                $(".window-iframe").contents().find("body").attr("style", app.iframe.data.bodyStyle);

		            }

					app.edit.topbar.reposition();
				},

				isOpen: function(){
					return app.edit.sidepanel.data.open;
				},

				tab: {
					onClick: function(e){
						console.log("APP ::: EDIT ::: SIDEPANEL ::: ONCLICK");





						// User has clicked on one of the side panel tabs (except for Settings Tab which calls eventHandlers.clickSidePanelSettingsTab)
			            var clickedTabID = $(this).attr("id");

			            app.data.body.setAttribute("data-INDIGO-GWT-PANEL-TAB", clickedTabID);

			            // Menus for the Tabs that call this listener require a normal side panel display
			            // app.data.body.setAttribute("data-edit-window-style", "default");

			            var tabMenuActive = $(this).hasClass("x-tab-strip-active"),
			                sidePanelOpen = app.data.body.getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open";

			            if(tabMenuActive && sidePanelOpen){
			                // CLOSE SIDE PANEL: Already open for current Tab Menu
			                app.edit.sidepanel.close()
			            } else {
			                // OPEN SIDE PANEL.
							app.edit.sidepanel.open(false);

			            }
					},
				},
				row: {
					onContext: function(e){
						// Open Context Menu when clicking "More" button.
			            var acceptClick = $(e.target).hasClass("x-grid3-td-displayName");

			            if(acceptClick){
			                $(e.target).trigger({
			                    type: 'mousedown',
			                    button: 2,
			                    which:3,
			                    clientX: e.pageX,
			                    clientY: e.pageY
			                }).trigger({
			                    type:"contextmenu",
			                    clientX: e.pageX,
			                    clientY: e.pageY
			                });
			            }
					}
				}
			},

			settings: {
				data: {
					opened: false
				},
				onChange: function(value){

					if(value == "true"){
						if(app.data.currentApp == "edit"){
							app.edit.settings.open();

						}


					} else {
						// alert("YO");
						// app.edit.settings.close();

					}
				},
				open: function(){
					console.log("::: APP ::: EDIT ::: SETTINGS ::: OPEN");

					$(".window-iframe").contents().find("head").prepend("<style>.well{border:none!important; box-shadow: none!important;} body{background-image: none!important; background-color:#f5f5f5!important}</style>");

					if(app.edit.settings.data.opened){
						return false;
					}



					app.edit.settings.data.opened = true;
					app.data.body.setAttribute("data-edit-window-style", "settings");

		            app.edit.sidepanel.open(true);

		            if(!app.edit.history.get("editpage")){
		                // Need to store the button of the current edit page so we can revert later
						app.edit.history.add("editpage", document.querySelectorAll("#JahiaGxtPagesTab .x-grid3-row")[1]);

		            }

		            if(app.edit.history.get("settingspage")){
		                // Trigger click on last viewed settings page
		                mouse.trigger(app.edit.history.get("settingspage"), "click");

		            } else {
		                // Wait until the menu has been loaded, then click on the first available menu button
		                Dex("#JahiaGxtSettingsTab").onceTreeChange(function(tree){
							console.log("MENUHASBEENLOADED");
		                    var branch,
		                        nodeJoint,
		                        firstClickableBranch;

		                    for (n = 0;  n < tree.length; n++){

		    					branch = tree[n];
		    					nodeJoint = branch.querySelectorAll(".x-tree3-node-joint")[0];

		    					// If node jint has no background then it opens a page
		    					if(	nodeJoint &&
		                            nodeJoint.style &&
		    						!nodeJoint.style.backgroundImage){

		    						// Branch has no children, so it opens a page
		                            firstClickableBranch = branch;

		                            break;
		    					}

		                    }

		                    mouse.trigger(firstClickableBranch, "mousedown");
		                    mouse.trigger(firstClickableBranch, "click");
		                });

		            }



				},
				close: function(){
					console.log("::: APP ::: EDIT ::: SETTINGS ::: CLOSE");

					var previousEditPage = app.edit.history.get("editpage");

					console.log("OPEN THIS PAGE:::::::: ", previousEditPage);

					app.edit.settings.data.opened = false;
					app.data.body.setAttribute("data-edit-window-style", "default");

		            app.edit.sidepanel.close();

		            if(previousEditPage){
		                // Trigger click on last viewed settings page
		                mouse.trigger(previousEditPage, "mousedown");
		                mouse.trigger(previousEditPage, "mouseup");
		            } else {
						// Trigger Click on Second page (first row is not an actual page)
		                mouse.trigger(document.querySelectorAll("#JahiaGxtPagesTab .x-grid3-row:nth-child(2)")[0], "mousedown");

		            }
				}
			}

		},
		dashboard: {
			// Event Handlers
			onOpen: function(){
				console.log("::: APP ::: DASHBOARD ::: OPENED");

				// Use Anthracite CSS
				app.theme.on();

                // Set attributes to be used by CSS
                app.data.body.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "no");
                app.data.body.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");
			},
			onClose: function(){},

			// Controls

		},
		studio: {
			// Event Handlers
			onOpen: function(){
				console.log("::: APP ::: STUDIO ::: OPENED");

				// Dont use Anthracite CSS
				app.theme.off();

                // Set attributes to be used by CSS
                app.data.body.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "");
                app.data.body.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "yes");
			},
			onClose: function(){},

			// Controls

		},
		contribute: {
			// Event Handlers
			onOpen: function(){
				console.log("::: APP ::: CONTRIBUTE ::: OPENED");

				// Use Anthracite CSS
				app.theme.on();

				app.contribute.topbar.build();


                // Set attributes to be used by CSS
                app.data.body.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "");
                app.data.body.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "yes");
			},
			onClose: function(){},

			// Controls
			topbar: {
				build: function(){
					console.log("::: APP ::: CONTRIBUTE ::: TOPBAR ::: BUILD");
					var pageTitle,
						multiselect = "off";

					// Presumably in Edit Mode or Contribute Mode, in which case we need to set the page title
					switch(app.iframe.data.selectionCount){
						case 0:
							pageTitle = app.iframe.data.displayName;
							break;

						case 1:
							pageTitle = "1 selected item";
							pageTitle = app.data.body.getAttribute("data-singleselection-node-displayname");
							multiselect = "on";


							break;

						default:
							pageTitle = app.iframe.data.selectionCount + " selected items";
							multiselect = "on";
							break;
					}

					// Set multiselect status in body attribute...
					app.data.body.setAttribute("data-multiselect", multiselect);

					// Page Title in Contribute Made
					$(".x-viewport-contributemode .toolbar-itemsgroup-languageswitcher").attr("data-PAGE-NAME",pageTitle);

					// Page Titles need centering
					app.contribute.topbar.reposition();


				},
				reposition: function(e){
					console.log("::: APP ::: CONTRIBUTE ::: TOPBAR ::: REPOSITION");
					// Center title to page and move surrounding menus to right and left.

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
					$(".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div input").css({
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
				}
			}

		}
	}



    // EVENT LISTENERS
    var eventListeners = {
        attach: function(){
            // HOME BREW EVENT LISTENERS
            // Set up INDIGO listeners (listening to changes in DOM)

            Dex("#JahiaGxtSettingsTab").onTreeChange(function(tree){

				var firstBranch = tree[0],
					parentBranch = firstBranch.previousSibling,
					branch,
					nodeJoint;

                for (n = 0;  n < tree.length; n++){

					branch = tree[n],
					nodeJoint = branch.querySelectorAll(".x-tree3-node-joint")[0];

					// See if Node joint is activated ( activation is assumed when a background image is assigned to the button )
					if(	nodeJoint &&
                        nodeJoint.style &&
						nodeJoint.style.backgroundImage){

						// Branch has children, so disable clicks by adding class name "unselectable-row"
						branch.classList.add("unselectable-row");
					}

                }

				if(parentBranch){
					parentBranch.classList.add("indigo-opened");
				}

            });


            Dex("#JahiaGxtContentPickerWindow").onOpen(app.picker.onOpen);

            Dex("#JahiaGxtEnginePanel").onOpen(app.engine.onOpen);

            Dex("#JahiaGxtImagePopup").onOpen(app.imagePreview.onOpen);


            Dex(".x-dd-drag-proxy").onOpen(app.edit.sidepanel.onStartDrag);

            Dex(".x-dd-drag-proxy").onClose(app.edit.sidepanel.onStopDrag);

            Dex("body").onAttr("data-selection-count", app.iframe.onSelect);

            Dex("body").onAttr("data-main-node-displayname", app.iframe.onChange);

            Dex(".window-iframe").onAttr("src", app.iframe.onChangeSRC);

            Dex(".x-jahia-root").onAttr("class", app.onChange);

            Dex("body").onAttr("data-sitesettings", app.edit.settings.onChange);

            Dex("#JahiaGxtContentPickerWindow").onClose(app.picker.onClose);

            Dex("#JahiaGxtEnginePanel").onClose(app.engine.onClose);

            Dex("#JahiaGxtImagePopup").onClose(app.imagePreview.onClose);

            Dex(".workflow-dashboard-engine").onOpen(app.workflow.dashboard.onOpen)

            // BROWSER LISTENERS
            window.onresize = app.onResize;

            // JQUERY EVENT LISTENERS
            $(window).on("blur", app.onBlur);

            $("body")
				.on("mousedown", ".x-tree3-node-joint", function(){
					$(this).closest(".x-grid3-row").toggleClass("indigo-opened");
				})
                .on("click", ".app-container", app.onClick)
                .on("click", ".toolbar-item-filepreview", app.picker.previewButton.onClick)
                .on("mouseenter", ".toolbar-item-filepreview", app.picker.previewButton.onMouseOver)
                .on("mouseleave", ".toolbar-item-filepreview", app.picker.previewButton.onMouseOut)
                .on("click", "#JahiaGxtManagerLeftTree + div .x-grid3 .x-grid3-row", app.picker.row.onClick)
				.on("click", ".x-viewport-adminmode .x-grid3 .x-grid3-row", function(){
					$(".x-viewport-adminmode .x-grid3 .x-grid3-row.x-grid3-row-selected").removeClass("x-grid3-row-selected");

					$(this).addClass("x-grid3-row-selected");
				})
                .on("click", ".x-grid3-row .x-grid3-td-size", app.picker.search.onContext)
                .on("click", ".x-grid3-row .x-tree3-el", app.picker.row.onContext)
                .on("click", "#JahiaGxtManagerLeftTree + div .thumb-wrap .thumb", app.picker.thumb.onContext)
                .on("click", "#JahiaGxtManagerLeftTree + div .thumb-wrap", app.picker.thumb.onClick)
                .on("click", ".x-viewport-editmode .x-toolbar-first > table", app.theme.onToggle)
                .on("click", ".editmode-managers-menu", app.contextMenus.managerMenu.onOpen)
                .on("click", ".menu-editmode-managers-menu", app.contextMenus.managerMenu.onClose)
                .on("mousedown", ".menu-edit-menu-mode, .menu-edit-menu-user", app.contextMenus.managerMenu.onClose)
                .on("click", "#JahiaGxtSidePanelTabs > .x-tab-panel-header .x-tab-strip-spacer", app.edit.settings.close)
                .on("click", "#JahiaGxtSidePanelTabs .x-grid3-td-displayName", app.edit.sidepanel.row.onContext)
                .on("click", "#JahiaGxtContentPickerWindow", app.picker.onClick)
                .on("click", "#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview", app.picker.onListView)
                .on("click", "#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-thumbsview", app.picker.onThumbView)
                .on("click", ".x-current-page-path", app.iframe.clearSelection)
                .on("click", ".x-viewport-editmode #JahiaGxtSidePanelTabs .x-grid3-row", app.edit.onNav)
                .on("mousedown", "#JahiaGxtManagerLeftTree__CRTbrowseTabItem", app.picker.search.close)
                .on("mousedown", "#JahiaGxtManagerLeftTree__CRTsearchTabItem", app.picker.search.open)
                .on("click", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header", app.picker.source.close)
                .on("click", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.toggle)
                .on("mouseenter", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.onMouseOver)
                .on("mouseleave", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.onMouseOut)
                .on("mouseenter", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div .x-grid3-row", app.picker.row.onMouseOver)
                .on("mouseenter", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div .thumb-wrap", app.picker.thumb.onMouseOver)
                .on("mouseup", "#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab", app.edit.sidepanel.tab.onClick)
                .on("mouseup", "#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", function(){
					app.edit.settings.open(true);
				});
        }
    }



    // INITIALISE
    var init = function(){
        // Copy Anthracite CSS to remove / add when dropping in and out of STUDIO mode
        app.theme.data.storedCSS = $('link[rel=stylesheet][href$="edit_en.css"]').clone();

		app.data.body = window.document.body;

        // Initialise Dex Observer
        Dex.init();

        eventListeners.attach();
    }

    $(document).ready(function(){
        init();
    });


    // EXPOSED
    if(exposeAs){
        window[exposeAs] = app;
    }

})("DX");
