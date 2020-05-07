(function () {




     /**
      * jGet is a small JavaScript Library enabling DOM traversal and manipulation. (To use jGet in this fashion please refer to {@link jGet.fn})
      *
      * Otherwise, here are some functions that do not require the use of selectors...
      *
      * @property {method} appendCSS Appends CSS file to header in <link> tag
      * @property {method} class Returns a jGet object with elements matching the supplied classname
      * @property {method} clearCache Removes a cached jGet Object
      * @property {method} collection Build jGet object by converting a nodeCollection to a normal node list
      * @property {method} dump Dumps a list of all event listeners created with jGet to the console panel
      * @property {method} getCached Returns a cached jGet Object
      * @property {method} id Returns a jGet object with an element matching the supplied ID
      * @property {method} iframe Create a jGet Object with the iframes content document
      * @property {method} node Return jGet object with the passed node
      * @property {method} tag Returns a jGet object with a elements matching the supplied tag
      * @property {object} fn Contains DOM manipulation methods <br> See {@link jGet.fn}
      * @returns {jGet}
      * @namespace jGet
      */
    var jGet = function (selector, nodes) {
        return new jGet.fn.init(selector, nodes);
    };

    jGet.fn = jGet.prototype = {
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
    var DOMMutationCallback = function (mutations, selector) {
        var mutationRecord;
        var addedNode;
        var removedNode;
        var modifiedNodeAttribute;
        var callbacks;
        var modifiedSelector;
        var groupedCallbacks;
        var executeCallbacks = function (queue, node, arg1, arg2) {
            for (var mutation_id in queue) {
                // Call callback function
                queue[mutation_id].callback.call(node, arg1, arg2);

                if (!queue[mutation_id].persistant) {
                    delete queue[mutation_id];
                }
            }
        };
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
    var generateListenerID = function () {
        eventListenerCounter++;
        return 'EVENT_ID_' + eventListenerCounter;
    };

    var createEventListener = function (jGetObject, eventType, selector, target, callback, persistant, event_id) {
        // Check that the event type exists in queue
        if (!delegatedEventListeners[eventType]) {
            delegatedEventListeners[eventType] = {};
        }

        // Check that this selector is registered
        if (!delegatedEventListeners[eventType][selector]) {
            delegatedEventListeners[eventType][selector] = {};
        }

        // Setup listener
        attachEventListeners(eventType, jGetObject);

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

    var attachEventListeners = function (eventType, jGetObject) {
        var selector = {
            string: jGetObject.selector
        };

        for (var n = 0; n < jGetObject.nodes.length; n++) {
            selector.node = jGetObject.nodes[n];

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

    /**
     * jGet allows us to select element(s) in the DOM and manipulate them.
     *
     * Here is a list of methods that can be executed on a set of elements that match the supplied selector
     * @version 8.0
     * @property {method} addClass Add class to all matched elements
     * @property {method} append Append the passed node to all matching elements
     * @property {method} appendClone Appends a clone of the passed node to all matching elements
     * @property {method} cache Cache matched elements with cacheID as UID
     * @property {method} clone Clone all matching elements and return as jGet object
     * @property {method} closest Traverse up through the DOM tree until the matchSelector is found and return it as a jGet object
     * @property {method} css Set CSS of all matched elements
     * @property {method} customTrigger Trigger custom event on all matched elements
     * @property {method} each Apply the callback to all matched elements
     * @property {method} exists Check if there is at least one matched element
     * @property {method} filter Filter the first matched element with the passed selector and return as jGet object
     * @property {method} first Reduce matched elements to the first in the list
     * @property {method} getAttribute Get attribute of first matched elements
     * @property {method} getHTML Get innerHTML of first matched element
     * @property {method} getNode Return the HTML node at the specified index
     * @property {method} hasClass Check whether first matched element contains classname
     * @property {method} index Remove all matched elements except the one at the specified index
     * @property {method} onAttribute Attach listeners to matched elements that execute whenever their specified attribute changes (create, modify or remove)
     * @property {method} onBlur Callback executed when matched elements lose focus
     * @property {method} onceAttribute Attach listeners to matched elements that execute whenever their specified attribute changes (create, modify or remove)
     * @property {method} onceClose Attach listeners to matched elements that execute execute when they are removed from the DOM
     * @property {method} onceGroupClose Attach listeners to matched elements that execute execute when they are removed from the DOM
     * @property {method} onceGroupOpen Attach listeners to matched elements that execute execute when they are added to the DOM
     * @property {method} onceOpen Attach listeners to matched elements that execute when they are added to the DOM
     * @property {method} onChange Callback executed when matched \<input> elements value changes
     * @property {method} onClick Callback executed when the matched elements are clicked
     * @property {method} onClose Attach listeners to matched elements that execute execute when they are removed from the DOM
     * @property {method} oneClick Callback executed when the matched elements are clicked
     * @property {method} oneMouseDown Callback executed when the mouse is pressed on matched elements
     * @property {method} oneMouseEnter Callback executed when the mouse enters the matched elements
     * @property {method} oneMouseLeave Callback executed when the mouse leaves the matched elements
     * @property {method} oneMouseOut Callback executed when the mouse moves out of the matched elements
     * @property {method} oneMouseOver Callback executed when the mouse moves over the matched elements
     * @property {method} oneMouseUp Callback executed when the mouse is released on matched elements
     * @property {method} onFocus Callback executed when matched elements receive focus
     * @property {method} onGroupClose Attach listeners to matched elements that execute execute when they are removed from the DOM
     * @property {method} onGroupOpen Attach listeners to matched elements that execute execute when they are added to the DOM
     * @property {method} onInput Callback executed when the matched \<input> elements receive input
     * @property {method} onKeyDown Callback executed when key is pressed down on matched \<input> elements
     * @property {method} onKeyPress Callback executed whilst key is pressed on matched \<input> elements
     * @property {method} onKeyUp Callback executed when key is released on matched \<input> elements
     * @property {method} onMouseDown Callback executed when the mouse is pressed on matched elements
     * @property {method} onMouseEnter Callback executed when the mouse enters the matched elements
     * @property {method} onMouseLeave Callback executed when the mouse leaves the matched elements
     * @property {method} onMouseOut Callback executed when the mouse moves out of the matched elements
     * @property {method} onMouseOver Callback executed when the mouse moves over the matched elements
     * @property {method} onMouseUp Callback executed when the mouse is released on matched elements
     * @property {method} onMutation Used by jGet
     * @property {method} onOpen Attach listeners to matched elements that execute when they are added to the DOM
     * @property {method} parent Get the parent node of the first matched element and return as jGet object
     * @property {method} prepend Prepend the passed node to all matching elements
     * @property {method} remove Remove all matching elements from the DOM
     * @property {method} removeClass Remove class from all matched elements
     * @property {method} replaceClass Replace old_classname with new_classname on all matched elements
     * @property {method} setAttribute Set attribute of all matched elements
     * @property {method} setHTML Set innerHTML of all matched elements
     * @property {method} toggleAttribute Toggle the attributes of all matched elements
     * @property {method} toggleClass Toggle classnames on all matched elements
     * @property {method} trigger Trigger event on all matched elements
     * @namespace jGet.fn
     */
    // EXPOSED SELECTOR FUNCTIONS
    jGet.fn.init.prototype = {
         /**
          * Reduce matched elements to the first in the list
          * @memberof jGet.fn
          * @namespace jGet.fn.first
          * @method first
          * @example
          * var buttons = jGet(".myButton"); // matches 5 elements
          *
          * buttons
          *   .first() // Reduces the matched elements to the first in the list
          *   .css({
          *      "color": "red"
          *   });
          *
          * @returns {jGet}
          */
        first: function () {
            this.nodes = [this.nodes[0]];
            return this;
        },
        /**
         * Remove all matched elements except the one at the specified index
         * @memberof jGet.fn
         * @namespace jGet.fn.index
         * @method index
         * @example
         * var buttons = jGet(".myButton"); // matches 5 elements
         *
         * buttons
         *   .index(1) // Reduces the matched elements to the second in the list
         *   .css({
         *      "color": "red"
         *   });
         *
         * @returns {jGet}
         */
        index: function (index) {
            this.nodes = [this.nodes[index]];
            return this;
        },
        /**
         * Return the HTML node at the specified index
         * @memberof jGet.fn
         * @namespace jGet.fn.getNode
         * @method getNode
         * @example
         * // Extract a matched jGet element as a node and add an HTML event listener directly to the node.
         *
         * var buttons = jGet(".myButton"); // matches 5 elements
         * var firstButtonAsNode = buttons.getNode(0);
         *
         * firstButtonAsNode.addEventListener("click", function(){
         *    console.log("Clicked");
         * })
         *
         * @returns {HTMLElement}
         */
        getNode: function (index) {
            return this.nodes[index];
        },
        /**
         * Check if there is at least one matched element
         * @memberof jGet.fn
         * @namespace jGet.fn.exists
         * @method exists
         * @example
         * var myButtons = jGet(".myButton");
         *
         * if (myButtons.exists()) {
         *     console.log("At least one matching button exists");
         * } else {
         *     console.log("Could not find any matching buttons, sorry");
         * }
         *
         * @returns {boolean}
         */
        exists: function () {
            return this.nodes[0] != null;
        },
        /**
         * Appends a clone of the passed node to all matching elements
         * @memberof jGet.fn
         * @namespace jGet.fn.appendClone
         * @method appendClone
         * @returns {jGet}
         */
        appendClone: function (node) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].appendChild(node.cloneNode(true));
            }

            return this;
        },
        /**
         * Append the passed node to all matching elements
         * @memberof jGet.fn
         * @namespace jGet.fn.append
         * @method append
         * @returns {jGet}
         */
        append: function (node) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].appendChild(node);
            }

            return this;
        },
        /**
         * Prepend the passed node to all matching elements
         * @memberof jGet.fn
         * @namespace jGet.fn.prepend
         * @method prepend
         * @returns {jGet}
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
         * Remove all matching elements from the DOM
         * @memberof jGet.fn
         * @namespace jGet.fn.remove
         * @method remove
         * @returns {jGet}
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
         * Clone all matching elements and return as jGet object
         * @memberof jGet.fn
         * @namespace jGet.fn.clone
         * @method clone
         * @returns {jGet}
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
         * Get the parent node of the first matched element and return as jGet object
         * @memberof jGet.fn
         * @namespace jGet.fn.parent
         * @method parent
         * @returns {jGet}
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
         * Traverse up through the DOM tree until the matchSelector is found and return it as a jGet object

         * @memberof jGet.fn
         * @namespace jGet.fn.closest
         * @method closest
         * @returns {jGet}
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
         * Apply the callback to all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.each
         * @method each
         * @returns {jGet}
         */
        each: function (callback) {
            for (var n = 0; n < this.nodes.length; n++) {
                callback.call(this.nodes[n], jGet.node(this.nodes[n]), n);
            }

            return this;
        },
        /**
         * Filter the first matched element with the passed selector and return as jGet object
         * @memberof jGet.fn
         * @namespace jGet.fn.filter
         * @method filter
         * @returns {jGet}
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
         * Set innerHTML of all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.setHTML
         * @method setHTML
         * @returns {jGet}
         */
        setHTML: function (value) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].innerHTML = value;
            }

            return this;
        },
        /**
         * Get innerHTML of first matched element
         * @memberof jGet.fn
         * @namespace jGet.fn.getHTML
         * @method getHTML
         * @returns {string}
         */
        getHTML: function () {
            return (this.nodes[0]) ? this.nodes[0].innerHTML : null;
        },
        /**
         * Set CSS of all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.css
         * @method css
         * @returns {jGet}
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
         * Set attribute of all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.setAttribute
         * @method setAttribute
         * @returns {jGet}
         */
        setAttribute: function (key, value) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].setAttribute(key, value);
            }

            return this;
        },
        /**
         * Get attribute of first matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.getAttribute
         * @method getAttribute
         * @returns {string|null}
         */
        getAttribute: function (key) {
            return (this.nodes[0]) ? this.nodes[0].getAttribute(key) : null;
        },
        /**
         * Toggle the attributes of all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.toggleAttribute
         * @method toggleAttribute
         * @returns {jGet}
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
         * Cache matched elements with cacheID as UID
         * @memberof jGet.fn
         * @namespace jGet.fn.cache
         * @method cache
         */
        cache: function (cacheID) {
            cachedSelections[cacheID] = this;
        },

        /**
         * Add class to all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.addClass
         * @method addClass
         * @returns {jGet}
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
         * Remove class from all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.removeClass
         * @method removeClass
         * @returns {jGet}
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
         * Check whether first matched element contains classname
         * @memberof jGet.fn
         * @namespace jGet.fn.hasClass
         * @method hasClass
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
         * Toggle classnames on all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.toggleClass
         * @method toggleClass
         * @returns {jGet}
         */
        toggleClass: function (classname) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].classList.toggle(classname);
            }

            return this;
        },
        /**
         * Replace old_classname with new_classname on all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.replaceClass
         * @method replaceClass
         * @returns {jGet}
         */
        replaceClass: function (old_classname, new_classname) {
            for (var n = 0; n < this.nodes.length; n++) {
                this.nodes[n].classList.replace(old_classname, new_classname);
            }

            return this;
        },
        /**
         * Trigger event on all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.trigger
         * @method trigger
         * @returns {jGet}
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
         * Trigger custom event on all matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.customTrigger
         * @method customTrigger
         * @returns {jGet}
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
         * Attach listeners to matched elements that execute when they are added to the DOM
         * @memberof jGet.fn
         * @namespace jGet.fn.onOpen
         * @method onOpen
         * @returns {jGet}
         */
        onOpen: function (listener) {
            this.onMutation('onOpen', listener.target, listener.callback, {
                children: false,
                persistant: true,
                mutation_id: listener.uid
            });

            return this;
        },
        /**
         * Attach listeners to matched elements that execute when they are added to the DOM
         *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.onceOpen
         * @method onceOpen
         * @returns {jGet}
         */
        onceOpen: function (listener) {
            this.onMutation('onOpen', listener.target, listener.callback, {
                children: false,
                persistant: false,
                mutation_id: listener.uid
            });

            return this;
        },
        /**
         * Attach listeners to matched elements that execute execute when they are added to the DOM
         *  - If multiple matches are added at the same time then the callback is only executed once all items have been detected
         * @memberof jGet.fn
         * @namespace jGet.fn.onGroupOpen
         * @method onGroupOpen
         * @returns {jGet}
         */
        onGroupOpen: function (listener) {
            this.onMutation('onGroupOpen', listener.target, listener.callback, {
                children: false,
                persistant: true,
                mutation_id: listener.uid,
                groupNodes: true
            });

            return this;
        },
        /**
        * Attach listeners to matched elements that execute execute when they are added to the DOM
         *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.onceGroupOpen
         * @method onceGroupOpen
         * @returns {jGet}
         */
        onceGroupOpen: function (listener) {
            this.onMutation('onGroupOpen', listener.target, listener.callback, {
                children: false,
                persistant: false,
                mutation_id: listener.uid,
                groupNodes: true
            });

            return this;
        },
        /**
        * Attach listeners to matched elements that execute execute when they are removed from the DOM
         * @memberof jGet.fn
         * @namespace jGet.fn.onClose
         * @method onClose
         * @returns {jGet}
         */
        onClose: function (listener) {

            this.onMutation('onClose', listener.target, listener.callback, {
                children: false,
                persistant: true,
                mutation_id: listener.uid
            });

            return this;
        },
        /**
        * Attach listeners to matched elements that execute execute when they are removed from the DOM
        *  - If multiple matches are added at the same time then the callback is only executed once all items have been detected
         * @memberof jGet.fn
         * @namespace jGet.fn.onGroupClose
         * @method onGroupClose
         * @returns {jGet}
         */
        onGroupClose: function (listener) {
            this.onMutation('onGroupClose', listener.target, listener.callback, {
                children: false,
                persistant: true,
                mutation_id: listener.uid,
                groupNodes: true
            });

            return this;
        },
        /**
        * Attach listeners to matched elements that execute execute when they are removed from the DOM
        *  - If multiple matches are added at the same time then the callback is only executed once all items have been detected
        *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.onceGroupClose
         * @method onceGroupClose
         * @returns {jGet}
         */
        onceGroupClose: function (listener) {
            this.onMutation('onGroupClose', listener.target, listener.callback, {
                children: false,
                persistant: false,
                mutation_id: listener.uid,
                groupNodes: true
            });

            return this;
        },
        /**
        * Attach listeners to matched elements that execute execute when they are removed from the DOM
        *  - If multiple matches are added at the same time then the callback is only executed once all items have been detected
        *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.onceClose
         * @method onceClose
         * @returns {jGet}
         */
        onceClose: function (listener) {

            this.onMutation('onClose', listener.target, listener.callback, {
                children: false,
                persistant: false,
                mutation_id: listener.uid
            });

            return this;
        },
        /**
         * Attach listeners to matched elements that execute whenever their specified attribute changes (create, modify or remove)
         * @memberof jGet.fn
         * @namespace jGet.fn.onAttribute
         * @method onAttribute
         * @returns {jGet}
         */
        onAttribute: function (listener) {
            this.onMutation('onAttribute', listener.target, listener.callback, {
                attrKey: listener.attrKey,
                persistant: true,
                mutation_id: listener.uid
            });

            return this;
        },
        /**
        * Attach listeners to matched elements that execute whenever their specified attribute changes (create, modify or remove)
        *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.onceAttribute
         * @method onceAttribute
         * @returns {jGet}
         */
        onceAttribute: function (listener) {
            this.onMutation('onAttribute', listener.target, listener.callback, {
                attrKey: listener.attrKey,
                persistant: false,
                mutation_id: listener.uid
            });

            return this;
        },
        /**
         * Used by jGet
         * @memberof jGet.fn
         * @namespace jGet.fn.onMutation
         * @method onMutation
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

                if (target && target.match(regexpressions.id)) {
                    // Its a simple ID tag
                    result = {
                        type: 'id',
                        modifiedSelector: target.slice(1)
                    };
                } else if (target && target.match(regexpressions.classname)) {
                    // Its a simple ID tag
                    result = {
                        type: 'classname',
                        modifiedSelector: target.slice(1)
                    };
                } else if (target && target.match(regexpressions.tagname)) {
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
         * Callback executed when the matched \<input> elements receive input
         * @memberof jGet.fn
         * @namespace jGet.fn.onInput
         * @method onInput
         * @returns {jGet}
         */
        onInput: function (listener) {
            createEventListener(this, 'input', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
         * Callback executed when matched \<input> elements value changes
         * @memberof jGet.fn
         * @namespace jGet.fn.onChange
         * @method onChange
         * @returns {jGet}
         */
        onChange: function (listener) {
            createEventListener(this, 'change', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
         * Callback executed when matched elements receive focus
         * @memberof jGet.fn
         * @namespace jGet.fn.onFocus
         * @method onFocus
         * @returns {jGet}
         */
        onFocus: function (listener) {
            createEventListener(this, 'focusin', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
         * Callback executed when matched elements lose focus
         * @memberof jGet.fn
         * @namespace jGet.fn.onBlur
         * @method onBlur
         * @returns {jGet}
         */
        onBlur: function (listener) {
            createEventListener(this, 'focusout', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
         * Callback executed when the matched elements are clicked
         * @memberof jGet.fn
         * @namespace jGet.fn.onClick
         * @method onClick
         * @returns {jGet}
         */
		 onClick: function (listener) {
             createEventListener(this, 'click', this.selector, listener.target, listener.callback, true, listener.uid);
             return this;
         },
         /**
          * Callback executed when key is pressed down on matched \<input> elements
          * @memberof jGet.fn
          * @namespace jGet.fn.onKeyDown
          * @method onKeyDown
          * @returns {jGet}
          */
		 onKeyDown: function (listener) {
             createEventListener(this, 'keydown', this.selector, listener.target, listener.callback, true, listener.uid);
             return this;
         },
         /**
         * Callback executed when key is released on matched \<input> elements
          * @memberof jGet.fn
          * @namespace jGet.fn.onKeyUp
          * @method onKeyUp
          * @returns {jGet}
          */
		 onKeyUp: function (listener) {
             createEventListener(this, 'keyup', this.selector, listener.target, listener.callback, true, listener.uid);
             return this;
         },
         /**
         * Callback executed whilst key is pressed on matched \<input> elements
          * @memberof jGet.fn
          * @namespace jGet.fn.onKeyPress
          * @method onKeyPress
          * @returns {jGet}
          */
		 onKeyPress: function (listener) {
             createEventListener(this, 'keypress', this.selector, listener.target, listener.callback, true, listener.uid);
             return this;
         },
        /**
         * Callback executed when the matched elements are clicked
         *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.oneClick
         * @method oneClick
         * @returns {jGet}
         */
        oneClick: function (listener) {
            createEventListener(this, 'click', this.selector, listener.target, listener.callback, false, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse is released on matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.onMouseUp
         * @method onMouseUp
         * @returns {jGet}
         */
        onMouseUp: function (listener) {
            createEventListener(this, 'mouseup', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse is released on matched elements
         *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.oneMouseUp
         * @method oneMouseUp
         * @returns {jGet}
         */
        oneMouseUp: function (listener) {
            createEventListener(this, 'mouseup', this.selector, listener.target, listener.callback, false, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse is pressed on matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.onMouseDown
         * @method onMouseDown
         * @returns {jGet}
         */
        onMouseDown: function (listener) {
            createEventListener(this, 'mousedown', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse is pressed on matched elements
         *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.oneMouseDown
         * @method oneMouseDown
         * @returns {jGet}
         */
        oneMouseDown: function (listener) {
            createEventListener(this, 'mousedown', this.selector, listener.target, listener.callback, false, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse enters the matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.onMouseEnter
         * @method onMouseEnter
         * @returns {jGet}
         */
        onMouseEnter: function (listener) {
            createEventListener(this, 'mouseenter', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse enters the matched elements
         *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.oneMouseEnter
         * @method oneMouseEnter
         * @returns {jGet}
         */
        oneMouseEnter: function (listener) {
            createEventListener(this, 'mouseenter', this.selector, listener.target, listener.callback, false, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse leaves the matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.onMouseLeave
         * @method onMouseLeave
         * @returns {jGet}
         */
        onMouseLeave: function (listener) {
            createEventListener(this, 'mouseleave', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse leaves the matched elements
         *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.oneMouseLeave
         * @method oneMouseLeave
         * @returns {jGet}
         */
        oneMouseLeave: function (listener) {
            createEventListener(this, 'mouseleave', this.selector, listener.target, listener.callback, false, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse moves over the matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.onMouseOver
         * @method onMouseOver
         * @returns {jGet}
         */
        onMouseOver: function (listener) {
            createEventListener(this, 'mouseover', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse moves over the matched elements
         *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.oneMouseOver
         * @method oneMouseOver
         * @returns {jGet}
         */
        oneMouseOver: function (listener) {
            createEventListener(this, 'mouseover', this.selector, listener.target, listener.callback, false, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse moves out of the matched elements
         * @memberof jGet.fn
         * @namespace jGet.fn.onMouseOut
         * @method onMouseOut
         * @returns {jGet}
         */
        onMouseOut: function (listener) {
            createEventListener(this, 'mouseout', this.selector, listener.target, listener.callback, true, listener.uid);
            return this;
        },
        /**
        * Callback executed when the mouse moves out of the matched elements
         *  - The callback is removed after it is executed (non-persitant)
         * @memberof jGet.fn
         * @namespace jGet.fn.oneMouseOut
         * @method oneMouseOut
         * @returns {jGet}
         */
        oneMouseOut: function (listener) {
            createEventListener(this, 'mouseout', this.selector, listener.target, listener.callback, false, listener.uid);
            return this;
        }
    };
    /**
     * Adds a \<link> to the \<head> section of the HTML document targeting the supplied url
     * @memberof jGet
     * @method appendCSS
     * @param url - URL of the CSS to append
     * @example
     * jGet.appendCSS("http://www.example.com/myStyles.css")
     *
     * // Outputs
     * <link type="text/css" rel="stylesheet" href="http://www.example.com/myStyles.css">
     * @returns {HTMLElement}
     */
    jGet.appendCSS = function (url) {
        var head = document.head;
        var link = document.createElement('link');

        link.type = 'text/css';
        link.rel = 'stylesheet';
        link.href = url;

        head.appendChild(link);
        return link;
    };
    /**
     * Returns a cached jGet Object
      * @memberof jGet
      * @method getCached
      * @param cacheID - Unique Identifier for the cached jGet object
      * @example
      * // Assigning cached object to variable
      * var myCachedBody = jGet.getCached("cachedBody");
      * myCachedBody.setAttribute("data-my-name", "lee")
      *
      * // Chaining directly on the cached object
      * jGet.getCached("cachedBody").setAttribute("data-my-name", "lee")
      * @returns {jGet}
     */
    jGet.getCached = function (cacheID) {
        return cachedSelections[cacheID];
    };
    /**
     * Removes a cached jGet Object
      * @memberof jGet
      * @method clearCache
      * @param cacheID - Unique Identifier for the cached jGet object
      * @example
      * // Delete the cached jGet object, "cachedBody"
      * jGet.clearCache("cachedBody")
      *
      *
     */
    jGet.clearCache = function (cacheID) {
        delete cachedSelections[cacheID];
    };
    /**
    * Returns a jGet object with a elements matching the supplied tag
     *  - Much quicker than using the general jGet(tag)
      * @memberof jGet
      * @method tag
      * @param tag - Tag type to target
      * @example
      * // Set color of text to red in all <p> elements
      * var pTags = jGet.tag("p");
      * pTags.css({
      *     "color": "red"
      * });
      *
      * @returns {jGet}
     */
    jGet.tag = function (tag) {
        /* Use Tag() to select nodes using the getElementsByTagName */
        var nodes = document.getElementsByTagName(tag);
        return jGet(tag, nodes);
    };

    /**
     * Returns a jGet object with elements matching the supplied classname
     *  - Much quicker than using the general jGet(classname)
     *  - Note that classname does not expect a preceding dot (.)
      * @memberof jGet
      * @method class
      * @param classname - Tag type to target
      * @example
      * // Set color of text to red to all elements with the class .paragraph
      * var paragraphs = jGet.class("paragraph");
      * paragraphs.css({
      *     "color": "red"
      * });
      *
      * @returns {jGet}
     */
    jGet.class = function (classname) {
        /* Use Tag() to select nodes using the getElementsByClassName */
        var nodeCollection = document.getElementsByClassName(classname);
        var nodes = Array.prototype.slice.call(nodeCollection);
        return jGet('.' + classname, nodes);
    };
    /**
     * Create a jGet Object with the iframes content document
     *  - This allows us to manipulate the content of the iframe
      * @memberof jGet
      * @method iframe
      * @param selector - Selector that targets the required iFrame
      * @example
      * // Hide an iFrame
      * var myIframe = jGet.iframe("myIframe");
      * myIframe.css({
      *    "display": "none"
      * });
      * @returns {HTMLElement}
     */
    jGet.iframe = function (selector) {
        var iframe = document.querySelectorAll(selector)[0];
        return jGet.node(iframe.contentDocument || iframe.contentWindow.document);
    };
    /**
    * Returns a jGet object with an element matching the supplied ID
     *  - Much quicker than using the general jGet(id)
     *  - Note that ID does not expect a preceding hashtag (#)
      * @memberof jGet
      * @method id
      * @param id - ID of the element to target
      * @example
      * // Set color of text to red to tag with title ID
      * var title = jGet.class("title");
      * title.css({
      *     "color": "red"
      * });
      *
      * @returns {jGet}
     */
    jGet.id = function (id) {
        /* Use Tag() to select nodes using the getElementById */
        var nodes = [document.getElementById(id)];
        return jGet('#' + id, nodes);
    };
    /**
     * Return jGet object with the passed node
      * @memberof jGet
      * @method node
      * @param node - HTMLElement
      * @example
      * // Set color of text to red
      * var nodeButton = document.getElementById("myButton");
      * var jGetButton = jGet.node(nodeButton);
      * jGetButton.css({
      *     "color": "red"
      * });
      *
      * @returns {node}
     */
    jGet.node = function (node) {
        /* Use Node to create a jGet object with a DOM node directly */
        return jGet('node', [node]);
    };
    /**
     * Build jGet object by converting a nodeCollection to a normal node list
      * @memberof jGet
      * @method collection
      * @param nodeCollection - Node Collection
      * @example
      * // Set color of text to red
      * var nodeCollectionButtons = document.querySelectorAll(".button");
      * var jGetButtons = jGet.collection(nodeCollectionButtons);
      * jGetButtons.css({
      *     "color": "red"
      * });
      * @returns {jGet}
     */
    jGet.collection = function (nodeCollection) {
        /* Use Node to create a jGet object with an HTML Node Collection  directly */
        var nodes = [];
        for (var n = 0; n < nodeCollection.length; n++) {
            nodes.push(nodeCollection[n]);
        }

        return jGet('node', nodes);
    };
    /**
     * Dumps a list of all event listeners created with jGet to the console panel
      * @memberof jGet
      * @example
      * // Outputs a list of all event listeners to the console panel - use ful for debuging
      * jGet.dump();
      *
      *
      * @method dump
     */
    jGet.dump = function () {
        console.log(mutationObservers);
        console.log(delegatedEventListeners);
    };

    window.jGet = jGet;

})();
