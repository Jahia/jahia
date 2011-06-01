<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

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
<c:if test="${jcr:isAllowedChildNodeType(targetNode, 'jnt:file')}">
    <template:tokenizedForm>
    <form class="file_upload" id="file_upload${currentNode.identifier}" action="<c:url value='${url.base}${targetNode.path}'/>" method="POST" enctype="multipart/form-data"  accept="application/json">
        <div id="file_upload_container">
            <input type="file" name="file" multiple>
            <button><fmt:message key="label.upload"/></button>
            <div><fmt:message key="label.dropHere"/></div>
        </div>
        <c:url var="targetNodePath" value="${url.base}${linked.path}.html.ajax">
            <c:param name="targetNodePath" value="${targetNode.path}"/>
        </c:url>

    </form>
    </template:tokenizedForm>
    <table id="files${currentNode.identifier}" class="table"></table>
    <script>
        /*global $ */
        $(function () {
            $('#file_upload${currentNode.identifier}').fileUploadUI({
                namespace: 'file_upload_${currentNode.identifier}',
                onComplete: function (event, files, index, xhr, handler) {
                    $('#fileList${linked.identifier}').load('${targetNodePath}');
                },
                uploadTable: $('#files${currentNode.identifier}'),
                dropZone: $('#file_upload_container'),
                beforeSend: function (event, files, index, xhr, handler, callBack) {
                    handler.formData = {
                        'jcrNodeType':"jnt:file",
                        'jcrReturnContentType':"json",
                        'jcrRedirectTo':"<c:url value='${url.base}${renderContext.mainResource.node.path}'/>",
                        'jcrNewNodeOutputFormat':"${renderContext.mainResource.template}.html",
                        'form-token': $('#file_upload${currentNode.identifier} input[name=form-token]').val()
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
</c:if>
<c:if test="${!jcr:isAllowedChildNodeType(targetNode, 'jnt:file')}">
    <c:if test="${renderContext.editMode}">
        <fmt:message key="label.warningWrongTarget"/>
    </c:if>
</c:if>
