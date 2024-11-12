<%@page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<%@ page import="org.jahia.settings.SettingsBean" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html>
<% pageContext.setAttribute("isFullReadOnly", Boolean.valueOf(SettingsBean.getInstance().isFullReadOnlyMode())); %>
<% pageContext.setAttribute("buildNumber", SettingsBean.getInstance().getBuildNumber()); %>
<head>
    <!-- Meta info -->
    <title><fmt:message key="label.login"/></title>
    <meta name="description" content="401"/>
    <meta name="keywords" content="">
    <meta charset="UTF-8">
    <meta name="robots" content="noindex, nofollow"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.ico">

    <!-- Import font (Nunito Sans) -->
    <link href="${pageContext.request.contextPath}/css/loginFont.css?${buildNumber}" rel="stylesheet">

    <!-- Main style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/loginMain_dark.css?${buildNumber}">
</head>
<body>

<section class="login" style="background-image: url(${pageContext.request.contextPath}/css/images/login-background.jpg?${buildNumber});">
    <div class="login-main">
        <div class="position-container">
            <div class="logo-form-container">
                <div class="logo">
                    <img src="${pageContext.request.contextPath}/css/images/dx_logo.png" alt="jahia logo">
                </div>
                <div class="login-form">
                <ui:loginArea>

                    <ui:isLoginError var="loginResult">
                        <div class="login-error">
                            <c:choose>
                                <c:when test="${loginResult == 'account_locked'}">
                                    <fmt:message key="message.accountLocked"/>
                                </c:when>
                                <c:when test="${loginResult == 'logged_in_users_limit_reached'}">
                                    <fmt:message key="message.loggedInUsersLimitReached"/>
                                </c:when>
                                <c:otherwise>
                                    <fmt:message key="message.invalidUsernamePassword"/>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="group">
                            <label class="inputlabel" for="username"><fmt:message key="label.username"/></label>
                            <input type="text" name="username" id="username" maxlength="250" value="${fn:escapeXml(param['username'])}" required />
                            <span class="highlight"></span>
                            <span class="bar"></span>
                        </div>
                        <div class="group">
                            <label class="inputlabel" for="password"><fmt:message key="label.password"/></label>
                            <input type="password" name="password" id="password" maxlength="250" autocomplete="off" required />
                            <span class="highlight"></span>
                            <span class="bar"></span>
                        </div>
                    </ui:isLoginError>

                    <c:if test="${empty loginResult}">
                        <div class="group">
                            <label class="inputlabel"><fmt:message key="label.username"/></label>
                            <input type="text" name="username" maxlength="250" required />
                            <span class="highlight"></span>
                            <span class="bar"></span>
                        </div>
                        <div class="group">
                            <label class="inputlabel"><fmt:message key="label.password"/></label>
                            <input type="password" name="password" maxlength="250" autocomplete="off" required />
                            <span class="highlight"></span>
                            <span class="bar"></span>
                        </div>
                    </c:if>

                    <c:if test="${(not isFullReadOnly) and (not fn:contains(param.redirect, '/administration'))}">
                        <div class="check-button-container">
                            <ui:loginRememberMe name="checkbox" class="fs1" />
                            <label class="check-label no-select">
                                <fmt:message key="label.rememberme"/>
                            </label>
                        </div>
                    </c:if>

                    <button type="submit"><fmt:message key='label.login'/></button>

                </ui:loginArea>
                </div>
            </div>
            <div class="login-footer">Jahia - Copyrights © 2002-2025 All Rights Reserved by Jahia Solutions Group SA.</div>
        </div>
    </div>
</section>
<script>
    var redirectInput = document.querySelector('input[name="redirect"]');
    if (redirectInput) {
        redirectInput.value = redirectInput.value + window.location.hash;
    }
</script>
</body>
</html>
