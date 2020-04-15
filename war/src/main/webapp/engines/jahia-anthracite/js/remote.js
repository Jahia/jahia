/**
* This object concerns the Remote Mode in Jahia.
 * @memberof Anthracite
 * @property {object} config Configuration settings for Remote Mode
 * @property {object} data Stores data for Object
 * @property {method} onOpen Callback executed when switching to the Remote Mode
 * @property {method} onClose Callback executed when leaving the Remote Mode
 * @namespace Anthracite.remote
 * @type {object}
 */
 Anthracite.addModule("remote", {
     /**
      * XXX
      * @memberof Anthracite.remote
      * @property {boolean} chrome XXX
      * @type {object}
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    config: {
        chrome: true
    },
    /**
     * Stores data for Object
     * @memberof Anthracite.remote
     * @property {object} history XXX
     * @property {boolean} history.settingspage XXX
     * @property {boolean} history.editpage XXX
     * @property {object} search XXX
     * @property {boolean} search.refreshButtonClasslist XXX
     * @property {boolean} search.emptyGridStyle XXX
     * @property {boolean} search.status XXX
     * @property {boolean} returnURL XXX
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
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
      * @memberof Anthracite.remote
      * @method onOpen
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpen: function () {
        Anthracite.dev.log('::: APP ::: REMOTE ::: ONOPEN');

        // Add Background mask used for modals
        if(!jGet.class('background-mask').exists()){
          var backGroundMask = document.createElement('div');

          backGroundMask.classList.add('background-mask');
          jGet.getCached('body').append(backGroundMask);

        }

        // Add Publication Status Bar
        if (!jGet.class('publication-status').exists()) {
            // Create div for publication status of page / slected element because currently it is a pseudo element and we cant reposition when in pinned mode
            var publicationStatus = document.createElement('div'),
                status = (Anthracite.iframe.data.publication && Anthracite.iframe.data.publication.status) ? Anthracite.iframe.data.publication.status : 'unknown';

            publicationStatus.classList.add('publication-status');
            publicationStatus.setAttribute('data-publication-status', status);

            jGet.getCached('body').prepend(publicationStatus);
        }


        Anthracite.data.currentApp = 'edit';
        jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
        jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

        jGet('.window-side-panel > .x-panel-bwrap > div:nth-child(2).x-panel-footer').addClass('side-panel-pin');
        jGet.getCached('body').setAttribute('data-indigo-styled-combos', 'true');
        jGet.getCached('body').setAttribute('data-indigo-sidepanel-pinned', 'false');
        Anthracite.edit.sidepanel.data.pinned = false;
        Anthracite.edit.data.returnURL = window.location.pathname;

        // Reset History
        Anthracite.edit.history.reset();

        Anthracite.edit.topbar.build();

        // Set attributes to be used by CSS
        jGet.getCached('body')
            .setAttribute('data-edit-window-style', 'default')
            .setAttribute('data-INDIGO-GWT-SIDE-PANEL', '')
            .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'yes');

        // Setup the alternative channels system
        Anthracite.edit.sidepanel.initChannels();

        Anthracite.common.resizeLanguageInput();

        if (jGet.id('JahiaGxtSidePanelTabs').exists()) {
            jGet.id('JahiaGxtSidePanelTabs').nodes[0].style.setProperty('width', '245px', 'important');
            jGet.getCached('body').setAttribute('data-indigo-gwt-side-panel', '');
        }
        jGet('.mainmodule > div:nth-child(2) > div:not(.x-abs-layout-container)').nodes[0].setAttribute('style', 'height:100vh !important; transform: translateY(-109px) !important;');
    },
    /**
     * Callback executed when leaving the Remote Mode
     * @memberof Anthracite.remote
     * @method onClose
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onClose: function () {

    },
});
