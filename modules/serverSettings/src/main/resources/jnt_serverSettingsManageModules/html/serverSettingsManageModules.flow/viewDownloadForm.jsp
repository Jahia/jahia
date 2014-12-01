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
    <template:addResources type="inlinejavascript">
        <script type="text/javascript">
            $(document).ready(function() {
                $("#newScmUri").change(function() {
                    var selectedBranchTag = $(this).find('option:selected').text();
                    if (selectedBranchTag == "${scmMaster}") {
                        selectedBranchTag = "";
                    }
                    $('#branchOrTag').val(selectedBranchTag);
                });
            });
        </script>
    </template:addResources>
</c:if>
<h2>
    <fmt:message key='serverSettings.manageModules.downloadSources' />
</h2>
<c:if test="${not empty error}">
    <div class="alert alert-error"><fmt:message key='${error}'/></div>
</c:if>
<form action="${flowExecutionUrl}" method="POST" onsubmit="workInProgress('${i18nWaiting}');">
    <c:choose>
        <c:when test="${not empty branchTagInfos}">
            <fieldset>
                <input type="hidden" id="branchOrTag" name="branchOrTag" value="${not empty branchOrTag ? branchOrTag : ''}"/>
                <select name="newScmUri" id="newScmUri">
                    <c:forEach var="branchTagInfo" items="${branchTagInfos}">
                        <option value="${branchTagInfo.value}" ${branchTagInfo.key eq branchOrTag ? 'selected' : ''}>${branchTagInfo.key}</option>
                    </c:forEach>
                </select>
            </fieldset>
        </c:when>
        <c:otherwise>
            <fieldset>
                <div class="row-fluid">
                    <div class="span2">
                        <label for="newScmUriText"><fmt:message key="serverSettings.manageModules.downloadSources.scmUri" /></label>
                    </div>
                    <div class="span10">
                        <input class="span12" type="text" id="newScmUriText" name="newScmUri" value="${not empty newScmUri ? newScmUri : scmUri}"/>
                    </div>
                </div>
                <div class="row-fluid">
                    <div class="span2">
                        <label for="branchOrTagText"><fmt:message key="serverSettings.manageModules.downloadSources.branchOrTag" /></label>
                    </div>
                    <div class="span10">
                        <input class="span12" type="text" id="branchOrTagText" name="branchOrTag" value="${not empty branchOrTag ? branchOrTag : ''}"/>
                    </div>
                </div>
            </fieldset>
        </c:otherwise>
    </c:choose>
    <div>
        <button class="btn btn-primary" type="submit" name="_eventId_downloadSources">
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
    <input type="hidden" name="_eventId" value="cancel" />
</form>
