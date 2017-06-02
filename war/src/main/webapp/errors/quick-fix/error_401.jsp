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
    <!-- Meta info -->
    <title><fmt:message key="label.login"/></title>
    <meta name="description" content=""/>
    <meta name="keywords" content="">
    <meta charset="UTF-8">
    <meta name="robots" content="noindex, nofollow"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.ico">

    <!-- Import Google font (Lato) -->
    <link href="${pageContext.request.contextPath}/css/loginFont.css" rel="stylesheet">

    <!-- Import jQuery (from Google) -->
    <script src="${pageContext.request.contextPath}/css/jquery.min.js"></script>

    <!-- Main style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/loginMain.css">
</head>
<body>
<div class="login-bg">
<div class="login-card">
    <h1>QUICK-FIX <fmt:message key="label.login.welcome"/></h1>
    <div class="sep-top"></div>
    <ui:loginArea>
        <ui:isLoginError var="loginResult">
            <div class="login-error"><fmt:message
                    key="${loginResult == 'account_locked' ? 'message.accountLocked' : 'message.invalidUsernamePassword'}"/></div>
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
        <c:if test="${not fn:contains(param.redirect, '/administration')}">
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

<script>

    $(window, document, undefined).ready(function() {

        // Fix for input label
        setTimeout(function() {
            var loginInput = $("input[name='username'],input[name='password']")
            loginInput.blur(function() {
                var $this = $(this);
                if ($this.val())
                    $this.addClass('used');
                else
                    $this.removeClass('used');
            });

            // Fix for autofill
            if ($("input[name='username']").val()) {
                loginInput.addClass('used');
            }
        }, 20);


        // Button animation effects

        var $ripples = $('.ripples');

        $ripples.on('click.Ripples', function(e) {

            var $this = $(this);
            var $offset = $this.parent().offset();
            var $circle = $this.find('.ripplesCircle');

            var x = e.pageX - $offset.left;
            var y = e.pageY - $offset.top;

            $circle.css({
                top: y + 'px',
                left: x + 'px'
            });

            $this.addClass('is-active');

        });

        $ripples.on('animationend webkitAnimationEnd mozAnimationEnd oanimationend MSAnimationEnd', function(e) {
            $(this).removeClass('is-active');
        });

    });

</script>
</div>
</body>
</html>
