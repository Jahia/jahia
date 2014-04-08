<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%@ page import="org.jahia.settings.SettingsBean "%>
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
<%--@elvariable id="searchCriteria" type="org.jahia.modules.serversettings.users.management.SearchCriteria"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,jquery.metadata.js,jquery.tablesorter.js,jquery.tablecloth.js"/>
<template:addResources type="css" resources="admin-bootstrap.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css,tablecloth.css"/>

<c:set var="userDisplayLimit" value="${serverSettingsProperties.userDisplayLimit}"/>
<c:set var="jcrUserCountLimit" value="<%= SettingsBean.getInstance().getJahiaJCRUserCountLimit() %>"/>

<template:addResources>
<script type="text/javascript">
    $(document).ready(function () {
        $(".needUsersSelection").submit(function () {
            var selected = $("input[name='userSelected']:checked").val();
            if(undefined==selected) {
            	<fmt:message key="serverSettings.user.select.one" var="i18nSelectUser"/>
                alert('${functions:escapeJavaScript(i18nSelectUser)}');
                return false;
            }
            $("input[name='selectedUsers']").val(selected);
            return true;
        })

        $(".needUsersSelectionCheckbox").submit(function () {
            var selected = $("input[name='userToDelete']:checked").map(function() {
                return this.value;
            }).get().join(',');
            console.log(selected);
            if(undefined==selected) {
                <fmt:message key="serverSettings.user.select.one" var="i18nSelectUser"/>
                alert('${functions:escapeJavaScript(i18nSelectUser)}');
                return false;
            }
            $("input[name='selectedUsers']").val(selected);
            return true;
        })
        $("table").tablecloth({
            theme: "default",
            sortable: true
        });
    });
    function toggleModeToBulkDelete(){
        $('.userRadio').toggle();
        $('.userCheckbox').toggle();
        $('.singleMode').toggle();
        $('#bulkDeleteForm button').toggle();
    };
</script>
</template:addResources>

<c:set var="multipleProvidersAvailable" value="${fn:length(providersList) > 1}"/>

<div class="box-1">
    <form class="form-inline " action="${flowExecutionUrl}" id="searchForm" method="post">
        <fieldset>
            <h2><fmt:message key="label.search"/></h2>
            <div class="input-append">
                <label style="display: none;"  for="searchString"><fmt:message key="label.search"/></label>
                <input class="span6" type="text" id="searchString" name="searchString"
                       value='${searchCriteria.searchString}'
                       onkeydown="if (event.keyCode == 13) submitForm('search');"/>
                <button class="btn btn-primary" type="submit"  name="_eventId_search">
                    <i class="icon-search icon-white"></i>
                    &nbsp;<fmt:message key='label.search'/>
                </button>
            </div>
            <br/>
            <br/>
            <label for="searchIn"><span class="badge badge-info"><fmt:message key="label.in"/></span></label>
            <input type="radio" id="searchIn" name="searchIn" value="allProps"
                   <c:if test="${empty searchCriteria.searchIn or searchCriteria.searchIn eq 'allProps'}">checked</c:if>
                   onclick="$('.propCheck').attr('disabled',true);">&nbsp;<label for="searchIn"><fmt:message
                key="label.allProperties"/></label>
            <input type="radio" name="searchIn" id="searchInProperties" value="properties"
                   <c:if test="${searchCriteria.searchIn eq 'properties'}">checked</c:if>
                   onclick="$('.propCheck').removeAttr('disabled');">&nbsp;<label for="searchInProperties"><fmt:message
                key="serverSettings.user.properties.selected"/></label>:&nbsp;


            <input type="checkbox" class="propCheck" name="properties" value="username" id="propsUsersname"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'username')}">checked="checked"</c:if> >
            <label for="propsUsersname"><fmt:message key="label.username"/></label>

            <input type="checkbox" class="propCheck" name="properties" value="j:firstName" id="propsFirstName"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:firstName')}">checked="checked"</c:if> >
            <label for="propsFirstName"><fmt:message key="label.firstName"/></label>

            <input type="checkbox" class="propCheck" name="properties" value="j:lastName" id="propsLastName"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:lastName')}">checked="checked"</c:if> >
            <label for="propsLastName"><fmt:message key="label.lastName"/></label>

            <input type="checkbox" class="propCheck" name="properties" value="j:email" id="propsEmail"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:email')}">checked="checked"</c:if> >
            <label for="propsEmail"><fmt:message key="label.email"/></label>

            <input type="checkbox" class="propCheck" name="properties" value="j:organization" id="propsOrganization"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:organization')}">checked="checked"</c:if> >
            <label for="propsOrganization"><fmt:message key="label.organization"/></label>

            <c:if test="${multipleProvidersAvailable}">
            <br/>
            <label for="storedOn"><span class="badge badge-info"><fmt:message key="label.on"/></span></label>
            <%--@elvariable id="providersList" type="java.util.List"--%>
            <input type="radio" name="storedOn" id="storeOnEverywhere" value="everywhere"
                       <c:if test="${empty searchCriteria.storedOn or searchCriteria.storedOn eq 'everywhere'}">checked</c:if>
                       onclick="$('.provCheck').attr('disabled',true);">&nbsp;<label for="storeOnEverywhere"><fmt:message
                    key="label.everyWhere"/></label>

            <input type="radio" id="storedOn" name="storedOn" value="providers"
            <c:if test="${searchCriteria.storedOn eq 'providers'}">
                   checked </c:if>
                   onclick="$('.provCheck').removeAttr('disabled');">&nbsp;<label for="storedOn"><fmt:message key="label.providers"/></label>:&nbsp;

            <c:forEach items="${providersList}" var="curProvider">
                <input type="checkbox" class="provCheck" name="providers" id="provider-${curProvider.key}" value="${curProvider.key}"
                       <c:if test="${fn:length(providersList) le 1 or searchCriteria.storedOn ne 'providers'}">disabled </c:if>
                <c:if test="${fn:length(providersList) le 1 or (not empty searchCriteria.providers and functions:contains(searchCriteria.providers, curProvider.key))}">
                       checked </c:if>>
                <label for="provider-${curProvider.key}">
                <fmt:message var="i18nProviderLabel" key="providers.${curProvider.key}.label"/>
                ${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? curProvider.key : i18nProviderLabel)}
                </label>
            </c:forEach>

            </c:if>
        </fieldset>
    </form>
