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
<c:set var="linked" value="${ui:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:set var="targetNode" value="${renderContext.mainResource.node}"/>
<c:if test="${!empty currentNode.properties.target}">
    <c:set var="targetNode" value="${currentNode.properties.target.node}"/>
</c:if>
<form class="file_upload" id="file_upload${currentNode.identifier}" action="${url.base}${targetNode.path}" method="POST" enctype="multipart/form-data"  accept="application/json">
    <div id="file_upload_container">
    	<input type="file" name="file" multiple>
	    <button><fmt:message key="label.upload"/></button>
	    <div><fmt:message key="label.dropHere"/></div>
    </div>

</form>
<table id="files${currentNode.identifier}" class="table"></table>
<script>
    /*global $ */
    $(function () {
        $('#file_upload${currentNode.identifier}').fileUploadUI({
            namespace: 'file_upload_${currentNode.identifier}',
            onComplete: function (event, files, index, xhr, handler) {
                $('#fileList${linked.identifier}').load('${url.base}${linked.path}.html.ajax?targetNodePath=${targetNode.path}');
            },
            uploadTable: $('#files${currentNode.identifier}'),
			dropZone: $('#file_upload_container'),
			beforeSend: function (event, files, index, xhr, handler, callBack) {
				handler.formData = {
					nodeType:"jnt:file",
					returnContentType:"json",
					redirectTo:"${url.base}${renderContext.mainResource.node.path}",
					newNodeOutputFormat:"${renderContext.mainResource.template}.html"
				};
				callBack();
			},
            buildUploadRow: function (files, index) {
                return $('<tr><td>' + files[index].name + '<\/td>' +
                        '<td class="file_upload_progress"><div><\/div><\/td>' +
                        '<td class="file_upload_cancel">' +
                        '<button class="ui-state-default ui-corner-all" title="Cancel">' +
                        '<span class="ui-icon ui-icon-cancel">Cancel<\/span>' +
                        '<\/button><\/td><\/tr>');
            }
        });
    });
</script>
