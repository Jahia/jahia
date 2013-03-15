<%--@elvariable id="userProperties" type="org.jahia.modules.serversettings.users.management.UserProperties"--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
    <p><c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
        <c:if test="${message.severity eq 'ERROR'}">
            <span style="color: red;">${message.text}</span><br/>
        </c:if>
    </c:forEach> </p>
    <form action="${flowExecutionUrl}" method="post">
        <fieldset title="<fmt:message key="label.user.profile"/>">
            <label for="username"><fmt:message key="label.username"/></label>
            <input name="username" id="username" value="${userProperties.username}"><br/>
            <label for="firstName"><fmt:message key="label.firstName"/></label>
            <input name="firstName" id="firstName" value="${userProperties.firstName}"><br/>
            <label for="lastName"><fmt:message key="label.lastName"/></label>
            <input name="lastName" id="lastName" value="${userProperties.lastName}"><br/>
            <label for="email"><fmt:message key="label.email"/></label>
            <input name="email" id="email" value="${userProperties.email}"><br/>
            <label for="organization"><fmt:message key="label.organization"/></label>
            <input name="organization" id="organization" value="${userProperties.organization}"><br/>
            <label for="password"><fmt:message key="label.password"/></label>
            <input type="password" name="password" id="password" value=""><br/>
            <label for="passwordConfirm"><fmt:message key="label.confirmPassword"/></label>
            <input type="password" name="passwordConfirm" id="passwordConfirm" value=""><br/>
        </fieldset>
        <fieldset title="<fmt:message key='label.user.options'/>">
            <label for="emailNotifications"><fmt:message key="label.emailNotifications"/>&nbsp;</label>
            <input type="checkbox" name="emailNotifications" id="emailNotifications" <c:if test="${userProperties.emailNotifications}">checked="checked"</c:if>><br/>
            <label for="accountLocked"><fmt:message key="label.accountLocked"/>&nbsp;</label>
            <input type="checkbox" name="accountLocked" id="accountLocked" <c:if test="${userProperties.accountLocked}">checked="checked"</c:if>><br/>
            <label for="preferredLanguage"><fmt:message key="label.preferredLanguage"/></label>
            <select id="preferredLanguage" name="preferredLanguage" size="1">
                <c:forEach items="${functions:availableAdminBundleLocale(renderContext.UILocale)}" var="uiLanguage">
                    <option value="${uiLanguage}" <c:if test="${uiLanguage eq userProperties.preferredLanguage}">selected="selected" </c:if>>${functions:displayLocaleNameWith(uiLanguage, renderContext.UILocale)}</option>
                </c:forEach>
            </select>
        </fieldset>
        <fieldset>
            <input type="submit" name="_eventId_add" value="<fmt:message key='label.add'/>"/>
            <input type="submit" name="_eventId_cancel" value="<fmt:message key='label.cancel'/>"/>
        </fieldset>
    </form>
</div>