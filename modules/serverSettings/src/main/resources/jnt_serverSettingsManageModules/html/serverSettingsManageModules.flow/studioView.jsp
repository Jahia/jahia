<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.settings.SettingsBean" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<c:set var="developmentMode" value="<%= SettingsBean.getInstance().isDevelopmentMode() %>"/>
<template:addResources type="javascript"
                       resources="jquery.min.js,admin-bootstrap.js,jquery.blockUI.js,bootstrap-filestyle.min.js,jquery.metadata.js,workInProgress.js"/>
<template:addResources type="javascript"
                       resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>
<template:addResources type="css" resources="admin-bootstrap.css,datatables/css/bootstrap-theme.css,tablecloth.css"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>
<h2><fmt:message key="serverSettings.manageModules"/></h2>

<template:addResources>
    <script type="text/javascript">
        $(document).ready(function () {
            $(":file").filestyle({classButton: "btn", classIcon: "icon-folder-open"/*,buttonText:"Translation"*/});
        });
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            $('#module_table').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength": 25,
                "sPaginationType": "bootstrap",
                "aaSorting": [] //this option disable sort by default, the user steal can use column names to sort the table
            });
        });
    </script>
</template:addResources>

<%@include file="common/moduleLabels.jspf" %>
<table cellpadding="0" cellspacing="0" border="0" class="table table-striped table-bordered" id="module_table">
    <thead>
    <tr>
        <th><fmt:message key='serverSettings.manageModules.moduleName'/></th>
        <th><fmt:message key='serverSettings.manageModules.moduleId'/></th>
        <th><fmt:message key='serverSettings.manageModules.groupId'/></th>
        <th class="{sorter: false}">${i18nModuleDetails}</th>
        <th><fmt:message key='serverSettings.manageModules.versions'/></th>
        <th><fmt:message key='serverSettings.manageModules.usedInSites'/></th>
    </tr>
    </thead>
    <tbody>
        <c:set var="isStudio" value="${true}"/>
        <c:forEach items="${allModuleVersions}" var="entry">
            <%@include file="common/currentModuleVars.jspf" %>
            <%@include file="common/modulesTableRow.jspf" %>
        </c:forEach>
    </tbody>
</table>
