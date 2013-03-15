<%--@elvariable id="searchCriteria" type="org.jahia.modules.serversettings.users.management.SearchCriteria"--%>
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
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<script type="text/javascript">
    $(document).ready(function () {
        $(".needUsersSelection").submit(function () {
            $("input[name='selectedUsers']").val($("input[name='userSelected']").val());
            return true;
        })
    });
</script>
<h3><fmt:message key="label.manageUsers"/></h3>

<div>
    <div>
        <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
            <input type="submit" name="_eventId_addUser"
                   value="<fmt:message key="org.jahia.admin.users.ManageUsers.createNewUser.label"/>"/>
        </form>
        <form action="${flowExecutionUrl}" method="POST" class="needUsersSelection" style="display: inline;">
            <input type="hidden" name="selectedUsers"/>
            <input type="submit" name="_eventId_editUser"
                   value="<fmt:message key="org.jahia.admin.users.ManageUsers.editViewProp.label"/>"/>
        </form>
        <form action="${flowExecutionUrl}" method="POST" class="needUsersSelection" style="display: inline;">
            <input type="hidden" name="selectedUsers"/>
            <input type="submit" name="_eventId_removeUser"
                   value="<fmt:message key="org.jahia.admin.users.ManageUsers.removeSelectedUser.label"/>"/>
        </form>
        <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
            <input type="submit" name="_eventId_bulkAddUser"
                   value="<fmt:message key="org.jahia.admin.users.ManageUsers.batchCreateUsers.label"/>"/>
        </form>
    </div>

    <form action="${flowExecutionUrl}" id="searchForm" method="post">
        <fieldset>
            <label for="searchString"><fmt:message key="label.search"/></label>
            <input type="text" id="searchString" name="searchString" size="15"
                   value='${searchCriteria.searchString}'
                   onkeydown="if (event.keyCode == 13) javascript:submitForm('search');"/><br/>
            <label for="searchIn"><fmt:message key="label.in"/></label>
            <input type="radio" id="searchIn" name="searchIn" value="allProps"
                   <c:if test="${empty searchCriteria.searchIn or searchCriteria.searchIn eq 'allProps'}">checked</c:if>
                   onclick="$('.propCheck').attr('disabled',true);">&nbsp;<fmt:message
                key="label.allProperties"/>
            <input type="radio" name="searchIn" value="properties"
                   <c:if test="${searchCriteria.searchIn eq 'properties'}">checked</c:if>
                   onclick="$('.propCheck').removeAttr('disabled');">&nbsp;<fmt:message
                key="label.properties"/><br>
            <fieldset>

                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="checkbox" class="propCheck" name="properties" value="username"
                       <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                       <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'username')}">checked="checked"</c:if> >
                <nobr><fmt:message key="label.username"/></nobr>
                <br>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="checkbox" class="propCheck" name="properties" value="j:firstName"
                       <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                       <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:firstName')}">checked="checked"</c:if> >
                <fmt:message key="org.jahia.admin.firstName.label"/><br>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="checkbox" class="propCheck" name="properties" value="j:lastName"
                       <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                       <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:lastName')}">checked="checked"</c:if> >
                <fmt:message key="org.jahia.admin.lastName.label"/><br>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="checkbox" class="propCheck" name="properties" value="j:email"
                       <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                       <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:email')}">checked="checked"</c:if> >
                <fmt:message key="label.email"/><br>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="checkbox" class="propCheck" name="properties" value="j:organization"
                       <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                       <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:organization')}">checked="checked"</c:if> >
                <nobr><fmt:message key="org.jahia.admin.organization.label"/></nobr>
                <br>
            </fieldset>
            <label for="storedOn"><fmt:message key="label.on"/></label>
            <c:if test="${fn:length(providersList) gt 1}">
                <input type="radio" name="storedOn" value="everywhere"
                       <c:if test="${empty searchCriteria.storedOn or searchCriteria.storedOn eq 'everywhere'}">checked</c:if>
                       onclick="$('.provCheck').attr('disabled',true);">&nbsp;<fmt:message
                    key="label.everyWhere"/>
            </c:if>

            <input type="radio" id="storedOn" name="storedOn" value="providers"
            <c:if test="${fn:length(providersList) le 1 or searchCriteria.storedOn eq 'providers'}">
                   checked </c:if>
            <c:if test="${fn:length(providersList) gt 1}">
                   onclick="$('.provCheck').removeAttr('disabled');"</c:if>>&nbsp;<fmt:message
                key="label.providers"/></nobr>&nbsp;:<br>
            <fieldset>
                <c:forEach items="${providersList}" var="curProvider">
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <input type="checkbox" class="provCheck" name="providers" value="${curProvider.key}"
                           <c:if test="${fn:length(providersList) le 1 or searchCriteria.storedOn ne 'providers'}">disabled </c:if>
                    <c:if test="${fn:length(providersList) le 1 or (not empty searchCriteria.providers and functions:contains(searchCriteria.providers, curProvider.key))}">
                           checked </c:if>>
                    ${curProvider.key}<br>
                </c:forEach>
                <input type="submit" name="_eventId_search"
                       value="<fmt:message key="label.search"/>"/>
            </fieldset>
        </fieldset>
    </form>
    <div>
        <label><fmt:message key="org.jahia.admin.users.ManageUsers.searchResult.label"/></label><br/>
        <table border="1" cellpadding="5" cellspacing="5">
            <thead>
            <tr>
                <th>&nbsp;</th>
                <th class="sortable"><fmt:message key="label.name"/></th>
                <th class="sortable"><fmt:message key="label.properties"/></th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${fn:length(users) eq 0}">
                    <tr>
                        <td colspan="3"><fmt:message key="org.jahia.admin.users.ManageUsers.noUserFound.label"/></td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${users}" var="curUser">
                        <tr class="sortable-row">
                            <td><input type="radio" name="userSelected" value="${fn:escapeXml(curUser.userKey)}"></td>
                            <td>${user:displayName(curUser)}</td>
                            <td>${user:fullName(curUser)}</td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>
