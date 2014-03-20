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

<template:addResources type="css" resources="datatables/css/bootstrap-theme.css,tablecloth.css"/>
<template:addResources type="css" resources="files.css"/>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.blockUI.js,admin-bootstrap.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>
<template:addResources type="javascript" resources="bootbox.min.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="myFilesDashboard.js"/>

<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/>
<c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>

<c:set var="apiPath" value="${url.context}/modules/api/jcr/v1/default/${currentResource.locale}"/>
<c:set var="currentView" value="icon"/>

<template:addResources>
    <script type="text/javascript">

        var apiPath = '${apiPath}';

        var currentNodePath = '${functions:escapeJavaScript(currentNode.path)}';

        var myFilesVideo1 = "<fmt:message key="myFiles.video1"/>";
        var myFilesVideo2 = "<fmt:message key="myFiles.video2"/>";
        var myFilesVideo3 = "<fmt:message key="myFiles.video3"/>";

        var myFilesAudio1 = "<fmt:message key="myFiles.audio1"/>";
        var myFilesAudio2 = "<fmt:message key="myFiles.audio2"/>";
        var myFilesAudio3 = "<fmt:message key="myFiles.audio3"/>";

        var myFilesDeleteBox = "<fmt:message key="myFiles.deleteBox"/>";
        var myFilesDeleteError = "<fmt:message key="myFiles.deleteError"/>";

        var myFilesUpdateTagsError = "<fmt:message key="myFiles.updateTagsError"/>";

        var myFilesRenameFolderError = "<fmt:message key="myFiles.renameFolderError"/>";
        var myFilesRenameErrorCharacters = "<fmt:message key="myFiles.renameErrorCharacters"/>";

        var myFilesRenameFileError = "<fmt:message key="myFiles.renameFileError"/>";

        var myFilesUploadedFiles = "<fmt:message key="myFiles.uploadedFiles"/>";
        var myFilesUploadedFileErrorCharacters = "<fmt:message key="myFiles.uploadedFileErrorCharacters"/>";

        var myFilesAlertInfoCharacters = "<fmt:message key="myFiles.alertInfoCharacters"/>";

        var myFilesCreateNewFolder = "<fmt:message key="myFiles.createNewFolder"/>";
        var myFilesCreateFolderError = "<fmt:message key="myFiles.createFolderError"/>";
        var myFilesCreateFolderErrorCharacters = "<fmt:message key="myFiles.createFolderErrorCharacters"/>";

        var labelDelete = "<fmt:message key="label.delete"/>";
        var labelCancel = "<fmt:message key="label.cancel"/>";
        var labelError = "<fmt:message key="label.error"/>";
        var labelRename = "<fmt:message key="label.rename"/>";
        var labelNewDirName = "<fmt:message key="newDirName.label"/>";
        var labelNewName = "<fmt:message key="newName.label"/>";
        var labelName = "<fmt:message key="label.name"/>";
        var labelStatus = "<fmt:message key="label.status"/>";
        var labelMessage = "<fmt:message key="label.message"/>";
        var labelOK = "<fmt:message key="label.ok"/>";
        var labelAddFile = "<fmt:message key="addFile.label"/>";
        var labelUploadFile = "<fmt:message key="uploadFile.label"/>";
        var labelAdd = "<fmt:message key="label.add"/>";
        var labelCreateFolder = "<fmt:message key="label.createFolder"/>";

        var addFileIndex = 0;
        var index = 0;
        var fileUp = [];

        $(document).ready(function(){

            $(document).ajaxStart($.blockUI).ajaxStop($.unblockUI);

            $('.divFH').hover(
                function(){
                    $(this).find('.btnFH').show();
                },
                function(){
                    $(this).find('.btnFH').hide();
                }
            );
        });
    </script>
</template:addResources>

<%@include file="folder.userDashboard-toolbarBreadcrumb.jspf" %>

