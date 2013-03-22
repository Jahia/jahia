<%--@elvariable id="userProperties" type="org.jahia.modules.serversettings.users.management.UserProperties"--%>
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
<div>
    <span style="font-size: larger;"><fmt:message key="label.remove"/>&nbsp;${userProperties.displayName}</span>
    <p>
        <fmt:message key="label.user.definitivelyRemove"/><br/>
        <fmt:message key="label.user.definitivelyRemove.files"/>&nbsp;
        (<a href="<c:url value='/cms/export/default${userProperties.localPath}.zip?cleanup=simple'/>" target="_blank"><fmt:message key="label.export"/></a>)
    </p>
    <p>
        <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
            <c:if test="${message.severity eq 'ERROR'}">
                <span style="color: red;">${message.text}</span><br/>
            </c:if>
        </c:forEach>
    </p>

    <form action="${flowExecutionUrl}" method="post" id="editUser">
        <fieldset title="<fmt:message key='label.user.profile'/>">
            <label for="firstName"><fmt:message key="label.firstName"/></label>
            <input name="firstName" id="firstName" value="${userProperties.firstName}" disabled="disabled"><br/>
            <label for="lastName"><fmt:message key="label.lastName"/></label>
            <input name="lastName" id="lastName" value="${userProperties.lastName}" disabled="disabled"><br/>
            <label for="email"><fmt:message key="label.email"/></label>
            <input name="email" id="email" value="${userProperties.email}" disabled="disabled"><br/>
            <label for="organization"><fmt:message key="label.organization"/></label>
            <input name="organization" id="organization" value="${userProperties.organization}" disabled="disabled"><br/>
        </fieldset>
        <fieldset title="<fmt:message key='label.user.options'/>">
            <label for="emailNotificationsDisabled"><fmt:message key="label.emailNotifications"/>&nbsp;</label>
            <input type="checkbox" name="emailNotificationsDisabled" id="emailNotificationsDisabled"
                   <c:if test="${userProperties.emailNotificationsDisabled}">checked="checked"</c:if> disabled="disabled"><br/>
            <label for="accountLocked"><fmt:message key="label.accountLocked"/>&nbsp;</label>
            <input type="checkbox" name="accountLocked" id="accountLocked"
                   <c:if test="${userProperties.accountLocked}">checked="checked"</c:if> disabled="disabled"><br/>
            <label for="preferredLanguage"><fmt:message key="label.preferredLanguage"/></label>
            <select id="preferredLanguage" name="preferredLanguage" size="1" disabled="disabled">
                <c:forEach items="${functions:availableAdminBundleLocale(renderContext.UILocale)}" var="uiLanguage">
                    <option value="${uiLanguage}"
                            <c:if test="${uiLanguage eq userProperties.preferredLanguage}">selected="selected" </c:if>>${functions:displayLocaleNameWith(uiLanguage, renderContext.UILocale)}</option>
                </c:forEach>
            </select>
        </fieldset>

        <fieldset id="groupsFields" title="<fmt:message key="label.user.groups.list"/>">
            <label for="groupsFields"><fmt:message key="label.user.groups.list"/></label>
            <select class="fontfix" name="selectMember" size="6" multiple disabled="disabled">
                <c:forEach items="${userProperties.groups}" var="group">
                    <option value="${user:formatUserValueOption(group)}">${user:formatUserTextOption(group, 'Name, 20;SiteTitle, 15;Properties, 20')}</option>
                </c:forEach>
            </select>

        </fieldset>
        <fieldset>
            <input type="submit" name="_eventId_confirm" value="<fmt:message key='label.remove.confirm'/>"/>
            <input type="submit" name="_eventId_cancel" value="<fmt:message key='label.cancel'/>"/>
        </fieldset>
    </form>
</div>