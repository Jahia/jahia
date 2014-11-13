function callWorkInProgress(){
    if($.browser.msie == true){
        $.blockUI({ css: {
            border: 'none',
            padding: '15px',
            backgroundColor: '#000',
            '-webkit-border-radius': '10px',
            '-moz-border-radius': '10px',
            opacity: .5,
            color: '#fff'
        }, message: jsVarMap.i18nWaiting });
    } else {
        workInProgress(jsVarMap.i18nWaiting);
    }
}

function backToTagManager() {
    callWorkInProgress();
    $("#eventInput").attr("name", "_eventId_backToTagsList");
    $("#formTagManagement").submit();
}

function bbRenameTag(nodeID) {
    bootbox.dialog({
        title: "<h3>" + jsVarMap.labelRename + " : " + $('#selectedTag').val() + "<h3>",
        message: "<p>" + jsVarMap.labelTagNewName +
            "</p><input id='renameTag' class='typeahead' type='text' value='" + $('#selectedTag').val() + "'>" +
            "<script>" +
                "$('.modal-body').css('overflow', 'visible');" +
                "$('#renameTag').keyup(function() {" +
                    "if ($('#renameTag').val() != '') {" +
                        "$('.renameButton').removeAttr('disabled');" +
                    "} else {" +
                        "$('.renameButton').attr('disabled', 'disabled');" +
                    "}" +
                "});" +
                "$('#renameTag').typeahead(null, {" +
                    "source: tagsSuggester.ttAdapter()" +
                "});" +
            "</script>" +
            "<br /><br /><p>" +
            jsVarMap.modalRename +
            "</p>",
        buttons: {
            danger: {
                label: jsVarMap.labelCancel,
                className: "btn-default",
                callback: function() {}
            },
            success: {
                label: jsVarMap.labelRename,
                className: "btn-primary renameButton",
                callback: function() {
                    callWorkInProgress();
                    $("#eventInput").attr("name", "_eventId_renameTagOnNode");
                    $("#nodeToUpdateId").val(nodeID);
                    $("#tagNewName").val($("#renameTag").val());
                    $("#formTagManagement").submit();
                }
            }
        }
    });
}

function bbDeleteTag(nodeID) {
    bootbox.dialog({
        title: "<h3>" + jsVarMap.labelDelete + " : " + $('#selectedTag').val() + "<h3>",
        message: "<p>" + jsVarMap.modalDelete + "</p>",
        buttons: {
            danger: {
                label: jsVarMap.labelCancel,
                className: "btn-default",
                callback: function() {}
            },
            success: {
                label: jsVarMap.labelDelete,
                className: "btn-danger",
                callback: function() {
                    callWorkInProgress();
                    $("#eventInput").attr("name", "_eventId_deleteTagOnNode");
                    $("#nodeToUpdateId").val(nodeID);
                    $("#formTagManagement").submit();
                }
            }
        }
    });
}