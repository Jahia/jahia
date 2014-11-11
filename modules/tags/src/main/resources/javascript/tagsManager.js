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

function bbRenameTag(oldName) {
    bootbox.dialog({
        title: "<h3>" + jsVarMap.labelRename + " : " + oldName + "<h3>",
        message: "<p>" + jsVarMap.labelTagNewName +
                 "</p><input id='renameTag' class='typeahead' type='text' value='" + oldName + "'>" +
                 "<script>" +
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
                 jsVarMap.modalRenameAll +
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
                    $("#eventInput").attr("name", "_eventId_renameAllTags");
                    $("#selectedTag").val(oldName);
                    $("#tagNewName").val($("#renameTag").val());
                    $("#formTagsManagement").submit();
                }
            }
        }
    });
}

function bbDeleteTag(selectedTag) {
    bootbox.dialog({
        title: "<h3>" + jsVarMap.labelDelete + " : " + selectedTag + "<h3>",
        message: "<p>" + jsVarMap.modalDeleteAll + "</p>",
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
                    $("#eventInput").attr("name", "_eventId_deleteAllTags");
                    $("#selectedTag").val(selectedTag);
                    $("#formTagsManagement").submit();
                }
            }
        }
    });
}

function viewUsages(selectedTag) {
    callWorkInProgress();
    $("#eventInput").attr("name", "_eventId_viewTagUsages");
    $("#selectedTag").val(selectedTag);
    $("#formTagsManagement").submit();
}