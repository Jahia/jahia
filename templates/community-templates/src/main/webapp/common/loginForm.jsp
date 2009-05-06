<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="declarations.jspf" %>
<!-- login -->
<ui:loginArea>
    <p>
        <label class="hide" for="username"><fmt:message key="username"/></label><ui:loginUsername class="text" id="username" size="8"/>
        <label class="hide" for="password"><fmt:message key="password"/></label><ui:loginPassword class="text" id="password" size="8"/>
        <input type="image" class="gobutton"
               src="${pageContext.request.contextPath}/templates/community_templates/theme/${requestScope.currentTheme}/img/go-button.png" tabindex="3"/>
        <!-- input type bouton image non géré pour le moment -->
        <ui:isLoginError>
          <span class="error"><fmt:message key="invalidUsernamePasswordKey"/></span>
        </ui:isLoginError>        
        
    </p>
</ui:loginArea>

<c:if test="${requestScope.currentRequest.logged}">
    <p id="loginFormTopTools">
        <span class="currentUser" ><c:out value="${requestScope.currentRequest.currentUserName}"/></span>
        <a class="loginFormTopMySettingsShortcuts" href="<template:composePageURL page="mySettings"/>" ><fmt:message key="mySettings.title"/></a>
        <a class="loginFormTopLogoutShortcuts"href="<template:composePageURL page="logout"/>"><fmt:message key="logout"/></a>
    </p>
</c:if>
