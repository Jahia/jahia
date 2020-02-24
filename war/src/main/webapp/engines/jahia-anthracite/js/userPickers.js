/**
* This object concerns the User Pickers in Jahia.
 * @memberof Anthracite
 * @property {object} users XXX <br> See {@link Anthracite.userPickers.users}
 * @namespace Anthracite.userPickers
 * @type {object}
 */
 Anthracite.addModule("userPickers", {
    /**
    * Everything here concerns User Pickers
    * @memberof Anthracite.userPickers
    * @property {object} data XXX
    * @property {method} onInput Callback executed when the value of text input changes
    * @property {method} onInputFocus Callback executed when the text input receives focus
    * @property {method} onInputBlur Callback executed when the user search field loses focus
    * @property {method} clearSearch Callback executed when the user clicks the clear button
    * @property {method} onOpen Callback executed when the Select Users modal opens
    * @namespace Anthracite.userPickers.users
    * @type {object}
    * @example
    *
    *
    * Add Example here ...
    *
    *
    */
    users: {
        /**
         * Data Storage
         * @memberof Anthracite.userPickers.users
         * @type {object}
         * @property {object} closeInterval XXX
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        data: {
            closeInterval: null
        },
         /**
          * Callback executed when the value of text input changes
          *  - By looking at its value we can hide / show the clear button accordingly
          * @memberof Anthracite.userPickers.users
          * @method onInput
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onInput: function () {
            // If search text field is empty, remove the clear button
            if (this.value == '') {
                jGet.class('indigo-clear-button').addClass('indigo-empty-field');
            } else {
                jGet.class('indigo-clear-button').removeClass('indigo-empty-field');
            }
        },
        /**
         * Callback executed when the text input receives focus
         *  - By looking at its value we can hide / show the clear button accordingly
         * @memberof Anthracite.userPickers.users
         * @method onInputFocus
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onInputFocus: function () {
            // Expand the search field
            jGet.class('indigo-search-component').addClass('indigo-show');

            // If the search input value is empty add the clear button
            if (this.value == '') {
                jGet.class('indigo-clear-button').addClass('indigo-empty-field');
            }
        },
        /**
         * Callback executed when the user search field loses focus
         * @memberof Anthracite.userPickers.users
         * @method onInputBlur
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onInputBlur: function () {
            // Close the search panel ( if the search input field is empty )
            var searchString = this.value;

            if (searchString == '') {
                // The value of the search input is empty, so close.
                // However, do not close immediately - because the user may actually be clicking the clear button, so...
                // Use an interval timer that closes the search panel after a split second. If the user clicks on the clear button
                // before this time has passed , we can cancel the interval and therefore keep the panel open

                Anthracite.userPickers.users.data.closeInterval = setInterval(function () {
                    jGet.class('indigo-search-component').removeClass('indigo-show');

                    clearInterval(Anthracite.userPickers.users.data.closeInterval);
                }, 150);
            }
        },
        /**
         * Callback executed when the user clicks the clear button
         * @memberof Anthracite.userPickers.users
         * @method clearSearch
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        clearSearch: function () {
            // Clear the timer (if any ) that hides the search panel
            clearInterval(Anthracite.userPickers.users.data.closeInterval);

            // Get elements
            var searchInput = jGet.id('JahiaGxtUserGroupSelect').filter('.indigo-search-input').nodes[0];
            var searchButton = jGet.id('JahiaGxtUserGroupSelect').filter('.indigo-search-button > table');

            // Set classes for display
            jGet.class('indigo-clear-button').addClass('indigo-empty-field');
            jGet.class('indigo-search-component').addClass('indigo-show');

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
         * @memberof Anthracite.userPickers.users
         * @method onOpen
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onOpen: function () {
            jGet.node(this).filter('.x-panel-tbar .x-toolbar-left > table').addClass('indigo-search-component');

            jGet.class('indigo-search-component').filter('.x-toolbar-cell:nth-child(1)').addClass('indigo-clear-button');
            jGet.class('indigo-search-component').filter('.x-toolbar-cell:nth-child(2) input').addClass('indigo-search-input');
            jGet.class('indigo-search-component').filter('.x-toolbar-cell:nth-child(3)').addClass('indigo-search-button');

            jGet.class('indigo-search-component')
                .onInput({
                    target: '.indigo-search-input',
                    callback: Anthracite.userPickers.users.onInput,
                    uid: "INDIGO-SEARCH-COMPONENT"
                })
                .onClick({
                    target: '.indigo-clear-button',
                    callback: Anthracite.userPickers.users.clearSearch,
                    uid: "INDIGO-SEARCH-COMPONENT"
                })
                .onFocus({
                    target: '.indigo-search-input',
                    callback: Anthracite.userPickers.users.onInputFocus,
                    uid: "INDIGO-SEARCH-COMPONENT"
                })
                .onBlur({
                    target: '.indigo-search-input',
                    callback: Anthracite.userPickers.users.onInputBlur,
                    uid: "INDIGO-SEARCH-COMPONENT"
                });

            // Remove the clear button on init
            jGet.class('indigo-clear-button').addClass('indigo-empty-field');

            jGet.id('JahiaGxtUserGroupSelect')
                .onOpen({
                    target: '.x-grid-empty',
                    callback: function () {
                        jGet.id('JahiaGxtUserGroupSelect').addClass('indigo-no-results');
                    },
                    uid: "INDIGO-SEARCH-COMPONENT"
                })
                .onClose({
                    target: '.x-grid-empty',
                    callback: function () {
                        jGet.id('JahiaGxtUserGroupSelect').removeClass('indigo-no-results');
                    },
                    uid: "INDIGO-SEARCH-COMPONENT"
                });
        }
    }
});