</div>
<h2><fmt:message key="label.manageUsers"/></h2>

<div>
    <div>
        <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
            <button class="btn" type="submit" name="_eventId_addUser" >
                <i class="icon-plus"></i>
                &nbsp;<fmt:message key='serverSettings.user.create'/>
            </button>
        </form>
        <form action="${flowExecutionUrl}" method="POST" class="needUsersSelection singleMode" style="display: inline;">
            <input type="hidden" name="selectedUsers"/>
            <button class="btn" type="submit" name="_eventId_editUser" >
                <i class="icon-edit"></i>
                &nbsp;<fmt:message key='serverSettings.user.edit'/>
            </button>
        </form>
        <form action="${flowExecutionUrl}" method="POST" class="needUsersSelection singleMode" style="display: inline;">
            <input type="hidden" name="selectedUsers"/>
            <button class="btn" type="submit" name="_eventId_removeUser" >
                <i class="icon-remove"></i>
                &nbsp;<fmt:message key='serverSettings.user.remove'/>
            </button>
        </form>
        <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
            <button class="btn" type="submit" name="_eventId_bulkAddUser" >
                <i class="icon-cog"></i>
                &nbsp;<fmt:message key='serverSettings.users.bulk.create'/>
            </button>
        </form>
        <button class="btn" type="submit" onclick="toggleModeToBulkDelete(); ">
            <i class="icon-check"></i>
            &nbsp;<fmt:message key='serverSettings.users.bulk.remove.switch'/>
        </button>
        <form action="${flowExecutionUrl}" method="POST" style="display: inline" id="bulkDeleteForm" class="needUsersSelectionCheckbox">
            <input type="hidden" name="selectedUsers"/>
            <button class="btn" type="submit" name="_eventId_bulkDeleteUser" style="display: none">
                <i class="icon-remove"></i>
                &nbsp;<fmt:message key='serverSettings.users.bulk.delete'/>
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

    <c:set var="userCount" value="${fn:length(users)}"/>
    <div>
        <h2><fmt:message key="serverSettings.user.search.result"/></h2>
        <div class="alert alert-info">
            <c:if test="${userCount < userDisplayLimit || jcrUserCountLimit < 0}">
            <fmt:message key="serverSettings.user.search.found">
                <fmt:param value="${userCount}"/>
            </fmt:message></c:if><c:if test="${userCount > userDisplayLimit}">&nbsp;<fmt:message key="serverSettings.user.search.found.limit">
                    <fmt:param value="${userDisplayLimit}"/>
                </fmt:message>
            </c:if>
        </div>
        
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th class="sortable" width="5%">#</th>
                <th class="{sorter: false}" width="5%">&nbsp;</th>
                <th class="sortable"><fmt:message key="label.name"/></th>
                <th width="45%" class="sortable"><fmt:message key="label.properties"/></th>
                <c:if test="${multipleProvidersAvailable}">
                    <th width="10%"><fmt:message key="column.provider.label"/></th>
                </c:if>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <%--@elvariable id="users" type="java.util.List"--%>
                <c:when test="${userCount eq 0}">
                    <tr>
                        <td colspan="${multipleProvidersAvailable ? '5' : '4'}"><fmt:message key="serverSettings.user.search.no.result"/></td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${users}" var="curUser" end="${userDisplayLimit - 1}" varStatus="loopStatus">
                        <tr class="sortable-row">
                            <td>${loopStatus.count}</td>
                            <td><input type="radio" name="userSelected" value="${fn:escapeXml(curUser.userKey)}" class="userRadio"><input type="checkbox" name="userToDelete" value="${fn:escapeXml(curUser.userKey)}" class="userCheckbox" style="display: none"></td>
                            <td>${user:displayName(curUser)}</td>
                            <td>${user:fullName(curUser)}</td>
                            <c:if test="${multipleProvidersAvailable}">
                                <fmt:message var="i18nProviderLabel" key="providers.${curUser.providerName}.label"/>
                                <td>${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? curUser.providerName : i18nProviderLabel)}</td>
                            </c:if>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>
