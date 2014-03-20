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
<c:set var="currentView" value="slider"/>

<template:addResources>
    <script type="text/javascript">

        var apiPath = '${apiPath}';

        var currentNodePath = '${functions:escapeJavaScript(currentNode.path)}';

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

<div id="carouselView" class="carousel slide box-1">
    <div class="carousel-inner">
        <c:set value="${0}" var="index"/>
        <c:forEach items="${jcr:getChildrenOfType(currentNode,'jnt:file')}" var="node" varStatus="status">
            <c:if test="${(fn:split(node.fileContent.contentType, '/')[0]) eq 'image'}">
                <c:choose>
                    <c:when test="${index eq 0}">
                        <div class="active item" style="height: 600px" data-pause="hover">
                    </c:when>
                    <c:otherwise>
                        <div class="item" style="height: 600px" data-pause="hover">
                    </c:otherwise>
                </c:choose>
                <img src="<c:url value='${url.files}${functions:escapePath(node.path)}'/>" width="${node.properties['j:width'].string}" height="${node.properties['j:height'].string}" alt="${node.name}" style="margin: auto;"/>
                <div class="carousel-caption">
                    <div class="pull-right">
                        <a class="pull-right" href="#" title="<fmt:message key="label.delete"/>" onclick="bbDelete('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;">
                            <i class="icon-trash"></i>
                        </a>
                        <a class="pull-right" href="#" title="<fmt:message key="label.rename"/>" onclick="bbRenameFile('${functions:escapeJavaScript(node.name)}', '${node.identifier}');return false;" style="text-decoration: none;">
                            <i class="icon-pencil"></i>&nbsp;&nbsp;
                        </a>
                        <a class="pull-right" href="<c:url value='${url.files}${functions:escapePath(node.path)}'/>" title="<fmt:message key="label.download"/>" style="text-decoration: none;" download>
                            <i class="icon-download-alt"></i>&nbsp;&nbsp;
                        </a>
                    </div>
                    <h4>${node.name}</h4>
                    <p>
                        <fmt:message key="label.size"/>&nbsp;:&nbsp;${functions:humanReadableByteCount(node.fileContent.contentLength)}
                        <br />
                        <fmt:message key="myFiles.dimension"/>&nbsp;:&nbsp;${node.properties['j:width'].string}x${node.properties['j:height'].string}
                    </p>

                </div>
            </div>
            <c:set value="${index + 1}" var="index"/>
            </c:if>
        </c:forEach>
        <c:if test="${index eq 0}">
            <div class="active item" style="height: 600px" data-pause="hover">
                <fmt:message key="myFiles.slideView.noImage"/>
            </div>
        </c:if>
    </div>
    <a class="carousel-control left" href="#carouselView" data-slide="prev">&lsaquo;</a>
    <a class="carousel-control right" href="#carouselView" data-slide="next">&rsaquo;</a>
</div>