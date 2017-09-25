(function(exposeAs){
    var Dex = function(selector, nodes){

        // Return Dex object
        return new Dex.fn.init(selector, nodes);

    };

    Dex.fn = Dex.prototype = {
		init: function(selector, nodes){
            this.selector = selector;

			if(nodes){
				this.nodes = nodes;
			} else {
				this.nodes = document.querySelectorAll(selector);
			}

            return this;
        }


    };

    // CONSTANTS
    var UNDEFINED = "undefined";

    // HIDDEN VARIABLES
    var cachedSelections = {},
		observers = {},
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
                            Dex(removedID).triggerObserverCallBack({
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
                            Dex(selector).triggerObserverCallBack({
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
                                Dex(selector).triggerObserverCallBack({
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
                                Dex(selector).triggerObserverCallBack({
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

		first: function(){
			/* Remove all nodes except first from node list */

			this.nodes = [this.nodes[0]];

			return this;
		},

		index: function(index){
			/* Remove all nodes except requested index */

			this.nodes = [this.nodes[index]];

			return this;
		},

		getNode: function(index){
			/* Return DOM node */

			return this.nodes[index];
		},

        exists: function(){
			/* See if the node list contains at least one node */

            return this.nodes[0] != null;
        },

		filter: function(selector){
			/* Filter then current node list - note: only filters on the first node in list */

			this.nodes = this.nodes[0].querySelectorAll(selector);

			return this;

		},

        // DOM modification
        setHTML: function(value){
			/* Set innerHTML of all nodes in nodelist */

            var n;

            for(n = 0; n < this.nodes.length; n++){
                this.nodes[n].innerHTML = value;
            }

			return this;
        },

        getHTML: function(value){
			/* Get innerHTML of all first node in nodelist */

            return this.nodes[0].innerHTML;
        },

		css: function(styles){
			/* Set CSS of all nodes in nodelist */

			var style,
				n;

			for(n = 0; n < this.nodes.length; n++){

				for(style in styles){

					this.nodes[n].style[style] = styles[style];

				}

			}

			return this;



		},

		setAttribute: function(key, value){
			/* Set attribute of all nodes in nodelist */

            var n;

            for(n = 0; n < this.nodes.length; n++){
                this.nodes[n].setAttribute(key, value);
            }

			return this;
        },

		getAttribute: function(key){
			/* Get attribute of first node in nodelist */

            return this.nodes[0].getAttribute(key);
        },

		cache: function(cacheID){
			/* Cache node list with cacheID as ID */

			cachedSelections[cacheID] = this;
		},

		// Classes
        addClass: function(classname){
			/* Add class to all nodes in nodelist */

            var n;

            for(n = 0; n < this.nodes.length; n++){
                this.nodes[n].classList.add(classname);
            }

			return this;
        },

        removeClass: function(classname){
			/* Remove class from all nodes in nodelist */

            var n;

            for(n = 0; n < this.nodes.length; n++){
                this.nodes[n].classList.remove(classname);
            }
			return this;
        },

		hasClass: function(classname){
			/* Check whether first node in list contains a classname */

			var result = false;

			if(this.nodes[0]){
				result = this.nodes[0].classList.contains(classname);
			}

			return result;

		},

		toggleClass: function(classname){
			/* Toggle classnames on all nodes in nodelist */

			var n;

            for(n = 0; n < this.nodes.length; n++){
                this.nodes[n].classList.toggle(classname);
            }

			return this;
		},

		replaceClass: function(old_classname, new_classname){
			/* Replace old_classname with new_classname on all nodes in nodelist */

			var n;

            for(n = 0; n < this.nodes.length; n++){
                this.nodes[n].classList.replace(old_classname, new_classname);
            }

			return this;
		},

        trigger: function(eventType){
			/* Trigger eventType (click, mouseover, etc, ...) on all nodes in nodelist */

            if(this.nodes[0]){
                var clickEvent = document.createEvent("MouseEvents");

                clickEvent.initEvent(eventType, true, true);
                this.nodes[0].dispatchEvent(clickEvent);
            }

			return this;
        },

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
        triggerObserverCallBack: function(params){
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

	Dex.getCached = function(cacheID){
		return cachedSelections[cacheID];
	}

	Dex.clearCache = function(cacheID){
		delete cachedSelections[cacheID];

	}

	Dex.tag = function(tag){
		/* Use Tag() to select nodes using the getElementsByTagName */

		var nodes = document.getElementsByTagName(tag);

		return Dex(tag, nodes);

	}

	Dex.class = function(classname){
		/* Use Tag() to select nodes using the getElementsByClassName */

		var nodes = document.getElementsByClassName(classname);

		return Dex("." + classname, nodes);

	}

	Dex.id = function(id){
		/* Use Tag() to select nodes using the getElementById */

		var nodes = [document.getElementById(id)];

		return Dex("#" + id, nodes);

	}

	Dex.node = function(node){
		/* Use Node to create a Dex object with a DOM node directly */

		return Dex("node", [node]);

	}

	Dex.collection = function(nodeCollection){
		/* Use Node to create a Dex object with an HTML Node Collection  directly */

		var nodes = [];

		for(n = 0; n < nodeCollection.length; n++){
			nodes.push(nodeCollection[n]);
		}

		return Dex("node", nodes)
	}

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
			currentApp: null,
			previousModeClass: null,
			UILanguage: null
		},
		dev: {
			data: {
				on: false
			},
			log: function(message, save){
				if(app.dev.data.on){
					console.log(message);
				}
			}
		},
		onChange: function(mode){

			if(app.data.previousModeClass == mode){
				return false;
			}

			app.data.previousModeClass = mode;

			app.dev.log("::: APP ::: ONCHANGE");
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
			app.dev.log("::: APP ::: ONRESIZE");
			if(app.data.currentApp == "edit"){
				app.edit.topbar.reposition();
			}

			if(app.data.currentApp == "contribute"){
				app.contribute.topbar.reposition();
			}
		},
		onBlur: function(){
			app.dev.log("::: APP ::: ONBLUR");
			// Window has lost focus, so presume that the user has clicked in the iframe.
            // If the side panel is open, then close it
            if(Dex.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open"){
                app.edit.sidepanel.close();
            }
		},
		onClick: function(e){

			if(Dex.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open"){
				var inSidePanel = $(e.target).closest("#JahiaGxtSidePanelTabs, .edit-menu-sites, .window-side-panel #JahiaGxtRefreshSidePanelButton");

		        if(inSidePanel.length == 0){
					app.dev.log("::: APP ::: ONCLICK");
	                app.edit.sidepanel.close();
	            }
			}


		},
		switch: function(appID){
			if(app.data.currentApp == appID){
				return false;

			}
			app.dev.log("::: APP ::: SWITCH: " + appID);
			app.data.currentApp = appID;

			app[appID].onOpen();

		},
		contextMenus: {
            setTitle: function(contextmenu, params){
				app.dev.log("::: APP ::: CONTEXTMENUS ::: SETTITLE");
                var contextMenuList = contextmenu.getElementsByClassName("x-menu-list")[0],
                    contextMenuTitle;

                if(contextMenuList){
                    switch(app.iframe.data.selectionCount){
                        case 0:
                            // Page
                            contextMenuTitle = params.noSelection;
                            break;

                        case 1:
                            // Selected Item
                            contextMenuTitle = params.singleSelection.replace("{{node}}", Dex.getCached("body").getAttribute("data-singleselection-node-displayname"));
                            break;

                        default:
                            // Multiple selection
                            contextMenuTitle = params.multipleSelection;
                            break;
                    }

                    contextMenuList.setAttribute("data-indigo-title", contextMenuTitle)
                }




            },
			managerMenu: {
				onOpen: function(contextmenu){
					app.dev.log("::: APP ::: CONTEXTMENUS ::: MANAGERMENU ::: ONOPEN");
                    var returnText;

                    app.dev.log("app.data.currentApp: " + app.data.currentApp);

                    switch(app.data.currentApp){
                        case "edit":
                            returnText = "Edit (" + app.iframe.data.displayName + ")";
                            break;

                        case "admin":
                            returnText = "Administration";
                            break;

                        case "dashboard":
                            returnText = "My Dashboard";
                            break;

                        case "contribute":
						returnText = "Contribute (" + app.iframe.data.displayName + ")";
                            break;

                        default:
                            returnText = "Back";

                            break;
                    }
                    contextmenu.setAttribute("data-indigo-title", returnText);


                },
				onClose: function(){
					app.dev.log("::: APP ::: CONTEXTMENUS ::: MANAGERMENU ::: ONCLOSE");
					// Manager Menu has been closed by clicking on the X.
		            // Can not remove the actual DOM node as it causes problems with GWT, so just hide it instead.
		            $(".menu-editmode-managers-menu").fadeOut();
				}
			},
            previewMenu: {
                onOpen: function(contextmenu){
					app.dev.log("::: APP ::: CONTEXTMENUS ::: PREVIEWMENU ::: ONOPEN");
                    app.contextMenus.setTitle(contextmenu, {
                        noSelection: "Page Preview",
                        singleSelection: "Preview {{node}}",
                        multipleSelection: "Preview selection"
                    });

                }
            },
            publicationMenu: {
                onOpen: function(contextmenu){
					app.dev.log("::: APP ::: CONTEXTMENUS ::: PUBLICATIONMENU ::: ONOPEN");
                    app.contextMenus.setTitle(contextmenu, {
                        noSelection: "Publish Page",
                        singleSelection: "Publish {{node}}",
                        multipleSelection: "Publish selection"
                    });

                }
            },
            moreInfoMenu: {
                onOpen: function(contextmenu){
					app.dev.log("::: APP ::: CONTEXTMENUS ::: MOREINFOMENU ::: ONOPEN");
                    app.contextMenus.setTitle(contextmenu, {
                        noSelection: "Page Options",
                        singleSelection: "{{node}} Options",
                        multipleSelection: "Selection Options"
                    });

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
				app.dev.log("::: APP ::: THEME ::: ONTOGGLE");
				// Toggle the UI Theme by changing the body attribute accordingly.

	            /* The button firing this event is actually a pseudo element atached to a table.
	            // The tables CSS has been set to ignore all pointer events EXCEPT the pseudo element who accepts pointer events.
	            // This allows us to capture a click on the pseudo element, but we have to check that it a child of the table want the one that was clicked */
				if(Dex.node(e.target).hasClass("x-toolbar-ct")){
					app.dev.log("CLICKED THEME BUTTON");
					if(app.theme.data.skin == "dark"){
						app.theme.data.skin = "light";
					} else {
						app.theme.data.skin = "dark";
					}

					Dex.tag("body").setAttribute("data-INDIGO-UI", app.theme.data.skin);

	            }
			},
			on: function(changeSkin){
				app.dev.log("::: APP ::: THEME ::: ON");
				if(changeSkin){
					app.theme.data.skin = changeSkin;
				}

				if(!app.theme.data.enabled){
					// Anthracite CSS has been removed, so plug it back in
                    $("head").append(app.theme.data.storedCSS);

					app.theme.data.enabled = true;
				}
			},
			off: function(){
				app.dev.log("::: APP ::: THEME ::: OFF");
				if(app.theme.data.enabled){
					// Remove Anthracite CSS style sheet
					$('link[rel=stylesheet][href$="' + app.theme.data.cssReference + '"]').remove();

	               // Register the fact that it has been removed
	               app.theme.data.enabled = false;
			   } else {
				   app.dev.log("DO NOT REMOVE");
			   }
			},
		},
		picker: {
			data: {
				currentItem: null,
				title: null
			},
			onOpen: function(){
				app.dev.log("::: APP ::: PICKER ::: ONOPEN");
				Dex.getCached("body")
					.setAttribute("data-INDIGO-PICKER-SEARCH", "")
					.setAttribute("data-INDIGO-PICKER", "open")
					.setAttribute("indigo-PICKER-DISPLAY", "thumbs");
			},
			onClose: function(){
				app.dev.log("::: APP ::: PICKER ::: ONCLOSE");
				Dex.getCached("body").setAttribute("data-INDIGO-PICKER", "");

			},
			onClick: function(){
				app.dev.log("::: APP ::: PICKER ::: ONCLICK");
				Dex.getCached("body").setAttribute("data-INDIGO-PICKER-SOURCE-PANEL", "");

			},
			onListView: function(){
				app.dev.log("::: APP ::: PICKER ::: ONLISTVIEW");
				Dex.getCached("body").setAttribute("indigo-PICKER-DISPLAY", "list");

			},
			onThumbView: function(){
				app.dev.log("::: APP ::: PICKER ::: ONTHUMBVIEW");
				Dex.getCached("body").setAttribute("indigo-PICKER-DISPLAY", "thumbs");

			},
			row: {
				onClick: function(){
					app.dev.log("::: APP ::: PICKER ::: ROW ::: ONCLICK");
					Dex.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

				},
				onMouseOver: function(e){
					app.dev.log("::: APP ::: PICKER ::: ROW ::: ONMOUSEOVER");
					// Position the preview button next to the file whilst hovering
		            app.picker.previewButton.reposition(e, {
		                left: -58,
		                top: 0
		            });

					app.picker.data.currentItem = Dex.node(this).getNode(0);
					app.picker.data.title = Dex.node(this).filter(".x-grid3-col-name").getHTML();

					if(Dex.node(this).hasClass("x-grid3-row-selected")){
						Dex.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

		            } else {
						Dex.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "unselected");

		            }

					Dex.class("toolbar-item-filepreview").setAttribute("indigo-preview-button", "show");
				},
				onContext: function(e){
					app.dev.log("::: APP ::: PICKER ::: ROW ::: ONCONTEXT");
					// Open Context Menu when clicking "More" button.
					// if matchClass is passed, then the click is ONLY accepted if the clicked element has that class.
					// if matchClass is not passed then it is accepted.
					var acceptClick = Dex.node(e.target).hasClass("x-tree3-el");

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
					app.dev.log("::: APP ::: PICKER ::: THUMB ::: ONCLICK");
					Dex.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

				},
				onMouseOver: function(e){
					app.dev.log("::: APP ::: PICKER ::: THUMB ::: ONMOUSEOVER");
					// Position the preview button next to the file whilst hovering
		            app.picker.previewButton.reposition(e, {
		                left: -52,
		                top: 0
		            });

					app.picker.data.currentItem = Dex.node(this).getNode(0);
		            app.picker.data.title = Dex.node(this).getAttribute("id");

		            if(Dex.node(this).hasClass("x-view-item-sel")){
		                Dex.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

		            } else {
		                Dex.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "unselected");

		            }

		            Dex.class("toolbar-item-filepreview").setAttribute("indigo-preview-button", "show");
				},
				onContext: function(e){
					app.dev.log("::: APP ::: PICKER ::: THUMB ::: ONCONTEXT");
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
					app.dev.log("::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONMOUSEOVER");
					Dex.node(app.picker.data.currentItem)
		                .addClass("x-view-over")
		                .addClass("x-grid3-row-over");
				},
				onMouseOut: function(){
					app.dev.log("::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONMOUSEOUT");
					Dex.node(app.picker.data.currentItem)
		                .removeClass("x-view-over")
		                .removeClass("x-grid3-row-over");
				},
				onClick: function(e, secondClick){
					app.dev.log("::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONCLICK");
					Dex.node(app.picker.data.currentItem)
						.trigger("mousedown")
						.trigger("mouseup");

		            if(!secondClick){
		                $("#JahiaGxtImagePopup").remove(); // remove OLD preview
		                $(this).trigger("click", [true]); // Reopen with new preview
		                Dex("#JahiaGxtImagePopup .x-window-bwrap").setAttribute("data-file-name", app.picker.data.title);

		            }

		            Dex.class("toolbar-item-filepreview").setAttribute("indigo-preview-button", "hide");
				},
				reposition: function(e, offset){
					app.dev.log("::: APP ::: PICKER ::: PREVIEWBUTTON ::: REPOSITION");
					var offset = offset || {
		                    left: 0,
		                    top: 0
		                },
		                file = Dex.node(e.currentTarget),
		                box = file.getNode(0).getBoundingClientRect(),
		                left = box.left,
		                top = box.top,
		                width = box.width;


		            Dex("#JahiaGxtManagerToolbar .toolbar-item-filepreview")
		                .css({
		                    top: (top + (offset.top)) + "px",
		                    left: ((left + width) + offset.left + 5) + "px"
		                })
		                .addClass("indigo-show-button");

				},
			},

			source: {
				onChange: function(){},
				onMouseOver: function(){
					app.dev.log("::: APP ::: PICKER ::: SOURCE ::: ONMOUSEOVER");
					// USER HAS ROLLED OVER THE COMBO TRIGGER
		            if(Dex.getCached("body").getAttribute("data-indigo-picker-source-panel") != "open"){
		                Dex("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").addClass("indigo-hover");
		            }
				},
				onMouseOut: function(){
					app.dev.log("::: APP ::: PICKER ::: SOURCE ::: ONMOUSEOUT");
					// USER HAS ROLLED OUT OF THE COMBO TRIGGER
		            Dex("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");
				},
				close: function(){
					app.dev.log("::: APP ::: PICKER ::: SOURCE ::: CLOSE");
					// CHANGE SOURCE
		            // The user has changed SOURCE, so we just need to hide the combo...
		            Dex.getCached("body").setAttribute("data-INDIGO-PICKER-SOURCE-PANEL", "");
				},
				open: function(){},
				toggle: function(e){
					app.dev.log("::: APP ::: PICKER ::: SOURCE ::: TOGGLE");
					// USER HAS CLICKED THE COMBO TRIGGER
		            e.stopPropagation();

		            Dex("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");

		            /// Toggle the attribute in body tag
		            $("body").attr("data-INDIGO-PICKER-SOURCE-PANEL", function(id, label){
		                return (label == "open") ? "" : "open";
		            });
				}
			},
			search: {
				open: function(){
					app.dev.log("::: APP ::: PICKER ::: SEARCH ::: OPEN");
					// OPEN SEARCH PANEL

		            // Close source picker if open
		            app.picker.onClose();


		            // Display the search panel
		            Dex.getCached("body").setAttribute("data-INDIGO-PICKER-SEARCH", "open");

		            // Put the results in LIST mode
		            Dex("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview").trigger("click");

		            // Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
		            Dex.id("CRTbrowseTabItem").addClass("x-hide-display");


		            // Remove the directory listing ( gives the search panel an empty start)
		            setTimeout(function(){
		                $("#JahiaGxtManagerTobTable .x-grid3 .x-grid3-row").remove();
		            }, 250);
				},
				close: function(){
					app.dev.log("::: APP ::: PICKER ::: SEARCH ::: CLOSE");
					// CLOSE SEARCH PANEL

		            // Hide the search panel
		            Dex.getCached("body").setAttribute("data-INDIGO-PICKER-SEARCH", "");

		            // Display the BROWSE panels
		            Dex("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)").removeClass("x-hide-display");

		            // Get the refresh button
		            var refreshButton = $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel").not(".x-panel-collapsed").find(".x-tool-refresh")[0];

		            // CLick on the refresh button to reload the content of the directory
					Dex.node(refreshButton).trigger("click");

				},

				onContext: function(e){
					app.dev.log("::: APP ::: PICKER ::: SEARCH ::: ONCONTEXT");
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
				app.dev.log("::: APP ::: PICKER ::: IMAGEPREVIEW ::: ONOPEN");
				Dex.getCached("body").setAttribute("data-INDIGO-IMAGE-PREVIEW", "open");

				// Attribute used to display the friendly name in edit panel
				Dex(".engine-panel > div.x-panel-header .x-panel-header-text").setAttribute("data-friendly-name", "nodeDisplayName");
			},
			onClose: function(){
				app.dev.log("::: APP ::: PICKER ::: IMAGEPREVIEW ::: ONCLOSE");
				Dex.getCached("body").setAttribute("data-INDIGO-IMAGE-PREVIEW", "");

			}
		},
		engine: {
			onOpen: function(){
				app.dev.log("::: APP ::: ENGINE ::: ONOPEN");
				var nodeDisplayName = Dex.getCached("body").getAttribute("data-singleselection-node-displayname");

				Dex.getCached("body").setAttribute("data-INDIGO-EDIT-ENGINE", "open");

				// Attribute used to display the friendly name in edit panel
				Dex(".engine-panel > div.x-panel-header .x-panel-header-text").setAttribute("data-friendly-name", nodeDisplayName);
			},
			onClose: function(){
				app.dev.log("::: APP ::: ENGINE ::: ONCLOSE");
				app.iframe.clearSelection();
				Dex.getCached("body").setAttribute("data-INDIGO-EDIT-ENGINE", "");

			},
		},
		workflow: {
			dashboard: {
				onOpen: function(){
					app.dev.log("::: APP ::: WORKFLOW ::: DASHBOARD ::: ONOPEN");
					Dex(".workflow-dashboard-engine .x-tool-maximize").trigger("click");

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
			onChangeSRC: function(url){
				app.dev.log("::: APP ::: IFRAME ::: ONCHANGESRC [src='" + url + "' ::: currentApp='" + app.data.currentApp + "']");

				app.iframe.data.previousUrl = app.iframe.data.currentUrl;
				app.iframe.data.currentUrl = url;

				if(app.data.currentApp == "edit"){
					// TEMP BLIND
	                // $(".window-iframe").hide();

	                var elements = {
	                    iframe: document.getElementsByClassName("window-iframe")[0],
	                    title: document.getElementsByClassName("x-current-page-path")[0],
	                    // publishButton: document.getElementsByClassName("edit-menu-publication")[0],
	                    // refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
	                    previewButton: document.getElementsByClassName("edit-menu-view")[0],
	                    moreInfo: document.getElementsByClassName("edit-menu-edit")[0],
	                };

	                if( elements.iframe &&
	                    elements.iframe.style){
	                        elements.iframe.style.opacity = 0;

	                }

	                if( elements.title &&
	                    elements.title.style){
	                        elements.title.style.opacity = 0;

	                }

	                // if( elements.publishButton &&
	                //     elements.publishButton.style){
	                //         elements.publishButton.style.opacity = 0;
                    //
	                // }

	                // if( elements.refreshButton &&
	                //     elements.refreshButton.style){
	                //         elements.refreshButton.style.opacity = 0;
                    //
	                // }

	                if( elements.previewButton &&
	                    elements.previewButton.style){
	                        elements.previewButton.style.opacity = 0;

	                }

	                if( elements.moreInfo &&
	                    elements.moreInfo.style){
	                        elements.moreInfo.style.opacity = 0;

	                }
				} else if(app.data.currentApp == "contribute"){

                } else if(app.data.currentApp == "admin" || app.data.currentApp == "dashboard"){

				}









			},
			onChange: function(value){





				if(app.iframe.data.displayName == value || app.data.currentApp == "studio"){
					return false;
				}
				app.dev.log("::: APP ::: IFRAME ::: ONCHANGE: " + app.data.currentApp);

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
				var count = parseInt(value);

				app.dev.log("::: APP ::: IFRAME ::: ONSELECT [value='" + value + "']");

				// Refresh the title of the page accordingly
				switch(app.data.currentApp){
					case "edit":
						app.iframe.data.selectionCount = count;
						// Need to update the header bar
						app.edit.topbar.build();

						if(app.edit.sidepanel.isOpen()){
							app.iframe.disableClicks();
						}

						break;

					case "contribute":
						app.iframe.data.selectionCount = count;
						// Need to update the header bar
						app.contribute.topbar.build();

						break;
				}


				// MOVE TO A DEX LISTENER ON MENU ITEM
				// setTimeout(function(){
				// 	$(".editModeContextMenu .x-menu-list").attr("data-selected-name", Dex.getCached("body").getAttribute("data-singleselection-node-displayname"));
				// }, 50);
			},

			clearSelection: function(){
				app.dev.log("::: APP ::: IFRAME ::: CLEARSELECTION");

				Dex.class("window-iframe").trigger("click");

			},

			disableClicks: function(){

				if(Dex.getCached("body").getAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL") == "yes" && Dex.getCached("body").getAttribute("data-sitesettings") == "false"){
					app.dev.log("::: APP ::: IFRAME ::: DISABLECLICKS");
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
				app.dev.log("::: APP ::: ADMIN ::: OPENED");

				var systemSettingsTabs = document.querySelectorAll(".tab_systemSiteSettings")[0],
                    serverSettingsTabs = document.querySelectorAll(".tab_serverSettings")[0];

                if(systemSettingsTabs){
                    if(window.getComputedStyle(systemSettingsTabs).display == "none"){
                        // System Settings Tabs have not been loaded, so trigger click to open them
                        mouse.trigger(document.querySelectorAll("#JahiaGxtSidePanelTabs li")[1], "click");
                    }

                } else {
                    mouse.trigger(document.querySelectorAll("#JahiaGxtSidePanelTabs li")[1], "click");

                }

                if(serverSettingsTabs){
                    if(window.getComputedStyle(serverSettingsTabs).display == "none"){
                        // Server Settings Tabs have not been loaded, so trigger click to open them
                        mouse.trigger(document.querySelectorAll("#JahiaGxtSidePanelTabs li")[0], "click");
                    }

                } else {
                    mouse.trigger(document.querySelectorAll("#JahiaGxtSidePanelTabs li")[0], "click");

                }

                // Use Anthracite CSS
				app.theme.on();

                // Set attributes to be used by CSS
                Dex.getCached("body")
					.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "no")
                	.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");
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
				app.dev.log("::: APP ::: EDIT ::: ONOPEN");

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
                Dex.getCached("body")
					.setAttribute("data-edit-window-style", "default")
                	.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "")
                	.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "yes");
			},
			onClose: function(){},

			onNav: function(){
				app.dev.log("::: APP ::: EDIT ::: ONNAV");

				if(app.edit.settings.data.opened){
					// CLicked on a settings page
					app.edit.sidepanel.data.firstRun = false;

                    app.dev.log(["ONNAV ::: ", this]);
                    if(this.classList.contains("unselectable-row")){
                        app.dev.log("DO NOT REMEMBER THIS PAGE IN HISTORY AS IT IS A FOLDER");
                    } else {
                        app.edit.history.add("settingspage", this);

                    }

				} else {
					app.edit.history.add("editpage", this);

				}

			},

			infoBar: {
                data: {
                    on: false,
                    taskCount: 0
                },
                toggle: function(){
					app.dev.log("::: APP ::: EDIT ::: INFOBAR ::: TOGGLE");
                    app.edit.infoBar.data.on = !app.edit.infoBar.data.on;

                    Dex.getCached("body").setAttribute("data-indigo-infoBar", app.edit.infoBar.data.on);

                },
                tasks: {
					data: {
						classes: null,
						taskCount: 0,
						dashboardButtonLabel: null
					},
                    onChange: function(classes){

						if(app.edit.infoBar.tasks.data.classes == classes){
							return false;
						}

						app.edit.infoBar.tasks.data.classes = classes;

						app.dev.log("::: APP ::: EDIT ::: INFOBAR ::: TASKS ::: ONCHANGE");

                        var taskButton = Dex("." + app.data.currentApp + "-menu-tasks button");;




                        if(taskButton.exists()){
                            var taskString = taskButton.getHTML(),
                            	regexp = /\(([^)]+)\)/,
                                result = taskString.match(regexp),
								taskCount,
								workflowButtonLabel,
								dashboardButtonLabel,
								dashboardButton = Dex.class("menu-edit-menu-workflow");


                            if(result){
								taskCount = parseInt(result[1]);

                            } else {
                                taskCount = 0;
                            }

							switch(taskCount){
								case 0:
									dashboardButtonLabel = "Open Dashboard";

									break;
								case 1:
									dashboardButtonLabel = "Dashboard (" + taskCount + " task)";

									break;
								default:
									dashboardButtonLabel = "Dashboard (" + taskCount + " tasks)";

									break;
							}

							if(taskCount > 9){
								workflowButtonLabel = "+9";

							} else {
								workflowButtonLabel = taskCount;
							}

                            Dex(".edit-menu-workflow").setAttribute("data-info-count", workflowButtonLabel);
							Dex(".contribute-menu-workflow").setAttribute("data-info-count", workflowButtonLabel);



							if(dashboardButton.exists()){
								dashboardButton.filter(".toolbar-item-workflowdashboard").setHTML(dashboardButtonLabel);
							}

							app.edit.infoBar.data.taskCount = taskCount;
							app.edit.infoBar.data.workflowButtonLabel = workflowButtonLabel;
							app.edit.infoBar.data.dashboardButtonLabel = dashboardButtonLabel;



                        }
                    },
					updateMenuLabel: function(node){
						Dex.node(node)
							.filter(".toolbar-item-workflowdashboard")
								.setHTML(app.edit.infoBar.data.dashboardButtonLabel);

					}
                },
                jobs: {
					data: {
						classes: null,
						jobString: null
					},
                    onChange: function(classes){
						// if(app.edit.infoBar.jobs.data.classes == classes){
						// 	app.dev.log("No change in job classes - ignore");
						// 	return false;
						// }

						app.edit.infoBar.jobs.data.classes = classes;

						app.dev.log("::: APP ::: EDIT ::: INFOBAR ::: JOBS ::: ONCHANGE");

                        var jobButton = Dex(".toolbar-item-workinprogressadmin button");

                        if(jobButton.exists()){
                            var jobStringSplit = jobButton.getHTML().split("<"),
                            	jobString = jobStringSplit[0],
                            	jobIcon = jobButton.filter("img"),
								activeJob,
								buttonParent = Dex.class("toolbar-item-workinprogressadmin"),
								jobTooltip;

                            if(jobIcon.getAttribute("src").indexOf("workInProgress.png") == -1){
                                // A job is active
								activeJob = true;
								jobTooltip = jobString;
								Dex(".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(10)").addClass("indigo-job-running");

                            } else {
                                // No Jobs active
								activeJob = false;

								if(app.data.UILanguage == "FR"){
									jobTooltip = "Processus";

								} else {
									jobTooltip = "Jobs";


								}
								Dex(".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(10)").removeClass("indigo-job-running");

                            }

							app.edit.infoBar.jobs.data.jobString = jobString;
							app.edit.infoBar.jobs.data.activeJob = activeJob;

							buttonParent.setAttribute("data-indigo-label", jobTooltip);


                        }

                    }
                },
                publicationStatus: {
                    onChange: function(){
						app.dev.log("::: APP ::: EDIT ::: INFOBAR ::: PUVLICATIONSTATUS ::: ONCHANGE");
                    }
                }
            },

			// Controls
			history: {
				data: {},
				add: function(type, node){
					app.dev.log("::: APP ::: EDIT ::: HISTORY ::: ADD");
					app.edit.history.data[type] = node;

				},
				get: function(type){
					app.dev.log("::: APP ::: EDIT ::: HISTORY ::: GET");

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
					app.dev.log("::: APP ::: EDIT ::: HISTORY ::: RESET");
					app.edit.history.data = {
						settingspage: null,
						editpage: null,
					};
				},
			},

			topbar: {
				build: function(){
					app.dev.log("::: APP ::: EDIT ::: TOPBAR ::: BUILD");

                    // TEMP BLIND
                    // $(".window-iframe").fadeIn("fast");
					if(app.data.currentApp == "edit" || app.data.currentApp == "contribute"){
						var elements = {
	                        iframe: document.getElementsByClassName("window-iframe")[0],
	                        title: document.getElementsByClassName("x-current-page-path")[0],
	                        // publishButton: document.getElementsByClassName("edit-menu-publication")[0],
	                        // refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                            // nodePathTitle: document.getElementsByClassName("node-path-title")[0],
	                        previewButton: document.getElementsByClassName("edit-menu-view")[0],
	                        moreInfo: document.getElementsByClassName("edit-menu-edit")[0],
	                    };


	                    if( elements.iframe &&
	                        elements.iframe.style){
	                            elements.iframe.style.opacity = 1;

	                    }

	                    if( elements.title &&
	                        elements.title.style){
	                            elements.title.style.opacity = 1;

	                    }

                        // if( elements.publishButton &&
	                    //     elements.publishButton.style){
	                    //         elements.publishButton.style.opacity = 1;
                        //
	                    // }

                        // if( elements.nodePathTitle &&
	                    //     elements.nodePathTitle.style){
	                    //         elements.nodePathTitle.style.opacity = 1;
                        //
	                    // }

	                    // if( elements.refreshButton &&
	                    //     elements.refreshButton.style){
	                    //         elements.refreshButton.style.opacity = 1;
                        //
	                    // }

	                    if( elements.previewButton &&
	                        elements.previewButton.style){
	                            elements.previewButton.style.opacity = 1;

	                    }

	                    if( elements.moreInfo &&
	                        elements.moreInfo.style){
	                            elements.moreInfo.style.opacity = 1;

	                    }




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
								pageTitle = Dex.getCached("body").getAttribute("data-singleselection-node-displayname");
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
	                    Dex.getCached("body")
							.setAttribute("data-multiselect", multiselect)
	                    	.setAttribute("data-select-type", selectType);

						// Page Title in Edit Made
                        if(pageTitle){
                            Dex.class("x-current-page-path").setAttribute("data-PAGE-NAME",pageTitle);

                        }
	                    Dex.class("node-path-text-inner").setHTML(app.iframe.data.displayName);

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

	                    app.dev.log("::: app.iframe.data.publication.status ['" + app.iframe.data.publication.status + "']");

                        app.iframe.data.pageTitle = pageTitle;

						// Page Titles need centering
						app.edit.topbar.reposition();
					}




				},
				reposition: function(e){
					app.dev.log("::: APP ::: EDIT ::: TOPBAR ::: REPOSITION");
					// Center title to page and move surrounding menus to right and left.

					if(document.getElementsByClassName("x-current-page-path").length > 0){

						if(Dex.class("x-current-page-path").getAttribute("data-page-name") != null){
                            document.getElementsByClassName("edit-menu-publication")[0].style.display = "block";

                            var elements = {
                                    body: document.getElementsByTagName("body")[0],
                                    title: document.getElementsByClassName("x-current-page-path")[0],
                                    innerTitle: document.getElementsByClassName("node-path-text-inner")[0],
                                    publishButton: document.getElementsByClassName("edit-menu-publication")[0],
                                    // refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                                    nodePathTitle: document.getElementsByClassName("node-path-title")[0],
                                    previewButton: document.getElementsByClassName("edit-menu-view")[0],
                                    moreInfo: document.getElementsByClassName("edit-menu-edit")[0],
                                },

                                boxes = {
                                    body: elements.body.getBoundingClientRect(),
                                    title: elements.title.getBoundingClientRect()
                                };


                                // Center Page Title
                                elements.title.style.left = ((boxes.body.width / 2) - (boxes.title.width / 2)) + "px";

                                if(elements.innerTitle){
                                    // Get Inner title bunding box
                                    boxes.innerTitle = elements.innerTitle.getBoundingClientRect();

                                    // Center Inner title bounding box
                                    elements.innerTitle.style.left = ((boxes.body.width / 2) - (boxes.innerTitle.width / 2)) + 5 + "px";
                                }


                                // Refresh bounding box for title as it has moved
                                boxes.title = elements.title.getBoundingClientRect();

                                if(app.iframe.data.selectionCount > 0){
                                    // Multiselect, so display differently
                                    // elements.publishButton.style.left = (boxes.title.left - 20) + "px";
                                    // elements.refreshButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                                    elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                                    elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 30) + "px";
                                    elements.nodePathTitle.style.left = (boxes.title.left - 82) + "px";
                                    Dex(".edit-menu-publication .x-btn-mc").setAttribute("data-publication-label", app.iframe.data.pageTitle);
                                } else {
                                    // No Select
                                    // elements.publishButton.style.left = (boxes.title.left - 20) + "px";
                                    // elements.refreshButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                                    elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 9) + "px";
                                    elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 33) + "px";
                                    elements.nodePathTitle.style.left = (boxes.title.left - 80) + "px";

                                    elements.nodePathTitle.setAttribute("data-indigo-file-path", Dex.getCached("body").getAttribute("data-main-node-path"));
                                    Dex(".edit-menu-publication .x-btn-mc").setAttribute("data-publication-label", app.iframe.data.publication.label);
                                }

                                // Make sure correct class is added to publication button
                                elements.publishButton.setAttribute("data-publication-status", app.iframe.data.publication.status)




                        } else {
                            document.getElementsByClassName("edit-menu-publication")[0].style.display = "none"
                        }

					}



				}
			},

			sidepanel: {
				data: {
					firstRun: true,
					firstRunPages: true,
					firstRunSettings: true,
					open: false,
					currentTab: null
				},
				onStartDrag: function(){
					app.dev.log("::: APP ::: EDIT ::: SIDEPANEL ::: ONSTARTDRAG");
					app.edit.sidepanel.close();

				},
				onStopDrag: function(){
					app.dev.log("::: APP ::: EDIT ::: SIDEPANEL ::: ONSTOPDRAG");

				},
				open: function(isSettings){
					app.dev.log("::: APP ::: EDIT ::: SIDEPANEL ::: OPEN [isSettings='" + isSettings + "']");

					Dex.getCached("body").setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");
					app.edit.sidepanel.data.open = true;

					// GWT has problems populating the site page tree when the side panel is hidden.
					// Solution: When the side panel is opened for the FIRST TIME ONLY, the refresh button is triggered and the sites page tree is populated correctly.
					if(Dex.getCached("body").getAttribute("data-sitesettings") == "false"){

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
					if(Dex.getCached("body").getAttribute("data-edit-window-style") !== "settings" && Dex.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open" && Dex.getCached("body").getAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL") == "yes"){
						app.dev.log("::: APP ::: EDIT ::: SIDEPANEL ::: CLOSE");
		                Dex.getCached("body").setAttribute("data-INDIGO-GWT-SIDE-PANEL", "");

		                // Revert iframes body style attribute to what it was originally
		                $(".window-iframe").contents().find("body").attr("style", app.iframe.data.bodyStyle);

		            }

					// app.edit.topbar.reposition();
				},

				isOpen: function(){
					return app.edit.sidepanel.data.open;
				},

				tab: {
					onClick: function(e){
						app.dev.log("APP ::: EDIT ::: SIDEPANEL ::: TAB ::: ONCLICK");

						// User has clicked on one of the side panel tabs (except for Settings Tab which calls eventHandlers.clickSidePanelSettingsTab)
			            var clickedTabID = Dex.node(this).getAttribute("id");

						app.edit.sidepanel.data.currentTab = clickedTabID;

						app.dev.log("app.edit.sidepanel.data.currentTab: " + app.edit.sidepanel.data.currentTab);

			            Dex.getCached("body").setAttribute("data-INDIGO-GWT-PANEL-TAB", clickedTabID);

			            // Menus for the Tabs that call this listener require a normal side panel display
			            // Dex.getCached("body").setAttribute("data-edit-window-style", "default");

			            var tabMenuActive = Dex.node(this).hasClass("x-tab-strip-active"),
			                sidePanelOpen = Dex.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open";

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
						app.dev.log("APP ::: EDIT ::: SIDEPANEL ::: ROW ::: ONCONTEXT");
						// Open Context Menu when clicking "More" button.
			            var acceptClick = Dex.node(e.target).hasClass("x-grid3-td-displayName");

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
							app.dev.log("APP ::: EDIT ::: SETTINGS ::: ONCHANGE");
							app.edit.settings.open();

						}


					} else {
						// alert("YO");
						// app.edit.settings.close();

					}
				},
				open: function(){
					app.dev.log("::: APP ::: EDIT ::: SETTINGS ::: OPEN");

					$(".window-iframe").contents().find("head").prepend("<style>.well{border:none!important; box-shadow: none!important;} body{background-image: none!important; background-color:#f5f5f5!important}</style>");

					if(app.edit.settings.data.opened){
						return false;
					}



					app.edit.settings.data.opened = true;
					Dex.getCached("body").setAttribute("data-edit-window-style", "settings");

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
					app.dev.log("::: APP ::: EDIT ::: SETTINGS ::: CLOSE");

					var previousEditPage = app.edit.history.get("editpage");

					app.edit.settings.data.opened = false;
					Dex.getCached("body").setAttribute("data-edit-window-style", "default");

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
				app.dev.log("::: APP ::: DASHBOARD ::: OPENED");

				// Use Anthracite CSS
				app.theme.on();

                // Set attributes to be used by CSS
                Dex.getCached("body")
					.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "no")
                	.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");
			},
			onClose: function(){},

			// Controls

		},
		studio: {
			// Event Handlers
			onOpen: function(){
				app.dev.log("::: APP ::: STUDIO ::: OPENED");

				// Dont use Anthracite CSS
				app.theme.off();

                // Set attributes to be used by CSS
                Dex.getCached("body")
					.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "")
                	.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "yes");
			},
			onClose: function(){},

			// Controls

		},
		contribute: {
			// Event Handlers
            data: {
                mode: null
            },
			onOpen: function(){
				app.dev.log("::: APP ::: CONTRIBUTE ::: OPENED");

				// Use Anthracite CSS
				app.theme.on();

				app.contribute.topbar.build();


                // Set attributes to be used by CSS
                Dex.getCached("body")
					.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "")
                	.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "yes");
			},
			onClose: function(){},

            onChangeMode: function(nodePath){

                if(app.data.currentApp != "contribute"){
                    return false;

                }


                var nodePathSplit = nodePath.split("/"),
                    modePath = nodePathSplit[3],
                    mode,

                    iframeSRC = Dex.class("window-iframe").getAttribute("src"),
                    displayingNode = iframeSRC.indexOf("viewContent.html") > -1;

                switch(modePath){
                    case "files":
                        mode = "files";
                        break;

                    case "contents":
                        mode = "content";

                        break;

                    default:
                        mode = "site";

                        break;
                }

                app.contribute.data.mode = mode;

                Dex.getCached("body").setAttribute("data-contribute-mode", app.contribute.data.mode);

                app.dev.log("????????????????????????????????????");
                app.dev.log("CHANGED nodePath: " + nodePath);
                app.dev.log("CHANGED section: " + nodePathSplit[3]);
                app.dev.log("displayingNode: " + displayingNode);
                app.dev.log("????????????????????????????????????");



                app.contribute.data.displayingNode = displayingNode;

                Dex.getCached("body").setAttribute("data-contribute-displaying-node", app.contribute.data.displayingNode);
            },

			// Controls
			topbar: {
				build: function(){

                    if(!app.contribute.data.mode){
                        app.dev.log("::: APP ::: CONTRIBUTE ::: TOPBAR ::: BUILD ( CAN NOT YET BUILD)");
                        return false;

                    }

                    app.dev.log("::: APP ::: CONTRIBUTE ::: TOPBAR ::: BUILD (MODE: " + app.contribute.data.mode + ")");

                    // TEMP BLIND
                    // $(".window-iframe").fadeIn("fast");
					if(app.data.currentApp == "edit" || app.data.currentApp == "contribute"){
						var elements = {
	                        iframe: document.getElementsByClassName("window-iframe")[0],
	                        title: document.getElementsByClassName("toolbar-item-publicationstatus")[0],
	                        // publishButton: document.getElementsByClassName("edit-menu-publication")[0],
	                        // refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                            // nodePathTitle: document.getElementsByClassName("node-path-title")[0],
	                        previewButton: document.getElementsByClassName("edit-menu-view")[0],
	                        moreInfo: document.getElementsByClassName("edit-menu-edit")[0],
	                    };


	                    if( elements.iframe &&
	                        elements.iframe.style){
	                            elements.iframe.style.opacity = 1;

	                    }

	                    if( elements.title &&
	                        elements.title.style){
	                            elements.title.style.opacity = 1;

	                    }

                        // if( elements.publishButton &&
	                    //     elements.publishButton.style){
	                    //         elements.publishButton.style.opacity = 1;
                        //
	                    // }

                        // if( elements.nodePathTitle &&
	                    //     elements.nodePathTitle.style){
	                    //         elements.nodePathTitle.style.opacity = 1;
                        //
	                    // }

	                    // if( elements.refreshButton &&
	                    //     elements.refreshButton.style){
	                    //         elements.refreshButton.style.opacity = 1;
                        //
	                    // }

	                    if( elements.previewButton &&
	                        elements.previewButton.style){
	                            elements.previewButton.style.opacity = 1;

	                    }

	                    if( elements.moreInfo &&
	                        elements.moreInfo.style){
	                            elements.moreInfo.style.opacity = 1;

	                    }


                    var pageTitle = app.iframe.data.displayName,
                        publicationStatus = publicationStatus = document.querySelectorAll(".toolbar-item-publicationstatus .gwt-Image")[0],

                        extractStatus = function(url){
                            var urlSplit = url.split("/"),
                                fileName = urlSplit[urlSplit.length-1],
                                statusSplit = fileName.split(".png"),
                                status = statusSplit[0];

                            return status
                        };


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

                    elements.title.setAttribute("data-PAGE-NAME", pageTitle);

                    app.dev.log("app.iframe.data.publication:" + app.iframe.data.publication);
                	app.contribute.topbar.reposition();

                    //
                    //
                    //
                    //
					// 	var pageTitle,
	                //         selectType = "none",
					// 		multiselect = "off",
	                //         publicationStatus = document.querySelectorAll(".toolbar-item-publicationstatuswithtext .gwt-Image")[0],
                    //
	                //         extractStatus = function(url){
	                //             var urlSplit = url.split("/"),
	                //                 fileName = urlSplit[urlSplit.length-1],
	                //                 statusSplit = fileName.split(".png"),
	                //                 status = statusSplit[0];
                    //
	                //             return status
	                //         };
                    //
					// 	// Presumably in Edit Mode or Contribute Mode, in which case we need to set the page title
					// 	switch(app.iframe.data.selectionCount){
					// 		case 0:
					// 			pageTitle = app.iframe.data.displayName;
	                //             selectType = "none";
					// 			break;
                    //
					// 		case 1:
					// 			pageTitle = Dex.getCached("body").getAttribute("data-singleselection-node-displayname");
					// 			multiselect = "on";
	                //             selectType = "single";
                    //
                    //
					// 			break;
                    //
					// 		default:
					// 			pageTitle = app.iframe.data.selectionCount + " selected items";
					// 			multiselect = "on";
	                //             selectType = "multiple";
					// 			break;
					// 	}
                    //
					// 	// Set multiselect status in body attribute...
	                //     Dex.getCached("body")
					// 		.setAttribute("data-multiselect", multiselect)
	                //     	.setAttribute("data-select-type", selectType);
                    //
					// 	// Page Title in Edit Made
                    //     if(pageTitle){
                    //         Dex.class("x-current-page-path").setAttribute("data-PAGE-NAME",pageTitle);
                    //
                    //     }
	                //     Dex.class("node-path-text-inner").setHTML(app.iframe.data.displayName);
                    //
	                //     // Determine publication status
	                //     if(publicationStatus){
	                //         app.iframe.data.publication = {
	                //             status: extractStatus(publicationStatus.getAttribute("src")),
	                //             label: publicationStatus.getAttribute("title")
	                //         };
	                //     } else {
	                //         app.iframe.data.publication = {
	                //             status: null,
	                //             label: null
	                //         };
	                //     }
                    //
	                //     app.dev.log("::: app.iframe.data.publication.status ", app.iframe.data.publication.status);
                    //
                    //     app.iframe.data.pageTitle = pageTitle;
                    //
					// 	// Page Titles need centering
					// 	app.edit.topbar.reposition();
					}




				},
				reposition: function(e){
					app.dev.log("::: APP ::: CONTRIBUTE ::: TOPBAR ::: REPOSITION");

                    if(Dex.class("toolbar-item-publicationstatus").getAttribute("data-page-name") != null){
                            var elements = {
                                body: document.getElementsByTagName("body")[0],
                                title: document.getElementsByClassName("toolbar-item-publicationstatus")[0],
                                // innerTitle: document.getElementsByClassName("node-path-text-inner")[0],
                                publishButton: document.getElementsByClassName("contribute-menu-publication")[0],
                                // refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                                // nodePathTitle: document.getElementsByClassName("node-path-title")[0],
                                previewButton: document.getElementsByClassName("edit-menu-view")[0],
                                editPage: Dex(".x-toolbar-first .x-toolbar-cell:nth-child(5) table").getNode(0),
                            },

                            boxes = {
                                body: elements.body.getBoundingClientRect(),
                                title: elements.title.getBoundingClientRect()
                            };


                            // Center Page Title
                            elements.title.style.left = ((boxes.body.width / 2) - (boxes.title.width / 2)) + "px";

                            // if(elements.innerTitle){
                            //     // Get Inner title bunding box
                            //     boxes.innerTitle = elements.innerTitle.getBoundingClientRect();
                            //
                            //     // Center Inner title bounding box
                            //     elements.innerTitle.style.left = ((boxes.body.width / 2) - (boxes.innerTitle.width / 2)) + 5 + "px";
                            // }


                            // Refresh bounding box for title as it has moved
                            boxes.title = elements.title.getBoundingClientRect();

                            // No Select
                            // elements.publishButton.style.left = (boxes.title.left - 20) + "px";
                            // elements.refreshButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                            elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 9) + "px";
                            elements.editPage.style.left = (boxes.title.left + boxes.title.width + 33) + "px";
                            // elements.nodePathTitle.style.left = (boxes.title.left - 80) + "px";

                            // elements.nodePathTitle.setAttribute("data-indigo-file-path", Dex.getCached("body").getAttribute("data-main-node-path"));
                            Dex(".contribute-menu-publication .x-btn-mc").setAttribute("data-publication-label", app.iframe.data.publication.label);


                            // Make sure correct class is added to publication button
                            elements.publishButton.setAttribute("data-publication-status", app.iframe.data.publication.status)
                    }

					// Center title to page and move surrounding menus to right and left.

					// if(document.getElementsByClassName("x-current-page-path").length > 0){
                    //
					// 	if(Dex.class("x-current-page-path").getAttribute("data-page-name") != null){
                    //         document.getElementsByClassName("edit-menu-publication")[0].style.display = "block";
                    //
                    //         var elements = {
                    //                 body: document.getElementsByTagName("body")[0],
                    //                 title: document.getElementsByClassName("x-current-page-path")[0],
                    //                 innerTitle: document.getElementsByClassName("node-path-text-inner")[0],
                    //                 publishButton: document.getElementsByClassName("edit-menu-publication")[0],
                    //                 // refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                    //                 nodePathTitle: document.getElementsByClassName("node-path-title")[0],
                    //                 previewButton: document.getElementsByClassName("edit-menu-view")[0],
                    //                 moreInfo: document.getElementsByClassName("edit-menu-edit")[0],
                    //             },
                    //
                    //             boxes = {
                    //                 body: elements.body.getBoundingClientRect(),
                    //                 title: elements.title.getBoundingClientRect()
                    //             };
                    //
                    //
                    //             // Center Page Title
                    //             elements.title.style.left = ((boxes.body.width / 2) - (boxes.title.width / 2)) + "px";
                    //
                    //             if(elements.innerTitle){
                    //                 // Get Inner title bunding box
                    //                 boxes.innerTitle = elements.innerTitle.getBoundingClientRect();
                    //
                    //                 // Center Inner title bounding box
                    //                 elements.innerTitle.style.left = ((boxes.body.width / 2) - (boxes.innerTitle.width / 2)) + 5 + "px";
                    //             }
                    //
                    //
                    //             // Refresh bounding box for title as it has moved
                    //             boxes.title = elements.title.getBoundingClientRect();
                    //
                    //             if(app.iframe.data.selectionCount > 0){
                    //                 // Multiselect, so display differently
                    //                 // elements.publishButton.style.left = (boxes.title.left - 20) + "px";
                    //                 // elements.refreshButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                    //                 elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                    //                 elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 30) + "px";
                    //                 elements.nodePathTitle.style.left = (boxes.title.left - 82) + "px";
                    //                 Dex(".edit-menu-publication .x-btn-mc").setAttribute("data-publication-label", app.iframe.data.pageTitle);
                    //             } else {
                    //                 // No Select
                    //                 // elements.publishButton.style.left = (boxes.title.left - 20) + "px";
                    //                 // elements.refreshButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                    //                 elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 9) + "px";
                    //                 elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 33) + "px";
                    //                 elements.nodePathTitle.style.left = (boxes.title.left - 80) + "px";
                    //
                    //                 elements.nodePathTitle.setAttribute("data-indigo-file-path", Dex.getCached("body").getAttribute("data-main-node-path"));
                    //                 Dex(".edit-menu-publication .x-btn-mc").setAttribute("data-publication-label", app.iframe.data.publication.label);
                    //             }
                    //
                    //             // Make sure correct class is added to publication button
                    //             elements.publishButton.setAttribute("data-publication-status", app.iframe.data.publication.status)
                    //
                    //
                    //
                    //
                    //     } else {
                    //         document.getElementsByClassName("edit-menu-publication")[0].style.display = "none"
                    //     }
                    //
					// }



				}
			}

		}
	}



    // EVENT LISTENERS
    var eventListeners = {
        attach: function(){
            // HOME BREW EVENT LISTENERS
            // Set up INDIGO listeners (listening to changes in DOM)

			Dex("#JahiaGxtContentBrowseTab").onceOpen(function(element){
				Dex.node(element).filter(".x-box-item:nth-child(2) .x-grid3-body").addClass("results-column");
			});

			Dex("#JahiaGxtFileImagesBrowseTab").onceOpen(function(element){
				Dex.node(element).filter("#images-view > div").addClass("results-column");
			});

			Dex("#JahiaGxtCategoryBrowseTab").onceOpen(function(element){
				Dex.node(element).filter(".x-box-item:nth-child(2) .x-grid3-body").addClass("results-column");
			});

			Dex("#JahiaGxtSearchTab").onceOpen(function(element){
				Dex.node(element).filter(".JahiaGxtSearchTab-results .x-grid3-body").addClass("results-column");
			});

			Dex("#JahiaGxtCreateContentTab").onceOpen(function(value){
				Dex.node(value).filter("input.x-form-text").setAttribute("placeholder", "Filter Content ...")
			});



			Dex(".x-grid-empty").onOpen(function(value){
				if(app.edit.sidepanel.data.open){
					var isTreeEntry = $(value).parent().hasClass("results-column");

					if(isTreeEntry){
						if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab"){
							Dex.id("JahiaGxtCategoryBrowseTab").removeClass("show-results");

						} else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab"){
							Dex.id("JahiaGxtContentBrowseTab").removeClass("show-results");

						} else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtSearchTab"){
							Dex.id("JahiaGxtSearchTab").removeClass("show-results");

						}
						Dex.getCached("body").removeClass("show-results");
					}
				}

			});

			Dex(".x-grid3-row").onOpen(function(value){
				if(app.edit.sidepanel.data.open){
					var isTreeEntry = $(value).parent().hasClass("results-column");

					if(isTreeEntry){
						if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab"){
							Dex.id("JahiaGxtCategoryBrowseTab").addClass("show-results");

						} else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab"){
							Dex.id("JahiaGxtContentBrowseTab").addClass("show-results");

						} else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtSearchTab"){
							Dex.id("JahiaGxtSearchTab").addClass("show-results");

						}
						Dex.getCached("body").addClass("show-results");
					}
				}

			});

			Dex(".x-clear").onOpen(function(value){
				if(app.edit.sidepanel.data.open){
					var isTreeEntry = $(value).parent().hasClass("results-column");

					if(isTreeEntry){
						Dex.id("JahiaGxtFileImagesBrowseTab").removeClass("show-results");
						Dex.getCached("body").removeClass("show-results");
					}
				}

			});

			Dex(".thumb-wrap").onOpen(function(value){
				if(app.edit.sidepanel.data.open){
					var isTreeEntry = $(value).parent().hasClass("results-column");

					if(isTreeEntry){
						Dex.id("JahiaGxtFileImagesBrowseTab").addClass("show-results");
						Dex.getCached("body").addClass("show-results");
					}
				}

			});

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

            Dex(".menu-edit-menu-workflow").onOpen(app.edit.infoBar.tasks.updateMenuLabel);
			Dex(".menu-contribute-menu-workflow").onOpen(app.edit.infoBar.tasks.updateMenuLabel);

			Dex(".menu-edit-menu-view").onOpen(app.contextMenus.previewMenu.onOpen);

            Dex(".menu-edit-menu-publication").onOpen(app.contextMenus.publicationMenu.onOpen);

            Dex(".menu-edit-menu-edit").onOpen(app.contextMenus.moreInfoMenu.onOpen);

            Dex(".editModeContextMenu").onOpen(app.contextMenus.moreInfoMenu.onOpen);

            Dex(".menu-editmode-managers-menu").onOpen(app.contextMenus.managerMenu.onOpen);


            Dex("#JahiaGxtContentPickerWindow").onOpen(app.picker.onOpen);

            Dex("#JahiaGxtEnginePanel").onOpen(app.engine.onOpen);

            Dex("#JahiaGxtImagePopup").onOpen(app.imagePreview.onOpen);

            Dex(".edit-menu-tasks").onAttr("class", app.edit.infoBar.tasks.onChange);

			Dex(".contribute-menu-tasks").onAttr("class", app.edit.infoBar.tasks.onChange);

            Dex(".toolbar-item-workinprogressadmin").onAttr("class", app.edit.infoBar.jobs.onChange);


            Dex(".x-dd-drag-proxy").onOpen(app.edit.sidepanel.onStartDrag);

            Dex(".x-dd-drag-proxy").onClose(app.edit.sidepanel.onStopDrag);

            Dex("body").onAttr("data-selection-count", app.iframe.onSelect);

            Dex("body").onAttr("data-main-node-displayname", app.iframe.onChange);

            Dex("body").onAttr("data-main-node-path", app.contribute.onChangeMode);

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
                // .on("click", ".x-viewport-editmode .x-toolbar-first > table", app.theme.onToggle)
                .on("click", ".menu-editmode-managers-menu", app.contextMenus.managerMenu.onClose)
                .on("mousedown", ".menu-edit-menu-mode, .menu-edit-menu-user", app.contextMenus.managerMenu.onClose)
                .on("click", "#JahiaGxtSidePanelTabs > .x-tab-panel-header .x-tab-strip-spacer", app.edit.settings.close)
                .on("click", "#JahiaGxtSidePanelTabs .x-grid3-td-displayName", app.edit.sidepanel.row.onContext)
                .on("click", "#JahiaGxtContentPickerWindow", app.picker.onClick)
                .on("click", "#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview", app.picker.onListView)
                .on("click", "#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-thumbsview", app.picker.onThumbView)
                .on("click", ".node-path-title", app.iframe.clearSelection)
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
				// app.theme.off();
        }
    }



    // INITIALISE
    var init = function(){
        // Copy Anthracite CSS to remove / add when dropping in and out of STUDIO mode

		var anthraciteCSS_EN = $('link[rel=stylesheet][href$="edit_en.css"]'),
			anthraciteCSS_FR = $('link[rel=stylesheet][href$="edit_fr.css"]');

		if(anthraciteCSS_EN.length > 0){
			app.data.UILanguage = "EN";
			app.theme.data.cssReference = "edit_en.css";
			app.theme.data.storedCSS = anthraciteCSS_EN.clone();

		} else if(anthraciteCSS_FR.length > 0){
			app.data.UILanguage = "FR";
			app.theme.data.cssReference = "edit_fr.css";
			app.theme.data.storedCSS = anthraciteCSS_FR.clone();

		}

		// use Dex to cache an Dex Object
		Dex("body").cache("body");

		// Carry out operations on cached Dex Object
		Dex.getCached("body").addClass("this-is-from-chached");

		// Clear Cached Object
		// Dex.clearCache("body");

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
