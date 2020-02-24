/**
* This object concerns Contribute Mode in Jahia.
 * @memberof Anthracite
 * @property {object} config Configuration settings for Contribute Mode
 * @property {object} data XXX
 * @property {method} onOpen XXX
 * @property {method} onClose XXX
 * @property {method} onChangeMode XXX
 * @property {object} topbar XXX <br> See {@link Anthracite.contribute.topbar}
 * @namespace Anthracite.contribute
 * @type {object}
 */
 Anthracite.addModule("contribute", {
     /**
     * XXX
     * @memberof Anthracite.contribute
     * @type {object}
     * @property {boolean} chrome XXX
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
    * XXX
    * @memberof Anthracite.contribute
    * @type {object}
    * @property {boolean} data XXX
    * @example
    *
    *
    * Add Example here ...
    *
    *
    */
    data: {
        mode: null
    },
     /**
      * Callback executed when switching to the Contribute Mode
      * @memberof Anthracite.contribute
      * @method onOpen

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpen: function () {
        Anthracite.dev.log('::: APP ::: CONTRIBUTE ::: OPENED');

        // Set attributes to be used by CSS
        jGet.getCached('body')
            .setAttribute('data-INDIGO-GWT-SIDE-PANEL', '')
            .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'yes')
            .setAttribute('data-indigo-sidepanel-pinned', 'false');

        Anthracite.edit.sidepanel.data.pinned = false;
        Anthracite.contribute.topbar.build();
    },
     /**
      * Callback executed when leaving the Contribute Mode
      * @memberof Anthracite.contribute
      * @method onOpen

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onClose: function () {},
     /**
      * Callback executed when the user navigates in the iframe
      * @memberof Anthracite.contribute
      * @method onChangeMode
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
    onChangeMode: function (attrKey, attrValue) {
        if (Anthracite.data.currentApp != 'contribute') {
            return false;
        }

        var nodePathSplit = attrValue.split('/');
        var modePath = nodePathSplit[3];
        var mode;
        var iframeSRC = jGet.class('window-iframe').getAttribute('src');
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

        Anthracite.contribute.data.mode = mode;
        jGet.getCached('body').setAttribute('data-contribute-mode', Anthracite.contribute.data.mode);

        Anthracite.dev.log('????????????????????????????????????');
        Anthracite.dev.log('CHANGED nodePath: ' + attrValue);
        Anthracite.dev.log('CHANGED section: ' + nodePathSplit[3]);
        Anthracite.dev.log('displayingNode: ' + displayingNode);
        Anthracite.dev.log('????????????????????????????????????');

        Anthracite.contribute.data.displayingNode = displayingNode;

        jGet.getCached('body').setAttribute('data-contribute-displaying-node', Anthracite.contribute.data.displayingNode);
    },

    /**
     * XXX
     * @memberof Anthracite.contribute
     * @property {method} build Builds the top bar in Contribute Mode
     * @property {method} reposition Callback executes when the window resizes
     * @namespace Anthracite.contribute.topbar
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    topbar: {
         /**
          * Builds the top bar in Contribute Mode
          * @memberof Anthracite.contribute.topbar
          * @method build
          * @returns {boolean|string|undefined} XXX
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        build: function () {
            if (!Anthracite.contribute.data.mode) {
                Anthracite.dev.log('::: APP ::: CONTRIBUTE ::: TOPBAR ::: BUILD ( CAN NOT YET BUILD)');
                return false;
            }

            Anthracite.dev.log('::: APP ::: CONTRIBUTE ::: TOPBAR ::: BUILD (MODE: ' + Anthracite.contribute.data.mode + ')');

            // TEMP BLIND
            if (Anthracite.data.currentApp == 'edit' || Anthracite.data.currentApp == 'contribute') {
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

                var pageTitle = Anthracite.iframe.data.displayName;
                var publicationStatus = publicationStatus = document.querySelectorAll('.toolbar-item-publicationstatus .gwt-Image')[0];
                var extractStatus = function (url) {
                    var urlSplit = url.split('/');
                    var fileName = urlSplit[urlSplit.length - 1];
                    var statusSplit = fileName.split('.png');

                    return statusSplit[0];
                };

                if (publicationStatus) {
                    Anthracite.iframe.data.publication = {
                        status: extractStatus(publicationStatus.getAttribute('src')),
                        label: publicationStatus.getAttribute('title')
                    };
                } else {
                    Anthracite.iframe.data.publication = {
                        status: null,
                        label: null
                    };
                }

                elements.title.setAttribute('data-PAGE-NAME', pageTitle);

                Anthracite.dev.log('Anthracite.iframe.data.publication:' + Anthracite.iframe.data.publication);
                Anthracite.contribute.topbar.reposition();
            }
        },
         /**
          * Callback executes when the window resizes
          * @memberof Anthracite.contribute.topbar
          * @method reposition

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        reposition: function () {
            Anthracite.dev.log('::: APP ::: CONTRIBUTE ::: TOPBAR ::: REPOSITION');

            if (jGet.class('toolbar-item-publicationstatus').getAttribute('data-page-name') != null) {
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

                var elements = {
                    body: document.getElementsByTagName('body')[0],
                    title: document.getElementsByClassName('toolbar-item-publicationstatus')[0],
                    // innerTitle: document.getElementsByClassName("node-path-text-inner")[0],
                    publishButton: document.getElementsByClassName('contribute-menu-publication')[0],
                    // refreshButton: document.getElementsByClassName("window-actions-refresh")[0],
                    // nodePathTitle: document.getElementsByClassName("node-path-title")[0],
                    previewButton: document.getElementsByClassName('edit-menu-view')[0],
                    editPage: jGet('.action-toolbar .x-toolbar-cell:nth-child(5) table').getNode(0)
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
                jGet('.contribute-menu-publication .x-btn-mc').setAttribute('data-publication-label', Anthracite.iframe.data.publication.label);

                // Make sure correct class is added to publication button
                elements.publishButton.setAttribute('data-publication-status', Anthracite.iframe.data.publication.status);

                if (jGet.class('publication-status').exists()) {
                    jGet.class("publication-status-path").setHTML(jGet.getCached('body').getAttribute('data-main-node-path'));
                    jGet.class("publication-status-label").setHTML(jahia_gwt_messages["label_publication_" + Anthracite.iframe.data.publication.status]);
                    jGet.class('publication-status').setAttribute('data-publication-status', Anthracite.iframe.data.publication.status);
                }
            }

        }
    }

});
