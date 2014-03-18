<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>

<template:addResources type="css" resources="admin-bootstrap.css,datatables/css/bootstrap-theme.css,tablecloth.css"/>
<template:addResources type="css" resources="files.css"/>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.blockUI.js,admin-bootstrap.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>
<template:addResources type="javascript" resources="bootbox.min.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>

<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/>
<c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>

<c:set var="apiPath" value="/modules/api/jcr/v1/default/${currentResource.locale}"/>

<template:addResources>
    <script type="text/javascript">

        var addFileIndex = 0;
        var index = 0;
        var fileUp = [];

        function bbShowVideo(name, path, type){
            bootbox.alert("<h1>" + name + "</h1><br />" + "<video width='320' height='240' controls><source src='" + path + "' type='" + type + "'></video><br /><p><fmt:message key="myFiles.video1"/>&nbsp;<a href='" + path + "'><fmt:message key="myFiles.video2"/></a>&nbsp;<fmt:message key="myFiles.video3"/></p>", function(){});
        }

        function bbShowAudio(name, path, type){
            bootbox.alert("<h1>" + name + "</h1><br />" + "<audio controls><source src='" + path + "' type='" + type + "'></audio><br /><p><fmt:message key="myFiles.audio1"/>&nbsp;<a href='" + path + "'><fmt:message key="myFiles.audio2"/></a>&nbsp;<fmt:message key="myFiles.audio3"/></p>", function(){});
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
                message: "<p><fmt:message key="myFiles.deleteBox"/>&nbsp;" + name + " ?</p>",
                title: "<fmt:message key="label.delete"/>&nbsp;:&nbsp;" + name,
                buttons: {
                    danger: {
                        label: "<fmt:message key="label.cancel"/>",
                        className: "btn-danger",
                        callback: function() {}
                    },
                    success: {
                        label: "<fmt:message key="label.delete"/>",
                        className: "btn-success",
                        callback: function() {
                            $.ajax({
                                url: '${url.context}${apiPath}/nodes/' + id,
                                type: 'DELETE',
                                success: function(){
                                    window.location.reload();
                                },
                                error: function(result){
                                    bootbox.alert("<fmt:message key="myFiles.DeleteError"/>&nbsp;:&nbsp;" + name + "<br />" + result.responseJSON.localizedMessage, function() {});
                                }
                            })
                        }
                    }
                }
            });
        }

        function bbRenameFolder(name, id){
            bootbox.dialog({
                message: "<label><fmt:message key="newDirName.label"/>&nbsp;:&nbsp;</label><input type='text' id='renameFolder'/>",
                title: "<fmt:message key="label.rename"/> : " + name,
                buttons: {
                    danger: {
                        label: "<fmt:message key="label.cancel"/>",
                        className: "btn-danger",
                        callback: function() {}
                    },
                    success: {
                        label: "<fmt:message key="label.rename"/>",
                        className: "btn-success",
                        callback: function() {
                            var regex = /[:<>[\]*|"\\]/;

                            if(!regex.test($('#renameFolder').val())){
                                $.ajax({
                                    url: '${url.context}${apiPath}/nodes/' + id + '/moveto/' + $('#renameFolder').val(),
                                    type: 'POST',
                                    contentType: 'application/json',
                                    success: function(){
                                        window.location.reload();
                                    },
                                    error: function(result){
                                        bootbox.alert("<h1><fmt:message key="label.error"/>&nbsp;!</h1><br /><fmt:message key="myFiles.renameFolderError"/>&nbsp;:<br /><br />" + result.responseJSON.localizedMessage);
                                    }
                                })
                            }else{
                                bootbox.alert("<h1><fmt:message key="label.error"/>&nbsp;!</h1><br /><fmt:message key="myFiles.renameErrorCharacters"/>");
                            }
                        }
                    }
                }
            });
        }

        function bbRenameFile(name, id){
            bootbox.dialog({
                message: "<label><fmt:message key="newName.label"/>&nbsp;:&nbsp;</label><input type='text' id='renameFile'/>",
                title: "<fmt:message key="label.rename"/> : " + name,
                buttons: {
                    danger: {
                        label: "<fmt:message key="label.cancel"/>",
                        className: "btn-danger",
                        callback: function() {}
                    },
                    success: {
                        label: "<fmt:message key="label.rename"/>",
                        className: "btn-success",
                        callback: function() {
                            var regex = /[:<>[\]*|"\\]/;
                            var fileExt = '';

                            if(name.split('.').pop() != name){
                                fileExt = '.' + name.split('.').pop();
                            }

                            if(!regex.test($('#renameFile').val())){
                                $.ajax({
                                    url: '${url.context}${apiPath}/nodes/' + id + '/moveto/' + $('#renameFile').val() + fileExt,
                                    type: 'POST',
                                    contentType: 'application/json',
                                    success: function(){
                                        window.location.reload();
                                    },
                                    error: function(result){
                                        bootbox.alert("<h1><fmt:message key="label.error"/>&nbsp;!</h1><br /><fmt:message key="myFiles.renameFileError"/>&nbsp;:<br /><br />" + result.responseJSON.localizedMessage);
                                    }
                                })
                            }else{
                                bootbox.alert("<h1><fmt:message key="label.error"/>&nbsp;!</h1><br /><fmt:message key="myFiles.renameErrorCharacters"/>");
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
                var table = "<table class='table table-hover table-bordered'><thead><tr><th><fmt:message key="label.name"/></th><th><fmt:message key="label.status"/></th><th><fmt:message key="label.message"/></th></tr></thead><tbody>";
                for(var j = 0 ; j < fileUp.length ; j++){
                    if(fileUp[j][1] == 'error'){
                        table += "<tr><td>"+fileUp[j][0]+"</td><td><span class='label label-important'><fmt:message key="label.error"/></span></td><td>"+fileUp[j][2]+"</td></tr>";
                    }else{
                        table += "<tr><td>"+fileUp[j][0]+"</td><td><span class='label label-success'><fmt:message key="label.ok"/></span></td><td>"+fileUp[j][2]+"</td></tr>";
                    }
                };
                table += "</tbody></table>";
                bootbox.alert("<h1><fmt:message key="myFiles.uploadedFiles"/></h1><br />" + table, function(){
                    window.location.reload();
                });
            }
        }

        function bbAddFile(){
            bootbox.dialog({
                message: "<label><fmt:message key="addFile.label"/>&nbsp;:&nbsp;</label><button class='btn btn-primary pull-right' onclick='addInputForAddFile()' ><i class='icon-plus icon-white'></i>&nbsp;<fmt:message key="addFile.label"/></button><form id='fileFormUpload' enctype='multipart/form-data'><input name='file' type='file' id='file" + addFileIndex + "' /><br /></form><br /><br /><div class='alert alert-info'><h4><fmt:message key="myFiles.alertInfoCharacters"/>&nbsp;:</h4><br />: / \\ | \" < > [ ] * </div>",
                title: "<fmt:message key="uploadFile.label"/>",
                buttons: {
                    danger: {
                        label: "<fmt:message key="label.cancel"/>",
                        className: "btn-danger",
                        callback: function() {}
                    },
                    success: {
                        label: "<fmt:message key="label.add"/>",
                        className: "btn-success",
                        callback: function() {
                            for(var i = 0 ; i <= addFileIndex ; i++){
                                if($('#file' + i).val() != ''){
                                    $.ajaxFileUpload({
                                        url: '${url.context}${apiPath}/byPath${functions:escapeJavaScript(currentNode.path)}',
                                        secureuri:false,
                                        fileElementId: 'file' + i,
                                        dataType: 'json',
                                        success: function(result){
                                            if(result.name){
                                                endAddFile(result.name, addFileIndex, 'success', '');
                                            }else{
                                                endAddFile(result.subElements[0], addFileIndex, 'error', '<fmt:message key="myFiles.uploadedFileErrorCharacters"/><br />' + result.message);
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
                message: "<label><fmt:message key="label.name"/>&nbsp;:&nbsp;</label><input type='text' id='nameFolder'/><br /><br /><div class='alert alert-info'><h4><fmt:message key="myFiles.alertInfoCharacters"/>&nbsp;:</h4><br />: / \\ | \" < > [ ] * </div>",
                title: "Create new folder",
                buttons: {
                    danger: {
                        label: "<fmt:message key="label.cancel"/>",
                        className: "btn-danger",
                        callback: function() {}
                    },
                    success: {
                        label: "<fmt:message key="label.createFolder"/>",
                        className: "btn-success",
                        callback: function() {
                            var regex = /[:<>[\]*|"\\]/;

                            if(!regex.test($('#nameFolder').val())){
                                $.ajax({
                                    url: '${url.context}${apiPath}/nodes/' + id,
                                    type: 'PUT',
                                    contentType: 'application/json',
                                    data: "{\"children\":{\"" + $('#nameFolder').val() + "\":{\"name\":\"" + $('#nameFolder').val() + "\",\"type\":\"jnt:folder\"}}}",
                                    success: function(){
                                        window.location.reload();
                                    },
                                    error: function(result){
                                        bootbox.alert("<h1><fmt:message key="label.error"/>&nbsp;!</h1><br /><fmt:message key="myFiles.createFolderError"/>&nbsp;:<br /><br />" + result.responseJSON.message);
                                    }
                                })
                            }else{
                                bootbox.alert("<h1><fmt:message key="label.error"/>&nbsp;!</h1><br /><fmt:message key="myFiles.createFolderErrorCharacters"/>");
                            }
                        }
                    }
                }
            });
        }

        $(document).ready(function(){

            $(document).ajaxStart($.blockUI).ajaxStop($.unblockUI);

            $('#myFilesDataTables').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength":25,
                "sPaginationType": "bootstrap",
                "aaSorting": [] //this option disable sort by default, the user steal can use column names to sort the table
            });
        });
    </script>
</template:addResources>

<div class="row-fluid">
    <div class="pull-left btn-group">
        <button class="btn" onclick="bbAddFile()" title="<fmt:message key="uploadFile.label"/>">
            <i class="icon-arrow-up"></i>
            <i class="icon-file"></i>
        </button>
        <button class="btn" onclick="bbAddFolder('${currentNode.identifier}')" title="<fmt:message key="label.createFolder"/>">
            <i class="icon-plus"></i>
            <i class="icon-folder-open"></i>
        </button>
    </div>
<%--    <div class="pull-right btn-group">
        <button class="btn" title="<fmt:message key="myFiles.detailledView"/>">
            <i class="icon-th-list"></i>
        </button>
        <c:url value="${url.baseUserBoardFrameLive}${currentUser.localPath}.my-files.html" var="link">
            <c:param name="view" value="${functions:encodeUrlParam('icon')}"/>
            <c:param name="path" value="${functions:encodeUrlParam(currentNode.path)}"/>
        </c:url>
        <a class="btn" href="<c:out value='${link}' escapeXml='false'/>" title="<fmt:message key="myFiles.iconView"/>">
            <i class="icon-th"></i>
        </a>
        <a class="btn" title="<fmt:message key="myFiles.slideView"/>">
            <i class="icon-th-large"></i>
        </a>
    </div>--%>
</div>

<ul class="breadcrumb">
    <c:set var="compare" value="${currentUser.localPath}/files"/>
    <c:choose>
        <c:when test="${currentNode.path ne compare}">
            <li>
                <a href="<c:url value='${url.baseUserBoardFrameEdit}${currentUser.localPath}.files.html'/>" style="text-decoration: none;">
                    <i class="icon-home"></i> Home
                </a>
            </li>
            <c:set value="${fn:substringAfter(currentNode.path, compare)}" var="sub"/>
            <c:set value="${fn:split(sub, '/')}" var="split"/>
            <c:forEach items="${split}" var="folder" varStatus="folderIndex">
                <c:set value="${folderUrl}/${folder}" var="folderUrl"/>
                <c:choose>
                    <c:when test="${folderIndex.last}">
                        <li class="active">
                            <span class="divider">/</span>
                                ${folder}
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <span class="divider">/</span>
                            <c:set var="folderPath" value="${compare}${folderUrl}"/>
                            <c:url value="${url.baseUserBoardFrameEdit}${currentUser.localPath}.files.html" var="link">
                                <c:param name="path" value="${functions:encodeUrlParam(folderPath)}"/>
                            </c:url>
                            <a href="<c:out value='${link}' escapeXml='false'/>" style="text-decoration: none;">${folder}</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <li class="active">
                <i class="icon-home"></i>&nbsp;<fmt:message key="label.home"/>
            </li>
        </c:otherwise>
    </c:choose>
</ul>

<div id="detailView">
    <table class="table table-hover table-striped table-bordered" id="myFilesDataTables">
        <thead>
        <tr>
            <th><fmt:message key="label.name"/></th>
            <th><fmt:message key="label.type"/></th>
            <th><fmt:message key="label.created"/></th>
            <th><fmt:message key="label.size"/></th>
            <th><fmt:message key="label.modified"/></th>
            <th><fmt:message key="label.actions"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${jcr:getChildrenOfType(currentNode,'jnt:folder')}" var="node">
            <tr>
                <td>
                    <c:url value="${url.baseUserBoardFrameEdit}${currentUser.localPath}.files.html" var="link">
                        <c:param name="path" value="${functions:encodeUrlParam(node.path)}"/>
                    </c:url>
                    <a href="<c:out value='${link}' escapeXml='false'/>" style="text-decoration: none;">
                        <i class="icon-folder-close"></i>&nbsp;&nbsp;${node.name}
                    </a>
                </td>
                <td>
                    <fmt:message key="nt_folder"/>
                </td>
                <td>
                    <fmt:formatDate value="${node.properties['jcr:created'].date.time}" pattern="yy-MM-dd" var="displayCreatedDate"/>
                    ${displayCreatedDate}
                </td>
                <td>

                </td>
                <td>
                    <fmt:formatDate value="${node.properties['jcr:lastModified'].date.time}" pattern="yy-MM-dd" var="displayModifiedDate"/>
                    ${displayModifiedDate}
                </td>
                <td>
                    <c:if test="${(node.properties['jcr:createdBy'].string ne 'system')}">
                        <a class="pull-right" href="#" title="<fmt:message key="label.delete"/>" onclick="bbDelete('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;" >
                            <i class="icon-trash"></i>
                        </a>
                        <a class="pull-right" href="#" title="<fmt:message key="label.rename"/>" onclick="bbRenameFolder('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;">
                            <i class="icon-pencil"></i>&nbsp;&nbsp;
                        </a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:forEach items="${jcr:getChildrenOfType(currentNode,'jnt:file')}" var="node">
            <tr>
                <td>
                    <c:choose>
                        <c:when test="${(fn:split(node.fileContent.contentType, '/')[0]) eq 'video'}">
                            <a href="#" onclick="bbShowVideo('${functions:escapeJavaScript(node.name)}','${url.files}${functions:escapeJavaScript(node.path)}', '${node.fileContent.contentType}');return false;" style="text-decoration: none;">
                                <span class="icon ${functions:fileIcon(node.name)}"></span>
                                ${node.name}
                            </a>
                        </c:when>
                        <c:when test="${(fn:split(node.fileContent.contentType, '/')[0]) eq 'audio'}">
                            <a href="#" onclick="bbShowAudio('${functions:escapeJavaScript(node.name)}','${url.files}${functions:escapeJavaScript(node.path)}', '${node.fileContent.contentType}');return false;" style="text-decoration: none;">
                                <span class="icon ${functions:fileIcon(node.name)}"></span>
                                ${node.name}
                            </a>
                        </c:when>
                        <c:when test="${(fn:split(node.fileContent.contentType, '/')[0]) eq 'image'}">
                            <a href="#" onclick="bbShowImage('${functions:escapeJavaScript(node.name)}','${url.files}${functions:escapeJavaScript(node.path)}', '${node.properties['j:width'].string}', '${node.properties['j:height'].string}');return false;" style="text-decoration: none;">
                                <span class="icon ${functions:fileIcon(node.name)}"></span>
                                ${node.name}
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="<c:url value='${url.files}${functions:escapePath(node.path)}'/>" style="text-decoration: none;" download>
                                <span class="icon ${functions:fileIcon(node.name)}"></span>
                                ${node.name}
                            </a>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${(fn:split(node.fileContent.contentType, '/')[0]) eq 'application'}">
                            <c:choose>
                                <c:when test="${functions:fileExtensionFromMimetype(node.fileContent.contentType) ne null}">
                                    ${functions:fileExtensionFromMimetype(node.fileContent.contentType)}
                                </c:when>
                                <c:otherwise>
                                    ${fn:split(node.fileContent.contentType, '/')[1]}
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            ${fn:split(node.fileContent.contentType, '/')[1]}
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <fmt:formatDate value="${node.properties['jcr:created'].date.time}" pattern="yy-MM-dd" var="displayCreatedDate"/>
                    ${displayCreatedDate}
                </td>
                <td>
                    ${functions:humanReadableByteCount(node.fileContent.contentLength)}
                </td>
                <td>
                    <fmt:formatDate value="${node.properties['jcr:lastModified'].date.time}" pattern="yy-MM-dd" var="displayModifiedDate"/>
                    ${displayModifiedDate}
                </td>
                <td>
                    <a class="pull-right" href="#" title="<fmt:message key="label.delete"/>" onclick="bbDelete('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;">
                        <i class="icon-trash"></i>
                    </a>
                    <a class="pull-right" href="#" title="<fmt:message key="label.rename"/>" onclick="bbRenameFile('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;">
                        <i class="icon-pencil"></i>&nbsp;&nbsp;
                    </a>
                    <a class="pull-right" href="<c:url value='${url.files}${functions:escapePath(node.path)}'/>" title="<fmt:message key="label.download"/>" style="text-decoration: none;" download>
                        <i class="icon-download-alt"></i>&nbsp;&nbsp;
                    </a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>