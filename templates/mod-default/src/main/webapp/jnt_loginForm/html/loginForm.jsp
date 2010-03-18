<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="loginForm.css"/>

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
<c:if test="${renderContext.editMode}">
    <legend> Login form : ${currentNode.properties['jcr:title'].string}</legend>
</c:if>
<ui:loginArea class="Form loginForm" action="${pageContext.request.contextPath}/cms/login">
    <h3 class="loginIcon">${currentNode.properties['jcr:title'].string}</h3>
    <ui:isLoginError>
        <span class="error"><fmt:message bundle="JahiaInternalResources"
                                         key="loginForm.error.invalidpasswordorusername.label"/></span>
    </ui:isLoginError>

    <p>
        <label class="left" for="username">Username: </label>
        <input type="text" value="" tabindex="1" maxlength="250" name="username" id="username"/>
    </p>

    <p>
        <label class="left" for="password">Password: </label>
        <input type="password" tabindex="2" maxlength="250" name="password" id="password"/>
    </p>

    <p>
        <input type="checkbox" id="rememberme"/>
        <label for="rememberme" class="rememberLabel"><fmt:message key="loginForm.rememberMe.label"/></label>

    </p>

    <div class="divButton">
        <input type="submit" onclick="document.forms.loginForm.submit(); return false;" name="search" class="button"
               value="<fmt:message key="loginForm.loginbutton.label"/>"/>
    </div>

</ui:loginArea>