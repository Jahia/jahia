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
            delegatedEventListeners = {},
            mutationObservers = {},
            DOMMutationCallback = function(mutations, selector, parentNodes){
                var n, x, y,
                    mutationRecord,
                    addedNode,
                    removedNode,
                    modifiedNodeAttribute,
                    callbacks,
                    modifiedSelector,
                    _target,
                    executeCallbacks = function(queue, node, arg1, arg2){
                        var n = 0;

                        while(queue[n]){
                            // Call callback function
                            queue[n].callback.call(node, arg1, arg2);

                            if(!queue[n].persistant){
                                queue.splice(n, 1);
                            }

                            n++;

                        }
                    },
                    executeAttributecallbacks = function(queue, node, attrKey, attrValue){
                        y = 0;

                        while(queue[y]){
                            // Call callback function
                            if(queue[y].attrKey == attrKey){
                                queue[y].callback.call(node, attrKey, attrValue);

                                if(!queue[y].persistant){
                                    queue.splice(y, 1);

                                }

                            }

                            y++;
                        }
                    };


                // Loop through mutation records
                for(n = 0; n < mutations.length; n++){
                    mutationRecord = mutations[n];

                    // Loop through added nodes
                    if(mutationRecord.addedNodes.length > 0){
                        callbacks = mutationObservers[selector].callbacks.onOpen;

                        if(callbacks){
                            for(x = 0; x < mutationRecord.addedNodes.length; x++){
                                addedNode = mutationRecord.addedNodes[x];

                                // Check if node type is valid
                                if(addedNode.nodeType == 1){

                                    // Loop through callbacks
                                    for(_target in callbacks){
                                        modifiedSelector = callbacks[_target].matchType.modifiedSelector;

                                        // See if addedNode matches the _target of the callback
                                        switch(callbacks[_target].matchType.type){
                                            case "tag":
                                                if(addedNode.tagName.toUpperCase() == modifiedSelector){
                                                    // Loop through all callbacks
                                                    executeCallbacks(callbacks[_target].queue, addedNode);

                                                }
                                                break;
                                            case "id":
                                                if(addedNode.id == modifiedSelector){
                                                    // Loop through all callbacks
                                                    executeCallbacks(callbacks[_target].queue, addedNode);

                                                }
                                                break;
                                            case "classname":
                                                if(addedNode.classList.contains(modifiedSelector)){
                                                    // Loop through all callbacks
                                                    executeCallbacks(callbacks[_target].queue, addedNode, mutationRecord.addedNodes);

                                                }
                                                break;
                                            case "complex":
                                                if(addedNode.matches(modifiedSelector)){
                                                    // Loop through all callbacks
                                                    executeCallbacks(callbacks[_target].queue, addedNode);

                                                }
                                                break;

                                        }

                                    }
                                }

                            }
                        }
                    }

                    if(mutationRecord.removedNodes.length > 0){
                        callbacks = mutationObservers[selector].callbacks.onClose;

                        if(callbacks){
                            // Loop through removed nodes
                            for(x = 0; x < mutationRecord.removedNodes.length; x++){
                                removedNode = mutationRecord.removedNodes[x];

                                // Check if node type is valid
                                if(removedNode.nodeType == 1){

                                    // Loop through callbacks
                                    for(_target in callbacks){

                                        modifiedSelector = callbacks[_target].matchType.modifiedSelector;

                                        // See if removedNode matches the _target of the callback
                                        switch(callbacks[_target].matchType.type){
                                            case "tag":
                                                if(removedNode.tagName.toUpperCase() == modifiedSelector){
                                                    // Loop through all callbacks
                                                    executeCallbacks(callbacks[_target].queue, removedNode);

                                                }
                                                break;
                                            case "id":
                                                if(removedNode.id == modifiedSelector){
                                                    // Loop through all callbacks
                                                    executeCallbacks(callbacks[_target].queue, removedNode);

                                                }
                                                break;
                                            case "classname":
                                                if(removedNode.classList.contains(modifiedSelector)){
                                                    // Loop through all callbacks
                                                    executeCallbacks(callbacks[_target].queue, removedNode);

                                                }
                                                break;
                                            case "complex":
                                                if(removedNode.matches(modifiedSelector)){
                                                    // Loop through all callbacks
                                                    executeCallbacks(callbacks[_target].queue, removedNode);

                                                }
                                                break;

                                        }

                                    }

                                }

                            }
                        }
                    }

                    if(mutationRecord.attributeName){
                        callbacks = mutationObservers[selector].callbacks.onAttribute;

                        if(callbacks){
                            // Loop through modified attributes
                            modifiedNodeAttribute = mutationRecord.target;

                            // Loop through callbacks
                            for(_target in callbacks){
                                modifiedSelector = callbacks[_target].matchType.modifiedSelector;

                                // See if modifiedNodeAttribute matches the _target of the callback
                                switch(callbacks[_target].matchType.type){
                                    case "tag":
                                        if(modifiedNodeAttribute.tagName.toUpperCase() == modifiedSelector){
                                            // Loop through all callbacks
                                            if(mutationRecord.target.attributes[mutationRecord.attributeName]){ // Its a live list, so check it hasnt been removed
                                                executeAttributecallbacks(callbacks[_target].queue, modifiedNodeAttribute, mutationRecord.attributeName, mutationRecord.target.attributes[mutationRecord.attributeName].value);

                                            }

                                        }
                                        break;
                                    case "id":
                                        if(modifiedNodeAttribute.id == modifiedSelector){
                                            // Loop through all callbacks
                                            if(mutationRecord.target.attributes[mutationRecord.attributeName]){ // Its a live list, so check it hasnt been removed
                                                executeAttributecallbacks(callbacks[_target].queue, modifiedNodeAttribute, mutationRecord.attributeName, mutationRecord.target.attributes[mutationRecord.attributeName].value);

                                            }
                                        }
                                        break;
                                    case "classname":
                                        if(modifiedNodeAttribute.classList.contains(modifiedSelector)){
                                            // Loop through all callbacks
                                            if(mutationRecord.target.attributes[mutationRecord.attributeName]){ // Its a live list, so check it hasnt been removed
                                                executeAttributecallbacks(callbacks[_target].queue, modifiedNodeAttribute, mutationRecord.attributeName, mutationRecord.target.attributes[mutationRecord.attributeName].value);

                                            }
                                        }
                                        break;
                                    case "complex":
                                        if(modifiedNodeAttribute.matches(modifiedSelector)){
                                            // Loop through all callbacks
                                            if(mutationRecord.target.attributes[mutationRecord.attributeName]){ // Its a live list, so check it hasnt been removed
                                                executeAttributecallbacks(callbacks[_target].queue, modifiedNodeAttribute, mutationRecord.attributeName, mutationRecord.target.attributes[mutationRecord.attributeName].value);

                                            }
                                        }
                                        break;
                                }

                            }
                        }
                    }

                }

            },
            createEventListener = function(dexObject, eventType, selector, target, callback, persistant){
                // Check that the event type exists in queue
                if(!delegatedEventListeners[eventType]){
                    delegatedEventListeners[eventType] = {};
                }

                // Check that this selector is registered
                if(!delegatedEventListeners[eventType][selector]){
                    delegatedEventListeners[eventType][selector] = {};

                    // Setup listener

                    attachEventListeners(eventType, dexObject);
                }

                // Check that this target is registered
                if(!delegatedEventListeners[eventType][selector][target]){
                    delegatedEventListeners[eventType][selector][target] = [];
                }

                // register delegated event listener
                delegatedEventListeners[eventType][selector][target].push({
                    callback: callback,
                    persistant: persistant
                });


            },
            attachEventListeners = function(eventType, dexObject){

                var n,
                    selector = {
                        string: dexObject.selector
                    };

                for(n = 0; n < dexObject.nodes.length; n++){
                    selector.node = dexObject.nodes[n];

                    selector.node.addEventListener(eventType, function(e){
                        var eventHandlers = delegatedEventListeners[eventType][selector.string];

                        for(handlerSelector in eventHandlers){

                            clickedNode = e.target;


                            while(	clickedNode &&
                                    clickedNode !== document &&
                                    clickedNode !== selector.node) { // Stop looking when we hit the node that contains the event listener

                                if (clickedNode.matches(handlerSelector)) {

                                    var handlerIndex = 0;

                                    while (eventHandlers[handlerSelector][handlerIndex]){
                                        eventHandlers[handlerSelector][handlerIndex].callback.call(clickedNode, e);

                                        if(!eventHandlers[handlerSelector][handlerIndex].persistant){
                                            eventHandlers[handlerSelector].splice(handlerIndex, 1);
                                        }

                                        handlerIndex++;
                                    }

                                }

                                clickedNode = clickedNode.parentNode;

                            }
                        }

                    });
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

            appendClone: function(node){

                var n;

                for(n = 0; n < this.nodes.length; n++){
                    this.nodes[n].appendChild(node.cloneNode(true));
                }

                return this;

            },

            append: function(node){

                var n;

                for(n = 0; n < this.nodes.length; n++){
                    this.nodes[n].appendChild(node);
                }

                return this;

            },

            remove: function(){

                var n;

                for(n = 0; n < this.nodes.length; n++){
                    this.nodes[n];

                    if (this.nodes[n].parentNode) {
                        this.nodes[n].parentNode.removeChild(this.nodes[n]);
                    }
                }

                return this;

            },

            clone: function(){
                var n,
                    clonedNodes = [];

                for(n = 0; n < this.nodes.length; n++){
                    clonedNodes.push(this.nodes[n].cloneNode(true));
                }

                this.nodes = clonedNodes;

                return this;
            },

            parent: function(){

                this.nodes = [this.nodes[0].parentNode];

                return this;

            },

            closest: function(matchSelector){

                var closest = [];

                node = this.nodes[0];

                // Go through parents
                while(node && node !== document) {
                    if (node.matches(matchSelector)) {

                        closest = [node];
                        break;

                    }

                    node = node.parentNode;

                }

                this.nodes = closest;

                return this;

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

            toggleAttribute: function(key, value){
                /* Get attribute of first node in nodelist */

                var n,
                    attrValue;

                for(n = 0; n < this.nodes.length; n++){
                    attrValue = this.nodes[n].getAttribute(key);

                    if(attrValue == value[0]){
                        this.nodes[n].setAttribute(key, value[1]);
                    } else {
                        this.nodes[n].setAttribute(key, value[0]);
                    }
                }

                return this;
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

            onOpen: function(target, callback){
                this.onMutation("onOpen", target, callback, {
                    children: false,
                    persistant: true
                });

                return this;
            },

            onceOpen: function(target, callback){
                this.onMutation("onOpen", target, callback, {
                    children: false,
                    persistant: false
                });

                return this;
            },

            onClose: function(target, callback){

                this.onMutation("onClose", target, callback, {
                    children: false,
                    persistant: true
                });

                return this;
            },

            onceClose: function(target, callback){

                this.onMutation("onClose", target, callback, {
                    children: false,
                    persistant: false
                });

                return this;
            },

            onAttribute: function(target, attrKey, callback){
                this.onMutation("onAttribute", target, callback, {
                    attrKey: attrKey,
                    persistant: true
                });

                return this;
            },

            onceAttribute: function(target, attrKey, callback){
                this.onMutation("onAttribute", target, callback, {
                    attrKey: attrKey,
                    persistant: false
                });

                return this;
            },

            onMutation: function(mutationType, target, callback, parameters){
                var selector = this.selector,
                    parentNodes = this.nodes,
                    matchType = function(target){
                        // This needs updating to cater for complex selectors.

                        var result;

                        if(target[0] == "."){
                            result = {
                                type: "classname",
                                modifiedSelector: target.slice(1)
                            }


                        } else if(target[0] == "#"){
                            result = {
                                type: "id",
                                modifiedSelector: target.slice(1)
                            }

                        } else if(target[0] == "["){
                            result = {
                                type: "complex",
                                modifiedSelector: target
                            }

                        } else {
                            result = {
                                type: "tag",
                                modifiedSelector: target.toUpperCase()
                            }
                        }

                        return result;

                    }(target),
                    n,
                    parameters = parameters || {};


                // See if we need to attach a mutation observer


                if(!mutationObservers[this.selector]){
                    mutationObservers[this.selector] = {
                        observer: new MutationObserver(function(mutations){
                            DOMMutationCallback(mutations, selector, parentNodes);
                        }),
                        callbacks: {}
                    }

                    // Attach observer to all matches nodes
                    for(n = 0; n < this.nodes.length; n++){
                        mutationObservers[this.selector].observer.observe(this.nodes[n], {
                            attributes: true,
                            childList: true,
                            characterData: false,
                            subtree: true
                        });
                    }
                }



                // See if there are already callbacks for mutationType
                if(!mutationObservers[this.selector].callbacks[mutationType]){
                    mutationObservers[this.selector].callbacks[mutationType] = {}
                }

                // See if there are already calbacks for this target
                if(!mutationObservers[this.selector].callbacks[mutationType][target]){
                    // mutationObservers[this.selector][childtree].callbacks[mutationType][target] = []
                    mutationObservers[this.selector].callbacks[mutationType][target] = {
                        matchType: matchType,
                        queue: []
                    }
                }

                // Save callback
                mutationObservers[this.selector].callbacks[mutationType][target].queue.push({
                    callback: callback,
                    attrKey: parameters.attrKey,
                    persistant: parameters.persistant
                });
            },


            onClick: function(target, callback){
                createEventListener(this, "click", this.selector, target, callback, true);

                return this;

            },

            oneClick: function(target, callback){
                createEventListener(this, "click", this.selector, target, callback, false);

                return this;

            },

            onMouseUp: function(target, callback){
                createEventListener(this, "mouseup", this.selector, target, callback, true);

                return this;

            },

            oneMouseUp: function(target, callback){
                createEventListener(this, "mouseup", this.selector, target, callback, false);

                return this;

            },

            onMouseDown: function(target, callback){
                createEventListener(this, "mousedown", this.selector, target, callback, true);

                return this;

            },

            oneMouseDown: function(target, callback){
                createEventListener(this, "mousedown", this.selector, target, callback, false);

                return this;

            },

            onMouseEnter: function(target, callback){
                createEventListener(this, "mouseenter", this.selector, target, callback, true);

                return this;

            },

            oneMouseEnter: function(target, callback){
                createEventListener(this, "mouseenter", this.selector, target, callback, false);

                return this;

            },

            onMouseLeave: function(target, callback){
                createEventListener(this, "mouseleave", this.selector, target, callback, true);

                return this;

            },

            oneMouseLeave: function(target, callback){
                createEventListener(this, "mouseleave", this.selector, target, callback, false);

                return this;

            },

            onMouseOver: function(target, callback){
                createEventListener(this, "mouseover", this.selector, target, callback, true);

                return this;

            },

            oneMouseOver: function(target, callback){
                createEventListener(this, "mouseover", this.selector, target, callback, false);

                return this;

            },

            onMouseOut: function(target, callback){
                createEventListener(this, "mouseout", this.selector, target, callback, true);

                return this;

            },

            oneMouseOut: function(target, callback){
                createEventListener(this, "mouseout", this.selector, target, callback, false);

                return this;

            },


        }

        Dex.appendCSS = function(url){
            var head = document.head,
                link = document.createElement('link');

              link.type = "text/css";
              link.rel = "stylesheet";
              link.href = url;

              head.appendChild(link);

              return link;
        }

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

            var nodeCollection = document.getElementsByClassName(classname),
                nodes = Array.prototype.slice.call(nodeCollection);

            return Dex("." + classname, nodes);

        }

        Dex.iframe = function(selector){

            var iframe = document.querySelectorAll(selector)[0];

            return Dex.node(iframe.contentDocument || iframe.contentWindow.document);

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

        Dex.dump = function(object){
            console.log(mutationObservers);
        };

        if(exposeAs){
            window[exposeAs] = Dex;
        }

    })("DexV2");

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

	var localisedStrings = {
		"EN": {
			jobs: "Jobs",
			zeroTasks: "Open Dashboard",
			singleTask: "Dashboard (%n% task)",
			multipleTasks: "Dashboard (%n% tasks)"
		},
		"FR": {
			jobs: "Processus",
			zeroTasks: "Tableau de bord",
			singleTask: "Tableau de bord (%n% Tâche)",
			multipleTasks: "Tableau de bord (%n% Tâches)"
		}
	}


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
			log: function(message, force){
				if(app.dev.data.on || force){
					console.log(message);
				}
			}
		},
        onChange: function(attrKey, attrValue){

            if(app.data.previousModeClass == attrValue){
                return false;
            }

            app.data.previousModeClass = attrValue;

            app.dev.log("::: APP ::: ONCHANGE");

            attrValue.split(" ").forEach(function(cl) {
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
        onChange2: function(mode, arg1, arg2){
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
            if(DexV2.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open"){
                app.edit.sidepanel.close();
            }
		},
		onClick: function(e){
            app.dev.log("CLICKED APP");
			if(DexV2.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open"){
                var inSidePanel = DexV2.node(e.target).closest("#JahiaGxtSidePanelTabs, .edit-menu-sites, .window-side-panel #JahiaGxtRefreshSidePanelButton");

		        if(inSidePanel.nodes.length == 0){
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
                            contextMenuTitle = params.singleSelection.replace("{{node}}", DexV2.getCached("body").getAttribute("data-singleselection-node-displayname"));
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
                    DexV2.node(this).setAttribute("data-indigo-title", returnText);


                },
				onClose: function(){
					app.dev.log("::: APP ::: CONTEXTMENUS ::: MANAGERMENU ::: ONCLOSE");
					// Manager Menu has been closed by clicking on the X.
		            // Can not remove the actual DOM node as it causes problems with GWT, so just hide it instead.
		            DexV2(".menu-editmode-managers-menu").css({
                        "display": "none"
                    });
				}
			},
            previewMenu: {
                onOpen: function(){
					app.dev.log("::: APP ::: CONTEXTMENUS ::: PREVIEWMENU ::: ONOPEN");
                    app.contextMenus.setTitle(this, {
                        noSelection: "Page Preview",
                        singleSelection: "Preview {{node}}",
                        multipleSelection: "Preview selection"
                    });

                }
            },
            publicationMenu: {
                onOpen: function(contextmenu){
					app.dev.log("::: APP ::: CONTEXTMENUS ::: PUBLICATIONMENU ::: ONOPEN");
                    app.contextMenus.setTitle(this, {
                        noSelection: "Publish Page",
                        singleSelection: "Publish {{node}}",
                        multipleSelection: "Publish selection"
                    });

                }
            },
            moreInfoMenu: {
                onOpen: function(){
					app.dev.log("::: APP ::: CONTEXTMENUS ::: MOREINFOMENU ::: ONOPEN");
                    app.contextMenus.setTitle(this, {
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
				anthraciteCSSNode: null
			},
			onToggle: function(e){
				app.dev.log("::: APP ::: THEME ::: ONTOGGLE");
				// Toggle the UI Theme by changing the body attribute accordingly.

	            /* The button firing this event is actually a pseudo element atached to a table.
	            // The tables CSS has been set to ignore all pointer events EXCEPT the pseudo element who accepts pointer events.
	            // This allows us to capture a click on the pseudo element, but we have to check that it a child of the table want the one that was clicked */
				if(DexV2.node(e.target).hasClass("x-toolbar-ct")){
					app.dev.log("CLICKED THEME BUTTON");
					if(app.theme.data.skin == "dark"){
						app.theme.data.skin = "light";
					} else {
						app.theme.data.skin = "dark";
					}

					DexV2.tag("body").setAttribute("data-INDIGO-UI", app.theme.data.skin);

	            }
			},
			on: function(){
				app.dev.log("::: APP ::: THEME ::: ON");

				if(!app.theme.data.enabled){
					// Anthracite CSS has been removed, so plug it back in
                    DexV2.tag("head").append(app.theme.data.anthraciteCSSNode);

					app.theme.data.enabled = true;
				}
			},
			off: function(){
				app.dev.log("::: APP ::: THEME ::: OFF");
				if(app.theme.data.enabled){
					// Remove Anthracite CSS style sheet
                    DexV2.node(app.theme.data.anthraciteCSSNode).remove();

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
				app.dev.log("::: APP ::: PICKER ::: ONOPEN");
				DexV2.getCached("body")
					.setAttribute("data-INDIGO-PICKER-SEARCH", "")
					.setAttribute("data-INDIGO-PICKER", "open")
					.setAttribute("indigo-PICKER-DISPLAY", "thumbs");
			},
			onClose: function(){
				app.dev.log("::: APP ::: PICKER ::: ONCLOSE");
				DexV2.getCached("body").setAttribute("data-INDIGO-PICKER", "");

			},
			onClick: function(){
				app.dev.log("::: APP ::: PICKER ::: ONCLICK");
				DexV2.getCached("body").setAttribute("data-INDIGO-PICKER-SOURCE-PANEL", "");

			},
			onListView: function(){
				app.dev.log("::: APP ::: PICKER ::: ONLISTVIEW");
				DexV2.getCached("body").setAttribute("indigo-PICKER-DISPLAY", "list");

			},
			onThumbView: function(){
				app.dev.log("::: APP ::: PICKER ::: ONTHUMBVIEW");
				DexV2.getCached("body").setAttribute("indigo-PICKER-DISPLAY", "thumbs");

			},
			row: {
				onClick: function(){
					app.dev.log("::: APP ::: PICKER ::: ROW ::: ONCLICK");
					DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

				},
				onMouseOver: function(e){
					app.dev.log("::: APP ::: PICKER ::: ROW ::: ONMOUSEOVER");
					// Position the preview button next to the file whilst hovering
		            app.picker.previewButton.reposition(e, {
		                left: -58,
		                top: 0
		            });

					app.picker.data.currentItem = DexV2.node(this).getNode(0);
					app.picker.data.title = DexV2.node(this).filter(".x-grid3-col-name").getHTML();

					if(DexV2.node(this).hasClass("x-grid3-row-selected")){
						DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

		            } else {
						DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "unselected");

		            }

					DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button", "show");
				},
				onContext: function(e){
					app.dev.log("::: APP ::: PICKER ::: ROW ::: ONCONTEXT");
					// Open Context Menu when clicking "More" button.
					// if matchClass is passed, then the click is ONLY accepted if the clicked element has that class.
					// if matchClass is not passed then it is accepted.
					var acceptClick = DexV2.node(e.target).hasClass("x-tree3-el");

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
					DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

				},
				onMouseOver: function(e){
					app.dev.log("::: APP ::: PICKER ::: THUMB ::: ONMOUSEOVER", e, this);
					// Position the preview button next to the file whilst hovering
		            app.picker.previewButton.reposition(e, {
		                left: -52,
		                top: 0
		            });

					app.picker.data.currentItem = DexV2.node(this).getNode(0);
		            app.picker.data.title = DexV2.node(this).getAttribute("id");

		            if(DexV2.node(this).hasClass("x-view-item-sel")){
		                DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

		            } else {
		                DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "unselected");

		            }

		            DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button", "show");
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
					DexV2.node(app.picker.data.currentItem)
		                .addClass("x-view-over")
		                .addClass("x-grid3-row-over");
				},
				onMouseOut: function(){
					app.dev.log("::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONMOUSEOUT");
					DexV2.node(app.picker.data.currentItem)
		                .removeClass("x-view-over")
		                .removeClass("x-grid3-row-over");
				},
				onClick: function(e, secondClick){
					app.dev.log("::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONCLICK");
					DexV2.node(app.picker.data.currentItem)
						.trigger("mousedown")
						.trigger("mouseup");

		            if(!secondClick){
		                DexV2.id("JahiaGxtImagePopup").remove(); // remove OLD preview
		                $(this).trigger("click", [true]); // Reopen with new preview
		                DexV2("#JahiaGxtImagePopup .x-window-bwrap").setAttribute("data-file-name", app.picker.data.title);

		            }

		            DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button", "hide");
				},
				reposition: function(e, offset){
					app.dev.log("::: APP ::: PICKER ::: PREVIEWBUTTON ::: REPOSITION");
					var offset = offset || {
		                    left: 0,
		                    top: 0
		                },
		                file = e.target,
		                box = file.getBoundingClientRect(),
		                left = box.left,
		                top = box.top,
		                width = box.width;

		            DexV2("#JahiaGxtManagerToolbar .toolbar-item-filepreview")
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
		            if(DexV2.getCached("body").getAttribute("data-indigo-picker-source-panel") != "open"){
		                DexV2("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").addClass("indigo-hover");
		            }
				},
				onMouseOut: function(){
					app.dev.log("::: APP ::: PICKER ::: SOURCE ::: ONMOUSEOUT");
					// USER HAS ROLLED OUT OF THE COMBO TRIGGER
		            DexV2("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");
				},
				close: function(){
					app.dev.log("::: APP ::: PICKER ::: SOURCE ::: CLOSE");
					// CHANGE SOURCE
		            // The user has changed SOURCE, so we just need to hide the combo...
		            DexV2.getCached("body").setAttribute("data-INDIGO-PICKER-SOURCE-PANEL", "");
				},
				open: function(){},
				toggle: function(e){
					app.dev.log("::: APP ::: PICKER ::: SOURCE ::: TOGGLE");
					// USER HAS CLICKED THE COMBO TRIGGER
		            e.stopPropagation();

		            DexV2("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");

                    DexV2.getCached("body").toggleAttribute("data-INDIGO-PICKER-SOURCE-PANEL", ["open", ""]);

				}
			},
			search: {
				open: function(){
					app.dev.log("::: APP ::: PICKER ::: SEARCH ::: OPEN");
					// OPEN SEARCH PANEL

		            // Close source picker if open
		            app.picker.onClose();


		            // Display the search panel
		            DexV2.getCached("body").setAttribute("data-INDIGO-PICKER-SEARCH", "open");

		            // Put the results in LIST mode
		            DexV2("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview").trigger("click");

		            // Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
		            DexV2.id("CRTbrowseTabItem").addClass("x-hide-display");


		            // Remove the directory listing ( gives the search panel an empty start)
		            setTimeout(function(){
		                DexV2("#JahiaGxtManagerTobTable .x-grid3 .x-grid3-row").remove();
		            }, 250);
				},
				close: function(){
					app.dev.log("::: APP ::: PICKER ::: SEARCH ::: CLOSE");
					// CLOSE SEARCH PANEL

		            // Hide the search panel
		            DexV2.getCached("body").setAttribute("data-INDIGO-PICKER-SEARCH", "");

		            // Display the BROWSE panels
		            DexV2("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)").removeClass("x-hide-display");

		            // Get the refresh button
		            var refreshButton = $("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel").not(".x-panel-collapsed").find(".x-tool-refresh")[0];

		            // CLick on the refresh button to reload the content of the directory
					DexV2.node(refreshButton).trigger("click");

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
				DexV2.getCached("body").setAttribute("data-INDIGO-IMAGE-PREVIEW", "open");

				// Attribute used to display the friendly name in edit panel
				DexV2(".engine-panel > div.x-panel-header .x-panel-header-text").setAttribute("data-friendly-name", "nodeDisplayName");
			},
			onClose: function(){
				app.dev.log("::: APP ::: PICKER ::: IMAGEPREVIEW ::: ONCLOSE");
				DexV2.getCached("body").setAttribute("data-INDIGO-IMAGE-PREVIEW", "");

			}
		},
		engine: {
			onOpen: function(){
				app.dev.log("::: APP ::: ENGINE ::: ONOPEN");
				var nodeDisplayName = DexV2.getCached("body").getAttribute("data-singleselection-node-displayname");

				DexV2.getCached("body").setAttribute("data-INDIGO-EDIT-ENGINE", "open");

				// Attribute used to display the friendly name in edit panel
				DexV2(".engine-panel > div.x-panel-header .x-panel-header-text").setAttribute("data-friendly-name", nodeDisplayName);
			},
			onClose: function(){
				app.dev.log("::: APP ::: ENGINE ::: ONCLOSE");
				app.iframe.clearSelection();
				DexV2.getCached("body")
                    .setAttribute("data-INDIGO-EDIT-ENGINE", "")
                    .setAttribute("data-INDIGO-PICKER-SEARCH", "");

			},
		},
		workflow: {
			dashboard: {
				onOpen: function(){
					app.dev.log("::: APP ::: WORKFLOW ::: DASHBOARD ::: ONOPEN");
					DexV2(".workflow-dashboard-engine .x-tool-maximize").trigger("click");

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
			onChangeSRC: function(attrKey, attrValue){
				app.dev.log("::: APP ::: IFRAME ::: ONCHANGESRC [src='" + attrValue + "' ::: currentApp='" + app.data.currentApp + "']");

				app.iframe.data.previousUrl = app.iframe.data.currentUrl;
				app.iframe.data.currentUrl = attrValue;

				if(app.data.currentApp == "edit"){
					// TEMP BLIND

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
			onChange: function(attrKey, attrValue){

				if(app.iframe.data.displayName == attrValue || app.data.currentApp == "studio"){
					return false;
				}

				app.dev.log("::: APP ::: IFRAME ::: ONCHANGE: " + app.data.currentApp);

				app.iframe.data.displayName = attrValue;

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
			onSelect: function(attrKey, attrValue){
                app.dev.log("::: APP ::: IFRAME ::: ONSELECT [attrValue='" + attrValue + "']");

				var count = parseInt(attrValue);

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



			},

			clearSelection: function(){
				app.dev.log("::: APP ::: IFRAME ::: CLEARSELECTION");

				DexV2.class("window-iframe").trigger("click");

			},

			disableClicks: function(){
                app.dev.log("::: APP ::: IFRAME ::: DISABLECLICKS");

                if( DexV2.getCached("body").getAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL") == "yes" &&
                    DexV2.getCached("body").getAttribute("data-sitesettings") == "false"){

	                // SAVE the curent style properties of the iframes body tag so we can revert to it once the side panel is closed.
	                var iframeBody = DexV2.iframe(".window-iframe").filter("body");

	                app.iframe.data.bodyStyle = iframeBody.getAttribute("style") || "";

	                // Remove pointer events from the iframes body, which means that once a user clicks on the iframe to exit the side panel, the content is not automatically selected.
	                iframeBody.setAttribute("style", app.iframe.data.bodyStyle + " pointer-events: none !important");
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
                DexV2.getCached("body")
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
                DexV2.getCached("body")
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

				} else if(DexV2.getCached("body").getAttribute("data-indigo-gwt-panel-tab") == "JahiaGxtSidePanelTabs__JahiaGxtPagesTab"){
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

                    DexV2.getCached("body").setAttribute("data-indigo-infoBar", app.edit.infoBar.data.on);

                },
                tasks: {
					data: {
						classes: null,
						taskCount: 0,
						dashboardButtonLabel: null
					},
                    onChange: function(attrKey, attrValue){

						if(app.edit.infoBar.tasks.data.classes == attrValue){
							return false;
						}

						app.edit.infoBar.tasks.data.classes = attrValue;

						app.dev.log("::: APP ::: EDIT ::: INFOBAR ::: TASKS ::: ONCHANGE");

                        var taskButton = DexV2("." + app.data.currentApp + "-menu-tasks button");;




                        if(taskButton.exists()){
                            var taskString = taskButton.getHTML(),
                            	regexp = /\(([^)]+)\)/,
                                result = taskString.match(regexp),
								taskCount,
								workflowButtonLabel,
								dashboardButtonLabel,
								dashboardButton = DexV2.class("menu-edit-menu-workflow");


                            if(result){
								taskCount = parseInt(result[1]);

                            } else {
                                taskCount = 0;
                            }

							switch(taskCount){
								case 0:
									dashboardButtonLabel = localisedStrings[app.data.UILanguage].zeroTasks;

									break;
								case 1:
									dashboardButtonLabel = localisedStrings[app.data.UILanguage].singleTask.replace("%n%", taskCount);

									break;
								default:
									dashboardButtonLabel = localisedStrings[app.data.UILanguage].multipleTasks.replace("%n%", taskCount);

									break;
							}

							if(taskCount > 9){
								workflowButtonLabel = "+9";

							} else {
								workflowButtonLabel = taskCount;
							}

                            DexV2(".edit-menu-workflow").setAttribute("data-info-count", workflowButtonLabel);
							DexV2(".contribute-menu-workflow").setAttribute("data-info-count", workflowButtonLabel);



							if(dashboardButton.exists()){
								dashboardButton.filter(".toolbar-item-workflowdashboard").setHTML(dashboardButtonLabel);
							}

							app.edit.infoBar.data.taskCount = taskCount;
							app.edit.infoBar.data.workflowButtonLabel = workflowButtonLabel;
							app.edit.infoBar.data.dashboardButtonLabel = dashboardButtonLabel;



                        }
                    },
					updateMenuLabel: function(node){
						DexV2.node(this)
							.filter(".toolbar-item-workflowdashboard")
								.setHTML(app.edit.infoBar.data.dashboardButtonLabel);

					}
                },
                jobs: {
					data: {
						classes: null,
						jobString: null
					},
                    onChange: function(attrKey, attrValue){
						// if(app.edit.infoBar.jobs.data.classes == classes){
						// 	app.dev.log("No change in job classes - ignore");
						// 	return false;
						// }

						app.edit.infoBar.jobs.data.classes = attrValue;

						app.dev.log("::: APP ::: EDIT ::: INFOBAR ::: JOBS ::: ONCHANGE");

                        var jobButton = DexV2(".toolbar-item-workinprogressadmin button");

                        if(jobButton.exists()){
                            var jobStringSplit = jobButton.getHTML().split("<"),
                            	jobString = jobStringSplit[0],
                            	jobIcon = jobButton.filter("img"),
								activeJob,
								buttonParent = DexV2.class("toolbar-item-workinprogressadmin"),
								jobTooltip;

                            if(jobIcon.getAttribute("src").indexOf("workInProgress.png") == -1){
                                // A job is active
								activeJob = true;
								jobTooltip = jobString;
								DexV2(".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(10)").addClass("indigo-job-running");

                            } else {
                                // No Jobs active
								activeJob = false;
								jobTooltip = localisedStrings[app.data.UILanguage].jobs;

								DexV2(".x-viewport-editmode .x-toolbar-first .x-toolbar-cell:nth-child(10)").removeClass("indigo-job-running");

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
								pageTitle = DexV2.getCached("body").getAttribute("data-singleselection-node-displayname");
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
	                    DexV2.getCached("body")
							.setAttribute("data-multiselect", multiselect)
	                    	.setAttribute("data-select-type", selectType);

						// Page Title in Edit Made
                        if(pageTitle){
                            DexV2.class("x-current-page-path").setAttribute("data-PAGE-NAME",pageTitle);

                        }
	                    DexV2.class("node-path-text-inner").setHTML(app.iframe.data.displayName);

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

						if(DexV2.class("x-current-page-path").getAttribute("data-page-name") != null){
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
                                    DexV2(".edit-menu-publication .x-btn-mc").setAttribute("data-publication-label", app.iframe.data.pageTitle);
                                } else {
                                    // No Select
                                    // elements.publishButton.style.left = (boxes.title.left - 20) + "px";
                                    // elements.refreshButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                                    elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 9) + "px";
                                    elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 33) + "px";
                                    elements.nodePathTitle.style.left = (boxes.title.left - 80) + "px";

                                    elements.nodePathTitle.setAttribute("data-indigo-file-path", DexV2.getCached("body").getAttribute("data-main-node-path"));
                                    DexV2(".edit-menu-publication .x-btn-mc").setAttribute("data-publication-label", app.iframe.data.publication.label);
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

					DexV2.getCached("body").setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");
					app.edit.sidepanel.data.open = true;

					// GWT has problems populating the site page tree when the side panel is hidden.
					// Solution: When the side panel is opened for the FIRST TIME ONLY, the refresh button is triggered and the sites page tree is populated correctly.
					if(DexV2.getCached("body").getAttribute("data-sitesettings") == "false"){

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
					if(DexV2.getCached("body").getAttribute("data-edit-window-style") !== "settings" && DexV2.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open" && DexV2.getCached("body").getAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL") == "yes"){
						app.dev.log("::: APP ::: EDIT ::: SIDEPANEL ::: CLOSE");
		                DexV2.getCached("body").setAttribute("data-INDIGO-GWT-SIDE-PANEL", "");

		                // Revert iframes body style attribute to what it was originally
                        DexV2.iframe(".window-iframe").filter("body").setAttribute("style", app.iframe.data.bodyStyle);

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
			            var clickedTabID = DexV2.node(this).getAttribute("id");

						app.edit.sidepanel.data.currentTab = clickedTabID;

						app.dev.log("app.edit.sidepanel.data.currentTab: " + app.edit.sidepanel.data.currentTab);

			            DexV2.getCached("body").setAttribute("data-INDIGO-GWT-PANEL-TAB", clickedTabID);

			            // Menus for the Tabs that call this listener require a normal side panel display
			            // DexV2.getCached("body").setAttribute("data-edit-window-style", "default");

			            var tabMenuActive = DexV2.node(this).hasClass("x-tab-strip-active"),
			                sidePanelOpen = DexV2.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open";

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
			            var acceptClick = DexV2.node(e.target).hasClass("x-grid3-td-displayName");

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
					opened: false,
                    iframeCSSOverRide: ".well{border:none!important; box-shadow: none!important;} body{background-image: none!important; background-color:#f5f5f5!important}"
				},
                onTreeLoad: function(nodeGroup, arg1, arg2){
                    var branch,
                        nodeJoint,
                        firstClickableBranch;

                    for (n = 0;  n < nodeGroup.length; n++){

                        branch = nodeGroup[n];
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
                },
                onTreeChange: function(nodeGroup, arg1, arg2){
                    var JahiaGxtSettingsTab = DexV2.node(this).closest("#JahiaGxtSettingsTab").nodes.length > 0;

                    if(JahiaGxtSettingsTab){
                        var firstBranch = nodeGroup[0],
        					parentBranch = firstBranch.previousSibling,
        					branch,
        					nodeJoint;

                        for (n = 0;  n < nodeGroup.length; n++){

        					branch = nodeGroup[n],
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
                    }


                },
				onChange: function(attrKey, attrValue){

					if(attrValue == "true"){
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

                    var cssStyle = document.createElement("style");

                    cssStyle.type = "text/css";
                    cssStyle.appendChild(document.createTextNode(app.edit.settings.data.iframeCSSOverRide));

                    DexV2.iframe(".window-iframe").filter("head").append(cssStyle);

					if(app.edit.settings.data.opened){
						return false;
					}

					app.edit.settings.data.opened = true;
					DexV2.getCached("body").setAttribute("data-edit-window-style", "settings");

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
                        DexV2("body").onceOpen(".x-grid3-row", app.edit.settings.onTreeLoad); // Once matchType is improved the target selector can be changed to #JahiaGxtSettingsTab .x-grid3-row


		            }



				},
				close: function(){
					var previousEditPage = app.edit.history.get("editpage");

					app.edit.settings.data.opened = false;
					DexV2.getCached("body").setAttribute("data-edit-window-style", "default");

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
                DexV2.getCached("body")
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
                DexV2.getCached("body")
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
                DexV2.getCached("body")
					.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "")
                	.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "yes");
			},
			onClose: function(){},

            onChangeMode: function(attrKey, attrValue){

                if(app.data.currentApp != "contribute"){
                    return false;
                }

                var nodePathSplit = attrValue.split("/"),
                    modePath = nodePathSplit[3],
                    mode,

                    iframeSRC = DexV2.class("window-iframe").getAttribute("src"),
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

                DexV2.getCached("body").setAttribute("data-contribute-mode", app.contribute.data.mode);

                app.dev.log("????????????????????????????????????");
                app.dev.log("CHANGED nodePath: " + attrValue);
                app.dev.log("CHANGED section: " + nodePathSplit[3]);
                app.dev.log("displayingNode: " + displayingNode);
                app.dev.log("????????????????????????????????????");



                app.contribute.data.displayingNode = displayingNode;

                DexV2.getCached("body").setAttribute("data-contribute-displaying-node", app.contribute.data.displayingNode);
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
					// 			pageTitle = DexV2.getCached("body").getAttribute("data-singleselection-node-displayname");
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
	                //     DexV2.getCached("body")
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

                    if(DexV2.class("toolbar-item-publicationstatus").getAttribute("data-page-name") != null){
                            var elements = {
                                body: document.getElementsByTagName("body")[0],
                                title: document.getElementsByClassName("toolbar-item-publicationstatus")[0],
                                // innerTitle: document.getElementsByClassName("node-path-text-inner")[0],
                                publishButton: document.getElementsByClassName("contribute-menu-publication")[0],
                                // refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                                // nodePathTitle: document.getElementsByClassName("node-path-title")[0],
                                previewButton: document.getElementsByClassName("edit-menu-view")[0],
                                editPage: DexV2(".x-toolbar-first .x-toolbar-cell:nth-child(5) table").getNode(0),
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

                            // elements.nodePathTitle.setAttribute("data-indigo-file-path", DexV2.getCached("body").getAttribute("data-main-node-path"));
                            DexV2(".contribute-menu-publication .x-btn-mc").setAttribute("data-publication-label", app.iframe.data.publication.label);


                            // Make sure correct class is added to publication button
                            elements.publishButton.setAttribute("data-publication-status", app.iframe.data.publication.status)
                    }

				}
			}

		}
	}

    // EVENT LISTENERS
    var eventListeners = {
        attach: function(){
			DexV2("body")
                .onceOpen("#JahiaGxtContentBrowseTab", function(){
                    DexV2.node(this).filter(".x-box-item:nth-child(2) .x-grid3-body").addClass("results-column");
                })
    			.onceOpen("#JahiaGxtFileImagesBrowseTab", function(){
    				DexV2.node(this).filter("#images-view > div").addClass("results-column");
    			})
    			.onceOpen("#JahiaGxtCategoryBrowseTab", function(){
    				DexV2.node(this).filter(".x-box-item:nth-child(2) .x-grid3-body").addClass("results-column");
    			})
    			.onceOpen("#JahiaGxtSearchTab", function(){
    				DexV2.node(this).filter(".JahiaGxtSearchTab-results .x-grid3-body").addClass("results-column");
    			})
    			.onceOpen("#JahiaGxtCreateContentTab", function(){
    				DexV2.node(this).filter("input.x-form-text").setAttribute("placeholder", "Filter Content ...")
    			})
                .onOpen(".x-grid3-row", app.edit.settings.onTreeChange) // Once matchType is improved the target selector can be changed to #JahiaGxtSettingsTab .x-grid3-row
    			.onOpen(".x-grid-empty", function(value){
    				if(app.edit.sidepanel.data.open){
                        var isTreeEntry = DexV2.node(this).parent().hasClass("results-column");

    					if(isTreeEntry){
    						if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab"){
    							DexV2.id("JahiaGxtCategoryBrowseTab").removeClass("show-results");

    						} else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab"){
    							DexV2.id("JahiaGxtContentBrowseTab").removeClass("show-results");

    						} else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtSearchTab"){
    							DexV2.id("JahiaGxtSearchTab").removeClass("show-results");

    						}
    						DexV2.getCached("body").removeClass("show-results");
    					}
    				}

    			})
    			.onOpen(".x-grid3-row", function(value){
    				if(app.edit.sidepanel.data.open){
                        var isTreeEntry = DexV2.node(this).parent().hasClass("results-column");

    					if(isTreeEntry){
    						if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab"){
    							DexV2.id("JahiaGxtCategoryBrowseTab").addClass("show-results");

    						} else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab"){
    							DexV2.id("JahiaGxtContentBrowseTab").addClass("show-results");

    						} else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtSearchTab"){
    							DexV2.id("JahiaGxtSearchTab").addClass("show-results");

    						}
    						DexV2.getCached("body").addClass("show-results");
    					}
    				}

    			})
    			.onOpen(".x-clear", function(value){
    				if(app.edit.sidepanel.data.open){
                        var isTreeEntry = DexV2.node(this).parent().hasClass("results-column");

    					if(isTreeEntry){
    						DexV2.id("JahiaGxtFileImagesBrowseTab").removeClass("show-results");
    						DexV2.getCached("body").removeClass("show-results");
    					}
    				}

    			})
    			.onOpen(".thumb-wrap", function(value){
    				if(app.edit.sidepanel.data.open){
                        var isTreeEntry = DexV2.node(this).parent().hasClass("results-column");

    					if(isTreeEntry){
    						DexV2.id("JahiaGxtFileImagesBrowseTab").addClass("show-results");
    						DexV2.getCached("body").addClass("show-results");
    					}
    				}

    			})
                .onOpen(".menu-edit-menu-workflow", app.edit.infoBar.tasks.updateMenuLabel)
    			.onOpen(".menu-contribute-menu-workflow", app.edit.infoBar.tasks.updateMenuLabel)
    			.onOpen(".menu-edit-menu-view", app.contextMenus.previewMenu.onOpen)
                .onOpen(".menu-edit-menu-publication", app.contextMenus.publicationMenu.onOpen)
                .onOpen(".menu-edit-menu-edit", app.contextMenus.moreInfoMenu.onOpen)
                .onOpen(".editModeContextMenu", app.contextMenus.moreInfoMenu.onOpen)
                .onOpen(".menu-editmode-managers-menu", app.contextMenus.managerMenu.onOpen)
                .onOpen("#JahiaGxtContentPickerWindow", app.picker.onOpen)
                .onOpen("#JahiaGxtEnginePanel", app.engine.onOpen)
                .onOpen("#JahiaGxtImagePopup", app.imagePreview.onOpen)
                .onAttribute(".edit-menu-tasks", "class", app.edit.infoBar.tasks.onChange)
                .onAttribute(".contribute-menu-tasks", "class", app.edit.infoBar.tasks.onChange)
                .onAttribute(".toolbar-item-workinprogressadmin", "class", app.edit.infoBar.jobs.onChange)
                .onOpen(".x-dd-drag-proxy", app.edit.sidepanel.onStartDrag)
                .onClose(".x-dd-drag-proxy", app.edit.sidepanel.onStopDrag)
                .onAttribute("body", "data-sitesettings", app.edit.settings.onChange)
                .onAttribute("body", "data-selection-count", app.iframe.onSelect)
                .onAttribute("body", "data-main-node-displayname", app.iframe.onChange)
                .onAttribute("body", "data-main-node-path", app.contribute.onChangeMode)
                .onAttribute(".window-iframe", "src", app.iframe.onChangeSRC)
                .onAttribute(".x-jahia-root", "class", app.onChange)
                .onClose("#JahiaGxtContentPickerWindow", app.picker.onClose)
                .onClose("#JahiaGxtEnginePanel", app.engine.onClose)
                .onClose("#JahiaGxtImagePopup", app.imagePreview.onClose)
                .onOpen(".workflow-dashboard-engine", app.workflow.dashboard.onOpen)
                .onClick(".app-container", app.onClick)
                .onClick(".toolbar-item-filepreview", app.picker.previewButton.onClick)
                .onClick("#JahiaGxtManagerLeftTree + div .x-grid3 .x-grid3-row", app.picker.row.onClick)
                .onClick(".x-viewport-adminmode .x-grid3 .x-grid3-row", function(){
                    DexV2(".x-viewport-adminmode .x-grid3 .x-grid3-row.x-grid3-row-selected").removeClass("x-grid3-row-selected");
                    DexV2.node(this).addClass("x-grid3-row-selected");
                })
                .onClick(".x-grid3-row .x-grid3-td-size", app.picker.search.onContext)
                .onClick(".x-grid3-row .x-tree3-el", app.picker.row.onContext)
                .onClick("#JahiaGxtManagerLeftTree + div .thumb-wrap .thumb", app.picker.thumb.onContext)
                .onClick("#JahiaGxtManagerLeftTree + div .thumb-wrap", app.picker.thumb.onClick)
                // DexV2(".x-viewport-editmode .x-toolbar-first > table").onClick(app.theme.onToggle)
                .onClick(".menu-editmode-managers-menu", app.contextMenus.managerMenu.onClose)
                .onClick("#JahiaGxtSidePanelTabs > .x-tab-panel-header .x-tab-strip-spacer", app.edit.settings.close)
                .onMouseOver(".toolbar-item-filepreview", app.picker.previewButton.onMouseOver)
                .onMouseOut(".toolbar-item-filepreview", app.picker.previewButton.onMouseOut)
                .onMouseDown(".x-tree3-node-joint", function(){
                    DexV2.node(this).closest(".x-grid3-row").toggleClass("indigo-opened");
                })
                .onMouseDown(".menu-edit-menu-mode, .menu-edit-menu-user", app.contextMenus.managerMenu.onClose)
                .onClick("#JahiaGxtSidePanelTabs .x-grid3-td-displayName", app.edit.sidepanel.row.onContext)
                .onClick("#JahiaGxtContentPickerWindow", app.picker.onClick)
                .onClick("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview", app.picker.onListView)
                .onClick("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-thumbsview", app.picker.onThumbView)
                .onClick(".node-path-title", app.iframe.clearSelection)
                .onClick(".x-viewport-editmode #JahiaGxtSidePanelTabs .x-grid3-row", app.edit.onNav)
                .onMouseDown("#JahiaGxtManagerLeftTree__CRTbrowseTabItem", app.picker.search.close)
                .onMouseDown("#JahiaGxtManagerLeftTree__CRTsearchTabItem", app.picker.search.open)
                .onClick("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header", app.picker.source.close)
                .onClick("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.toggle)
                .onMouseEnter("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.onMouseOver)
                .onMouseLeave("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.onMouseOut)
                .onMouseOver("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div .x-grid3-row", app.picker.row.onMouseOver)
                .onMouseOver("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div .thumb-wrap", app.picker.thumb.onMouseOver)
                .onMouseUp("#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab", app.edit.sidepanel.tab.onClick)
                .onMouseUp("#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", app.edit.settings.open)

            // WINDOW LISTENERS
            window.onresize = app.onResize; // Use some kind of timer to reduce repaints / DOM manipulations
            window.addEventListener("blur", app.onBlur);
        }
    }



    // INITIALISE
    var init = function(){
        // Copy Anthracite CSS to remove / add when dropping in and out of STUDIO mode

		var anthraciteCSS_EN = DexV2("link[rel=stylesheet][href$='edit_en.css']").getNode(0),
			anthraciteCSS_FR = DexV2("link[rel=stylesheet][href$='edit_fr.css']").getNode(0);

		if(anthraciteCSS_EN){
			app.data.UILanguage = "EN";
			app.theme.data.cssReference = "edit_en.css";
			app.theme.data.anthraciteCSSNode = anthraciteCSS_EN;

		} else if(anthraciteCSS_FR){
			app.data.UILanguage = "FR";
			app.theme.data.cssReference = "edit_fr.css";
			app.theme.data.anthraciteCSSNode = anthraciteCSS_FR;

		}

		// use Dex to cache an Dex Object
		DexV2("body").cache("body");

        // Attach event listeners
        eventListeners.attach();
    }




    // Start when DOM is ready
    document.addEventListener("DOMContentLoaded", function(event) {
        init();
    });

    // Expose DX to window
    if(exposeAs){
        window[exposeAs] = app;
    }

})("DX");
