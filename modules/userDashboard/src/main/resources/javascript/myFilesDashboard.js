/**
 * Created by dgaillard on 19/03/14.
 */

function bbShowVideo(name, path, type){
    bootbox.alert("<h1>" + name + "</h1><br />" + "<video width='320' height='240' controls><source src='" + path + "' type='" + type + "'></video><br /><p>" + myFilesVideo1 + "&nbsp;<a href='" + path + "' download>" + myFilesVideo2 + "</a>&nbsp;" + myFilesVideo3 + "</p>", function(){});
}

function bbShowAudio(name, path, type){
    bootbox.alert("<h1>" + name + "</h1><br />" + "<audio controls><source src='" + path + "' type='" + type + "'></audio><br /><p>" + myFilesAudio1 + "&nbsp;<a href='" + path + "' download>" + myFilesAudio2 + "</a>&nbsp;" + myFilesAudio3 + "</p>", function(){});
}

function bbShowImage(name, path, width, height){
    bootbox.alert("<h1>" + name + "</h1><br />" + "<img src='" + path + "' alt='" + name + "' height='" + height + "' width='" + width + "'>", function(){});
}

function addInputForAddFile(){
    addFileIndex ++;
    $('#fileFormUpload').append("<input type='file' name='file' id='file" + addFileIndex + "' /><br />");
}

function bbDelete(name, id){
    bootbox.dialog({
        message: "<p>" + myFilesDeleteBox + "&nbsp;" + name + " ?</p>",
        title: labelDelete + "&nbsp;:&nbsp;" + name,
        buttons: {
        danger: {
            label: labelCancel,
                className: "btn-danger",
                callback: function() {}
        },
        success: {
            label: labelDelete,
                className: "btn-success",
                callback: function() {
                    $.ajax({
                        url: apiPath + '/nodes/' + id,
                        type: 'DELETE',
                        success: function(){
                            window.location.reload();
                        },
                        error: function(result){
                            bootbox.alert(myFilesDeleteError + "&nbsp;:&nbsp;" + name + "<br />" + result.responseJSON.localizedMessage, function() {});
                        }
                    })
                }
            }
        }
    });
}

