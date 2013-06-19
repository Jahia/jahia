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

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js"/>
<template:addResources type="css" resources="admin-bootstrap.css,jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources>
    <link type="text/css" href="<c:url value='/gwt/resources/css/gwt-1.4.min.css'/>" rel="stylesheet"/>
</template:addResources>

<fmt:message var="i18nRemoveMultipleConfirm" key="siteSettings.groups.removeMembers.confirm"/>
<fmt:message var="i18nContinue" key="label.confirmContinue"/>

<c:set var="isGroupEditable" value="${!providers[group.providerName].readOnly}"/>

<c:if test="${isGroupEditable}">
<template:addResources>
<script type="text/javascript">
var newMembersArray = new Array();
function addOptions(text, value) {
	$('#newMembers').show();
	if ($.inArray(value, newMembersArray) == -1) {
		newMembersArray.push(value);
		var tokens=text.split('|');
		var principal=$.trim(tokens[0]);
		var provider=$.trim(tokens[1]);
		var name=$.trim(tokens[2]);
		var displayName=$.trim(tokens[3]);
		var key = value.substring(0, 1) + ':' + value.substring(1, value.length);
		$('#newMembersTableBody').append($('<tr><td><input type="hidden" name="newMembers" value="' 
				+ key + '"/>' + name + '</td><td>'
				+ displayName + '</td></tr>'));
		}
}
function removeGroupMember(confirmMsg, member) {
	if (confirm(confirmMsg)) {
		$.each($(':checkbox[name=selectedMembers]'), function() {
			this.checked=$(this).val() == member;
		});
		workInProgress();
		return true;
	} else {
		return false;
	}
}
function removeMultipleGroupMembers() {
	if ($('input:checked[name=selectedMembers]').length == 0) {
		<fmt:message var="i18nRemoveMultipleNothingSelected" key="siteSettings.groups.removeMembers.nothingSelected"/>
		alert('${functions:escapeJavaScript(i18nRemoveMultipleNothingSelected)}');
		return false;
	}
	if (confirm('${functions:escapeJavaScript(i18nRemoveMultipleConfirm)} ${functions:escapeJavaScript(i18nContinue)}')) {
		workInProgress();
		return true;
	}
	return false;
}
$(document).ready(function() {
	$('#btnNewMembersReset').click(function() {
		$('#newMembersTableBody tr').remove();
		newMembersArray=new Array();
		$('#newMembers').hide();
		return false;
	});
	$(':checkbox[name="selectedMembers"]').click(function() {
		if (!this.checked) {
			$.each($('#cbSelectedAllMembers'), function() {
				this.checked = false;
			}) 
		}
	})
	$('#cbSelectedAllMembers').click(function() {
		var state=this.checked;
		$.each($(':checkbox[name="selectedMembers"]'), function() {
			this.checked=state;
		}); 
	});
})
</script>
</template:addResources>
</c:if>

<h2><fmt:message key="label.group"/>: ${fn:escapeXml(user:displayName(group))}</h2>

<c:set var="multipleProvidersAvailable" value="${fn:length(providers) > 1}"/>
<c:set var="members" value="${group.members}"/>
<c:set var="membersFound" value="${fn:length(members) > 0}"/>

