// MOUSE CONTROLLER
var DX_mouse = {
    /**
     * used to trigger a mouse click on specified node
     * @param node - The node which will recieve the triggered click
     * @param eventType - Type of click
     * @returns {boolean} - Returns false if the node isn't found
     */
    trigger: function (node, eventType) {
        if (!node) {
            return false;
        }

        var clickEvent = document.createEvent('MouseEvents');
        clickEvent.initEvent(eventType, true, true);
        node.dispatchEvent(clickEvent);
    }
};

var DX_app = {
    config: {
        /**
         * Outputs some helpers to the console for devs
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
         */
        toggleLog: function () {
            DX_app.dev.data.on = !DX_app.dev.data.on;
        },
        /**
         * Toggles whether or not to autohide the side panel when it loses focus
         */
        toggleAutoHide: function () {
            DX_app.nav.data.autoHideSidePanel = !DX_app.nav.data.autoHideSidePanel;
        }
    },
    data: {
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
        /**
         * Retrieves information from URL and Quesy String to determine context (edit mode, contribute mode, manager, ...)
         */
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

            if (servletPath == '/engines/contentpicker.jsp') {
                app = 'contentpicker';
                DXApp = 'miniApp';
                picker = QS['type'] || 'default';

            } else if (servletPath == '/engines/manager.jsp') {
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
    storage: {
        data: {
            available: typeof(Storage) !== 'undefined', // Check browser has Local Storage
            keyPrefix: 'DA' // Use a prefix to ensure we dont inadvertantly over write items set by GWT
        },
        /**
         * Create key (with or without prefix depending on config) for variables stored in Localstorage
         * @param key
         * @returns {string}
         */
        buildKey: function(key){
            return (DX_app.storage.data.keyPrefix) ? DX_app.storage.data.keyPrefix + '-' + key : key;
        },
        /**
         * Create entry in Localstorage
         * @param key
         * @param value
         * @param dataType - Unused, should be deleted
         * @returns {boolean|*} - If Localstorage is not accessible returns false; otherwise returns with the specified value
         */
        set: function(key, value, dataType){
            if(!DX_app.storage.data.available){
                // Browser doesnt support Local Storage, so exit
                return false
            }

            // Set item in local storage
            localStorage.setItem(DX_app.storage.buildKey(key), value);

            return value;

        },
        /**
         * Retrieve value from Locastorage
         * @param key
         * @returns {string|boolean} - Returns false if Localstorage unavailable, otherwise it returns the value of the requested variable
         */
        get: function(key){
            if(!DX_app.storage.data.available){
                // Browser doesnt support Local Storage, so exit
                return false
            }

            // Get item from localstorage
            var storedValue = localStorage.getItem(DX_app.storage.buildKey(key)),
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
         * @param key
         * @returns {null|boolean} Returns false if Localstorage is unavailable; otherwise returns null
         */
        remove: function(key){
            if(!DX_app.storage.data.available){
                // Browser doesnt support Local Storage, so exit
                return false
            }

            // Remove item from Local Storage
            localStorage.removeItem(DX_app.storage.buildKey(key));

            return null
        }

    },
    /**
     * Get a string from the Dictionary.
     * @param key
     * @param lang - If the language doesnt exist then it falls back on default language
     * @returns {string} - Returns 'not_found' if string doesnt exist
     */
    dictionary: function (key, lang) {
        lang = lang || DX_app.data.UILanguage;
        var returnString = 'not_found';

        if (DX_localisedStrings[lang] && DX_localisedStrings[lang][key]) {
            returnString = DX_localisedStrings[lang][key];
        } else if (DX_localisedStrings[DX_app.data.fallbackLanguage] && DX_localisedStrings[DX_app.data.fallbackLanguage][key]) {
            returnString = DX_localisedStrings[DX_app.data.fallbackLanguage][key];
        }

        return returnString;
    },
    /**
     * Show / Remove the Anthracite User Interface
     * @param status - true shows the UI, false hides it
     */
    chrome: function (status) {
        DX_app.data.chrome = status;

        DexV2.getCached('body').setAttribute('data-chrome', status);
    },
    dev: {
        data: {
            on: false
        },
        /**
         * Send Anthracite logs to the console panel if DX_app.dev.data.on is true.
         * @param message
         * @param force - if set to true then the log is sent to the console regardless of whether DX_app.dev.data.on is set to true or false
         */
        log: function (message, force) {
            if (DX_app.dev.data.on || force) {
                console.log(message);
            }
        }
    },
    /**
     * Used to determine whether or not the site has changed
     *  - There is now a better way to detect when the site changes [DEVNOTE]
     * @param attrKey
     * @param attrValue
     */
    onChangeNodePath: function (attrKey, attrValue) {
        var nodePathSplit = attrValue.split('/');
        var site = nodePathSplit[2];

        if (site && site != DX_app.data.currentSite) {
            DX_app.data.currentSite = site;
            DX_app.onChangeSite(site);
        }
    },
    /**
     * Callback executed when the site has changed
     */
    onChangeSite: function () {
        DX_app.edit.history.reset();
        // Switch to pages view (even though maybe hidden, so that the refresh button relates to pages list)
        DexV2.id('JahiaGxtSidePanelTabs__JahiaGxtPagesTab').trigger('click');
    },
    nav: {
        data: {
            autoHideSidePanel: true
        },
        /**
         * TO DO
         * @param closeButton
         */
        pullState: function (closeButton) {
            DX_app.dev.log('APP ::: NAV ::: PULLSTATE');

            var removeID = null;
            for (var n = 0; n < DX_app.data.openedXWindows.length; n++) {
                if (DX_app.data.openedXWindows[n].nodes[0] == closeButton.nodes[0]) {
                    removeID = n;
                }
            }

            if (removeID !== null) {
                DX_app.data.openedXWindows.splice(removeID, 1);
            }
        },
        /**
         * TO DO
         * @param closeButton
         */
        pushState: function (closeButton) {
            DX_app.dev.log('APP ::: NAV ::: PUSHSTATE');

            var url = window.location.pathname;
            var qs = window.location.search;
            var pushUrl = url + qs;
            var DXStateObject = window.history.state; // DX Seems to need this so keep it the same

            DX_app.data.openedXWindows.push(closeButton);

            history.pushState(DXStateObject, 'DX', pushUrl);
        },
        /**
         * TO DO
         * @param event
         */
        onPopState: function (event) {
            DX_app.dev.log('APP ::: NAV ::: ONPOPSTATE');

            if (event.state && DX_app.data.openedXWindows.length > 0) {
                DX_app.data.openedXWindows[DX_app.data.openedXWindows.length - 1].trigger('click');
            }
        }
    },
    /**
     * Callback executed when the app changes mode
     * @param attrKey
     * @param attrValue
     * @returns {boolean}
     */
    onChange: function (attrKey, attrValue) {
        if (DX_app.data.previousModeClass == attrValue) {
            return false;
        }

        DX_app.data.previousModeClass = attrValue;

        DX_app.dev.log('::: APP ::: ONCHANGE');

        attrValue.split(' ').forEach(function (cl) {
            if (cl.indexOf('x-viewport') == 0) {
                switch (cl) {
                    case 'x-viewport-editmode':
                        DX_app.updateGWTMenus();
                        DX_app.switch('edit');
                        break;
                    case 'x-viewport-adminmode':
                        DX_app.switch('admin');
                        break;
                    case 'x-viewport-sitesettingsmode':
                        DX_app.updateGWTMenus();
                        DX_app.switch('remote');
                        break;
                    case 'x-viewport-dashboardmode':
                        DX_app.switch('dashboard');
                        break;
                    case 'x-viewport-studiomode':
                        DX_app.switch('studio');
                        break;
                    case 'x-viewport-contributemode':
                        DX_app.switch('contribute');
                        break;
                }
            }
        });
    },
    /**
     * Called when the browser window is resized
     *  - Executes both whilst the browser is being resized AND once again at the end
     */
    resized: function () {
        DX_app.dev.log('::: APP ::: RESIZED');
        if (DX_app.data.currentApp == 'edit') {
            DX_app.edit.topbar.reposition();
            DX_app.edit.sidepanel.onWindowResize();
        } else if (DX_app.data.currentApp == 'admin' || DX_app.data.currentApp == 'dashboard') {
            DX_app.edit.sidepanel.resizeSidePanel();
        }

        if (DX_app.data.currentApp == 'contribute') {
            DX_app.contribute.topbar.reposition();
        }
    },
    /**
     * Executed once the window has stopped being resized
     */
    onResizeFinish: function () {
        DX_app.dev.log('::: APP ::: ONRESIZEFINISH');
        DX_app.resized();
    },
    /**
     * Executed whilst the window is being resized
     */
    onResize: function () {
        DX_app.dev.log('::: APP ::: ONRESIZE');

        clearTimeout(DX_app.data.resizingWindow);
        DX_app.data.resizingWindow = setTimeout(DX_app.onResizeFinish, 500);

        DX_app.resized();
        DX_app.edit.sidepanel.clipPageTitle();
    },
    /**
     * Executed when the window loses focus
     *  - This is used to guess when the user has clicked on the iframe.
     */
    onBlur: function () {
        DX_app.dev.log('::: APP ::: ONBLUR');
        if (DexV2.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open' && DX_app.nav.data.autoHideSidePanel) {
            DX_app.edit.sidepanel.close();
            // Trigger mousedown / mouseup on body to close any open context menus and combo menus
            DexV2.tag('body').trigger('mousedown').trigger('mouseup');
        }
    },
    /**
     * Executed when the user clicks on the .app-container DIV
     * @param e - mouse event
     */
    onClick: function (e) {
        DX_app.dev.log('CLICKED APP');
        if (DexV2.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open') {
            var inSidePanel = DexV2.node(e.target).closest('.window-side-panel');
            var inSideToolBar = DexV2.node(e.target).closest('.edit-menu-righttop');

            if (inSidePanel.nodes.length == 0 && inSideToolBar.nodes.length == 0 && DX_app.nav.data.autoHideSidePanel) {
                DX_app.dev.log('::: APP ::: ONCLICK');
                DX_app.edit.sidepanel.close();
            }
        }
    },
    /**
     * Used to update the GWT menus in Anthracite
     *  - In addition to class modifications, the DOM tree is also modified when switching
     */
    updateGWTMenus: function () {
        DX_app.dev.log('::: APP ::: UPDATEGWTMENUS');

        var targetMenu = document.querySelectorAll('.edit-menu-centertop .x-toolbar-left-row')[0],
            statusMenuButton = document.querySelectorAll('.edit-menu-status')[0],
            publishMenuButton = document.querySelectorAll('.edit-menu-publication')[0],
            advancedPublishMenuButton = document.querySelectorAll('.toolbar-item-publishone')[0],
            editMenuButton = (document.querySelectorAll('.edit-menu-edit')[0]) ? document.querySelectorAll('.edit-menu-edit')[0].parentNode : null;

        if (targetMenu && advancedPublishMenuButton && publishMenuButton && statusMenuButton) {
            advancedPublishMenuButton.parentNode.classList.remove('x-hide-display');
            advancedPublishMenuButton.parentNode.classList.add('force-display-inline-block');
            targetMenu.insertBefore(advancedPublishMenuButton.parentNode, editMenuButton);
            targetMenu.insertBefore(publishMenuButton.parentNode, editMenuButton);
            targetMenu.insertBefore(statusMenuButton.parentNode, editMenuButton);
        }

    },
    /**
     * Callback executed when the app changes
     * @param appID - edit|admin|remote|dashboard|studio|contribute
     * @param _config - optional, uses default config if not specified
     * @returns {boolean}
     */
    switch: function (appID, _config) {
        DX_app.dev.log('::: APP ::: SWITCH: ' + appID);

        if (DX_app.data.currentApp == appID) {
            // Not switching apps, so no point in continuing with app inits
            return false;
        }

        DX_app.data.previousApp = DX_app.data.currentApp;
        DX_app.data.currentApp = appID;

        DexV2.getCached('body').setAttribute('data-INDIGO-APP', appID);

        if (DX_app[DX_app.data.currentApp] && DX_app[DX_app.data.currentApp].onOpen) {
            var appConfig = DX_app[DX_app.data.currentApp].config;
            DX_app[DX_app.data.currentApp].onOpen();
        }

        if (DX_app[DX_app.data.previousApp] && DX_app[DX_app.data.previousApp].onClose) {
            DX_app[DX_app.data.previousApp].onClose();
        }

        var config = appConfig || _config || {};
        if (typeof config.chrome !== 'undefined') {
            // Deal with Chrome
            DX_app.chrome(config.chrome);
        }
    },
    contextMenus: {
        /**
         * Adds a title to contextual menus
         * @param contextmenu
         * @param params
         */
        setTitle: function (contextmenu, params) {
            DX_app.dev.log('::: APP ::: CONTEXTMENUS ::: SETTITLE');

            var contextMenuTitle;
            var contextMenuList = contextmenu.getElementsByClassName('x-menu-list')[0];
            if (contextMenuList) {
                switch (DX_app.iframe.data.selectionCount) {
                    case 0:
                        // Page
                        contextMenuTitle = params.noSelection;
                        break;
                    case 1:
                        // Selected Item
                        contextMenuTitle = params.singleSelection.replace('{{node}}', DexV2.getCached('body').getAttribute(
                            'data-singleselection-node-displayname'));
                        break;
                    default:
                        // Multiple selection
                        contextMenuTitle = params.multipleSelection.replace('{{count}}', DX_app.iframe.data.selectionCount);
                        break;
                }

                contextMenuList.setAttribute('data-indigo-title', contextMenuTitle);
            }
        },
        managerMenu: {
            data: {
                opened: false
            },
            /**
             * Callback executed when the Hamburger Menu is opened
             */
            onOpen: function () {
                DX_app.dev.log('::: APP ::: CONTEXTMENUS ::: MANAGERMENU ::: ONOPEN');
                DexV2.getCached('body').setAttribute('data-indigo-hamburger-menu', 'open');
                DX_app.contextMenus.managerMenu.data.opened = true;

                DexV2.node(this).setAttribute('data-indigo-current-app', DX_app.data.currentApp);

                if (!DexV2.class('menu-editmode-managers-menu').hasClass('managers-menu-built')) {
                    var footerContainer = document.createElement('div');
                    footerContainer.classList.add('footer');

                    var loggedUserLabel = document.createElement('label');
                    var loggedUser = DexV2.getCached('body').getAttribute('data-currentuser');

                    var closeButton = document.createElement('button');
                    var closeButtonLabel = document.createTextNode('Close');
                    var backgroundMask = document.createElement('div');

                    loggedUserLabel.innerHTML = 'Logged in as <span>' + loggedUser + '</span>';
                    loggedUserLabel.classList.add('user');

                    backgroundMask.classList.add('managers-menu-mask');
                    closeButton.classList.add('managers-menu-close');
                    closeButton.appendChild(closeButtonLabel);

                    backgroundMask.addEventListener('click', function () {
                        DexV2.getCached('body')
                            .trigger('mousedown')
                            .trigger('mouseup');
                    });

                    footerContainer.appendChild(loggedUserLabel);
                    footerContainer.appendChild(loggedUserLabel);

                    DexV2.class('menu-editmode-managers-menu').prepend(footerContainer);
                    DexV2.class('menu-editmode-managers-menu').prepend(closeButton);
                    DexV2.class('menu-editmode-managers-menu').append(backgroundMask);

                    DexV2('.menu-editmode-managers-menu').onClick('.managers-menu-close', function () {
                        DexV2.getCached('body')
                            .trigger('mousedown')
                            .trigger('mouseup');
                    }, 'CLOSE-DX-MENU');

                    DexV2.class('menu-editmode-managers-menu').addClass('managers-menu-built');
                }
            },
            /**
             * Callback executed when the Hamburger Menu is closed
             */
            onClose: function () {
                DX_app.dev.log('::: APP ::: CONTEXTMENUS ::: MANAGERMENU ::: ONCLOSE');
                DexV2.getCached('body').setAttribute('data-indigo-hamburger-menu', '');
                DX_app.contextMenus.managerMenu.data.opened = false;
            }
        },
        previewMenu: {
            /**
             * Callback executed when the user opens the preview drop down
             */
            onOpen: function () {
                DX_app.dev.log('::: APP ::: CONTEXTMENUS ::: PREVIEWMENU ::: ONOPEN');

                // Set the title that appears in the Context Menu
                DX_app.contextMenus.setTitle(this, {
                    noSelection: DX_app.dictionary('previewPage'),
                    singleSelection: DX_app.dictionary('previewSingleSelection'),
                    multipleSelection: DX_app.dictionary('previewMultipleSelection')
                });
            }
        },
        publicationMenu: {
            /**
             * Callback executed when the user opens the publication drop down
             */
            onOpen: function () {
                DX_app.dev.log('::: APP ::: CONTEXTMENUS ::: PUBLICATIONMENU ::: ONOPEN');

                // Set the title that appears in the Context Menu
                DX_app.contextMenus.setTitle(this, {
                    noSelection: DX_app.dictionary('publishPage'),
                    singleSelection: DX_app.dictionary('publishSingleSelection'),
                    multipleSelection: DX_app.dictionary('publishMultipleSelection')
                });
            }
        },
        moreInfoMenu: {
            /**
             * Callback executed when the user opens the more info drop down
             */
            onOpen: function () {
                DX_app.dev.log('::: APP ::: CONTEXTMENUS ::: MOREINFOMENU ::: ONOPEN');
                DX_app.contextMenus.setTitle(this, {
                    noSelection: DX_app.dictionary('optionsPage'),
                    singleSelection: DX_app.dictionary('optionsSingleSelection'),
                    multipleSelection: DX_app.dictionary('optionsMultipleSelection')
                });
            }
        }
    },
    backgroundJobs: {
        data: {
            filters: [],
            open: false
        },
        /**
         * Callback executed when the Background Jobs modal is opened in Edit Mode
         */
        onOpen: function () {
            // Update title
            DexV2.class('job-list-window').filter('.x-window-tl .x-window-header-text').setHTML(DX_app.dictionary('backgroundJobs'));
            DexV2.class('job-list-window').filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar')
                .setAttribute('indigo-label', DX_app.dictionary('jobs'));
            DexV2.class('job-list-window')
                .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(3) > div')
                .setAttribute('indigo-label', DX_app.dictionary('autoRefresh'));

            // Reset the filters array
            DX_app.backgroundJobs.data.filters = [];

            // Open GWT Filter menu to copy the entries and build our own menu
            DexV2.class('job-list-window') // Get thomas to add a class on the filtered combo ...
                .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(1) > table')
                .trigger('click');

            // Wait until the filter menu is opened, then copy the contents to create our own filter menu
            DexV2('body').onceOpen('.x-menu-list-item', function () {
                var menu = DexV2('.x-menu-list .x-menu-list-item span');
                menu.each(function (menuItem) {
                    // Get Label
                    var textNode = menuItem.getHTML();
                    var labelSplit = textNode.split('<');
                    var label = labelSplit[0];

                    // Get checked status
                    var img = menuItem.filter('img');
                    var backgroundPosition = window.getComputedStyle(img.nodes[0])['background-position'];
                    var isChecked = (backgroundPosition !== '-18px 0px');

                    // Save to filters array
                    DX_app.backgroundJobs.data.filters.push({
                        label: label,
                        isChecked: isChecked
                    });
                });

                // Build the side menu
                DX_app.backgroundJobs.buildFilterMenu();

                // Close the drop down menu
                DexV2.getCached('body').trigger('mousedown').trigger('mouseup');
            }, 'BACKGROUND-JOBS-INIT-FILTER');

            // Filter toggles
            DexV2.class('job-list-window').onClick('.indigo-switch > div', function () {
                var filterEntry = DexV2.node(this).parent();
                var filterID = filterEntry.getAttribute('data-indigo-switch-id');

                filterEntry.toggleAttribute('data-indigo-switch-checked', ['true', 'false']);

                // Open the GWT filter combo
                DexV2.class('job-list-window') // Get thomas to add a class on the filtered combo ...
                    .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(1) > table')
                    .trigger('click');
                // When it has opened, trigger click the selected filter type
                DexV2('body').onceOpen('.x-menu', function () {
                    var menu = DexV2('.x-menu-list .x-menu-list-item span').index(filterID);
                    menu.trigger('click');
                }, 'BACKGROUND-JOBS-TRIGGER-FILTER');
            }, 'BACKGROUND-JOBS-TOGGLE-FILTER');

            // Executes when results are loaded into the list
            DexV2.class('job-list-window').onOpen('.x-grid-group, .x-grid3-row', function () {
                var previousButton = DexV2
                    .class('job-list-window')
                    .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-bbar .x-toolbar-layout-ct .x-toolbar-left .x-toolbar-cell:nth-child(2) > table');
                var nextButton = DexV2
                    .class('job-list-window')
                    .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-bbar .x-toolbar-layout-ct .x-toolbar-left .x-toolbar-cell:nth-child(8) > table');

                // Look at the previous and next buttons to determine if there is more than one page of results
                if (previousButton.hasClass('x-item-disabled') && nextButton.hasClass('x-item-disabled')) {
                    // Only one page, so hide pager
                    DexV2.class('job-list-window').setAttribute('indigo-results-multiple-pages', 'false');
                } else {
                    // More than one page, so show pager
                    DexV2.class('job-list-window').setAttribute('indigo-results-multiple-pages', 'true');
                }

                // Add info and delete button to each row
                var rows = DexV2.class('job-list-window').filter('.x-grid3-row');

                // Build the menu
                var actionMenu = document.createElement('menu');
                var deleteButton = document.createElement('button');
                var infoButton = document.createElement('button');

                // Add classes to menu elements
                actionMenu.classList.add('action-menu');
                deleteButton.classList.add('delete-button');
                infoButton.classList.add('info-button');

                // Add buttons to the menu
                actionMenu.appendChild(infoButton);
                actionMenu.appendChild(deleteButton);

                // Duplicate and add the menu to each row
                rows.each(function () {
                    var clonedActionMenu = actionMenu.cloneNode(true);

                    // This listener is sometimes called more than once, so check if the row has already had action menu added before adding ...
                    if (!DexV2.node(this).hasClass('indigo-contains-actions-menu')) {
                        DexV2.node(this)
                            .addClass('indigo-contains-actions-menu')
                            .append(clonedActionMenu);
                    }
                });

                // Flag that there are results ...
                DexV2.class('job-list-window').setAttribute('indigo-results', 'true');
            }, 'BACKGROUND-JOBS-FILTERED-RESULTS');

            // Excutes when there are no rsults ...
            DexV2.class('job-list-window').onOpen('.x-grid-empty', function () {
                // Flag that there are no results
                DexV2.class('job-list-window').setAttribute('indigo-results', 'false');

            }, 'BACKGROUND-JOBS-FILTERED-RESULTS');

            // User has toggled the auto refresh checkbox, display the seconds input accordingly
            DexV2('.job-list-window').onClick('input[type=\'checkbox\']', function () {
                DX_app.backgroundJobs.autoRefreshUpdate();
            });

            // User has clicked on the delete entry button
            DexV2('.job-list-window').onClick('.delete-button', function () {
                // Trigger click on the hidden GWT delete button
                DexV2.class('job-list-window').filter(
                    '.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(7) > table')
                    .trigger('click');
            }, 'BACKGROUND-JOBS-DELETE-ENTRY');

            // User has clicked on the info button
            DexV2('.job-list-window').onClick('.info-button', function () {
                // Open the details panel by flagging the attribute
                DexV2.class('job-list-window').setAttribute('data-indigo-details', 'open');
            }, 'BACKGROUND-JOBS-DETAILS-ENTRY');

            // User has clicked on the close details panel
            DexV2('.job-list-window').onClick('.x-window-bwrap .x-panel:nth-child(2) .x-panel-header .x-panel-toolbar', function () {
                // Remove the  flag that displays the details panel
                DexV2.class('job-list-window').setAttribute('data-indigo-details', '');
            });

            DexV2('.job-list-window').onMouseOver('.x-grid3-row', function (e) {
                // Impossible to know if an entry can be deleted or not WITHOUT first selecting it.
                // Now we select a row when it is rolled over, check if the delete button is enabled, if it is not clickable,
                //   then we hide the delete button that we previously added to the row
                var row = DexV2.node(e.target);
                var isRow = row.hasClass('x-grid3-row');
                if (isRow) {
                    // Select the row
                    row.trigger('mousedown');

                    // See if the GWT delete button is clickable
                    var cantDelete = DexV2
                        .class('job-list-window')
                        .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(7) > table')
                        .hasClass('x-item-disabled');

                    if (cantDelete) {
                        // The GWT delete button is disactivated, so hide our delete button
                        row.addClass('indigo-cant-delete');
                    }
                }
            }, 'BACKGROUND-JOBS-DETAILS-ROW-OVER');

            // Initiate the auto refresh display type
            DX_app.backgroundJobs.autoRefreshUpdate();
        },
        /**
         * Callback executed when the Background Jobs modal is closed
         */
        onClose: function () {},
        /**
         * If the user has chosen auto refresh of background jobs we need to display the text input so user can select interval
         */
        autoRefreshUpdate: function () {
            var isChecked = DexV2.class('job-list-window').filter('input[type=\'checkbox\']').nodes[0].checked;
            DexV2.class('job-list-window').setAttribute('indigo-auto-refresh', isChecked);
        },
        /**
         * Build the filter list on the side panel of the Background Jobs Modal
         */
        buildFilterMenu: function () {
            // Build the filter menu on the left side of the screen
            // The details have been previously recuperated from the GWT filter by combo
            var filters = DX_app.backgroundJobs.data.filters;

            var filterMenu = document.createElement('div');
            var filterMenuTitle = document.createElement('h1');
            var filterMenuTitleText = document.createTextNode('Filters ...');

            var switchHolder = document.createElement('div');
            var switchRail = document.createElement('div');
            var switchShuttle = document.createElement('div');

            // Define Menu
            filterMenu.classList.add('indigo-background-jobs-filters');

            // Define Title
            filterMenuTitle.appendChild(filterMenuTitleText);
            filterMenuTitle.classList.add('indigo-background-jobs-filters-title');

            // Create Switch Master
            switchHolder.classList.add('indigo-switch');
            switchRail.classList.add('indigo-switch--rail');
            switchShuttle.classList.add('indigo-switch--shuttle');
            switchHolder.appendChild(switchRail);
            switchHolder.appendChild(switchRail);
            switchHolder.appendChild(switchShuttle);

            for (var n = 0; n < filters.length; n++) {
                var filterEntry = switchHolder.cloneNode(true);

                filterEntry.setAttribute('data-indigo-switch-id', n);
                filterEntry.setAttribute('data-indigo-switch-label', filters[n].label);
                filterEntry.setAttribute('data-indigo-switch-checked', filters[n].isChecked);

                filterMenu.appendChild(filterEntry);
            }

            // Remove the filters, just incase it has already been added
            DexV2('.indigo-background-jobs-filters').remove();

            // Add the new Filters Menu
            DexV2('.job-list-window').append(filterMenu);
        }
    },
    picker: {
        data: {
            currentItem: null,
            title: null,
            pickerTitle: null,
            displayType: 'listview',
            subPickerDisplayType: 'listview',
            currentDisplayType: 'displayType',
            previousDisplayType: null,
            ID: 'JahiaGxtContentPickerWindow',
            standaloneID: 'contentpicker',
            standaloneManagerID: 'contentmanager',
            inpageID: 'JahiaGxtContentPickerWindow',
            hasPreview: null,
            selectedFileCount: 0,
            selectedSubFileCount: 0,
            explorer: {
                width: 340
            },
            zooms: {
                thumbsview: 1,
                detailedview: 2
            },
            open: false
        },
        /**
         * Fix the picker side panel when it has been resized
         * @param splitterLeft
         */
        repositionSidePanel: function (splitterLeft) {
            var searchIsOpen = DexV2.getCached('body').getAttribute('data-INDIGO-PICKER-SEARCH') == 'open',
                explorerWidth = (searchIsOpen) ? 1 : (splitterLeft || DX_app.picker.data.explorer.width);

            if(!searchIsOpen && splitterLeft){
                // Want to save the
                DX_app.picker.data.explorer.width = splitterLeft;
            }

            DX_app.dev.log('::: APP ::: PICKER ::: REPOSITIONSIDEPANEL');

            var isNestedPicker = DexV2.getCached('body').getAttribute('data-indigo-sub-picker') == 'open';
            var DOMPaths = {
                'JahiaGxtManagerLeftTree': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree' : '#JahiaGxtManagerLeftTree',
                'JahiaGxtManagerTobTable': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #JahiaGxtManagerTobTable' : '#JahiaGxtManagerTobTable',
                'JahiaGxtManagerToolbar': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #JahiaGxtManagerToolbar' : '#JahiaGxtManagerToolbar',
                '#JahiaGxtManagerLeftTree .x-tab-strip-spacer': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-strip-spacer' : '#JahiaGxtManagerLeftTree .x-tab-strip-spacer',
                '#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap' : '#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap',
                '#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap .x-accordion-hd': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap .x-accordion-hd' : '#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap .x-accordion-hd'
            };

            // Calculate Scale size for the picker title
            var pickerTitle = (DX_app.picker.data.standalone) ? DexV2('#pickerTitle')
                : DexV2.id(DX_app.picker.data.ID).filter('.x-window-tl .x-window-header-text');

            // Reset the Title size to recalculate the scale from scratch
            pickerTitle.css({
                transform: 'scale(1)',
                transformOrigin: 'left'
            });

            var pickerTitleBox = pickerTitle.getNode(0).getBoundingClientRect();
            var pickerTitleBoxLeft = pickerTitleBox.left;
            var pickerTitleBoxWidth = pickerTitleBox.width;
            var pickerTitleBoxPadding = (pickerTitleBoxLeft * 2);
            var searchButtonWidth = 50;
            var pickerTitleBoxScale = Math.min(explorerWidth / (pickerTitleBoxPadding + pickerTitleBoxWidth + searchButtonWidth), 1);

            // Set size of the Title
            pickerTitle.css({
                transform: 'scale(' + pickerTitleBoxScale + ')',
                transformOrigin: 'left center'
            });

            // Update title box info
            pickerTitleBox = pickerTitle.getNode(0).getBoundingClientRect();
            pickerTitleBoxLeft = pickerTitleBox.left;
            pickerTitleBoxWidth = pickerTitleBox.width;
            var searchLeftPosition = (pickerTitleBoxLeft + pickerTitleBoxWidth + 5);

            // Move the search button
            DexV2.id('JahiaGxtManagerLeftTree__CRTsearchTabItem').css({
                left: searchLeftPosition + 'px'
            });

            // Set width of the side panel
            DexV2(DOMPaths['JahiaGxtManagerLeftTree']).nodes[0].style.setProperty('width', explorerWidth + 'px',
                'important');
            DexV2(DOMPaths['JahiaGxtManagerLeftTree']).nodes[0].style.setProperty('left', '0px', 'important');

            // Set width and position of right panel
            if (DexV2.getCached('body').getAttribute('indigo-picker-panel') == 'collapsed') {
                DexV2(DOMPaths['JahiaGxtManagerTobTable']).nodes[0].style.setProperty('left', '0px', 'important');
                DexV2(DOMPaths['JahiaGxtManagerTobTable']).nodes[0].style.setProperty('width', '100%', 'important');
                // Move the top toolbar
                DexV2(DOMPaths['JahiaGxtManagerToolbar']).nodes[0].style.setProperty('left', searchLeftPosition + 'px', 'important');

                // Move filter toolbar
                if (DexV2(DOMPaths['JahiaGxtManagerTobTable']).filter('.x-panel-tbar').exists()) {
                    DexV2(DOMPaths['JahiaGxtManagerTobTable']).filter('.x-panel-tbar').nodes[0].style.setProperty('left', '20px',
                        'important');
                }
            } else {
                DexV2(DOMPaths['JahiaGxtManagerTobTable']).nodes[0].style.setProperty('left', explorerWidth + 'px',
                    'important');
                DexV2(DOMPaths['JahiaGxtManagerTobTable']).nodes[0].style.setProperty('width',
                    'calc(100% - ' + explorerWidth + 'px) ',
                    'important');
                // Move the top toolbar
                DexV2(DOMPaths['JahiaGxtManagerToolbar']).nodes[0].style.setProperty('left', explorerWidth + 'px',
                    'important');

                // Move filter toolbar
                if (DexV2(DOMPaths['JahiaGxtManagerTobTable']).filter('.x-panel-tbar').exists()) {
                    DexV2(DOMPaths['JahiaGxtManagerTobTable']).filter('.x-panel-tbar').nodes[0].style.setProperty('left',
                        (explorerWidth + 20) + 'px',
                        'important');
                }
            }

            // Move toggle button
            DexV2.id('toggle-picker-files').css({
                left: (explorerWidth - 25) + 'px'
            });

            // Set the width of the left tree
            DexV2(DOMPaths['#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap']).each(function () {
                this.style.setProperty('width', explorerWidth + 'px', 'important');
            });

            DexV2(DOMPaths['#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap .x-accordion-hd']).each(function () {
                this.style.setProperty('width', explorerWidth + 'px', 'important');
            });

            // Set the position of the refresh button based on VISIBLE combo header
            var sourceCombo = DexV2('#CRTbrowseTabItem > div > .x-panel:not(.x-panel-collapsed) .x-accordion-hd .x-panel-header-text');
            var sourceComboBox = sourceCombo.getNode(0).getBoundingClientRect();
            var sourceComboBoxLeft = sourceComboBox.left;
            var sourceComboBoxWidth = sourceComboBox.width;
            var RefreshLeftPosition = (sourceComboBoxLeft + sourceComboBoxWidth);

            DexV2('#CRTbrowseTabItem > div > .x-panel .x-accordion-hd .x-tool-refresh').css({
                left: RefreshLeftPosition + 'px'
            });

            // Set the width of the Combo Hot Spot
            DexV2(DOMPaths['#JahiaGxtManagerLeftTree .x-tab-strip-spacer']).css({
                width: (sourceComboBoxWidth + 4) + 'px'
            });
        },
        /**
         * Callback executed whilst the picker side panel is being resized
         * @returns {boolean}
         */
        onResize: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: ONRESIZE');

            var previousDisplayButton = DexV2('.toolbar-item-' + DX_app.picker.data[DX_app.picker.data.currentDisplayType]);

            if(previousDisplayButton.exists()){
                previousDisplayButton.trigger('click');
            }

            // User has resized the left panel in a picker
            if (DexV2.getCached('body').getAttribute('data-indigo-picker') == 'open') {
                var splitterLeft = parseInt(DexV2.node(this).nodes[0].style.left);

                // If the requested drag position of the user is too narrow, reset to minimum width
                if (splitterLeft < 290) {
                    // Too narrow, so set to minimum width
                    DexV2('.x-vsplitbar').css({
                        left: '290px'
                    });

                    // Stop execution
                    return false;
                } else {
                    DX_app.picker.repositionSidePanel(splitterLeft);
                }
            }
        },
        /**
         * Callback executed when the picker is opened
         */
        onOpen: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: ONOPEN');

            // See if GWT has enabled previews for files, if so then set the preview flag to true
            DX_app.picker.data.enablePreviews = DexV2('#' + DX_app.picker.data.ID + ' .toolbar-item-filepreview').nodes.length > 0;

            // Set flags for CSS
            DexV2.getCached('body')
                .setAttribute('data-INDIGO-PICKER-SEARCH', '')
                .setAttribute('data-INDIGO-PICKER', 'open')
                .setAttribute('indigo-PICKER-DISPLAY', DX_app.picker.data[DX_app.picker.data.currentDisplayType]);

            if (DX_app.picker.data.standalone) {
                // Create title
                var pickerH1 = document.createElement('h1');
                var pickerH1Label = document.createTextNode(DX_app.dictionary('pickerTitle-' + DX_app.data.HTTP.picker));

                pickerH1.id = 'pickerTitle';
                pickerH1.appendChild(pickerH1Label);

                // Add title to page
                DexV2.id(DX_app.picker.data.ID).prepend(pickerH1);

                if (DX_app.data.HTTP.app == 'manager') {
                    // See if GWT has enabled previews for files, if so then set the preview flag to true
                    DX_app.picker.data.enablePreviews = true;
                }
            }

            // Set zoom states
            DX_app.picker.updateZoomLevel();

            var pickerTitle = (DX_app.picker.data.standalone) ? DexV2('#pickerTitle')
                : DexV2.id(DX_app.picker.data.ID).filter('.x-window-tl .x-window-header-text');
            var box = pickerTitle.getNode(0).getBoundingClientRect();
            var left = box.left;
            var width = box.width;
            var searchLeftPosition = (left + width + 5);

            DexV2.id('JahiaGxtManagerLeftTree__CRTsearchTabItem').css({
                left: searchLeftPosition + 'px'
            });

            // Save picker title ( for later use in search placeholder )
            DX_app.picker.data.pickerTitle = pickerTitle.getHTML();

            // Create button to toggle the left panel
            var toggleFilesButton = document.createElement('button');

            toggleFilesButton.id = 'toggle-picker-files';
            toggleFilesButton.classList.add('toggle-picker-files');

            DexV2.id(DX_app.picker.data.ID).prepend(toggleFilesButton);

            // Add placeholders to form elements
            DX_app.picker.setPlaceholders();

            // Reset classes that may have been previously added
            DexV2.id(DX_app.picker.data.ID).removeClass('search-panel-opened');

            // Register the side panel as open:
            DexV2.id(DX_app.picker.data.ID).setAttribute('indigo-picker-panel', 'opened');

            // Listen for clicks on toggle button
            DexV2.id(DX_app.picker.data.ID).onClick('#toggle-picker-files', function () {
                DexV2.id(DX_app.picker.data.ID).toggleClass('indigo-collapsed');
                DexV2.id(DX_app.picker.data.ID).toggleAttribute('indigo-picker-panel', ['collapsed', 'opened']);
                DexV2.getCached('body').toggleAttribute('indigo-picker-panel', ['collapsed', 'opened']);

                var pickerTitle = (DX_app.picker.data.standalone) ? DexV2('#pickerTitle')
                    : DexV2.id(DX_app.picker.data.ID).filter('.x-window-tl .x-window-header-text');
                var box = pickerTitle.getNode(0).getBoundingClientRect();
                var left = box.left;
                var width = box.width;
                var toolbarLeft = (left + width);

                DexV2.id('JahiaGxtManagerToolbar').css({
                    left: toolbarLeft + 'px'
                });

                DX_app.picker.repositionSidePanel();
            }, 'TOGGLE-PICKER-FILES');

            // Listen for changes in slider (input range)
            DexV2.id(DX_app.picker.data.ID).onInput('#thumb-size-slider', function (e) {
                var zoomSize = e.target.value;

                // Save zoom level
                DX_app.picker.data.zooms[DX_app.picker.data[DX_app.picker.data.currentDisplayType]] = zoomSize;

                DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree + div #images-view .x-view')
                    .setAttribute('indigo-thumb-zoom', zoomSize);
            }, 'THUMB-SIZE-SLIDER');

            // If it is a multi picker we need to do this ...
            if (DexV2.id(DX_app.picker.data.ID).filter('#JahiaGxtManagerBottomTabs').exists()) {
                // Create a toggle button for multiple selection
                var toggleButton = document.createElement('button');
                var toggleButtonLabel = document.createTextNode('Multiple Selection');

                toggleButton.appendChild(toggleButtonLabel);
                toggleButton.classList.add('toggle-multiple-selection');

                DexV2.id(DX_app.picker.data.ID).filter('#JahiaGxtManagerBottomTabs').prepend(toggleButton);

                // Add class for CSS
                DexV2.id(DX_app.picker.data.ID).addClass('indigo-picker-multi-select');

                // Listen for files being added to the multiple selection
                DexV2.id(DX_app.picker.data.ID).onGroupOpen('#JahiaGxtManagerBottomTabs .x-grid-group', function () {
                    DX_app.picker.data.selectedFileCount = this.length;
                    DX_app.picker.updateMultipleCount();
                }, 'ADDED_FILES_MULTI_SELECT');

                // Listen for files being removed from the multiple selection
                DexV2.id(DX_app.picker.data.ID).onGroupClose('.x-grid-group', function () {
                    // Need to manually count the files in multiple selection ...
                    DX_app.picker.data.selectedFileCount = DexV2.id(DX_app.picker.data.ID).filter('#JahiaGxtManagerBottomTabs .x-grid-group').nodes.length;
                    DX_app.picker.updateMultipleCount();
                }, 'REMOVED_FILES_MULTI_SELECT');

                // Listen for clicks on the multiple selection toggle button
                DexV2.id(DX_app.picker.data.ID).onClick('.toggle-multiple-selection', function () {
                    DexV2.id('JahiaGxtManagerBottomTabs').toggleClass('indigo-collapsed');
                }, 'TOGGLE_MULTI_SELECT');
            }

            // See if GWT has included a slider for thumb preview, if so then we can add ours ( which is a GWT replacement )
            var hasSlider = DexV2('#' + DX_app.picker.data.ID + ' .x-slider').nodes.length > 0;
            if (hasSlider || DX_app.data.HTTP.app == 'manager') {
                var thumbSlider = document.createElement('input');
                thumbSlider.id = 'thumb-size-slider';
                thumbSlider.classList.add('thumb-size-slider');
                thumbSlider.type = 'range';
                thumbSlider.value = 4;
                thumbSlider.min = 1;
                thumbSlider.max = 6;

                DexV2.id(DX_app.picker.data.ID).prepend(thumbSlider);
            }
        },
        /**
         * Callback executed when a picker opened from within a picker is closed
         */
        onCloseSubPicker: function(){
            DX_app.dev.log('::: APP ::: PICKER ::: ONCLOSESUBPICKER');
            DX_app.picker.data.currentDisplayType = 'displayType';
            DexV2.getCached('body').setAttribute('indigo-PICKER-DISPLAY', DX_app.picker.data.displayType);
        },
        /**
         * Callback executed when a picker is opened from within a picker
         */
        onOpenSubPicker: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: ONOPENSUBPICKER');

            DX_app.picker.data.currentDisplayType = 'subPickerDisplayType';

            // Set flags for CSS
            DexV2.getCached('body')
                .setAttribute('data-INDIGO-SUB-PICKER', 'open')
                .setAttribute('data-INDIGO-PICKER-SEARCH', '')
                .setAttribute('data-INDIGO-PICKER', 'open')
                .setAttribute('indigo-PICKER-DISPLAY', DX_app.picker.data.subPickerDisplayType);

            // Set zoom states
            DX_app.picker.updateZoomLevel();

            var pickerTitle = DexV2('body > #JahiaGxtContentPickerWindow .x-window-header-text');
            var box = pickerTitle.getNode(0).getBoundingClientRect();
            var left = box.left;
            var width = box.width;
            var searchLeftPosition = (left + width + 5);

            DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree__CRTsearchTabItem').css({
                left: searchLeftPosition + 'px'
            });

            // Save picker title ( for later use in search placeholder )
            DX_app.picker.data.subPickerTitle = pickerTitle.getHTML();

            // Create button to toggle the left panel
            var toggleFilesButton = document.createElement('button');
            toggleFilesButton.id = 'toggle-sub-picker-files';
            toggleFilesButton.classList.add('toggle-picker-files');

            DexV2('body > #JahiaGxtContentPickerWindow').prepend(toggleFilesButton);

            // Add placeholders to form elements
            var filterField = DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(2) .x-form-text');
            var sortBy = DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(5) .x-form-text');

            filterField.setAttribute('placeholder', DX_app.dictionary('filterField'));
            sortBy.setAttribute('placeholder', DX_app.dictionary('sortBy'));

            // Reset classes that may have been previously added
            DexV2('body > #JahiaGxtContentPickerWindow').removeClass('search-panel-opened');
            DexV2.id(DX_app.picker.data.ID).removeClass('search-panel-opened');

            // Register the side panel as open:
            DexV2('body > #JahiaGxtContentPickerWindow').setAttribute('indigo-picker-panel', 'opened');

            // Listen for clicks on toggle button
            DexV2('body > #JahiaGxtContentPickerWindow').onClick('#toggle-sub-picker-files', function () {
                DexV2('body > #JahiaGxtContentPickerWindow').toggleClass('indigo-collapsed');
                DexV2('body > #JahiaGxtContentPickerWindow').toggleAttribute('indigo-picker-panel', ['collapsed', 'opened']);
                DexV2.getCached('body').toggleAttribute('indigo-sub-picker-panel', ['collapsed', 'opened']);

                var pickerTitle = DexV2('body > #JahiaGxtContentPickerWindow .x-window-header-text');
                var box = pickerTitle.getNode(0).getBoundingClientRect();
                var left = box.left;
                var width = box.width;
                var toolbarLeft = (left + width);

                DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerToolbar').css({
                    left: toolbarLeft + 'px'
                });
            }, 'TOGGLE-SUB-PICKER-FILES');

            // Listen for changes in slider (input range)
            DexV2('body > #JahiaGxtContentPickerWindow').onInput('#sub-picker-thumb-size-slider', function (e) {
                var zoomSize = e.target.value;
                // Save zoom level
                DX_app.picker.data.zooms[DX_app.picker.data[DX_app.picker.data.currentDisplayType]] = zoomSize;

                DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div #images-view .x-view')
                    .setAttribute('indigo-thumb-zoom', zoomSize);
            }, 'SUB-PICKER-THUMB-SIZE-SLIDER');

            // If it is a multi picker we need to do this ...
            if (DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').exists()) {
                // Create a toggle button for multiple selection
                var toggleButton = document.createElement('button');
                var toggleButtonLabel = document.createTextNode('Multiple Selection');
                toggleButton.appendChild(toggleButtonLabel);
                toggleButton.classList.add('toggle-multiple-selection');

                DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').prepend(toggleButton);

                // Add class for CSS
                DexV2('body > #JahiaGxtContentPickerWindow').addClass('indigo-picker-multi-select');

                // Listen for files being added to the multiple selection
                DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs')
                    .onGroupOpen('.x-grid-group', function () {
                        DX_app.picker.data.selectedSubFileCount = this.length;
                        DX_app.picker.updateMultipleSubCount();
                    }, 'ADDED_FILES_MULTI_SELECT_SUB');

                // Listen for files being removed from the multiple selection
                DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs')
                    .onGroupClose('.x-grid-group', function () {
                        // Need to manually count the files in multiple selection ...
                        DX_app.picker.data.selectedSubFileCount = DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs')
                            .filter('.x-grid-group').nodes.length;
                        DX_app.picker.updateMultipleSubCount();
                    }, 'REMOVED_FILES_MULTI_SELECT_SUB');

                // Listen for clicks on the multiple selection toggle button
                DexV2.id('JahiaGxtManagerBottomTabs').onClick('.toggle-multiple-selection', function () {
                    DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').toggleClass('indigo-collapsed');
                }, 'TOGGLE_MULTI_SELECT_SUB');
            }

            // Add slider for zooming images
            var thumbSlider = document.createElement('input');
            thumbSlider.id = 'sub-picker-thumb-size-slider';
            thumbSlider.classList.add('thumb-size-slider');
            thumbSlider.type = 'range';
            thumbSlider.value = 4;
            thumbSlider.min = 1;
            thumbSlider.max = 6;

            DexV2('body > #JahiaGxtContentPickerWindow').prepend(thumbSlider);
            var subPickerListViewButton = DexV2('.modal-imagepicker .toolbar-item-' + DX_app.picker.data[DX_app.picker.data.currentDisplayType]);
            subPickerListViewButton.trigger('click');
        },
        /**
         * Callback executed when multiple picker selection changes
         */
        updateMultipleCount: function () {
            var toggleString;
            var selectedFileCount = DX_app.picker.data.selectedFileCount;
            if (selectedFileCount > 0) {
                DexV2.id('JahiaGxtManagerBottomTabs').addClass('selected-files');
                toggleString = 'Multiple selection (' + selectedFileCount + ')';
            } else {
                DexV2.id('JahiaGxtManagerBottomTabs').removeClass('selected-files');
                toggleString = 'Multiple selection';
            }

            DexV2.class('toggle-multiple-selection').setHTML(toggleString);
        },
        /**
         * Callback executed when nested multiple picker selection changes
         */
        updateMultipleSubCount: function () {
            var toggleString;
            var selectedFileCount = DX_app.picker.data.selectedSubFileCount;
            if (selectedFileCount > 0) {
                DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').addClass('selected-files');
                toggleString = 'Multiple selection (' + selectedFileCount + ')';
            } else {
                DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').removeClass('selected-files');
                toggleString = 'Multiple selection';
            }

            DexV2('body > #JahiaGxtContentPickerWindow .toggle-multiple-selection').setHTML(toggleString);
        },
        /**
         * Callback executed when the thumbnail size slider is changed
         */
        updateZoomLevel: function () {
            if (DexV2.id('thumb-size-slider').nodes[0]) {
                DexV2.id('thumb-size-slider').nodes[0].value = DX_app.picker.data.zooms[DX_app.picker.data[DX_app.picker.data.currentDisplayType]];
            }

            DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree + div #images-view .x-view')
                .setAttribute('indigo-thumb-zoom', DX_app.picker.data.zooms[DX_app.picker.data[DX_app.picker.data.currentDisplayType]]);
        },
        /**
         * Add placeholders to fields in pickers
         */
        setPlaceholders: function () {
            if (DX_app.data.HTTP.app == 'manager') {
                // Have to wait until the fields are loaded
                DexV2.id('JahiaGxtManagerTobTable').onOpen('.x-panel-tbar', function () {
                    var filterField = DexV2('#JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(2) .x-form-text');
                    var sortBy = DexV2('#JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(5) .x-form-text');

                    filterField.setAttribute('placeholder', DX_app.dictionary('filterField'));
                    sortBy.setAttribute('placeholder', DX_app.dictionary('sortBy'));
                }, 'UPDATE_PLACEHOLDERS');
            } else {
                var filterField = DexV2('#images-view .x-toolbar .x-toolbar-left .x-toolbar-cell:nth-child(2) .x-form-text');
                var sortBy = DexV2('#images-view .x-toolbar .x-toolbar-left .x-toolbar-cell:nth-child(5) .x-form-text');
                filterField.setAttribute('placeholder', DX_app.dictionary('filterField'));
                sortBy.setAttribute('placeholder', DX_app.dictionary('sortBy'));
            }
        },
        /**
         * Callback executed when picker closes
         */
        onClose: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: ONCLOSE');
            if (DexV2.getCached('body').getAttribute('data-INDIGO-SUB-PICKER') == 'open') {
                // Closing a sub picker
                DexV2.getCached('body')
                    .setAttribute('data-indigo-sub-picker', '')
                    .setAttribute('data-INDIGO-PICKER-SEARCH', '');
            } else {
                DexV2.getCached('body')
                    .setAttribute('data-INDIGO-PICKER', '');
            }
        },
        /**
         * Callback executed when a picker opened from within a picker closes
         */
        onSubClose: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: ONSUBCLOSE');

            DexV2.getCached('body')
                .setAttribute('data-indigo-sub-picker', '')
                .setAttribute('data-INDIGO-PICKER', '');
        },
        /**
         * Callback executed when user clicks on picker
         */
        onClick: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: ONCLICK');
            DexV2.getCached('body').setAttribute('data-INDIGO-PICKER-SOURCE-PANEL', '');
        },
        /**
         * Callback executed when picker changes to List View
         */
        onListView: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: ONLISTVIEW');
            DX_app.picker.data[DX_app.picker.data.currentDisplayType] = 'listview';
            DexV2.getCached('body').setAttribute('indigo-PICKER-DISPLAY', 'listview');
            DX_app.picker.repositionSidePanel();
        },
        /**
         * Callback executed when picker changes to Thumb View
         */
        onThumbView: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: ONTHUMBVIEW');
            DX_app.picker.data[DX_app.picker.data.currentDisplayType] = 'thumbsview';
            DexV2.getCached('body').setAttribute('indigo-PICKER-DISPLAY', 'thumbsview');
            DX_app.picker.setPlaceholders();
            DX_app.picker.updateZoomLevel();
            DX_app.picker.repositionSidePanel();
        },
        /**
         * Callback executed when picker changes to Detailed View
         */
        onDetailView: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: ONDETAILVIEW');
            DX_app.picker.data[DX_app.picker.data.currentDisplayType] = 'detailedview';
            DexV2.getCached('body').setAttribute('indigo-PICKER-DISPLAY', 'detailedview');
            DX_app.picker.setPlaceholders();
            DX_app.picker.updateZoomLevel();
            DX_app.picker.repositionSidePanel();
        },
        row: {
            /**
             * TO DO
             */
            onClick: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: ROW ::: ONCLICK');
                DexV2.class('toolbar-item-filepreview').setAttribute('indigo-preview-button-state', 'selected');
            },
            /**
             * TO DO
             */
            onMouseOver: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: ROW ::: MOUSEOVER');

                // Dealing with file manager, possibility of images ( and therefore preview button )
                if (DX_app.data.HTTP.app == 'manager'
                    && (DX_app.data.HTTP.picker == 'filemanager-anthracite'
                        || DX_app.data.HTTP.picker == 'repositoryexplorer-anthracite')) {
                    var isImage = DexV2.node(this).filter('img[src$="/jnt_file_img.png"]').nodes.length;
                    // Preview is posible ( dealing with an image)
                    if (isImage) {
                        // See if the button has already been added ...
                        if (DexV2.node(this).filter('.preview-button').nodes.length == 0) {
                            var previewButton = document.createElement('button');
                            previewButton.classList.add('preview-button');
                            DexV2.node(this).prepend(previewButton);
                        }
                    }
                }

                // Create and more info button ( if it hasnt aleady been added )
                if (DexV2.node(this).filter('.more-info-button').nodes.length == 0) {
                    var moreInfoButton = document.createElement('button');
                    moreInfoButton.classList.add('more-info-button');
                    DexV2.node(this).prepend(moreInfoButton);
                }

                // Create and edit button ( If this is a Manager and if it hasnt aleady been added )
                if (DX_app.data.HTTP.app == 'manager') {
                    if (DexV2.node(this).filter('.edit-button').nodes.length == 0) {
                        var editButton = document.createElement('button');
                        editButton.classList.add('edit-button');
                        DexV2.node(this).prepend(editButton);
                    }
                }

                DX_app.picker.data.currentItem = DexV2.node(this).getNode(0);
                DX_app.picker.data.title = DexV2.node(this).filter('.x-grid3-col-name').getHTML();

                if (DexV2.node(this).hasClass('x-grid3-row-selected')) {
                    DexV2.class('toolbar-item-filepreview').setAttribute('indigo-preview-button-state', 'selected');
                } else {
                    DexV2.class('toolbar-item-filepreview').setAttribute('indigo-preview-button-state', 'unselected');
                }

                DexV2.class('toolbar-item-filepreview').setAttribute('indigo-preview-button', 'show');
            },
            /**
             * Callback executed when the user clicks on the more info button
             * @param e - Mouse event
             */
            onContext: function (e) {
                DX_app.dev.log('::: APP ::: PICKER ::: ROW ::: ONCONTEXT');
                // if matchClass is passed, then the click is ONLY accepted if the clicked element has that class.
                // if matchClass is not passed then it is accepted.
                var acceptClick = DexV2.node(e.target).hasClass('x-tree3-el');

                if (acceptClick) {
                    DexV2.node(e.target).trigger('contextmenu', e.pageX, e.pageY);
                }
            }
        },
        thumb: {
            /**
             * TO DO
             */
            onClick: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: THUMB ::: ONCLICK');
                DexV2.class('toolbar-item-filepreview').setAttribute('indigo-preview-button-state', 'selected');
                DexV2.node(this).addClass('x-view-over');
            },
            /**
             * TO DO
             */
            onMouseOver: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: THUMB ::: MOUSEOVER');

                // Create and more info button ( if it hasnt aleady been added )
                if (DexV2.node(this).filter('.thumb .more-info-button').nodes.length == 0) {
                    var moreInfoButton = document.createElement('button');
                    moreInfoButton.classList.add('more-info-button');
                    DexV2.node(this).filter('.thumb').prepend(moreInfoButton);
                }

                // Create and add preview button ( if previews exist and have not aleady been added )
                if (DX_app.picker.data.enablePreviews) {
                    if (DexV2.node(this).filter('.thumb .preview-button').nodes.length == 0) {
                        var previewButton = document.createElement('button');
                        previewButton.classList.add('preview-button');
                        DexV2.node(this).filter('.thumb').prepend(previewButton);
                    }
                }

                // Create and edit button ( If this is a Manager and if it hasnt aleady been added )
                if (DX_app.data.HTTP.app == 'manager') {
                    if (DexV2.node(this).filter('.thumb .edit-button').nodes.length == 0) {
                        var editButton = document.createElement('button');
                        editButton.classList.add('edit-button');
                        DexV2.node(this).filter('.thumb').prepend(editButton);
                    }
                }

                DX_app.picker.data.currentItem = DexV2.node(this).getNode(0);
                DX_app.picker.data.title = DexV2.node(this).getAttribute('id');

                if (!DexV2.node(this).hasClass('indigo-force-open')) {
                    DexV2('.x-view-item-sel.indigo-force-open').removeClass('indigo-force-open');
                }
            },
            /**
             * TO DO
             */
            onMouseOut: function () {
                DexV2.class('toolbar-item-filepreview').setAttribute('indigo-preview-button', '');
            },
            /**
             * TO DO
             * @param e
             */
            onContext: function (e) {
                var that = this;

                DexV2.node(this).trigger('contextmenu', e.pageX, e.pageY);
                DexV2.node(this).closest('.thumb-wrap').addClass('indigo-force-open');

                DexV2('body').onceClose('.imagepickerContextMenu', function () {
                    DexV2.node(that).closest('.thumb-wrap').removeClass('indigo-force-open');
                });
            },
            /**
             * TO DO
             */
            openPreview: function () {
                if (DX_app.data.HTTP.app == 'manager') {
                    DexV2.node(this).parent().trigger('dblclick');
                } else {
                    DexV2('#JahiaGxtManagerToolbar .toolbar-item-filepreview').trigger('click');
                }
            },
            /**
             * Open the edit engine for the currently highlighted thumb in the picker
             */
            openEdit: function () {
                DexV2.node(this).parent().trigger('contextmenu');

                // When context menu is opened click on the EDIT button
                DexV2('body').onceOpen('.x-menu', function () {
                    // Need to shift the context menu out of view because it doesnt dissappear until the alert has been closed.
                    DexV2('.x-menu').css({
                        left: '-50000px'
                    });

                    DexV2('.x-menu .toolbar-item-editcontent').trigger('click');
                });
            },
            /**
             * Callback executed when the Edit Engine is closed from within a picker
             */
            closeEdit: function () {
                // Called to close the Edit Engine, either when the user clicks Cancel or Save.
                DexV2.getCached('body').setAttribute('data-indigo-edit-engine', '');
            }
        },
        previewButton: {
            /**
             * DELETE
             */
            onMouseOver: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONMOUSEOVER');
                DexV2.node(DX_app.picker.data.currentItem)
                    .addClass('x-view-over')
                    .addClass('x-grid3-row-over');
            },
            /**
             * DELETE
             */
            onMouseOut: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONMOUSEOUT');
                DexV2.node(DX_app.picker.data.currentItem)
                    .removeClass('x-view-over')
                    .removeClass('x-grid3-row-over');
            },
            /**
             * DELETE
             */
            onClick: function (e) {
                DX_app.dev.log('::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONCLICK');

                if (e.detail.secondClick) {
                    // Just set the good title
                    DexV2('#JahiaGxtImagePopup .x-window-bwrap').setAttribute('data-file-name', DX_app.picker.data.title);
                } else {
                    // Need to select the currently hovered thumb first ...
                    DexV2.node(DX_app.picker.data.currentItem)
                        .trigger('mousedown')
                        .trigger('mouseup');

                    if (DexV2('#' + DX_app.picker.data.ID + ' .toolbar-item-filepreview').hasClass('x-item-disabled')) {
                        alert('Preview unavailable');
                    } else {
                        // Now need to remove the preview ( just incase it is previewing a previously selected thumb)
                        DexV2.id('JahiaGxtImagePopup').remove(); // remove OLD preview

                        // Reclick on the preview button for the newly selected thumb
                        DexV2.node(this).customTrigger('click', { secondClick: true });
                    }
                }

                DexV2.class('toolbar-item-filepreview').setAttribute('indigo-preview-button', 'hide');
            },
            /**
             * DELETE
             */
            reposition: function (node, offset) {
                DX_app.dev.log('::: APP ::: PICKER ::: PREVIEWBUTTON ::: REPOSITION');
                offset = offset || {
                    left: 0,
                    top: 0
                };
                var box = node.getBoundingClientRect();
                var left = box.left;
                var top = box.top;
                var width = box.width;

                DexV2('#JahiaGxtManagerToolbar .toolbar-item-filepreview')
                    .css({
                        top: (top + (offset.top)) + 'px',
                        left: ((left + width) + offset.left + 5) + 'px'
                    })
                    .addClass('indigo-show-button');
            }
        },

        source: {
            /**
             * DELETE
             */
            onChange: function () {},
            /**
             * DELETE
             */
            onMouseOver: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: SOURCE ::: ONMOUSEOVER');
                // USER HAS ROLLED OVER THE COMBO TRIGGER
                if (DexV2.getCached('body').getAttribute('data-indigo-picker-source-panel') != 'open') {
                    DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-panel-header').addClass('indigo-hover');
                }
            },
            /**
             * DELETE
             */
            onMouseOut: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: SOURCE ::: ONMOUSEOUT');
                // USER HAS ROLLED OUT OF THE COMBO TRIGGER
                DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-panel-header').removeClass('indigo-hover');
            },
            /**
             * Callback executed when the source combo is closed
             */
            close: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: SOURCE ::: CLOSE');
                // CHANGE SOURCE
                // The user has changed SOURCE, so we just need to hide the combo...
                DexV2.getCached('body').setAttribute('data-INDIGO-PICKER-SOURCE-PANEL', '');
            },
            /**
             * DELETE
             */
            open: function () {},
            /**
             * Callback executed when the source combo opens
             */
            toggle: function (e) {
                DX_app.dev.log('::: APP ::: PICKER ::: SOURCE ::: TOGGLE');
                // USER HAS CLICKED THE COMBO TRIGGER
                e.stopPropagation();

                DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-panel-header').removeClass('indigo-hover');

                DexV2.getCached('body').toggleAttribute('data-INDIGO-PICKER-SOURCE-PANEL', ['open', '']);
            }
        },
        search: {
            /**
             * Reorder the screen when the search has been opened in a picker
             */
            setUpScreen: function () {
                // Save the current display time see we can switch back when closing the search panel
                DX_app.picker.data.previousDisplayType = DX_app.picker.data[DX_app.picker.data.currentDisplayType];

                if(DX_app.data.HTTP.app == 'manager'){
                    // In a picker so go into list mode
                    DexV2('#' + DX_app.picker.data.ID + ' .x-panel-tbar .action-bar-tool-item.toolbar-item-listview').trigger('click');
                }

                // Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
                DexV2.id('CRTbrowseTabItem').addClass('x-hide-display');
                DexV2('#CRTsearchTabItem').removeClass('x-hide-display');

                DexV2.getCached('body').setAttribute('data-INDIGO-PICKER-SEARCH', 'open');
                DexV2.id(DX_app.picker.data.ID).addClass('search-panel-opened');

                // Ask for class names ...
                var searchField = DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(1) td:nth-child(1) input');
                var languageField = DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(2) input');
                var fromDate = DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(5) input');
                var toDate = DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(6) input');
                var dateRange = DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7) input');

                searchField.setAttribute('placeholder', DX_app.dictionary('search').replace('%n%', DX_app.picker.data.pickerTitle));
                languageField.setAttribute('placeholder', DX_app.dictionary('languageField'));
                fromDate.setAttribute('placeholder', DX_app.dictionary('fromDate'));
                toDate.setAttribute('placeholder', DX_app.dictionary('toDate'));
                dateRange.setAttribute('placeholder', DX_app.dictionary('dateAnyTime'));

                // Callback when user opens Date Range context menu ...
                DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7)')
                    .oneClick('img', function () {
                        var alreadyAddedButtons = DexV2('.x-combo-list').hasClass('indigo-buttons');

                        if (!alreadyAddedButtons) {
                            var anyTimeButton = document.createElement('span');
                            var customRangeButton = document.createElement('span');

                            anyTimeButton.innerHTML = DX_app.dictionary('dateAnyTime');
                            anyTimeButton.classList.add('search-anytime-button');
                            anyTimeButton.classList.add('x-combo-list-item');

                            customRangeButton.innerHTML = DX_app.dictionary('dateCustomLabel');
                            customRangeButton.classList.add('search-custom-date-range-button');
                            customRangeButton.classList.add('x-combo-list-item');

                            DexV2('.x-combo-list')
                            // Add Two new buttons to the context menu ...
                                .prepend(anyTimeButton)
                                .append(customRangeButton)
                                .addClass('indigo-buttons');
                        }

                        DexV2('.x-combo-list')
                            .onMouseDown('.x-combo-list-item', function () {
                                // Clicked on a Normal Date Filter ( ie. 1 day, 2 days, etc )
                                DexV2.id(DX_app.picker.data.ID).setAttribute('data-indigo-search-date', 'simple');
                            }, 'PREDEFINED_DATE_RANGE')
                            .onMouseDown('.search-custom-date-range-button', function () {
                                // Clicked on the custom date range button
                                DexV2.id(DX_app.picker.data.ID).setAttribute('data-indigo-search-date', 'custom');
                                dateRange.setAttribute('placeholder', DX_app.dictionary('dateCustom'));

                                // Close the context menu by trigger clicking the page
                                DexV2('#' + DX_app.picker.data.ID).trigger('mousedown').trigger('mouseup');
                            }, 'CUSTOM_DATE_RANGE')
                            .onMouseDown('.search-anytime-button', function () {
                                // Clicked on Any TIme ( removes times filter )
                                dateRange.setAttribute('placeholder', DX_app.dictionary('dateAnyTime'));
                                DexV2.id(DX_app.picker.data.ID).setAttribute('data-indigo-search-date', '');

                                // Close the context menu by trigger clicking the page
                                DexV2('#' + DX_app.picker.data.ID).trigger('mousedown').trigger('mouseup');
                            }, 'ANY_TIME');
                    }, 'SEARCH_PANEL_DATE_RANGE_BUTTON');

                DexV2.id(DX_app.picker.data.ID)
                // Listen for changes to meta tags ...
                    .onClick(
                        '#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)',
                        DX_app.picker.search.updateMetaLabel, 'CHANGE_META_FILTER')
                    // Listen for changes to modification type ...
                    .onClick(
                        '#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) .x-form-check-wrap',
                        DX_app.picker.search.updateModificationLabel, 'CHANGE_MODIFICATION_FILTER')
                    // Toggle modification menu when clicking ...
                    .onClick(
                        '#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) > label',
                        DX_app.picker.search.toggleModificationMenu, 'TOGGLE_MODIFICATION_FILTER');

                // Trigger clicks to initiate the labels of Modification and Meta
                DexV2.id('CRTsearchTabItem')
                    .filter('.x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)')
                    .trigger('click');
                DexV2.id('CRTsearchTabItem')
                    .filter('.x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) input[type=radio]:checked')
                    .trigger('click');

                // Set width of the side panel
                DexV2.id('JahiaGxtManagerLeftTree').nodes[0].style.setProperty('left', '-' + DX_app.picker.data.explorer.width + 'px', 'important');

                // Set position of display results
                DexV2.id('JahiaGxtManagerTobTable').nodes[0].style.setProperty('left', '0px', 'important');
                DexV2.id('JahiaGxtManagerTobTable').nodes[0].style.setProperty('width', '100%', 'important');

                DX_app.picker.repositionSidePanel();
            },
            /**
             * Set up search screen in a picker opened from within a picker
             */
            setUpSubScreen: function () {
                // Save the current display time see we can switch back when closing the search panel
                DX_app.picker.data.previousDisplayType = DX_app.picker.data[DX_app.picker.data.currentDisplayType];

                // Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
                DexV2('body > #JahiaGxtContentPickerWindow #CRTbrowseTabItem').addClass('x-hide-display');
                DexV2('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem').removeClass('x-hide-display');

                DexV2.getCached('body').setAttribute('data-INDIGO-PICKER-SEARCH', 'open');
                DexV2('body > #JahiaGxtContentPickerWindow').addClass('search-panel-opened');

                // Ask for class names ...
                var searchField = DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(1) td:nth-child(1) input');
                var languageField = DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(2) input');
                var fromDate = DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(5) input');
                var toDate = DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(6) input');
                var dateRange = DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7) input');

                searchField.setAttribute('placeholder', DX_app.dictionary('search').replace('%n%', DX_app.picker.data.subPickerTitle));
                languageField.setAttribute('placeholder', DX_app.dictionary('languageField'));

                fromDate.setAttribute('placeholder', DX_app.dictionary('fromDate'));
                toDate.setAttribute('placeholder', DX_app.dictionary('toDate'));
                dateRange.setAttribute('placeholder', DX_app.dictionary('dateAnyTime'));

                // Callback when user opens Date Range context menu ...
                DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7)')
                    .oneClick('img', function () {
                        var alreadyAddedSubButtons = DexV2('.x-combo-list').hasClass('indigo-sub-buttons');

                        if (!alreadyAddedSubButtons) {
                            var anyTimeButton = document.createElement('span');
                            var customRangeButton = document.createElement('span');

                            anyTimeButton.innerHTML = DX_app.dictionary('dateAnyTime');
                            anyTimeButton.classList.add('search-anytime-button');
                            anyTimeButton.classList.add('x-combo-list-item');

                            customRangeButton.innerHTML = DX_app.dictionary('dateCustomLabel');
                            customRangeButton.classList.add('search-custom-date-range-button');
                            customRangeButton.classList.add('x-combo-list-item');

                            DexV2('.x-combo-list')
                            // Add Two new buttons to the context menu ...
                                .prepend(anyTimeButton)
                                .append(customRangeButton)
                                .addClass('indigo-sub-buttons');
                        }

                        DexV2('.x-combo-list.indigo-sub-buttons')
                        // Clicked on a Normal Date Filter ( ie. 1 day, 2 days, etc )
                            .onMouseDown('.x-combo-list-item', function () {
                                DexV2('body > #JahiaGxtContentPickerWindow').setAttribute('data-indigo-search-date', 'simple');
                            }, 'PREDEFINED_DATE_RANGE_SUB')
                            // Clicked on the custom date range button
                            .onMouseDown('.search-custom-date-range-button', function () {
                                DexV2('body > #JahiaGxtContentPickerWindow').setAttribute('data-indigo-search-date', 'custom');
                                dateRange.setAttribute('placeholder', DX_app.dictionary('dateCustom'));

                                // Close the context menu by trigger clicking the page
                                DexV2('body > #JahiaGxtContentPickerWindow').trigger('mousedown').trigger('mouseup');
                            }, 'CUSTOM_DATE_RANGE_SUB')
                            .onMouseDown('.search-anytime-button', function () {
                                // Clicked on Any TIme ( removes times filter )
                                dateRange.setAttribute('placeholder', DX_app.dictionary('dateAnyTime'));
                                DexV2('body > #JahiaGxtContentPickerWindow').setAttribute('data-indigo-search-date', '');

                                // Close the context menu by trigger clicking the page
                                DexV2('body > #JahiaGxtContentPickerWindow').trigger('mousedown').trigger('mouseup');
                            }, 'ANY_TIME_SUB');
                    }, 'SEARCH_PANEL_DATE_RANGE_BUTTON_SUB');

                DexV2('body > #JahiaGxtContentPickerWindow')
                // Listen for changes to meta tags ...
                    .onClick('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)',
                        DX_app.picker.search.updateMetaLabel, 'CHANGE_META_FILTER_SUB')

                    // Listen for changes to modification type ...
                    .onClick('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) .x-form-check-wrap',
                        DX_app.picker.search.updateModificationLabel, 'CHANGE_MODIFICATION_FILTER_SUB')

                    // Toggle modification menu when clicking ...
                    .onClick('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) > label',
                        DX_app.picker.search.toggleModificationMenu, 'TOGGLE_MODIFICATION_FILTER_SUB');

                // Trigger clicks to initiate the labels of Modification and Meta
                DexV2('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem')
                    .filter('.x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)')
                    .trigger('click');
                DexV2('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem')
                    .filter('.x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) input[type=radio]:checked')
                    .trigger('click');

                // Set width of the side panel
                DexV2('.modal-imagepicker #JahiaGxtManagerLeftTree').nodes[0].style.setProperty('left', '-' + DX_app.picker.data.explorer.width + 'px', 'important');

                // Set position of display results
                DexV2('.modal-imagepicker #JahiaGxtManagerTobTable').nodes[0].style.setProperty('left', '0px', 'important');
                DexV2('.modal-imagepicker #JahiaGxtManagerTobTable').nodes[0].style.setProperty('width', '100%', 'important');

            },
            /**
             * Callback executed when the search is opened
             */
            open: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: SEARCH ::: OPEN');

                var searchTabAvailable;
                if (DexV2.getCached('body').getAttribute('data-INDIGO-SUB-PICKER') == 'open') {
                    searchTabAvailable = DexV2('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem').exists();

                    if (searchTabAvailable) {
                        DX_app.picker.search.setUpSubScreen();
                    } else {
                        DexV2('body > #JahiaGxtContentPickerWindow').onOpen('#CRTsearchTabItem', DX_app.picker.search.setUpSubScreen);
                    }
                } else {
                    // OPEN SEARCH PANEL
                    searchTabAvailable = DexV2.id('CRTsearchTabItem').exists();

                    if (searchTabAvailable) {
                        DX_app.picker.search.setUpScreen();
                    } else {
                        DexV2.tag('body').onOpen('#CRTsearchTabItem', DX_app.picker.search.setUpScreen);
                    }
                }
            },
            /**
             * Callback executed when user clicks on Picker > Search > Publication|Modification|Creation
             */
            toggleModificationMenu: function () {
                var menu = DexV2.node(this).parent();
                menu.toggleClass('indigo-show-menu');
            },
            /**
             * Callback executed when the user changes the filter by Publication|Modification|Creation
             *  - Updates the text displayed in the combo according to what has been selected
             */
            updateModificationLabel: function () {
                var dateType = DexV2.node(this).filter('label').getHTML();
                var label = DX_app.dictionary('dateType').replace('%n%', dateType);

                DexV2.node(this).closest('.x-form-item')
                    .removeClass('indigo-show-menu')
                    .setAttribute('data-indigo-modification-label', label);
            },
            /**
             * Callback executed when the user clicks on the meta drop down
             *  - Changes the text displayed in the combo according to selection
             */
            updateMetaLabel: function () {
                var checkboxes = DexV2.node(this).filter('.x-form-check-wrap:not(.x-hide-display) input[type=\'checkbox\']');
                var selMeta = [];
                var checkboxCount = checkboxes.nodes.length;
                var metaMenuLabel;

                checkboxes.each(function () {
                    var checkboxLabel = this.nextSibling.innerHTML;

                    if (this.checked == true) {
                        // Its checked, so add to string ...
                        selMeta.push(checkboxLabel);
                    }
                });

                if (selMeta.length == checkboxCount) {
                    // ALL meta data
                    metaMenuLabel = DX_app.dictionary('allMetadata');
                } else if (selMeta.length == 0) {
                    metaMenuLabel = DX_app.dictionary('ignoreMetadata');
                } else {
                    metaMenuLabel = DX_app.dictionary('metaLabel').replace('%n%', selMeta.join(', '));
                }

                DexV2.node(this).setAttribute('data-indigo-meta-label', metaMenuLabel);
            },
            /**
             * Callback executed when the search is closed
             */
            close: function () {
                DX_app.dev.log('::: APP ::: PICKER ::: SEARCH ::: CLOSE');

                // CLOSE SEARCH PANEL
                DexV2.getCached('body').setAttribute('data-INDIGO-PICKER-SEARCH', '');

                // Hide the search panel
                DexV2('body > #JahiaGxtContentPickerWindow').removeClass('search-panel-opened');

                DexV2('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem').addClass('x-hide-display');

                // Display the BROWSE panels
                DexV2('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)')
                    .removeClass('x-hide-display');

                // Hide the search panel
                DexV2.id(DX_app.picker.data.ID).removeClass('search-panel-opened');

                DexV2.id('CRTsearchTabItem').addClass('x-hide-display');

                // Display the BROWSE panels
                DexV2('#' + DX_app.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)')
                    .removeClass('x-hide-display');

                DX_app.picker.repositionSidePanel();
            },
            /**
             * DELETE - ?
             */
            onContext: function (e) {
                DX_app.dev.log('::: APP ::: PICKER ::: SEARCH ::: ONCONTEXT');
                // Open Context Menu when clicking "More" button.
                DexV2.node(e.target).trigger('contextmenu', e.pageX, e.pageY);
            }
        }
    },
    imagePreview: {
        /**
         * Callback executed when an image preview is opened from within a picker
         */
        onOpen: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: IMAGEPREVIEW ::: ONOPEN');
            DexV2.getCached('body').setAttribute('data-INDIGO-IMAGE-PREVIEW', 'open');
        },
        /**
         * Callback executed when an image preview is closed from within a picker
         */
        onClose: function () {
            DX_app.dev.log('::: APP ::: PICKER ::: IMAGEPREVIEW ::: ONCLOSE');
            DexV2.getCached('body').setAttribute('data-INDIGO-IMAGE-PREVIEW', '');
            var sidePanel = DexV2.id('JahiaGxtSidePanelTabs').nodes[0];
            sidePanel.style.setProperty('width', '360px', 'important');
        }
    },
    engine: {
        data: {
            returnToEditEngine: false,
            open: false
        },
        /**
         * Callback executed when the language has been changed in Edit Mode
         *  - This function resizes the width of the language input field to the size of the text so that the arrow is displayed just after the text
         */
        methods: {
            resizeInput: function (selector) {
                var inputHolder = DexV2(selector),
                    input = DexV2(selector).filter('input');

                if (input.nodes[0]) {
                    var inputValue = input.nodes[0].value;

                    var wideChars = 'ABCDEFGHJKLMNOPQRSTUVWXYZ';
                    var mediumChars = 'abcdefghkmnopqrstuvwxyzI';
                    var slimChars = '()ijl ';

                    var textWidth = function (inputValue) {
                        var returnWidth = 0;

                        for (var charIndex in inputValue) {
                            var isWide = (wideChars.indexOf(inputValue[charIndex]) > -1) ? 10 : 0;
                            var isMedium = (mediumChars.indexOf(inputValue[charIndex]) > -1) ? 7 : 0;
                            var isSlim = (slimChars.indexOf(inputValue[charIndex]) > -1) ? 5 : 0;
                            var addWidth = (isWide + isMedium + isSlim);


                            returnWidth = returnWidth + (addWidth || 10);
                        }

                        return returnWidth;

                    }(inputValue);

                    inputHolder.nodes[0].style.setProperty('width', ((textWidth + 15) + 'px'), 'important');
                }

            }
        },
        resizeLanguageInput: function() {
            app.edit.methods.resizeInput('.toolbar-itemsgroup-languageswitcher');

            DexV2.getCached('body').onAttribute('body', 'data-main-node-displayname', function () {
                app.edit.methods.resizeInput('.toolbar-itemsgroup-languageswitcher');
            }, 'RESIZE_LANGUAGE_INPUT');

        },
            /**
         * Callback executed when the Edit Engine is opened
         */
        onOpen: function () {
            DX_app.dev.log('::: APP ::: ENGINE ::: ONOPEN');

            // Get close button
            var closeButton = DexV2.node(this).filter('.button-cancel');

            // Push State
            DX_app.nav.pushState(closeButton);

            // Register Edit Engine as Opened and set locked flag to off
            DexV2.getCached('body').setAttribute('data-INDIGO-EDIT-ENGINE', 'open');

            // Check if we need to create our own toggle switch when a fieldset header is loaded
            DexV2('body').onOpen('.x-fieldset-header', function () {
                var fieldsetInput = DexV2.node(this).filter('input');

                // This fieldset HAS a toggle, so replace with our own ...
                if (fieldsetInput.nodes.length > 0) {
                    this.classList.add('contains-indigo-switch');
                    this.parentNode.classList.add('fieldset-contains-indigo-switch');

                    var switchHolder = document.createElement('div');
                    var switchRail = document.createElement('div');
                    var switchShuttle = document.createElement('div');

                    switchHolder.classList.add('indigo-switch');
                    switchRail.classList.add('indigo-switch--rail');
                    switchShuttle.classList.add('indigo-switch--shuttle');

                    switchHolder.appendChild(switchRail);
                    switchHolder.appendChild(switchRail);
                    switchHolder.appendChild(switchShuttle);

                    this.insertBefore(switchHolder, this.children[0]);
                }
            }, 'EDIT-ENGINE-INDIGO-SWITCH-CREATOR');

            // Trigger click the GWT checkbox when the user clicks out toggle switch
            DexV2('body').onClick('.x-fieldset-header .indigo-switch', function () {
                var checkbox = DexV2.node(this).parent().filter('input');
                checkbox.trigger('click');
            }, 'EDIT-ENGINE-INDIGO-SWITCH-LISTENER');
        },
        /**
         * Callback executed when the Edit Engine is closed
         */
        onClose: function () {
            DX_app.dev.log('::: APP ::: ENGINE ::: ONCLOSE');

            // Get close button
            var closeButton = DexV2.node(this).filter('.button-cancel');

            // Remove state
            DX_app.nav.pullState(closeButton);

            DX_app.iframe.clearSelection();
            DexV2.getCached('body').setAttribute('data-INDIGO-EDIT-ENGINE', '');

            // If there is a picker open we need to reposition the page elements ( ie. Search button, refresh button )
            if (DexV2.getCached('body').getAttribute('data-indigo-picker') == 'open') {
                var splitVBar = DexV2.class('x-vsplitbar');
                var splitVBarLeft = parseInt(splitVBar.nodes[0].style.left);
                DX_app.picker.repositionSidePanel(splitVBarLeft);
            }

            if(DX_app.data.HTTP.app == 'manager'){
                DexV2('.toolbar-item-' + DX_app.picker.data[DX_app.picker.data.currentDisplayType]).trigger('click');
            }
        },
        /**
         * Callback executed when the History tab is opened in the Edit Engine
         */
        onOpenHistory: function () {
            // User has clicked on the close details panel
            DexV2.id('JahiaGxtEditEnginePanel-history').onClick('.x-panel:nth-child(2) .x-panel-toolbar', function () {
                // Remove the  flag that displays the details panel
                DexV2.id('JahiaGxtEditEnginePanel-history').setAttribute('data-indigo-details', '');
                DexV2.getCached('body').setAttribute('data-indigo-history-display', '');
            });

            // Executes when results are loaded into the list
            DexV2.id('JahiaGxtEditEnginePanel-history').onGroupOpen('.x-grid3-row', function () {
                var previousButton = DexV2.id('JahiaGxtEditEnginePanel-history').filter('.x-panel-bbar .x-toolbar-left .x-toolbar-cell:nth-child(2) > table');
                var nextButton = DexV2.id('JahiaGxtEditEnginePanel-history').filter('.x-panel-bbar .x-toolbar-left .x-toolbar-cell:nth-child(8) > table');

                // Look at the previous and next buttons to determine if there is more than one page of results
                if (previousButton.hasClass('x-item-disabled') &&
                    nextButton.hasClass('x-item-disabled')) {
                    // Only one page, so hide pager
                    DexV2.id('JahiaGxtEditEnginePanel-history').setAttribute('indigo-results-multiple-pages', 'false');
                } else {
                    // More than one page, so show pager
                    DexV2.id('JahiaGxtEditEnginePanel-history').setAttribute('indigo-results-multiple-pages', 'true');
                }

                // Add info and delete button to each row
                var rows = DexV2.id('JahiaGxtEditEnginePanel-history').filter('.x-grid3-row');

                // Build the menu
                var actionMenu = document.createElement('menu');
                var infoButton = document.createElement('button');

                // Add classes to menu elements
                actionMenu.classList.add('action-menu');
                infoButton.classList.add('info-button');

                // Add buttons to the menu
                actionMenu.appendChild(infoButton);

                // Duplicate and add the menu to each row
                rows.each(function () {
                    var clonedActionMenu = actionMenu.cloneNode(true);

                    // This listener is sometimes called more than once, so check if the row has already had action menu added before adding ...
                    if (!DexV2.node(this).hasClass('indigo-contains-actions-menu')) {
                        DexV2.node(this).addClass('indigo-contains-actions-menu').append(clonedActionMenu);
                    }
                });

                // Flag that there are results ...
                DexV2.id('JahiaGxtEditEnginePanel-history').setAttribute('indigo-results', 'true');
            }, 'HISTORY-RESULTS-LIST');

            // Excutes when there are no results ...
            DexV2.id('JahiaGxtEditEnginePanel-history').onOpen('.x-grid-empty', function () {
                // Flag that there are no results
                DexV2.id('JahiaGxtEditEnginePanel-history').setAttribute('indigo-results', 'false');
            }, 'HISTORY-NO-RESULTS-LIST');

            // User has clicked on the info button
            DexV2.id('JahiaGxtEditEnginePanel-history').onClick('.info-button', function () {
                // Open the details panel by flagging the attribute
                DexV2.id('JahiaGxtEditEnginePanel-history').setAttribute('data-indigo-details', 'open');
                DexV2.getCached('body').setAttribute('data-indigo-history-display', 'true');
            }, 'HISTORY-DETAILS-ENTRY');
        },
        /**
         * Callback executed when the Workflow tab is opened in the Edit Engine
         */
        onOpenWorkflow: function () {
            // Used to prefix the labels with the name of the Selected workflows ...
            DexV2.node(this).onClick('.x-grid3-row', function () {
                var label = DexV2.node(this).filter('.x-grid3-col-displayName').getHTML();
                var localisedLabel = DX_app.dictionary('workflowType').replace('%n%', label);
                var localisedChooseLabel = DX_app.dictionary('chooseWorkflowType').replace('%n%', label);

                // Update labels
                DexV2('#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) > .x-panel form > div:nth-child(1)')
                    .setAttribute('data-indigo-workflow-type', localisedLabel);
                DexV2('#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) > .x-panel .x-form-field-wrap')
                    .setAttribute('data-indigo-workflow-type', localisedChooseLabel);

            }, 'CHANGE_WORKFLOW_TYPE');

            // Init by clicking first workflow item
            DexV2.node(this).onceOpen('.x-grid3-row', function () {
                DexV2.node(this).trigger('click');
            });
        },
        /**
         * Callback executed when the Condition Editor is closed in the Edit Engine Visibility tab
         */
        closeConditionEditor: function () {
            DexV2('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3)').removeClass('indigo-show');
            DexV2.getCached('body').setAttribute('data-indigo-editing-condition', false);
        },
        /**
         * Adds a cancel and OK button to the create condition modal
         */
        createConditionMenu: function (newMenu) {
            DexV2('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3) .x-panel-footer').setHTML('').append(newMenu);
        },
        /**
         * Callback executed when the user clicks on the Edit Condition button
         */
        editCondition: function () {
            DexV2('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3)').addClass('indigo-show');

            DexV2.getCached('body').setAttribute('data-indigo-add-visibility-condition', '').setAttribute('data-indigo-editing-condition', true);
            // Create menu ...
            var newMenu = document.createElement('menu');
            var doneButton = document.createElement('button');
            var doneButtonLabel = document.createTextNode(DX_app.dictionary('save'));

            DexV2.node(doneButton).addClass('done-with-condition');

            doneButton.appendChild(doneButtonLabel);
            newMenu.appendChild(doneButton);

            if (DexV2('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3) .x-panel-footer').exists()) {
                DX_app.engine.createConditionMenu(newMenu);
            } else {
                DexV2.id('JahiaGxtEditEnginePanel-visibility').onceOpen('.x-component:nth-child(3) .x-panel-footer', function () {
                    DX_app.engine.createConditionMenu(newMenu);
                });
            }
        },
        /**
         * Callback executed when the user clicks on a menu item in the Add New Condition drop down
         */
        addCondition: function () {
            DexV2('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3)').addClass('indigo-show');
            DexV2.getCached('body').setAttribute('data-indigo-editing-condition', 'new');

            var newMenu = document.createElement('menu');
            var saveButton = document.createElement('button');
            var saveButtonLabel = document.createTextNode(DX_app.dictionary('create'));
            var closeButton = document.createElement('button');
            var closeButtonLabel = document.createTextNode(DX_app.dictionary('cancel'));

            DexV2.node(saveButton).addClass('save-new-condition');
            DexV2.node(closeButton).addClass('cancel-new-condition');

            closeButton.appendChild(closeButtonLabel);
            saveButton.appendChild(saveButtonLabel);

            newMenu.appendChild(closeButton);
            newMenu.appendChild(saveButton);

            DexV2('body').oneClick('#JahiaGxtEditEnginePanel-visibility .cancel-new-condition', function () {
                // DEV NOTE ::: Get rid of this timeout
                setTimeout(function () {
                    DexV2.id('JahiaGxtEditEnginePanel-visibility').filter('.x-grid3-row.x-grid3-row-selected .x-grid3-col-remove > table .x-btn-small').trigger('click');
                }, 5);
            });

            if (DexV2('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3) .x-panel-footer').exists()) {
                DX_app.engine.createConditionMenu(newMenu);
            } else {
                DexV2.id('JahiaGxtEditEnginePanel-visibility').onceOpen('.x-component:nth-child(3) .x-panel-footer', function () {
                    DX_app.engine.createConditionMenu(newMenu);
                });
            }
        },
        /**
         * Callback executed when the user clicks on the Add new Condition button
         */
        openConditionsMenu: function () {
            DexV2('body').onceOpen('.x-combo-list', function () {
                DexV2('body').setAttribute('data-indigo-add-visibility-condition', 'new');
                DexV2('body').oneMouseDown('.x-combo-list-item', function () {
                    // DEV NOTE ::: Get rid of this timeout
                    setTimeout(function () {
                        DexV2('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(1) > .x-component:nth-child(2) > table > tbody > tr > td:nth-child(5) > table').trigger('click');
                    }, 5);
                });
            });

            DexV2('body').onceClose('.x-combo-list', function () {
                // Remove class that modifies the set up for this context menu
                DexV2('body').setAttribute('data-indigo-add-visibility-condition', '');
            });
        }
    },
    workflow: {
        data: {
            opened: false
        },
        dashboard: {
            /**
             * Callback executed when the Workflow Dashboard is opened
             */
            onOpen: function () {
                DX_app.dev.log('::: APP ::: WORKFLOW ::: DASHBOARD ::: ONOPEN');
                DexV2('.workflow-dashboard-engine .x-tool-maximize').trigger('click');
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
        /**
         * Callback executed when the iframe in Edit Mode changes URL
         *  - Listens to the SRC attribute on .window-iframe
         * @param attrKey
         * @param attrValue
         */
        onChangeSRC: function (attrKey, attrValue) {
            DX_app.dev.log('::: APP ::: IFRAME ::: ONCHANGESRC [src=\'' + attrValue + '\' ::: currentApp=\'' + DX_app.data.currentApp + '\']');

            DX_app.iframe.data.previousUrl = DX_app.iframe.data.currentUrl;
            DX_app.iframe.data.currentUrl = attrValue;

            if (DX_app.data.currentApp == 'edit') {
                // TEMP BLIND
                DX_app.edit.sidepanel.onNewChannel();

                // A new page has been loaded in the Edit Window Iframe
                // If it is NOT a settings page then we need to save the URL so we can use
                // it as a return URL when coming back from Settings ( only if not page can be found )
                if (!DX_app.edit.settings.data.opened) {
                    // Not a settings page so we can save the URL
                    // Note that we have modify the URL because the iframe is slightly different from the Edit Mode URL
                    DX_app.edit.data.returnURL = attrValue.replace('/cms/editframe/', '/cms/edit/');
                }

                var elements = {
                    title: document.getElementsByClassName('x-current-page-path')[0],
                    previewButton: document.getElementsByClassName('edit-menu-view')[0],
                    moreInfo: document.getElementsByClassName('edit-menu-edit')[0]
                };

                if (elements.title &&
                    elements.title.style) {
                    elements.title.style.opacity = 0;

                }

                DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
                DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

                if (elements.previewButton &&
                    elements.previewButton.style) {
                    elements.previewButton.style.opacity = 0;
                }

                if (elements.moreInfo &&
                    elements.moreInfo.style) {
                    elements.moreInfo.style.opacity = 0;
                }

            }
        },
        /**
         * Callback executed when the iframe changes SRC
         *  - Listens to the data-main-node-displayname attribute on the body tag
         * @param attrKey
         * @param attrValue
         * @returns {boolean}
         */
        onChange: function (attrKey, attrValue) {
            if(DX_app.data.currentApp == 'edit'){
                DexV2.getCached('body').setAttribute("data-edit-mode-status", "initialised");
            }

            if (DX_app.iframe.data.displayName == attrValue || DX_app.data.currentApp == 'studio') {
                return false;
            }

            DX_app.dev.log('::: APP ::: IFRAME ::: ONCHANGE: ' + DX_app.data.currentApp);

            DX_app.iframe.data.displayName = attrValue;

            switch (DX_app.data.currentApp) {
                case 'edit':
                    // Need to update the header bar
                    DX_app.edit.topbar.build();
                    if (DX_app.edit.sidepanel.isOpen()) {
                        DX_app.iframe.disableClicks();
                    }
                    break;
                case 'contribute':
                    // Need to update the header bar
                    DX_app.contribute.topbar.build();
                    break;
                case 'dashboard':
                    DX_app.dashboard.onChange();
                    break;
            }
        },
        /**
         * Callback executes when the user selects a node in the Edit Mode window
         * @param attrKey
         * @param attrValue
         */
        onSelect: function (attrKey, attrValue) {
            DX_app.dev.log('::: APP ::: IFRAME ::: ONSELECT [attrValue=\'' + attrValue + '\']');

            var count = parseInt(attrValue);
            // Refresh the title of the page accordingly
            switch (DX_app.data.currentApp) {
                case 'edit':
                    DX_app.iframe.data.selectionCount = count;
                    // Need to update the header bar
                    DX_app.edit.topbar.build();
                    if (DX_app.edit.sidepanel.isOpen()) {
                        DX_app.iframe.disableClicks();
                    }
                    break;
                case 'contribute':
                    DX_app.iframe.data.selectionCount = count;
                    // Need to update the header bar
                    DX_app.contribute.topbar.build();
                    break;
            }
        },
        /**
         * Callback executes when the selected node(s) in the Edit Mode window clear
         */
        clearSelection: function () {
            DX_app.dev.log('::: APP ::: IFRAME ::: CLEARSELECTION');

            DexV2.class('window-iframe').trigger('click');
        },
        /**
         * Disables clicks in the iframe in Edit Mode
         *  - This is used when the side panel is opened. That way a user can click outside the side panel to close it without selecting nodes in the Edit Mode iframe window
         */
        disableClicks: function () {
            DX_app.dev.log('::: APP ::: IFRAME ::: DISABLECLICKS');

            if (DexV2.getCached('body').getAttribute('data-indigo-gwt-side-panel') == 'open' &&
                DexV2.getCached('body').getAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL') == 'yes' &&
                DexV2.getCached('body').getAttribute('data-sitesettings') == 'false' &&
                DexV2.getCached('body').getAttribute('data-indigo-sidepanel-pinned') != 'true') {

                // SAVE the curent style properties of the iframes body tag so we can revert to it once the side panel is closed.
                var iframeBody = DexV2.iframe('.window-iframe').filter('body');

                iframeBody.nodes[0].style.pointerEvents = 'none';
            }
        }
    },
    /**
     * Everything here concerns the Admin Mode
     */
    admin: {
        config: {
            chrome: true
        },
        data: {
            firstLoadSettingsType: null
        },
        sidepanel: {
            /**
             * Toggle the display of the System Site Settings Menu
             * @param e
             */
            toggleSiteSettingsMenu: function(e){
                var clickedElement = DexV2.node(e.target),
                    toggleMenu = clickedElement.hasClass('x-panel');

                if(toggleMenu){
                    clickedElement.toggleClass('open-sub-menu')
                }
            },
            row: {
                /**
                 * Callback executes when user clicks on a navigation element in the side panel
                 */
                onClick: function () {
                    // Because the two menus are simultaneously visible now, we need to deselect the previously selected row when selecting a page from the other menu group.
                    DexV2('.x-viewport-adminmode .x-grid3 .x-grid3-row.x-grid3-row-selected').removeClass('x-grid3-row-selected');
                    DexV2.node(this).addClass('x-grid3-row-selected');
                }
            }
        },
        /**
         * Callback executes when switching to Admin Mode
         */
        onOpen: function () {
            var mouse = DX_mouse;
            DX_app.dev.log('::: APP ::: ADMIN ::: OPENED');

            // Reset incase user is coming in from another app
            DX_app.admin.data.firstLoadSettingsType = null;

            DX_app.edit.sidepanel.buildSplitter();
            DX_app.edit.sidepanel.resizeSidePanel();

            DexV2.getCached('body').setAttribute('data-indigo-styled-combos', 'true');

            // Set attributes to be used by CSS
            DexV2.getCached('body')
                .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'no')
                .setAttribute('data-INDIGO-GWT-SIDE-PANEL', 'open');
        },
        /**
         * Callback executes when leaving the Admin Mode
         */
        onClose: function () {}
    },
    common: {
        trees: {
            /**
             * Callback executed when the user clicks on the arrow to expand / collpase child nodes
             */
            onToggleChildNodes: function () {
                DexV2.node(this).closest('.x-grid3-row').toggleClass('indigo-opened');
            }
        },
        /**
         * Resize the width of the site selector when site is changed
         */
        resizeSiteSelector: function () {
            var languageInput = DexV2('.edit-menu-sites input');
            if (languageInput.nodes[0]) {
                var languageInputValue = DexV2('.edit-menu-sites input').nodes[0].value;
                var wideChars = 'ABCDEFGHJKLMNOPQRSTUVWXYZ ';
                var mediumChars = 'abcdefghkmnopqrstuvwxyzI';
                var slimChars = 'ijl';

                var textWidth = function (languageInputValue) {
                    var returnWidth = 0;

                    for (var charIndex in languageInputValue) {
                        var isWide = (wideChars.indexOf(languageInputValue[charIndex]) > -1) ? 10 : 0;
                        var isMedium = (mediumChars.indexOf(languageInputValue[charIndex]) > -1) ? 9 : 0;
                        var isSlim = (slimChars.indexOf(languageInputValue[charIndex]) > -1) ? 3 : 0;
                        var addWidth = (isWide + isMedium + isSlim);
                        returnWidth = returnWidth + (addWidth || 10);

                    }

                    return returnWidth;
                }(languageInputValue);

                DexV2('.edit-menu-sites').nodes[0].style.setProperty('width', ((textWidth + 15) + 'px'), 'important');
            }

        }
    },
    /**
     * Everything here concerns Modals
     */
    modals: {
        /**
         * Callback executed when a modal opens
         */
        onOpen: function(){
            // Get close button
            var closeButton = DexV2.node(this).filter('.x-tool-close');

            // Push State
            DX_app.nav.pushState(closeButton);

            // Create Modal Mask
            // Can no longer use GWT modal with peace of mind, so insert our own one in the popup.
            // It is fairly universal except for pickers, background jobs & workflows which hide it via CSS
            var modalMask = document.createElement('div');
            modalMask.classList.add('indigo-modal-mask');
            DexV2.node(this).append(modalMask);
        },
        /**
         * Callback executed when a modal closes
         */
        onClose: function () {
            // Get close button
            var closeButton = DexV2.node(this).filter('.x-tool-close');
            // Remove state
            // Update the Browser History ( need to see why this has been done, it was regarding a bug fix )
            DX_app.nav.pullState(closeButton);
        }
    },
    /**
     * Everything here concerns Dialogs
     */
    dialog: {
        /**
         * Callback executed when the dialog is opened
         */
        onOpen: function(){
            // Scrolls the Dialog to the top when it opens
            var dialog = this;

            setTimeout(function(){
                dialog.scrollTop = 0
            }, 50)
        }
    },
    /**
     * Everything here concerns Pickers
     */
    pickers: {
        /**
         * Everything here concerns User Pickers
         */
        users: {
            data: {
                closeInterval: null
            },
            /**
             * Callback executed when the value of text input changes
             *  - By looking at its value we can hide / show the clear button accordingly
             */
            onInput: function () {
                // If search text field is empty, remove the clear button
                if (this.value == '') {
                    DexV2.class('indigo-clear-button').addClass('indigo-empty-field');
                } else {
                    DexV2.class('indigo-clear-button').removeClass('indigo-empty-field');
                }
            },
            /**
             * Callback executed when the text input receives focus
             *  - By looking at its value we can hide / show the clear button accordingly
             */
            onInputFocus: function () {
                // Expand the search field
                DexV2.class('indigo-search-component').addClass('indigo-show');

                // If the search input value is empty add the clear button
                if (this.value == '') {
                    DexV2.class('indigo-clear-button').addClass('indigo-empty-field');
                }
            },
            /**
             * Callback executed when the user search field loses focus
             */
            onInputBlur: function () {
                // Close the search panel ( if the search input field is empty )
                var searchString = this.value;

                if (searchString == '') {
                    // The value of the search input is empty, so close.
                    // However, do not close immediately - because the user may actually be clicking the clear button, so...
                    // Use an interval timer that closes the search panel after a split second. If the user clicks on the clear button
                    // before this time has passed , we can cancel the interval and therefore keep the panel open

                    DX_app.pickers.users.data.closeInterval = setInterval(function () {
                        DexV2.class('indigo-search-component').removeClass('indigo-show');

                        clearInterval(DX_app.pickers.users.data.closeInterval);
                    }, 150);
                }
            },
            /**
             * Callback executed when the user clicks the clear button
             */
            clearSearch: function () {
                // Clear the timer (if any ) that hides the search panel
                clearInterval(DX_app.pickers.users.data.closeInterval);

                // Get elements
                var searchInput = DexV2.id('JahiaGxtUserGroupSelect').filter('.indigo-search-input').nodes[0];
                var searchButton = DexV2.id('JahiaGxtUserGroupSelect').filter('.indigo-search-button > table');

                // Set classes for display
                DexV2.class('indigo-clear-button').addClass('indigo-empty-field');
                DexV2.class('indigo-search-component').addClass('indigo-show');

                // Set the value of the text field to empty string
                searchInput.value = '';

                // Trigger search on empty field to reset the results ( need to wait a split second )
                setTimeout(function () {
                    searchButton.trigger('click');
                    searchInput.focus();
                }, 100);
            },
            /**
             * Callback executed when the Select Users modal opens
             */
            onOpen: function () {
                DexV2.node(this).filter('.x-panel-tbar .x-toolbar-left > table').addClass('indigo-search-component');

                DexV2.class('indigo-search-component').filter('.x-toolbar-cell:nth-child(1)').addClass('indigo-clear-button');
                DexV2.class('indigo-search-component').filter('.x-toolbar-cell:nth-child(2) input').addClass('indigo-search-input');
                DexV2.class('indigo-search-component').filter('.x-toolbar-cell:nth-child(3)').addClass('indigo-search-button');

                DexV2.class('indigo-search-component')
                    .onInput('.indigo-search-input', DX_app.pickers.users.onInput, 'INDIGO-SEARCH-COMPONENT')
                    .onClick('.indigo-clear-button', DX_app.pickers.users.clearSearch, 'INDIGO-SEARCH-COMPONENT')
                    .onFocus('.indigo-search-input', DX_app.pickers.users.onInputFocus, 'INDIGO-SEARCH-COMPONENT')
                    .onBlur('.indigo-search-input', DX_app.pickers.users.onInputBlur, 'INDIGO-SEARCH-COMPONENT');

                // Remove the clear button on init
                DexV2.class('indigo-clear-button').addClass('indigo-empty-field');

                DexV2.id('JahiaGxtUserGroupSelect')
                    .onOpen('.x-grid-empty', function () {
                        DexV2.id('JahiaGxtUserGroupSelect').addClass('indigo-no-results');
                    }, 'INDIGO-SEARCH-COMPONENT')
                    .onClose('.x-grid-empty', function () {
                        DexV2.id('JahiaGxtUserGroupSelect').removeClass('indigo-no-results');
                    }, 'INDIGO-SEARCH-COMPONENT');
            }
        }
    },
    /**
     * Everything here concerns the Remote Mode
     */
    remote: {
        config: {
            chrome: true
        },
        // Data
        data: {
            history: {
                settingspage: null,
                editpage: null
            },
            search: {
                refreshButtonClasslist: null,
                emptyGridStyle: null,
                status: null
            },
            returnURL: null
        },
        /**
         * Callback executed when switching to the Remote Mode
         */
        onOpen: function () {
            DX_app.dev.log('::: APP ::: REMOTE ::: ONOPEN');

            // Add Background mask used for modals
            if(!DexV2.class('background-mask').exists()){
              var backGroundMask = document.createElement('div');

              backGroundMask.classList.add('background-mask');
              DexV2.getCached('body').append(backGroundMask);

            }

            // Add Publication Status Bar
            if (!DexV2.class('publication-status').exists()) {
                // Create div for publication status of page / slected element because currently it is a pseudo element and we cant reposition when in pinned mode
                var publicationStatus = document.createElement('div'),
                    status = (DX_app.iframe.data.publication && DX_app.iframe.data.publication.status) ? DX_app.iframe.data.publication.status : 'unknown';

                publicationStatus.classList.add('publication-status');
                publicationStatus.setAttribute('data-publication-status', status);

                DexV2.getCached('body').prepend(publicationStatus);
            }


            DX_app.data.currentApp = 'edit';
            DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
            DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

            DexV2('.window-side-panel > .x-panel-bwrap > div:nth-child(2).x-panel-footer').addClass('side-panel-pin');
            DexV2.getCached('body').setAttribute('data-indigo-styled-combos', 'true');
            DexV2.getCached('body').setAttribute('data-indigo-sidepanel-pinned', 'false');
            DX_app.edit.sidepanel.data.pinned = false;
            DX_app.edit.data.returnURL = window.location.pathname;

            // Reset History
            DX_app.edit.history.reset();

            DX_app.edit.topbar.build();

            // Set attributes to be used by CSS
            DexV2.getCached('body')
                .setAttribute('data-edit-window-style', 'default')
                .setAttribute('data-INDIGO-GWT-SIDE-PANEL', '')
                .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'yes');

            // Setup the alternative channels system
            DX_app.edit.sidepanel.initChannels();

            DX_app.edit.resizeLanguageInput();

            if (DexV2.id('JahiaGxtSidePanelTabs').exists()) {
                DexV2.id('JahiaGxtSidePanelTabs').nodes[0].style.setProperty('width', '360px', 'important');
                DexV2.getCached('body').setAttribute('data-indigo-gwt-side-panel', '');
            }
            DexV2('.mainmodule > div:nth-child(2) > div:not(.x-abs-layout-container)').nodes[0].setAttribute('style', 'height:100vh !important; transform: translateY(-109px) !important;');
        },
        /**
         * Callback executed when leaving the Remote Mode
         */
        onClose: function () {

        },
    },
    /**
     * Everything here concerns the Edit Mode
     */
    edit: {
        config: {
            chrome: true
        },
        // Data
        data: {
            history: {
                settingspage: null,
                editpage: null
            },
            search: {
                refreshButtonClasslist: null,
                emptyGridStyle: null,
                status: null
            },
            returnURL: null
        },
        /**
         * Callback executed when the site is changed in the Edit Mode
         */
        onNewSite: function(){
            DX_app.dev.log("app ::: edit ::: onNewSite (disabled the dodgy code )");

            // Flag that a new site has been loaded
            DX_app.edit.sidepanel.data.newSite = true;

            DexV2.getCached('body').setAttribute("data-edit-mode-status", "loading");

            DX_app.common.resizeSiteSelector();
        },
        /**
         * Add a placeholder the input filter and focus it
         */
        addPlaceholderToContentFilter: function () {
            DexV2('.content-type-window .x-form-field-wrap input').setAttribute('placeholder', DX_app.dictionary('filterContent'));
            // Firefox has bug which doesnt always set focus on text input, wait a split second before settings focus
            var that = this;
            setTimeout(function () {
                that.focus();
            }, 100);
        },
        /**
        * Toggle the tooltip for page info when the user hovers the page title
        */
        togglePageInfoToolTip: function(){
          DexV2.class("publication-status-tooltip").toggleClass("indigo-show");
        },
        /**
         * Resize the width of the language selector when site is changed
         */
        resizeLanguageInput: function () {
            var languageInput = DexV2('.toolbar-itemsgroup-languageswitcher input');

            if (languageInput.nodes[0]) {
                var languageInputValue = DexV2('.toolbar-itemsgroup-languageswitcher input').nodes[0].value;

                var wideChars = 'ABCDEFGHJKLMNOPQRSTUVWXYZ';
                var mediumChars = 'abcdefghkmnopqrstuvwxyzI';
                var slimChars = 'ijl';

                var textWidth = function (languageInputValue) {
                    var returnWidth = 0;

                    for (var charIndex in languageInputValue) {
                        var isWide = (wideChars.indexOf(languageInputValue[charIndex]) > -1) ? 10 : 0;
                        var isMedium = (mediumChars.indexOf(languageInputValue[charIndex]) > -1) ? 9 : 0;
                        var isSlim = (slimChars.indexOf(languageInputValue[charIndex]) > -1) ? 3 : 0;
                        var addWidth = (isWide + isMedium + isSlim);

                        returnWidth = returnWidth + (addWidth || 10);
                    }

                    return returnWidth;

                }(languageInputValue);

                DexV2('.toolbar-itemsgroup-languageswitcher').nodes[0].style.setProperty('width', ((textWidth + 15) + 'px'), 'important');
            }
        },
        /**
         * Callback executed when witching to the Edit Mode
         */
        onOpen: function () {
            DX_app.dev.log('::: APP ::: EDIT ::: ONOPEN');

            DexV2.getCached('body').setAttribute("data-edit-mode-status", "loading");

            // Add Background mask used for modals
            if(!DexV2.class('background-mask').exists()){
              var backGroundMask = document.createElement('div');

              backGroundMask.classList.add('background-mask');
              DexV2.getCached('body').append(backGroundMask);

            }

            // Add Publication Status Bar
            if (!DexV2.class('publication-status').exists()) {
                // Create div for publication status of page / selected element because currently it is a pseudo element and we cant reposition when in pinned mode
                var publicationStatus = document.createElement('div'),
                    publicationStatusTooltip = document.createElement('div'),
                    publicationStatusLabel = document.createElement('label'),
                    publicationStatusPath = document.createElement('p'),

                    status = (DX_app.iframe.data.publication && DX_app.iframe.data.publication.status) ? jahia_gwt_messages["label_publication_" + DX_app.iframe.data.publication.status] : 'unknown',
                    path = jahiaGWTParameters[jahiaGWTParameters.lang];

                publicationStatusLabel.setAttribute("data-label", jahia_gwt_messages.label_publication_status + ": ");
                publicationStatusLabel.innerHTML = status;
                publicationStatusLabel.classList.add('publication-status-label');

                publicationStatusPath.setAttribute("data-label", jahia_gwt_messages.label_path + ": ");
                publicationStatusPath.innerHTML = path;
                publicationStatusPath.classList.add('publication-status-path');

                publicationStatusTooltip.classList.add('publication-status-tooltip');
                publicationStatusTooltip.appendChild(publicationStatusLabel);
                publicationStatusTooltip.appendChild(publicationStatusPath);

                publicationStatus.appendChild(publicationStatusTooltip);
                publicationStatus.classList.add('publication-status');
                publicationStatus.setAttribute('data-publication-status', status);
                DexV2.getCached('body').prepend(publicationStatus);
            }

            DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
            DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

            DexV2('.window-side-panel > .x-panel-bwrap > div:nth-child(2).x-panel-footer').addClass('side-panel-pin');

            DexV2.getCached('body').setAttribute('data-indigo-styled-combos', 'true');
            DexV2.getCached('body').setAttribute('data-indigo-sidepanel-pinned', 'false');
            DX_app.edit.sidepanel.data.pinned = false;
            DX_app.edit.data.returnURL = window.location.pathname;

            // Reset History
            DX_app.edit.history.reset();

            DX_app.edit.topbar.build();

            // Set attributes to be used by CSS
            DexV2.getCached('body')
                .setAttribute('data-edit-window-style', 'default')
                .setAttribute('data-INDIGO-GWT-SIDE-PANEL', '')
                .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'yes');

            // Setup the alternative channels system
            DX_app.edit.sidepanel.initChannels();

            DX_app.edit.resizeLanguageInput();

            if (DexV2.id('JahiaGxtSidePanelTabs').exists()) {
                DexV2.id('JahiaGxtSidePanelTabs').nodes[0].style.setProperty('width', '60px', 'important');
                DexV2.getCached('body').setAttribute('data-indigo-gwt-side-panel', '');
            }

            var pinnedPanel = DX_app.storage.get('pinnedPanel');

            if(pinnedPanel){
                DexV2.class('side-panel-pin').trigger('click');
                DexV2('#JahiaGxtSidePanelTabs .x-tab-strip-active').trigger('mousedown').trigger('mouseup');

            }
        },
        /**
         * Callback executes when leaving the Edit Mode
         */
        onClose: function () {
            DexV2.getCached('body').setAttribute("data-edit-mode-status", null);
        },
        /**
         * Callback executed when the User changes page in Edit Mode
         */
        onNav: function () {
            DX_app.dev.log('::: APP ::: EDIT ::: ONNAV');
            if (DX_app.edit.settings.data.opened) {
                // CLicked on a settings page
                DX_app.dev.log(['ONNAV ::: ', this]);
                if (this.classList.contains('unselectable-row')) {
                    DX_app.dev.log('DO NOT REMEMBER THIS PAGE IN HISTORY AS IT IS A FOLDER');
                } else {
                    DX_app.edit.history.add('settingspage', this);
                }
            } else if (DexV2.getCached('body').getAttribute('data-indigo-gwt-panel-tab') == 'JahiaGxtSidePanelTabs__JahiaGxtPagesTab') {
                DX_app.edit.history.add('editpage', this);
            }
        },

        infoBar: {
            data: {
                on: false,
                taskCount: 0
            },
            /**
             * DO LATER
             */
            toggle: function () {
                DX_app.dev.log('::: APP ::: EDIT ::: INFOBAR ::: TOGGLE');
                DX_app.edit.infoBar.data.on = !DX_app.edit.infoBar.data.on;

                DexV2.getCached('body').setAttribute('data-indigo-infoBar', DX_app.edit.infoBar.data.on);
            },
            tasks: {
                data: {
                    classes: null,
                    taskCount: 0,
                    dashboardButtonLabel: null
                },
                /**
                 * Callback executes when tasks are being run
                 */
                onChange: function (attrKey, attrValue) {
                    if (DX_app.edit.infoBar.tasks.data.classes == attrValue) {
                        return false;
                    }

                    DX_app.edit.infoBar.tasks.data.classes = attrValue;

                    DX_app.dev.log('::: APP ::: EDIT ::: INFOBAR ::: TASKS ::: ONCHANGE');

                    var taskButton = DexV2('.' + DX_app.data.currentApp + '-menu-tasks button');
                    if (taskButton.exists()) {
                        var taskCount;
                        var regexp = /\(([^)]+)\)/;
                        var taskString = taskButton.getHTML();
                        var result = taskString.match(regexp);
                        if (result) {
                            taskCount = parseInt(result[1]);
                        } else {
                            taskCount = 0;
                        }

                        var dashboardButtonLabel;
                        var dashboardButton = DexV2.class('menu-edit-menu-workflow');
                        switch (taskCount) {
                            case 0:
                                dashboardButtonLabel = DX_app.dictionary('zeroTasks');
                                break;
                            case 1:
                                dashboardButtonLabel = DX_app.dictionary('singleTask').replace('%n%', taskCount);
                                break;
                            default:
                                dashboardButtonLabel = DX_app.dictionary('multipleTasks').replace('%n%', taskCount);
                                break;
                        }

                        var workflowButtonLabel;
                        if (taskCount > 9) {
                            workflowButtonLabel = '+9';
                        } else {
                            workflowButtonLabel = taskCount;
                        }

                        DexV2('.edit-menu-workflow').setAttribute('data-info-count', workflowButtonLabel);
                        DexV2('.contribute-menu-workflow').setAttribute('data-info-count', workflowButtonLabel);

                        if (dashboardButton.exists()) {
                            dashboardButton.filter('.toolbar-item-workflowdashboard').setHTML(dashboardButtonLabel);
                        }

                        DX_app.edit.infoBar.data.taskCount = taskCount;
                        DX_app.edit.infoBar.data.workflowButtonLabel = workflowButtonLabel;
                        DX_app.edit.infoBar.data.dashboardButtonLabel = dashboardButtonLabel;
                    }
                },
                /**
                 * Add number of tasks to the Dashboard Button, ie: Dashboard (1 task)
                 */
                updateMenuLabel: function () {
                    DexV2.node(this).filter('.toolbar-item-workflowdashboard').setHTML(DX_app.edit.infoBar.data.dashboardButtonLabel);
                }
            },
            jobs: {
                data: {
                    classes: null,
                    jobString: null
                },
                /**
                 * DO LATER
                 */
                onChange: function (attrKey, attrValue) {
                    var workInProgressAdminButton = (DexV2.node(this).hasClass('toolbar-item-workinprogressadmin')) ? 'toolbar-item-workinprogressadmin' : 'toolbar-item-workinprogress';
                    DX_app.edit.infoBar.jobs.data.classes = attrValue;

                    DX_app.dev.log('::: APP ::: EDIT ::: INFOBAR ::: JOBS ::: ONCHANGE');

                    var jobButton = DexV2('.' + workInProgressAdminButton + ' button');
                    if (jobButton.exists()) {
                        var jobStringSplit = jobButton.getHTML().split('<');
                        var jobString = jobStringSplit[0];

                        var activeJob;
                        var jobTooltip;
                        var jobIcon = jobButton.filter('img');
                        if (jobIcon.getAttribute('src').indexOf('workInProgress.png') == -1) {
                            // A job is active
                            activeJob = true;
                            jobTooltip = jobString;
                            DexV2.class(workInProgressAdminButton).setAttribute('job-in-progress', 'true');
                            DexV2('.x-viewport-editmode .action-toolbar .x-toolbar-cell:nth-child(10)').addClass('indigo-job-running');
                        } else {
                            // No Jobs active
                            activeJob = false;
                            jobTooltip = DX_app.dictionary('jobs');

                            DexV2.class(workInProgressAdminButton).setAttribute('job-in-progress', '');

                            DexV2('.x-viewport-editmode .action-toolbar .x-toolbar-cell:nth-child(10)').removeClass('indigo-job-running');
                        }

                        DX_app.edit.infoBar.jobs.data.jobString = jobString;
                        DX_app.edit.infoBar.jobs.data.activeJob = activeJob;

                        var buttonParent = DexV2.class(workInProgressAdminButton);
                        buttonParent.setAttribute('data-indigo-label', jobTooltip);
                    }
                }
            },
            publicationStatus: {
                /**
                 * DO LATER
                 */
                onChange: function () {
                    DX_app.dev.log('::: APP ::: EDIT ::: INFOBAR ::: PUVLICATIONSTATUS ::: ONCHANGE');
                }
            }
        },

        // Controls
        history: {
            data: {},
            /**
             * DO LATER
             */
            add: function (type, node) {
                DX_app.dev.log('::: APP ::: EDIT ::: HISTORY ::: ADD');
                DX_app.edit.history.data[type] = node;
            },
            /**
             * DO LATER
             */
            get: function (type) {
                DX_app.dev.log('::: APP ::: EDIT ::: HISTORY ::: GET');

                var returnResult = null;
                if (DX_app.edit.history.data[type]) {
                    var stillInVisibleDOM = document.body.contains(DX_app.edit.history.data[type]);
                    if (stillInVisibleDOM) {
                        returnResult = DX_app.edit.history.data[type];
                    }
                }

                return returnResult;
            },
            /**
             * DO LATER
             */
            reset: function () {
                DX_app.dev.log('::: APP ::: EDIT ::: HISTORY ::: RESET');
                DX_app.edit.history.data = {
                    settingspage: null,
                    editpage: null
                };
            }
        },

        topbar: {
            publicationButtonContainer: {
                /**
                 * Callback executed when user hovers the Publication Button Container
                 */
                onMouseOver: function () {
                    // Add class to Menu Group to allow advanced CSS styling of the Publish Button
                    var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                    editMenuCentertop.classList.add('hover-publish');
                },
                /**
                 * Callback executed when user leaves the Publication Button Container
                 */
                onMouseOut: function () {
                    // DX_app.edit.topbar.publicationButtonContainer.onMouseOver
                    var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                    editMenuCentertop.classList.remove('hover-publish');
                }
            },
            publicationButtonArrow: {
                /**
                 * Callback execute when the user hover the arrow on the publication button
                 */
                onMouseOver: function () {
                    // Add class to Menu Group to allow advanced CSS styling of the Publish Button
                    var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                    editMenuCentertop.classList.add('hover-publish-advanced');
                },
                /**
                 * Callback execute when the user leaves the arrow on the publication button
                 */
                onMouseOut: function () {
                    // Remove class to Menu Group to allow advanced CSS styling of the Publish Button
                    var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                    editMenuCentertop.classList.remove('hover-publish-advanced');
                }
            },
            previewButtonContainer: {
                /**
                 * Callback execute when the use hovers the preview button container
                 */
                onMouseOver: function () {
                    // Add class to Menu Group to allow advanced CSS styling of the Preview Button
                    var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                    editMenuCentertop.classList.add('hover-preview');
                },
                onMouseOut: function () {
                    // Remove class to Menu Group to allow advanced CSS styling of the Preview Button
                    var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                    editMenuCentertop.classList.remove('hover-preview');
                }
            },
            previewButtonArrow: {
                /**
                 * Callback executed when user hovers the Preview Arrow button
                 */
                onMouseOver: function () {
                    // Add class to Menu Group to allow advanced CSS styling of the Preview Button
                    var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                    editMenuCentertop.classList.add('hover-preview-advanced');
                },
                /**
                 * Callback executed when user leaves the Preview Arrow button
                 */
                onMouseOut: function () {
                    // Remove class to Menu Group to allow advanced CSS styling of the Preview Button
                    var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                    editMenuCentertop.classList.remove('hover-preview-advanced');
                }
            },
            /**
             * Builds the top bar in Edit Mode
             */
            build: function () {
                DX_app.dev.log('::: APP ::: EDIT ::: TOPBAR ::: BUILD');

                // TEMP BLIND
                if (DX_app.data.currentApp == 'edit' || DX_app.data.currentApp == 'contribute') {
                    var elements = {
                        title: document.getElementsByClassName('x-current-page-path')[0],
                        previewButton: document.getElementsByClassName('edit-menu-view')[0],
                        moreInfo: document.getElementsByClassName('edit-menu-edit')[0]
                    };

                    if (elements.title && elements.title.style) {
                        elements.title.style.opacity = 1;
                    }

                    if (elements.previewButton && elements.previewButton.style) {
                        elements.previewButton.style.opacity = 1;
                    }

                    if (elements.moreInfo && elements.moreInfo.style) {
                        elements.moreInfo.style.opacity = 1;
                    }

                    var pageTitle;
                    var selectType = 'none';
                    var multiselect = 'off';
                    var publicationStatus = document.querySelectorAll('.toolbar-item-publicationstatuswithtext .gwt-Image')[0];

                    var extractStatus = function (url) {
                        var urlSplit = url.split('/');
                        var fileName = urlSplit[urlSplit.length - 1];
                        var statusSplit = fileName.split('.png');

                        return statusSplit[0];
                    };

                    // Presumably in Edit Mode or Contribute Mode, in which case we need to set the page title
                    switch (DX_app.iframe.data.selectionCount) {
                        case 0:
                            pageTitle = DX_app.iframe.data.displayName;
                            selectType = 'none';
                            break;
                        case 1:
                            pageTitle = DexV2.getCached('body').getAttribute('data-singleselection-node-displayname');
                            multiselect = 'on';
                            selectType = 'single';
                            break;
                        default:
                            pageTitle = DX_app.dictionary('optionsMultipleSelection').replace('{{count}}', DX_app.iframe.data.selectionCount);
                            multiselect = 'on';
                            selectType = 'multiple';
                            break;
                    }

                    // Set multiselect status in body attribute...
                    DexV2.getCached('body')
                        .setAttribute('data-multiselect', multiselect)
                        .setAttribute('data-select-type', selectType);

                    // Page Title in Edit Made
                    if (pageTitle) {
                        DexV2.class('x-current-page-path').setAttribute('data-PAGE-NAME', pageTitle);
                    }
                    DexV2.class('node-path-text-inner').setHTML(DX_app.iframe.data.displayName);

                    // Determine publication status
                    if (publicationStatus) {
                        DX_app.iframe.data.publication = {
                            status: extractStatus(publicationStatus.getAttribute('src')),
                            label: publicationStatus.getAttribute('title')
                        };
                    } else {
                        DX_app.iframe.data.publication = {
                            status: null,
                            label: null
                        };
                    }

                    DX_app.dev.log('::: DX_app.iframe.data.publication.status [\'' + DX_app.iframe.data.publication.status + '\']');

                    DX_app.iframe.data.pageTitle = pageTitle;

                    // Page Titles need centering
                    DX_app.edit.topbar.reposition();
                }
            },
            /**
             * Callback executes when the window resizes
             */
            reposition: function () {
                DX_app.dev.log('::: APP ::: EDIT ::: TOPBAR ::: REPOSITION');

                // Center title to page and move surrounding menus to right and left.
                DX_app.edit.sidepanel.resizeSidePanel();

                var offset = (DexV2.getCached('body').getAttribute('data-indigo-sidepanel-pinned') == 'true') ? 160 : 0;

                if (document.getElementsByClassName('x-current-page-path').length > 0) {
                    if (DexV2.class('x-current-page-path').getAttribute('data-page-name') != null) {

                        document.getElementsByClassName('edit-menu-publication')[0].style.display = 'block';
                        var elements = {
                            body: document.getElementsByTagName('body')[0],
                            title: document.getElementsByClassName('x-current-page-path')[0],
                            innerTitle: document.getElementsByClassName('node-path-text-inner')[0],
                            publishButton: document.getElementsByClassName('edit-menu-publication')[0],
                            refreshButton: document.getElementsByClassName('window-actions-refresh')[0],
                            nodePathTitle: document.getElementsByClassName('node-path-title')[0],
                            previewButton: document.getElementsByClassName('edit-menu-view')[0],
                            moreInfo: document.getElementsByClassName('edit-menu-edit')[0]
                        };

                        var boxes = {
                            body: elements.body.getBoundingClientRect(),
                            title: elements.title.getBoundingClientRect()
                        };

                        // Center Page Title
                        elements.title.style.left = (((boxes.body.width / 2) - (boxes.title.width / 2)) + offset - 30) + 'px';

                        if (elements.innerTitle) {
                            // Get Inner title bunding box
                            boxes.innerTitle = elements.innerTitle.getBoundingClientRect();

                            // Center Inner title bounding box
                            elements.innerTitle.style.left = ((boxes.body.width / 2) - (boxes.innerTitle.width / 2)) + 5 + offset + 'px';
                        }

                        // Refresh bounding box for title as it has moved
                        boxes.title = elements.title.getBoundingClientRect();

                        if (DX_app.iframe.data.selectionCount > 0) {
                            // Multiselect, so display differently
                            elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 10) + 'px';
                            elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 30) + 'px';
                            elements.nodePathTitle.style.left = (boxes.title.left - 20) + 'px';

                            DexV2('.edit-menu-publication .x-btn-mc').setAttribute('data-publication-label', DX_app.iframe.data.pageTitle);
                        } else {
                            // No Select
                            if (elements.refreshButton) {
                                elements.refreshButton.style.left = (boxes.title.left + boxes.title.width) + 'px';
                            }

                            elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 39) + 'px';
                            elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 63) + 'px';
                            elements.nodePathTitle.style.left = (boxes.title.left - 20) + 'px';

                            elements.nodePathTitle.setAttribute('data-indigo-file-path', DexV2.getCached('body').getAttribute('data-main-node-path'));
                            DexV2('.edit-menu-publication .x-btn-mc').setAttribute('data-publication-label', DX_app.iframe.data.publication.label);
                        }

                        // Make sure correct class is added to publication button
                        elements.publishButton.setAttribute('data-publication-status', DX_app.iframe.data.publication.status);

                        if (DexV2.class('publication-status').exists()) {
                            DexV2.class("publication-status-path").setHTML(DexV2.getCached('body').getAttribute('data-main-node-path'));
                            DexV2.class("publication-status-label").setHTML(jahia_gwt_messages["label_publication_" + DX_app.iframe.data.publication.status]);
                            DexV2.class('publication-status').setAttribute('data-publication-status', DX_app.iframe.data.publication.status);
                        }
                    } else {
                        document.getElementsByClassName('edit-menu-publication')[0].style.display = 'none';
                    }
                }
            }
        },
        /**
         * Everything here concerns the Side Panel
         */
        sidepanel: {
            data: {
                open: false,
                currentTab: null,
                previousTab: null,
                pinned: false,
                firstRun: true,
                channel: {
                    autofit: false,
                    opened: false
                }
            },
            browseTree: {
                onEmpty: function () {
                    // Hide Edit Mode > Side Panel > [Category | Content] > Drawer if nothing to display
                    if (DX_app.edit.sidepanel.data.open) {
                        var isTreeEntry = DexV2.node(this).parent().hasClass('results-column');
                        if (isTreeEntry) {
                            switch (DX_app.edit.sidepanel.data.currentTab) {
                                case 'JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab':
                                    DexV2.id('JahiaGxtCategoryBrowseTab').removeClass('show-results');
                                    break;
                                case 'JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab':
                                    DexV2.id('JahiaGxtContentBrowseTab').removeClass('show-results');
                                    break;
                            }

                            DexV2.getCached('body').removeClass('show-results');
                        }
                    }
                },
                onResults: function () {
                    if (DX_app.edit.sidepanel.data.open) {
                        if (DexV2.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open' && DexV2.getCached('body').getAttribute('data-INDIGO-SIDEPANEL-PINNED') != 'true') {
                            DX_app.edit.sidepanel.resizeSidePanel();
                        }

                        switch(DX_app.edit.sidepanel.data.currentTab){
                            case 'JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab':
                                DexV2.id('JahiaGxtCategoryBrowseTab').addClass('show-results');
                                break;
                            case 'JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab':
                                DexV2.id('JahiaGxtContentBrowseTab').addClass('show-results');
                                break;
                            case 'JahiaGxtSidePanelTabs__JahiaGxtSearchTab':
                                DexV2.id('JahiaGxtSearchTab').addClass('show-results');
                                break;
                        }

                        DexV2.getCached('body').addClass('show-results');
                    }
                }
            },
            thumbPanel: {
                onClose: function () {
                    if (DX_app.edit.sidepanel.data.open) {
                        DexV2.id('JahiaGxtFileImagesBrowseTab').removeClass('show-results');
                        DexV2.getCached('body').removeClass('show-results');
                    }
                },
                onOpen: function () {
                    if (DX_app.edit.sidepanel.data.open) {
                        var isTreeEntry = DexV2.node(this).parent().hasClass('results-column');

                        if (isTreeEntry) {
                            DexV2.id('JahiaGxtFileImagesBrowseTab').addClass('show-results');
                            DexV2.getCached('body').addClass('show-results');
                        }
                    }
                }
            },
            resultsPanel: {
                onEmpty: function () {
                    // Display empty list message
                    var myPagingDisplay = DexV2('#JahiaGxtSearchTab .my-paging-display');
                    if (myPagingDisplay.exists()) {
                        var pagingValue = myPagingDisplay.nodes[0].innerHTML;
                        var noResults = pagingValue === 'No data to display' || pagingValue === 'Aucune donnée à afficher' || pagingValue === 'Keine Daten vorhanden';
                        var status = null;

                        if (noResults) {
                            status = 'no-results';
                        } else if (pagingValue === '') {
                            status = 'init';
                        } else {
                            status = 'searching';
                        }

                        if (DX_app.edit.data.search.status !== status) {
                            DX_app.edit.data.search.status = status;
                            DexV2.id('JahiaGxtSearchTab').filter('.results-column').setAttribute('data-results-status', DX_app.edit.data.search.status);
                        }
                    }
                },
                onResults: function () {
                    var status = 'results';
                    if (DX_app.edit.data.search.status !== status) {
                        DX_app.edit.data.search.status = status;
                        DexV2.id('JahiaGxtSearchTab').filter('.results-column').setAttribute('data-results-status', DX_app.edit.data.search.status);
                    }

                }
            },
			siteSelector: {
				/**
	             * Removes focus from the site selector
	             */
				blur: function(){
				   setTimeout(function(){
					   DexV2(".edit-menu-sites").trigger("mousedown").trigger("mouseup");
				   }, 50);
			   },
			   /**
				* Callback executed when the user clicks on the Site Selector text input field
				*/
				onClick: function(){
					// This opens the drop down menu when the user clicks on the text input
					var arrowButton = this.nextElementSibling;

					DexV2.node(arrowButton).trigger('click');
				},
				/**
	             * Callback executed after the user presses a key (onKeyUp)
				 * - Detects up and down arrow keys (navigates through the menu items)
				 * - Detects Enter key (will trigger click the menu item that matches text in the input field)
	             */
				onKeyUp: function(e){
				   var textInput = this,
					   skip = function(direction){
						   var currentEntry = DexV2('.menu-edit-menu-sites .x-combo-list-item.x-view-highlightrow').nodes[0],
							   skipTo = (direction === 'up') ? currentEntry.previousElementSibling : currentEntry.nextElementSibling,
							   skipToValue = (skipTo) ? skipTo.innerHTML : null;

						   if(skipToValue){
							   DexV2.node(currentEntry).removeClass('x-view-highlightrow');
							   DexV2.node(skipTo).addClass('x-view-highlightrow');
							   textInput.value = skipToValue;
							   DX_app.common.resizeSiteSelector();
						   }
					   };

				   switch(e.keyCode){
					   // UP Arrow
					   case 38:
						   skip('up');
						   break;

					   // DOWN Arrow
					   case 40:
						   skip('down');
						   break;

					   // ENTER Key
					   case 13:
						   var dropDownEntries = DexV2('.menu-edit-menu-sites .x-combo-list-item'),
						   siteName = e.target.value;

						   // Loop through entries to find the predicted site name and click it
						   dropDownEntries.each(function(){
						   if(this.innerHTML === siteName){
							   // GWT fires on the MouseDown / MouseUp events and not Click event
							   DexV2.node(this)
								   .trigger('mousedown')
								   .trigger('mouseup');
							   }
						   });
						   break;
				   }

				}
			},
			/**
             * Callback executed when the side panel is resized
             * @param xPos
             * @returns {boolean}
             */
            resizeSidePanel: function (xPos) {
                DX_app.dev.log('APP ::: SIDEPANEL ::: RESIZESIDEPANEL (' + xPos + ')');

                xPos = xPos || function () {
                    var splitter = DexV2.id('indigoSplitter');
                    var splitterXPos = (splitter.exists()) ? parseInt(splitter.nodes[0].style.getPropertyValue('left')) : null;

                    return splitterXPos || null;
                }();

                if (xPos == null || DexV2.getCached('body').getAttribute('data-indigo-gwt-side-panel') !== 'open') {
                    return false;
                }

                // Block the minimum and maximum widths of the side panel
                if (xPos < 360) {
                    // Block at minimum width
                    xPos = 360;

                    // This is the minimum it can go, so change to an east only cursor when hovering the split bar
                    DexV2.id('indigoSplitter').addClass('move-east-only');
                } else if (xPos > 800) {
                    // Block at maximum width
                    xPos = 800;

                    // This is the maximum it can go, so change to an west only cursor when hovering the split bar
                    DexV2.id('indigoSplitter').addClass('move-west-only');
                } else {
                    // The split bar can be made smaller or bigger, so just display the normal cursor when hovering
                    DexV2.id('indigoSplitter').removeClass('move-east-only');
                    DexV2.id('indigoSplitter').removeClass('move-west-only');
                }

                // Reposition the main frame
                var dashboardMode = DX_app.data.currentApp === 'dashboard';
                var settingsMode = DexV2.getCached('body').getAttribute('data-edit-window-style') == 'settings'
                    || DexV2.getCached('body').getAttribute('data-sitesettings') == 'true'
                    || DexV2.getCached('body').getAttribute('data-indigo-app') == 'admin';
                var mainFrameWidth = (settingsMode || dashboardMode) ? xPos - 68 : xPos + 5;
                var mainFrameLeft = (settingsMode || dashboardMode) ? xPos : xPos + 10;

                var pageTitle;
                var pageTitleBox;
                var siteSettings = DexV2.getCached('body').getAttribute('data-sitesettings') === 'true';
                if (siteSettings || DX_app.data.currentApp === 'admin' || DX_app.data.currentApp === 'dashboard') {
                    // Site Settings, Admin or Dashboard

                    mainFrameWidth = xPos - 48;
                    mainFrameLeft = xPos + 21;

                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('width', 'calc(100% - ' + mainFrameWidth + 'px)',
                        'important');
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('left', mainFrameLeft + 'px', 'important');

                } else if (DX_app.edit.sidepanel.data.pinned) {
                    // Edit Mode pinned
                    mainFrameWidth = xPos + 6;
                    mainFrameLeft = xPos + 45;

                    if (DexV2.class('publication-status').exists()) {
                        DexV2.class('publication-status').nodes[0].style.setProperty('left', mainFrameLeft + 'px', 'important');
                    }

                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('width', 'calc(100% - ' + mainFrameWidth + 'px)', 'important');
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('left', mainFrameLeft + 'px', 'important');

                    // Title
                    if (DexV2.class('node-path-container').exists()) {
                        DexV2.class('node-path-container').nodes[0].style.setProperty('left', (mainFrameLeft - 5) + 'px', 'important');
                    }

                    if (DexV2.class('node-path-text').exists()) {
                        DexV2.class('node-path-text').nodes[0].style.setProperty('max-width', 'calc(100vw - ' + (mainFrameLeft + 60 + 380) + 'px)', 'important');
                    }

                    pageTitle = document.getElementsByClassName('x-current-page-path')[0];
                    pageTitleBox = (pageTitle) ? pageTitle.getBoundingClientRect() : null;

                    if (DexV2.class('window-actions-refresh').exists() && pageTitleBox) {
                        DexV2.class('window-actions-refresh').nodes[0].style.setProperty('left', (pageTitleBox.left + pageTitleBox.width) + 'px', 'important');
                    }


                } else if (DX_app.data.currentApp === 'edit') {
                    // Edit Mode Unpinned
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

                    // Title
                    if (DexV2.class('publication-status').exists()) {
                        DexV2.class('publication-status').nodes[0].style.removeProperty('left');
                    }

                    if (DexV2.class('node-path-container').exists() && DexV2.class('node-path-text').exists()) {
                        DexV2.class('node-path-container').nodes[0].style.removeProperty('left');
                        DexV2.class('node-path-text').nodes[0].style.removeProperty('max-width');
                    }

                    pageTitle = document.getElementsByClassName('x-current-page-path')[0];
                    pageTitleBox = (pageTitle) ? pageTitle.getBoundingClientRect() : null;

                    if (DexV2.class('window-actions-refresh').exists() && pageTitleBox) {
                        DexV2.class('window-actions-refresh').nodes[0].style.setProperty('left', (pageTitleBox.left + pageTitleBox.width) + 'px', 'important');
                    }
                }

                // Reposition the pin button
                if (DexV2.class('side-panel-pin').exists()) {
                    DexV2.class('side-panel-pin').css({
                        left: (xPos - 45) + 'px'
                    });
                }

                if (DexV2.id('JahiaGxtRefreshSidePanelButton')) {
                    DexV2.id('JahiaGxtRefreshSidePanelButton').css({
                        left: (xPos - 85) + 'px'
                    });
                }

                // Set position of content create text filter
                var contentCreateFilter = DexV2('#JahiaGxtCreateContentTab > .x-border-layout-ct > .x-form-field-wrap');

                if (contentCreateFilter.exists()) {
                    contentCreateFilter.nodes[0].style.setProperty('width', (xPos - 109) + 'px', 'important');
                }

                // Set position of Results panels
                var categoriesResultsPane = DexV2('#JahiaGxtCategoryBrowseTab.tab_categories .x-box-inner .x-box-item:nth-child(2)');
                var searchResultPane = DexV2('#JahiaGxtSearchTab.tab_search .JahiaGxtSearchTab-results .x-panel-bwrap');
                var imagesResultPane = DexV2('#JahiaGxtFileImagesBrowseTab.tab_filesimages #images-view');
                var contentResultsPane = DexV2('#JahiaGxtContentBrowseTab.tab_content .x-box-inner .x-box-item:nth-child(2)');
                var xPosOffset = 20;

                if (searchResultPane.exists()) {
                    searchResultPane.nodes[0].style.setProperty('left', (xPos + xPosOffset) + 'px', 'important');
                }

                if (categoriesResultsPane.exists()) {
                    categoriesResultsPane.nodes[0].style.setProperty('left', (xPos + xPosOffset) + 'px', 'important');
                }

                if (imagesResultPane.exists()) {
                    imagesResultPane.nodes[0].style.setProperty('left', (xPos + xPosOffset) + 'px', 'important');
                }

                if (contentResultsPane.exists()) {
                    contentResultsPane.nodes[0].style.setProperty('left', (xPos + xPosOffset) + 'px', 'important');
                }

                // Set width of the Side Panel
                if (DexV2.id('JahiaGxtSidePanelTabs').exists()) {
                    DexV2.id('JahiaGxtSidePanelTabs').nodes[0].style.setProperty('width', xPos + 'px', 'important');
                }

                // Move the split bar to the position of the mouse
                if (DexV2.id('indigoSplitter').exists()) {
                    DexV2.id('indigoSplitter').nodes[0].style.setProperty('left', xPos + 'px', 'important');
                }
            },
            /**
             * Callback executed once when the user starts to resize the side panel
             */
            onStartResize: function () {
                DX_app.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: ONSTARTRESIZE');

                // Register that the side panel is being resized ( CSS uses this to remove pointer events on iframe )
                DexV2.getCached('body').setAttribute('indigo-dragging-panel', 'true');

                // Cancel the resizing when the mouse is released
                document.onmouseup = DX_app.edit.sidepanel.onStopResize;

                // Update the width of the Side panel when mouse is being moved
                document.onmousemove = DX_app.edit.sidepanel.onResize;
            },
            /**
             * Callback executes whilst the side panel is being resized
             * @param e
             */
            onResize: function (e) {
                DX_app.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: ONRESIZE');
                e = e || window.event;

                // Get position of Split bar that will be used to calculate the width of the side panel:
                var xPos = e.clientX;

                DX_app.edit.sidepanel.resizeSidePanel(xPos);
                DX_app.edit.sidepanel.clipPageTitle();
            },
            /**
             * Callback is executed when the user stops resizing the side panel
             */
            onStopResize: function () {
                // Stop listening to the mouse and kill mousemove and mouseup listeners
                document.onmousemove = null;
                document.onmouseup = null;

                // Unregister the resizing of the side panel ( CSS will now remove the no pointer events on the iframe )
                DexV2.getCached('body').setAttribute('indigo-dragging-panel', '');
            },
            /**
             * Callback executed when the user changes the Mobile View
             */
            onNewChannel: function () {
                // Dev note: This is also triggered when the user changes pages by navigation in Device Channel Preview
                if (DX_app.edit.sidepanel.data.channel.opened) {
                    DexV2.id('channel-auto-fit-button').addClass('selected');
                    DexV2.id('channel-zoom-button').removeClass('selected');
                    DexV2.id('channel-size-slider-holder').addClass('disabled');

                    DX_app.edit.sidepanel.zoomChannel(0);
                    DX_app.edit.sidepanel.data.channel.autofit = true;

                    DX_app.edit.sidepanel.close();

                    DexV2('.mainmodule > div:nth-child(2)').removeClass('channel-zoom');
                }
            },
            /**
             * Callback executed when the window resizes
             */
            onWindowResize: function () {
                if (DX_app.edit.sidepanel.data.channel.opened
                    && DX_app.edit.sidepanel.data.channel.autofit) {
                    DX_app.edit.sidepanel.zoomChannel(0);
                }
            },
            /**
             * Builds the Channel controls
             */
            initChannels: function () {
                // Force GWT to load the GWT tab for channels
                // Remove just incase already been added ( can happen when returning to Edit Mode from another app)
                DexV2.id('channel-menu').remove();

                DexV2.id('JahiaGxtSidePanelTabs__JahiaGxtChannelsTab').trigger('click');

                // Build the Channels bar
                var channelMenu = document.createElement('menu');
                var channelCloseButton = document.createElement('button');
                var channelZoomHolder = document.createElement('div');
                var channelAutoFitButton = document.createElement('div');
                var channelAutoFitButtonLabel = document.createTextNode('Autofit');
                var channelZoomButton = document.createElement('div');
                var channelZoomButtonLabel = document.createTextNode('Default');
                var channelSlider = document.createElement('input');
                var channelSliderHolder = document.createElement('div');
                var channelTitle = document.createElement('div');
                var channelOrientaion = document.createElement('div');

                // CLose button
                channelCloseButton.id = 'channel-close-button';

                // Channel Menu
                channelMenu.id = 'channel-menu';

                // Channel Holder
                channelZoomHolder.id = 'channel-zoom-holder';

                // Channel Title
                channelTitle.id = 'channel-title';

                // Auto fit button
                channelAutoFitButton.appendChild(channelAutoFitButtonLabel);
                channelAutoFitButton.id = 'channel-auto-fit-button';

                // Auto fit button
                channelZoomButton.appendChild(channelZoomButtonLabel);
                channelZoomButton.id = 'channel-zoom-button';
                channelZoomButton.classList.add('selected');

                // Orientation button
                channelOrientaion.id = 'channel-orientation';

                // Channel Slider Holder
                channelSliderHolder.id = 'channel-size-slider-holder';

                // Channel Slider
                channelSlider.id = 'channel-size-slider';
                channelSlider.type = 'range';
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
                DexV2.getCached('body').prepend(channelMenu);

                // Get title of clicked channel for the DX Menu
                DexV2('body').onMouseDown('.x-combo-list.channel-device-combo-box .thumb-wrap .x-editable', function () {
                    var channelLabel = DexV2.node(this).getHTML();
                    DexV2.id('channel-title').setAttribute('data-indigo-label', channelLabel);
                }, 'CHANNEL-ONCLICK');

                // Auto fit the channel preview to the screen
                // Dev note: When this is ON we need to update on page resize
                DexV2.getCached('body').onClick('#channel-auto-fit-button', function () {
                    DexV2.id('channel-auto-fit-button').addClass('selected');
                    DexV2.id('channel-zoom-button').removeClass('selected');
                    DexV2.id('channel-size-slider-holder').addClass('disabled');

                    DX_app.edit.sidepanel.zoomChannel(0);
                    DX_app.edit.sidepanel.data.channel.autofit = true;

                    DexV2('.mainmodule > div:nth-child(2)').removeClass('channel-zoom');
                }, 'CHANNEL-AUTO-FIT-BUTTON-ONCLICK');

                // Close button
                DexV2.getCached('body').onClick('#channel-close-button', function () {
                    // Trigger the close button
                    // Click on the Channel drop down in the (hidden) side panel
                    DexV2.id('JahiaGxtChannelsTab').filter('.x-form-trigger').index(0).trigger('click');

                    // When the combo menu opens, add a class to enable repositioning to bottom of screen
                    DexV2.getCached('body').onceOpen('.x-combo-list', function () {
                        // CLick first in the list
                        DexV2.class('x-combo-list').filter('.thumb-wrap').index(0).trigger('mousedown');

                    });

                    DX_app.edit.sidepanel.data.channel.opened = false;
                }, 'CHANNEL-CLOSE-ONCLICK');

                DexV2.getCached('body').onClick('#channel-zoom-button', function () {
                    DexV2.id('channel-auto-fit-button').removeClass('selected');
                    DexV2.id('channel-zoom-button').addClass('selected');
                    DexV2.id('channel-size-slider-holder').removeClass('disabled');

                    DX_app.edit.sidepanel.data.channel.autofit = false;

                    DexV2('.mainmodule > div:nth-child(2)').addClass('channel-zoom');

                    DexV2.id('channel-size-slider').trigger('input');
                }, 'CHANNEL-ZOOM-ONCLICK');

                // Open the combo to change the Channel
                DexV2.getCached('body').onClick('#channel-title', function () {
                    // Click on the Channel drop down in the (hidden) side panel
                    DexV2.id('JahiaGxtChannelsTab').filter('.x-form-trigger').index(0).trigger('click');

                    // When the combo menu opens, add a class to enable repositioning to bottom of screen
                    DexV2.getCached('body').onceOpen('.x-combo-list', function () {
                        DexV2.class('x-combo-list').addClass('channel-device-combo-box');
                    });

                    DX_app.edit.sidepanel.data.channel.opened = true;
                }, 'CHANNEL-TITLE-ONCLICK');

                // Toggle between orientations
                DexV2.getCached('body').onClick('#channel-orientation', function () {
                    // Open the Orrientation combo in the (hidden) side panel
                    DexV2.id('JahiaGxtChannelsTab').filter('.x-form-trigger').index(1).trigger('click');

                    // When it is opened, click on the orientation that is NOT selected
                    DexV2.getCached('body').onceOpen('.x-combo-list', function () {
                        DexV2('.x-combo-list .x-combo-list-item:not(.x-view-highlightrow)').trigger('mousedown');
                    });
                }, 'CHANNEL-ORIENTATION-ONCLICK');

                // Redimension the Channel Preview
                DexV2.getCached('body').onInput('#channel-size-slider', function (e) {
                    var zoomSize = e.target.value;
                    DX_app.edit.sidepanel.zoomChannel(zoomSize);
                }, 'CHANNEL-SIZE-SLIDER');
            },
            /**
             * Callback executed when the user zooms on the Channel View
             * @param zoomSize
             */
            zoomChannel: function (zoomSize) {
                var windowHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
                var actualHeight = parseInt(DexV2('.mainmodule > div:nth-child(2) > div').nodes[0].style.height);
                var windowPadding = 136;
                var transformOrigin = '50% 0';
                var scale = (zoomSize > 0) ? (zoomSize / 100) : ((windowHeight - windowPadding) / actualHeight);

                if (scale > 1) {
                    scale = 1;
                }

                DexV2('.x-abs-layout-container').css({
                    transform: 'scale(' + scale + ')',
                    transformOrigin: transformOrigin
                });
            },
            /**
             * Callback executed when the user toggle the Side Panel Pin
             */
            togglePin: function () {
                DX_app.edit.sidepanel.data.pinned = !DX_app.edit.sidepanel.data.pinned;

                DexV2.getCached('body').setAttribute('data-INDIGO-SIDEPANEL-PINNED', DX_app.edit.sidepanel.data.pinned);
                DexV2.iframe('.window-iframe').filter('body').nodes[0].style.pointerEvents = 'all';

                if (DX_app.edit.sidepanel.data.pinned && DexV2.id('indigoSplitter').exists()) {
                    var xPos = parseInt(DexV2.id('indigoSplitter').nodes[0].style.getPropertyValue('left'));
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('width', 'calc(100% - ' + (xPos + 5) + 'px)',
                        'important');
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('left', (xPos + 10) + 'px', 'important');
                } else {
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');
                }

                DX_app.edit.topbar.reposition();
                DX_app.edit.sidepanel.clipPageTitle();

                DX_app.storage.set('pinnedPanel', DX_app.edit.sidepanel.data.pinned);

                if(DX_app.iframe.data.selectionCount > 0){
                    // At least one node in the page has been selected, so we should redraw
                    authoringApi.redrawSelection();
                }
            },
            /**
             * DO LATER
             * @param e
             */
            toggleFloatingPanel: function (e) {
                DX_app.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: TOGGLEFLOATINGPANEL');

                if (DexV2.node(e.target).getAttribute('id') == 'images-view'
                    || DexV2.node(e.target).hasClass('x-panel-bwrap')
                    || DexV2.node(e.target).hasClass('x-box-item')) {

                    if (DexV2.getCached('body').hasClass('show-results')) {
                        DexV2.getCached('body').toggleClass('minimise-results');
                    } else {
                        DexV2.getCached('body').removeClass('minimise-results');
                    }
                }

                DX_app.edit.sidepanel.clipPageTitle();
            },
            /**
             * Callback executed when the user starts to drag an element from the Side Panel
             * @param start
             */
            onDrag: function (start) {
                DX_app.dev.log('::: APP ::: EDIT ::: SIDEPANEL :::' + start ? 'ONSTARTDRAG' : 'ONSTOPDRAG');

                var cachedBody = DexV2.getCached('body');
                cachedBody.toggleClass('indigo-drag-to-drop');
                if (DX_app.edit.sidepanel.data.pinned) {
                    cachedBody.toggleClass('show-results');
                    cachedBody.toggleClass('minimise-results');
                    DX_app.edit.sidepanel.clipPageTitle();
                } else if (cachedBody.getAttribute('data-indigo-gwt-panel-tab') === 'JahiaGxtSidePanelTabs__JahiaGxtPagesTab') {
                    // do nothing if pages is open
                } else {
                    start ? DX_app.edit.sidepanel.close() : DX_app.edit.sidepanel.open();
                }
            },
            /**
             * Builds a splitter used for controlling the width of the side panel
             */
            buildSplitter: function () {
                // Handle Splitter ( used for changing width of Side Panel )
                if (!DexV2.id('indigoSplitter').exists()) {
                    // Create Side Panel splitter ( cant gain proper control of GWT splitter)

                    var sidePanelSplitter = document.createElement('div');
                    // Set ID
                    sidePanelSplitter.id = 'indigoSplitter';
                    sidePanelSplitter.style.setProperty('left', '360px', 'important');

                    // Attach event listener for drag start
                    sidePanelSplitter.onmousedown = DX_app.edit.sidepanel.onStartResize;

                    // Add the spliiter to the body
                    DexV2.getCached('body').prepend(sidePanelSplitter);
                }
            },
            /**
             * Used to hide the title of the page when the side panel is opened
             */
            clipPageTitle: function () {
                DX_app.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: CLIPPAGETITLE');

                if (DX.data.currentApp === 'edit') {
                    var sidepanelWidth = parseInt(document.getElementById('JahiaGxtSidePanelTabs').style.width) - 78;
                    var pageTitleClip = null;
                    var wideSidepanels = ['JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab', 'JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab', 'JahiaGxtSidePanelTabs__JahiaGxtSearchTab', 'JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab'];
                    var isWide = wideSidepanels.indexOf(DX_app.edit.sidepanel.data.currentTab) > -1;
                    var isMinimised = isWide && DexV2.getCached('body').hasClass('minimise-results');
                    var isPinned = DX_app.edit.sidepanel.data.pinned;
                    var topRightMenuClip = null;
                    var topRightMenuWidth = parseInt(window.getComputedStyle(DexV2.class('edit-menu-topright').nodes[0])['width']);

                    if (DX_app.edit.sidepanel.data.firstRun) {
                        sidepanelWidth += 60;
                        DX_app.edit.sidepanel.data.firstRun = false;
                    }

                    if (DX_app.edit.sidepanel.data.open) {
                        if (isPinned && isWide && !isMinimised) {
                            // PINNED - WIDE PANEL - EXPANDED
                            pageTitleClip = 343;
                            topRightMenuClip = pageTitleClip - topRightMenuWidth + 128 + sidepanelWidth;
                        } else if (!isPinned && isWide && !isMinimised) {
                            // UNPINNED - WIDE PANEL - EXPANDED
                            pageTitleClip = sidepanelWidth + 353;
                            topRightMenuClip = pageTitleClip - topRightMenuWidth + 118;
                        } else if (isPinned && isWide && isMinimised) {
                            // PINNED - WIDE PANEL - COLLAPSED
                            pageTitleClip = 14;
                            topRightMenuClip = null;
                        } else if (!isPinned && isWide && isMinimised) {
                            // UNPINNED - WIDE PANEL - COLLAPSED
                            pageTitleClip = sidepanelWidth + 24;
                            topRightMenuClip = pageTitleClip - topRightMenuWidth + 118;
                        } else if (isPinned && !isWide) {
                            // PINNED - NORMAL PANEL
                            pageTitleClip = null;
                            topRightMenuClip = null;
                        } else if (!isPinned && !isWide) {
                            // UNPINNED - NORMAL PANEL
                            pageTitleClip = 282;
                            topRightMenuClip = pageTitleClip - topRightMenuWidth + 118;
                        }
                    }

                    if (pageTitleClip === null) {
                        DexV2.class('x-current-page-path').nodes[0].style.removeProperty('clip');
                        DexV2.class('edit-menu-topright').nodes[0].style.removeProperty('clip');
                    } else {
                        DexV2.class('x-current-page-path').nodes[0].style.setProperty('clip', 'rect(0px, 100vw, 30px, ' + pageTitleClip + 'px)', 'important');
                    }

                    if (topRightMenuClip === null) {
                        DexV2.class('edit-menu-topright').nodes[0].style.removeProperty('clip');
                    } else {
                        DexV2.class('edit-menu-topright').nodes[0].style.setProperty('clip', 'rect(0px, 100vw, 30px, ' + topRightMenuClip + 'px)', 'important');
                    }
                }
            },
            /**
             * Opens the Side Panel
             * @param isSettings
             */
            open: function (isSettings) {
                DX_app.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: OPEN [isSettings=\'' + isSettings + '\']');
                // Set CSS to open side panel
                DexV2.getCached('body').setAttribute('data-INDIGO-GWT-SIDE-PANEL', 'open');
                DX_app.edit.sidepanel.data.open = true;
                DX_app.edit.sidepanel.resizeSidePanel();

                DX_app.edit.sidepanel.buildSplitter();

                var keepCheckingForEmpties = true;
                // Check if there are any empty rows, if so then refresh the panel
                DexV2.id('JahiaGxtPagesTab').filter('.x-grid3-row').each(function (dexObject) {
                    if (keepCheckingForEmpties) {
                        if (dexObject.getHTML() == '') {
                            keepCheckingForEmpties = false;
                            DexV2.id('JahiaGxtRefreshSidePanelButton').trigger('click');
                        }
                    }
                });

                if (!isSettings) {
                    // Disable clicks
                    DX_app.iframe.disableClicks();
                }

                DX_app.edit.sidepanel.clipPageTitle();
            },
            /**
             * Closes the Side Panel
             */
            close: function () {
                if (DexV2.getCached('body').getAttribute('data-sitesettings') !== 'true'
                    && DexV2.getCached('body').getAttribute('data-edit-window-style') !== 'settings'
                    && DexV2.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open'
                    && DexV2.getCached('body').getAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL') == 'yes'
                    && DexV2.getCached('body').getAttribute('data-INDIGO-SIDEPANEL-PINNED') != 'true') {
                    DX_app.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: CLOSE');
                    DX_app.edit.sidepanel.data.open = false;

                    var siteCombo = DexV2('body[data-indigo-gwt-side-panel=\'open\'] .window-side-panel div[role=\'combobox\']');
                    if (siteCombo.exists()) {
                        siteCombo.nodes[0].style.setProperty('width', 'auto', 'important');
                    }

                    DexV2.getCached('body').setAttribute('data-INDIGO-GWT-SIDE-PANEL', '');

                    // Revert iframes body style attribute to what it was originally
                    DexV2.iframe('.window-iframe').filter('body').nodes[0].style.pointerEvents = 'all';

                    if (DexV2.id('JahiaGxtSidePanelTabs').exists()) {
                        DexV2.id('JahiaGxtSidePanelTabs').nodes[0].style.setProperty('width', '60px', 'important');
                        DexV2.getCached('body').setAttribute('data-indigo-gwt-side-panel', '');
                    }
                }

                DX_app.edit.sidepanel.clipPageTitle();
            },
            /**
             * Used to determine whether or not the side panel is opened
             * @returns {boolean}
             */
            isOpen: function () {
                return DX_app.edit.sidepanel.data.open;
            },
            tabs: {
                all: {
                    onClick: function () {
                        DX_app.dev.log('APP ::: EDIT ::: SIDEPANEL ::: TAB ::: ONCLICK');

                        if(DexV2.node(this).getAttribute('id') == 'JahiaGxtSidePanelTabs__JahiaGxtSettingsTab' && DexV2.getCached('body').getAttribute('data-edit-mode-status') !== 'initialised'){
                            // Edit Mode has not yet finsihed loading the page, so just ignore clicks on settings button until it is ready
                            return false;
                        }

                        // User has clicked on one of the side panel tabs (except for Settings Tab which calls eventHandlers.clickSidePanelSettingsTab)
                        var clickedTabID = DexV2.node(this).getAttribute('id');

                        DX_app.edit.sidepanel.data.previousTab = DX_app.edit.sidepanel.data.currentTab;
                        DX_app.edit.sidepanel.data.currentTab = clickedTabID;

                        if(DX_app.edit.sidepanel.data.newSite){
                            DX_app.edit.sidepanel.data.newSite = false;

                        } else {
                            if (DX_app.edit.sidepanel.data.previousTab === DX_app.edit.sidepanel.data.currentTab) {
                                if (DexV2.getCached('body').getAttribute('data-sitesettings') == 'true'
                                    && clickedTabID !== 'JahiaGxtSidePanelTabs__JahiaGxtSettingsTab') {
                                    setTimeout(function () {
                                        DexV2.id('JahiaGxtSidePanelTabs__JahiaGxtSettingsTab').trigger('mousedown').trigger('mouseup').trigger('click');
                                    }, 0);
                                } else if (DexV2.getCached('body').getAttribute('data-sitesettings') !== 'true'
                                    && DexV2.getCached('body').getAttribute('data-indigo-sidepanel-pinned') == 'true'
                                    && clickedTabID === 'JahiaGxtSidePanelTabs__JahiaGxtSettingsTab') {
                                    setTimeout(function () {
                                        DexV2.id('JahiaGxtSidePanelTabs__JahiaGxtPagesTab').trigger('mousedown').trigger('mouseup').trigger('click');
                                    }, 0);
                                }
                            }
                        }

                        DexV2.getCached('body').setAttribute('data-INDIGO-GWT-PANEL-TAB', clickedTabID);

                        // Menus for the Tabs that call this listener require a normal side panel display
                        var tabMenuActive = DexV2.node(this).hasClass('x-tab-strip-active');
                        var sidePanelOpen = DexV2.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open';
                        if (tabMenuActive && sidePanelOpen) {
                            // CLOSE SIDE PANEL: Already open for current Tab Menu
                            DX_app.edit.sidepanel.close();
                        } else {
                            // OPEN SIDE PANEL.
                            DX_app.edit.sidepanel.open(false);
                        }

                        if(DX_app.edit.sidepanel.data.currentTab == 'JahiaGxtSidePanelTabs__JahiaGxtSearchTab'){
                            DexV2.id('JahiaGxtSearchTab').addClass('show-results');
                            DexV2.getCached('body').addClass('show-results');
                        }
                    }
                },
                contentTab: {
                    /**
                     * Callback executed when the Files Tab is opened
                     */
                    onOpen: function () {
                        // Add Class to tree to create the sub tree drawer
                        DexV2.node(this).filter('.x-box-item:nth-child(2) .x-grid3-body').addClass('results-column');
                    }
                },
                categoryTab: {
                    /**
                     * Callback executed when the Files Tab is opened
                     */
                    onOpen: function () {
                        // Add Class to tree to create the sub tree drawer
                        DexV2.node(this).filter('.x-box-item:nth-child(2) .x-grid3-body').addClass('results-column');
                    }
                },
                filesTab: {
                    /**
                     * Callback executed when the Files Tab is opened
                     */
                    onOpen: function () {
                        // Add Class to tree to create the sub tree drawer
                        DexV2.node(this).filter('#images-view > div').addClass('results-column');
                    }
                },
                createContent: {
                    onOpen: function () {
                        // Add placeholder to text input
                        DexV2.node(this).filter('input.x-form-text').setAttribute('placeholder', DX_app.dictionary('filterContent'));
                    }
                },
                searchTab: {
                    onOpen: function () {
                        // Force the drawer to stay open regardless of whether or not there are any results
                        if (DX_app.edit.sidepanel.data.open) {
                            DexV2.id('JahiaGxtSearchTab').addClass('show-results');
                            DexV2.getCached('body').addClass('show-results');
                            DX_app.edit.sidepanel.resizeSidePanel();
                        }

                        DexV2.node(this).filter('.JahiaGxtSearchTab-results .x-grid3-body').addClass('results-column');
                        DexV2('#JahiaGxtSearchTab.tab_search .JahiaGxtSearchTab-results .x-toolbar-left-row td.x-toolbar-cell:last-child > table.x-btn.x-component.x-unselectable.x-btn-icon')
                            .addClass('search-side-panel-refresh-button'); // Ask thomas to add a classname here
                    }
                }
            },

            tab: {
                /**
                 * Callback executed when the user clicks on a Side Panel tab
                 */

            },
            row: {
                /**
                 * Callback executed when user clicks on tree row in Side Panel
                 *  - Determines whether or not to open the context menu
                 * @param e
                 */
                onContext: function (e) {
                    DX_app.dev.log('APP ::: EDIT ::: SIDEPANEL ::: ROW ::: ONCONTEXT');

                    // Open Context Menu when clicking "More" button.
                    var acceptClick = DexV2.node(e.target).hasClass('x-grid3-td-displayName');
                    if (acceptClick) {
                        DexV2.node(e.target).trigger('contextmenu', e.pageX, e.pageY);
                    }
                },
                /**
                 * Callback executed when the user mouse down on a row in side panel tree
                 * @param e
                 */
                onMouseDown: function (e) {
                    var nodeJoint = DexV2.node(e.target).hasClass('x-tree3-node-joint');
                    if (!nodeJoint) {
                        var alreadySelected = DexV2.node(this).hasClass('indigo-selected');
                        if (alreadySelected) {
                            // Toggle the drawer
                            DexV2.getCached('body').toggleClass('minimise-results');
                        } else {
                            // Show drawer
                            DexV2.getCached('body').removeClass('minimise-results');

                            if (DexV2.id('JahiaGxtSidePanelTabs').exists()) {
                                DexV2.id('JahiaGxtSidePanelTabs').filter('.indigo-selected').removeClass('indigo-selected');
                            }
                            DexV2.node(this).addClass('indigo-selected');
                        }

                        DX_app.edit.sidepanel.clipPageTitle();
                    }
                }
            }
        },
        /**
         * Everything here concerns settings in Edit Mode ( not to be confused with Admin mode)
         */
        settings: {
            data: {
                opened: false,
                iframeCSSOverRide: '.well{border:none!important; box-shadow: none!important;} body{background-image: none!important; background-color:#f5f5f5!important}'
            },
            /**
             * NOT USED - CHECK BEFORE DELETING
             */
            onTreeLoad: function () {},
            /**
             * NOT USED - CHECK BEFORE DELETING
             */
            onTreeChange: function () {},
            /**
             * DO LATER
             * @param attrKey
             * @param attrValue
             */
            onChange: function (attrKey, attrValue) {
                DX_app.dev.log('::: APP ::: SETTINGS ::: EDIT ::: SETTINGS ::: ONCHANGE');
                if (attrKey == 'data-sitesettings' && attrValue == 'true') {
                    if (DX_app.data.currentApp == 'edit') {
                        DX_app.edit.settings.open(null, 'directAccess');
                    }
                }
            },
            /**
             * NOT USED - CHECK BEFORE DELETING
             */
            onReady: function () {
                DX_app.dev.log('::: APP ::: EDIT ::: SETTINGS ::: ONREADY');
            },
            /**
             * Callback executed when the Settings are opened
             */
            open: function () {
                DX_app.dev.log('::: APP ::: EDIT ::: SETTINGS ::: OPEN');

                DX_app.edit.sidepanel.buildSplitter();
                DX_app.edit.sidepanel.resizeSidePanel();

                DexV2.getCached('body').setAttribute('data-indigo-gwt-side-panel', 'open');
                DexV2.id('JahiaGxtSidePanelTabs__JahiaGxtSettingsTab').trigger('click');
            },
            /**
             * NOT USED - CHECK BEFORE DELETING
             */
            close: function () {
                DX_app.dev.log('::: APP ::: EDIT ::: SETTINGS ::: CLOSE');
            }
        }
    },
    /**
     * Everyhting here concerns the Dashboard
     */
    dashboard: {
        config: {
            chrome: true
        },
        // Event Handlers
        data: {
            beta: false
        },
        /**
         * Callback executed when switching to the Dashboard
         */
        onOpen: function () {
            DX_app.dev.log('::: APP ::: DASHBOARD ::: OPENED');

            DexV2.getCached('body').setAttribute('data-indigo-styled-combos', 'true');

            // Set attributes to be used by CSS
            DexV2.getCached('body')
                .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'no')
                .setAttribute('data-INDIGO-GWT-SIDE-PANEL', 'open');
        },
        /**
         * Callback executed when the users navigates in Dashboard
         */
        onChange: function () {
            if (DX_app.dashboard.data.beta) {
                DexV2.getCached('body').setAttribute('data-INDIGO-BETA', 'true');
                DX_app.dashboard.setBetaStyle();

            } else {
                DexV2.getCached('body').setAttribute('data-INDIGO-BETA', '');
                DX_app.edit.sidepanel.buildSplitter();
                DX_app.edit.sidepanel.resizeSidePanel();
            }
        },
        /**
         * Callback executed when leaving the Dashboard
         */
        onClose: function () {},
        /**
         * BETA STYLING HAS BEEN REMOVED - THIS CAN BE DELETED BUT NEED TO REMOVE CALLS TO THIS FUNCTION TOO
         */
        setBetaStyle: function () {}
    },
    /**
     * Everything here concerns the Contribute Mode
     */
    contribute: {
        config: {
            chrome: true
        },
        // Event Handlers
        data: {
            mode: null
        },
        /**
         * Callback executed when switching to the Contribute Mode
         */
        onOpen: function () {
            DX_app.dev.log('::: APP ::: CONTRIBUTE ::: OPENED');

            // Set attributes to be used by CSS
            DexV2.getCached('body')
                .setAttribute('data-INDIGO-GWT-SIDE-PANEL', '')
                .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'yes')
                .setAttribute('data-indigo-sidepanel-pinned', 'false');

            DX_app.edit.sidepanel.data.pinned = false;
            DX_app.contribute.topbar.build();
        },
        /**
         * Callback executed when leaving the Contribute Mode
         */
        onClose: function () {},
        /**
         * Callback executed when the user navigates in the iframe
         * @param attrKey
         * @param attrValue
         * @returns {boolean}
         */
        onChangeMode: function (attrKey, attrValue) {
            if (DX_app.data.currentApp != 'contribute') {
                return false;
            }

            var nodePathSplit = attrValue.split('/');
            var modePath = nodePathSplit[3];
            var mode;
            var iframeSRC = DexV2.class('window-iframe').getAttribute('src');
            var displayingNode = iframeSRC.indexOf('viewContent.html') > -1;

            switch (modePath) {
                case 'files':
                    mode = 'files';
                    break;
                case 'contents':
                    mode = 'content';
                    break;
                default:
                    mode = 'site';
                    break;
            }

            DX_app.contribute.data.mode = mode;
            DexV2.getCached('body').setAttribute('data-contribute-mode', DX_app.contribute.data.mode);

            DX_app.dev.log('????????????????????????????????????');
            DX_app.dev.log('CHANGED nodePath: ' + attrValue);
            DX_app.dev.log('CHANGED section: ' + nodePathSplit[3]);
            DX_app.dev.log('displayingNode: ' + displayingNode);
            DX_app.dev.log('????????????????????????????????????');

            DX_app.contribute.data.displayingNode = displayingNode;

            DexV2.getCached('body').setAttribute('data-contribute-displaying-node', DX_app.contribute.data.displayingNode);
        },

        // Controls
        topbar: {
            /**
             * Builds the top bar in Contribute Mode
             * @returns {boolean}
             */
            build: function () {
                if (!DX_app.contribute.data.mode) {
                    DX_app.dev.log('::: APP ::: CONTRIBUTE ::: TOPBAR ::: BUILD ( CAN NOT YET BUILD)');
                    return false;
                }

                DX_app.dev.log('::: APP ::: CONTRIBUTE ::: TOPBAR ::: BUILD (MODE: ' + DX_app.contribute.data.mode + ')');

                // TEMP BLIND
                if (DX_app.data.currentApp == 'edit' || DX_app.data.currentApp == 'contribute') {
                    var elements = {
                        title: document.getElementsByClassName('toolbar-item-publicationstatus')[0],
                        previewButton: document.getElementsByClassName('edit-menu-view')[0],
                        moreInfo: document.getElementsByClassName('edit-menu-edit')[0]
                    };

                    if (elements.title &&
                        elements.title.style) {
                        elements.title.style.opacity = 1;

                    }

                    if (elements.previewButton &&
                        elements.previewButton.style) {
                        elements.previewButton.style.opacity = 1;
                    }

                    if (elements.moreInfo &&
                        elements.moreInfo.style) {
                        elements.moreInfo.style.opacity = 1;
                    }

                    var pageTitle = DX_app.iframe.data.displayName;
                    var publicationStatus = publicationStatus = document.querySelectorAll('.toolbar-item-publicationstatus .gwt-Image')[0];
                    var extractStatus = function (url) {
                        var urlSplit = url.split('/');
                        var fileName = urlSplit[urlSplit.length - 1];
                        var statusSplit = fileName.split('.png');

                        return statusSplit[0];
                    };

                    if (publicationStatus) {
                        DX_app.iframe.data.publication = {
                            status: extractStatus(publicationStatus.getAttribute('src')),
                            label: publicationStatus.getAttribute('title')
                        };
                    } else {
                        DX_app.iframe.data.publication = {
                            status: null,
                            label: null
                        };
                    }

                    elements.title.setAttribute('data-PAGE-NAME', pageTitle);

                    DX_app.dev.log('DX_app.iframe.data.publication:' + DX_app.iframe.data.publication);
                    DX_app.contribute.topbar.reposition();
                }
            },
            /**
             * Callback executes when the window resizes
             */
            reposition: function () {
                DX_app.dev.log('::: APP ::: CONTRIBUTE ::: TOPBAR ::: REPOSITION');

                if (DexV2.class('toolbar-item-publicationstatus').getAttribute('data-page-name') != null) {
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
                    DexV2('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

                    var elements = {
                        body: document.getElementsByTagName('body')[0],
                        title: document.getElementsByClassName('toolbar-item-publicationstatus')[0],
                        // innerTitle: document.getElementsByClassName("node-path-text-inner")[0],
                        publishButton: document.getElementsByClassName('contribute-menu-publication')[0],
                        // refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                        // nodePathTitle: document.getElementsByClassName("node-path-title")[0],
                        previewButton: document.getElementsByClassName('edit-menu-view')[0],
                        editPage: DexV2('.action-toolbar .x-toolbar-cell:nth-child(5) table').getNode(0)
                    };

                    var boxes = {
                        body: elements.body.getBoundingClientRect(),
                        title: elements.title.getBoundingClientRect()
                    };

                    // Center Page Title
                    elements.title.style.left = ((boxes.body.width / 2) - (boxes.title.width / 2)) + 'px';

                    // Refresh bounding box for title as it has moved
                    boxes.title = elements.title.getBoundingClientRect();

                    // No Select
                    elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 9) + 'px';
                    elements.editPage.style.left = (boxes.title.left + boxes.title.width + 33) + 'px';
                    DexV2('.contribute-menu-publication .x-btn-mc').setAttribute('data-publication-label', DX_app.iframe.data.publication.label);

                    // Make sure correct class is added to publication button
                    elements.publishButton.setAttribute('data-publication-status', DX_app.iframe.data.publication.status);

                    if (DexV2.class('publication-status').exists()) {
                        DexV2.class("publication-status-path").setHTML(DexV2.getCached('body').getAttribute('data-main-node-path'));
                        DexV2.class("publication-status-label").setHTML(jahia_gwt_messages["label_publication_" + DX_app.iframe.data.publication.status]);
                        DexV2.class('publication-status').setAttribute('data-publication-status', DX_app.iframe.data.publication.status);
                    }
                }

            }
        }

    }
};
