/**
* This object concerns the iframe that the Edit Mode is displayed in Jahia.
 * @memberof Anthracite
 * @property {object} data XXX
 * @property {method} onChangeSRC Callback executed when the iframe in Edit Mode changes URL
 * @property {method} onChange Callback executed when the iframe changes SRC
 * @property {method} onSelect Callback executes when the user selects a node in the Edit Mode window
 * @property {method} clearSelection Callback executes when the selected node(s) in the Edit Mode window clear
 * @property {method} disableClicks Disables clicks in the iframe in Edit Mode
 * @namespace Anthracite.iframe
 * @type {object}
 * @example
 *
 *
 * Add Example here ...
 *
 *
 */
 Anthracite.addModule("iframe", {
     /**
      * Stores data for Object
      * @memberof Anthracite.iframe
      * @property {integer} previousUrl XXX
      * @property {boolean} currentUrl XXX
      * @property {boolean} displayName XXX
      * @property {integer} selectionCount XXX
      * @property {string} bodyStyle XXX
      * @type {object}
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    data: {
        previousUrl: -1,
        currentUrl: null,
        displayName: null,
        selectionCount: 0,
        bodyStyle: null
    },
     /**
      * Callback executed when the iframe in Edit Mode changes URL
      *  - Listens to the SRC attribute on .window-iframe
      * @memberof Anthracite.iframe
      * @method onChangeSRC
      * @param {string} attrKey XXX
      * @param {string} attrValue XXX
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onChangeSRC: function (attrKey, attrValue) {
        Anthracite.dev.log('::: APP ::: IFRAME ::: ONCHANGESRC [src=\'' + attrValue + '\' ::: currentApp=\'' + Anthracite.data.currentApp + '\']');

        Anthracite.iframe.data.previousUrl = Anthracite.iframe.data.currentUrl;
        Anthracite.iframe.data.currentUrl = attrValue;

        if (Anthracite.data.currentApp == 'edit') {
            // TEMP BLIND
            Anthracite.edit.sidepanel.onNewChannel();

            // A new page has been loaded in the Edit Window Iframe
            // If it is NOT a settings page then we need to save the URL so we can use
            // it as a return URL when coming back from Settings ( only if not page can be found )
            if (!Anthracite.edit.settings.data.opened) {
                // Not a settings page so we can save the URL
                // Note that we have modify the URL because the iframe is slightly different from the Edit Mode URL
                Anthracite.edit.data.returnURL = attrValue.replace('/cms/editframe/', '/cms/edit/');
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

            jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
            jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

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
      * Utility function to unescape html
      * @param str
      */
     unEscapeHTML: str => {
         // this prevents any overhead from creating the object each time
         const element = document.createElement('div');

         function decodeHTMLEntities (str) {
             if(str && typeof str === 'string') {
                 // strip script/html tags
                 str = str.replace(/<script[^>]*>([\S\s]*?)<\/script>/gmi, '');
                 str = str.replace(/<\/?\w(?:[^"'>]|"[^"]*"|'[^']*')*>/gmi, '');
                 element.innerHTML = str;
                 str = element.textContent;
                 element.textContent = '';
             }

             return str;
         }

         return decodeHTMLEntities(str);
     },
     /**
      * Utility function to escape html
      * @param str
      */
     escapeHTML: str => str.replace(/[&<>'"]/g,
         tag => ({
             '&': '&amp;',
             '<': '&lt;',
             '>': '&gt;',
             "'": '&#39;',
             '"': '&quot;'
         }[tag])),
     /**
      * Callback executed when the iframe changes SRC
      *  - Listens to the data-main-node-displayname attribute on the body tag
      * @memberof Anthracite.iframe
      * @method onChange
      * @param {string} attrKey XXX
      * @param {string} attrValue XXX
      * @returns {boolean|undefined} XXX
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onChange: function (attrKey, attrValue) {
        if(Anthracite.data.currentApp == 'edit'){
            jGet.getCached('body').setAttribute("data-edit-mode-status", "initialised");

            if(Anthracite.edit.sidepanel.data.pinned){
                if( Anthracite.edit.sidepanel.data.previousTab === "JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab" ||
                    Anthracite.edit.sidepanel.data.previousTab === "JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab" ||
                    Anthracite.edit.sidepanel.data.previousTab === "JahiaGxtSidePanelTabs__JahiaGxtSearchTab" ||
                    Anthracite.edit.sidepanel.data.previousTab === "JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab"){
                        jGet.id(Anthracite.edit.sidepanel.data.previousTab).trigger("mousedown").trigger("mouseup");
                }
            }

        }

        if (Anthracite.iframe.data.displayName == attrValue || Anthracite.data.currentApp == 'studio') {
            return false;
        }

        Anthracite.dev.log('::: APP ::: IFRAME ::: ONCHANGE: ' + Anthracite.data.currentApp);
        Anthracite.iframe.data.displayName = Anthracite.iframe.escapeHTML(attrValue);

        switch (Anthracite.data.currentApp) {
            case 'edit':
                // Need to update the header bar
                Anthracite.edit.topbar.build();
                if (Anthracite.edit.sidepanel.isOpen()) {
                    Anthracite.iframe.disableClicks();
                }
                break;
            case 'contribute':
                // Need to update the header bar
                Anthracite.contribute.topbar.build();
                break;
            case 'dashboard':
                Anthracite.dashboard.onChange();
                break;
        }
    },
     /**
      * Callback executes when the user selects a node in the Edit Mode window
      * @memberof Anthracite.iframe
      * @method onSelect
      * @param {string} attrKey XXX
      * @param {string} attrValue XXX
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onSelect: function (attrKey, attrValue) {
        Anthracite.dev.log('::: APP ::: IFRAME ::: ONSELECT [attrValue=\'' + attrValue + '\']');

        var count = parseInt(attrValue);
        // Refresh the title of the page accordingly
        switch (Anthracite.data.currentApp) {
            case 'edit':
                Anthracite.iframe.data.selectionCount = count;
                // Need to update the header bar
                Anthracite.edit.topbar.build();
                if (Anthracite.edit.sidepanel.isOpen()) {
                    Anthracite.iframe.disableClicks();
                }
                break;
            case 'contribute':
                Anthracite.iframe.data.selectionCount = count;
                // Need to update the header bar
                Anthracite.contribute.topbar.build();
                break;
        }
    },
     /**
      * Callback executes when the selected node(s) in the Edit Mode window clear
      * @memberof Anthracite.iframe
      * @method clearSelection
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    clearSelection: function () {
        Anthracite.dev.log('::: APP ::: IFRAME ::: CLEARSELECTION');

        jGet.class('window-iframe').trigger('click');
    },
     /**
      * Disables clicks in the iframe in Edit Mode
      *  - This is used when the side panel is opened. That way a user can click outside the side panel to close it without selecting nodes in the Edit Mode iframe window
      * @memberof Anthracite.iframe
      * @method disableClicks
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    disableClicks: function () {
        Anthracite.dev.log('::: APP ::: IFRAME ::: DISABLECLICKS');

        if (jGet.getCached('body').getAttribute('data-indigo-gwt-side-panel') == 'open' &&
            jGet.getCached('body').getAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL') == 'yes' &&
            jGet.getCached('body').getAttribute('data-sitesettings') == 'false' &&
            jGet.getCached('body').getAttribute('data-indigo-sidepanel-pinned') != 'true') {

            // SAVE the curent style properties of the iframes body tag so we can revert to it once the side panel is closed.
            var iframeBody = jGet.iframe('.window-iframe').filter('body');

            iframeBody.nodes[0].style.pointerEvents = 'none';
        }
    }
});
