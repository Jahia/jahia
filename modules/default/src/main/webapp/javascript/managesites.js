
function deleteSite() {
    if ($(".sitecheckbox:checked").length == 0) {
        $("#nothing-selected").dialog({
             resizable:false,
             height:180,
             modal:true,
             zIndex:1200,
             buttons:{
                 "Ok":function () {
                     $(this).dialog("close");
                 }
             }
         });
        return;
    }

    $(".addedInput").remove();

    $(".sitecheckbox:checked").each(function (index) {
        $('<input class="addedInput" type="hidden" name="sitebox" value="'+$(this).attr("name")+'">').appendTo("#deleteSiteForm")
    });

    $("#dialog-delete-confirm").dialog({
        resizable:false,
        height:180,
        modal:true,
        zIndex:1200,
        buttons:{
            "Ok":function () {
                $(this).dialog("close");
                showLoading();
                $('#deleteSiteForm').ajaxSubmit(function() {
                    window.location.reload();
                })
            },
            "Cancel":function () {
                $(this).dialog("close");
            }
        }
    });
}

function editProperties(id) {
    showLoading();
    $('#editSiteForm'+id).ajaxSubmit({
        dataType: "json",
        success: function(response) {
            if (response.warn != undefined) {
                alert(response.warn);
                hideLoading();
            } else {
                window.location.reload();
            }
        },
        error: function(response) {
            hideLoading();
        }
    });
}

function createSite() {
    showLoading();
    $('#webProjectCreationForm').ajaxSubmit({
        dataType: "json",
        success: function(response) {
            if (response.warn != undefined) {
                alert(response.warn);
                hideLoading();
            } else {
                window.location.reload();
            }
        },
        error: function(response) {
            hideLoading();
        }
    });
    return true;
}

function exportSite(url,live) {
    if ($(".sitecheckbox:checked").length == 0) {
        $("#nothing-selected").dialog({
             resizable:false,
             height:180,
             modal:true,
             zIndex:1200,
             buttons:{
                 "Ok":function () {
                     $(this).dialog("close");
                 }
             }
         });
        return;
    }

    $(".addedInput").remove();

    if ($(".sitecheckbox:checked").length == 1) {
        name = $(".sitecheckbox:checked").attr("name");
        url = url.replace("/cms/export/default/sites","/cms/export/default/"+name);
    }
    $(".sitecheckbox:checked").each(function (index) {
        $('<input class="addedInput" type="hidden" name="sitebox" value="'+$(this).attr("name")+'">').appendTo("#exportForm")
    });
    $('#exportForm input[name=live]').val(live);
    $('#exportForm').attr("action",url);
    $('#exportForm').submit();
}

function showLoading() {
    $('.loading').show();
    $(".loading").appendTo("body")
}

function hideLoading() {
    $('.loading').each(function () {
        $(this).hide();
    });
}
