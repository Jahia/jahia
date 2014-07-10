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
<h2>
    <fmt:message key='serverSettings.manageModules.duplicateModule' />
</h2>
<c:if test="${not empty error}">
    <div class="alert alert-error">${error}</div>
</c:if>

<form action="${flowExecutionUrl}" method="POST" onsubmit="workInProgress('${i18nWaiting}');">
    <fieldset>
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
    <c:if test="${containsNodetypes}">
        <div class="alert alert-error">
            <fmt:message key="serverSettings.manageModules.duplicateModule.uninstallSrcModuleWarning" />
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
