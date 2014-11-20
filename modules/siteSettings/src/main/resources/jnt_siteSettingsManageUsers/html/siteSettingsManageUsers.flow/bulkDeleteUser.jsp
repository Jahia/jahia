<%--@elvariable id="searchCriteria" type="org.jahia.modules.sitesettings.users.management.SearchCriteria"--%>
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
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js,jquery.metadata.js,jquery.tablesorter.js,jquery.tablecloth.js"/>
<template:addResources type="css" resources="admin-bootstrap.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css,tablecloth.css"/>
<div>
    <h2><fmt:message key="label.remove"/></h2>

    <div class="box-1">
        <form action="${flowExecutionUrl}" method="post">
            <table class="table table-bordered table-striped table-hover">
                <thead>
                <tr>
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
                    <%--@elvariable id="usersToDelete" type="java.util.List"--%>
                    <c:when test="${fn:length(usersToDelete) eq 0}">
                        <tr>
                            <td colspan="${multipleProvidersAvailable ? '4' : '3'}">
                                <fmt:message key="siteSettings.user.search.no.result"/></td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${usersToDelete}" var="curUser">
                            <tr class="sortable-row">
                                <td>
                                    <input type="checkbox" name="userToDelete" value="${fn:escapeXml(curUser.userKey)}" class="userCheckbox" readonly="readonly" checked="checked">
                                </td>
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
            <fieldset>
                <div class="container-fluid">
                    <div class="row-fluid">
                        <div class="span12">
                            <c:if test="${!userProperties.readOnly}">
                                <button class="btn btn-danger" type="submit" name="_eventId_confirm" onclick="workInProgress('${i18nWaiting}'); return true;">
                                    <i class="icon-remove icon-white"></i>
                                    &nbsp;<fmt:message key="label.remove"/>
                                </button>
                            </c:if>
                            <button class="btn" type="submit" name="_eventId_cancel">
                                <i class="icon-ban-circle"></i>
                                &nbsp;<fmt:message key="label.cancel"/>
                            </button>
                        </div>
                    </div>
                </div>
            </fieldset>
        </form>
    </div>
</div>