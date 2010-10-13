<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="currentAliasUser" type="org.jahia.services.usermanager.JahiaUser"--%>

<template:addResources type="css" resources="loginForm.css"/>

<c:if test="${!renderContext.loggedIn || currentAliasUser.username eq 'guest'}">

    <script type="text/javascript">
        document.onkeydown = keyDown;

        function keyDown(e) {
            if (!e) e = window.event;
            var ieKey = e.keyCode;
            if (ieKey == 13) {
                document.loginForm.submit();
            }
        }
    </script>
    <ui:loginArea class="Form loginForm" action="${pageContext.request.contextPath}/cms/login">
        <h3 class="loginIcon">${currentNode.properties['jcr:title'].string}</h3>
        <ui:isLoginError var="loginResult">
            <span class="error"><fmt:message bundle="JahiaInternalResources" key="${loginResult == 'account_locked' ? 'message.accountLocked' : 'message.invalidUsernamePassword'}"/></span>
        </ui:isLoginError>

        <p>
            <label class="left" for="username"><fmt:message key="label.username"/></label>
            <input type="text" value="" tabindex="1" maxlength="250" name="username" id="username"/>
        </p>

        <p>
            <label class="left" for="password"><fmt:message key="label.password"/></label>
            <input type="password" tabindex="2" maxlength="250" name="password" id="password"/>
        </p>

        <p>
            <input type="checkbox" id="rememberme" name="useCookie"/>
            <label for="rememberme" class="rememberLabel"><fmt:message key="loginForm.rememberMe.label"/></label>

        </p>

        <div class="divButton">
            <input type="submit" onclick="document.forms.loginForm.submit(); return false;" name="search" class="button"
                   value="<fmt:message key="loginForm.loginbutton.label"/>"/>
        </div>
    </ui:loginArea>
</c:if>
<c:if test="${renderContext.loggedIn}">
    <p>Logged as ${renderContext.user.username}</p>
    <p><a class="loginFormTopLogoutShortcuts"
          href='${url.logout}'><span><fmt:message key="label.logout"/></span></a></p>
</c:if>
