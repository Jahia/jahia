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
                console.log("NO MATCHES ...");
            }
        }
    };

    // EXPOSED BASE FUNCTIONS
    Dex.init = function(){
        Dex("body").attach(mutationObserverCallback);
    };

    Dex.dumpQueue = function(){
        console.log("// Dex queue ::: ", queue);
    };

    if(exposeAs){
        window[exposeAs] = Dex;
    }

})("Dex");


Dex.dumpQueue();





(function(exposeAs){
    console.log("VERSION Dex");

    // MOUSE CONTROLLER
    var mouse = {
        trigger: function(node, eventType){
            if(!node) return false;

            var clickEvent = document.createEvent("MouseEvents");
            clickEvent.initEvent(eventType, true, true);
            node.dispatchEvent(clickEvent);

        }
    };

    // EVENT HANDLERS
    var eventHandlers = {
        // HOME BREW HANDLERS

        pageTreeUpdate: function(tree){
            // console.log("PAGE TREE UPDATED ...", tree);

        },
        pickerOpened: function(){
            console.log("OPEN PICKER");
            eventHandlers.picker("open");
        },
        enginePanelOpened: function(){
            console.log("OPEN EDIT ENGINE");
            eventHandlers.editEngine("open");
        },
        imagePopupOpened: function(){
            // console.log("OPEN IMAGE POPUP");
            eventHandlers.imagePreview("open");
        },
        modeMenuOpened: function(){
            console.log("OPEN MENU EDIT MENU MODE");
        },
        startDrag: function(){
            // console.log("::: XXX ::: STARTED TO DRAG");
            eventHandlers.closeSidePanel();
        },
        stopDrag: function(){
            // console.log("::: XXX ::: STOPPED DRAGGING");
        },
        multiSelectUpdate: function(value){
            // console.log("::: XXX ::: UPDATED MULTI SELECT");
            eventHandlers.countChanged(parseInt(value));
        },
        pageChanged: function(value){
            // console.log("::: XXX ::: UPDATED PAGE NAME");

            data.currentPage.displayname = value;
            eventHandlers.displaynameChanged();

            if(data.body.getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open"){
                eventHandlers.disableIframeClick();
            }

            console.log("Changed ...");

        },
        changeMode: function(value){
            // console.log("::: XXX ::: SITE HOLDER HAS CHANGED", value);
            eventHandlers.changedMode(value);
        },
        settingsChanged: function(value){
            // console.log("::: XXX ::: data-sitesettings HAS CHANGED", value);

            if(data.body.getAttribute("data-sitesettings") == "true" && data.body.getAttribute.attr("data-edit-window-style") != "settings"){
                eventHandlers.clickSidePanelSettingsTab(true);
            }
        },
        closedPicker: function(){
            // console.log("CLOSE PICKER");
            eventHandlers.picker("close");
        },
        closedEditEngine: function(){
            // console.log("CLOSE EDIT ENGINE");
            eventHandlers.clearMultiSelection();
            eventHandlers.editEngine("close");
        },
        closedImagePopup: function(){
            // console.log("CLOSE IMAGE POPUP");
            eventHandlers.imagePreview("close");
        },
        workflowDashboardOpened: function(){
            // console.log("OPEN IMAGE POPUP");
            mouse.trigger($(".workflow-dashboard-engine .x-tool-maximize")[0],"click")
        },

        // JQUERY HANDLERS
        // Window listeners
        windowLoad: function(){},
        windowResize: function(){
            eventHandlers.updatePageMenuPositions();
        },
        windowBlur: function(){
            // Window has lost focus, so presume that the user has clicked in the iframe.
            // If the side panel is open, then close it
            if(data.body.getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open"){
                eventHandlers.closeSidePanel();
            }

        },

        clickAppContainer: function(e){
            var inSidePanel = $(e.target).closest("#JahiaGxtSidePanelTabs, .edit-menu-sites, .window-side-panel #JahiaGxtRefreshSidePanelButton");
            if(inSidePanel.length == 0){
                eventHandlers.closeSidePanel();
            }
        },

        closeSourcePicker: function(){
            data.body.setAttribute("data-INDIGO-PICKER-SOURCE-PANEL", "");
        },

        listView: function(){
            data.body.setAttribute("indigo-PICKER-DISPLAY", "list");
        },
        thumbView: function(){
            data.body.setAttribute("indigo-PICKER-DISPLAY", "thumbs");
        },

        closeSearchPanel: function(){
            // CLOSE SEARCH PANEL

            // Hide the search panel
            data.body.setAttribute("data-INDIGO-PICKER-SEARCH", "");

            // Display the BROWSE panels
            $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)").removeClass("x-hide-display");

            // Get the refresh button
            var refreshButton = $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel").not(".x-panel-collapsed").find(".x-tool-refresh")[0];

            // CLick on the refresh button to reload the content of the directory
            mouse.trigger(refreshButton, "click");
        },
        openSearchPanel: function(){
            // OPEN SEARCH PANEL

            // Close source picker if open
            eventHandlers.closeSourcePicker();


            // Display the search panel
            data.body.setAttribute("data-INDIGO-PICKER-SEARCH", "open");

            // Put the results in LIST mode
            $("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview").trigger("click");

            // Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
            $("#CRTbrowseTabItem").addClass("x-hide-display");


            // Remove the directory listing ( gives the search panel an empty start)
            setTimeout(function(){
                $("#JahiaGxtManagerTobTable .x-grid3 .x-grid3-row").remove();
            }, 250);
        },
        changePickerSource: function(){
            // CHANGE SOURCE
            // The user has changed SOURCE, so we just need to hide the combo...
            data.body.setAttribute("data-INDIGO-PICKER-SOURCE-PANEL", "");
        },
        togglePickerSourceCombo: function(e){
            // USER HAS CLICKED THE COMBO TRIGGER
            e.stopPropagation();

            $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");

            /// Toggle the attribute in body tag
            $("body").attr("data-INDIGO-PICKER-SOURCE-PANEL", function(id, label){
                return (label == "open") ? "" : "open";
            });
        },
        mouseOverPickerSourceTrigger: function(){
            // USER HAS ROLLED OVER THE COMBO TRIGGER
            if(data.body.getAttribute("data-indigo-picker-source-panel") != "open"){
                $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").addClass("indigo-hover");
            }
        },
        mouseOutPickerSourceTrigger: function(){
            // USER HAS ROLLED OUT OF THE COMBO TRIGGER
            $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");
        },
        repositionFilePreviewButton: function(e, offset){
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
        selectPickerFile: function(){
            $(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");
        },

        // User has changed modes
        changedMode: function(mode){
            mode.split(" ").forEach(function(cl) {
                if (cl.indexOf("x-viewport") == 0) {
                    switch (cl) {
                        case "x-viewport-studiomode":
                            // Remove Anthracite CSS style sheet
                            $('link[rel=stylesheet][href$="edit_en.css"]').remove();

                            // Register the fact that it has been removed
                            data.css.active = false;
                            break;

                        case "x-viewport-editmode":
                        case "x-viewport-contributemode":
                        case "x-viewport-adminmode":
                        case "x-viewport-dashboardmode":
                        default:

                            if (!data.css.active) {
                                // Anthracite CSS has been removed, so plug it back in
                                $("head").append(data.css.storedCSS);
                            }

                            break;
                    }
                }
            })

        },



        // Edit Engine Controller
        imagePreview: function(state){
            switch(state){
                case "open":
                    data.body.setAttribute("data-INDIGO-IMAGE-PREVIEW", "open");

                    // Attribute used to display the friendly name in edit panel
                    $(".engine-panel > div.x-panel-header .x-panel-header-text").attr("data-friendly-name", "nodeDisplayName");
                    break;

                case "close":
                    data.body.setAttribute("data-INDIGO-IMAGE-PREVIEW", "");
                    break;
            }
        },

        editEngine: function(state){
            var nodeDisplayName = data.body.getAttribute("data-singleselection-node-displayname");

            switch(state){
                case "open":
                    data.body.setAttribute("data-INDIGO-EDIT-ENGINE", "open");

                    // Attribute used to display the friendly name in edit panel
                    $(".engine-panel > div.x-panel-header .x-panel-header-text").attr("data-friendly-name", nodeDisplayName);
                    break;

                case "close":
                    data.body.setAttribute("data-INDIGO-EDIT-ENGINE", "");
                    break;
            }
        },

        // Picker Controller
        picker: function(state){
            switch(state){
                case "open":

                    data.body.setAttribute("data-INDIGO-PICKER-SEARCH", "");
                    data.body.setAttribute("data-INDIGO-PICKER", "open");
                    data.body.setAttribute("indigo-PICKER-DISPLAY", "thumbs");
                    break;

                case "close":
                    data.body.setAttribute("data-INDIGO-PICKER", "");
                    break;
            }
        },

        // Clear Multi select
        clearMultiSelection: function(e){
            $("iframe").trigger("click");
        },

        // Body updates
        displaynameChanged: function(){

            var pageTitle,
                multiselect = "off";

            switch(data.currentPage.displayname){
                case "settings":
                case "System Site":
                    // Need to trigger a click on Settings tabs to make sure that the menus are loaded in advance.
                    $("#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab").trigger("click");

                    // Attach an observer to the Side Panel Menu
                    // observers => panelMenuObserver(); PUT BACK ??????
                    break;

                default:
                    // Presumably in Edit Mode or Contribute Mode, in which case we need to set the page title
                    switch(data.multiselection.count){
                        case 0:
                            pageTitle = data.currentPage.displayname;
                            break;

                        case 1:
                            pageTitle = "1 selected item";
                            pageTitle = data.body.getAttribute("data-singleselection-node-displayname");
                            multiselect = "on";


                            break;

                        default:
                            pageTitle = data.multiselection.count + " selected items";
                            multiselect = "on";
                            break;
                    }

                    // Set multiselect status in body attribute...
                    data.body.setAttribute("data-multiselect", multiselect);



                    // Page Title in Edit Made
                    $(".x-current-page-path").attr("data-PAGE-NAME",pageTitle);

                    // Page Title in Contribute Made
                    $(".x-viewport-contributemode .toolbar-itemsgroup-languageswitcher").attr("data-PAGE-NAME",pageTitle);

                    // Page Titles need centering
                    eventHandlers.updatePageMenuPositions();

                    // Remove Mutation Observer used in Settings pages (if attached)
                    if(data.panelMenu.observer){
                        data.panelMenu.observer.disconnect();
                        data.panelMenu.observer = null;

                    }
            }


        },
        countChanged: function(count){
            // Multiple Items have been selected (in Edit Mode)
            data.multiselection.count = count;

            // Refresh the title of the page accordingly
            eventHandlers.displaynameChanged();

            setTimeout(function(){
                $(".editModeContextMenu .x-menu-list").attr("data-selected-name", data.body.getAttribute("data-singleselection-node-displayname"));
            }, 50);
        },
        publicationStatusChanged: function(status){
            // Publication status of the current page has changed (in edit or contribute mode). Update status accordingly.
            data.body().setAttribute("data-PAGE-PUBLICATION-STATUS", status);

        },

        updatePageMenuPositions: function(){
            // Center title to page and move surrounding menus to right and left.
            // Ask Thomas for a body attribute to distinguish EDIT and CONTRIBUTE modes.

            // EDIT MODE page title positions
            var editMode = {};
            editMode.pageNameLeft = parseInt($(data.temp.selectors.editModePageName).position().left);
            editMode.pageNameWidth = Math.floor($(data.temp.selectors.editModePageName).width()) - 1;
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
            $(data.temp.selectors.editModeMoreInfo).css({
                "left": (editMode.pageNameLeft + 92) + "px",
                "opacity": 1
            });

            // More Language Menu (previously labeled as Edit )
            $(data.temp.selectors.editModeLanguageSelector).attr("style", "left: " + (editMode.pageNameLeft + 92) + "px !important; opacity: 1");

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
            $(data.temp.selectors.contributeModeLanguageSelector).css({
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
            // Close the side panel if it is open.

        },
        closeManagerMenu: function(){
            // Manager Menu has been closed by clicking on the X.
            // Can not remove the actual DOM node as it causes problems with GWT, so just hide it instead.
            $(".menu-editmode-managers-menu").fadeOut();
        },
        toggleSidePanelDocking: function(e){
            // This listener has a dual purpose depending on where it was called from.
            // If called from the Edit Mode then it toggles the Side Panel Menu as PINNED and FLOATING
            // If it is called from the settings window then it acts as a close button, closing the settings and returning to the Edit Mode.

            var windowStyle = data.body.getAttribute("data-edit-window-style");

            switch(windowStyle){
                case "settings":
                    // SETTINGS MODE: Button acts as a button that closed the Settings Overlay Page

                    data.body.setAttribute("data-edit-window-style", "default");
                    eventHandlers.closeSidePanel()

                    // Load the last page displayed in the Edit Mode. Technically this should never be NULL. However, need to assign a value on first window load as it is currently only assigned when a user clicks a page in the Page Tree.
                    if(data.lastOpenedPage){
                        data.lastOpenedPage.trigger("mousedown");

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
			// User has clicked the Settings Tab Button.
			if(data.currentPage.displayname != "settings" && (data.body.getAttribute("data-sitesettings") == "false" || forceClick)){
				data.body.setAttribute("data-edit-window-style", "settings");

				eventHandlers.openSidePanel()

				if(data.lastSettingsPage){
					// Found settings page in history so open it
					data.lastSettingsPage.trigger("click");
				} else {
					// Trigger click on first list item WHEN it has loaded...
					if ($("#JahiaGxtSettingsTab .x-grid3-row")[0]) {
						var firstInList = $("#JahiaGxtSettingsTab .x-grid3-row")[0]  ;
						mouse.trigger(firstInList, "mousedown");
						mouse.trigger(firstInList, "click");
					} else {
						Dex("#JahiaGxtSettingsTab").onceTreeChange(function(nodes){
							var firstInList = nodes[0];
							mouse.trigger(firstInList, "mousedown");
							mouse.trigger(firstInList, "click");
						});
					}

				}
			}

		},
        clickSidePanelTab: function(){
            // User has clicked on one of the side panel tabs (except for Settings Tab which calls eventHandlers.clickSidePanelSettingsTab)

            var clickedTabID = $(this).attr("id");

            data.body.setAttribute("data-INDIGO-GWT-PANEL-TAB", clickedTabID);

            // Menus for the Tabs that call this listener require a normal side panel display
            data.body.setAttribute("data-edit-window-style", "default");

            var tabMenuActive = $(this).hasClass("x-tab-strip-active"),
                sidePanelOpen = data.body.getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open";

            if(tabMenuActive && sidePanelOpen){
                // CLOSE SIDE PANEL: Already open for current Tab Menu
                eventHandlers.closeSidePanel()
            } else {
                // OPEN SIDE PANEL.
                eventHandlers.openSidePanel()

            }



        },
        closeSidePanel: function(){

            if(data.body.getAttribute("data-edit-window-style") !== "settings"){
                data.body.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "");

                // Revert iframes body style attribute to what it was originally
                $(".window-iframe").contents().find("body").attr("style", data.sidePanelTabs.iframeBodyStyle);

            }


        },
        addPageToHistory: function(){
            if(data.body.getAttribute("data-sitesettings") !== "true" && data.body.getAttribute("data-main-node-displayname") !== "settings"){
                var openedPage = $(this).closest("#JahiaGxtPagesTab").length > 0,
                    openedSettings = $(this).closest("#JahiaGxtSettingsTab").length > 0;

                if(openedPage){
                    data.lastOpenedPage = $(this);
                } else if(openedSettings){
                    data.lastSettingsPage = $(this);
                }

            }


        },
        openSidePanel: function(){
            data.body.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");

            // GWT has problems populating the site page tree when the side panel is hidden.
            // Solution: When the side panel is opened for the FIRST TIME ONLY, the refresh button is triggered and the sites page tree is populated correctly.
            if(data.body.getAttribute("data-sitesettings") == "false"){
                if(data.sidePanelTabs.firstLoad){
                    $(".window-side-panel #JahiaGxtRefreshSidePanelButton").trigger("click");
                    data.sidePanelTabs.firstLoad = false;
                }

                eventHandlers.disableIframeClick();
            }


        },
        disableIframeClick: function(){
            // SAVE the curent style properties of the iframes body tag so we can revert to it once the side panel is closed.
            var iframeBody = $(".window-iframe").contents().find("body");
            data.sidePanelTabs.iframeBodyStyle = iframeBody.attr("style") || "";

            // Remove pointer events from the iframes body, which means that once a user clicks on the iframe to exit the side panel, the content is not automatically selected.
            iframeBody.attr("style", data.sidePanelTabs.iframeBodyStyle + " pointer-events: none !important");
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

            eventHandlers.openSidePanel()
        },
        mouseOverSidePanelTab: function(){
            // Mouseover Side Panel tabs, so open it.
            if(data.body.getAttribute("data-selection-count") == "0"){
                eventHandlers.openSidePanel()

            }
        },
        mouseLeaveSidePanelTabs: function(e){
            // CHECK if the user has actually left the Side Panel OR if they have just opened a context menu, in which case keep the Side Panel Open.
            // Note that this only applies when the Side Panel is activated on mouse over.

            if($("body > div.x-menu").length > 0){
                // A Context Menu has been found, so do not close the Side Panel.

            } else {
                // No Context menu found, so assume that the Side Panel has really been mouseout-ed - close it.

                eventHandlers.closeSidePanel()

                // Set flag and timer to remove after 100ms.
                data.sidePanelTabs.justBeenClosed = true;
            }

        },
        clickMoreOptionsButton: function(e, matchClass){
            // Open Context Menu when clicking "More" button.
            // if matchClass is passed, then the click is ONLY accepted if the clicked element has that class.
            // if matchClass is not passed then it is accepted.

            var acceptClick = (matchClass) ? $(e.target).hasClass(matchClass) : true,
                eV;

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

        },
        mouseOverImagePickerRow: function(e){
            data.filePicker.currentItem = $(this)[0];
            data.filePicker.title = $(this).find(".x-grid3-col-name").html();

            if($(this).hasClass("x-grid3-row-selected")){
                $(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");

            } else {
                $(".toolbar-item-filepreview").attr("indigo-preview-button-state", "unselected");

            }

            $(".toolbar-item-filepreview").attr("indigo-preview-button", "show");
        },
        mouseOverImagePickerThumb: function(e){
            data.filePicker.currentItem = $(this)[0];
            data.filePicker.title = $(this).attr("id");

            if($(this).hasClass("x-view-item-sel")){
                $(".toolbar-item-filepreview").attr("indigo-preview-button-state", "selected");

            } else {
                $(".toolbar-item-filepreview").attr("indigo-preview-button-state", "unselected");

            }

            $(".toolbar-item-filepreview").attr("indigo-preview-button", "show");

        },
        mouseOverFilePreviewButton: function(){
            $(data.filePicker.currentItem)
                .addClass("x-view-over")
                .addClass("x-grid3-row-over");
        },
        mouseOutFilePreviewButton: function(){
            $(data.filePicker.currentItem)
                .removeClass("x-view-over")
                .removeClass("x-grid3-row-over");
        },
        mouseClickFilePreviewButton: function(e, secondClick){
            mouse.trigger(data.filePicker.currentItem, "mousedown");
            mouse.trigger(data.filePicker.currentItem, "mouseup");

            if(!secondClick){
                $("#JahiaGxtImagePopup").remove(); // remove OLD preview
                $(this).trigger("click", [true]); // Reopen with new preview
                $("#JahiaGxtImagePopup .x-window-bwrap").attr("data-file-name", data.filePicker.title);

            }

            $(".toolbar-item-filepreview").attr("indigo-preview-button", "hide");

        },
        mouseOverTreeRow: function(e){
            // Position the preview button next to the file whilst hovering
            eventHandlers.repositionFilePreviewButton(e, {
                left: -58,
                top: 0
            });

        },
        mouseOverTreeThumb: function(e){
            // Position the preview button next to the file whilst hovering
            eventHandlers.repositionFilePreviewButton(e, {
                left: -52,
                top: 0
            });


        }

    };


    // EVENT LISTENERS
    var eventListeners = {
        attach: function(){
            // HOME BREW EVENT LISTENERS
            // Set up INDIGO listeners (listening to changes in DOM)
            console.log("ATTACHING HOME BREW LISTENERS");

            Dex("#JahiaGxtPagesTab").onTreeChange(eventHandlers.pageTreeUpdate);

            Dex("#JahiaGxtContentPickerWindow").onOpen(eventHandlers.pickerOpened);

            Dex("#JahiaGxtEnginePanel").onOpen(eventHandlers.enginePanelOpened);

            Dex("#JahiaGxtImagePopup").onOpen(eventHandlers.imagePopupOpened);

            Dex(".menu-edit-menu-mode").onOpen(eventHandlers.modeMenuOpened);

            Dex(".x-dd-drag-proxy").onOpen(eventHandlers.startDrag);

            Dex(".x-dd-drag-proxy").onClose(eventHandlers.stopDrag);

            Dex("body").onAttr("data-selection-count", eventHandlers.multiSelectUpdate);

            Dex("body").onAttr("data-main-node-displayname", eventHandlers.pageChanged);

            Dex(".x-jahia-root").onAttr("class", eventHandlers.changedMode);

            Dex("body").onAttr("data-sitesettings", eventHandlers.settingsChanged);

            Dex("#JahiaGxtContentPickerWindow").onClose(eventHandlers.closedPicker);

            Dex("#JahiaGxtEnginePanel").onClose(eventHandlers.closedEditEngine);

            Dex("#JahiaGxtImagePopup").onClose(eventHandlers.closedImagePopup);

            Dex(".workflow-dashboard-engine").onOpen(eventHandlers.workflowDashboardOpened)

            // BROWSER LISTENERS
            console.log("ATTACHING BROWSER LISTENERS");
            window.onresize = eventHandlers.windowResize;

            // JQUERY EVENT LISTENERS
            console.log("ATTACHING JQUERY LISTENERS");
            $(window).on("blur", eventHandlers.windowBlur);

            $("body")
                .on("click", ".app-container", eventHandlers.clickAppContainer)
                .on("click", ".toolbar-item-filepreview", eventHandlers.mouseClickFilePreviewButton)
                .on("mouseenter", ".toolbar-item-filepreview", eventHandlers.mouseOverFilePreviewButton)
                .on("mouseleave", ".toolbar-item-filepreview", eventHandlers.mouseOutFilePreviewButton)
                .on("mouseenter", ".thumb-wrap", eventHandlers.mouseOverImagePickerThumb)
                .on("mouseenter", "#JahiaGxtManagerLeftTree + div .x-grid3 .x-grid3-row", eventHandlers.mouseOverImagePickerRow)
                .on("click", "#JahiaGxtManagerLeftTree + div .x-grid3 .x-grid3-row", eventHandlers.selectPickerFile)
                .on("click", ".x-grid3-row .x-grid3-td-size", eventHandlers.clickMoreOptionsButton) // File Picker > Search > Results List
                .on("click", ".x-grid3-row .x-tree3-el", function(e){
                    // Side Panel > Trees

                    eventHandlers.clickMoreOptionsButton(e, "x-tree3-el");
                })
                .on("click", "#JahiaGxtManagerLeftTree + div .thumb-wrap .thumb", eventHandlers.clickMoreOptionsButton) // File Picker > Thumb View
                .on("click", "#JahiaGxtManagerLeftTree + div .thumb-wrap", eventHandlers.selectPickerFile)
                .on("click", ".x-viewport-editmode .x-toolbar-first > table", eventHandlers.toggleThemeMode)
                .on("click", ".editmode-managers-menu", eventHandlers.openManagerMenu)
                .on("click", ".menu-editmode-managers-menu", eventHandlers.closeManagerMenu)
                .on("mousedown", ".menu-edit-menu-mode, .menu-edit-menu-user", eventHandlers.closeManagerMenu)
                .on("click", "#JahiaGxtSidePanelTabs > .x-tab-panel-header .x-tab-strip-spacer", eventHandlers.toggleSidePanelDocking)
                .on("click", "#JahiaGxtSidePanelTabs .x-grid3-td-displayName", function(e){
                    eventHandlers.clickMoreOptionsButton(e, "x-grid3-td-displayName");
                })
                .on("click", "#JahiaGxtContentPickerWindow", eventHandlers.closeSourcePicker)
                .on("click", "#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview", eventHandlers.listView)
                .on("click", "#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-thumbsview", eventHandlers.thumbView)
                // .on("click", "#JahiaGxtFileImagesBrowseTab .thumb-wrap > div:nth-child(1) > div:nth-child(2) div:nth-child(1) b", eventHandlers.clickMoreOptionsButton) // NOT IN USE
                .on("click", ".x-current-page-path", eventHandlers.clearMultiSelection)
                .on("click", "#JahiaGxtSidePanelTabs .x-grid3-row", eventHandlers.addPageToHistory)
                .on("mousedown", "#JahiaGxtManagerLeftTree__CRTbrowseTabItem", eventHandlers.closeSearchPanel)
                .on("mousedown", "#JahiaGxtManagerLeftTree__CRTsearchTabItem", eventHandlers.openSearchPanel)
                .on("click", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header", eventHandlers.changePickerSource)
                .on("click", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", eventHandlers.togglePickerSourceCombo)
                .on("mouseenter", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", eventHandlers.mouseOverPickerSourceTrigger)
                .on("mouseleave", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", eventHandlers.mouseOutPickerSourceTrigger)
                .on("mouseenter", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div .x-grid3-row", eventHandlers.mouseOverTreeRow)
                .on("mouseenter", "#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div .thumb-wrap", eventHandlers.mouseOverTreeThumb)
                .on("mouseup", "#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab", eventHandlers.clickSidePanelTab)
                .on("mouseup", "#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", eventHandlers.clickSidePanelSettingsTab);
        }
    }


    // DATA
    var data = {
        css: {
            storedCSS: null,
            active: true
        },
        sidePanelTabs: {
            mouseOutTimer: null,
            justBeenClosed: false,
            firstLoad: true
        },
        panelMenu: {
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
        filePicker: {
            currentItem: null
        },
        temp: {
            selectors: { // Ask Thomas for classes where possible
                editModePageName: ".mainmodule-head-container .toolbar-left-container",
                editModeMoreInfo: "body[data-selection-count='0'] .x-panel-body.x-border-layout-ct > div:nth-child(2) .x-panel-header > div:nth-child(2) > table > tbody > tr > td > div > table > tbody > tr > td:nth-child(5)",
                contributeModeLanguageSelector: ".x-viewport-contributemode .x-toolbar-first > table:nth-child(1) > tbody > tr > td:nth-child(1) > table > tbody > tr > td:nth-child(16) div input",
                editModeLanguageSelector: ".mainmodule-head-container .toolbar-itemsgroup-languageswitcher",
                closeSidePanelCapture: "[data-INDIGO-GWT-SIDE-PANEL='open'] .x-panel-body.x-border-layout-ct > div:nth-child(1) > div:nth-child(2) > div:nth-child(2)",
                closeSidePanelCapture: "[data-INDIGO-GWT-SIDE-PANEL='open'] .gwt-body-edit"
            }
        }
    }

    // DEV NOTE: DX is NOT IN USE YET.
    var DX = {

        closeSourcePicker: function(){},
        listView: function(){},
        thumbView: function(){},
        closeSearchPanel: function(){},
        openSearchPanel: function(){},
        changePickerSource: function(){},
        togglePickerSourceCombo: function(){},
        mouseOverPickerSourceTrigger: function(){},
        mouseOutPickerSourceTrigger: function(){},
        repositionFilePreviewButton: function(){},
        selectPickerFile: function(){},
        changedMode: function(){},
        imagePreview: function(){},
        editEngine: function(){},
        picker: function(){},
        clearMultiSelection: function(){},
        displaynameChanged: function(){},
        countChanged: function(){},
        publicationStatusChanged: function(){},
        updatePageMenuPositions: function(){},
        toggleThemeMode: function(){},
        openManagerMenu: function(){},
        closeManagerMenu: function(){},
        toggleSidePanelDocking: function(){},
        clickSidePanelSettingsTab: function(){},
        clickSidePanelTab: function(){},
        closeSidePanel: function(){},
        addPageToHistory: function(){},
        openSidePanel: function(){},
        mouseEnterSidePanelTab: function(){},
        mouseEnterSiteSelector: function(){},
        mouseOverSidePanelTab: function(){},
        mouseLeaveSidePanelTabs: function(){},
        clickMoreOptionsButton: function(){},
        mouseOverImagePickerRow: function(){},
        mouseOverImagePickerThumb: function(){},
        mouseOverFilePreviewButton: function(){},
        mouseOutFilePreviewButton: function(){},
        mouseClickFilePreviewButton: function(){}
    }


    // INITIALISE
    var init = function(){
        // Copy Anthracite CSS to remove / add when dropping in and out of STUDIO mode
        data.css.storedCSS = $('link[rel=stylesheet][href$="edit_en.css"]').clone();

        data.body = window.document.body;

        // Initialise Dex Observer
        Dex.init();

        eventListeners.attach();
    }

    $(document).ready(function(){
        init();
    });


    // EXPOSED
    if(exposeAs){
        window[exposeAs] = DX;
    }

})("DX");
