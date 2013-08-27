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
<%--@elvariable id="memberSearchCriteria" type="org.jahia.services.usermanager.SearchCriteria"--%>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js"/>
<template:addResources type="css"
                       resources="admin-bootstrap.css,jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<c:set var="multipleProvidersAvailable" value="${fn:length(providers) > 1}"/>

<c:set var="memberCount" value="${fn:length(members)}"/>
<c:set var="membersFound" value="${memberCount > 0}"/>

<c:set var="memberDisplayLimit" value="${siteSettingsProperties.memberDisplayLimit}"/>

<c:set var="isGroupEditable" value="${!providers[group.providerName].readOnly}"/>

<c:if test="${flowHandler.searchType eq 'users'}">
    <c:set var="prefix" value="u:"/>
    <c:set var="displayUsers" value="selected"/>
</c:if>
<c:if test="${flowHandler.searchType eq 'groups'}">
    <c:set var="prefix" value="g:"/>
    <c:set var="displayGroups" value="selected"/>
</c:if>

<script type="text/javascript">
    var addedMembers = []
    var removedMembers = []
    $(document).ready(function() {
        $(".selectedMember").change(function(event) {
            v = $(this).val();

            name = '${prefix}' + $(this).attr('value');
            if ($(this).is(':checked')) {
                if (removedMembers.indexOf(name) > -1) {
                    removedMembers.splice(removedMembers.indexOf(name),1)
                } else {
                    addedMembers[addedMembers.length] = name
                }
            } else {
                if (addedMembers.indexOf(name) > -1) {
                    addedMembers.splice(addedMembers.indexOf(name),1)
                } else {
                    removedMembers[removedMembers.length] = name
                }
            }

            if (addedMembers.length == 0 && removedMembers.length == 0) {
                $('#saveButton').attr('disabled', 'disabled')
            } else {
                $('#saveButton').removeAttr("disabled")
            }
//            if ($(this).is(':checked') && $('#removedMembers'))})
        })

        $('#cbSelectedAllMembers').click(function() {
            var state=this.checked;
            $.each($(':checkbox[name="selectedMembers"]'), function() {
                if (this.checked != state) {
                    this.checked = state;
                    $(this).change()
                }
            });
        });

        $("#saveForm").submit(function() {
            workInProgress();
            $("#addedMembers").val(addedMembers)
            $("#removedMembers").val(removedMembers)
        })
    })

</script>

<div>
    <form action="${flowExecutionUrl}" method="post" style="display: inline;">
        <div>
            <h2>${role}</h2>
            <button class="btn" type="submit" name="_eventId_rolesList">
                <i class="icon-arrow-left"></i>
                &nbsp;<fmt:message key="siteSettings.label.backToRoles"/>
            </button>
            <button class="btn ${displayUsers}" type="submit" name="_eventId_users">
                <i class="icon-user"></i>
                &nbsp;<fmt:message key="label.users"/>
            </button>

            <button class="btn ${displayGroups}" type="submit" name="_eventId_groups">
                <i class="icon-group"></i>
                &nbsp;<fmt:message key="label.groups"/>
            </button>

        </div>
    </form>
</div>

<div class="box-1">
    <form class="form-inline " action="${flowExecutionUrl}" id="searchForm" method="post">
        <input type="hidden" id="searchIn" name="searchIn" value="allProps"/>
        <fieldset>
            <h2><fmt:message key="label.search"/></h2>

            <div class="input-append">
                <label style="display: none;" for="searchString"><fmt:message key="label.search"/></label>
                <input class="span6" type="text" id="searchString" name="searchString"
                       value='${memberSearchCriteria.searchString}'
                       onkeydown="if (event.keyCode == 13) submitForm('search');"/>
                <button class="btn btn-primary" type="submit" name="_eventId_search">
                    <i class="icon-search icon-white"></i>
                    &nbsp;<fmt:message key='label.search'/>
                </button>
            </div>
            <c:if test="${multipleProvidersAvailable}">
                <br/>
                <label for="storedOn"><span class="badge badge-info"><fmt:message
                        key="label.on"/></span></label>
                <input type="radio" name="storedOn" value="everywhere"
                    ${empty memberSearchCriteria.storedOn || memberSearchCriteria.storedOn == 'everywhere' ? ' checked="checked" ' : ''}
                       onclick="$('.provCheck').attr('disabled',true);">&nbsp;<fmt:message
                    key="label.everyWhere"/>

                <input type="radio" name="storedOn" value="providers"
                    ${memberSearchCriteria.storedOn == 'providers' ? 'checked="checked"' : ''}
                       onclick="$('.provCheck').removeAttr('disabled');"/>&nbsp;<fmt:message
                    key="label.providers"/>

                <c:forEach items="${providers}" var="curProvider">
                    <input type="checkbox" class="provCheck" name="providers" value="${curProvider.key}"
                        ${memberSearchCriteria.storedOn != 'providers' ? 'disabled="disabled"' : ''}
                        ${empty memberSearchCriteria.providers || functions:contains(memberSearchCriteria.providers, curProvider.key) ? 'checked="checked"' : ''}/>
                    <fmt:message var="i18nProviderLabel" key="providers.${curProvider.key}.label"/>
                    ${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? curProvider.key : i18nProviderLabel)}
                </c:forEach>
            </c:if>
        </fieldset>
    </form>
</div>

</div>

<form action="${flowExecutionUrl}" method="post" id="saveForm">
    <input id="addedMembers" type="hidden" name="addedMembers"/>
    <input id="removedMembers" type="hidden" name="removedMembers"/>
    <button class="btn btn-primary" type="submit" name="_eventId_save" id="saveButton" disabled="disabled">
        <i class="icon-ok"></i>
        &nbsp;<fmt:message key="label.save"/>
    </button>

</form>

<div>
    <c:set var="principalsCount" value="${fn:length(principals)}"/>
    <c:set var="principalsFound" value="${principalsCount > 0}"/>

    <c:if test="${principalsCount > memberDisplayLimit}">
        <div class="alert alert-info">
            <fmt:message key="siteSettings.${flowHandler.searchType}.found">
                <fmt:param value="${principalsCount}"/>
                <fmt:param value="${memberDisplayLimit}"/>
            </fmt:message>
        </div>
    </c:if>

    <table class="table table-bordered table-striped table-hover">
        <thead>
        <tr>
            <th width="2%"><input type="checkbox" name="selectedAllMembers" id="cbSelectedAllMembers"/></th>
            <th><fmt:message key="label.name"/></th>
            <c:if test="${multipleProvidersAvailable}">
                <th width="10%"><fmt:message key="column.provider.label"/></th>
            </c:if>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${!principalsFound}">
                <tr>
                    <td colspan="${multipleProvidersAvailable ? '3' : '2'}"><fmt:message key="label.noItemFound"/></td>
                </tr>
            </c:when>
            <c:otherwise>
                <c:forEach items="${principals}" var="principal" end="${memberDisplayLimit - 1}" varStatus="loopStatus">
                    <tr>
                        <td><input class="selectedMember" type="checkbox" name="selectedMembers" value="${principal.name}" ${functions:contains(members, principal) ? 'checked="checked"' : ''}/> </td>
                        <td>
                                ${fn:escapeXml(user:displayName(principal))}
                        </td>
                        <c:if test="${multipleProvidersAvailable}">
                            <fmt:message var="i18nProviderLabel" key="providers.${principal.providerName}.label"/>
                            <td>${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? principal.providerName : i18nProviderLabel)}</td>
                        </c:if>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>


</div>




