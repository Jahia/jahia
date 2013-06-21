<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
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
<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>
<%--@elvariable id="searchCriteria" type="org.jahia.modules.sitesettings.groups.SearchCriteria"--%>

<c:set var="groupDisplayLimit" value="${siteSettingsProperties.groupDisplayLimit}"/>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js"/>
<template:addResources type="css" resources="admin-bootstrap.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<template:addResources>
<script type="text/javascript">
function submitGroupForm(act, group) {
	$('#groupFormAction').val(act);
	$('#groupFormSelected').val(group);
	$('#groupForm').submit();
}
</script>
</template:addResources>

<c:set var="site" value="${renderContext.mainResource.node.resolveSite}"/>

<h2><fmt:message key="label.manageGroups"/> - ${fn:escapeXml(site.displayableName)}</h2>

<c:set var="multipleProvidersAvailable" value="${fn:length(providers) > 1}"/>

<div class="box-1">
    <form class="form-inline " action="${flowExecutionUrl}" id="searchForm" method="post">
        <input type="hidden" id="searchIn" name="searchIn" value="allProps"/>
        <fieldset>
            <h2><fmt:message key="label.search"/></h2>
            <div class="input-append">
                <label style="display: none;"  for="searchString"><fmt:message key="label.search"/></label>
                <input class="span6" type="text" id="searchString" name="searchString"
                       value='${searchCriteria.searchString}'
                       onkeydown="if (event.keyCode == 13) submitForm('search');"/>
                <button class="btn btn-primary" type="submit" name="_eventId_search">
                    <i class="icon-search icon-white"></i>
                    &nbsp;<fmt:message key='label.search'/>
                </button>
            </div>
            <c:if test="${multipleProvidersAvailable}">
                <br/>
                <label for="storedOn"><span class="badge badge-info"><fmt:message key="label.on"/></span></label>
                <input type="radio" name="storedOn" value="everywhere" 
                    ${empty searchCriteria.storedOn || searchCriteria.storedOn == 'everywhere' ? ' checked="checked" ' : ''}   
                    onclick="$('.provCheck').attr('disabled',true);">&nbsp;<fmt:message
                    key="label.everyWhere"/>

                <input type="radio" name="storedOn" value="providers"
                       ${searchCriteria.storedOn == 'providers' ? 'checked="checked"' : ''}
                       onclick="$('.provCheck').removeAttr('disabled');"/>&nbsp;<fmt:message
                    key="label.providers"/>
                    
                <c:forEach items="${providers}" var="curProvider">
                    <input type="checkbox" class="provCheck" name="providers" value="${curProvider.key}"
                           ${searchCriteria.storedOn != 'providers' ? 'disabled="disabled"' : ''}
                           ${empty searchCriteria.providers || functions:contains(searchCriteria.providers, curProvider.key) ? 'checked="checked"' : ''}/>
                    <fmt:message var="i18nProviderLabel" key="providers.${curProvider.key}.label"/>
                    ${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? curProvider.key : i18nProviderLabel)}
                </c:forEach>
            </c:if>
        </fieldset>
    </form>
</div>


<div>
    <div>
        <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
            <button class="btn" type="submit" name="_eventId_createGroup">
                <i class="icon-plus"></i>
                &nbsp;<fmt:message key="siteSettings.groups.create"/>
            </button>
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

    <div>
        <c:set var="groupCount" value="${fn:length(groups)}"/>
        <c:set var="groupsFound" value="${groupCount > 0}"/>
        
        <c:if test="${groupCount > groupDisplayLimit}">
            <div class="alert alert-success">
                <fmt:message key="siteSettings.groups.found">
                    <fmt:param value="${groupCount}"/>
                    <fmt:param value="${groupDisplayLimit}"/>
                </fmt:message>
            </div>
        </c:if>
        
        <c:if test="${groupsFound}">
            <form action="${flowExecutionUrl}" method="post" style="display: inline;" id="groupForm">
                <input type="hidden" name="selectedGroup" id="groupFormSelected"/>
                <input type="hidden" id="groupFormAction" name="_eventId" value="" />
            </form>
        </c:if>
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th width="3%">#</th>
                <th><fmt:message key="label.name"/></th>
                <c:if test="${multipleProvidersAvailable}">
                    <th width="10%"><fmt:message key="column.provider.label"/></th>
                </c:if>
                <th width="20%"><fmt:message key="label.actions"/></th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <%--@elvariable id="groups" type="java.util.List"--%>
                <c:when test="${!groupsFound}">
                    <tr>
                        <td colspan="${multipleProvidersAvailable ? '4' : '3'}"><fmt:message key="label.noItemFound"/></td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <fmt:message var="i18nEdit" key="label.edit"/><c:set var="i18nEdit" value="${fn:escapeXml(i18nEdit)}"/>
                    <fmt:message var="i18nCopy" key="label.copy"/><c:set var="i18nCopy" value="${fn:escapeXml(i18nCopy)}"/>
                    <fmt:message var="i18nRemove" key="label.remove"/><c:set var="i18nRemove" value="${fn:escapeXml(i18nRemove)}"/>
                    <fmt:message var="i18nRemoveNote" key="siteSettings.groups.remove.confirm"/>
                    <fmt:message var="i18nContinue" key="label.confirmContinue"/>
                    <c:set var="i18nRemoveConfirm" value="${functions:escapeJavaScript(i18nRemoveNote)} ${functions:escapeJavaScript(i18nContinue)}"/>
                    <c:forEach items="${groups}" var="grp" end="${groupDisplayLimit - 1}" varStatus="loopStatus">
                        <tr>
                            <td>${loopStatus.count}</td>
                            <td>
                                <a title="${i18nEdit}" href="#edit" onclick="submitGroupForm('editGroup', '${grp.groupKey}'); return false;">${fn:escapeXml(user:displayName(grp))}</a>
                            </td>
                            <c:if test="${multipleProvidersAvailable}">
                                <fmt:message var="i18nProviderLabel" key="providers.${grp.providerName}.label"/>
                                <td>${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? grp.providerName : i18nProviderLabel)}</td>
                            </c:if>
                            <td>
                                <a style="margin-bottom:0;" class="btn btn-small" title="${i18nEdit}" href="#edit" onclick="submitGroupForm('editGroup', '${grp.groupKey}'); return false;">
                                    <i class="icon-edit"></i>
                                </a>
                                <a style="margin-bottom:0;" class="btn btn-small" title="${i18nCopy}" href="#copy" onclick="submitGroupForm('copyGroup', '${grp.groupKey}'); return false;">
                                    <i class="icon-share"></i>
                                </a>
                                <c:if test="${!providers[grp.providerName].readOnly && !functions:contains(systemGroups, grp.groupKey)}">
                                    <a style="margin-bottom:0;" class="btn btn-danger btn-small" title="${i18nRemove}" href="#delete" onclick="if (confirm('${i18nRemoveConfirm}')) { workInProgress(); submitGroupForm('removeGroup', '${grp.groupKey}');} return false;">
                                        <i class="icon-remove icon-white"></i>
                                    </a>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>
