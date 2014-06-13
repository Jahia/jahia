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
<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js,jquery.blockUI.js,bootstrap-filestyle.min.js,jquery.metadata.js,workInProgress.js,jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jquery.cuteTime.settings.${currentResource.locale}.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>
<template:addResources type="css" resources="admin-bootstrap.css,datatables/css/bootstrap-theme.css,tablecloth.css"/>
<template:addResources type="css" resources="manageModules.css"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>
<fmt:message key="serverSettings.manageModules.checkForUpdates" var="i18nRefreshModules" />
<fmt:message var="lastUpdateTooltip" key="serverSettings.manageModules.lastUpdate"/>
<h2><fmt:message key="serverSettings.manageModules"/></h2>

<c:set var="moduleTableId" value="module_table_${adminModuleTableUUID}"/>

<template:addResources>
    <script type="text/javascript">
        $(document).ready(function () {
            $(":file").filestyle({classButton: "btn", classIcon: "icon-folder-open"/*,buttonText:"Translation"*/});
        });
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            var oldStart = 0;
            $('#${moduleTableId}').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'<'refresh_modules'>f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength": 25,
                "sPaginationType": "bootstrap",
                "aaSorting": [], //this option disable sort by default, the user steal can use column names to sort the table
                "bStateSave": true,
                "fnDrawCallback": function (o) {
                    // auto scroll to top on paginate
                    if ( o._iDisplayStart != oldStart ) {
                        var targetOffset = $('#${moduleTableId}').offset().top;
                        $('html,body').animate({scrollTop: targetOffset}, 350);
                        oldStart = o._iDisplayStart;
                    }
                }
            });

            var refreshModulesButton = $("<button class='btn'><i class='icon-refresh'/>&nbsp; ${i18nRefreshModules}</button>");
            refreshModulesButton.on('click', function(){
                $("#reloadModulesForm").submit();
            });

            $('.timestamp').cuteTime({ refresh: 60000 });
            $('.refresh_modules').append(refreshModulesButton).css("float", "right").css("margin-left","10px");
        });
    </script>
</template:addResources>

<form id="viewAvailableModulesForm" style="display: none" action="${flowExecutionUrl}" method="POST">
    <input type="hidden" name="_eventId" value="viewAvailableModules"/>
</form>
<form id="reloadModulesForm" style="display: none" action="${flowExecutionUrl}" method="POST" onsubmit="workInProgress('${i18nWaiting}');">
    <input type="hidden" name="_eventId" value="reloadModules"/>
</form>
<ul class="nav nav-tabs">
    <li class="active">
        <a href="#"><fmt:message key="serverSettings.manageModules.installedModules"/></a>
    </li>
    <li><a href="#" onclick="$('#viewAvailableModulesForm').submit()"><fmt:message key="serverSettings.manageModules.availableModules"/></a></li>
    <li class="last-modules-update">
        <span><fmt:message key="serverSettings.manageModules.lastUpdate"/>:&nbsp;<span class="timestamp"><fmt:formatDate value="${lastModulesUpdate}" pattern="yyyy/MM/dd HH:mm"/></span></span>
    </li>
</ul>
<div class="tab-content">
    <form:form modelAttribute="moduleFile" class="form" enctype="multipart/form-data" method="post"
               onsubmit="workInProgress('${i18nWaiting}');">
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
            <c:if test="${message.source eq 'moduleExists'}">
                <c:set var="forceUpdateDisplay" value="true"/>

            </c:if>
        </c:forEach>
        <c:if test="${forceUpdateDisplay eq 'true'}">
            <div class="alert alert-warning">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                <fmt:message key="serverSettings.manageModules.upload.force.info"/>
            </div>
        </c:if>
        <div class="alert alert-info">
            <label for="moduleFileUpload"><fmt:message key="serverSettings.manageModules.upload.module"/></label>
            <input type="file" id="moduleFileUpload" name="moduleFile" accept=""/>
            <c:if test="${forceUpdateDisplay eq 'true'}">
                <label for="moduleForceUpdate"><fmt:message key="serverSettings.manageModules.upload.force"/>
                    <input type="checkbox" name="moduleForceUpdate" id="moduleForceUpdate"/>
                </label>
            </c:if>
            <button class="btn btn-primary" type="submit" name="_eventId_upload">
                <i class="icon-download icon-white"></i>
                &nbsp;<fmt:message key='label.upload'/>
            </button>
        </div>
    </form:form>
    <%@include file="common/moduleLabels.jspf" %>
    <table cellpadding="0" cellspacing="0" border="0" class="table table-striped table-bordered" id="${moduleTableId}">
        <thead>
        <tr>
            <th><fmt:message key='serverSettings.manageModules.moduleName'/></th>
            <th><fmt:message key='serverSettings.manageModules.moduleId'/></th>
            <th><fmt:message key='serverSettings.manageModules.groupId'/></th>
            <th class="{sorter: false}">${i18nModuleDetails}</th>
            <th><fmt:message key='serverSettings.manageModules.versions'/></th>
            <th><fmt:message key='serverSettings.manageModules.status'/></th>
            <c:if test="${developmentMode}">
                <th><fmt:message key='serverSettings.manageModules.sources'/></th>
            </c:if>
            <th><fmt:message key='serverSettings.manageModules.usedInSites'/></th>
        </tr>
        </thead>
        <tbody>
        <c:set var="isStudio" value="${false}"/>
        <c:forEach items="${allModuleVersions}" var="entry">
            <%@include file="common/currentModuleVars.jspf" %>
            <c:if test="${!isMandatoryDependency && sourcesDownloadable}">
                <%@include file="common/modulesTableRow.jspf" %>
            </c:if>
        </c:forEach>
        <c:forEach items="${allModuleVersions}" var="entry">
            <%@include file="common/currentModuleVars.jspf" %>
            <c:if test="${!isMandatoryDependency && !sourcesDownloadable}">
                <%@include file="common/modulesTableRow.jspf" %>
            </c:if>
        </c:forEach>
        <c:forEach items="${allModuleVersions}" var="entry">
            <%@include file="common/currentModuleVars.jspf" %>
            <c:if test="${isMandatoryDependency}">
                <%@include file="common/modulesTableRow.jspf" %>
            </c:if>
        </c:forEach>
        </tbody>
    </table>
    <p><a id="mandatory-dependency">&nbsp;</a><span class="text-error"><strong>*</strong></span>&nbsp;-&nbsp;${i18nMandatoryDependency}</p>
</div>
