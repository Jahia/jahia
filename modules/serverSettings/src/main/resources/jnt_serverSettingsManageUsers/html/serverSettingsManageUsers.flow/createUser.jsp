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
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<div>
    <p>
        <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
            <c:if test="${message.severity eq 'ERROR'}">
                <div class="alert alert-error">
                    <button type="button" class="close" data-dismiss="alert">&times;</button>
                        ${message.text}
                </div>
            </c:if>
        </c:forEach>
    </p>
    <h2><fmt:message key="serverSettings.user.create"/></h2>
    <div class="box-1">
        <form action="${flowExecutionUrl}" method="post" autocomplete="off">
            <fieldset title="<fmt:message key="serverSettings.user.profile"/>">
                <div class="container-fluid">
                    <div class="row-fluid">
                        <p><fmt:message key="label.noteThat"/>:&nbsp;<fmt:message key="serverSettings.user.errors.username.syntax"/></p>
                    </div>
                    <div class="row-fluid">
                        <div class="span4">
                            <label for="username"><fmt:message key="label.username"/> <span class="text-error"><strong>*</strong></span></label>
                            <input type="text" name="username" class="span12" id="username" value="${userProperties.username}">
                        </div>
                        <div class="span4">
                            <label for="firstName"><fmt:message key="label.firstName"/></label>
                            <input type="text" name="firstName" class="span12" id="firstName" value="${userProperties.firstName}">
                        </div>
                    </div>
                    <div class="row-fluid">
                        <div class="span4">
                            <label for="lastName"><fmt:message key="label.lastName"/></label>
                            <input type="text" name="lastName" class="span12" id="lastName" value="${userProperties.lastName}">
                        </div>
                        <div class="span4">
                            <label for="email"><fmt:message key="label.email"/></label>
                            <input type="text" name="email" class="span12" id="email" value="${userProperties.email}">
                        </div>
                    </div>
                    <div class="row-fluid">
                        <div class="span4">
                            <label for="organization"><fmt:message key="label.organization"/></label>
                            <input type="text" name="organization" class="span12" id="organization" value="${userProperties.organization}">
                        </div>
                        <div class="span4">
                        </div>
                    </div>
                    <div class="row-fluid">
                        <div class="span4">
                            <label for="password"><fmt:message key="label.password"/> <span class="text-error"><strong>*</strong></span></label>
                            <input type="password" name="password" class="span12" id="password" value="">
                        </div>
                        <div class="span4">
                            <label for="passwordConfirm"><fmt:message key="label.confirmPassword"/> <span class="text-error"><strong>*</strong></span></label>
                            <input type="password" name="passwordConfirm" class="span12" id="passwordConfirm" value="">
                        </div>
                    </div>

                </div>

            </fieldset>
            <fieldset title="<fmt:message key='label.options'/>">
                <div class="container-fluid">
                    <div class="row-fluid">
                        <div class="span4">
                            <label for="emailNotificationsDisabled">
                                <input type="checkbox" name="emailNotificationsDisabled" id="emailNotificationsDisabled" <c:if test="${userProperties.emailNotificationsDisabled}">checked="checked"</c:if>>
                                <fmt:message key="serverSettings.user.emailNotifications"/>
                            </label>
                            <label for="accountLocked">
                                <input type="checkbox" name="accountLocked" id="accountLocked" <c:if test="${userProperties.accountLocked}">checked="checked"</c:if>>
                                <fmt:message key="label.accountLocked"/>
                            </label>
                        </div>
                        <div class="span4">
                            <label for="preferredLanguage"><fmt:message key="serverSettings.user.preferredLanguage"/></label>
                            <select class="span12" id="preferredLanguage" name="preferredLanguage">
                                <c:forEach items="${functions:availableAdminBundleLocale(renderContext.UILocale)}" var="uiLanguage">
                                    <option value="${uiLanguage}" <c:if test="${uiLanguage eq userProperties.preferredLanguage}">selected="selected" </c:if>>${functions:displayLocaleNameWith(uiLanguage, renderContext.UILocale)}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                </div>
            </fieldset>
            <fieldset>
                <div class="container-fluid">
                    <div class="row-fluid">
                        <div class="span12">
                            <button class="btn btn-primary" type="submit" name="_eventId_add" onclick="workInProgress(); return true;">
                                <i class="icon-plus icon-white"></i>
                                &nbsp;<fmt:message key='label.add'/>
                            </button>
                            <button class="btn" type="submit" name="_eventId_cancel">
                                <i class="icon-ban-circle"></i>
                                &nbsp;<fmt:message key='label.cancel'/>
                            </button>
                        </div>
                    </div>
                </div>

            </fieldset>
        </form>
    </div>
</div>