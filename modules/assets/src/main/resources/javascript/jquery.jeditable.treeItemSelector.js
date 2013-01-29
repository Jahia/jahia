$.editable.addInputType('treeItemSelector', {
    /* create input element */
    element : function(settings, original) {
        var input = $('<a href="#treeItemSelectorJeditable-treeItemSelector" id="treeItemSelectorJeditable-treeItemSelectorTrigger">'+settings.selectorLabel+'</a>' +
            '<div style="display:none"><div id="treeItemSelectorJeditable-treeItemSelector"><ul id="treeItemSelectorJeditable-treeItemSelectorTree"></ul></div></div>' +
            '<input id="treeItemSelectorJeditable-input" readonly="true"/>' +
            '<div id="treeItemSelectorJeditable-preview"></div><input type="hidden" id="treeItemSelectorJeditable" readonly="true"/>');
        $(this).append(input);
        return(input);
    },

    content : function(string, settings, original) {
        /* do nothing */
    },

    submit: function (settings, original) {
    //    $("#datetimepicker_", this).val(jQuery.trim($("#datetimepicker_", this).val()).replace(" ", "T"));
    },
    /* attach 3rd party plugin to input element */
    plugin : function(settings, original) {
        var form = this;
        settings.onblur = null;
        var baseURL = settings.baseURL;
        var nodeTypes = settings.nodeTypes;
        var selectableNodeTypes = settings.selectableNodeTypes;
        var root = settings.root;
        var preview = settings.preview;
        var previewPath = settings.previewPath;
        $("#treeItemSelectorJeditable-treeItemSelectorTrigger").fancybox($.extend({
            autoDimensions: false,
            height: 400,
            width: 350,
            hideOnOverlayClick: false,
            hideOnContentClick: false,
            onClosed : function() {
                $("#treeItemSelectorJeditable-treeItemSelectorTree").empty();
            },
            onComplete: function () {
                var queryString = (nodeTypes.length > 0 ? "nodeTypes=" + encodeURIComponent(nodeTypes) : "") +
                                  (selectableNodeTypes.length > 0 ? "&selectableNodeTypes=" +
                                                                    encodeURIComponent(selectableNodeTypes) : "");
                queryString = queryString.length > 0 ? "?" + queryString : "";
                $("#treeItemSelectorJeditable-treeItemSelectorTree").treeview($.extend({
                    urlBase: baseURL,
                    urlExtension: ".tree.json" + queryString,
                    urlStartWith: baseURL + root + ".treeRootItem.json" + queryString,
                    url: baseURL + root + ".treeRootItem.json" + queryString,
                    preview: preview,
                    previewPath: previewPath,
                    callback: function (uuid, path, title) {
                        $("#treepreview").remove();
                        $("#treeItemSelectorJeditable").val(uuid);
                        $("#treeItemSelectorJeditable-input").val(title);
                        if(preview == "true") {
                            $("#treeItemSelectorJeditable-preview").html("<img src='" + previewPath+path + "' alt='"+title+"' />");
                        }
                        $.fancybox.close();
                    }
                }, 'null'));
            }
        }, 'null'));
        $("#treeItemSelectorJeditable-treeItemSelectorTrigger").click();
    }
});