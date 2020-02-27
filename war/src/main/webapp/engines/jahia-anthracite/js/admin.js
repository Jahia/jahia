/**
 * This object concerns the Administration screens in Jahia.
 * @memberof Anthracite
 * @property {object} config Configuration settings for Admininstration screen
 * @property {object} data Stores data for Object
 * @property {object} sidepanel Controls the side panel in Admin Mode <br> See {@link Anthracite.admin.sidepanel}
 * @property {method} onOpen Callback executes when switching to Admin Mode
 * @property {method} onClose Callback executes when leaving the Admin Mode
 * @namespace Anthracite.admin
 * @type {object}
 */
 Anthracite.addModule("admin", {
     /**
      * XXX
      * @memberof Anthracite.admin
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
     * @memberof Anthracite.admin
     * @property {boolean} firstLoadSettingsType XXX
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    data: {
        firstLoadSettingsType: null
    },
    /**
     * Controls the side panel in Admin Mode
     * @memberof Anthracite.admin
     * @property {method} toggleSiteSettingsMenu Toggle the display of the System Site Settings Menu
     * @property {object} row Methods and properties for the menu items in the side panel <br> See {@link Anthracite.admin.sidepanel.row}
     * @namespace Anthracite.admin.sidepanel
     * @type {object}
     */
    sidepanel: {
         /**
          * Toggle the display of the System Site Settings Menu
          * @memberof Anthracite.admin.sidepanel
          * @method toggleSiteSettingsMenu
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        toggleSiteSettingsMenu: function(e){
            var clickedElement = jGet.node(e.target),
                toggleMenu = clickedElement.hasClass('x-panel');

            if(toggleMenu){
                clickedElement.toggleClass('open-sub-menu')
            }
        },
        /**
         * Methods and properties for the menu items in the side panel
         * @memberof Anthracite.admin.sidepanel
         * @property {method} onClick Callback executes when user clicks on a navigation element in the side panel
         * @namespace Anthracite.admin.sidepanel.row
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        row: {
            /**
            * Callback executes when user clicks on a navigation element in the side panel
            * @memberof Anthracite.admin.sidepanel.row
            * @method onClick
            * @example
            *
            *
            * Add Example here ...
            *
            *
            */
            onClick: function () {
                // Because the two menus are simultaneously visible now, we need to deselect the previously selected row when selecting a page from the other menu group.
                jGet('.x-viewport-adminmode .x-grid3 .x-grid3-row.x-grid3-row-selected').removeClass('x-grid3-row-selected');
                jGet.node(this).addClass('x-grid3-row-selected');
            }
        }
    },
     /**
     * Callback executes when switching to Admin Mode
     * @memberof Anthracite.admin
     * @method onOpen
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onOpen: function () {
        Anthracite.dev.log('::: APP ::: ADMIN ::: OPENED');

        // Reset incase user is coming in from another app
        Anthracite.admin.data.firstLoadSettingsType = null;

        Anthracite.edit.sidepanel.buildSplitter();
        Anthracite.edit.sidepanel.resizeSidePanel();

        jGet.getCached('body').setAttribute('data-indigo-styled-combos', 'true');

        // Set attributes to be used by CSS
        jGet.getCached('body')
            .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'no')
            .setAttribute('data-INDIGO-GWT-SIDE-PANEL', 'open');
    },
     /**
     * Callback executes when leaving the Admin Mode
     * @memberof Anthracite.admin
     * @method onClose
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onClose: function () {}
});
