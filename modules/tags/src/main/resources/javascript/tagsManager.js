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
        title: "<h3>Rename : " + oldName + "<h3>",
        message: "<p>New name :" +
            "</p><input id='renameTag' type='text'>" +
            "<script>" +
                "$('.renameButton').attr('disabled', 'disabled');" +
                "$('#renameTag').keypress(function() {" +
                    "if ($('#renameTag').val() != '') {" +
                        "$('.renameButton').removeAttr('disabled');" +
                    "} else {" +
                        "$('.renameButton').attr('disabled', 'disabled');" +
                    "}" +
                "});" +
            "</script>",
        buttons: {
            danger: {
                label: jsVarMap.labelCancel,
                className: "btn-default",
                callback: function() {}
            },
            success: {
                label: jsVarMap.labelOk,
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