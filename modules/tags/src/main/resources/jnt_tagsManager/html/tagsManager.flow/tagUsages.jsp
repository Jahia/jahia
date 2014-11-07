<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>

<template:addResources type="css" resources="admin-bootstrap-v3.2.min.css"/>
<template:addResources type="css" resources="admin-font-awesome-v4.2.0.min.css"/>

<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="admin-bootstrap-v3.2.min.js"/>
<template:addResources type="javascript" resources="jquery-ui.min.js,jquery.blockUI.js,workInProgress.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js"/>
<template:addResources type="javascript" resources="bootbox.min.js"/>
<template:addResources type="javascript" resources="tagUsages.js"/>

<fmt:message key="label.cancel" var="labelCancel"/>
<fmt:message key="label.ok" var="labelOk"/>
<fmt:message key="label.rename" var="labelRename"/>
<fmt:message key="label.delete" var="labelDelete"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/>
<fmt:message key="jnt_tagsManager.label.tagNewName" var="labelTagNewName"/>
<fmt:message key="jnt_tagsManager.modal.rename" var="modalRename"/>
<fmt:message key="jnt_tagsManager.modal.delete" var="modalDelete"/>

<template:addResources type="inlinejavascript">
    <script>
        var jsVarMap = {
            labelCancel: '${functions:escapeJavaScript(labelCancel)}',
            labelOk: '${functions:escapeJavaScript(labelOk)}',
            labelRename: '${functions:escapeJavaScript(labelRename)}',
            labelDelete: '${functions:escapeJavaScript(labelDelete)}',
            i18nWaiting: '${functions:escapeJavaScript(i18nWaiting)}',
            labelTagNewName: '${functions:escapeJavaScript(labelTagNewName)}',
            modalRename: '${functions:escapeJavaScript(modalRename)}',
            modalDelete: '${functions:escapeJavaScript(modalDelete)}'
        };

        $(document).ready(function () {
            $('#tableTagDetails').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6 text-right'f>r>t<'row-fluid'<'span6'i><'span6 text-right'>>",
                "bPaginate": false,
                "aaSorting": [[1, 'desc']]
            });
        });
    </script>
</template:addResources>

<c:forEach items="${tagDetails}" var="tag">
    <c:set value="${tag.key}" var="currentTagName"/>
    <c:set value="${tag.value}" var="currentTagValue"/>
</c:forEach>

<div class="container-fluid">
    <div class="row">
        <h1><fmt:message key="jnt_tagsManager"/></h1>
    </div>
    <div class="row well">
        <h3><fmt:message key="jnt_tagsManager.title.detailsForTag"/>&nbsp;${currentTagName}</h3>
        <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered table-striped" id="tableTagDetails">
            <thead>
            <tr>
                <th>
                    <fmt:message key="label.page"/>
                </th>
                <th>
                    <fmt:message key="label.path"/>
                </th>
                <th>
                    <fmt:message key="label.actions"/>
                </th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${currentTagValue}" var="nodeId">
                <jcr:node var="currentNodeTag" uuid="${nodeId}"/>
                <tr>
                    <td>
                        <%--${currentNodeTag.page}--%>
                    </td>
                    <td>
                        ${currentNodeTag.path}
                    </td>
                    <td>
                        <div class="dropdown">
                            <button class="btn btn-primary dropdown-toggle" type="button" id="dropdownMenuActions" data-toggle="dropdown">
                                <i class="fa fa-list-ul"></i>&nbsp;<fmt:message key="label.actions"/>
                                <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenuActions">
                                <li role="presentation">
                                    <a role="menuitem" tabindex="-1" href="#" onclick="bbRenameTag('${functions:escapeJavaScript(currentNodeTag.identifier)}')">
                                        <i class="fa fa-pencil"></i>&nbsp;<fmt:message key="label.rename"/>
                                    </a>
                                </li>
                                <li role="presentation">
                                    <a role="menuitem" tabindex="-1" href="#" onclick="bbDeleteTag('${functions:escapeJavaScript(currentNodeTag.identifier)}')">
                                        <i class="fa fa-trash"></i>&nbsp;<fmt:message key="label.delete"/>
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
    <div class="row hide">
        <form:form id="formTagManagement" action="${flowExecutionUrl}" method="post">
            <input type="hidden" id="eventInput" name="_eventId_">
            <input type="hidden" name="selectedTag" value="${currentTagName}">
            <input type="hidden" id="nodeToUpdateId" name="nodeToUpdateId"/>
            <input type="hidden" id="tagNewName" name="tagNewName">
        </form:form>
    </div>
</div>