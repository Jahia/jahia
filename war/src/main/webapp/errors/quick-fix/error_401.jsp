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

    <!-- Import font (Nunito Sans) -->
    <link href="${pageContext.request.contextPath}/css/loginFont.css" rel="stylesheet">

    <!-- jQuery -->
    <script src="${pageContext.request.contextPath}/css/jquery.min.js"></script>

    <!-- Main style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/loginMain_dark.css">
</head>
<body>

  <section class="login" style="background-image: url(../../css/images/Background_Login-01.png);">
  	<div class="login-main">
  		<div class="position-container">

  			<div class="logo">
  				<img src="../../css/images/dx_logo.png" alt="jahia logo">
  			</div>

  			<div class="login-form">
  				<ui:loginArea>

            <ui:isLoginError var="loginResult">
              <div class="login-error">
                <fmt:message key="${loginResult == 'account_locked' ? 'message.accountLocked' : 'message.invalidUsernamePassword'}"/>
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

            <c:if test="${not fn:contains(param.redirect, '/administration')}">
    					<label class="check-label no-select">
    						<ui:loginRememberMe name="checkbox" class="fs1" />
    						<lb><fmt:message key="label.rememberme"/></lb>
    					</label>
            </c:if>

  					<button type="submit"><fmt:message key='label.login'/></button>

  				</ui:loginArea>
  			</div>

  		</div>
  		<div class="login-footer">Digital Experience manager 7.2 - Copyrights © 2002-2017 All Rights Reserved by Jahia Solutions Group SA.</div>
  	</div>
  </section>

  <script>
    $(window, document, undefined).ready(function() {

    	// Input label
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
    	}, 40);

    });
  </script>

</body>
</html>
