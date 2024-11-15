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
    <link href="${pageContext.request.contextPath}/css/loginFont.css" rel="stylesheet">

    <!-- Main style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/loginMain_dark.css">
</head>
<body>

<section class="login" style="background-image: url(${pageContext.request.contextPath}/css/images/Background_Login-01.png);">
    <div class="login-main">
        <div class="position-container">

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
                            <input type="text" name="username" maxlength="250" value="${fn:escapeXml(param['username'])}" required />
                            <span class="highlight"></span>
                            <span class="bar"></span>
                            <label class="inputlabel"><fmt:message key="label.username"/></label>
                        </div>
                        <div class="group">
                            <input type="password" name="password" maxlength="250" autocomplete="off" required />
                            <span class="highlight"></span>
                            <span class="bar"></span>
                            <label class="inputlabel"><fmt:message key="label.password"/></label>
                        </div>
                    </ui:isLoginError>

                    <c:if test="${empty loginResult}">
                        <div class="group">
                            <input type="text" name="username" maxlength="250" required />
                            <span class="highlight"></span>
                            <span class="bar"></span>
                            <label class="inputlabel"><fmt:message key="label.username"/></label>
                        </div>
                        <div class="group">
                            <input type="password" name="password" maxlength="250" autocomplete="off" required />
                            <span class="highlight"></span>
                            <span class="bar"></span>
                            <label class="inputlabel"><fmt:message key="label.password"/></label>
                        </div>
                    </c:if>

                    <c:if test="${(not isFullReadOnly) and (not fn:contains(param.redirect, '/administration'))}">
                        <label class="check-label no-select">
                            <ui:loginRememberMe name="checkbox" class="fs1" />
                            <lb><fmt:message key="label.rememberme"/></lb>
                        </label>
                    </c:if>

                    <button type="submit"><fmt:message key='label.login'/></button>

                </ui:loginArea>
            </div>

        </div>
        <div class="login-footer">Jahia - Copyrights © 2002-2024 All Rights Reserved by Jahia Solutions Group SA.</div>
    </div>
</section>

</body>
</html>
