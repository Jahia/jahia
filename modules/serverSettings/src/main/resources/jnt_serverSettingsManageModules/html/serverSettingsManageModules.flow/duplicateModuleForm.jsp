<%@ page import="org.springframework.web.servlet.tags.form.FormTag" %>
<%@ page import="javax.servlet.jsp.tagext.*" %>
<%@ page import="java.util.Arrays" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.blockUI.js,workInProgress.js"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>
<c:if test="${not empty branchTagInfos}">
    <fmt:message key="serverSettings.manageModules.duplicateModule.scm.master" var="scmMaster"/>
    <template:addResources type="inlinejavascript">
        <script type="text/javascript">
            $(document).ready(function() {
                $("#newScmUri").change(function() {
                    var selectedTag = $(this).find('option:selected').text();
                    if (selectedTag == "${scmMaster}") {
                        selectedTag = "";
                    }
                    $('#branchOrTag').val(selectedTag);
                });
            });
        </script>
    </template:addResources>
</c:if>
<h2>
    <fmt:message key='serverSettings.manageModules.duplicateModule' />
</h2>
<c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
    <c:if test="${message.severity eq 'ERROR'}">
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${message.text}
        </div>
    </c:if>
</c:forEach>

<form action="${flowExecutionUrl}" method="POST" onsubmit="workInProgress('${i18nWaiting}');">
    <fieldset>
        <c:if test="${empty srcPath}">
        <c:choose>
        <c:when test="${not empty branchTagInfos}">
            <label for="newScmUri"><fmt:message key="serverSettings.manageModules.downloadSources.scm.${fn:endsWith(version,'-SNAPSHOT') ? 'branch' : 'tag'}" /></label>
            <input type="hidden" id="branchOrTag" name="branchOrTag" value="${not empty branchOrTag ? branchOrTag : ''}"/>
            <select name="newScmUri" id="newScmUri">
                <c:forEach var="branchTagInfo" items="${branchTagInfos}">
                    <option value="${branchTagInfo.value}" ${branchTagInfo.key eq branchOrTag ? 'selected' : ''}>${branchTagInfo.key}</option>
                </c:forEach>
            </select>
        </c:when>
        <c:when test="${hasError}">
            <label for="newScmUriText"><fmt:message key="serverSettings.manageModules.downloadSources.scmUri" /></label>
            <input type="text" id="newScmUriText" name="newScmUri" value="${not empty newScmUri ? newScmUri : scmUri}"/>
            <label for="branchOrTagText"><fmt:message key="serverSettings.manageModules.downloadSources.branchOrTag" /></label>
            <input type="text" id="branchOrTagText" name="branchOrTag" value="${not empty branchOrTag ? branchOrTag : ''}"/>
        </c:when>
        <c:when test="${not empty newScmUri}">
            <input type="hidden" name="newScmUri" value="${newScmUri}"/>
            <input type="hidden" name="branchOrTag" value="${not empty branchOrTag ? branchOrTag : ''}"/>
        </c:when>
        </c:choose>
        </c:if>

        <fmt:message key='label.moduleName.copy' var="moduleNameCopy">
            <fmt:param value="${moduleName}"/>
        </fmt:message>
        <label for="newModuleName"><fmt:message key='label.moduleName'/></label>
        <input type="text" id="newModuleName" name="newModuleName" value="${not empty newModuleName ? newModuleName : moduleNameCopy}" required />

        <fmt:message key='label.moduleId.empty' var="moduleIdEmpty"/>
        <label for="newModuleId"><fmt:message key='label.moduleId'/></label>
        <input type="text" id="newModuleId" name="newModuleId" placeholder="${moduleIdEmpty}" value="${newModuleId}" />

        <label for="newGroupId"><fmt:message key='label.groupId'/></label>
        <input type="text" id="newGroupId" name="newGroupId" placeholder="org.jahia.modules" value="${newGroupId ? newGroupId : groupId}" />

        <label for="newDstPath"><fmt:message key='label.sources.folder'/></label>
        <input type="text" id="newDstPath" name="newDstPath" placeholder="${dstPath}" value="${newDstPath}" />
    </fieldset>
    <c:if test="${not empty moduleNodetypes}">
        <div class="alert alert-error">
            <fmt:message key="serverSettings.manageModules.duplicateModule.uninstallSrcModuleWarning">
                <fmt:param value="${fn:join(moduleNodetypes, ', ')}" />
            </fmt:message>
        </div>
    </c:if>
    <div>
        <button class="btn btn-primary" type="submit" name="_eventId_duplicateModule">
            <i class="icon-chevron-right icon-white"></i>
            &nbsp;<fmt:message key='label.next'/>
        </button>
        <button class="btn" type="button" onclick="$('#${currentNode.identifier}CancelForm').submit()">
            <i class="icon-ban-circle"></i>
            &nbsp;<fmt:message key='label.cancel' />
        </button>
    </div>
</form>

<form id="${currentNode.identifier}CancelForm" action="${flowExecutionUrl}" method="POST" onsubmit="workInProgress('${i18nWaiting}');">
    <input type="hidden" name="_eventId" value="cancelDuplicate" />
</form>
