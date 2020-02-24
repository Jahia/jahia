/**
* This object concerns the Publication Dashboard in Jahia.
 * @memberof Anthracite
 * @property {object} config Configuration settings for Publication Dashboard
 * @property {object} data Stores data for Object
 * @property {method} onOpen XXX
 * @property {method} onChange XXX
 * @property {method} onClose XXX
 * @property {method} setBetaStyle XXX
 * @namespace Anthracite.dashboard
 * @type {object}
 */
 Anthracite.addModule("dashboard", {
     /**
      * XXX
      * @memberof Anthracite.dashboard
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
     * @memberof Anthracite.dashboard
     * @property {boolean} beta XXX
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    data: {
        beta: false
    },
     /**
      * Callback executed when switching to the Dashboard
      * @memberof Anthracite.dashboard
      * @method onOpen

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpen: function () {
        Anthracite.dev.log('::: APP ::: DASHBOARD ::: OPENED');

        jGet.getCached('body').setAttribute('data-indigo-styled-combos', 'true');

        // Set attributes to be used by CSS
        jGet.getCached('body')
            .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'no')
            .setAttribute('data-INDIGO-GWT-SIDE-PANEL', 'open');
    },
     /**
      * Callback executed when the users navigates in Dashboard
      * @memberof Anthracite.dashboard
      * @method onChange

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onChange: function () {
        if (Anthracite.dashboard.data.beta) {
            jGet.getCached('body').setAttribute('data-INDIGO-BETA', 'true');
            Anthracite.dashboard.setBetaStyle();

        } else {
            jGet.getCached('body').setAttribute('data-INDIGO-BETA', '');
            Anthracite.edit.sidepanel.buildSplitter();
            Anthracite.edit.sidepanel.resizeSidePanel();
        }
    },
     /**
      * Callback executed when leaving the Dashboard
      * @memberof Anthracite.dashboard
      * @method onClose

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onClose: function () {},
     /**
      * Set Beta Styling
      * @deprecated BETA STYLING HAS BEEN REMOVED - THIS CAN BE DELETED BUT NEED TO REMOVE CALLS TO THIS FUNCTION TOO
      * @memberof Anthracite.dashboard
      * @method onClose

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    setBetaStyle: function () {}
});
