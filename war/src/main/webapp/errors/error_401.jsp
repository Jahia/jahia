<%@page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html>
<head>
    <meta charset="utf-8">
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/errors.css" type="text/css"/>
    <title><fmt:message key="label.login"/></title>
    <script type="text/javascript">
        var clickcounter = 0;
        document.onkeydown = function (e) {
            if ((e || window.event).keyCode == 13) clickcounter++;
            doSubmit();
        };
    </script>
</head>
<body onload="document.loginForm.username.focus()" class="login">
<div class="row-fluid login-wrapper">
    <img class="logo" alt="jahia" src="${pageContext.request.contextPath}/css/images/jahia-logo-white.png">

    <div class="span4 box">
        <div class="content-wrap">
            <ui:loginArea>
                <script type="text/javascript">
                    function doSubmit() {
                        if (clickcounter == 1) {
                            document.forms.loginForm.submit();
                        }
                    }
                </script>
                <h1><fmt:message key="label.login"/></h1>
                <ui:isLoginError var="loginResult">
                    <div class="alert alert-error"><fmt:message
                            key="${loginResult == 'account_locked' ? 'message.accountLocked' : 'message.invalidUsernamePassword'}"/></div>
                </ui:isLoginError>
                <input class="span12" type="text" placeholder="<fmt:message key="label.username"/>" tabindex="1"
                       maxlength="250" name="username"/>
                <input class="span12" type="password" placeholder="<fmt:message key="label.password"/>" tabindex="2"
                       maxlength="250" name="password"/>
                <c:if test="${not fn:contains(param.redirect, '/administration')}">
                    <div class="remember">
                        <ui:loginRememberMe id="rememberme" tabindex="3"/>
                        <label for="rememberme"><fmt:message key="label.rememberme"/></label>
                    </div>
                </c:if>
                <a class="btn btn-block btn-primary" href="#login"
                   onClick="document.forms.loginForm.submit(); return false;" tabindex="5"
                   title="<fmt:message key='label.login'/>">
                    <i class="icon-ok icon-white"></i>
                    &nbsp;<fmt:message key="label.login"/>
                </a>
            </ui:loginArea>
        </div>
    </div>
</div>
</body>
</html>
