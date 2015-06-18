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
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/errors.css" type="text/css"/>
    <title><fmt:message key="label.login"/></title>

</head>

<body>

<div class="bgImgLeft"></div>

<div class="logobg">
    <img src="${pageContext.request.contextPath}/css/logo.png" alt="Jahia Solutions">
</div>

<div id="textbox">
    <div id="ic">

        <h2>Digital Factory 7.1</h2>
        <p>To continue, please enter your credentials below.</p>

            <ui:loginArea id="girisyap">

            <div class="group">
                <input type="text" name="username" required>
                <span class="highlight"></span>
                <label><fmt:message key="label.username"/></label>
            </div>

            <div class="group">
                <input type="password" name="password" autofocus required>
                <span class="highlight"></span>
                <label><fmt:message key="label.password"/></label>
            </div>

            <input type="submit" value="<fmt:message key='label.login'/>" class="girisbtn" tabindex="100" />

            </ui:loginArea>

    </div>
</div>

<%-- <ui:isLoginError var="loginResult">
     <div class="alert alert-error"><fmt:message
             key="${loginResult == 'account_locked' ? 'message.accountLocked' : 'message.invalidUsernamePassword'}"/></div>
 </ui:isLoginError>

 <c:if test="${not fn:contains(param.redirect, '/administration')}">
     <div class="remember">
         <ui:loginRememberMe id="rememberme" tabindex="3"/>
         <label for="rememberme"><fmt:message key="label.rememberme"/></label>
     </div>
 </c:if>--%>

</body>
</html>
