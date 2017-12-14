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

                    if (this.nodes[n] && this.nodes[n].parentNode) {
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
					if(this.nodes[n]){
						this.nodes[n].classList.remove(classname);

					}
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
                        queue: {}
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
        "DE": {
			autoRefresh: "automatische Aktualisierung",
            jobs: "Aufgaben",
            zeroTasks: "Dashboard öffnen",
            singleTask: "Dashboard (%n% Aufgabe)",
            multipleTasks: "Dashboard (%n% Aufgaben)",
            workflowType: "%n% Workflow:",
            chooseWorkflowType: "Wählen Sie einen Workflow für %n%:",
            filterField: "Filter",
            sortBy: "Sortieren nach",
            search: "Suche %n%",
            languageField: "Alle Sprachen",
            fromDate: "Von ...",
            toDate: "bis ...",
            dateRange: "Datumsbereich",
            dateAnyTime: "Jederzeit",
            dateCustom: "Zeitspanne",
            dateCustomLabel: "Zeitspanne ...",
            dateType: "Nach %n% Datum",
            allMetadata: "Alle Metadaten",
            ignoreMetadata: "Metadaten ignorieren",
            metaLabel: "Meta: %n%",
            cancel: "Abbrechen",
            backgroundJobs: "Hintergrund-Tasks",
            filterContent: "Content filtern",
			pickerTitles: {
				default: "File Picker",
                imagepicker: jahia_gwt_messages.label_imagepicker,
				editoriallinkpicker: jahia_gwt_messages.label_editorialpicker,
				"filemanager-anthracite": jahia_gwt_messages.label_filemanager_title,
				"editorialcontentmanager-anthracite": jahia_gwt_messages.label_editorialcontentmanager_title,
				"portletmanager-anthracite": jahia_gwt_messages.label_portletmanager_title,
                "repositoryexplorer-anthracite": jahia_gwt_messages.label_repositoryexplorer_title,
                "categorymanager-anthracite": jahia_gwt_messages.label_categorymanager_title,
                "sitemanager-anthracite": jahia_gwt_messages.label_sitemanager_title
			},
        },
		"EN": {
			autoRefresh: "Auto refresh",
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
			cancel: "Cancel",
			filterContent: "Filter Content",
            backgroundJobs: "Background Jobs",
			pickerTitles: {
				default: "File Picker",
				imagepicker: jahia_gwt_messages.label_imagepicker,
				editoriallinkpicker: jahia_gwt_messages.label_editorialpicker,
				"filemanager-anthracite": jahia_gwt_messages.label_filemanager_title,
				"editorialcontentmanager-anthracite": jahia_gwt_messages.label_editorialcontentmanager_title,
				"portletmanager-anthracite": jahia_gwt_messages.label_portletmanager_title,
                "repositoryexplorer-anthracite": jahia_gwt_messages.label_repositoryexplorer_title,
				"categorymanager-anthracite": jahia_gwt_messages.label_categorymanager_title,
                "sitemanager-anthracite": jahia_gwt_messages.label_sitemanager_title
			},
		},
		"FR": {
			autoRefresh: "Rafraîchissement automatique",
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
			cancel: "Annuler",
			filterContent: "Filtrer le contenu",
            backgroundJobs: "Tâches de fond",
			pickerTitles: {
				default: "File Picker",
                imagepicker: jahia_gwt_messages.label_imagepicker,
				editoriallinkpicker: jahia_gwt_messages.label_editorialpicker,
				"filemanager-anthracite": jahia_gwt_messages.label_filemanager_title,
				"editorialcontentmanager-anthracite": jahia_gwt_messages.label_editorialcontentmanager_title,
				"portletmanager-anthracite": jahia_gwt_messages.label_portletmanager_title,
				"repositoryexplorer-anthracite": jahia_gwt_messages.label_repositoryexplorer_title,
                "categorymanager-anthracite": jahia_gwt_messages.label_categorymanager_title,
				"sitemanager-anthracite": jahia_gwt_messages.label_sitemanager_title

			},
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
            ckeditorVersion: CKEDITOR.version,
			HTTP: function(){
				var pathnameSplit = document.location.pathname.split("/"),
					page = pathnameSplit[pathnameSplit.length - 1],
					root = pathnameSplit[1],
                    servletPath = jahiaGWTParameters.servletPath,
					queryString = document.location.href,
					queryStringParameters = queryString.split("?"),
					queryStringKeyValuePairs,
					queryStringKeyValuePairsSplit,
					n,
					picker =  null,
					QS = {};

				if(queryStringParameters.length > 1){
					// Found a query string ...
					queryStringKeyValuePairs = queryStringParameters[1].split("&");

					for(var n = 0; n < queryStringKeyValuePairs.length; n++){
						queryStringKeyValuePairsSplit = queryStringKeyValuePairs[n].split("=");
						QS[queryStringKeyValuePairsSplit[0]] = queryStringKeyValuePairsSplit[1];
					}
				}

                if(servletPath == "/engines/contentpicker.jsp"){
                    app = "contentpicker";
					picker = QS["type"] || "default";

                } else if(servletPath == "/engines/manager.jsp"){
                    app = "manager";
                    picker = QS["conf"] || "default";
                }

				return {
					page: page,
					root: root,
					picker: picker,
					QS: QS,
					app: app
				};
			}(),
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
        nav: {
            data: {
            },
            pushState: function(){
                var url = window.location.pathname,
                    DXStateObject = window.history.state; // DX Seems to need this so keep it the same

                history.pushState(DXStateObject, "DX", url);
            },
            onPopState: function(event) {

                if(app.workflow.data.open){
                    DexV2(".workflow-dashboard-engine .x-tool-close").trigger("click");

                } else if(app.backgroundJobs.data.open){
                    DexV2(".job-list-window .x-tool-close").trigger("click");

                } else if(app.picker.data.open){
                    DexV2("#JahiaGxtContentPickerWindow .button-cancel").trigger("click");

                } else if(app.engine.data.open){
                    DexV2(".engine-panel .button-cancel").trigger("click");

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
		onResize: function(){
			app.dev.log("::: APP ::: ONRESIZE");
			if(app.data.currentApp == "edit"){
				app.edit.topbar.reposition();
                app.edit.sidepanel.onWindowResize();

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

                // Trigger mousedown / mouseup on body to close any open context menus and combo menus
                DexV2.tag("body").trigger("mousedown").trigger("mouseup");

            }
		},
		onClick: function(e){
            app.dev.log("CLICKED APP");
			if(DexV2.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open"){
                var inSidePanel = DexV2.node(e.target).closest(".window-side-panel");

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
				enabled: false,
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
                    var cssLink = document.createElement("link");

                    cssLink.type = "text/css";
                    cssLink.rel = "stylesheet";
                    cssLink.id = "anthractite-CSS";
                    cssLink.href = DXAnthraciteCSS;

                    DexV2("head").append(cssLink);

					app.theme.data.enabled = true;

				}
			},
			off: function(){
				app.dev.log("::: APP ::: THEME ::: OFF");

                if(app.theme.data.enabled){
                    DexV2.id("anthractite-CSS").remove();

                }

			},
		},
        backgroundJobs: {
            data: {
                filters: [],
                open: false
            },
            onOpen: function(){

                app.backgroundJobs.data.open = true;
                app.nav.pushState();

                // Update title
                DexV2.class("job-list-window").filter(".x-window-tl .x-window-header-text").setHTML(localisedStrings[app.data.UILanguage].backgroundJobs);
				DexV2.class("job-list-window").filter(".x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar").setAttribute("indigo-label", localisedStrings[app.data.UILanguage].jobs);
				DexV2.class("job-list-window").filter(".x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(3) > div").setAttribute("indigo-label", localisedStrings[app.data.UILanguage].autoRefresh);

                // Reset the filters array
                app.backgroundJobs.data.filters = [];

                // Open GWT Filter menu to copy the entries and build our own menu
                DexV2.class("job-list-window") // Get thomas to add a class on the filtered combo ...
                    .filter(".x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(1) > table").trigger("click");

                // Wait until the filter menu is opened, then copy the contents to create our own filter menu
                DexV2("body").onceOpen(".x-menu-list-item", function(){
                    var menu = DexV2(".x-menu-list .x-menu-list-item span");

                    menu.each(function(menuItem){
                        var // Get Label
                            textNode = menuItem.getHTML(),
                            labelSplit = textNode.split("<"),
                            label = labelSplit[0],

                            // Get checked status
                            img = menuItem.filter("img"),
                            backgroundPosition = window.getComputedStyle(img.nodes[0])["background-position"],
                            isChecked = (backgroundPosition == "-18px 0px") ? false : true;

                        // Save to filters array
                        app.backgroundJobs.data.filters.push({
                            label: label,
                            isChecked: isChecked
                        });

                    });

                    // Build the side menu
                    app.backgroundJobs.buildFilterMenu();

                    // Close the drop down menu
                    DexV2.getCached("body").trigger("mousedown").trigger("mouseup");

                }, "BACKGROUND-JOBS-INIT-FILTER")

                // Filter toggles
                DexV2.class("job-list-window").onClick(".indigo-switch > div", function(){
                    var filterEntry = DexV2.node(this).parent(),
                        filterID = filterEntry.getAttribute("data-indigo-switch-id");

                    filterEntry.toggleAttribute("data-indigo-switch-checked", ["true", "false"]);

                    // Open the GWT filter combo
                    DexV2.class("job-list-window") // Get thomas to add a class on the filtered combo ...
                        .filter(".x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(1) > table").trigger("click");

                    // When it has opened, trigger click the selected filter type
                    DexV2("body").onceOpen(".x-menu", function(){
                        var menu = DexV2(".x-menu-list .x-menu-list-item span").index(filterID);

                        menu.trigger("click");

                    }, "BACKGROUND-JOBS-TRIGGER-FILTER");

                }, "BACKGROUND-JOBS-TOGGLE-FILTER");

                // Executes when results are loaded into the list
                DexV2.class("job-list-window").onOpen(".x-grid-group, .x-grid3-row", function(){
                    var previousButton = DexV2.class("job-list-window").filter(".x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-bbar .x-toolbar-layout-ct .x-toolbar-left .x-toolbar-cell:nth-child(2) > table"),
                        nextButton = DexV2.class("job-list-window").filter(".x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-bbar .x-toolbar-layout-ct .x-toolbar-left .x-toolbar-cell:nth-child(8) > table");

                    // Look at the previous and next buttons to determine if there is more than one page of results
                    if( previousButton.hasClass("x-item-disabled") &&
                        nextButton.hasClass("x-item-disabled")){

                        // Only one page, so hide pager
                        DexV2.class("job-list-window").setAttribute("indigo-results-multiple-pages", "false");

                    } else {
                        // More than one page, so show pager
                        DexV2.class("job-list-window").setAttribute("indigo-results-multiple-pages", "true");

                    }

                    // Add info and delete button to each row
                    var rows = DexV2.class("job-list-window").filter(".x-grid3-row"),

                        // Build the menu
                        actionMenu = document.createElement("menu"),
                        deleteButton = document.createElement("button"),
                        infoButton = document.createElement("button");

                    // Add classes to menu elements
                    actionMenu.classList.add("action-menu");
                    deleteButton.classList.add("delete-button");
                    infoButton.classList.add("info-button");

                    // Add buttons to the menu
                    actionMenu.appendChild(infoButton);
                    actionMenu.appendChild(deleteButton);

                    // Duplicate and add the menu to each row
                    rows.each(function(){
                        var clonedActionMenu = actionMenu.cloneNode(true);

						// This listener is sometimes called more than once, so check if the row has already had action menu added before adding ...
						if(!DexV2.node(this).hasClass("indigo-contains-actions-menu")){
							DexV2.node(this)
								.addClass("indigo-contains-actions-menu")
								.append(clonedActionMenu);
						}


                    });

                    // Flag that there are results ...
                    DexV2.class("job-list-window").setAttribute("indigo-results", "true");

                }, "BACKGROUND-JOBS-FILTERED-RESULTS");

                // Excutes when there are no rsults ...
                DexV2.class("job-list-window").onOpen(".x-grid-empty", function(){
                    // Flag that there are no results
                    DexV2.class("job-list-window").setAttribute("indigo-results", "false");

                }, "BACKGROUND-JOBS-FILTERED-RESULTS");

                // User has toggled the auto refresh checkbox, display the seconds input accordingly
                DexV2(".job-list-window").onClick("input[type='checkbox']", function(){
                    app.backgroundJobs.autoRefreshUpdate();
                })

                // User has clicked on the delete entry button
                DexV2(".job-list-window").onClick(".delete-button", function(){
                    // Trigger click on the hidden GWT delete button
                    DexV2.class("job-list-window").filter(".x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(7) > table").trigger("click");
                }, "BACKGROUND-JOBS-DELETE-ENTRY");

                // User has clicked on the info button
                DexV2(".job-list-window").onClick(".info-button", function(){
                    // Open the details panel by flagging the attribute
                    DexV2.class("job-list-window").setAttribute("data-indigo-details", "open");
                }, "BACKGROUND-JOBS-DETAILS-ENTRY");

                // User has clicked on the close details panel
                DexV2(".job-list-window").onClick(".x-window-bwrap .x-panel:nth-child(2) .x-panel-header .x-panel-toolbar", function(){
                    // Remove the  flag that displays the details panel
                    DexV2.class("job-list-window").setAttribute("data-indigo-details", "");

                });

                DexV2(".job-list-window").onMouseOver(".x-grid3-row", function(e){
                    // Impossible to know if an entry can be deleted or not WITHOUT first selecting it.
                    // Now we select a row when it is rolled over, check if the delete button is enabled, if it is not clickable,
                    //   then we hide the delete button that we previously added to the row
                    var row = DexV2.node(e.target),
                        isRow = row.hasClass("x-grid3-row");

                    if(isRow){
                        // Select the row
                        row.trigger("mousedown");

                        // See if the GWT delete button is clickable
                        var cantDelete = DexV2.class("job-list-window").filter(".x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(7) > table").hasClass("x-item-disabled");

                        if(cantDelete){
                            // The GWT delete button is disactivated, so hide our delete button
                            row.addClass("indigo-cant-delete");
                        }

                    }

                }, "BACKGROUND-JOBS-DETAILS-ROW-OVER");

                // Initiate the auto refresh display type
                app.backgroundJobs.autoRefreshUpdate();

            },
            onClose: function(){
                app.backgroundJobs.data.open = false;
            },
            autoRefreshUpdate: function(){
                // Check if the auto refresh checkbox is checked, if so then display the seconds input field
                var isChecked = DexV2.class("job-list-window").filter("input[type='checkbox']").nodes[0].checked;

                DexV2.class("job-list-window").setAttribute("indigo-auto-refresh", isChecked)

            },
            buildFilterMenu: function(){
                // Build the filter menu on the left side of the screen
                // The details have been previously recuperated from the GWT filter by combo
                var n = 0,
                    filters = app.backgroundJobs.data.filters,

                    filterMenu = document.createElement("div"),
                    filterMenuTitle = document.createElement("h1"),
                    filterMenuTitleText = document.createTextNode("Filters ..."),

                    switchHolder = document.createElement("div"),
                    switchRail = document.createElement("div"),
                    switchShuttle = document.createElement("div");

                // Define Menu
                filterMenu.classList.add("indigo-background-jobs-filters");

                // Define Title
                filterMenuTitle.appendChild(filterMenuTitleText);
                filterMenuTitle.classList.add("indigo-background-jobs-filters-title");
                // filterMenu.appendChild(filterMenuTitle);

                // Create Switch Master
                switchHolder.classList.add("indigo-switch");
                switchRail.classList.add("indigo-switch--rail");
                switchShuttle.classList.add("indigo-switch--shuttle");
                switchHolder.appendChild(switchRail);
                switchHolder.appendChild(switchRail);
                switchHolder.appendChild(switchShuttle);

                for(n = 0; n < filters.length; n ++){
                    var filterEntry = switchHolder.cloneNode(true);

                    filterEntry.setAttribute("data-indigo-switch-id", n);
                    filterEntry.setAttribute("data-indigo-switch-label", filters[n].label);
                    filterEntry.setAttribute("data-indigo-switch-checked", filters[n].isChecked);

                    filterMenu.appendChild(filterEntry);

                }

                // Remove the filters, just incase it has already been added
                DexV2(".indigo-background-jobs-filters").remove();

                // Add the new Filters Menu
                DexV2(".job-list-window").append(filterMenu);

            }
        },
		picker: {
			data: {
				currentItem: null,
				title: null,
				pickerTitle: null,
				displayType: null,
				previousDisplayType: null,
				ID: "JahiaGxtContentPickerWindow",
				standaloneID: "contentpicker",
				standaloneManagerID: "contentmanager",
				inpageID: "JahiaGxtContentPickerWindow",
				hasPreview: null,
				selectedFileCount: 0,
				zooms: {
					thumbsview: 2,
					detailedview: 2
				},
                open: false
			},
			onOpen: function(){
				app.dev.log("::: APP ::: PICKER ::: ONOPEN");

                app.picker.data.open = true;
                app.nav.pushState();

				// See if GWT has enabled previews for files, if so then set the preview flag to true
				app.picker.data.enablePreviews = DexV2("#" + app.picker.data.ID + " .toolbar-item-filepreview").nodes.length > 0;

				// Set flags for CSS
				DexV2.getCached("body")
					.setAttribute("data-INDIGO-PICKER-SEARCH", "")
					.setAttribute("data-INDIGO-PICKER", "open")
					.setAttribute("indigo-PICKER-DISPLAY", "thumbsview");

				if(app.picker.data.standalone){
					// Set ID for picker ...

					// Create title
					var pickerH1 = document.createElement("h1"),
						pickerH1Label = document.createTextNode(localisedStrings[app.data.UILanguage].pickerTitles[app.data.HTTP.picker]);

					pickerH1.id = "pickerTitle";
					pickerH1.appendChild(pickerH1Label);

					// Add title to page
					DexV2.id(app.picker.data.ID).prepend(pickerH1);

					if(app.data.HTTP.app == "manager"){
						// See if GWT has enabled previews for files, if so then set the preview flag to true
						app.picker.data.enablePreviews = true;

						DexV2.getCached("body").setAttribute("indigo-PICKER-DISPLAY", "listview");
					}
				}

				// Save current view (by default loads on thumbs)
				app.picker.data.displayType = "thumbsview";

                // Set zoom states
                app.picker.updateZoomLevel();

				var pickerTitle = (app.picker.data.standalone) ? DexV2("#pickerTitle") : DexV2.id(app.picker.data.ID).filter(".x-window-tl .x-window-header-text"),
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

				DexV2.id(app.picker.data.ID).prepend(toggleFilesButton);

				// Add placeholders to form elements
				app.picker.setPlaceholders();



				// Reset classes that may have been previously added
				DexV2.id(app.picker.data.ID).removeClass("search-panel-opened");

				// Register the side panel as open:
				DexV2.id(app.picker.data.ID).setAttribute("indigo-picker-panel", "opened");

				// Listen for clicks on toggle button
				DexV2.id(app.picker.data.ID).onClick("#toggle-picker-files", function(){
					DexV2.id(app.picker.data.ID).toggleClass("indigo-collapsed");
					DexV2.id(app.picker.data.ID).toggleAttribute("indigo-picker-panel", ["collapsed", "opened"]);
					DexV2.getCached("body").toggleAttribute("indigo-picker-panel", ["collapsed", "opened"]);

					var pickerTitle = (app.picker.data.standalone) ? DexV2("#pickerTitle") : DexV2.id(app.picker.data.ID).filter(".x-window-tl .x-window-header-text"),
						box = pickerTitle.getNode(0).getBoundingClientRect(),
						left = box.left,
						top = box.top,
						width = box.width,
						toolbarLeft = (left + width);

					DexV2.id("JahiaGxtManagerToolbar").css({
						"left": toolbarLeft + "px"
					});


				}, "TOGGLE-PICKER-FILES");

				// Listen for changes in slider (input range)
				DexV2.id(app.picker.data.ID).onInput("#thumb-size-slider", function(e){
					var zoomSize = e.target.value;

					// Save zoom level
					app.picker.data.zooms[app.picker.data.displayType] = zoomSize;

					DexV2("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree + div #images-view .x-view").setAttribute("indigo-thumb-zoom", zoomSize);
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
					DexV2.id(app.picker.data.ID).addClass("indigo-picker-multi-select");

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
				var hasSlider = DexV2("#" + app.picker.data.ID + " .x-slider").nodes.length > 0;

				if(hasSlider || app.data.HTTP.app == "manager"){
					var thumbSlider = document.createElement("input");

					thumbSlider.id = "thumb-size-slider";
					thumbSlider.type = "range";
					thumbSlider.value = 4;
					thumbSlider.min = 1;
					thumbSlider.max = 6;

					DexV2.id(app.picker.data.ID).prepend(thumbSlider);

				}



			},
			updateMultipleCount: function(){
				DexV2.class("toggle-multiple-selection").setHTML("Multiple selection (" + app.picker.data.selectedFileCount + ")");

			},
			updateZoomLevel: function(){
				if(DexV2.id("thumb-size-slider").nodes[0]){
					DexV2.id("thumb-size-slider").nodes[0].value = app.picker.data.zooms[app.picker.data.displayType];

				}

				DexV2("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree + div #images-view .x-view").setAttribute("indigo-thumb-zoom", app.picker.data.zooms[app.picker.data.displayType]);

			},
			setPlaceholders: function(){

				if(app.data.HTTP.app == "manager"){
					// Have to wait until the fields are loaded
					DexV2.id("JahiaGxtManagerTobTable").onOpen(".x-panel-tbar", function(){
						var filterField = DexV2('#JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(2) .x-form-text'),
							sortBy = DexV2('#JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(5) .x-form-text');

						filterField.setAttribute("placeholder", localisedStrings[app.data.UILanguage].filterField);
						sortBy.setAttribute("placeholder", localisedStrings[app.data.UILanguage].sortBy);
					}, "UPDATE_PLACEHOLDERS");

				} else {
					var filterField = DexV2('#images-view .x-toolbar .x-toolbar-left .x-toolbar-cell:nth-child(2) .x-form-text'),
	                	sortBy = DexV2('#images-view .x-toolbar .x-toolbar-left .x-toolbar-cell:nth-child(5) .x-form-text');

					filterField.setAttribute("placeholder", localisedStrings[app.data.UILanguage].filterField);
					sortBy.setAttribute("placeholder", localisedStrings[app.data.UILanguage].sortBy);
				}



			},
			onClose: function(){
				app.dev.log("::: APP ::: PICKER ::: ONCLOSE");
                app.picker.data.open = false;
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

                    // Dealing with file manager, possibility of images ( and therefore preview button )
                    if( app.data.HTTP.app == "manager" &&
                        (   app.data.HTTP.picker == "filemanager-anthracite" ||
                            app.data.HTTP.picker == "repositoryexplorer-anthracite")
                        ){

                            var isImage = DexV2.node(this).filter('img[src$="/jnt_file_img.png"]').nodes.length;

                            // Preview is posible ( dealing with an image)
                            if(isImage){

                                // See if the button has already been added ...
                                if(DexV2.node(this).filter(".preview-button").nodes.length == 0){
        							var previewButton = document.createElement("button"),
        			                    previewButtonLabel = document.createTextNode("Preview");

        							previewButton.classList.add("preview-button");

        							DexV2.node(this).prepend(previewButton);

        						}
                            }

                    }

					// Create and more info button ( if it hasnt aleady been added )
					if(DexV2.node(this).filter(".more-info-button").nodes.length == 0){
						var moreInfoButton = document.createElement("button"),
							moreInfoButtonLabel = document.createTextNode("More Info");

						moreInfoButton.classList.add("more-info-button");

						DexV2.node(this).prepend(moreInfoButton);

					}

					// Create and edit button ( If this is a Manager and if it hasnt aleady been added )
					if(app.data.HTTP.app == "manager"){
						if(DexV2.node(this).filter(".edit-button").nodes.length == 0){
							var editButton = document.createElement("button"),
								moreInfoButtonLabel = document.createTextNode("More Info");

							editButton.classList.add("edit-button");

							DexV2.node(this).prepend(editButton);

						}
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



					// Create and more info button ( if it hasnt aleady been added )
					if(DexV2.node(this).filter(".thumb .more-info-button").nodes.length == 0){

						var moreInfoButton = document.createElement("button"),
							moreInfoButtonLabel = document.createTextNode("More Info");

						moreInfoButton.classList.add("more-info-button");

						DexV2.node(this).filter(".thumb").prepend(moreInfoButton);

					}

					// Create and add preview button ( if previews exist and have not aleady been added )
					if(app.picker.data.enablePreviews){
						if(DexV2.node(this).filter(".thumb .preview-button").nodes.length == 0){
							var previewButton = document.createElement("button"),
								previewButtonLabel = document.createTextNode("Preview");

							previewButton.classList.add("preview-button");

							DexV2.node(this).filter(".thumb").prepend(previewButton);

						}
					}

					// Create and edit button ( If this is a Manager and if it hasnt aleady been added )
					if(app.data.HTTP.app == "manager"){
						if(DexV2.node(this).filter(".thumb .edit-button").nodes.length == 0){
							var editButton = document.createElement("button"),
								moreInfoButtonLabel = document.createTextNode("More Info");

							editButton.classList.add("edit-button");

							DexV2.node(this).filter(".thumb").prepend(editButton);

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
				openPreview: function(e){

					if(app.data.HTTP.app == "manager"){

                        DexV2.node(this).parent().trigger("dblclick");


					} else {
						DexV2("#JahiaGxtManagerToolbar .toolbar-item-filepreview").trigger("click");

					}
				},
				openEdit: function(){
					// Called to open the edit mode from within the managers

					// Manager Edit Engine doesnt have a title, so determine it from the list. Depending on the Manager / View the title is taken from different places ...
					var title = DexV2.node(this).parent().filter(".x-grid3-col-name").getHTML() ||
								DexV2.node(this).parent().filter(".x-grid3-col-displayName").getHTML() ||
								DexV2.node(this).parent().filter(".thumb img").getAttribute("title") ||
								DexV2.node(this).parent().parent().getAttribute("id") ||
								DexV2.node(this).parent().parent().parent().getAttribute("id");


                    DexV2.node(this).parent().trigger("contextmenu");

                    // When context menu is opened click on the EDIT button
                    DexV2("body").onceOpen(".x-menu", function(){
                        // Need to shift the context menu out of view because it doesnt dissappear until the alert has been closed.
                        DexV2(".x-menu").css({
                            left: "-50000px"
                        });

                        DexV2(".x-menu .toolbar-item-editcontent").trigger("click");


                    })

				},
				closeEdit: function(){
					// Called to close the Edit Engine, either when the user clicks Cancel or Save.
					DexV2.getCached("body").setAttribute("data-indigo-edit-engine", "");

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

						if(DexV2("#" + app.picker.data.ID + " .toolbar-item-filepreview").hasClass("x-item-disabled")){
							alert("Preview unavailable");
						} else {
							// Now need to remove the preview ( just incase it is previewing a previously selected thumb)
							DexV2.id("JahiaGxtImagePopup").remove(); // remove OLD preview

							// Reclick on the preview button for the newly selected thumb
							DexV2.node(this).customTrigger("click", {secondClick: true});
						}


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
		                DexV2("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree .x-panel-header").addClass("indigo-hover");
		            }
				},
				onMouseOut: function(){
					app.dev.log("::: APP ::: PICKER ::: SOURCE ::: ONMOUSEOUT");
					// USER HAS ROLLED OUT OF THE COMBO TRIGGER
		            DexV2("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");
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

		            DexV2("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree .x-panel-header").removeClass("indigo-hover");

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
						DexV2("#" + app.picker.data.ID + " .x-panel-tbar .action-bar-tool-item.toolbar-item-listview").trigger("click");

						// Switch to list view, then remove ...
						DexV2("#JahiaGxtManagerTobTable").onceGroupOpen(".x-grid3 .x-grid3-row", function(){
							DexV2.collection(this).remove();

						});
					}

					// Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
					DexV2.id("CRTbrowseTabItem").addClass("x-hide-display");
					DexV2("#CRTsearchTabItem").removeClass("x-hide-display");

					DexV2.getCached("body").setAttribute("data-INDIGO-PICKER-SEARCH", "open");
					DexV2.id(app.picker.data.ID).addClass("search-panel-opened");

					// Ask for class names ...
					var searchField = DexV2('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(1) td:nth-child(1) input'),
						languageField = DexV2('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(2) input'),
						fromDate = DexV2('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(5) input'),
						toDate = DexV2('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(6) input'),
						dateRange = DexV2('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7) input');

					searchField.setAttribute("placeholder", localisedStrings[app.data.UILanguage].search.replace("%n%", app.picker.data.pickerTitle));
					// searchField.getNode(0).value = "";
					languageField.setAttribute("placeholder", localisedStrings[app.data.UILanguage].languageField);

					fromDate.setAttribute("placeholder", localisedStrings[app.data.UILanguage].fromDate);
					toDate.setAttribute("placeholder", localisedStrings[app.data.UILanguage].toDate);
					dateRange.setAttribute("placeholder", localisedStrings[app.data.UILanguage].dateAnyTime);

					// Callback when user opens Date Range context menu ...
					DexV2('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7)').oneClick("img", function(){
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
								DexV2.id(app.picker.data.ID).setAttribute("data-indigo-search-date", "simple");

							}, "PREDEFINED_DATE_RANGE")

							// Clicked on the custom date range button
							.onMouseDown(".search-custom-date-range-button", function(){
								DexV2.id(app.picker.data.ID).setAttribute("data-indigo-search-date", "custom");

								dateRange.setAttribute("placeholder", localisedStrings[app.data.UILanguage].dateCustom);

								// Close the context menu by trigger clicking the page
								DexV2("#" + app.picker.data.ID).trigger("mousedown").trigger("mouseup");

							}, "CUSTOM_DATE_RANGE")
							.onMouseDown(".search-anytime-button", function(){
								// Clicked on Any TIme ( removes times filter )

								dateRange.setAttribute("placeholder", localisedStrings[app.data.UILanguage].dateAnyTime);
								DexV2.id(app.picker.data.ID).setAttribute("data-indigo-search-date", "");

								// Close the context menu by trigger clicking the page
								DexV2("#" + app.picker.data.ID).trigger("mousedown").trigger("mouseup");
							}, "ANY_TIME");




					}, "SEARCH_PANEL_DATE_RANGE_BUTTON");

					DexV2.id(app.picker.data.ID)
						// Listen for changes to meta tags ...
						.onClick("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)", app.picker.search.updateMetaLabel, "CHANGE_META_FILTER")

						// Listen for changes to modification type ...
						.onClick("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) .x-form-check-wrap", app.picker.search.updateModificationLabel, "CHANGE_MODIFICATION_FILTER")

						// Toggle modification menu when clicking ...
						.onClick("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) > label", app.picker.search.toggleModificationMenu, "TOGGLE_MODIFICATION_FILTER");

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
					DexV2.id(app.picker.data.ID).removeClass("search-panel-opened");

					DexV2.id("CRTsearchTabItem").addClass("x-hide-display");


		            // Display the BROWSE panels
		            DexV2("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)").removeClass("x-hide-display");

					// // Put the results in previous mode
					DexV2("#" + app.picker.data.ID + " .x-panel-tbar .action-bar-tool-item.toolbar-item-" + app.picker.data.previousDisplayType).trigger("click");


					// CLick on the refresh button to reload the content of the directory
		            DexV2.id(app.picker.data.ID).filter("#JahiaGxtManagerLeftTree .x-panel:not(.x-panel-collapsed) .x-tool-refresh").trigger("click");

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
            data: {
                returnToEditEngine: false,
                open: false
            },
            onOpen: function(){
				app.dev.log("::: APP ::: ENGINE ::: ONOPEN");
                app.engine.data.open = true;

                app.nav.pushState();

                // Get Friendly Name
				var nodeDisplayName = DexV2.getCached("body").getAttribute("data-singleselection-node-displayname");

                // Register Edit Engine as Opened and set locked flag to off
                DexV2.getCached("body")
                    .setAttribute("data-indigo-edit-engine-locked", "false")
                    .setAttribute("data-INDIGO-EDIT-ENGINE", "open");

                // Add Friendly Name to attribute of header
                if(app.data.HTTP.app == "manager"){
                    DexV2(".engine-window .x-window-header .x-window-header-text").setAttribute("data-friendly-name", nodeDisplayName);
                } else {
                    DexV2(".engine-panel > div.x-panel-header .x-panel-header-text").setAttribute("data-friendly-name", nodeDisplayName);

                }

                // Check if we need to create our own toggle switch when a fieldset header is loaded
                DexV2("body").onOpen(".x-fieldset-header", function(){
                    var fieldsetInput = DexV2.node(this).filter("input");

                    // This fieldset HAS a toggle, so replace with our own ...
                    if(fieldsetInput.nodes.length > 0){

                        var switchHolder = document.createElement("div"),
                            switchRail = document.createElement("div"),
                            switchShuttle = document.createElement("div");

                        switchHolder.classList.add("indigo-switch");
                        switchRail.classList.add("indigo-switch--rail");
                        switchShuttle.classList.add("indigo-switch--shuttle");

                        switchHolder.appendChild(switchRail);
                        switchHolder.appendChild(switchRail);
                        switchHolder.appendChild(switchShuttle);

                        this.prepend(switchHolder);

                    }

                }, "EDIT-ENGINE-INDIGO-SWITCH-CREATOR");

                // Trigger click the GWT checkbox when the user clicks out toggle switch
                DexV2("body").onClick(".x-fieldset-header .indigo-switch", function(){
                    var checkbox = DexV2.node(this).parent().filter("input");

                    checkbox.trigger("click");

                }, "EDIT-ENGINE-INDIGO-SWITCH-LISTENER");

                // Wait a sec, ...
                // Then work out if the node has been locked
				setTimeout(function(){
					var nodeDetails = DexV2(".engine-panel > div.x-panel-header .x-panel-header-text").getHTML(),
						regExp = /\[(.*?)\]/gi,
						nodeStatus = nodeDetails.match(regExp),
						isLocked = false;

                    // Square brackets have been found (suggesting its locked) ...
					if(nodeStatus){
                        // See if the String contains the word locked, or verouillé or gesperrt
						isLocked = nodeStatus[0].indexOf("locked") > -1 || nodeStatus[0].indexOf("verrouillé") > -1 || nodeStatus[0].indexOf("gesperrt") > -1;

                        // Register the Node as locked
                        DexV2.getCached("body").setAttribute("data-indigo-edit-engine-locked", isLocked);

                        // Add ONLY the lock message to the header, which will be displayed on a hover
						DexV2(".engine-panel > div.x-panel-header .x-panel-header-text").setHTML(nodeStatus[0])

					}

				}, 500);
			},
			onClose: function(e){
				app.dev.log("::: APP ::: ENGINE ::: ONCLOSE");
                app.engine.data.open = false;
                // var workflowEngine = DexV2.node(this).hasClass("workflowactiondialog-card");

                // if(workflowEngine){
                //     console.log("CLOSED PUBLICTION SCREEN");
                //
                //     if(app.engine.data.returnToEditEngine){
                //         // DexV2(".edit-menu-edit").trigger("click");
                //         // DexV2(".toolbar-item-editcontent").trigger("click");
                //
                //         app.engine.data.returnToEditEngine = false;
                //     }
                //
                // }

				app.iframe.clearSelection();
				DexV2.getCached("body")
                    .setAttribute("data-INDIGO-EDIT-ENGINE", "")
                    .setAttribute("data-INDIGO-PICKER-SEARCH", "");

			},
            onOpenHistory: function(){

                // User has clicked on the close details panel
                DexV2.id("JahiaGxtEditEnginePanel-history").onClick(".x-panel:nth-child(2) .x-panel-toolbar", function(){
                    // Remove the  flag that displays the details panel
                    DexV2.id("JahiaGxtEditEnginePanel-history").setAttribute("data-indigo-details", "");
                    DexV2.getCached("body").setAttribute("data-indigo-history-display", "");
                });

                // Executes when results are loaded into the list
                DexV2.id("JahiaGxtEditEnginePanel-history").onGroupOpen(".x-grid3-row", function(){
                    var previousButton = DexV2.id("JahiaGxtEditEnginePanel-history").filter(".x-panel-bbar .x-toolbar-left .x-toolbar-cell:nth-child(2) > table"),
                        nextButton = DexV2.id("JahiaGxtEditEnginePanel-history").filter(".x-panel-bbar .x-toolbar-left .x-toolbar-cell:nth-child(8) > table");

                    // Look at the previous and next buttons to determine if there is more than one page of results
                    if( previousButton.hasClass("x-item-disabled") &&
                        nextButton.hasClass("x-item-disabled")){
                        // Only one page, so hide pager
                        DexV2.id("JahiaGxtEditEnginePanel-history").setAttribute("indigo-results-multiple-pages", "false");

                    } else {
                        // More than one page, so show pager
                        DexV2.id("JahiaGxtEditEnginePanel-history").setAttribute("indigo-results-multiple-pages", "true");

                    }

                    // Add info and delete button to each row
                    var rows = DexV2.id("JahiaGxtEditEnginePanel-history").filter(".x-grid3-row"),

                        // Build the menu
                        actionMenu = document.createElement("menu"),
                        infoButton = document.createElement("button");

                    // Add classes to menu elements
                    actionMenu.classList.add("action-menu");
                    infoButton.classList.add("info-button");

                    // Add buttons to the menu
                    actionMenu.appendChild(infoButton);

                    // Duplicate and add the menu to each row
                    rows.each(function(){
                        var clonedActionMenu = actionMenu.cloneNode(true);

						// This listener is sometimes called more than once, so check if the row has already had action menu added before adding ...
						if(!DexV2.node(this).hasClass("indigo-contains-actions-menu")){
							DexV2.node(this)
								.addClass("indigo-contains-actions-menu")
								.append(clonedActionMenu);
						}

                    });

                    // Flag that there are results ...
                    DexV2.id("JahiaGxtEditEnginePanel-history").setAttribute("indigo-results", "true");

                }, "HISTORY-RESULTS-LIST");


                // Excutes when there are no results ...
                DexV2.id("JahiaGxtEditEnginePanel-history").onOpen(".x-grid-empty", function(){
                    // Flag that there are no results
                    DexV2.id("JahiaGxtEditEnginePanel-history").setAttribute("indigo-results", "false");

                }, "HISTORY-NO-RESULTS-LIST");


                // User has clicked on the info button
                DexV2.id("JahiaGxtEditEnginePanel-history").onClick(".info-button", function(){
                    // Open the details panel by flagging the attribute
                    DexV2.id("JahiaGxtEditEnginePanel-history").setAttribute("data-indigo-details", "open");
                    DexV2.getCached("body").setAttribute("data-indigo-history-display", "true");
                }, "HISTORY-DETAILS-ENTRY");

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
                DexV2.getCached("body").setAttribute("data-indigo-editing-condition", false);
            },
            editCondition: function(){
                DexV2("#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3)").addClass("indigo-show");

                DexV2.getCached("body")
                    .setAttribute("data-indigo-add-visibility-condition", "")
                    .setAttribute("data-indigo-editing-condition", true);

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
                DexV2.getCached("body").setAttribute("data-indigo-editing-condition", true);

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

                    newMenu.appendChild(closeButton);
                    newMenu.appendChild(saveButton);

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
            data: {
                opened: false
            },
			dashboard: {
                onOpen: function(){
					app.dev.log("::: APP ::: WORKFLOW ::: DASHBOARD ::: ONOPEN");

                    app.workflow.data.open = true;
                    app.nav.pushState();

                    DexV2(".workflow-dashboard-engine .x-tool-maximize").trigger("click");
				},
                onClose: function(){
					app.dev.log("::: APP ::: WORKFLOW ::: DASHBOARD ::: ONOPEN");

                    app.workflow.data.open = false;

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

                    app.edit.sidepanel.onNewChannel();


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
                    DexV2.getCached("body").getAttribute("data-sitesettings") == "false" &&
					DexV2.getCached("body").getAttribute("data-indigo-sidepanel-pinned") != "true"){

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

                // Setup the alternative channels system
                app.edit.sidepanel.initChannels();

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
								DexV2(".x-viewport-editmode .action-toolbar .x-toolbar-cell:nth-child(10)").addClass("indigo-job-running");

                            } else {
                                // No Jobs active
								activeJob = false;
								jobTooltip = localisedStrings[app.data.UILanguage].jobs;

								DexV2(".x-viewport-editmode .action-toolbar .x-toolbar-cell:nth-child(10)").removeClass("indigo-job-running");

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
					var offset = (DexV2.getCached("body").getAttribute("data-indigo-sidepanel-pinned") == "true") ? 160 : 0;

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
                                elements.title.style.left = (((boxes.body.width / 2) - (boxes.title.width / 2)) + offset) + "px";

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
                                    elements.nodePathTitle.style.left = (boxes.title.left - 20) + "px";
                                    DexV2(".edit-menu-publication .x-btn-mc").setAttribute("data-publication-label", app.iframe.data.pageTitle);
                                } else {
                                    // No Select
                                    // elements.publishButton.style.left = (boxes.title.left - 20) + "px";
                                    // elements.refreshButton.style.left = (boxes.title.left + boxes.title.width + 10) + "px";
                                    elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 9) + "px";
                                    elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 33) + "px";
                                    elements.nodePathTitle.style.left = (boxes.title.left - 20) + "px";

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
					currentTab: null,
                    channel: {
                        autofit: false,
                        opened: false
                    }
				},
                onNewChannel: function(){
                    // Dev note: This is also triggered when the user changes pages by navigation in Device Channel Preview

                    if(app.edit.sidepanel.data.channel.opened){
                        DexV2.id("channel-auto-fit-button").addClass("selected");
                        DexV2.id("channel-zoom-button").removeClass("selected");
                        DexV2.id("channel-size-slider-holder").addClass("disabled");

                        app.edit.sidepanel.zoomChannel(0);
                        app.edit.sidepanel.data.channel.autofit = true;

                        app.edit.sidepanel.close();

                        DexV2(".mainmodule > div:nth-child(2)").removeClass("channel-zoom");
                    }

                },
                onWindowResize: function(){
                    if( app.edit.sidepanel.data.channel.opened
                        && app.edit.sidepanel.data.channel.autofit){

                        app.edit.sidepanel.zoomChannel(0);

                    }
                },
                initChannels: function(){
                    // Force GWT to load the GWT tab for channels
                    DexV2.id("JahiaGxtSidePanelTabs__JahiaGxtChannelsTab").trigger("click");

                    // Build the Channels bar
                    var channelMenu = document.createElement("menu"),
                        channelCloseButton = document.createElement("button"),
                        channelZoomHolder = document.createElement("div"),
                        channelAutoFitButton = document.createElement("div"),
                        channelAutoFitButtonLabel = document.createTextNode("Autofit"),
                        channelZoomButton = document.createElement("div"),
                        channelZoomButtonLabel = document.createTextNode("Zoom"),
                        channelSlider = document.createElement("input"),
                        channelSliderHolder = document.createElement("div"),
                        channelTitle = document.createElement("div"),
                        channelTitleTextNode = document.createTextNode("Device"),
                        channelOrientaion = document.createElement("div"),
                        channelOrientaionTextNode = document.createTextNode("Orientation");


                    // CLose button
                    channelCloseButton.id = "channel-close-button";

                    // Channel Menu
                    channelMenu.id = "channel-menu";

                    // Channel Holder
                    channelZoomHolder.id = "channel-zoom-holder";

                    // Channel Title
                    channelTitle.id = "channel-title";

                    // Auto fit button
                    channelAutoFitButton.appendChild(channelAutoFitButtonLabel);
                    channelAutoFitButton.id = "channel-auto-fit-button";

                    // Auto fit button
                    channelZoomButton.appendChild(channelZoomButtonLabel);
                    channelZoomButton.id = "channel-zoom-button";
                    channelZoomButton.classList.add("selected");

                    // Orientation button
                    channelOrientaion.id = "channel-orientation";

                    // Channel Slider Holder
                    channelSliderHolder.id = "channel-size-slider-holder";

                    // Channel Slider
    				channelSlider.id = "channel-size-slider";
    				channelSlider.type = "range";
    				channelSlider.value = 100;
    				channelSlider.min = 30;
    				channelSlider.max = 100;

                    // Stick them together
                    channelSliderHolder.appendChild(channelSlider);
                    channelZoomHolder.appendChild(channelAutoFitButton);
                    channelZoomHolder.appendChild(channelZoomButton);
                    channelZoomHolder.appendChild(channelSliderHolder);
                    channelMenu.appendChild(channelCloseButton);
                    channelMenu.appendChild(channelTitle);
                    channelMenu.appendChild(channelOrientaion);
                    channelMenu.appendChild(channelZoomHolder);

                    // Add the bar to the body
    				DexV2.getCached("body").prepend(channelMenu);

                    // Auto fit the channel preview to the screen
                    // Dev note: When this is ON we need to update on page resize
                    DexV2.getCached("body").onClick("#channel-auto-fit-button", function(){
                        DexV2.id("channel-auto-fit-button").addClass("selected");
                        DexV2.id("channel-zoom-button").removeClass("selected");
                        DexV2.id("channel-size-slider-holder").addClass("disabled");

                        app.edit.sidepanel.zoomChannel(0);
                        app.edit.sidepanel.data.channel.autofit = true;

                        DexV2(".mainmodule > div:nth-child(2)").removeClass("channel-zoom");

                    });


                    // Close button
                    DexV2.getCached("body").onClick("#channel-close-button", function(){

                        // Trigger the close button
                        // Click on the Channel drop down in the (hidden) side panel
                        DexV2.id("JahiaGxtChannelsTab").filter(".x-form-trigger").index(0).trigger("click");

                        // When the combo menu opens, add a class to enable repositioning to bottom of screen
                        DexV2.getCached("body").onceOpen(".x-combo-list", function(){
                            // CLick first in the list
                            DexV2.class("x-combo-list").filter(".thumb-wrap").index(0).trigger("mousedown");

                        });

                        app.edit.sidepanel.data.channel.opened = false;

                    });

                    DexV2.getCached("body").onClick("#channel-zoom-button", function(){
                        DexV2.id("channel-auto-fit-button").removeClass("selected");
                        DexV2.id("channel-zoom-button").addClass("selected");
                        DexV2.id("channel-size-slider-holder").removeClass("disabled");

                        app.edit.sidepanel.data.channel.autofit = false;

                        DexV2(".mainmodule > div:nth-child(2)").addClass("channel-zoom");

                        DexV2.id("channel-size-slider").trigger("input");
                    });

                    // Open the combo to change the Channel
                    DexV2.getCached("body").onClick("#channel-title", function(){

                        // Click on the Channel drop down in the (hidden) side panel
                        DexV2.id("JahiaGxtChannelsTab").filter(".x-form-trigger").index(0).trigger("click");

                        // When the combo menu opens, add a class to enable repositioning to bottom of screen
                        DexV2.getCached("body").onceOpen(".x-combo-list", function(){
                            DexV2.class("x-combo-list").addClass("channel-device-combo-box");
                        });

                        app.edit.sidepanel.data.channel.opened = true;

                    });

                    // Toggle between orientations
                    DexV2.getCached("body").onClick("#channel-orientation", function(){
                        // Open the Orrientation combo in the (hidden) side panel
                        DexV2.id("JahiaGxtChannelsTab").filter(".x-form-trigger").index(1).trigger("click");

                        // When it is opened, click on the orientation that is NOT selected
                        DexV2.getCached("body").onceOpen(".x-combo-list", function(){
                            DexV2(".x-combo-list .x-combo-list-item:not(.x-view-highlightrow)").trigger("mousedown");
                        })
                    });

                    // Redimension the Channel Preview
                    DexV2.getCached("body").onInput("#channel-size-slider", function(e){
                        zoomSize = e.target.value;

                        app.edit.sidepanel.zoomChannel(zoomSize);

                    }, "CHANNEL-SIZE-SLIDER");


                },
                zoomChannel: function(zoomSize){
                    var windowHeight = window.innerHeight
                                    || document.documentElement.clientHeight
                                    || document.body.clientHeight,
                        actualHeight = parseInt(DexV2(".mainmodule > div:nth-child(2) > div").nodes[0].style.height),
                        windowPadding = 136,
                        transformOrigin = "50% 0",
                        scale = (zoomSize > 0) ? (zoomSize / 100) : ((windowHeight - windowPadding) / actualHeight);


                    DexV2(".x-abs-layout-container").css({
                        transform: "scale(" + scale + ")",
                        transformOrigin: transformOrigin
                    });
                },
				togglePin: function(){
					DexV2.getCached("body").toggleAttribute("data-INDIGO-SIDEPANEL-PINNED", ["true", ""]);
					DexV2.iframe(".window-iframe").filter("body").nodes[0].style.pointerEvents = "all";

					app.edit.topbar.reposition();
				},

				toggleFloatingPanel: function(e){

					if(DexV2.node(e.target).getAttribute("id") == "images-view" ||
						DexV2.node(e.target).hasClass("x-panel-bwrap") ||
						DexV2.node(e.target).hasClass("x-box-item")){

						if(DexV2.getCached("body").hasClass("show-results")){
							DexV2.getCached("body").toggleClass("minimise-results");
						} else {
							DexV2.getCached("body").removeClass("minimise-results");

						}
					}


				},
				onStartDrag: function(){
					app.dev.log("::: APP ::: EDIT ::: SIDEPANEL ::: ONSTARTDRAG");

					DexV2.getCached("body").addClass("indigo-drag-to-drop");

					// Do not close the side panel as the user wants to drag a page somewhere else
					if(DexV2.getCached("body").getAttribute("data-indigo-gwt-panel-tab") != "JahiaGxtSidePanelTabs__JahiaGxtPagesTab"){
						app.edit.sidepanel.close();
					}

				},
				onStopDrag: function(){
					app.dev.log("::: APP ::: EDIT ::: SIDEPANEL ::: ONSTOPDRAG");
					DexV2.getCached("body").removeClass("indigo-drag-to-drop");

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
					if(DexV2.getCached("body").getAttribute("data-edit-window-style") !== "settings" && DexV2.getCached("body").getAttribute("data-INDIGO-GWT-SIDE-PANEL") == "open" && DexV2.getCached("body").getAttribute("data-INDIGO-COLLAPSABLE-SIDE-PANEL") == "yes" && DexV2.getCached("body").getAttribute("data-INDIGO-SIDEPANEL-PINNED") != "true"){
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
					},
					onMouseDown: function(e){
						var nodeJoint = DexV2.node(e.target).hasClass("x-tree3-node-joint"),
							alreadySelected = DexV2.node(this).hasClass("indigo-selected");

						if(!nodeJoint){
							if(alreadySelected){
								// Toggle the drawer
								DexV2.getCached("body").toggleClass("minimise-results");

							} else {
								// Show drawer
								DexV2.getCached("body").removeClass("minimise-results");

								DexV2.id("JahiaGxtSidePanelTabs").filter(".indigo-selected").removeClass("indigo-selected");
								DexV2.node(this).addClass("indigo-selected");
							}
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


          // Position the language selector dropdown.
          var siteSettingsList = DexV2.id("JahiaGxtSettingsTab").filter(".x-panel.x-component").nodes[0];
          var langSelector = DexV2.class("toolbar-itemsgroup-languageswitcher").nodes[0];

          function offset(el) {
            var rect = el.getBoundingClientRect(),
            scrollLeft = window.pageXOffset || document.documentElement.scrollLeft,
            scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            return {
              top: rect.top + scrollTop
            }
          }
          var siteSettingsListTop = offset(siteSettingsList).top;

          if(siteSettingsListTop <= 145) {
            langSelector.style.cssText='top: 112px !important';
          } else if (siteSettingsListTop >= 185) {
            langSelector.style.cssText='top: 152px !important';
          };


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

                            // DEV NOTE :: DONT LIKE HAVING TO WAIT LIKE THIS ...
                            setTimeout(function(){
                                DexV2.id("JahiaGxtRefreshSidePanelButton").trigger("click");

                                // Listen for first settings page to be added to tree, select it then stop listening.
    							DexV2("#JahiaGxtSettingsTab").onGroupOpen(".x-grid3-row", function(nodeGroup){

                                    if(app.edit.settings.data.opened){
                                        var firstRow = this[0];
                                        // DEV NOTE ::: May need to loop through nodeGroup to get first actual available PAGE (ie. not a folder)

                                        DexV2.node(firstRow)
        									.trigger("mousedown")
        									.trigger("click");
                                    } else {
                                    }


    							});
                            }, 50);

                            DexV2("#JahiaGxtSettingsTab .x-grid3-row")
                            .trigger("mousedown")
                            .trigger("click");



						}
					}

				},
				close: function(){
                    app.dev.log("::: APP ::: EDIT ::: SETTINGS ::: CLOSE");
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
                        // DEV NOTE ::: This is causing problems when switching site, not selecting a page in the new sites tree openeing settings then closing.
						// Trigger Click on Second page (first row is not an actual page)
						// Need to set side panel tab as pages to allow the capture of the click to save the page in history...
						DexV2.getCached("body").setAttribute("data-indigo-gwt-panel-tab", "JahiaGxtSidePanelTabs__JahiaGxtPagesTab");

                        DexV2("#JahiaGxtPagesTab .x-grid3-row:nth-child(2)")
                            .trigger("mousedown")
                            .trigger("mouseup");

		            }

          // Reset language dropdown position
          var langSelector = DexV2.class("toolbar-itemsgroup-languageswitcher").nodes[0];
          langSelector.style.cssText='top: 0';

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
                                editPage: DexV2(".action-toolbar .x-toolbar-cell:nth-child(5) table").getNode(0),
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
				.onClick(".window-side-panel .x-panel-footer", app.edit.sidepanel.togglePin)
				.onOpen(".job-list-window", app.backgroundJobs.onOpen)
                .onMouseDown(".toolbar-item-studio", function(){
                    var studioNodePath = sessionStorage.getItem("studiomode_nodePath"),
                        baseURL = jahiaGWTParameters.contextPath + "/cms/studio/default/" + jahiaGWTParameters.uilang;

                    if(studioNodePath){
                        studioURL = baseURL + studioNodePath + ".html"
                    } else {
                        studioURL = baseURL + "/settings.manageModules.html"

                    }

                    window.location = studioURL;

                })
				.onOpen(".content-type-window", function(){

					DexV2(".content-type-window .x-form-field-wrap input").setAttribute("placeholder", localisedStrings[app.data.UILanguage].filterContent);

					// Firefox has bug which doesnt always set focus on text input, wait a split second before settings focus
					setTimeout(function(){
						DexV2(".content-type-window .x-form-field-wrap input")
							.nodes[0].focus();
					}, 50);

				})
				// .onClick(".workflowactiondialog-ctn .x-grid3-row", function(){
				// 	DexV2.class("workflowactiondialog-card").addClass("indigo-opened");
				// })
				// .onClick(".workflow-action-dialog .x-tab-strip-spacer", function(){
				// 	DexV2.class("workflowactiondialog-card").removeClass("indigo-opened");
				// })
				.onClick(".toolbar-item-newfolder", function(){
					// Add new folder
					var isDisabled = DexV2.node(this).hasClass("x-item-disabled");

					if(isDisabled){
						// User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE

						var selectedFolder = DexV2("#CRTbrowseTabItem .x-grid3-row-selected");

						if(selectedFolder.nodes.length > 0){
							selectedFolder.trigger("contextmenu");

						} else {
							DexV2("#CRTbrowseTabItem .x-grid3-row")
								.first() // Get first row
								.trigger("mousedown") // Select it with a mouse down
								.trigger("mouseup") // Release the mousedown
								.trigger("contextmenu"); // Trigger a right click
						}

						// When context menu is opened click on the ADD FOLDER button
						DexV2("body").onceOpen(".x-menu", function(){
							// Need to shift the context menu out of view because it doesnt dissappear until the alert has been closed.
                            DexV2(".x-menu").css({
                                left: "-50000px"
                            });

							DexV2(".x-menu .toolbar-item-newfolder").trigger("click");

						})

					}

				})
				.onClick(".toolbar-item-newcontentfolder", function(){
					// Add new folder
					var isDisabled = DexV2.node(this).hasClass("x-item-disabled");

					if(isDisabled){
						// User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE

						var selectedFolder = DexV2("#CRTbrowseTabItem .x-grid3-row-selected");

						if(selectedFolder.nodes.length > 0){
							selectedFolder.trigger("contextmenu");

						} else {
							DexV2("#CRTbrowseTabItem .x-grid3-row")
								.first() // Get first row
								.trigger("mousedown") // Select it with a mouse down
								.trigger("mouseup") // Release the mousedown
								.trigger("contextmenu"); // Trigger a right click
						}

						// When context menu is opened click on the ADD FOLDER button
						DexV2("body").onceOpen(".x-menu", function(){
							DexV2(".x-menu .toolbar-item-newcontentfolder").trigger("click");

						})

					}

				})
				.onClick(".toolbar-item-newpage", function(){
					// Add new folder
					var isDisabled = DexV2.node(this).hasClass("x-item-disabled");

					if(isDisabled){
						// User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE

						var selectedFolder = DexV2("#CRTbrowseTabItem .x-grid3-row-selected");

						if(selectedFolder.nodes.length > 0){
							selectedFolder.trigger("contextmenu");

						} else {
							DexV2("#CRTbrowseTabItem .x-grid3-row")
								.first() // Get first row
								.trigger("mousedown") // Select it with a mouse down
								.trigger("mouseup") // Release the mousedown
								.trigger("contextmenu"); // Trigger a right click
						}

						// When context menu is opened click on the ADD FOLDER button
						DexV2("body").onceOpen(".x-menu", function(){
							DexV2(".x-menu .toolbar-item-newpage").trigger("click");

						})

					}

				})
                .onClick(".toolbar-item-upload", function(){
					// Add new folder
					var isDisabled = DexV2.node(this).hasClass("x-item-disabled");

					if(isDisabled){
						// User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE

						var selectedFolder = DexV2("#CRTbrowseTabItem .x-grid3-row-selected");

						if(selectedFolder.nodes.length > 0){
							selectedFolder.trigger("contextmenu");

						} else {
							DexV2("#CRTbrowseTabItem .x-grid3-row")
								.first() // Get first row
								.trigger("mousedown") // Select it with a mouse down
								.trigger("mouseup") // Release the mousedown
								.trigger("contextmenu"); // Trigger a right click
						}

						// When context menu is opened click on the ADD FOLDER button
						DexV2("body").onceOpen(".x-menu", function(){
							DexV2(".x-menu .toolbar-item-upload").trigger("click");

						})

					}
				})
                .onClick(".toolbar-item-newcontent", function(){
					// Add new content folder
					var isDisabled = DexV2.node(this).hasClass("x-item-disabled");

					if(isDisabled){
						// User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE

						var selectedFolder = DexV2("#CRTbrowseTabItem .x-grid3-row-selected");

						if(selectedFolder.nodes.length > 0){
							selectedFolder.trigger("contextmenu");

						} else {
							DexV2("#CRTbrowseTabItem .x-grid3-row")
								.first() // Get first row
								.trigger("mousedown") // Select it with a mouse down
								.trigger("mouseup") // Release the mousedown
								.trigger("contextmenu"); // Trigger a right click
						}

						// When context menu is opened click on the ADD FOLDER button
						DexV2("body").onceOpen(".x-menu", function(){
							DexV2(".x-menu .toolbar-item-newcontent").trigger("click");

						})

					}
				})
				.onAttribute("body", "data-singleselection-node-path", app.onChangeNodePath)
                .onOpen("#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) .x-grid-panel", app.engine.onOpenWorkflow)
				.onOpen("#JahiaGxtEditEnginePanel-history", app.engine.onOpenHistory)
                .onOpen("#JahiaGxtUserGroupSelect", app.pickers.users.onOpen)
				.onOpen("#JahiaGxtContentBrowseTab", function(){
					DexV2.node(this).filter(".x-box-item:nth-child(2) .x-grid3-body").addClass("results-column");
                })
    			.onOpen("#JahiaGxtFileImagesBrowseTab", function(){
    				DexV2.node(this).filter("#images-view > div").addClass("results-column");
    			})
    			.onOpen("#JahiaGxtCategoryBrowseTab", function(){
    				DexV2.node(this).filter(".x-box-item:nth-child(2) .x-grid3-body").addClass("results-column");
    			})
    			.onOpen("#JahiaGxtSearchTab", function(){
    				DexV2.node(this).filter(".JahiaGxtSearchTab-results .x-grid3-body").addClass("results-column");
    			})
    			.onOpen("#JahiaGxtCreateContentTab", function(){
    				DexV2.node(this).filter("input.x-form-text").setAttribute("placeholder", localisedStrings[app.data.UILanguage].filterContent)
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

    						}  else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab"){
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

    						}  else if(app.edit.sidepanel.data.currentTab == "JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab"){
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
                .onOpen("#" + app.picker.data.ID, app.picker.onOpen)
                .onOpen("#JahiaGxtEnginePanel", app.engine.onOpen)
                // .onOpen("#JahiaGxtEngineWindow", app.picker.thumb.openEdit)
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
                .onClose("#" + app.picker.data.ID, app.picker.onClose)
                .onClose("#JahiaGxtEnginePanel", app.engine.onClose)
                .onClose("#JahiaGxtImagePopup", app.imagePreview.onClose)
                .onOpen(".workflow-dashboard-engine", app.workflow.dashboard.onOpen)
                .onClose(".workflow-dashboard-engine", app.workflow.dashboard.onClose)
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
				.onMouseDown("#" + app.picker.data.ID + " .cancel-edit", app.picker.thumb.closeEdit)
				.onMouseDown("#" + app.picker.data.ID + " .edit-button", app.picker.thumb.openEdit)
				.onMouseDown("#" + app.picker.data.ID + " .more-info-button", app.picker.thumb.onContext)
                .onMouseDown("#" + app.picker.data.ID + " .preview-button", app.picker.thumb.openPreview)

                .onClick("#JahiaGxtManagerLeftTree + div .thumb-wrap", app.picker.thumb.onClick)
                // DexV2(".x-viewport-editmode .action-toolbar > table").onClick(app.theme.onToggle)
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
                .onClick("#" + app.picker.data.ID, app.picker.onClick)
                .onClick("#" + app.picker.data.ID + " .x-panel-tbar .action-bar-tool-item.toolbar-item-listview", app.picker.onListView)
				.onClick("#" + app.picker.data.ID + " .x-panel-tbar .action-bar-tool-item.toolbar-item-thumbsview", app.picker.onThumbView)
                .onClick("#" + app.picker.data.ID + " .x-panel-tbar .action-bar-tool-item.toolbar-item-detailedview", app.picker.onDetailView)
				.onMouseDown(".toolbar-item-listview", app.picker.onListView)
				.onMouseDown(".toolbar-item-thumbsview", app.picker.onThumbView)
                .onMouseDown(".toolbar-item-detailedview", app.picker.onDetailView)
				.onMouseUp("#contentmanager .x-panel-bbar .x-toolbar-cell:nth-child(1) .x-btn", app.picker.thumb.closeEdit)
                .onClick(".node-path-title", app.iframe.clearSelection)
                .onMouseDown(".x-viewport-editmode #JahiaGxtSidePanelTabs .x-grid3-row", app.edit.onNav)
                .onMouseDown("#JahiaGxtManagerLeftTree__CRTbrowseTabItem", app.picker.search.close)
                .onMouseUp("#JahiaGxtManagerLeftTree__CRTsearchTabItem", app.picker.search.open)
                .onClick("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree .x-panel-header", app.picker.source.close)
                .onClick("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.toggle)
                .onMouseEnter("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.onMouseOver)
                .onMouseLeave("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer", app.picker.source.onMouseOut)
				.onMouseOver("#" + app.picker.data.ID + " #JahiaGxtManagerTobTable .x-grid3-row", app.picker.row.onMouseOver)
				.onMouseOver("#" + app.picker.data.ID + " #JahiaGxtManagerTobTable .thumb-wrap", app.picker.thumb.onMouseOver)
                // .onMouseEnter("#" + app.picker.data.ID + " #JahiaGxtManagerLeftTree + div #images-view .x-view", app.picker.thumb.onMouseOut)
                .onMouseUp("#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab", app.edit.sidepanel.tab.onClick, "OPEN-SIDE-PANEL-TAB")
                .onMouseUp("#JahiaGxtSidePanelTabs__JahiaGxtSettingsTab", app.edit.settings.open)
				.onMouseDown("#JahiaGxtContentBrowseTab .x-box-item:nth-child(1) .x-grid3-row, #JahiaGxtFileImagesBrowseTab .x-grid3-row, #JahiaGxtCategoryBrowseTab .x-grid3-row", app.edit.sidepanel.row.onMouseDown)
				.onClick("#images-view, .x-box-inner .x-box-item:nth-child(2), .JahiaGxtSearchTab-results .x-panel-bwrap", app.edit.sidepanel.toggleFloatingPanel)

//

            // WINDOW LISTENERS
            window.onresize = app.onResize; // Use some kind of timer to reduce repaints / DOM manipulations
            window.addEventListener("blur", app.onBlur);

            window.onpopstate = app.nav.onPopState;
        }
    }



    // INITIALISE
    var init = function(){
        // Get UI Language from GWT parameters
		app.data.UILanguage = jahiaGWTParameters.uilang.toUpperCase();

		// use Dex to cache an Dex Object
		DexV2("body").cache("body");

        // Register CK Editor version ( needed by CSS )
        DexV2.getCached("body").setAttribute("data-CKEDITOR-VERSION", app.data.ckeditorVersion);

        // This is a content picker, not main app.
		if(app.data.HTTP.app == "contentpicker"){
			// This is a full page picker, not edit engine
			app.picker.data.standalone = true;
			app.picker.data.ID = app.picker.data.standaloneID;

			// Need to "open" the picker manually ...
			DexV2.tag("body").onOpen("#JahiaGxtContentPicker", function(){
				app.picker.onOpen();
			});

		}

        // This is a manager, not main app.
		if(app.data.HTTP.app == "manager"){
			// This is a manager, not edit engine
			app.picker.data.standalone = true;
			app.picker.data.ID = app.picker.data.standaloneManagerID;

			// Need to "open" the picker manually ...
			DexV2.tag("body").onOpen("#contentmanager > .x-viewport", function(){
				app.picker.onOpen();

                if(app.data.HTTP.picker == "portletmanager-anthracite"){
                    // Select list view
                    DexV2("#contentmanager #JahiaGxtManagerToolbar .action-bar-tool-item.toolbar-item-listview").trigger("click");
                }
			});


		}

        // Attach event listeners
        eventListeners.attach();




        // ======= Menu (jahia logo animation) =======

        var menu
        menu = DexV2.class("editmode-managers-menu").nodes[0]

        // Preload images for frame
        var images = new Array()

        function preload() {
        	for (i = 0; i < preload.arguments.length; i++) {
        		images[i] = new Image()
        		images[i].src = preload.arguments[i]
        	}
        }
        preload(
        	"./images/logo_frame_1.png",
          "./images/logo_frame_2.png",
          "./images/logo_frame_3.png",
          "./images/logo_frame_4.png",
          "./images/logo_frame_5.png",
          "./images/logo_frame_6.png",
          "./images/logo_frame_7.png",
          "./images/logo_frame_8.png",
          "./images/logo_frame_9.png",
        )

        // frames
        var frame = [
        	"url(./images/logo_frame_1.png)",
          "url(./images/logo_frame_2.png)",
          "url(./images/logo_frame_3.png)",
          "url(./images/logo_frame_4.png)",
          "url(./images/logo_frame_5.png)",
          "url(./images/logo_frame_6.png)",
          "url(./images/logo_frame_7.png)",
          "url(./images/logo_frame_8.png)",
          "url(./images/logo_frame_9.png)",

        function logoAnim(invert) {
        	for (let i = 0; i <= 8; i++) {
        		time = 40*(i+1);	// 25fps
        		setTimeout(function() {
        			menu.style.backgroundImage = invert ? frame[9-i]: frame[i];
        		}, time);
        	}
        }

        menu.onmouseenter = function() {
        	logoAnim();
        };

        menu.onmouseleave = function() {
        	logoAnim(invert=true);
        }


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
