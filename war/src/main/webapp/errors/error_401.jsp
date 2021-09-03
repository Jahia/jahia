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

    <!-- Import Google font (Lato) -->
    <link href="${pageContext.request.contextPath}/css/loginFont.css" rel="stylesheet">

    <!-- Import jQuery (from Google) -->
    <script src="${pageContext.request.contextPath}/css/jquery.min.js"></script>
    <script src="${pageContext.request.contextPath}/javascript/error401.js"></script>

    <!-- Main style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/loginMain.css">
</head>
<body>
<div class="login-bg">
<div class="login-card">
    <h1><fmt:message key="label.login.welcome"/></h1>
    <div class="sep-top"></div>
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
                <input class="used" type="text" maxlength="250" name="username" value="${fn:escapeXml(param['username'])}" required><span class="bar"></span>
                <label><fmt:message key="label.username"/></label>
            </div>
            <div class="group">
                <input type="password" maxlength="250" name="password" autocomplete="off" autofocus="autofocus" required><span class="bar"></span>
                <label><fmt:message key="label.password"/></label>
            </div>
        </ui:isLoginError>
        <c:if test="${empty loginResult}">
            <div class="group">
                <input type="text" maxlength="250" name="username" autofocus="autofocus" required><span class="bar"></span>
                <label><fmt:message key="label.username"/></label>
            </div>
            <div class="group">
                <input type="password" maxlength="250" name="password" autocomplete="off" required><span class="bar"></span>
                <label><fmt:message key="label.password"/></label>
            </div>
        </c:if>
        <c:if test="${(not isFullReadOnly) and (not fn:contains(param.redirect, '/administration'))}">
        <label class="label--checkbox">
            <ui:loginRememberMe class="checkbox" />
            <fmt:message key="label.rememberme"/>
        </label>
        </c:if>
        <button type="submit" class="button buttonBlue"><fmt:message key='label.login'/>
            <div class="ripples buttonRipples">
                <span class="ripplesCircle"></span>
            </div>
        </button>
    </ui:loginArea>
</div>

</div>
</body>
</html>
