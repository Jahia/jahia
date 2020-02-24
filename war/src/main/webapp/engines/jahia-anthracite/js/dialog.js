/**
* This object concerns the Publication Dashboard in Jahia.
 * @memberof Anthracite
 * @property {method} onOpen Callback executed when the dialog is opened
 * @namespace Anthracite.dialog
 * @type {object}
 */
 Anthracite.addModule("dialog", {
     /**
      * Callback executed when the dialog is opened
      * @memberof Anthracite.dialog
      * @method onOpen

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpen: function(){
        // Scrolls the Dialog to the top when it opens
        var dialog = this;

        setTimeout(function(){
            dialog.scrollTop = 0
        }, 50)
    }
});
