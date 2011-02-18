<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="jquery.fileupload.css"/>
<template:addResources type="javascript" resources="jquery.js,jquery-ui.min.js,jquery.fileupload.js,jquery.fileupload-ui.js"/>
<c:set var="targetNode" value="${renderContext.mainResource.node}"/>
<c:if test="${!empty currentNode.properties.target}">
    <c:set var="targetNode" value="${currentNode.properties.target.node}"/>
</c:if>
<form id="file_upload" action="${url.base}${targetNode.path}" method="POST" enctype="multipart/form-data">
    <input type="hidden" name="nodeType" value="jnt:file"/>
    <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
    <input type="hidden" name="newNodeOutputFormat" value="${renderContext.mainResource.template}"/>
    <input type="hidden" name="newNodeOutputFormat" value="${renderContext.mainResource.template}"/>
    <div class="hidden">
        <input type="file" name="file" multiple>
    </div>
    <button>Upload</button>
    <div>Upload files</div>
</form>
<table id="files"></table>
<script>
/*global $ */
$(function () {
    $('file_upload').fileUploadUI({
        uploadTable: $('files'),
        downloadTable: $('files'),
        buildUploadRow: function (files, index) {
            return $('<tr><td>' + files[index].name + '<\/td>' +
                    '<td class="file_upload_progress"><div><\/div><\/td>' +
                    '<td class="file_upload_cancel">' +
                    '<button class="ui-state-default ui-corner-all" title="Cancel">' +
                    '<span class="ui-icon ui-icon-cancel">Cancel<\/span>' +
                    '<\/button><\/td><\/tr>');
        },
        buildDownloadRow: function (file) {
            return $('<tr><td>' + file.name + '<\/td><\/tr>');
        }
    });
});
</script>