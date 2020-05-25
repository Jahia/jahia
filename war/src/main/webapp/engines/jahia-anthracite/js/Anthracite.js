(function(){
    /**
     * Anthracite is a listener based reactive app that manipulates the DOM according to user actions
     * @version 8.0
     * @property {method} addModule Add a new module to App
     * @property {method} init Start the ball rolling
     * @property {object} data Stores data for Object
     * @property {method} chrome Toggles the App UI
     * @property {method} onChangeNodePath Executed when the user changes Sites in Edit Mode (depreciated)
     * @property {method} onChangeSite Executed when the user changes site in Edit Mode
     * @property {method} onChange Executed when Jahia changes mode
     * @property {method} resized Executed ONCE when the browser window has been resized
     * @property {method} onResizeFinish Callback executed when the browser window has stopped being resized
     * @property {method} onResize Executed WHILST the browser window is being resized
     * @property {method} onBlur Executed when the window loses focus
     * @property {method} onClick Executed when the window recieves a click
     * @property {method} updateGWTMenus Makes modifications to GWT menus
     * @property {method} switch Executed when the App changes mode
     * @namespace Anthracite
     */
    window.Anthracite = window.DX = {
        /**
         * Used to add modules to the Anthracite Object Literal App
         * @memberof Anthracite
         * @method addModule
         * @param {string} module UID of the module
         * @param {object} code Object literal containing module code
         * @returns {boolean} Returns true if successfully create, false if not created
         * @example
         * Anthracite.addModule("contribute", {
         *    onOpen: function(){
         *        console.log("Contribute Mode opened");
         *    }
         * });
         */
        addModule: function(module, code){
            // Check its not a reserved module name
            var reserved = Anthracite.data.reservedModuleNames.indexOf(module) > -1;

            if(reserved){
                // Already in use do not create and log warning
                console.warn("Sorry, you cannot use " + module + " as a module name because it is already in use. Please choose another name");

            } else {
                // Add Module to Anthracite
                Anthracite[module] = code;

                // Register module name to stop it being over written
                Anthracite.data.reservedModuleNames.push(module);
            }

            return (Anthracite[module]) ? true : false;
        },
        /**
         * Callback executed once the DOM is ready, it sets up the basics and fires the event listeners
         * @memberof Anthracite
         * @param {method} callback Callback to be executed once the DOM is ready
         * @method init
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        init: function(callback){
            // Get UI Language from GWT parameters
            Anthracite.data.UILanguage = jahiaGWTParameters.uilang.toUpperCase();

            // use Dex to cache an Dex Object
            jGet('body').cache('body');

            // Register CK Editor version ( needed by CSS )
            jGet.getCached('body').setAttribute('data-CKEDITOR-VERSION', Anthracite.data.ckeditorVersion);

            // This is a content picker, not main Anthracite.
            if (Anthracite.data.HTTP.app === 'contentpicker') {
                // This is a full page picker, not edit engine
                Anthracite.picker.data.standalone = true;
                Anthracite.picker.data.ID = Anthracite.picker.data.standaloneID;

                // Need to "open" the picker manually ...


                jGet.tag('body').onOpen({
                    target: '#JahiaGxtContentPicker',
                    callback: Anthracite.picker.onOpen,
                });

                jGet.tag('body').onOpen({
                    target: '#JahiaGxtContentPickerWindow',
                    callback: Anthracite.picker.onOpenSubPicker,
                });

                jGet.getCached('body').setAttribute('data-indigo-is-manager', 'true');
            }

            // This is a manager, not main Anthracite.
            if (Anthracite.data.HTTP.app === 'manager') {
                // This is a manager, not edit engine
                Anthracite.picker.data.standalone = true;
                Anthracite.picker.data.ID = Anthracite.picker.data.standaloneManagerID;

                // Need to "open" the picker manually ...
                jGet.tag('body').onOpen({
                    target: '#contentmanager > .x-viewport',
                    callback: function (){
                        Anthracite.picker.onOpen();

                        if (Anthracite.data.HTTP.picker == 'portletmanager-anthracite') {
                            // Select list view
                            jGet('#contentmanager #JahiaGxtManagerToolbar .action-bar-tool-item.toolbar-item-listview').trigger('click');
                        }
                    }});

                jGet.tag('body').onClose({
                    target: '#JahiaGxtContentPickerWindow',
                    callback: Anthracite.picker.onCloseSubPicker,
                });

                jGet.getCached('body').setAttribute('data-indigo-is-manager', 'true');
            }

            // Attach event listeners
            if(callback){
                callback();
            }

            Anthracite.listeners.addListeners();
            Anthracite.listeners.queue.execute();

            window.onresize = Anthracite.onResize; // Use some kind of timer to reduce repaints / DOM manipulations
            /**
             * Target: Window object > an element has lost focus
             * Callback: Anthracite.onBlur
             */
            window.addEventListener('blur', Anthracite.onBlur);

            /**
             * Target: Window object > each time the active history entry changes between two history entries for the same document
             * Callback: Anthracite.nav.onPopState
             */
            window.onpopstate = Anthracite.nav.onPopState;
        },
        /**
         * For use by devs in the console panel. Allows to toggle certain features a,d also display information in the console
         * @memberof Anthracite
         * @property {method} help Outputs help to the console panel
         * @property {method} toggleLog Toggles Anthracite logs to browser console
         * @property {method} toggleAutoHide Toggles whether or not to autohide the side panel when it loses focus
         * @namespace Anthracite.config
         * @example
         *
         *
         * Add Example here ...
         *
         *
         * @type {object}
         */
        config: {
            /**
             * Retrieves information from URL and Quesy String to determine context (edit mode, contribute mode, manager, ...)
             * @memberof Anthracite.config
             * @method help
             * @example
             * Anthracite.help()
             *
             * // Output to console
             * // === CONFIG ===========
             * // You can stop the side panel from automatically closing with the following toggle:
             * //     DX.config.toggleAutoHide
             * //
             * // You can toggle the Log with the following:
             * //     DX.config.toggleLog()
             * ======================
             */
            help: function () {
                console.log('=== CONFIG ===========\n\n');
                console.log('You can stop the side panel from automatically closing with the following toggle:');
                console.log('DX.config.toggleAutoHide()\n\n');
                console.log('You can toggle the Log with the following:');
                console.log('DX.config.toggleLog()');
                console.log('======================');
            },
            /**
             * Toggles Anthracite logs to browser console
             * @memberof Anthracite.config
             * @method toggleLog
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            toggleLog: function () {
                Anthracite.dev.data.on = !Anthracite.dev.data.on;
            },
            /**
             * Toggles whether or not to autohide the side panel when it loses focus
             * @memberof Anthracite.config
             * @method toggleAutoHide
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            toggleAutoHide: function () {
                Anthracite.nav.data.autoHideSidePanel = !Anthracite.nav.data.autoHideSidePanel;
            }
        },
         /**
          * Stores top level data
          * @memberof Anthracite
          * @type {object}
          * @property {object} reservedModuleNames Array containing a list of reserved module names
          * @property {object} openedXWindows XXX
          * @property {string} currentApp Name of the current App Mode
          * @property {string} currentSite Name of the current site in Edit Mode
          * @property {string} previousModeClass Name of the previous App Mode
          * @property {string} UILanguage User Interface Language
          * @property {boolean} startedOnSettingsPage If the App was opened directly on a Setting Page
          * @property {boolean} startedOnEditPage If the App was opened directly on a Edit Mode
          * @property {boolean} firstApp Think this is depreciated - to check
          * @property {string} ckeditorVersion Version of the CK Editor installed in Jahia
          * @property {boolean} resizingWindow True whilst the window is being resized
          * @property {string} fallbackLanguage If a localised string does not exist in current language, use this default
          * @property {object} HTTP (Object created from self executing method) <br> Retrieves information from URL and Quesy String to determine context (edit mode, contribute mode, manager, ...)
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        data: {
            reservedModuleNames: [],
            openedXWindows: [],
            currentApp: null,
            currentSite: null,
            previousModeClass: null,
            UILanguage: null,
            startedOnSettingsPage: false,
            startedOnEditPage: true,
            firstApp: null,
            ckeditorVersion: CKEDITOR.version,
            resizingWindow: false,
            fallbackLanguage: 'EN',
            HTTP: function () {
                var contextIndexOffset = (jahiaGWTParameters.contextPath) ? 1 : 0; // DX is running under a context, need to take this into account with the URL
                var pathnameSplit = document.location.pathname.split('/');
                var page = pathnameSplit[pathnameSplit.length - 1];
                var root = pathnameSplit[1 + contextIndexOffset];
                var DXApp = pathnameSplit[2 + contextIndexOffset];
                var servletPath = jahiaGWTParameters.servletPath;
                var queryString = document.location.href;
                var queryStringParameters = queryString.split('?');
                var queryStringKeyValuePairs;
                var queryStringKeyValuePairsSplit;
                var picker = null;
                var QS = {};

                if (queryStringParameters.length > 1) {
                    // Found a query string ...
                    queryStringKeyValuePairs = queryStringParameters[1].split('&');

                    for (var n = 0; n < queryStringKeyValuePairs.length; n++) {
                        queryStringKeyValuePairsSplit = queryStringKeyValuePairs[n].split('=');
                        QS[queryStringKeyValuePairsSplit[0]] = queryStringKeyValuePairsSplit[1];
                    }
                }

                if (servletPath === '/engines/contentpicker.jsp') {
                    app = 'contentpicker';
                    DXApp = 'miniApp';
                    picker = QS['type'] || 'default';

                } else if (servletPath === '/engines/manager.jsp') {
                    app = 'manager';
                    DXApp = 'miniApp';
                    picker = QS['conf'] || 'default';
                } else {
                    app = DXApp;
                }


                return {
                    page: page,
                    root: root,
                    picker: picker,
                    QS: QS,
                    app: app,
                    DXApp: DXApp
                };
            }()
        },
          /**
           * Deals with App Listners
           * @memberof Anthracite
           * @property {object} data Stores data for Object
           * @property {object} queue Controls listener queue <br> See {@link Anthracite.listeners.queue}
           * @property {object} attach Attach Event Listener
           * @namespace Anthracite.listeners
           * @type {object}
           * @example
           *
           *
           * Add Example here ...
           *
           *
           */
        listeners: {
            /**
             * Data Storage
             * @memberof Anthracite.listeners
             * @type {object}
             * @property {array} queue XXX
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            data: {
                queue: []
            },
            /**
             * Data Storage
             * @memberof Anthracite.listeners
             * @namespace Anthracite.listeners.queue
             * @type {object}
             * @property {method} add XXX
             * @property {method} execute XXX
             *
             *
             * Add Example here ...
             *
             *
             */
            queue: {
                /**
                 * Add event listeners to the queue. Note: these are not executed, only stored.
                 * @memberof Anthracite.listeners.queue
                 * @param {object} listeners XXX
                 * @method add
                 * @example
                 *
                 *
                 * Add Example here ...
                 *
                 *
                 */
                add: function(listeners){
                    for(var n = 0; n < listeners.length; n++){
                        Anthracite.listeners.data.queue.push(listeners[n]);
                    }
                },
                /**
                 * Add event listeners to the queue. Note: these are not executed, only stored.
                 * @memberof Anthracite.listeners.queue
                 * @param {object} listeners XXX
                 * @type {method}
                 * @example
                 *
                 *
                 * Add Example here ...
                 *
                 *
                 */
                execute: function(){
                    while( (listener = Anthracite.listeners.data.queue.shift()) !== undefined ) {
                        Anthracite.listeners.attach(listener)
                    }
                }
            },
            /**
             * XXX
             * @memberof Anthracite.listeners
             * @param {object} listener XXX
             * @type {method}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            attach: function(listener){
                jGet(listener.root)[listener.type](listener)
            }
        },
        /**
         * Local storage manager
         * @memberof Anthracite
         * @type {object}
         * @namespace Anthracite.storage
         * @property {object} data Stores data for Object
         * @property {method} buildKey Create key (with or without prefix depending on config) for variables stored in Localstorage
         * @property {method} set Create entry in Localstorage
         * @property {method} get Retrieve value from Locastorage
         * @property {method} remove Removes an entry from the Localstorage
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        storage: {
            /**
             * Stores data for Object
             * @memberof Anthracite.storage
             * @type {object}
             * @property {boolean} available If borwser has local storage
             * @property {string} keyPrefix Used to prefix all items stored in localstorage ( to avoid conflicts )
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            data: {
                available: typeof(Storage) !== 'undefined', // Check browser has Local Storage
                keyPrefix: 'DA' // Use a prefix to ensure we dont inadvertantly over write items set by GWT
            },
             /**
              * Create key (with or without prefix depending on config) for variables stored in Localstorage
              * @memberof Anthracite.storage
              * @param {string} key XXX
              * @type {method}
              * @returns {string}
              * @example
              *
              *
              * Add Example here ...
              *
              *
              */
            buildKey: function(key){
                return (Anthracite.storage.data.keyPrefix) ? Anthracite.storage.data.keyPrefix + '-' + key : key;
            },
            /**
             * Create entry in Localstorage
             * @memberof Anthracite.storage
             * @param {string} key Key for item to be set
             * @param {string} value Vaue of item being set
             * @param {string} dataType - Unused, should be deleted
             * @returns {boolean|string} - If Localstorage is not accessible returns false; otherwise returns with the specified value
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            set: function(key, value, dataType){
                if(!Anthracite.storage.data.available){
                    // Browser doesnt support Local Storage, so exit
                    return false
                }

                // Set item in local storage
                localStorage.setItem(Anthracite.storage.buildKey(key), value);

                return value;

            },
            /**
             * Retrieve value from Locastorage
             * @memberof Anthracite.storage
             * @param {string} key Key for item to retrieve from localstorage
             * @returns {string|boolean} - Returns false if Localstorage unavailable, otherwise it returns the value of the requested variable
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            get: function(key){
                if(!Anthracite.storage.data.available){
                    // Browser doesnt support Local Storage, so exit
                    return false
                }

                // Get item from localstorage
                var storedValue = localStorage.getItem(Anthracite.storage.buildKey(key)),
                    value = storedValue;

                // Local Storage values are converted to strings, so just checking to reconvert any true/false strings to boolean
                if(storedValue == 'true'){
                    value = true;
                } else if(storedValue == 'false'){
                    value = false;
                }

                return value
            },
            /**
             * Removes an entry from the Localstorage
             * @memberof Anthracite.storage
             * @param {string} key Key of item to delete
             * @returns {null|boolean} Returns false if Localstorage is unavailable; otherwise returns null
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            remove: function(key){
                if(!Anthracite.storage.data.available){
                    // Browser doesnt support Local Storage, so exit
                    return false
                }

                // Remove item from Local Storage
                localStorage.removeItem(Anthracite.storage.buildKey(key));

                return null
            }

        },
         /**
          * Localised string manager
          * @memberof Anthracite
          * @property {object} data Stores data for Object
          * @property {object} get Retrieve localised string
          * @property {object} set Store localised string
          * @namespace Anthracite.dictionary
          * @type {object}
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        dictionary: {
            /**
             * Data Storage
             * @memberof Anthracite.dictionary
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            data: {},
            /**
             * XXX
             * @memberof Anthracite.dictionary
             * @param {string} key XXX
             * @param {string} lang XXX
             * @returns {string} XXX
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            get: function (key, lang) {
                lang = lang || Anthracite.data.UILanguage;
                var returnString = 'not_found';

                if (Anthracite.dictionary.data[lang] && Anthracite.dictionary.data[lang][key]) {
                    returnString = Anthracite.dictionary.data[lang][key];
                } else if (Anthracite.dictionary.data[Anthracite.data.fallbackLanguage] && Anthracite.dictionary.data[Anthracite.data.fallbackLanguage][key]) {
                    returnString = Anthracite.dictionary.data[Anthracite.data.fallbackLanguage][key];
                }

                return returnString;
            },
            /**
             * XXX
             * @memberof Anthracite.dictionary
             * @param {string} lang XXX
             * @param {object} data XXX
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            set: function(lang, data){

                // See if language exists
                if(!Anthracite.dictionary.data[lang]){
                    Anthracite.dictionary.data[lang] = {};
                }

                // Add localised strings
                for(var key in data){
                    Anthracite.dictionary.data[lang][key] = data[key]
                }

            }
        },
         /**
          * Show / Remove the Anthracite User Interface
          * @memberof Anthracite
          * @method chrome
          * @example
          * // Display Jahia User Interface
          * Anthracite.chrome(true);
          * // Hide Jahia User Interface
          * Anthracite.chrome(false);
          * @param {boolean} status - true shows the UI, false hides it
          *
          *
          * Add Example here ...
          *
          *
          */
        chrome: function (status) {
            Anthracite.data.chrome = status;

            jGet.getCached('body').setAttribute('data-chrome', status);
        },
        /**
         * Localised string manager
         * @memberof Anthracite
         * @property {object} data Stores data for Object
         * @property {method} log Send Anthracite logs to the console panel if Anthracite.dev.data.on is true
         * @namespace Anthracite.dev
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        dev: {
            /**
             * Data Storage
             * @memberof Anthracite.dev
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            data: {
                on: false
            },
            /**
             * Show / Remove the Anthracite User Interface
             * @memberof Anthracite.dev
             * @method log
             * @property {string} message Message to output to the console
             * @property {boolean} force Output to console even if dev console log is turned off
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            log: function (message, force) {
                if (Anthracite.dev.data.on || force) {
                    console.log(message);
                }
            }
        },
         /**
          * Executed when the user changes Sites in Edit Mode (depreciated)
          * @memberof Anthracite
          * @param {string} attrKey
          * @param {string} attrValue
          * @deprecated There is now a better way to detect when the site changes [DEVNOTE]
          * @method onChangeNodePath
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onChangeNodePath: function (attrKey, attrValue) {
            var nodePathSplit = attrValue.split('/');
            var site = nodePathSplit[2];

            if (site && site != Anthracite.data.currentSite) {
                Anthracite.data.currentSite = site;
                Anthracite.onChangeSite(site);
            }
        },
         /**
          * Callback executed when the site has changed
          * @memberof Anthracite
          * @method onChangeNodePath
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onChangeSite: function () {
            Anthracite.edit.history.reset();
            // Switch to pages view (even though maybe hidden, so that the refresh button relates to pages list)
            jGet.id('JahiaGxtSidePanelTabs__JahiaGxtPagesTab').trigger('click');
        },
        /**
         * Navigation History Manager
         * @memberof Anthracite
         * @property {object} data Stores data for Object
         * @property {method} pullState XXX
         * @property {method} pushState XXX
         * @property {method} onPopState XXX
         * @namespace Anthracite.nav
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        nav: {
            /**
             * Data Storage
             * @memberof Anthracite.nav
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            data: {
                autoHideSidePanel: true
            },
            /**
             * XXX
             * @memberof Anthracite.nav
             * @param closeButton XXX
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            pullState: function (closeButton) {
                Anthracite.dev.log('APP ::: NAV ::: PULLSTATE');

                var removeID = null;
                for (var n = 0; n < Anthracite.data.openedXWindows.length; n++) {
                    if (Anthracite.data.openedXWindows[n].nodes[0] == closeButton.nodes[0]) {
                        removeID = n;
                    }
                }

                if (removeID !== null) {
                    Anthracite.data.openedXWindows.splice(removeID, 1);
                }
            },
            /**
             * XXX
             * @memberof Anthracite.nav
             * @param closeButton XXX
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            pushState: function (closeButton) {
                Anthracite.dev.log('APP ::: NAV ::: PUSHSTATE');

                var url = window.location.pathname;
                var qs = window.location.search;
                var pushUrl = url + qs;
                var DXStateObject = window.history.state; // DX Seems to need this so keep it the same

                Anthracite.data.openedXWindows.push(closeButton);

                history.pushState(DXStateObject, 'DX', pushUrl);
            },
            /**
             * XXX
             * @memberof Anthracite.nav
             * @param {event} event XXX
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onPopState: function (event) {
                Anthracite.dev.log('APP ::: NAV ::: ONPOPSTATE');

                if (event.state && Anthracite.data.openedXWindows.length > 0) {
                    Anthracite.data.openedXWindows[Anthracite.data.openedXWindows.length - 1].trigger('click');
                }
            }
        },
         /**
          * Callback executed when the app changes mode
          * @memberof Anthracite
          * @param {string} attrKey
          * @param {string} attrValue
          * @method onChange
          * @returns {boolean|undefined}
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onChange: function (attrKey, attrValue) {
            if (Anthracite.data.previousModeClass == attrValue) {
                return false;
            }

            Anthracite.data.previousModeClass = attrValue;

            Anthracite.dev.log('::: APP ::: ONCHANGE');

            attrValue.split(' ').forEach(function (cl) {
                if (cl.indexOf('x-viewport') == 0) {
                    switch (cl) {
                        case 'x-viewport-editmode':
                            Anthracite.updateGWTMenus();
                            Anthracite.switch('edit');
                            break;
                        case 'x-viewport-adminmode':
                            Anthracite.switch('admin');
                            break;
                        case 'x-viewport-sitesettingsmode':
                            Anthracite.updateGWTMenus();
                            Anthracite.switch('remote');
                            break;
                        case 'x-viewport-dashboardmode':
                            Anthracite.switch('dashboard');
                            break;
                        case 'x-viewport-studiomode':
                            Anthracite.switch('studio');
                            break;
                        case 'x-viewport-contributemode':
                            Anthracite.switch('contribute');
                            break;
                    }
                }
            });
        },
         /**
          * Called as the browser window is being resized
          * @memberof Anthracite
          * @method resized

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        resized: function () {
            Anthracite.dev.log('::: APP ::: RESIZED');
            if (Anthracite.data.currentApp == 'edit') {
                Anthracite.edit.topbar.reposition();
                Anthracite.edit.sidepanel.onWindowResize();
            } else if (Anthracite.data.currentApp == 'admin' || Anthracite.data.currentApp == 'dashboard') {
                Anthracite.edit.sidepanel.resizeSidePanel();
            }

            if (Anthracite.data.currentApp == 'contribute') {
                Anthracite.contribute.topbar.reposition();
            }
        },
         /**
          * Executed once the window has stopped being resized
          * @memberof Anthracite
          * @method onResizeFinish

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onResizeFinish: function () {
            Anthracite.dev.log('::: APP ::: ONRESIZEFINISH');
            Anthracite.resized();
        },
         /**
          * Executed whilst the window is being resized
          * @memberof Anthracite
          * @method onResize

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onResize: function () {
            Anthracite.dev.log('::: APP ::: ONRESIZE');

            clearTimeout(Anthracite.data.resizingWindow);
            Anthracite.data.resizingWindow = setTimeout(Anthracite.onResizeFinish, 500);

            Anthracite.resized();
            Anthracite.edit.sidepanel.clipPageTitle();
        },
         /**
          * Executed when the window loses focus
          * @memberof Anthracite
          * @method onBlur

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onBlur: function () {
            Anthracite.dev.log('::: APP ::: ONBLUR');
            if (jGet.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open' && Anthracite.nav.data.autoHideSidePanel) {
                Anthracite.edit.sidepanel.close();
                // Trigger mousedown / mouseup on body to close any open context menus and combo menus
                jGet.tag('body').trigger('mousedown').trigger('mouseup');
            }
        },
         /**
          * Executed when the user clicks on the .app-container DIV
          * @memberof Anthracite
          * @param {mouseEvent} e mouse event
          * @method onClick

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onClick: function (e) {
            Anthracite.dev.log('CLICKED APP');
            if (jGet.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open') {
                var inSidePanel = jGet.node(e.target).closest('.window-side-panel');
                var inSideToolBar = jGet.node(e.target).closest('.edit-menu-righttop');

                if (inSidePanel.nodes.length == 0 && inSideToolBar.nodes.length == 0 && Anthracite.nav.data.autoHideSidePanel) {
                    Anthracite.dev.log('::: APP ::: ONCLICK');
                    Anthracite.edit.sidepanel.close();
                }
            }
        },
         /**
          * Used to update the GWT menus in Anthracite
          *  - In addition to class modifications, the DOM tree is also modified when switching
          * @memberof Anthracite
          * @method updateGWTMenus

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        updateGWTMenus: function () {
            Anthracite.dev.log('::: APP ::: UPDATEGWTMENUS');

            var targetMenu = document.querySelectorAll('.edit-menu-centertop .x-toolbar-left-row')[0],
                statusMenuButton = document.querySelectorAll('.edit-menu-status')[0],
                publishMenuButton = document.querySelectorAll('.edit-menu-publication')[0],
                advancedPublishMenuButton = document.querySelectorAll('.toolbar-item-publishone')[0],
                editMenuButton = (document.querySelectorAll('.edit-menu-edit')[0]) ? document.querySelectorAll('.edit-menu-edit')[0].parentNode : null;

            if (targetMenu && advancedPublishMenuButton && publishMenuButton && statusMenuButton) {
                if (advancedPublishMenuButton.classList.contains('x-item-disabled')) {
                    advancedPublishMenuButton.parentNode.style.pointerEvents = 'none';
                }
                targetMenu.insertBefore(advancedPublishMenuButton.parentNode, editMenuButton);
                targetMenu.insertBefore(publishMenuButton.parentNode, editMenuButton);
                targetMenu.insertBefore(statusMenuButton.parentNode, editMenuButton);
            }

        },
         /**
          * Callback executed when the app changes
          * @memberof Anthracite
          * @param {string} appID - edit|admin|remote|dashboard|studio|contribute
          * @param {object} _config - optional, uses default config if not specified
          * @returns {null|undefined}
          * @method switch
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        switch: function (appID, _config) {
            Anthracite.dev.log('::: APP ::: SWITCH: ' + appID);

            if (Anthracite.data.currentApp == appID) {
                // Not switching apps, so no point in continuing with app inits
                return false;
            }

            Anthracite.data.previousApp = Anthracite.data.currentApp;
            Anthracite.data.currentApp = appID;

            jGet.getCached('body').setAttribute('data-INDIGO-APP', appID);

            if (Anthracite[Anthracite.data.currentApp] && Anthracite[Anthracite.data.currentApp].onOpen) {
                var appConfig = Anthracite[Anthracite.data.currentApp].config;
                Anthracite[Anthracite.data.currentApp].onOpen();
            }

            if (Anthracite[Anthracite.data.previousApp] && Anthracite[Anthracite.data.previousApp].onClose) {
                Anthracite[Anthracite.data.previousApp].onClose();
            }

            var config = appConfig || _config || {};
            if (typeof config.chrome !== 'undefined') {
                // Deal with Chrome
                Anthracite.chrome(config.chrome);
            }
        },
        /**
         * Shared methods
         * @memberof Anthracite
         * @property {object} trees Tree controls <br> See {@link Anthracite.common.trees}
         * @property {method} calculateTextWidth Calculate width of text on screen
         * @property {method} resizeLanguageInput Resize the width of the language selector when site is changed
         * @property {method} resizeSiteSelector Resize the width of the site selector when site is changed
         * @namespace Anthracite.common
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        common: {
             /**
              * XXX
              * @memberof Anthracite.common
              * @namespace Anthracite.common.trees
              * @property {method} onToggleChildNodes XXX
              * @returns {boolean}
              * @type {object}
              */
            trees: {
                 /**
                  * XXX => Callback executed when the user clicks on the arrow to expand / collpase child nodes
                  * @memberof Anthracite.common.trees
                  * @method onToggleChildNodes

                  * @example
                  *
                  *
                  * Add Example here ...
                  *
                  *
                  */
                onToggleChildNodes: function () {
                    jGet.node(this).closest('.x-grid3-row').toggleClass('indigo-opened');
                }
            },
            /**
             * XXX
             * @memberof Anthracite.common
             * @method calculateTextWidth
             * @param {object} params
             * @returns {integer} XXX
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            calculateTextWidth: function(params){
                var canvas = document.createElement("canvas"),
                    ctx = canvas.getContext("2d");

                ctx.font = params.fontSize + "px " + params.fontFace;

                return ctx.measureText(params.text).width + params.padding;
            },
            /**
             * XXX
             * @memberof Anthracite.common
             * @method resizeLanguageInput

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            resizeLanguageInput: function () {
                Anthracite.dev.log("app ::: common ::: resizeLanguageInput");

                if(jGet('.toolbar-itemsgroup-languageswitcher input').exists() && Anthracite.data.currentApp == "edit"){
                    var languageInputValue = jGet('.toolbar-itemsgroup-languageswitcher input').nodes[0].value,
                        width = Anthracite.common.calculateTextWidth({
                            text: languageInputValue,
                            fontSize: 14,
                            fontFace: "Nunito Sans",
                            padding: 20
                        });

                    jGet('.toolbar-itemsgroup-languageswitcher').nodes[0].style.setProperty('width', (width + 'px'), 'important');
                }
            },
            /**
             * XXX
             * @memberof Anthracite.common
             * @method resizeSiteSelector

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            resizeSiteSelector: function () {
                Anthracite.dev.log("app ::: common ::: resizeSiteSelector");
                if(jGet('.edit-menu-sites input').exists() && Anthracite.data.currentApp == "edit"){
                    var siteInputValue = jGet('.edit-menu-sites input').nodes[0].value,
                        width = Anthracite.common.calculateTextWidth({
                            text: siteInputValue,
                            fontSize: 14,
                            fontFace: "Nunito Sans",
                            padding: 20
                        });

                    jGet('.edit-menu-sites').nodes[0].style.setProperty('width', (width + 'px'), 'important');
                }
            }
        },
    };

    // Protect Anthracte Objects
    for(var forbiddenName in Anthracite){
        Anthracite.data.reservedModuleNames.push(forbiddenName);
    }

})();