<div id="iconView">
    <ul class="thumbnails">
        <c:forEach items="${jcr:getChildrenOfType(currentNode,'jnt:folder')}" var="node">
            <li class="span2">
                <div class="divFH" style="height: 135px;overflow: hidden">
                    <c:url value="${url.baseUserBoardFrameEdit}${currentUser.localPath}.files.html" var="link">
                        <c:param name="path" value="${functions:encodeUrlParam(node.path)}"/>
                        <c:param name="view" value="${functions:encodeUrlParam('icon')}"/>
                    </c:url>
                    <a href="<c:out value='${link}' escapeXml='false'/>" class="thumbnail">
                        <img src="<c:url value='${url.currentModule}/img/folder-files.png'/>" alt="folder" width="48" height="48"/>
                    </a>
                    <p class="text-center" style="overflow: hidden;max-height: 32px;text-overflow: ellipsis">
                        <strong>${node.name}</strong>
                    </p>
                    <c:if test="${(node.properties['jcr:createdBy'].string ne 'system')}">
                        <p class="hide btnFH text-center">
                            <a class="pull-right" href="#" title="<fmt:message key="label.delete"/>" onclick="bbDelete('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;" >
                                <i class="icon-trash"></i>
                            </a>
                            <a class="pull-right" href="#" title="<fmt:message key="label.rename"/>" onclick="bbRenameFolder('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;">
                                <i class="icon-pencil"></i>&nbsp;&nbsp;
                            </a>
                        </p>
                    </c:if>
                </div>
            </li>
        </c:forEach>
        <c:forEach items="${jcr:getChildrenOfType(currentNode,'jnt:file')}" var="node">
            <li class="span2">
                <div class="divFH" style="height: 138px;">
                    <c:choose>
                        <c:when test="${(fn:split(node.fileContent.contentType, '/')[0]) eq 'video'}">
                            <a class="thumbnail text-center" href="#" onclick="bbShowVideo('${functions:escapeJavaScript(node.name)}','${url.context}${url.files}${functions:escapeJavaScript(node.path)}', '${node.fileContent.contentType}');return false;">
                                <span class="icon_large ${functions:fileIcon(node.name)}_large" style="margin: 0px;margin-left: auto;margin-right: auto;"></span>
                            </a>
                        </c:when>
                        <c:when test="${(fn:split(node.fileContent.contentType, '/')[0]) eq 'audio'}">
                            <a class="thumbnail text-center" href="#" onclick="bbShowAudio('${functions:escapeJavaScript(node.name)}','${url.context}${url.files}${functions:escapeJavaScript(node.path)}', '${node.fileContent.contentType}');return false;">
                                <span class="icon_large ${functions:fileIcon(node.name)}_large" style="margin: 0px;margin-left: auto;margin-right: auto;"></span>
                            </a>
                        </c:when>
                        <c:when test="${(fn:split(node.fileContent.contentType, '/')[0]) eq 'image'}">
                            <a class="thumbnail text-center" href="#" onclick="bbShowImage('${functions:escapeJavaScript(node.name)}','${url.context}${url.files}${functions:escapeJavaScript(node.path)}', '${node.properties['j:width'].string}', '${node.properties['j:height'].string}');return false;">
                                <img src="${functions:default(node.thumbnailUrls['thumbnail'],thumbnailURL)}" alt="${fn:escapeXml(node.name)}" style="max-height: 71px"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="thumbnail text-center" href="<c:url value='${url.files}${functions:escapePath(node.path)}'/>" download>
                                <span class="icon_large ${functions:fileIcon(node.name)}_large" style="margin: 0px;margin-left: auto;margin-right: auto;"></span>
                            </a>
                        </c:otherwise>
                    </c:choose>
                    <p class="text-center" style="overflow: hidden;max-height: 32px;text-overflow: ellipsis">
                        <strong>${node.name}</strong>
                    </p>
                    <p class="hide btnFH text-center">
                        <a class="pull-right" href="#" title="<fmt:message key="label.delete"/>" onclick="bbDelete('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;">
                            <i class="icon-trash"></i>
                        </a>
                        <a class="pull-right" href="#" title="<fmt:message key="label.rename"/>" onclick="bbRenameFile('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;">
                            <i class="icon-pencil"></i>&nbsp;&nbsp;
                        </a>
                        <a class="pull-right" href="<c:url value='${url.files}${functions:escapePath(node.path)}'/>" title="<fmt:message key="label.download"/>" style="text-decoration: none;" download>
                            <i class="icon-download-alt"></i>&nbsp;&nbsp;
                        </a>
                    </p>
                </div>
            </li>
        </c:forEach>
    </ul>
</div>