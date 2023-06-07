/**
* This object concerns Context Menus in Jahia.
 * @memberof Anthracite
 * @property {method} setTitle Adds a title to contextual menus
 * @property {object} managerMenu XXX <br> See {@link Anthracite.contextMenus.managerMenu}
 * @property {object} previewMenu XXX <br> See {@link Anthracite.contextMenus.previewMenu}
 * @property {object} publicationMenu XXX <br> See {@link Anthracite.contextMenus.publicationMenu}
 * @property {object} moreInfoMenu XXX <br> See {@link Anthracite.contextMenus.moreInfoMenu}
 * @namespace Anthracite.contextMenus
 * @type {object}
 */
 Anthracite.addModule("contextMenus", {
    /**
     * Adds a title to contextual menus
     * @memberof Anthracite.contextMenus
     * @method setTitle
     * @param {HTMLelement} contextmenu XXX
     * @param {object} params XXX

     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    setTitle: function (contextmenu, params) {
        Anthracite.dev.log('::: APP ::: CONTEXTMENUS ::: SETTITLE');

        var contextMenuTitle;
        var contextMenuList = contextmenu.getElementsByClassName('x-menu-list')[0];
        if (contextMenuList) {
            switch (Anthracite.iframe.data.selectionCount) {
                case 0:
                    // Page
                    contextMenuTitle = params.noSelection;
                    break;
                case 1:
                    // Selected Item
                    contextMenuTitle = params.singleSelection.replace('{{node}}', jGet.getCached('body').getAttribute(
                        'data-singleselection-node-displayname'));
                    break;
                default:
                    // Multiple selection
                    contextMenuTitle = params.multipleSelection.replace('{{count}}', Anthracite.iframe.data.selectionCount);
                    break;
            }

            contextMenuList.setAttribute('data-indigo-title', Anthracite.iframe.escapeHTML(contextMenuTitle));
        }
    },
    /**
     * XXX
     * @memberof Anthracite.contextMenus
     * @property {object} data Stores data for Object
     * @property {method} onOpen Callback executed when the Hamburger Menu is opened
     * @property {method} onClose Callback executed when the Hamburger Menu is closed
     * @namespace Anthracite.contextMenus.managerMenu
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    managerMenu: {
        /**
         * Stores top level data
         * @memberof Anthracite.contextMenus.managerMenu
         * @type {object}
         * @property {boolean} opened XXX
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        data: {
            opened: false
        },
         /**
          * Callback executed when the Hamburger Menu is opened
          * @memberof Anthracite.contextMenus.managerMenu
          * @method onOpen

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onOpen: function () {
            Anthracite.dev.log('::: APP ::: CONTEXTMENUS ::: MANAGERMENU ::: ONOPEN');
            jGet.getCached('body').setAttribute('data-indigo-hamburger-menu', 'open');
            Anthracite.contextMenus.managerMenu.data.opened = true;

            jGet.node(this).setAttribute('data-indigo-current-app', Anthracite.data.currentApp);

            if (!jGet.class('menu-editmode-managers-menu').hasClass('managers-menu-built')) {
                var footerContainer = document.createElement('div');
                footerContainer.classList.add('footer');

                var loggedUserLabel = document.createElement('label');
                var loggedUser = jGet.getCached('body').getAttribute('data-currentuser');

                var closeButton = document.createElement('button');
                var closeButtonLabel = document.createTextNode('Close');
                var backgroundMask = document.createElement('div');

                loggedUserLabel.innerHTML = 'Logged in as <span>' + loggedUser + '</span>';
                loggedUserLabel.classList.add('user');

                backgroundMask.classList.add('managers-menu-mask');
                closeButton.classList.add('managers-menu-close');
                closeButton.appendChild(closeButtonLabel);

                backgroundMask.addEventListener('click', function () {
                    jGet.getCached('body')
                        .trigger('mousedown')
                        .trigger('mouseup');
                });

                footerContainer.appendChild(loggedUserLabel);
                footerContainer.appendChild(loggedUserLabel);

                jGet.class('menu-editmode-managers-menu').prepend(footerContainer);
                jGet.class('menu-editmode-managers-menu').prepend(closeButton);
                jGet.class('menu-editmode-managers-menu').append(backgroundMask);

                jGet('.menu-editmode-managers-menu').onClick({
                    target: '.managers-menu-close',
                    callback: function () {
                        jGet.getCached('body')
                            .trigger('mousedown')
                            .trigger('mouseup');
                    },
                    uid: "CLOSE-DX-MENU"
                });

                jGet.class('menu-editmode-managers-menu').addClass('managers-menu-built');
            }
        },
         /**
          * Callback executed when the Hamburger Menu is closed
          * @memberof Anthracite.contextMenus.managerMenu
          * @method onClose

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onClose: function () {
            Anthracite.dev.log('::: APP ::: CONTEXTMENUS ::: MANAGERMENU ::: ONCLOSE');
            jGet.getCached('body').setAttribute('data-indigo-hamburger-menu', '');
            Anthracite.contextMenus.managerMenu.data.opened = false;
        }
    },
    /**
     * XXX
     * @memberof Anthracite.contextMenus
     * @property {method} onOpen Callback executed when the user opens the preview drop down
     * @namespace Anthracite.contextMenus.previewMenu
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    previewMenu: {
         /**
          * Callback executed when the user opens the preview drop down
          * @memberof Anthracite.contextMenus.previewMenu
          * @method onOpen

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onOpen: function () {
            Anthracite.dev.log('::: APP ::: CONTEXTMENUS ::: PREVIEWMENU ::: ONOPEN');

            // Set the title that appears in the Context Menu
            Anthracite.contextMenus.setTitle(this, {
                noSelection: Anthracite.dictionary.get('previewPage'),
                singleSelection: Anthracite.dictionary.get('previewSingleSelection'),
                multipleSelection: Anthracite.dictionary.get('previewMultipleSelection')
            });
        }
    },
    /**
     * XXX
     * @memberof Anthracite.contextMenus
     * @property {method} onOpen Callback executed when the user opens the publication drop down
     * @namespace Anthracite.contextMenus.publicationMenu
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    publicationMenu: {
         /**
          * Callback executed when the user opens the publication drop down
          * @memberof Anthracite.contextMenus.publicationMenu
          * @method onOpen

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onOpen: function () {
            Anthracite.dev.log('::: APP ::: CONTEXTMENUS ::: PUBLICATIONMENU ::: ONOPEN');

            // Set the title that appears in the Context Menu
            Anthracite.contextMenus.setTitle(this, {
                noSelection: Anthracite.dictionary.get('publishPage'),
                singleSelection: Anthracite.dictionary.get('publishSingleSelection'),
                multipleSelection: Anthracite.dictionary.get('publishMultipleSelection')
            });
        }
    },
    /**
     * XXX
     * @memberof Anthracite.contextMenus
     * @property {method} onOpen Callback executed when the user opens the more info drop down
     * @namespace Anthracite.contextMenus.moreInfoMenu
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    moreInfoMenu: {
         /**
          * Callback executed when the user opens the more info drop down
          * @memberof Anthracite.contextMenus.moreInfoMenu
          * @method onOpen

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onOpen: function () {
            Anthracite.dev.log('::: APP ::: CONTEXTMENUS ::: MOREINFOMENU ::: ONOPEN');
            Anthracite.contextMenus.setTitle(this, {
                noSelection: Anthracite.dictionary.get('optionsPage'),
                singleSelection: Anthracite.dictionary.get('optionsSingleSelection'),
                multipleSelection: Anthracite.dictionary.get('optionsMultipleSelection')
            });
        }
    }
});
