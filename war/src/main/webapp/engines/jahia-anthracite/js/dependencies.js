// Polyfills
if (!Element.prototype.matches) {
    Element.prototype.matches =
        Element.prototype.matchesSelector ||
        Element.prototype.mozMatchesSelector ||
        Element.prototype.msMatchesSelector ||
        Element.prototype.oMatchesSelector ||
        Element.prototype.webkitMatchesSelector ||
        function (s) {
            var matches = (this.document || this.ownerDocument).querySelectorAll(s),
                i = matches.length;
            while (--i >= 0 && matches.item(i) !== this) {}
            return i > -1;
        };
}

// Source: https://github.com/jserz/js_piece/blob/master/DOM/ParentNode/prepend()/prepend().md
(function (arr) {
    arr.forEach(function (item) {
        if (item.hasOwnProperty('prepend')) {
            return;
        }
        Object.defineProperty(item, 'prepend', {
            configurable: true,
            enumerable: true,
            writable: true,
            value: function prepend() {
                var argArr = Array.prototype.slice.call(arguments),
                    docFrag = document.createDocumentFragment();

                argArr.forEach(function (argItem) {
                    var isNode = argItem instanceof Node;
                    docFrag.appendChild(isNode ? argItem : document.createTextNode(String(argItem)));
                });

                this.insertBefore(docFrag, this.firstChild);
            }
        });
    });
})([Element.prototype, Document.prototype, DocumentFragment.prototype]);

