/**
* This object concerns the Image Preview Modals in Jahia.
 * @memberof Anthracite
 * @property {method} onOpen Callback executed when an image preview is opened from within a picker
 * @property {method} onClose Callback executed when an image preview is closed from within a picker
 * @namespace Anthracite.imagePreview
 * @type {object}
 */
 Anthracite.addModule("imagePreview", {
     /**
      * Callback executed when an image preview is opened from within a picker
      * @memberof Anthracite.imagePreview
      * @method onOpen
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpen: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: IMAGEPREVIEW ::: ONOPEN');
        jGet.getCached('body').setAttribute('data-INDIGO-IMAGE-PREVIEW', 'open');
    },
     /**
      * Callback executed when an image preview is closed from within a picker
      * @memberof Anthracite.imagePreview
      * @method onClose
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onClose: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: IMAGEPREVIEW ::: ONCLOSE');
        jGet.getCached('body').setAttribute('data-INDIGO-IMAGE-PREVIEW', '');
        var sidePanel = jGet.id('JahiaGxtSidePanelTabs').nodes[0];
        sidePanel.style.setProperty('width', '245px', 'important');
    }
});
