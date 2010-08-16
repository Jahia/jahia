<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

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

<template:addResources type="css" resources="loginForm.css"/>

<c:if test="${renderContext.editMode}">
    <legend> Login form : ${currentNode.properties['jcr:title'].string}</legend>
</c:if>
<c:if test="${!renderContext.loggedIn}">

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
        <label class="left" for="username">Username: </label>
        <input type="text" value="" tabindex="1" maxlength="250" name="username" id="username"/>
    </p>

    <p>
        <label class="left" for="password">Password: </label>
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
                                       href='${url.logout}'><span>logout</span></a></p>
</c:if>
