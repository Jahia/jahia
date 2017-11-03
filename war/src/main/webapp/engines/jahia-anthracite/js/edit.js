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
                        var mutation_id;

                        for(mutation_id in queue){
                            // Call callback function
                            queue[mutation_id].callback.call(node, arg1, arg2);

                            if(!queue[mutation_id].persistant){
                                delete queue[mutation_id];
                                // queue.splice(n, 1);
                            }

                        }
                    },
                    executeAttributecallbacks = function(queue, node, attrKey, attrValue){
                        var mutation_id;

                        for(mutation_id in queue){
                            // Call callback function
                            if(queue[mutation_id].attrKey == attrKey){
                                queue[mutation_id].callback.call(node, attrKey, attrValue);

                                if(!queue[mutation_id].persistant){
                                    delete queue[mutation_id];
                                    // queue.splice(y, 1);

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
                        groupedCallbacks = mutationObservers[selector].callbacks.onGroupOpen;

                        if(groupedCallbacks){
                            addedNode = mutationRecord.addedNodes[0];

                            if(addedNode.nodeType == 1){
                                for(_target in groupedCallbacks){

                                    modifiedSelector = groupedCallbacks[_target].matchType.modifiedSelector;

                                    // See if addedNode matches the _target of the callback
                                    switch(groupedCallbacks[_target].matchType.type){
                                        case "tag":
                                            if(addedNode.tagName.toUpperCase() == modifiedSelector){
                                                // Loop through all callbacks
                                                executeCallbacks(groupedCallbacks[_target].queue, mutationRecord.addedNodes, addedNode);

                                            }
                                            break;
                                        case "id":
                                            if(addedNode.id == modifiedSelector){
                                                // Loop through all callbacks
                                                executeCallbacks(groupedCallbacks[_target].queue, mutationRecord.addedNodes, addedNode);

                                            }
                                            break;
                                        case "classname":
                                            if(addedNode.classList.contains(modifiedSelector)){
                                                // Loop through all callbacks
                                                executeCallbacks(groupedCallbacks[_target].queue, mutationRecord.addedNodes, addedNode);

                                            }
                                            break;
                                        case "complex":
                                            if(addedNode.matches(modifiedSelector)){
                                                // Loop through all callbacks
                                                executeCallbacks(groupedCallbacks[_target].queue, mutationRecord.addedNodes, addedNode);

                                            }
                                            break;

                                    }

                                }
                            }


                        }


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
                                                    executeCallbacks(callbacks[_target].queue, addedNode, mutationRecord.addedNodes);

                                                }
                                                break;
                                            case "id":
                                                if(addedNode.id == modifiedSelector){
                                                    // Loop through all callbacks
                                                    executeCallbacks(callbacks[_target].queue, addedNode, mutationRecord.addedNodes);

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
                                                    executeCallbacks(callbacks[_target].queue, addedNode, mutationRecord.addedNodes);

                                                }
                                                break;

                                        }

                                    }
                                }

                            }
                        }
                    }

                    if(mutationRecord.removedNodes.length > 0){
						groupedCallbacks = mutationObservers[selector].callbacks.onGroupClose;

                        if(groupedCallbacks){
                            removedNode = mutationRecord.removedNodes[0];

                            if(removedNode.nodeType == 1){
                                for(_target in groupedCallbacks){

                                    modifiedSelector = groupedCallbacks[_target].matchType.modifiedSelector;

                                    // See if removedNode matches the _target of the callback
                                    switch(groupedCallbacks[_target].matchType.type){
                                        case "tag":
                                            if(removedNode.tagName.toUpperCase() == modifiedSelector){
                                                // Loop through all callbacks
                                                executeCallbacks(groupedCallbacks[_target].queue, mutationRecord.removedNodes, removedNode);

                                            }
                                            break;
                                        case "id":
                                            if(removedNode.id == modifiedSelector){
                                                // Loop through all callbacks
                                                executeCallbacks(groupedCallbacks[_target].queue, mutationRecord.removedNodes, removedNode);

                                            }
                                            break;
                                        case "classname":
                                            if(removedNode.classList.contains(modifiedSelector)){
                                                // Loop through all callbacks
                                                executeCallbacks(groupedCallbacks[_target].queue, mutationRecord.removedNodes, removedNode);

                                            }
                                            break;
                                        case "complex":
                                            if(removedNode.matches(modifiedSelector)){
                                                // Loop through all callbacks
                                                executeCallbacks(groupedCallbacks[_target].queue, mutationRecord.removedNodes, removedNode);

                                            }
                                            break;

                                    }

                                }
                            }


                        }

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
            eventListenerCounter = 0;
            mutationCounter = 0,
            generateMutationID = function(){
                mutationCounter ++;

                return "MUTATION_ID_" + mutationCounter;
            },
            generateListenerID = function(){
                eventListenerCounter ++;

                return "EVENT_ID_" + eventListenerCounter;
            },
            createEventListener = function(dexObject, eventType, selector, target, callback, persistant, event_id){
                // Check that the event type exists in queue
                if(!delegatedEventListeners[eventType]){
                    delegatedEventListeners[eventType] = {};
                }

                // Check that this selector is registered
                if(!delegatedEventListeners[eventType][selector]){
                    delegatedEventListeners[eventType][selector] = {};
                }

                // Setup listener
                attachEventListeners(eventType, dexObject);

                // Check that this target is registered
                if(!delegatedEventListeners[eventType][selector][target]){
                    delegatedEventListeners[eventType][selector][target] = [];
                }

                // register delegated event listener
                delegatedEventListeners[eventType][selector][target][event_id || generateListenerID()] = {
                    callback: callback,
                    persistant: persistant
                };

            },
            attachEventListeners = function(eventType, dexObject){

                var n,
                    selector = {
                        string: dexObject.selector
                    };

                for(n = 0; n < dexObject.nodes.length; n++){
                    selector.node = dexObject.nodes[n];

                    if(!selector.node.indigo){
                        selector.node.indigo = {
                            listeners: {}
                        }
                    }

                    if(!selector.node.indigo.listeners[eventType]){
                        // No event listener attached, attach now
                        selector.node.indigo.listeners[eventType] = "on";

                        selector.node.addEventListener(eventType, function (e){
                            var eventHandlers = delegatedEventListeners[eventType][selector.string];

                            for(handlerSelector in eventHandlers){

                                clickedNode = e.target;

                                while(	clickedNode &&
                                        clickedNode !== document &&
                                        clickedNode !== selector.node) { // Stop looking when we hit the node that contains the event listener

                                    if (clickedNode.matches(handlerSelector)) {

                                        for (handlerIndex in eventHandlers[handlerSelector]){
                                            eventHandlers[handlerSelector][handlerIndex].callback.call(clickedNode, e);

                                            if(eventHandlers[handlerSelector] && eventHandlers[handlerSelector][handlerIndex] && !eventHandlers[handlerSelector][handlerIndex].persistant){
                                                delete eventHandlers[handlerSelector][handlerIndex];
                                            }

                                            handlerIndex++;
                                        }

                                    }

                                    clickedNode = clickedNode.parentNode;

                                }
                            }

                        });
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

			prepend: function(node){

                var n;

                for(n = 0; n < this.nodes.length; n++){
                    this.nodes[n].prepend(node);
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

				if(this.nodes[0] && this.nodes[0].parentNode){
					this.nodes = [this.nodes[0].parentNode];
				} else {
					this.nodes = [];
				}


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

			each: function(callback){
				var n;

				for(n = 0; n < this.nodes.length; n++){
					callback.call(this.nodes[n], Dex.node(this.nodes[n]), n);

                }

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

                return (this.nodes[0]) ? this.nodes[0].innerHTML : null;
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


                return (this.nodes[0]) ? this.nodes[0].getAttribute(key) : null;
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

			trigger: function(eventType, xPos, yPos){
                /* Trigger eventType (click, mouseover, etc, ...) on all nodes in nodelist */

				var xPos = xPos || 0,
					yPos = yPos || 0;

                if(this.nodes[0]){
                    var clickEvent = document.createEvent("MouseEvents");

					if(eventType == "contextmenu"){
					    clickEvent.initMouseEvent(eventType, true, false, window,0,0,0,xPos,yPos,false,false,false,false,2,null);
					} else {
						clickEvent.initEvent(eventType, true, true);

					}

                    this.nodes[0].dispatchEvent(clickEvent);
                }

                return this;
            },

			customTrigger: function(eventType, params){

				var params = params || {};

				if(this.nodes[0]){
					var evt = new CustomEvent(eventType, {"bubbles":true, "cancelable":true, "detail": params});

                    this.nodes[0].dispatchEvent(evt);
                }

				return this;

			},

            onOpen: function(target, callback, mutation_id){
                this.onMutation("onOpen", target, callback, {
                    children: false,
                    persistant: true,
                    mutation_id: mutation_id
                });

                return this;
            },

            onceOpen: function(target, callback, mutation_id){
                this.onMutation("onOpen", target, callback, {
                    children: false,
                    persistant: false,
                    mutation_id: mutation_id
                });

                return this;
            },

            onGroupOpen: function(target, callback, mutation_id){
                this.onMutation("onGroupOpen", target, callback, {
                    children: false,
                    persistant: true,
                    mutation_id: mutation_id,
                    groupNodes: true
                });

                return this;
            },

            onceGroupOpen: function(target, callback, mutation_id){
                this.onMutation("onGroupOpen", target, callback, {
                    children: false,
                    persistant: false,
                    mutation_id: mutation_id,
                    groupNodes: true
                });

                return this;
            },

            onClose: function(target, callback, mutation_id){

                this.onMutation("onClose", target, callback, {
                    children: false,
                    persistant: true,
                    mutation_id: mutation_id
                });

                return this;
            },

			onGroupClose: function(target, callback, mutation_id){
                this.onMutation("onGroupClose", target, callback, {
                    children: false,
                    persistant: true,
                    mutation_id: mutation_id,
                    groupNodes: true
                });

                return this;
            },

			onceGroupClose: function(target, callback, mutation_id){
                this.onMutation("onGroupClose", target, callback, {
                    children: false,
                    persistant: false,
                    mutation_id: mutation_id,
                    groupNodes: true
                });

                return this;
            },

            onceClose: function(target, callback, mutation_id){

                this.onMutation("onClose", target, callback, {
                    children: false,
                    persistant: false,
                    mutation_id: mutation_id
                });

                return this;
            },

            onAttribute: function(target, attrKey, callback, mutation_id){
                this.onMutation("onAttribute", target, callback, {
                    attrKey: attrKey,
                    persistant: true,
                    mutation_id: mutation_id
                });

                return this;
            },

            onceAttribute: function(target, attrKey, callback, mutation_id){
                this.onMutation("onAttribute", target, callback, {
                    attrKey: attrKey,
                    persistant: false,
                    mutation_id: mutation_id
                });

                return this;
            },

            onMutation: function(mutationType, target, callback, parameters){
                var selector = this.selector,
                    parentNodes = this.nodes,
                    matchType = function(target){
                        // This needs updating to cater for complex selectors.
                        var result,
                            regexpressions = {
                                id: /^#[a-z]*$/gi,
                                classname: /^\.[a-z]*$/gi,
                                tagname: /^[a-z]*$/gi
                            };

                            if(target.match(regexpressions.id)){
                                // Its a simple ID tag
                                result = {
                                    type: "id",
                                    modifiedSelector: target.slice(1)
                                };


                            } else if(target.match(regexpressions.classname)){
                                // Its a simple ID tag
                                result = {
                                    type: "classname",
                                    modifiedSelector: target.slice(1)
                                }

                            } else if(target.match(regexpressions.tagname)){
                                // Its a simple ID tag
                                result = {
                                    type: "tag",
                                    modifiedSelector: target.toUpperCase()
                                }

                            } else {
                                // Its a simple ID tag
                                result = {
                                    type: "complex",
                                    modifiedSelector: target
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
                mutationObservers[this.selector].callbacks[mutationType][target].queue[parameters.mutation_id || generateListenerID()] = {
                    callback: callback,
                    attrKey: parameters.attrKey,
                    persistant: parameters.persistant,
                    groupNodes: parameters.groupNodes
                };
            },


			onInput: function(target, callback, event_id){
                createEventListener(this, "input", this.selector, target, callback, true, event_id);

                return this;

            },

			onChange: function(target, callback, event_id){
                createEventListener(this, "change", this.selector, target, callback, true, event_id);

                return this;

            },

            onFocus: function(target, callback, event_id){
                createEventListener(this, "focusin", this.selector, target, callback, true, event_id);

                return this;

            },

            onBlur: function(target, callback, event_id){
                createEventListener(this, "focusout", this.selector, target, callback, true, event_id);

                return this;

            },

            onClick: function(target, callback, event_id){
                createEventListener(this, "click", this.selector, target, callback, true, event_id);

                return this;

            },

            oneClick: function(target, callback, event_id){
                createEventListener(this, "click", this.selector, target, callback, false, event_id);

                return this;

            },

            onMouseUp: function(target, callback, event_id){
                createEventListener(this, "mouseup", this.selector, target, callback, true, event_id);

                return this;

            },

            oneMouseUp: function(target, callback, event_id){
                createEventListener(this, "mouseup", this.selector, target, callback, false, event_id);

                return this;

            },

            onMouseDown: function(target, callback, event_id){
                createEventListener(this, "mousedown", this.selector, target, callback, true, event_id);

                return this;

            },

            oneMouseDown: function(target, callback, event_id){
                createEventListener(this, "mousedown", this.selector, target, callback, false, event_id);

                return this;

            },

            onMouseEnter: function(target, callback, event_id){
                createEventListener(this, "mouseenter", this.selector, target, callback, true, event_id);

                return this;

            },

            oneMouseEnter: function(target, callback, event_id){
                createEventListener(this, "mouseenter", this.selector, target, callback, false, event_id);

                return this;

            },

            onMouseLeave: function(target, callback, event_id){
                createEventListener(this, "mouseleave", this.selector, target, callback, true, event_id);

                return this;

            },

            oneMouseLeave: function(target, callback, event_id){
                createEventListener(this, "mouseleave", this.selector, target, callback, false, event_id);

                return this;

            },

            onMouseOver: function(target, callback, event_id){
                createEventListener(this, "mouseover", this.selector, target, callback, true, event_id);

                return this;

            },

            oneMouseOver: function(target, callback, event_id){
                createEventListener(this, "mouseover", this.selector, target, callback, false, event_id);

                return this;

            },

            onMouseOut: function(target, callback, event_id){
                createEventListener(this, "mouseout", this.selector, target, callback, true, event_id);

                return this;

            },

            oneMouseOut: function(target, callback, event_id){
                createEventListener(this, "mouseout", this.selector, target, callback, false, event_id);

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
            console.log(delegatedEventListeners);
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
			multipleTasks: "Dashboard (%n% tasks)",
            workflowType: "%n% Workflow:",
            chooseWorkflowType: "Choose a workflow for %n%:",
            filterField: "Filter",
			sortBy: "Sort By",
			search: "Search %n%",
            languageField: "All Languages",
			fromDate: "From ...",
			toDate: "To ...",
			dateRange: "Date Range",
			dateAnyTime: "Any time",
			dateCustom: "Time range",
			dateCustomLabel: "Time range ...",
			dateType: "By %n% date",
			allMetadata: "All metadata",
			ignoreMetadata: "Ignore metadata",
			metaLabel: "Meta: %n%",
		},
		"FR": {
			jobs: "Processus",
			zeroTasks: "Tableau de bord",
			singleTask: "Tableau de bord (%n% Tâche)",
			multipleTasks: "Tableau de bord (%n% Tâches)",
            workflowType: "%n% Workflow type",
            chooseWorkflowType: "Choisir une workflow pour %n%:",
            filterField: "Filtrer",
			sortBy: "Sort By",
			search: "Rechercher dans %n%",
			languageField: "All Languages",
			fromDate: "From ...",
			toDate: "To ...",
			dateRange: "Date Range ...",
			dateAnyTime: "Any time",
			dateCustom: "Time range",
			dateCustomLabel: "Time range ...",
			dateType: "Par %n%",
			allMetadata: "All metadata",
			ignoreMetadata: "Ignore metadata",
			metaLabel: "Meta: %n%",
		}
	}


	var app = {
		data: {
			currentApp: null,
			currentSite: null,
			previousModeClass: null,
			UILanguage: null,
			startedOnSettingsPage: false,
			startedOnEditPage: true,
			firstApp: null,
            ckeditorVersion: CKEDITOR.version
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
		onChangeNodePath: function(attrKey, attrValue){
			var nodePathSplit = attrValue.split("/"),
				site = nodePathSplit[2];

			if(site && site != app.data.currentSite){
				app.data.currentSite = site;
				app.onChangeSite(site);
			}

		},
		onChangeSite: function(site){
			app.edit.history.reset();

			// Switch to pages view (even though maybe hidden, so that the refresh button relates to pages list)
			DexV2.id("JahiaGxtSidePanelTabs__JahiaGxtPagesTab").trigger("click");

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

            DexV2.getCached("body").setAttribute("data-INDIGO-APP", appID);

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

                    DexV2.getCached("body").setAttribute("data-indigo-hamburger-menu", "open");

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
                    DexV2.getCached("body").setAttribute("data-indigo-hamburger-menu", "");
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
				title: null,
				pickerTitle: null,
				displayType: null,
				previousDisplayType: null,
				hasPreview: null,
				selectedFileCount: 0,
				zooms: {
					thumbs: 3,
					details: 3
				}
			},
			onOpen: function(){
				app.dev.log("::: APP ::: PICKER ::: ONOPEN");

				// Save current view (by default loads on thumbs)
				app.picker.data.displayType = "thumbsview";

				// Position Search button slightly to the right of the picker title
				var pickerTitle = DexV2.id("JahiaGxtContentPickerWindow").filter(".x-window-tl .x-window-header-text"),
					box = pickerTitle.getNode(0).getBoundingClientRect(),
					left = box.left,
					top = box.top,
					width = box.width,
					searchLeftPosition = (left + width + 5);

				DexV2.id("JahiaGxtManagerLeftTree__CRTsearchTabItem").css({
					"left": searchLeftPosition + "px"
				});

				// Save picker title ( for later use in search placeholder )
				app.picker.data.pickerTitle = pickerTitle.getHTML();

				// Create button to toggle the left panel
				var toggleFilesButton = document.createElement("button"),
                    toggleFilesButtonLabel = document.createTextNode("Toggle");

				toggleFilesButton.id = "toggle-picker-files";

				DexV2.id("JahiaGxtContentPickerWindow").prepend(toggleFilesButton);

				// Add placeholders to form elements
				app.picker.setPlaceholders();

				// Set flags for CSS
				DexV2.getCached("body")
					.setAttribute("data-INDIGO-PICKER-SEARCH", "")
					.setAttribute("data-INDIGO-PICKER", "open")
					.setAttribute("indigo-PICKER-DISPLAY", "thumbsview");

				// Reset classes that may have been previously added
				DexV2.id("JahiaGxtContentPickerWindow").removeClass("search-panel-opened");

				// Listen for clicks on toggle button
				DexV2.id("JahiaGxtContentPickerWindow").onClick("#toggle-picker-files", function(){
					DexV2.id("JahiaGxtContentPickerWindow").toggleClass("indigo-collapsed");
				}, "TOGGLE-PICKER-FILES");

				// Listen for changes in slider (input range)
				DexV2.id("JahiaGxtContentPickerWindow").onInput("#thumb-size-slider", function(e){
					var zoomSize = e.target.value;

					// Save zoom level
					app.picker.data.zooms[app.picker.data.displayType] = zoomSize;

					DexV2("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div #images-view .x-view").setAttribute("indigo-thumb-zoom", zoomSize);
				}, "THUMB-SIZE-SLIDER");

				// Fires if multiselect is possible in picker...
				DexV2.tag("body").onOpen("#JahiaGxtManagerBottomTabs", function(){

					// Create a toggle button for multiple selection
					var toggleButton = document.createElement("button"),
						toggleButtonLabel = document.createTextNode("Multiple Selection");

					toggleButton.appendChild(toggleButtonLabel);
					toggleButton.classList.add("toggle-multiple-selection");

					DexV2.id("JahiaGxtManagerBottomTabs").prepend(toggleButton);

					// Add class for CSS
					DexV2.id("JahiaGxtContentPickerWindow").addClass("indigo-picker-multi-select");

					// Listen for files being added to the multiple selection
					DexV2.id("JahiaGxtManagerBottomTabs").onGroupOpen(".x-grid-group", function(groupedNodes){
						app.picker.data.selectedFileCount = this.length;
						app.picker.updateMultipleCount();
					}, "ADDED_FILES_MULTI_SELECT");

					// Listen for files being removed from the multiple selection
					DexV2.id("JahiaGxtManagerBottomTabs").onGroupClose(".x-grid-group", function(groupedNodes){
						// Need to manually count the files in multiple selection ...
						app.picker.data.selectedFileCount = DexV2.id("JahiaGxtManagerBottomTabs").filter(".x-grid-group").nodes.length;
						app.picker.updateMultipleCount();
					}, "REMOVED_FILES_MULTI_SELECT");

					// Listen for clicks on the multiple selection toggle button
					DexV2.id("JahiaGxtManagerBottomTabs").onClick(".toggle-multiple-selection", function(){
						DexV2.id("JahiaGxtManagerBottomTabs").toggleClass("indigo-collapsed");
					}, "TOGGLE_MULTI_SELECT");

				}, "CHECK_FOR_MULTISELECT");

				// See if GWT has included a slider for thumb preview, if so then we can add ours ( which is a GWT replacement )
				var hasSlider = DexV2("#JahiaGxtContentPickerWindow .x-slider").nodes.length > 0;

				if(hasSlider){
					var thumbSlider = document.createElement("input");

					thumbSlider.id = "thumb-size-slider";
					thumbSlider.type = "range";
					thumbSlider.value = 4;
					thumbSlider.min = 1;
					thumbSlider.max = 6;

					DexV2.id("JahiaGxtContentPickerWindow").prepend(thumbSlider);

				}

				// See if GWT has enabled previews for files, if so then set the preview flag to true
				app.picker.data.enablePreviews = DexV2("#JahiaGxtContentPickerWindow .toolbar-item-filepreview").nodes.length > 0;

			},
			updateMultipleCount: function(){
				DexV2.class("toggle-multiple-selection").setHTML("Multiple selection (" + app.picker.data.selectedFileCount + ")");

			},
			updateZoomLevel: function(){

				DexV2.id("thumb-size-slider").nodes[0].value = app.picker.data.zooms[app.picker.data.displayType];
				DexV2("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div #images-view .x-view").setAttribute("indigo-thumb-zoom", app.picker.data.zooms[app.picker.data.displayType]);

			},
			setPlaceholders: function(){
				var filterField = DexV2('#images-view .x-toolbar .x-toolbar-left .x-toolbar-cell:nth-child(2) .x-form-text'),
                    sortBy = DexV2('#images-view .x-toolbar .x-toolbar-left .x-toolbar-cell:nth-child(5) .x-form-text');

                filterField.setAttribute("placeholder", localisedStrings[app.data.UILanguage].filterField);
				sortBy.setAttribute("placeholder", localisedStrings[app.data.UILanguage].sortBy);
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
				DexV2.getCached("body").setAttribute("indigo-PICKER-DISPLAY", "listview");
				app.picker.data.displayType = "listview";


			},
			onThumbView: function(){
				app.dev.log("::: APP ::: PICKER ::: ONTHUMBVIEW");
				DexV2.getCached("body").setAttribute("indigo-PICKER-DISPLAY", "thumbsview");
				app.picker.data.displayType = "thumbsview";
				app.picker.setPlaceholders();
				app.picker.updateZoomLevel();

			},
			onDetailView: function(){
				app.dev.log("::: APP ::: PICKER ::: ONDETAILVIEW");
				DexV2.getCached("body").setAttribute("indigo-PICKER-DISPLAY", "detailedview");
				app.picker.data.displayType = "detailedview";
				app.picker.setPlaceholders();
				app.picker.updateZoomLevel();

			},
			row: {
				onClick: function(){
					app.dev.log("::: APP ::: PICKER ::: ROW ::: ONCLICK");

					DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

				},
				onMouseOver: function(e){
					app.dev.log("::: APP ::: PICKER ::: ROW ::: MOUSEOVER");

					if(app.picker.data.enablePreviews){
						if(DexV2.node(this).filter(".preview-button").nodes.length == 0){
							var previewButton = document.createElement("button"),
			                    previewButtonLabel = document.createTextNode("Preview");

							previewButton.classList.add("preview-button");

							DexV2.node(this).prepend(previewButton);

						}
					}

					if(DexV2.node(this).filter(".more-info-button").nodes.length == 0){
						var moreInfoButton = document.createElement("button"),
							moreInfoButtonLabel = document.createTextNode("More Info");

						moreInfoButton.classList.add("more-info-button");

						DexV2.node(this).prepend(moreInfoButton);

					}

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
						DexV2.node(e.target).trigger("contextmenu", e.pageX, e.pageY);

					}
				}
			},
			thumb: {
				onClick: function(){
					app.dev.log("::: APP ::: PICKER ::: THUMB ::: ONCLICK");
					DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button-state", "selected");

					DexV2.node(this).addClass("x-view-over");

				},
				onMouseOver: function(e){
					app.dev.log("::: APP ::: PICKER ::: THUMB ::: MOUSEOVER");

					if(DexV2.node(this).filter(".thumb .more-info-button").nodes.length == 0){

						var moreInfoButton = document.createElement("button"),
							moreInfoButtonLabel = document.createTextNode("More Info");

						moreInfoButton.classList.add("more-info-button");

						DexV2.node(this).filter(".thumb").prepend(moreInfoButton);

					}

					if(app.picker.data.enablePreviews){
						if(DexV2.node(this).filter(".thumb .preview-button").nodes.length == 0){
							var previewButton = document.createElement("button"),
								previewButtonLabel = document.createTextNode("Preview");

							previewButton.classList.add("preview-button");

							DexV2.node(this).filter(".thumb").prepend(previewButton);

						}
					}

					app.picker.data.currentItem = DexV2.node(this).getNode(0);
		            app.picker.data.title = DexV2.node(this).getAttribute("id");

					if(!DexV2.node(this).hasClass("indigo-force-open")){
						DexV2(".x-view-item-sel.indigo-force-open").removeClass("indigo-force-open");
					}


				},
				onMouseOut: function(){
					DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button", "");
				},
				onContext: function(e){
					var that = this;

					DexV2.node(this).trigger("contextmenu", e.pageX, e.pageY);
					DexV2.node(this).closest(".thumb-wrap").addClass("indigo-force-open");

					DexV2("body").onceClose(".imagepickerContextMenu", function(){
						DexV2.node(that).closest(".thumb-wrap").removeClass("indigo-force-open");

					});
				},
				openPreview: function(){
					DexV2("#JahiaGxtManagerToolbar .toolbar-item-filepreview").trigger("click");
				}

			},
			moreInfoButton: {
				reposition: function(e, offset){
					app.dev.log("::: APP ::: PICKER ::: MOREINFOBUTTON ::: REPOSITION");
					var offset = offset || {
		                    left: 0,
		                    top: 0
		                },
		                file = e.target,
		                box = file.getBoundingClientRect(),
		                left = box.left,
		                top = box.top,
		                width = box.width;

		            DexV2.node(e.target).filter(".more-info-button")
		                .css({
		                    top: (top + (offset.top)) + "px",
		                    left: ((left + width) + offset.left + 5) + "px"
		                })
		                .addClass("indigo-show-button");
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
				onClick: function(e){
					app.dev.log("::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONCLICK");

					if(e.detail.secondClick){
						// Just set the good title
						DexV2("#JahiaGxtImagePopup .x-window-bwrap").setAttribute("data-file-name", app.picker.data.title);

					} else {
						// Need to select the currently hovered thumb first ...
						DexV2.node(app.picker.data.currentItem)
							.trigger("mousedown")
							.trigger("mouseup");

						// Now need to remove the preview ( just incase it is previewing a previously selected thumb)
						DexV2.id("JahiaGxtImagePopup").remove(); // remove OLD preview

						// Reclick on the preview button for the newly selected thumb
						DexV2.node(this).customTrigger("click", {secondClick: true});
					}

		            DexV2.class("toolbar-item-filepreview").setAttribute("indigo-preview-button", "hide");
				},
				reposition: function(node, offset){
					app.dev.log("::: APP ::: PICKER ::: PREVIEWBUTTON ::: REPOSITION");
					var offset = offset || {
		                    left: 0,
		                    top: 0
		                },
		                box = node.getBoundingClientRect(),
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
				setUpScreen: function(){
					// Save the current display time see we can switch back when closing the search panel
					app.picker.data.previousDisplayType = app.picker.data.displayType;

					// Put the results in LIST mode
					if(app.picker.data.displayType == "listview"){
						// Directly remove the listing
						DexV2("#JahiaGxtManagerTobTable .x-grid3 .x-grid3-row").remove();

					} else {
						DexV2("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview").trigger("click");

						// Switch to list view, then remove ...
						DexV2("#JahiaGxtManagerTobTable").onceGroupOpen(".x-grid3 .x-grid3-row", function(){
							DexV2.collection(this).remove();

						});
					}

					// Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
					DexV2.id("CRTbrowseTabItem").addClass("x-hide-display");
					DexV2("#CRTsearchTabItem").removeClass("x-hide-display");

					DexV2.getCached("body").setAttribute("data-INDIGO-PICKER-SEARCH", "open");
					DexV2.id("JahiaGxtContentPickerWindow").addClass("search-panel-opened");

					// Ask for class names ...
					var searchField = DexV2('#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(1) td:nth-child(1) input'),
						languageField = DexV2('#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(2) input'),
						fromDate = DexV2('#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(5) input'),
						toDate = DexV2('#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(6) input'),
						dateRange = DexV2('#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7) input');

					searchField.setAttribute("placeholder", localisedStrings[app.data.UILanguage].search.replace("%n%", app.picker.data.pickerTitle));
					// searchField.getNode(0).value = "";
					languageField.setAttribute("placeholder", localisedStrings[app.data.UILanguage].languageField);

					fromDate.setAttribute("placeholder", localisedStrings[app.data.UILanguage].fromDate);
					toDate.setAttribute("placeholder", localisedStrings[app.data.UILanguage].toDate);
					dateRange.setAttribute("placeholder", localisedStrings[app.data.UILanguage].dateAnyTime);

					// Callback when user opens Date Range context menu ...
					DexV2('#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7)').oneClick("img", function(){
						var alreadyAddedButtons = DexV2(".x-combo-list").hasClass("indigo-buttons");

						if(!alreadyAddedButtons){
							var anyTimeButton = document.createElement("span"),
								customRangeButton = document.createElement("span");

							anyTimeButton.innerHTML = localisedStrings[app.data.UILanguage].dateAnyTime;
							anyTimeButton.classList.add("search-anytime-button");
							anyTimeButton.classList.add("x-combo-list-item");

							customRangeButton.innerHTML = localisedStrings[app.data.UILanguage].dateCustomLabel;
							customRangeButton.classList.add("search-custom-date-range-button");
							customRangeButton.classList.add("x-combo-list-item");

							DexV2(".x-combo-list")
								// Add Two new buttons to the context menu ...
								.prepend(anyTimeButton)
								.append(customRangeButton)
								.addClass("indigo-buttons");
						}

						DexV2(".x-combo-list")
							// Clicked on a Normal Date Filter ( ie. 1 day, 2 days, etc )
							.onMouseDown(".x-combo-list-item", function(){
								DexV2.id("JahiaGxtContentPickerWindow").setAttribute("data-indigo-search-date", "simple");

							}, "PREDEFINED_DATE_RANGE")

							// Clicked on the custom date range button
							.onMouseDown(".search-custom-date-range-button", function(){
								DexV2.id("JahiaGxtContentPickerWindow").setAttribute("data-indigo-search-date", "custom");

								dateRange.setAttribute("placeholder", localisedStrings[app.data.UILanguage].dateCustom);

								// Close the context menu by trigger clicking the page
								DexV2("#JahiaGxtContentPickerWindow").trigger("mousedown").trigger("mouseup");

							}, "CUSTOM_DATE_RANGE")
							.onMouseDown(".search-anytime-button", function(){
								// Clicked on Any TIme ( removes times filter )

								dateRange.setAttribute("placeholder", localisedStrings[app.data.UILanguage].dateAnyTime);
								DexV2.id("JahiaGxtContentPickerWindow").setAttribute("data-indigo-search-date", "");

								// Close the context menu by trigger clicking the page
								DexV2("#JahiaGxtContentPickerWindow").trigger("mousedown").trigger("mouseup");
							}, "ANY_TIME");




					}, "SEARCH_PANEL_DATE_RANGE_BUTTON");

					DexV2.id("JahiaGxtContentPickerWindow")
						// Listen for changes to meta tags ...
						.onClick("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)", app.picker.search.updateMetaLabel, "CHANGE_META_FILTER")

						// Listen for changes to modification type ...
						.onClick("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) .x-form-check-wrap", app.picker.search.updateModificationLabel, "CHANGE_MODIFICATION_FILTER")

						// Toggle modification menu when clicking ...
						.onClick("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) > label", app.picker.search.toggleModificationMenu, "TOGGLE_MODIFICATION_FILTER");

					// Trigger clicks to initiate the labels of Modification and Meta
					DexV2.id("CRTsearchTabItem").filter(".x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)").trigger("click");
					DexV2.id("CRTsearchTabItem").filter(".x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) input[type=radio]:checked").trigger("click");

				},
				open: function(){
					// OPEN SEARCH PANEL
					app.dev.log("::: APP ::: PICKER ::: SEARCH ::: OPEN");

					var searchTabAvailable = DexV2.id("CRTsearchTabItem").exists();

					if(searchTabAvailable){
						app.picker.search.setUpScreen();

					} else {
						DexV2.tag("body").onOpen("#CRTsearchTabItem", app.picker.search.setUpScreen);

					}

				},
				toggleModificationMenu: function(){
					var menu = DexV2.node(this).parent();

					menu.toggleClass("indigo-show-menu");
				},
				updateModificationLabel: function(){
					var dateType = DexV2.node(this).filter("label").getHTML(),
						label = localisedStrings[app.data.UILanguage].dateType.replace("%n%", dateType);

					DexV2.node(this).closest(".x-form-item")
						.removeClass("indigo-show-menu")
						.setAttribute("data-indigo-modification-label", label);
				},
				updateMetaLabel: function(){
					var checkboxes = DexV2.node(this).filter(".x-form-check-wrap:not(.x-hide-display) input[type='checkbox']"),
						selMeta = [],
						checkboxCount = checkboxes.nodes.length,
						metaMenuLabel;

					checkboxes.each(function(checkbox){
						var checkboxLabel = this.nextSibling.innerHTML;

						if(this.checked == true){
							// Its checked, so add to string ...
							selMeta.push(checkboxLabel);
						}
					});

					if(selMeta.length == checkboxCount){
						// ALL meta data
						metaMenuLabel = localisedStrings[app.data.UILanguage].allMetadata;

					} else if(selMeta.length == 0){
						metaMenuLabel = localisedStrings[app.data.UILanguage].ignoreMetadata;
					} else {
						metaMenuLabel = localisedStrings[app.data.UILanguage].metaLabel.replace("%n%", selMeta.join(", "))

					}

					DexV2.node(this).setAttribute("data-indigo-meta-label", metaMenuLabel);
				},
				close: function(){
					app.dev.log("::: APP ::: PICKER ::: SEARCH ::: CLOSE");
					// CLOSE SEARCH PANEL

		            // Hide the search panel
		            DexV2.getCached("body").setAttribute("data-INDIGO-PICKER-SEARCH", "");
					DexV2.id("JahiaGxtContentPickerWindow").removeClass("search-panel-opened");

					DexV2.id("CRTsearchTabItem").addClass("x-hide-display");


		            // Display the BROWSE panels
		            DexV2("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)").removeClass("x-hide-display");

					// // Put the results in previous mode
					DexV2("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-" + app.picker.data.previousDisplayType).trigger("click");


					// CLick on the refresh button to reload the content of the directory
		            DexV2.id("JahiaGxtContentPickerWindow").filter("#JahiaGxtManagerLeftTree .x-panel:not(.x-panel-collapsed) .x-tool-refresh").trigger("click");

				},

				onContext: function(e){
					app.dev.log("::: APP ::: PICKER ::: SEARCH ::: ONCONTEXT");
					// Open Context Menu when clicking "More" button.
					DexV2.node(e.target).trigger("contextmenu", e.pageX, e.pageY);

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
            onOpenWorkflow: function(){
                // Used to prefix the labels with the name of the Selected workflows ...
                DexV2.node(this).onClick(".x-grid3-row", function(){
                    var label = DexV2.node(this).filter(".x-grid3-col-displayName").getHTML(),
                        localisedLabel = localisedStrings[app.data.UILanguage].workflowType.replace("%n%", label),
                        localisedChooseLabel = localisedStrings[app.data.UILanguage].chooseWorkflowType.replace("%n%", label);

                    // Update labels
                    DexV2("#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) > .x-panel form > div:nth-child(1)").setAttribute("data-indigo-workflow-type", localisedLabel);
                    DexV2("#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) > .x-panel .x-form-field-wrap").setAttribute("data-indigo-workflow-type", localisedChooseLabel);

                }, "CHANGE_WORKFLOW_TYPE");

                // Init by clicking first worflow item
                DexV2.node(this).onceOpen(".x-grid3-row", function(){
                    DexV2.node(this).trigger("click");

                })
            },
            closeConditionEditor: function(){
                DexV2("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3)").removeClass("indigo-show");
            },
            editCondition: function(){
                DexV2("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3)").addClass("indigo-show");
                DexV2("body").setAttribute("data-indigo-add-visibility-condition", "");

                // Create menu ...
                var newMenu = document.createElement("menu"),
                    doneButton = document.createElement("button"),
                    doneButtonLabel = document.createTextNode("Done");

                DexV2.node(doneButton).addClass("done-with-condition");

                doneButton.appendChild(doneButtonLabel);
                newMenu.appendChild(doneButton);

                DexV2("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3) .x-panel-footer")
                    .setHTML("")
                    .append(newMenu);


            },
            addCondition: function(){
                DexV2("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3)").addClass("indigo-show");

                DexV2("body").onceOpen("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3) .x-panel-footer", function(){
                    var newMenu = document.createElement("menu"),
                        saveButton = document.createElement("button"),
                        saveButtonLabel = document.createTextNode("Create"),
                        closeButton = document.createElement("button"),
                        closeButtonLabel = document.createTextNode("Cancel");

                    DexV2.node(saveButton).addClass("save-new-condition");
                    DexV2.node(closeButton).addClass("cancel-new-condition");

                    closeButton.appendChild(closeButtonLabel);
                    saveButton.appendChild(saveButtonLabel);
                    newMenu.appendChild(saveButton);
                    newMenu.appendChild(closeButton);

                    DexV2("body").oneClick("#JahiaGxtEditEnginePanel-visibility .cancel-new-condition", function(){

                        // DEV NOTE ::: Get rid of this timeout
                        setTimeout(function(){
                            DexV2.id("JahiaGxtEditEnginePanel-visibility").filter(".x-grid3-row.x-grid3-row-selected .x-grid3-col-remove > table .x-btn-small").trigger("click");
                        }, 5);

                    });

                    DexV2("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3) .x-panel-footer")
                        .setHTML("")
                        .append(newMenu);

                });

            },
            openConditionsMenu: function(){
                DexV2("body").onceOpen(".x-combo-list", function(nodes){

                    DexV2("body").setAttribute("data-indigo-add-visibility-condition", "new");

                    DexV2("body").oneMouseDown(".x-combo-list-item", function(){

                        // DEV NOTE ::: Get rid of this timeout
                        setTimeout(function(){
                            DexV2("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(1) > .x-component:nth-child(2) > table > tbody > tr > td:nth-child(5) > table").trigger("click");
                        }, 5);

                    })

                });

				DexV2("body").onceClose(".x-combo-list", function(){
					// Remove class that modifies the set up for this context menu
					DexV2("body").setAttribute("data-indigo-add-visibility-condition", "");

				});
            }
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

                    case "dashboard":
                        app.dashboard.onChange();

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

                if( DexV2.getCached("body").getAttribute("data-indigo-gwt-side-panel") == "open" &&
                    DexV2.getCached("body").getAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL") == "yes" &&
                    DexV2.getCached("body").getAttribute("data-sitesettings") == "false"){

	                // SAVE the curent style properties of the iframes body tag so we can revert to it once the side panel is closed.
	                var iframeBody = DexV2.iframe(".window-iframe").filter("body");

                    iframeBody.nodes[0].style.pointerEvents = "none";
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
        pickers: {
            users: {
                data: {
                    closeInterval: null
                },
                onInput: function(){
                    // If search text field is empty, remove the clear button

                    if(this.value == ""){
                        DexV2.class("indigo-clear-button").addClass("indigo-empty-field");

                    } else {
                        DexV2.class("indigo-clear-button").removeClass("indigo-empty-field");
                    }
                },
                onInputFocus: function(){
                    // Expand the search field
                    DexV2.class("indigo-search-component").addClass("indigo-show");

                    // If the search input value is empty add the clear button
                    if(this.value == ""){
                        DexV2.class("indigo-clear-button").addClass("indigo-empty-field");

                    }
                },
                onInputBlur: function(){
                    // Close the search panel ( if the search input field is empty )
                    var searchString = this.value;

                    if(searchString == ""){
                        // The value of the search input is empty, so close.
                        // However, do not close immediately - because the user may actually be clicking the clear button, so...
                        // Use an interval timer that closes the search panel after a split second. If the user clicks on the clear button
                        //   before this time has passed , we can cancel the interval and therefore keep the panel open

                        app.pickers.users.data.closeInterval = setInterval(function(){
                            DexV2.class("indigo-search-component").removeClass("indigo-show");

                            clearInterval(app.pickers.users.data.closeInterval);
                        }, 150);

                    } else {

                    }

                },
                clearSearch: function(){
                    // User has clicked the clear button

                    // Clear the timer (if any ) that hides the search panel
                    clearInterval(app.pickers.users.data.closeInterval);

                    // Get elements
                    var searchInput = DexV2.id("JahiaGxtUserGroupSelect").filter(".indigo-search-input").nodes[0],
                        searchButton = DexV2.id("JahiaGxtUserGroupSelect").filter(".indigo-search-button > table");

                    // Set classes for display
                    DexV2.class("indigo-clear-button").addClass("indigo-empty-field");
                    DexV2.class("indigo-search-component").addClass("indigo-show");

                    // Set the value of the text field to empty string
                    searchInput.value = "";

                    // Trigger search on empty field to reset the results ( need to wait a split second )
                    setTimeout(function(){
                        searchButton.trigger("click");
                        searchInput.focus();
                    }, 100);


                },
                onOpen: function(){
                    DexV2.node(this).filter(".x-panel-tbar .x-toolbar-left > table").addClass("indigo-search-component");

                    DexV2.class("indigo-search-component").filter(".x-toolbar-cell:nth-child(1)").addClass("indigo-clear-button");
                    DexV2.class("indigo-search-component").filter(".x-toolbar-cell:nth-child(2) input").addClass("indigo-search-input");
                    DexV2.class("indigo-search-component").filter(".x-toolbar-cell:nth-child(3)").addClass("indigo-search-button");

                    DexV2.class("indigo-search-component")
                        .onInput(".indigo-search-input", app.pickers.users.onInput, "INDIGO-SEARCH-COMPONENT")
                        .onClick(".indigo-clear-button", app.pickers.users.clearSearch, "INDIGO-SEARCH-COMPONENT")
                        .onFocus(".indigo-search-input", app.pickers.users.onInputFocus, "INDIGO-SEARCH-COMPONENT")
                        .onBlur(".indigo-search-input", app.pickers.users.onInputBlur, "INDIGO-SEARCH-COMPONENT");

					// Remove the clear button on init
					DexV2.class("indigo-clear-button").addClass("indigo-empty-field");

                    DexV2.id("JahiaGxtUserGroupSelect")
                        .onOpen(".x-grid-empty", function(){

                            DexV2.id("JahiaGxtUserGroupSelect").addClass("indigo-no-results");

                        }, "INDIGO-SEARCH-COMPONENT")
                        .onClose(".x-grid-empty", function(){

                            DexV2.id("JahiaGxtUserGroupSelect").removeClass("indigo-no-results");

                        }, "INDIGO-SEARCH-COMPONENT")

                }
            }
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

					var keepCheckingForEmpties = true;

					// Set CSS to open side panel
					DexV2.getCached("body").setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");
					app.edit.sidepanel.data.open = true;

					// Check if there are any empty rows, if so then refresh the panel
					DexV2.id("JahiaGxtPagesTab").filter(".x-grid3-row").each(function(dexObject, index){
						if(keepCheckingForEmpties){
							if(dexObject.getHTML() == ""){
								keepCheckingForEmpties = false;
								DexV2.id("JahiaGxtRefreshSidePanelButton").trigger("click");
							}
						}

					});

                    if(!isSettings){
                        // Disable clicks
                        app.iframe.disableClicks();
                    }

				},
				close: function(){
					if(DexV2.getCached("body").getAttribute("data-edit-window-style") !== "settings" && DexV2.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open" && DexV2.getCached("body").getAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL") == "yes"){
						app.dev.log("::: APP ::: EDIT ::: SIDEPANEL ::: CLOSE");
		                DexV2.getCached("body").setAttribute("data-INDIGO-GWT-SIDE-PANEL", "");

		                // Revert iframes body style attribute to what it was originally
                        DexV2.iframe(".window-iframe").filter("body").nodes[0].style.pointerEvents = "all";
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
							DexV2.node(e.target).trigger("contextmenu", e.pageX, e.pageY);
			            }
					}
				}
			},

			settings: {
				data: {
					opened: false,
					firstRun: true,
                    iframeCSSOverRide: ".well{border:none!important; box-shadow: none!important;} body{background-image: none!important; background-color:#f5f5f5!important}"
				},
                onTreeLoad: function(nodeGroup, arg1, arg2){
					DexV2.node(this)
						.trigger("mousedown")
						.trigger("click");

                },
                onTreeChange: function(firstNode, arg1, arg2){
                    var nodeGroup = this,
                        firstBranch = firstNode,
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


                },
				onChange: function(attrKey, attrValue){
                    app.dev.log("::: APP ::: SETTINGS ::: EDIT ::: SETTINGS ::: ONCHANGE");
					if(attrKey == "data-sitesettings" && attrValue == "true"){
						if(app.data.currentApp == "edit"){
							app.edit.settings.open(null, "directAccess");

						}


					}
				},
				open:function(e, directAccess){
                    app.dev.log("::: APP ::: EDIT ::: SETTINGS ::: OPEN");

					if(directAccess){
						// Need to set back to the settings tree list (the onChangeSite will have switched it back to pages)
						DexV2.id("JahiaGxtSidePanelTabs__JahiaGxtSettingsTab").trigger("click");
					}

					// Setup CSS to display page with settings style
					app.edit.settings.data.opened = true;
					app.edit.sidepanel.data.open = true;
					DexV2.getCached("body").setAttribute("data-edit-window-style", "settings");
					DexV2.getCached("body").setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");

					if(directAccess){
						// Settings page was opened directly ( no passing via edit mode )

						// Find the selected page in the tree by looking for the added class x-grid3-row-selected
						DexV2.id("JahiaGxtSettingsTab").onAttribute(".x-grid3-row", "class", function(){
							// DEV NOTE ::: Need to add a way of killing a listener when it is no longer needed

							if(!app.edit.history.get("settingspage")){
								if(DexV2.node(this).hasClass("x-grid3-row-selected")) {
									// Save this page as the currently selected settings page
									app.edit.history.add("settingspage", this);
								}
							}

						});

					} else {
						// User has opened the settings from the edit mode
						if(app.edit.history.get("settingspage")){
							// There is already a settings page in the history, so select it
							DexV2.node(app.edit.history.get("settingspage")).trigger("mousedown").trigger("click");

						} else {
							// Could not find a previously selected settings page - this is a first run (or edit page has been changed)

                            // DEV NOTE :: DONT LIKE HAVING TO WAIT ...
                            setTimeout(function(){
                                DexV2.id("JahiaGxtRefreshSidePanelButton").trigger("click");
                            }, 50);

							// Listen for first settings page to be added to tree, select it then stop listening.
							DexV2("#JahiaGxtSettingsTab").onceOpen(".x-grid3-row", function(nodeGroup){
                                // DEV NOTE ::: May need to loop through nodeGroup to get first actual available PAGE (ie. not a folder)
								DexV2.node(this)
									.trigger("mousedown")
									.trigger("click");
							});
						}
					}

				},
				close: function(){
					var previousEditPage = app.edit.history.get("editpage");

					app.edit.settings.data.opened = false;
					DexV2.getCached("body").setAttribute("data-edit-window-style", "default");

		            app.edit.sidepanel.close();

		            if(previousEditPage && document.body.contains(previousEditPage)){
		                // Trigger click on last viewed settings page
						DexV2.node(previousEditPage)
							.trigger("mousedown")
							.trigger("mouseup");
		            } else {
						// Trigger Click on Second page (first row is not an actual page)
						// Need to set side panel tab as pages to allow the capture of the click to save the page in history...
						DexV2.getCached("body").setAttribute("data-indigo-gwt-panel-tab", "JahiaGxtSidePanelTabs__JahiaGxtPagesTab");

						DexV2("#JahiaGxtPagesTab .x-grid3-row:nth-child(2)")
							.trigger("mousedown")
							.trigger("mouseup");

		            }
				}
			}

		},
		dashboard: {
			// Event Handlers
            data: {
                beta: false
            },
            onOpen: function(){
                app.dev.log("::: APP ::: DASHBOARD ::: OPENED");

				// Use Anthracite CSS
				app.theme.on();

                // Set attributes to be used by CSS
                DexV2.getCached("body")
					.setAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL", "no")
                	.setAttribute("data-INDIGO-GWT-SIDE-PANEL", "open");


            },
            onChange: function(){
                if(app.dashboard.data.beta){
                    DexV2.getCached("body").setAttribute("data-INDIGO-BETA", "true");
                    app.dashboard.setBetaStyle();

                } else {
                    DexV2.getCached("body").setAttribute("data-INDIGO-BETA", "");

                }

            },
            onClose: function(){},
            setBetaStyle: function(){
                // DexV2.getCached("body").setAttribute("data-indigo-hamburger-menu", "");
				//
                // var iframeHead = DexV2.iframe(".window-iframe").filter("head"),
                //     iframeBody = DexV2.iframe(".window-iframe").filter("body");
				//
                // $("body").append("<div id='dashboard_preview_window_holder'><iframe id='dashboard_preview_window' src='http://localhost:8080/cms/render/default/en/sites/digitall/home.html'></iframe></div>");
				//
				//
                // var newCSS = document.createElement('link');
                //     newCSS.rel = "stylesheet";
                //     newCSS.type = "text/css";
                //     newCSS.href = "/engines/jahia-anthracite/_dashboard-import.css";
				//
                // iframeHead.append(newCSS);
				//
                // iframeBody.onClick("#userSites_table tr", function(){
                //     var tr = this,
                //     siteInput = DexV2.node(tr).filter("td:nth-child(1) input"),
                //     siteName = siteInput.getAttribute("name"),
				//
                //     previewAnchor = DexV2.node(tr).filter("td:nth-child(5) a"),
                //     url = previewAnchor.getAttribute("href");
				//
                //     DexV2.node(this).parent().filter("tr").removeClass("indigo-selected");
                //     DexV2.node(this).addClass("indigo-selected");
				//
                //     DexV2.id("dashboard_preview_window").setAttribute("src", url)
                // });
            }

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
				.onClick(".toolbar-item-newfolder", function(){
					// Add new folder
					var isDisabled = DexV2.node(this).hasClass("x-item-disabled");

					if(isDisabled){
						// User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
						DexV2("#CRTbrowseTabItem .x-grid3-row-selected").trigger("contextmenu");

						// When context menu is opened click on the ADD FOLDER button
						DexV2("body").onceOpen(".imagepickerContextMenu", function(){
							DexV2(".imagepickerContextMenu .toolbar-item-newfolder").trigger("click");

						})

					} else {
						console.log("Nothing to do ...");
					}
				})
				.onClick(".toolbar-item-upload", function(){
					// Add new folder
					var isDisabled = DexV2.node(this).hasClass("x-item-disabled");

					if(isDisabled){
						// User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
						DexV2("#CRTbrowseTabItem .x-grid3-row-selected").trigger("contextmenu");

						// When context menu is opened click on the ADD FOLDER button
						DexV2("body").onceOpen(".imagepickerContextMenu", function(){
							DexV2(".imagepickerContextMenu .toolbar-item-upload").trigger("click");

						})

					} else {
						console.log("Nothing to do ...");
					}
				})
				.onAttribute("body", "data-singleselection-node-path", app.onChangeNodePath)
				.onOpen("#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) .x-grid-panel", app.engine.onOpenWorkflow)
                .onOpen("#JahiaGxtUserGroupSelect", app.pickers.users.onOpen)
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
                .onGroupOpen("#JahiaGxtSettingsTab .x-grid3-row", app.edit.settings.onTreeChange) // Once matchType is improved the target selector can be changed to #JahiaGxtSettingsTab .x-grid3-row
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
    			.onClose(".thumb-wrap", function(value){
    				if(app.edit.sidepanel.data.open){
                        DexV2.id("JahiaGxtFileImagesBrowseTab").removeClass("show-results");
                        DexV2.getCached("body").removeClass("show-results");
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
				.onClick("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3) .x-panel-footer", app.engine.closeConditionEditor)
				.onClick("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(2) .x-grid3-row", app.engine.editCondition)
				.onClick("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(1) > .x-component:nth-child(2) td:nth-child(5) > table", app.engine.addCondition)
				.onMouseDown("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(1) img.x-form-trigger", app.engine.openConditionsMenu)
                .onClick(".x-grid3-row .x-grid3-td-size", app.picker.search.onContext)
                .onClick(".x-grid3-row .x-tree3-el", app.picker.row.onContext)
				.onMouseDown("#JahiaGxtContentPickerWindow .more-info-button", app.picker.thumb.onContext)
                .onMouseDown("#JahiaGxtContentPickerWindow .preview-button", app.picker.thumb.openPreview)

                .onClick("#JahiaGxtManagerLeftTree + div .thumb-wrap", app.picker.thumb.onClick)
                // DexV2(".x-viewport-editmode .x-toolbar-first > table").onClick(app.theme.onToggle)
                .onClick(".menu-editmode-managers-menu", app.contextMenus.managerMenu.onClose)
                .onClick("#JahiaGxtSidePanelTabs > .x-tab-panel-header .x-tab-strip-spacer", app.edit.settings.close)
                .onMouseOver(".toolbar-item-filepreview", app.picker.previewButton.onMouseOver)
                .onMouseOut(".toolbar-item-filepreview", app.picker.previewButton.onMouseOut)
                .onMouseDown(".x-tree3-node-joint", function(){
                    DexV2.node(this).closest(".x-grid3-row").toggleClass("indigo-opened");
                })
				.onMouseDown(".menu-edit-menu-mode", app.contextMenus.managerMenu.onClose)
                .onMouseDown(".menu-edit-menu-user", app.contextMenus.managerMenu.onClose)
                .onClick("#JahiaGxtSidePanelTabs .x-grid3-td-displayName", app.edit.sidepanel.row.onContext)
                .onClick("#JahiaGxtContentPickerWindow", app.picker.onClick)
                .onClick("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-listview", app.picker.onListView)
				.onClick("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-thumbsview", app.picker.onThumbView)
                .onClick("#JahiaGxtContentPickerWindow .x-panel-tbar .action-bar-tool-item.toolbar-item-detailedview", app.picker.onDetailView)
                .onClick(".node-path-title", app.iframe.clearSelection)
                .onMouseDown(".x-viewport-editmode #JahiaGxtSidePanelTabs .x-grid3-row", app.edit.onNav)
                .onMouseDown("#JahiaGxtManagerLeftTree__CRTbrowseTabItem", app.picker.search.close)
                .onMouseUp("#JahiaGxtManagerLeftTree__CRTsearchTabItem", app.picker.search.open)
                .onClick("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-panel-header", app.picker.source.close)
                .onClick("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.toggle)
                .onMouseEnter("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.onMouseOver)
                .onMouseLeave("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.onMouseOut)
				.onMouseOver("#JahiaGxtContentPickerWindow #JahiaGxtManagerTobTable .x-grid3-row", app.picker.row.onMouseOver)
				.onMouseOver("#JahiaGxtContentPickerWindow #JahiaGxtManagerTobTable .thumb-wrap", app.picker.thumb.onMouseOver)
                // .onMouseEnter("#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div #images-view .x-view", app.picker.thumb.onMouseOut)
                .onMouseUp("#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab", app.edit.sidepanel.tab.onClick)
                .onMouseUp("#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", app.edit.settings.open)

//

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

		if(DexV2.id("editmode").getAttribute("template") == "default"){
			app.data.firstApp = "edit"; // edit or contribute
		} else {
			app.data.firstApp = "settings"; // settings / admin

		}

		// use Dex to cache an Dex Object
		DexV2("body").cache("body");

        DexV2.getCached("body").setAttribute("data-CKEDITOR-VERSION", app.data.ckeditorVersion);

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
