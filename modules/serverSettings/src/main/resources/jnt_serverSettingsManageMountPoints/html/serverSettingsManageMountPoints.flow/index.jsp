<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="mailSettings" type="org.jahia.services.mail.MailSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="mountPointManager" type="org.jahia.modules.serversettings.mount.MountPointManager"--%>

<template:addResources type="javascript"
                       resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,bootstrap-filestyle.min.js,jquery.metadata.js,jquery.tablesorter.js,jquery.tablecloth.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css,tablecloth.css"/>
<template:addResources type="javascript"
                       resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>

<script type="text/javascript" charset="utf-8">
    $(document).ready(function () {
        var mountsTable = $('#mountsTable');

        mountsTable.dataTable({
            "sDom": "<'row-fluid'<'span6'l><'span6 text-right'f>r>t<'row-fluid'<'span6'i><'span6 text-right'p>>",
            "iDisplayLength": 10,
            "sPaginationType": "bootstrap",
            "aaSorting": [] //this option disable sort by default, the user steal can use column names to sort the table
        });
    });

    function submitEvent(action, mountPoint) {
        $("#MPFormValue").val(mountPoint);
        $("#MPFormAction").val(action);
        $("#MPForm").submit();
    }

    function submitEventWithConfirm(action, mountPoint, message) {
        if (confirm(message)) {
            submitEvent(action, mountPoint);
        }
    }

    function goToFactory(){
        var url = $("#mountPointFactory").val();
        if(url) {
            window.parent.goToUrl(url);
        }
    }
</script>

<form style="margin: 0; display: none;" action="${flowExecutionUrl}" method="post" id="MPForm">
    <input type="hidden" name="name" id="MPFormValue"/>
    <input type="hidden" name="action" id="MPFormAction"/>
    <input type="hidden" name="_eventId" value="doAction">
</form>

<h2><fmt:message key="serverSettings.mountPointsManagement"/></h2>

<div class="box-1">
    <h2><fmt:message key="serverSettings.mountPointsManagement.add"/></h2>
    <form style="margin: 0;">
        <select id="mountPointFactory">
            <c:forEach var="providerFactory" items="${mountPointManager.mountPointFactories}">
                <c:if test="${not empty providerFactory.value.endOfURL}">
                    <option value="<c:url value='${url.base}${providerFactory.value.endOfURL}'/>">${providerFactory.value.displayableName}</option>
                </c:if>
            </c:forEach>
        </select>

        <a class="btn btn-primary" href="#" onclick="goToFactory()">
            <i class="icon-plus  icon-white"></i>&nbsp;<span><fmt:message key="label.add"/></span>
        </a>
    </form>
</div>

<p>
<c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
    <c:if test="${message.severity eq 'INFO'}">
        <div class="alert alert-success">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${message.text}
        </div>
    </c:if>
    <c:if test="${message.severity eq 'ERROR'}">
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${message.text}
        </div>
    </c:if>
</c:forEach>
</p>

<table id="mountsTable" class="table table-bordered table-striped table-hover">
    <thead>
    <tr>
        <th>
            <fmt:message key="label.name"/>
        </th>
        <th>
            <fmt:message key="label.path"/>
        </th>
        <th width="100px">
            <fmt:message key="label.status"/>
        </th>
        <th class="{sorter: false}" width="250px">
            <fmt:message key="label.actions"/>
        </th>
    </tr>
    </thead>

    <tbody>
    <c:forEach items="${mountPointManager.mountPoints}" var="mountPoint" varStatus="loopStatus">
        <tr>
            <td>
                    ${mountPoint.name}
            </td>
            <td>
                    ${mountPoint.path}
            </td>
            <td>
                <span class="badge ${mountPoint.displayStatusClass}">
                    <fmt:message key="serverSettings.mountPointsManagement.mountStatus.${mountPoint.status}"/>
                </span>
            </td>
            <td>
                <c:if test="${mountPoint.showMountAction}">
                    <button class="btn btn-info" type="button" onclick="submitEvent('mount', '${mountPoint.name}')">
                        <i class="icon-download icon-white"></i>&nbsp;<fmt:message key="serverSettings.mountPointsManagement.action.mount"/>
                    </button>
                </c:if>
                <c:if test="${mountPoint.showUnmountAction}">
                    <button class="btn btn-info" type="button" onclick="submitEvent('unmount', '${mountPoint.name}')">
                        <i class="icon-upload icon-white"></i>&nbsp;<fmt:message key="serverSettings.mountPointsManagement.action.unmount"/>
                    </button>
                </c:if>
                <fmt:message var="confirmDelete" key="serverSettings.mountPointsManagement.action.confirmDelete">
                    <fmt:param value="${mountPoint.name}"/>
                </fmt:message>
                <c:if test="${not empty mountPointManager.mountPointFactories[mountPoint.nodetype]}">
                    <c:url var="editURL" value="${url.base}${mountPointManager.mountPointFactories[mountPoint.nodetype].endOfURL}"/>
                    <a class="btn" href="#" onclick="window.parent.goToUrl('${editURL}?edit=${mountPoint.identifier}')">
                        <i class="icon-edit"></i>&nbsp;<span><fmt:message key="label.edit"/></span>
                    </a>
                </c:if>
                <button class="btn btn-danger" type="button"
                        onclick="submitEventWithConfirm('delete', '${mountPoint.name}', '${functions:escapeJavaScript(confirmDelete)}')">
                    <i class="icon-remove icon-white"></i>&nbsp;<fmt:message key="label.delete"/>
                </button>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