(function (exposeAs) {

    /**
     * Returns a Dex Object with functions allowing us to manipulate nodes in nodelist
     * @param selector
     * @param nodes - optional, if not passed then the node list is built from the nodes matching the selector
     * @returns {Dex.init|Dex}
     * @constructor
     */
    var Dex = function (selector, nodes) {
        return new Dex.fn.init(selector, nodes);
    };

    Dex.fn = Dex.prototype = {
        /**
         * init
         * @param selector
         * @param nodes
         * @returns {Dex}
         */
        init: function (selector, nodes) {
            this.selector = selector;

            if (nodes) {
                this.nodes = nodes;
            } else {
                this.nodes = document.querySelectorAll(selector);
            }

            return this;
        }
    };

    // HIDDEN VARIABLES
    var cachedSelections = {};
    var delegatedEventListeners = {};
    var mutationObservers = {};
    /**
     * DOMMutationCallback
     * @param mutations
     * @param selector
     * @constructor
     */
    var DOMMutationCallback = function (mutations, selector) {
        var mutationRecord;
        var addedNode;
        var removedNode;
        var modifiedNodeAttribute;
        var callbacks;
        var modifiedSelector;
        var groupedCallbacks;
        /**
         * executeCallbacks
         * @param queue
         * @param node
         * @param arg1
         * @param arg2
         */
        var executeCallbacks = function (queue, node, arg1, arg2) {
            for (var mutation_id in queue) {
                // Call callback function
                queue[mutation_id].callback.call(node, arg1, arg2);

                if (!queue[mutation_id].persistant) {
                    delete queue[mutation_id];
                }
            }
        };
        /**
         * executeAttributecallbacks
         * @param queue
         * @param node
         * @param attrKey
         * @param attrValue
         */
        var executeAttributecallbacks = function (queue, node, attrKey, attrValue) {
            for (var mutation_id in queue) {
                if (queue[mutation_id].attrKey == attrKey) {

                    queue[mutation_id].callback.call(node, attrKey, attrValue);

                    if (!queue[mutation_id].persistant) {
                        delete queue[mutation_id];
                    }

                }
            }
        };

        // Loop through mutation records
        for (var n = 0; n < mutations.length; n++) {
            mutationRecord = mutations[n];

            // Loop through added nodes
            if (mutationRecord.addedNodes.length > 0) {
                groupedCallbacks = mutationObservers[selector].callbacks.onGroupOpen;

                if (groupedCallbacks) {
                    addedNode = mutationRecord.addedNodes[0];

                    if (addedNode.nodeType == 1) {
                        for (var gCallback in groupedCallbacks) {
                            modifiedSelector = groupedCallbacks[gCallback].matchType.modifiedSelector;

                            // See if addedNode matches the gCallback of the callback
                            switch (groupedCallbacks[gCallback].matchType.type) {
                                case 'tag':
                                    if (addedNode.tagName.toUpperCase() == modifiedSelector) {
                                        // Loop through all callbacks
                                        executeCallbacks(groupedCallbacks[gCallback].queue, mutationRecord.addedNodes, addedNode);
                                    }
                                    break;
                                case 'id':
                                    if (addedNode.id == modifiedSelector) {
                                        // Loop through all callbacks
                                        executeCallbacks(groupedCallbacks[gCallback].queue, mutationRecord.addedNodes, addedNode);
                                    }
                                    break;
                                case 'classname':
                                    if (addedNode.classList.contains(modifiedSelector)) {
                                        // Loop through all callbacks
                                        executeCallbacks(groupedCallbacks[gCallback].queue, mutationRecord.addedNodes, addedNode);
                                    }
                                    break;
                                case 'complex':
                                    if (addedNode.matches && addedNode.matches(modifiedSelector)) {
                                        // Loop through all callbacks
                                        executeCallbacks(groupedCallbacks[gCallback].queue, mutationRecord.addedNodes, addedNode);
                                    }
                                    break;
                            }
                        }
                    }
                }

                callbacks = mutationObservers[selector].callbacks.onOpen;

                if (callbacks) {
                    for (var i = 0; i < mutationRecord.addedNodes.length; i++) {
                        addedNode = mutationRecord.addedNodes[i];

                        // Check if node type is valid
                        if (addedNode.nodeType == 1) {
                            // Loop through callbacks
                            for (var cb in callbacks) {
                                modifiedSelector = callbacks[cb].matchType.modifiedSelector;

                                // See if addedNode matches the cb of the callback
                                switch (callbacks[cb].matchType.type) {
                                    case 'tag':
                                        if (addedNode.tagName.toUpperCase() == modifiedSelector) {
                                            // Loop through all callbacks
                                            executeCallbacks(callbacks[cb].queue, addedNode, mutationRecord.addedNodes);
                                        }
                                        break;
                                    case 'id':
                                        if (addedNode.id == modifiedSelector) {
                                            // Loop through all callbacks
                                            executeCallbacks(callbacks[cb].queue, addedNode, mutationRecord.addedNodes);
                                        }
                                        break;
                                    case 'classname':
                                        if (addedNode.classList.contains(modifiedSelector)) {
                                            // Loop through all callbacks
                                            executeCallbacks(callbacks[cb].queue, addedNode, mutationRecord.addedNodes);
                                        }
                                        break;
                                    case 'complex':
                                        if (addedNode.matches && addedNode.matches(modifiedSelector)) {
                                            // Loop through all callbacks
                                            executeCallbacks(callbacks[cb].queue, addedNode, mutationRecord.addedNodes);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            if (mutationRecord.removedNodes.length > 0) {
                groupedCallbacks = mutationObservers[selector].callbacks.onGroupClose;
                if (groupedCallbacks) {
                    removedNode = mutationRecord.removedNodes[0];

                    if (removedNode.nodeType == 1) {
                        for (var currentCallback in groupedCallbacks) {
                            modifiedSelector = groupedCallbacks[currentCallback].matchType.modifiedSelector;

                            // See if removedNode matches the currentCallback of the callback
                            switch (groupedCallbacks[currentCallback].matchType.type) {
                                case 'tag':
                                    if (removedNode.tagName.toUpperCase() == modifiedSelector) {
                                        // Loop through all callbacks
                                        executeCallbacks(groupedCallbacks[currentCallback].queue, mutationRecord.removedNodes, removedNode);
                                    }
                                    break;
                                case 'id':
                                    if (removedNode.id == modifiedSelector) {
                                        // Loop through all callbacks
                                        executeCallbacks(groupedCallbacks[currentCallback].queue, mutationRecord.removedNodes, removedNode);
                                    }
                                    break;
                                case 'classname':
                                    if (removedNode.classList.contains(modifiedSelector)) {
                                        // Loop through all callbacks
                                        executeCallbacks(groupedCallbacks[currentCallback].queue, mutationRecord.removedNodes, removedNode);
                                    }
                                    break;
                                case 'complex':
                                    if (removedNode.matches(modifiedSelector)) {
                                        // Loop through all callbacks
                                        executeCallbacks(groupedCallbacks[currentCallback].queue, mutationRecord.removedNodes, removedNode);
                                    }
                                    break;
                            }
                        }
                    }
                }

                callbacks = mutationObservers[selector].callbacks.onClose;

                if (callbacks) {
                    // Loop through removed nodes
                    for (var x = 0; x < mutationRecord.removedNodes.length; x++) {
                        removedNode = mutationRecord.removedNodes[x];

                        // Check if node type is valid
                        if (removedNode.nodeType == 1) {
                            // Loop through callbacks
                            for (var callback in callbacks) {
                                modifiedSelector = callbacks[callback].matchType.modifiedSelector;

                                // See if removedNode matches the _target of the callback
                                switch (callbacks[callback].matchType.type) {
                                    case 'tag':
                                        if (removedNode.tagName.toUpperCase() == modifiedSelector) {
                                            // Loop through all callbacks
                                            executeCallbacks(callbacks[callback].queue, removedNode);
                                        }
                                        break;
                                    case 'id':
                                        if (removedNode.id == modifiedSelector) {
                                            // Loop through all callbacks
                                            executeCallbacks(callbacks[callback].queue, removedNode);
                                        }
                                        break;
                                    case 'classname':
                                        if (removedNode.classList.contains(modifiedSelector)) {
                                            // Loop through all callbacks
                                            executeCallbacks(callbacks[callback].queue, removedNode);
                                        }
                                        break;
                                    case 'complex':
                                        if (removedNode.matches && removedNode.matches(modifiedSelector)) {
                                            // Loop through all callbacks
                                            executeCallbacks(callbacks[callback].queue, removedNode);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            if (mutationRecord.attributeName) {
                callbacks = mutationObservers[selector].callbacks.onAttribute;

                if (callbacks) {
                    // Loop through modified attributes
                    modifiedNodeAttribute = mutationRecord.target;

                    // Loop through callbacks
                    for (var _target in callbacks) {
                        modifiedSelector = callbacks[_target].matchType.modifiedSelector;

                        // See if modifiedNodeAttribute matches the _target of the callback
                        switch (callbacks[_target].matchType.type) {
                            case 'tag':
                                if (modifiedNodeAttribute.tagName.toUpperCase() == modifiedSelector) {
                                    // Loop through all callbacks
                                    if (mutationRecord.target.attributes[mutationRecord.attributeName]) { // Its a live list, so check it hasnt been removed
                                        executeAttributecallbacks(
                                            callbacks[_target].queue, modifiedNodeAttribute,
                                            mutationRecord.attributeName,
                                            mutationRecord.target.attributes[mutationRecord.attributeName].value);
                                    }
                                }
                                break;
                            case 'id':
                                if (modifiedNodeAttribute.id == modifiedSelector) {
                                    // Loop through all callbacks
                                    if (mutationRecord.target.attributes[mutationRecord.attributeName]) { // Its a live list, so check it hasnt been removed
                                        executeAttributecallbacks(callbacks[_target].queue, modifiedNodeAttribute,
                                            mutationRecord.attributeName,
                                            mutationRecord.target.attributes[mutationRecord.attributeName].value);
                                    }
                                }
                                break;
                            case 'classname':
                                if (modifiedNodeAttribute.classList.contains(modifiedSelector)) {
                                    // Loop through all callbacks
                                    if (mutationRecord.target.attributes[mutationRecord.attributeName]) { // Its a live list, so check it hasnt been removed
                                        executeAttributecallbacks(callbacks[_target].queue, modifiedNodeAttribute,
                                            mutationRecord.attributeName,
                                            mutationRecord.target.attributes[mutationRecord.attributeName].value);
                                    }
                                }
                                break;
                            case 'complex':
                                if (modifiedNodeAttribute.matches && modifiedNodeAttribute.matches(modifiedSelector)) {
                                    // Loop through all callbacks
                                    if (mutationRecord.target.attributes[mutationRecord.attributeName]) { // Its a live list, so check it hasnt been removed
                                        executeAttributecallbacks(callbacks[_target].queue, modifiedNodeAttribute,
                                            mutationRecord.attributeName,
                                            mutationRecord.target.attributes[mutationRecord.attributeName].value);
                                    }
                                }
                                break;
                        }
                    }
                }
            }
        }
    };

    var eventListenerCounter = 0;
    /**
     * generateListenerID
     * @returns {string}
     */
    var generateListenerID = function () {
        eventListenerCounter++;
        return 'EVENT_ID_' + eventListenerCounter;
    };
    /**
     * Registers an event listener and sends it to attachEventListeners() for the actual creation
     *
     * @param dexObject - object to which the event listener is attached
     * @param eventType - change|input|focusin|focusout|click|mouseup|mousedown|mouseenter|mouseleave|mouseover|mouseout
     * @param selector -
     * @param target
     * @param callback - function to execute when the event triggers
     * @param persistant - if set to false, then the event listener is removed once first executed
     * @param event_id - a unique ID to identify the event listener ( needed to allow us to remove it when neccessary )
     */
    var createEventListener = function (dexObject, eventType, selector, target, callback, persistant, event_id) {
        // Check that the event type exists in queue
        if (!delegatedEventListeners[eventType]) {
            delegatedEventListeners[eventType] = {};
        }

        // Check that this selector is registered
        if (!delegatedEventListeners[eventType][selector]) {
            delegatedEventListeners[eventType][selector] = {};
        }

        // Setup listener
        attachEventListeners(eventType, dexObject);

        // Check that this target is registered
        if (!delegatedEventListeners[eventType][selector][target]) {
            delegatedEventListeners[eventType][selector][target] = [];
        }

        // register delegated event listener
        delegatedEventListeners[eventType][selector][target][event_id || generateListenerID()] = {
            callback: callback,
            persistant: persistant
        };
    };
    /**
     * Actually creates and add the event listener
     * @param eventType
     * @param dexObject
     */
    var attachEventListeners = function (eventType, dexObject) {
        var selector = {
            string: dexObject.selector
        };

        for (var n = 0; n < dexObject.nodes.length; n++) {
            selector.node = dexObject.nodes[n];

            if (!selector.node.indigo) {
                selector.node.indigo = {
                    listeners: {}
                };
            }

            if (!selector.node.indigo.listeners[eventType]) {
                // No event listener attached, attach now
                selector.node.indigo.listeners[eventType] = 'on';

                selector.node.addEventListener(eventType, function (e) {
                    var eventHandlers = delegatedEventListeners[eventType][selector.string];

                    for (var handlerSelector in eventHandlers) {
                        var clickedNode = e.target;
                        while (clickedNode &&
                        clickedNode !== document &&
                        clickedNode !== selector.node) { // Stop looking when we hit the node that contains the event listener
                            if (clickedNode.matches(handlerSelector)) {
                                for (var handlerIndex in eventHandlers[handlerSelector]) {
                                    eventHandlers[handlerSelector][handlerIndex].callback.call(clickedNode, e);

                                    if (eventHandlers[handlerSelector] && eventHandlers[handlerSelector][handlerIndex] && !eventHandlers[handlerSelector][handlerIndex].persistant) {
                                        delete eventHandlers[handlerSelector][handlerIndex];
                                    }
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
        /**
         * Remove all nodes except first from node list
         * @returns {Dex.fn.init}
         */
        first: function () {
            this.nodes = [this.nodes[0]];
            return this;
        },
        /**
         * Remove all nodes except requested index
         * @param index - index of the node to be kept
         * @returns {Dex.fn.init}
         */
        index: function (index) {
            this.nodes = [this.nodes[index]];
            return this;
        },
        /**
         * Return DOM node
         * @param index of node to return
         * @returns {*}
         */
        getNode: function (index) {
            return this.nodes[index];
        },
        /**
         * See if the node list contains at least one node
         * @returns {boolean}
         */
        exists: function () {
            return this.nodes[0] != null;
        },
        /**
         * Clones the passed node and appends
         * @param node
         * @returns {Dex.fn.init}
         */
        appendClone: function (node) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].appendChild(node.cloneNode(true));
            }

            return this;
        },
        /**
         * append the actual passed node
         * @param node
         * @returns {Dex.fn.init}
         */
        append: function (node) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].appendChild(node);
            }

            return this;
        },
        /**
         * prepends the actual node
         * @param node
         * @returns {Dex.fn.init}
         */
        prepend: function (node) {
            var parentNode;
            for (var n = 0; n < this.nodes.length; n++) {
                parentNode = this.nodes[n];
                parentNode.insertBefore(node, parentNode.children[0]);
            }

            return this;
        },
        /**
         * remove all matching nodes
         * @returns {Dex.fn.init}
         */
        remove: function () {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n];

                if (this.nodes[n] && this.nodes[n].parentNode) {
                    this.nodes[n].parentNode.removeChild(this.nodes[n]);
                }
            }

            return this;
        },
        /**
         * clone all matching nodes and return new node list of cloned nodes
         * @returns {Dex.fn.init}
         */
        clone: function () {
            var clonedNodes = [];
            for (var n = 0; n < this.nodes.length; n++) {
                clonedNodes.push(this.nodes[n].cloneNode(true));
            }
            this.nodes = clonedNodes;

            return this;
        },
        /**
         * Replace Dex Object node list with parent node of first node
         * @returns {Dex.fn.init}
         */
        parent: function () {
            if (this.nodes[0] && this.nodes[0].parentNode) {
                this.nodes = [this.nodes[0].parentNode];
            } else {
                this.nodes = [];
            }

            return this;
        },
        /**
         * Go up the tree of the first node and return the first node that matches the matchSelector
         * @param matchSelector
         * @returns {Dex.fn.init}
         */
        closest: function (matchSelector) {
            var closest = [];
            var node = this.nodes[0];
            // Go through parents
            while (node && node !== document) {
                if (node.matches(matchSelector)) {
                    closest = [node];
                    break;
                }
                node = node.parentNode;
            }
            this.nodes = closest;

            return this;
        },
        /**
         * Apply callback to each node in the list
         * @param callback
         * @returns {Dex.fn.init}
         */
        each: function (callback) {
            for (var n = 0; n < this.nodes.length; n++) {
                callback.call(this.nodes[n], Dex.node(this.nodes[n]), n);
            }

            return this;
        },
        /**
         * Filter then current node list - note: only filters on the first node in list
         * @param selector
         * @returns {Dex.fn.init}
         */
        filter: function (selector) {
            if (this.nodes[0]) {
                this.nodes = this.nodes[0].querySelectorAll(selector);
            } else {
                this.nodes = [];
            }

            return this;
        },

        /**
         * Set innerHTML of all nodes in nodelist
         * @param value
         * @returns {Dex.fn.init}
         */
        setHTML: function (value) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].innerHTML = value;
            }

            return this;
        },
        /**
         * Get innerHTML of first node in nodelist
         * @returns {null}
         */
        getHTML: function () {
            return (this.nodes[0]) ? this.nodes[0].innerHTML : null;
        },
        /**
         * Set CSS of all nodes in nodelist
         * @param styles
         * @returns {Dex.fn.init}
         */
        css: function (styles) {
            for (var n = 0; n < this.nodes.length; n++) {
                for (var style in styles) {
                    this.nodes[n].style[style] = styles[style];
                }
            }

            return this;
        },
        /**
         * Set attribute of all nodes in nodelist
         * @param key
         * @param value
         * @returns {Dex.fn.init}
         */
        setAttribute: function (key, value) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].setAttribute(key, value);
            }

            return this;
        },
        /**
         * Get attribute of first node in nodelist
         * @param key
         * @returns {*}
         */
        getAttribute: function (key) {
            return (this.nodes[0]) ? this.nodes[0].getAttribute(key) : null;
        },
        /**
         * Toggle the attributes of all nodes in list
         * @param key
         * @param value - array with two values to be toggled
         * @returns {Dex.fn.init}
         */
        toggleAttribute: function (key, value) {
            var attrValue;
            for (var n = 0; n < this.nodes.length; n++) {
                attrValue = this.nodes[n].getAttribute(key);

                if (attrValue == value[0]) {
                    this.nodes[n].setAttribute(key, value[1]);
                } else {
                    this.nodes[n].setAttribute(key, value[0]);
                }
            }

            return this;
        },
        /**
         * Cache node list with cacheID as ID
         * @param cacheID
         */
        cache: function (cacheID) {
            cachedSelections[cacheID] = this;
        },

        /**
         * Add class to all nodes in nodelist
         * @param classname
         * @returns {Dex.fn.init}
         */
        addClass: function (classname) {
            for (var n = 0; n < this.nodes.length; n++) {
                if (this.nodes[n]) {
                    this.nodes[n].classList.add(classname);
                }
            }

            return this;
        },
        /**
         * Remove class from all nodes in nodelist
         * @param classname
         * @returns {Dex.fn.init}
         */
        removeClass: function (classname) {
            for (var n = 0; n < this.nodes.length; n++) {
                if (this.nodes[n]) {
                    this.nodes[n].classList.remove(classname);
                }
            }
            return this;
        },
        /**
         * Check whether first node in list contains a classname
         * @param classname
         * @returns {boolean}
         */
        hasClass: function (classname) {
            var result = false;
            if (this.nodes[0]) {
                result = this.nodes[0].classList.contains(classname);
            }

            return result;
        },
        /**
         * Toggle classnames on all nodes in nodelist
         * @param classname
         * @returns {Dex.fn.init}
         */
        toggleClass: function (classname) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].classList.toggle(classname);
            }

            return this;
        },
        /**
         * Replace old_classname with new_classname on all nodes in nodelist
         * @param old_classname
         * @param new_classname
         * @returns {Dex.fn.init}
         */
        replaceClass: function (old_classname, new_classname) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].classList.replace(old_classname, new_classname);
            }

            return this;
        },
        /**
         * Trigger event on all nodes in nodelist
         * @param eventType - click|mouseover|etc|...
         * @param xPos - optional X coordinate of mouse
         * @param yPos - optional Y coordinate of mouse
         * @returns {Dex.fn.init}
         */
        trigger: function (eventType, xPos, yPos) {
            xPos = xPos || 0;
            yPos = yPos || 0;

            if (this.nodes[0]) {
                var clickEvent = document.createEvent('MouseEvents');
                if (eventType == 'contextmenu') {
                    clickEvent.initMouseEvent(eventType, true, false, window, 0, 0, 0, xPos, yPos, false, false, false, false, 2, null);
                } else {
                    clickEvent.initEvent(eventType, true, true);
                }
                this.nodes[0].dispatchEvent(clickEvent);
            }

            return this;
        },
        /**
         * Trigger custom event on all nodes in nodelist
         * @param eventType
         * @param params
         * @returns {Dex.fn.init}
         */
        customTrigger: function (eventType, params) {
            params = params || {};
            if (this.nodes[0]) {
                var evt = new CustomEvent(eventType, { bubbles: true, cancelable: true, detail: params });
                this.nodes[0].dispatchEvent(evt);
            }

            return this;

        },
        /**
         * Attach listener that executes whenever the target is added to the DOM (persistent)
         * @param target
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onOpen: function (target, callback, mutation_id) {
            this.onMutation('onOpen', target, callback, {
                children: false,
                persistant: true,
                mutation_id: mutation_id
            });

            return this;
        },
        /**
         * Attach listener that executes ONCE when the target is first added to the DOM (non-persistent)
         * @param target
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onceOpen: function (target, callback, mutation_id) {
            this.onMutation('onOpen', target, callback, {
                children: false,
                persistant: false,
                mutation_id: mutation_id
            });

            return this;
        },
        /**
         * Attach listener that executes whenever the target is added to the DOM (persistent)
         *  - If multiple matches are added at the same time then the callback is only executed once all items have been detected
         * @param target
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onGroupOpen: function (target, callback, mutation_id) {
            this.onMutation('onGroupOpen', target, callback, {
                children: false,
                persistant: true,
                mutation_id: mutation_id,
                groupNodes: true
            });

            return this;
        },
        /**
         * Attach listener that executes ONCE the target is added to the DOM (non-persistent)
         *  - If multiple matches are added at the same time then the callback is only executed once all items have been detected
         * @param target
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onceGroupOpen: function (target, callback, mutation_id) {
            this.onMutation('onGroupOpen', target, callback, {
                children: false,
                persistant: false,
                mutation_id: mutation_id,
                groupNodes: true
            });

            return this;
        },
        /**
         * Attach listener that executes whenever the target is removed from the DOM (persistent)
         * @param target
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onClose: function (target, callback, mutation_id) {

            this.onMutation('onClose', target, callback, {
                children: false,
                persistant: true,
                mutation_id: mutation_id
            });

            return this;
        },
        /**
         * onGroupClose
         * @param target
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onGroupClose: function (target, callback, mutation_id) {
            this.onMutation('onGroupClose', target, callback, {
                children: false,
                persistant: true,
                mutation_id: mutation_id,
                groupNodes: true
            });

            return this;
        },
        /**
         * onceGroupClose
         * @param target
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onceGroupClose: function (target, callback, mutation_id) {
            this.onMutation('onGroupClose', target, callback, {
                children: false,
                persistant: false,
                mutation_id: mutation_id,
                groupNodes: true
            });

            return this;
        },
        /**
         * onceClose
         * @param target
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onceClose: function (target, callback, mutation_id) {

            this.onMutation('onClose', target, callback, {
                children: false,
                persistant: false,
                mutation_id: mutation_id
            });

            return this;
        },
        /**
         * Attach listener that executes whenever the attribute on the target is changed (created, modified or removed)
         * @param target
         * @param attrKey
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onAttribute: function (target, attrKey, callback, mutation_id) {
            this.onMutation('onAttribute', target, callback, {
                attrKey: attrKey,
                persistant: true,
                mutation_id: mutation_id
            });

            return this;
        },
        /**
         * onceAttribute
         * @param target
         * @param attrKey
         * @param callback
         * @param mutation_id
         * @returns {Dex.fn.init}
         */
        onceAttribute: function (target, attrKey, callback, mutation_id) {
            this.onMutation('onAttribute', target, callback, {
                attrKey: attrKey,
                persistant: false,
                mutation_id: mutation_id
            });

            return this;
        },
        /**
         * onMutation
         * @param mutationType
         * @param target
         * @param callback
         * @param parameters
         */
        onMutation: function (mutationType, target, callback, parameters) {
            var selector = this.selector;
            var parentNodes = this.nodes;
            var matchType = function (target) {
                // This needs updating to cater for complex selectors.
                var result;
                var regexpressions = {
                    id: /^#[a-z]*$/gi,
                    classname: /^\.[a-z]*$/gi,
                    tagname: /^[a-z]*$/gi
                };

                if (target.match(regexpressions.id)) {
                    // Its a simple ID tag
                    result = {
                        type: 'id',
                        modifiedSelector: target.slice(1)
                    };
                } else if (target.match(regexpressions.classname)) {
                    // Its a simple ID tag
                    result = {
                        type: 'classname',
                        modifiedSelector: target.slice(1)
                    };
                } else if (target.match(regexpressions.tagname)) {
                    // Its a simple ID tag
                    result = {
                        type: 'tag',
                        modifiedSelector: target.toUpperCase()
                    };
                } else {
                    // Its a simple ID tag
                    result = {
                        type: 'complex',
                        modifiedSelector: target
                    };
                }

                return result;
            }(target);

            parameters = parameters || {};

            // See if we need to attach a mutation observer
            if (!mutationObservers[this.selector]) {
                mutationObservers[this.selector] = {
                    observer: new MutationObserver(function (mutations) {
                        DOMMutationCallback(mutations, selector, parentNodes);
                    }),
                    callbacks: {}
                };
            }

            // Attach observer to all matches nodes
            for (var n = 0; n < this.nodes.length; n++) {
                mutationObservers[this.selector].observer.observe(this.nodes[n], {
                    attributes: true,
                    childList: true,
                    characterData: false,
                    subtree: true
                });
            }

            // See if there are already callbacks for mutationType
            if (!mutationObservers[this.selector].callbacks[mutationType]) {
                mutationObservers[this.selector].callbacks[mutationType] = {};
            }

            // See if there are already calbacks for this target
            if (!mutationObservers[this.selector].callbacks[mutationType][target]) {
                // mutationObservers[this.selector][childtree].callbacks[mutationType][target] = []
                mutationObservers[this.selector].callbacks[mutationType][target] = {
                    matchType: matchType,
                    queue: {}
                };
            }

            // Save callback
            mutationObservers[this.selector].callbacks[mutationType][target].queue[parameters.mutation_id || generateListenerID()] = {
                callback: callback,
                attrKey: parameters.attrKey,
                persistant: parameters.persistant,
                groupNodes: parameters.groupNodes
            };
        },
        /**
         * Callback executed when the target receives input (must be an input tag)
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onInput: function (target, callback, event_id) {
            createEventListener(this, 'input', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target value changes (must be an input tag)
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onChange: function (target, callback, event_id) {
            createEventListener(this, 'change', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target receives focus
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onFocus: function (target, callback, event_id) {
            createEventListener(this, 'focusin', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target loses focus
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onBlur: function (target, callback, event_id) {
            createEventListener(this, 'focusout', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target is clicked
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
		 onClick: function (target, callback, event_id) {
             createEventListener(this, 'click', this.selector, target, callback, true, event_id);
             return this;
         },
		 onKeyDown: function (target, callback, event_id) {
             createEventListener(this, 'keydown', this.selector, target, callback, true, event_id);
             return this;
         },
		 onKeyUp: function (target, callback, event_id) {
             createEventListener(this, 'keyup', this.selector, target, callback, true, event_id);
             return this;
         },
		 onKeyPress: function (target, callback, event_id) {
             createEventListener(this, 'keypress', this.selector, target, callback, true, event_id);
             return this;
         },
        /**
         * Callback executed when the target is clicked
         *  - The callback is removed after it is executed (non-persitant)
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        oneClick: function (target, callback, event_id) {
            createEventListener(this, 'click', this.selector, target, callback, false, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse up
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onMouseUp: function (target, callback, event_id) {
            createEventListener(this, 'mouseup', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse up
         *  - The callback is removed after it is executed (non-persitant)
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        oneMouseUp: function (target, callback, event_id) {
            createEventListener(this, 'mouseup', this.selector, target, callback, false, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse down
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onMouseDown: function (target, callback, event_id) {
            createEventListener(this, 'mousedown', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse down
         *  - The callback is removed after it is executed (non-persitant)
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        oneMouseDown: function (target, callback, event_id) {
            createEventListener(this, 'mousedown', this.selector, target, callback, false, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse enter
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onMouseEnter: function (target, callback, event_id) {
            createEventListener(this, 'mouseenter', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse enter
         *  - The callback is removed after it is executed (non-persitant)
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        oneMouseEnter: function (target, callback, event_id) {
            createEventListener(this, 'mouseenter', this.selector, target, callback, false, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse leave
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onMouseLeave: function (target, callback, event_id) {
            createEventListener(this, 'mouseleave', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse leave
         *  - The callback is removed after it is executed (non-persitant)
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        oneMouseLeave: function (target, callback, event_id) {
            createEventListener(this, 'mouseleave', this.selector, target, callback, false, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse over
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onMouseOver: function (target, callback, event_id) {
            createEventListener(this, 'mouseover', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse over
         *  - The callback is removed after it is executed (non-persitant)
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        oneMouseOver: function (target, callback, event_id) {
            createEventListener(this, 'mouseover', this.selector, target, callback, false, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse out
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        onMouseOut: function (target, callback, event_id) {
            createEventListener(this, 'mouseout', this.selector, target, callback, true, event_id);
            return this;
        },
        /**
         * Callback executed when the target registers a mouse out
         *  - The callback is removed after it is executed (non-persitant)
         * @param target
         * @param callback
         * @param event_id
         * @returns {Dex.fn.init}
         */
        oneMouseOut: function (target, callback, event_id) {
            createEventListener(this, 'mouseout', this.selector, target, callback, false, event_id);
            return this;
        }
    };
    /**
     * Appends CSS file to header in <link> tag
     * @param url
     * @returns {HTMLLinkElement}
     */
    Dex.appendCSS = function (url) {
        var head = document.head;
        var link = document.createElement('link');

        link.type = 'text/css';
        link.rel = 'stylesheet';
        link.href = url;

        head.appendChild(link);
        return link;
    };
    /**
     * Returns a cached Dex Object
     * @param cacheID
     * @returns {*}
     */
    Dex.getCached = function (cacheID) {
        return cachedSelections[cacheID];
    };
    /**
     * Removes a cached Dex Object
     * @param cacheID
     */
    Dex.clearCache = function (cacheID) {
        delete cachedSelections[cacheID];
    };
    /**
     * Build node list using a tag selector
     *  - Much quicker than using the general Dex(tag)
     * @param tag
     */
    Dex.tag = function (tag) {
        /* Use Tag() to select nodes using the getElementsByTagName */
        var nodes = document.getElementsByTagName(tag);
        return Dex(tag, nodes);
    };

    /**
     * Build node list using the class selector
     *  - Much quicker than using the general Dex(.class)
     * @param classname
     */
    Dex.class = function (classname) {
        /* Use Tag() to select nodes using the getElementsByClassName */
        var nodeCollection = document.getElementsByClassName(classname);
        var nodes = Array.prototype.slice.call(nodeCollection);
        return Dex('.' + classname, nodes);
    };
    /**
     * Create a Dex Object with the iframes document as index node
     *  - This allows us to manipulate the content of the iframe
     * @param selector
     */
    Dex.iframe = function (selector) {
        var iframe = document.querySelectorAll(selector)[0];
        return Dex.node(iframe.contentDocument || iframe.contentWindow.document);
    };
    /**
     * Build node list using the ID selector
     *  - Much quicker than using the general Dex(#id)
     * @param classname
     */
    Dex.id = function (id) {
        /* Use Tag() to select nodes using the getElementById */
        var nodes = [document.getElementById(id)];
        return Dex('#' + id, nodes);
    };
    /**
     * Return Dex object with the passed node
     * @param node
     * @returns {Dex.init|Dex}
     */
    Dex.node = function (node) {
        /* Use Node to create a Dex object with a DOM node directly */
        return Dex('node', [node]);
    };
    /**
     * Build Dex object by converting a nodeCollection to a normal node list
     * @param nodeCollection
     * @returns {Dex.init|Dex}
     */
    Dex.collection = function (nodeCollection) {
        /* Use Node to create a Dex object with an HTML Node Collection  directly */
        var nodes = [];
        for (var n = 0; n < nodeCollection.length; n++) {
            nodes.push(nodeCollection[n]);
        }

        return Dex('node', nodes);
    };
    /**
     * Dumps a list of all event listeners created with Dex to the console panel
     */
    Dex.dump = function () {
        console.log(mutationObservers);
        console.log(delegatedEventListeners);
    };

    if (exposeAs) {
        window[exposeAs] = Dex;
    }
})('DexV2');
