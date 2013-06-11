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
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,,jquery.metadata.js,jquery.tablesorter.js,jquery.tablecloth.js"/>
<template:addResources type="css" resources="admin-bootstrap.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css,tablecloth.css"/>

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
    });
</script>
<script type="text/javascript" charset="utf-8">
    $(document).ready(function() {
        $("table").tablecloth({
            theme: "default",
            sortable: true
        });
    });
</script>
</template:addResources>

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
                   onclick="$('.propCheck').attr('disabled',true);">&nbsp;<fmt:message
                key="label.allProperties"/>
            <input type="radio" name="searchIn" value="properties"
                   <c:if test="${searchCriteria.searchIn eq 'properties'}">checked</c:if>
                   onclick="$('.propCheck').removeAttr('disabled');">&nbsp;<fmt:message
                key="serverSettings.user.properties.selected"/>:&nbsp;


            <input type="checkbox" class="propCheck" name="properties" value="username"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'username')}">checked="checked"</c:if> >
            <fmt:message key="label.username"/>

            <input type="checkbox" class="propCheck" name="properties" value="j:firstName"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:firstName')}">checked="checked"</c:if> >
            <fmt:message key="label.firstName"/>

            <input type="checkbox" class="propCheck" name="properties" value="j:lastName"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:lastName')}">checked="checked"</c:if> >
            <fmt:message key="label.lastName"/>

            <input type="checkbox" class="propCheck" name="properties" value="j:email"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:email')}">checked="checked"</c:if> >
            <fmt:message key="label.email"/>

            <input type="checkbox" class="propCheck" name="properties" value="j:organization"
                   <c:if test="${searchCriteria.searchIn ne 'properties'}">disabled</c:if>
                   <c:if test="${not empty searchCriteria.properties and functions:contains(searchCriteria.properties, 'j:organization')}">checked="checked"</c:if> >
            <fmt:message key="label.organization"/>

            <br/>
            <label for="storedOn"><span class="badge badge-info"><fmt:message key="label.on"/></span></label>
            <%--@elvariable id="providersList" type="java.util.List"--%>
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
                   onclick="$('.provCheck').removeAttr('disabled');"</c:if>>&nbsp;<fmt:message key="label.providers"/>:&nbsp;

            <c:forEach items="${providersList}" var="curProvider">
                <input type="checkbox" class="provCheck" name="providers" value="${curProvider.key}"
                       <c:if test="${fn:length(providersList) le 1 or searchCriteria.storedOn ne 'providers'}">disabled </c:if>
                <c:if test="${fn:length(providersList) le 1 or (not empty searchCriteria.providers and functions:contains(searchCriteria.providers, curProvider.key))}">
                       checked </c:if>>
                ${curProvider.key}
            </c:forEach>


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
        <form action="${flowExecutionUrl}" method="POST" class="needUsersSelection" style="display: inline;">
            <input type="hidden" name="selectedUsers"/>
            <button class="btn" type="submit" name="_eventId_editUser" >
                <i class="icon-edit"></i>
                &nbsp;<fmt:message key='serverSettings.user.edit'/>
            </button>
        </form>
        <form action="${flowExecutionUrl}" method="POST" class="needUsersSelection" style="display: inline;">
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
        <h2><fmt:message key="serverSettings.user.search.result"/></h2>
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th class="{sorter: false}" width="5%">&nbsp;</th>
                <th width="50%" class="sortable"><fmt:message key="label.name"/></th>
                <th width="45%" class="sortable"><fmt:message key="label.properties"/></th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <%--@elvariable id="users" type="java.util.List"--%>
                <c:when test="${fn:length(users) eq 0}">
                    <tr>
                        <td colspan="3"><fmt:message key="serverSettings.user.search.no.result"/></td>
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
