/**
* This object concerns the Workflow Dashboard in Jahia.
 * @memberof Anthracite
 * @property {object} data Stores data for Object
 * @property {object} dashboard XXX <br> See {@link Anthracite.workflow.dashboard}
 * @namespace Anthracite.workflow
 * @type {object}
 */
 Anthracite.addModule("workflow", {
     /**
      * XXX
      * @memberof Anthracite.workflow
      * @property {boolean} opened XXX
      * @type {object}
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
     * XXX
     * @memberof Anthracite.workflow
     * @property {method} onOpen Callback executed when the Workflow Dashboard is opened
     * @namespace Anthracite.workflow.dashboard
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    dashboard: {
        /**
         * Callback executed when the Workflow Dashboard is opened
          * @memberof Anthracite.workflow.dashboard
          * @method onOpen
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        onOpen: function () {
            Anthracite.dev.log('::: APP ::: WORKFLOW ::: DASHBOARD ::: ONOPEN');
            jGet('.workflow-dashboard-engine .x-tool-maximize').trigger('click');
        }
    }
});
