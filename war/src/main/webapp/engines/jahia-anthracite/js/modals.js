/**
* This object concerns Modal Windows in Jahia.
 * @memberof Anthracite
 * @property {method} onOpen Callback executed when a modal opens
 * @property {method} onClose Callback executed when a modal closes
 * @namespace Anthracite.modals
 * @type {object}
 */
 Anthracite.addModule("modals", {
     /**
      * Callback executed when a modal opens
      * @memberof Anthracite.modals
      * @method onOpen
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpen: function(){
        // Get close button
        var closeButton = jGet.node(this).filter('.x-tool-close');

        // Push State
        Anthracite.nav.pushState(closeButton);

        // Create Modal Mask
        // Can no longer use GWT modal with peace of mind, so insert our own one in the popup.
        // It is fairly universal except for pickers, background jobs & workflows which hide it via CSS
        var modalMask = document.createElement('div');
        modalMask.classList.add('indigo-modal-mask');
        jGet.node(this).append(modalMask);
    },
     /**
      * Callback executed when a modal closes
      * @memberof Anthracite.modals
      * @method onClose
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onClose: function () {
        // Get close button
        var closeButton = jGet.node(this).filter('.x-tool-close');
        // Remove state
        // Update the Browser History ( need to see why this has been done, it was regarding a bug fix )
        Anthracite.nav.pullState(closeButton);
    }
});
