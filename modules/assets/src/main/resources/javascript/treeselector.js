function jahiaCreateTreeItemSelector(fieldId, displayFieldId, baseUrl, root, nodeTypes, selectableNodeTypes, valueType,
                                     onSelect, onClose, treeviewOptions, fancyboxOptions) {
    $("#" + fieldId + "-treeItemSelectorTrigger").fancybox($.extend({
        autoSize: false,
        height: 400,
        width: 350,
        hideOnOverlayClick: false,
        hideOnContentClick: false,
        afterClose : function() {
            $("#" + fieldId + "-treeItemSelectorTree").remove();

            if (onClose && (typeof onClose == 'function')) {
                onClose();
            }
        },
        afterLoad: function () {
            var queryString = (nodeTypes.length > 0 ? "nodeTypes=" + encodeURIComponent(nodeTypes) : "") + (
                    selectableNodeTypes.length > 0 ? "&selectableNodeTypes=" + encodeURIComponent(selectableNodeTypes) :
                            "");
            queryString = queryString.length > 0 ? "?" + queryString : "";

            var treeItemSelectorTree = $("<ul id='" + fieldId + "-treeItemSelectorTree'></ul>");
            $("#" + fieldId + "-treeItemSelector").append(treeItemSelectorTree);
            treeItemSelectorTree.treeview($.extend({
                urlBase: baseUrl,
                urlExtension: ".tree.json" + queryString,
                urlStartWith: baseUrl + root + ".treeRootItem.json" + queryString,
                url: baseUrl + root + ".treeRootItem.json" + queryString,
                callback: function (uuid, path, title) {
                    var setValue = true;
                    if (onSelect && (typeof onSelect == 'function')) {
                        setValue = onSelect(uuid, path, title);
                    }
                    if (setValue) {
                        document.getElementById(fieldId).value =
                                'title' == valueType ? title : ('identifier' == valueType ? uuid : path);
                        if (displayFieldId.length > 0) {
                            document.getElementById(displayFieldId).value = title;
                            document.getElementById(displayFieldId).innerHTML = title;
                        }
                    }
                    $.fancybox.close();
                }
            }, treeviewOptions));
        }
    }, fancyboxOptions));
}