<form action="${flowExecutionUrl}" method="post" style="display: inline;">
<div>
    <div>
        <c:if test="${isGroupEditable}">
            <button class="btn" type="submit" name="addMembers" onclick="openUserGroupSelect('','', 'Principal|Provider|Name|Properties,100'); return false;">
                <i class="icon-plus"></i>
                &nbsp;<fmt:message key="siteSettings.groups.addMembers"/>
            </button>

            <c:if test="${membersFound}">
                <button class="btn" type="submit" name="_eventId_removeMembers" onclick="return removeMultipleGroupMembers();">
                    <i class="icon-remove"></i>
                    &nbsp;<fmt:message key="siteSettings.groups.removeMembers"/>
                </button>
            </c:if>
        </c:if>

            <button class="btn" type="submit" name="_eventId_cancel">
                <i class="icon-arrow-left"></i>
                &nbsp;<fmt:message key="label.backToGroupList"/>
            </button>
    </div>
    
    <c:if test="${isGroupEditable}">
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
    </c:if>

    <div>
    <c:if test="${isGroupEditable}">
        <div id="newMembers" style="display:none">
            <h2><fmt:message key="siteSettings.groups.newMembers"/></h2>
            <table id="newMembersTable" class="table table-bordered table-striped table-hover" style="width: 50%">
                <thead>
                    <tr>
                        <th><fmt:message key="label.name"/></th>
                        <th><fmt:message key="label.properties"/></th>
                    </tr>
                </thead>
                <tbody id="newMembersTableBody">
                </tbody>
            </table>
            <button class="btn btn-primary" type="submit" name="_eventId_addMembers" onclick="workInProgress(); return true;">
               <i class="icon-ok"></i>
               &nbsp;<fmt:message key="label.saveChanges"/>
            </button>
            <button class="btn" type="submit" name="reset" id="btnNewMembersReset">
                <i class="icon-ban-circle"></i>
                &nbsp;<fmt:message key="label.reset"/>
            </button>
        </div>
    </c:if>
        
        <h2><fmt:message key="members.label"/></h2>
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <c:if test="${isGroupEditable}">
                <th width="2%"><input type="checkbox" name="selectedAllMembers" id="cbSelectedAllMembers"/></th>
                </c:if>
                <th width="3%">#</th>
                <th width="3%">&nbsp;</th>
                <th><fmt:message key="label.name"/></th>
                <th><fmt:message key="label.properties"/></th>
                <c:if test="${multipleProvidersAvailable}">
                    <th width="10%"><fmt:message key="column.provider.label"/></th>
                </c:if>
                <c:if test="${isGroupEditable}">
                <th width="20%"><fmt:message key="label.actions"/></th>
                </c:if>
            </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${!membersFound}">
                        <tr>
                            <td colspan="${(multipleProvidersAvailable ? 7 : 6) - (isGroupEditable ? 0 : 2)}"><fmt:message key="label.noItemFound"/></td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <fmt:message var="i18nRemove" key="label.remove"/><c:set var="i18nRemove" value="${fn:escapeXml(i18nRemove)}"/>
                        <c:forEach items="${members}" var="member" varStatus="loopStatus">
                            <c:set var="principalType" value="${user:principalType(member)}"/>
                            <c:set var="principalIcon" value="${principalType == 'u' ? 'usersmall' : 'group-icon'}"/>
                            <c:set var="principalKey" value="${principalType}:${principalType == 'u' ? member.userKey : member.groupKey}"/>
                            <tr>
                                <c:if test="${isGroupEditable}">
                                <td>
                                    <input type="checkbox" name="selectedMembers" value="${principalKey}"/>
                                </td>
                                </c:if>
                                <td>
                                    ${loopStatus.count}
                                </td>
                                <td>
                                    <img src="<c:url value='/modules/default/images/${principalIcon}.png'/>" alt="${principalType}" width="16" height="16"/>
                                </td>
                                <td>
                                    ${fn:escapeXml(user:displayName(member))}
                                </td>
                                <td>
                                    ${fn:escapeXml(user:fullName(member))}
                                </td>
                                <c:if test="${multipleProvidersAvailable}">
                                    <fmt:message var="i18nProviderLabel" key="providers.${member.providerName}.label"/>
                                    <td>${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? member.providerName : i18nProviderLabel)}</td>
                                </c:if>
                                <c:if test="${isGroupEditable}">
                                <td>
                                    <fmt:message var="i18RemoveConfirm" key="siteSettings.groups.removeMember.confirm">
                                        <fmt:param value="${fn:escapeXml(member.name)}"/>
                                    </fmt:message>
                                    <button style="margin-bottom:0;" class="btn btn-danger btn-small" type="submit" name="_eventId_removeMembers"
                                        onclick="return removeGroupMember('${functions:escapeJavaScript(i18RemoveConfirm)} ${functions:escapeJavaScript(i18nContinue)}', '${principalKey}')">
                                        <i class="icon-remove icon-white"></i>
                                    </button>
                                </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

</form>