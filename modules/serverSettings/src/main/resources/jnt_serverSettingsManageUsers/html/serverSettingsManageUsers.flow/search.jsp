<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>
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
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>

<script type="text/javascript">
    $(document).ready(function () {
        $(".needUsersSelection").submit(function () {
            $("input[name='selectedUsers']").val($("select[name='usersSelected']").val());
            return true;
        })
    });
</script>
<h3><fmt:message key="label.manageUsers"/></h3>

<div>
    <p>

    <form action="${flowExecutionUrl}" method="POST">
        <input type="submit" name="_eventId_addUser"
               value="<fmt:message key="org.jahia.admin.users.ManageUsers.createNewUser.label"/>"/>
    </form>
    <form action="${flowExecutionUrl}" method="POST" class="needUsersSelection">
        <input type="hidden" name="selectedUsers"/>
        <input type="submit" name="_eventId_editUser"
               value="<fmt:message key="org.jahia.admin.users.ManageUsers.editViewProp.label"/>"/>
    </form>
    <form action="${flowExecutionUrl}" method="POST" class="needUsersSelection">
        <input type="hidden" name="selectedUsers"/>
        <input type="submit" name="_eventId_removeUser"
               value="<fmt:message key="org.jahia.admin.users.ManageUsers.removeSelectedUser.label"/>"/>
    </form>
    </p>
    <form action="${flowExecutionUrl}" id="searchForm" method="post">
        <table border="0" style="width:100%">
            <tr>
                <td valign="top">
                    <!-- Search user and group -->
                    <table border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td colspan="2">
                                <br><label for="searchString"><fmt:message key="label.search"/></label>
                                <input type="text" id="searchString" name="searchString" size="15"
                                       value='${searchCriteria.searchString}'
                                       onkeydown="if (event.keyCode == 13) javascript:submitForm('search');"/>
                            </td>
                        </tr>
                        <tr>
                            <td>&nbsp;&nbsp;<fmt:message key="label.in"/>&nbsp;:</td>
                            <td>
                                <input type="radio" name="searchIn" value="allProps"
                                       <c:if test="${empty searchCriteria.searchIn or searchCriteria.searchIn eq 'allProps'}">checked</c:if>
                                       onclick="$('.propCheck').attr('disabled',true);">&nbsp;<fmt:message
                                    key="label.allProperties"/>
                            </td>
                        </tr>
                        <tr>
                            <td>&nbsp;</td>
                            <td valign="top">
                                <input type="radio" name="searchIn" value="properties"
                                       <c:if test="${searchCriteria.searchIn eq 'properties'}">checked</c:if>
                                       onclick="$('.propCheck').removeAttr('disabled');">&nbsp;<fmt:message
                                    key="label.properties"/><br>
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
                            </td>
                        </tr>
                        <c:if test="${fn:length(providersList) gt 1}">
                            <tr>
                                <td>&nbsp;&nbsp;<fmt:message key="label.on"/>&nbsp;:&nbsp;</td>
                                <td>
                                    <input type="radio" name="storedOn" value="everywhere"
                                           <c:if test="${empty searchCriteria.storedOn or searchCriteria.storedOn eq 'everywhere'}">checked</c:if>
                                           onclick="$('.provCheck').attr('disabled',true);">&nbsp;<fmt:message
                                        key="label.everyWhere"/>
                                </td>
                            </tr>
                        </c:if>
                        <tr>
                            <td><c:choose><c:when test="${fn:length(providersList) gt 1}">&nbsp;</c:when><c:otherwise>&nbsp;&nbsp;<fmt:message
                                    key="label.on"/>&nbsp;:&nbsp;</c:otherwise></c:choose></td>
                            <td>
                                <nobr/>
                                <input type="radio" name="storedOn" value="providers"
                                <c:if test="${fn:length(providersList) le 1 or searchCriteria.storedOn eq 'providers'}"> checked </c:if>
                                <c:if test="${fn:length(providersList) gt 1}"> onclick="$('.provCheck').removeAttr('disabled');"</c:if>>&nbsp;<fmt:message key="label.providers"/></nobr>&nbsp;:<br>
                                <c:forEach items="${providersList}" var="curProvider">
                                    &nbsp;&nbsp;&nbsp;&nbsp;
                                    <input type="checkbox" class="provCheck" name="providers" value="${curProvider.key}"
                                           <c:if test="${fn:length(providersList) le 1 or searchCriteria.storedOn ne 'providers'}">disabled </c:if>
                                    <c:if test="${fn:length(providersList) le 1 or (not empty searchCriteria.providers and functions:contains(searchCriteria.providers, curProvider.key))}"> checked </c:if>>
                                    ${curProvider.key}<br>
                                </c:forEach>
                            </td>
                        </tr>
                        <tr>
                            <td>&nbsp;</td>
                            <td align="right">
                                <input type="submit" name="_eventId_search"
                                       value="<fmt:message key="label.search"/>"/>
                            </td>
                        </tr>
                    </table>
                    <!-- -->
                </td>
                <td>
                    <!-- Display user list -->
                    <table class="text" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td>
                                <center><i><fmt:message key="org.jahia.admin.users.ManageUsers.searchResult.label"/></i>
                                </center>
                                <br>
                                <table class="text" width="100%" border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td>

                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                           href="javascript:sortSelectBox(document.mainForm.selectedUsers, false, /\|(.*)/g);"
                                           title="<fmt:message key='org.jahia.admin.users.ManageUsers.sortByUserName.label'/>"><fmt:message
                                                key="org.jahia.admin.users.ManageUsers.sortByUserName.label"/></a>
                                    </span>
                                </span>

                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                           href="javascript:sortSelectBox(document.mainForm.selectedUsers, false, /(.*)\|/g);"
                                           title="<fmt:message key='label.sortByLastname'/>"><fmt:message
                                                key="label.sortByLastname"/></a>
                                    </span>
                                </span>

                                        </td>
                                    </tr>
                                </table>
                                <select ondblclick="javascript:handleKey(event);"
                                        <c:if test="${fn:length(users) eq 0}">disabled</c:if>
                                        onkeydown="javascript:handleKeyCode(event.keyCode);"
                                        style="width:435px;" name="usersSelected" size="25" class="fontfix"
                                        multiple="multiple">
                                    <c:choose>
                                        <c:when test="${fn:length(users) eq 0}">
                                            <option value="null" selected>
                                                -- - -&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp; - <fmt:message
                                                    key="org.jahia.admin.users.ManageUsers.noUserFound.label"/> -&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;-
                                                - --
                                            </option>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${users}" var="curUser">
                                                <option value="${fn:escapeXml(curUser.userKey)}">${user:formatUserTextOption(curUser,'Name,30;Properties,30')}</option>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </select><br>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </form>
</div>