function bbRenameFolder(name, id){
    bootbox.dialog({
        message: "<label>" + labelNewDirName + "&nbsp;:&nbsp;</label><input type='text' id='renameFolder'/>",
        title: labelRename + " : " + name,
        buttons: {
        danger: {
            label: labelCancel,
                className: "btn-danger",
                callback: function() {}
        },
        success: {
            label: labelRename,
                className: "btn-success",
                callback: function() {
                    var regex = /[:<>[\]*|"\\]/;

                    if(!regex.test($('#renameFolder').val())){
                        $.ajax({
                            url: apiPath + '/nodes/' + id + '/moveto/' + $('#renameFolder').val(),
                            type: 'POST',
                            contentType: 'application/json',
                            success: function(){
                                window.location.reload();
                            },
                            error: function(result){
                                bootbox.alert("<h1>" + labelError + "&nbsp;!</h1><br />" + myFilesRenameFolderError + "&nbsp;:<br /><br />" + result.responseJSON.localizedMessage);
                            }
                        })
                    }else{
                        bootbox.alert("<h1>" + labelError + "&nbsp;!</h1><br />" + myFilesRenameErrorCharacters);
                    }
                }
            }
        }
    });
}

function bbRenameFile(name, id){
    bootbox.dialog({
        message: "<label>" + labelNewName + "&nbsp;:&nbsp;</label><input type='text' id='renameFile'/>",
        title: labelRename + " : " + name,
        buttons: {
        danger: {
            label: labelCancel,
                className: "btn-danger",
                callback: function() {}
        },
        success: {
            label: labelRename,
                className: "btn-success",
                callback: function() {
                    var regex = /[:<>[\]*|"\\]/;
                    var fileExt = '';

                    if(name.split('.').pop() != name){
                        fileExt = '.' + name.split('.').pop();
                    }

                    if(!regex.test($('#renameFile').val())){
                        $.ajax({
                            url: apiPath + '/nodes/' + id + '/moveto/' + $('#renameFile').val() + fileExt,
                            type: 'POST',
                            contentType: 'application/json',
                            success: function(){
                                window.location.reload();
                            },
                            error: function(result){
                                bootbox.alert("<h1>" + labelError + "&nbsp;!</h1><br />" + myFilesRenameFileError + "&nbsp;:<br /><br />" + result.responseJSON.localizedMessage);
                            }
                        })
                    }else{
                        bootbox.alert("<h1>" + labelError + "&nbsp;!</h1><br />" + myFilesRenameErrorCharacters);
                    }
                }
            }
        }
    });
}

function endAddFile(fileName, addFileIndex, status, messageError){
    index += 1;
    if(fileName != ''){
        fileUp.push([fileName, status, messageError]);
    }
    if(index == addFileIndex+1){
        var table = "<table class='table table-hover table-bordered'><thead><tr><th>" + labelName + "</th><th>" + labelStatus + "</th><th>" + labelMessage + "</th></tr></thead><tbody>";
        for(var j = 0 ; j < fileUp.length ; j++){
            if(fileUp[j][1] == 'error'){
                table += "<tr><td>" + fileUp[j][0] + "</td><td><span class='label label-important'>" + labelError + "</span></td><td>" + fileUp[j][2] + "</td></tr>";
            }else{
                table += "<tr><td>" + fileUp[j][0] + "</td><td><span class='label label-success'>" + labelOK + "</span></td><td>" + fileUp[j][2] + "</td></tr>";
            }
        };
        table += "</tbody></table>";
        bootbox.alert("<h1>" + myFilesUploadedFiles + "</h1><br />" + table, function(){
            window.location.reload();
        });
    }
}

function bbAddFile(){
    bootbox.dialog({
        message: "<label>" + labelAddFile + "&nbsp;:&nbsp;</label><button class='btn btn-primary pull-right' onclick='addInputForAddFile()' ><i class='icon-plus icon-white'></i>&nbsp;" + labelAddFile + "</button><form id='fileFormUpload' enctype='multipart/form-data'><input name='file' type='file' id='file" + addFileIndex + "' /><br /></form><br /><br /><div class='alert alert-info'><h4>" + myFilesAlertInfoCharacters + "&nbsp;:</h4><br />: / \\ | \" < > [ ] * </div>",
        title: labelUploadFile,
        buttons: {
        danger: {
            label: labelCancel,
                className: "btn-danger",
                callback: function() {}
        },
        success: {
            label: labelAdd,
                className: "btn-success",
                callback: function() {
                    for(var i = 0 ; i <= addFileIndex ; i++){
                        if($('#file' + i).val() != ''){
                            $.ajaxFileUpload({
                                url: apiPath + '/paths' + currentNodePath,
                                secureuri:false,
                                fileElementId: 'file' + i,
                                dataType: 'json',
                                success: function(result){
                                    if(result.name){
                                        endAddFile(result.name, addFileIndex, 'success', '');
                                    }else{
                                        endAddFile(result.subElements[0], addFileIndex, 'error', myFilesUploadedFileErrorCharacters + '<br />' + result.message);
                                    }
                                },
                                error: function(result){
                                    endAddFile(result.subElements[0], addFileIndex, 'error', result.message);
                                }
                            });
                        }
                        else{
                            endAddFile('', addFileIndex, '', '');
                        }
                    }
                }
            }
        }
    });
}

function bbAddFolder(id){
    bootbox.dialog({
        message: "<label>" + labelName + "&nbsp;:&nbsp;</label><input type='text' id='nameFolder'/><br /><br /><div class='alert alert-info'><h4>" + myFilesAlertInfoCharacters + "&nbsp;:</h4><br />: / \\ | \" < > [ ] * </div>",
        title: myFilesCreateNewFolder,
        buttons: {
        danger: {
            label: labelCancel,
                className: "btn-danger",
                callback: function() {}
        },
        success: {
            label: labelCreateFolder,
                className: "btn-success",
                callback: function() {
                    var regex = /[:<>[\]*|"\\]/;

                    if(!regex.test($('#nameFolder').val())){
                        $.ajax({
                            url: apiPath + '/nodes/' + id,
                            type: 'PUT',
                            contentType: 'application/json',
                            data: "{\"children\":{\"" + $('#nameFolder').val() + "\":{\"name\":\"" + $('#nameFolder').val() + "\",\"type\":\"jnt:folder\"}}}",
                            success: function(){
                                window.location.reload();
                            },
                            error: function(result){
                                bootbox.alert("<h1>" + labelError + "&nbsp;!</h1><br />" + myFilesCreateFolderError + "&nbsp;:<br /><br />" + result.responseJSON.message);
                            }
                        })
                    }else{
                        bootbox.alert("<h1>" + labelError + "&nbsp;!</h1><br />" + myFilesCreateFolderErrorCharacters);
                    }
                }
            }
        }
    });
}