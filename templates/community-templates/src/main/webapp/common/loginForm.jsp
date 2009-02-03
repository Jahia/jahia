<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="declarations.jspf" %>
<!-- login -->
<ui:loginArea>
    <p>
        <ui:loginUsername labelCssClassName="hide" cssClassName="text" labelKey="username" tabIndex="1"/>
        <ui:loginPassword labelCssClassName="hide" cssClassName="text" labelKey="password" tabIndex="2"/>
        <input type="image" class="gobutton"
               src="${pageContext.request.contextPath}/jsp/jahia/templates/community_templates/theme/${requestScope.currentTheme}/img/go-button.png" tabindex="3"/>
        <!-- input type bouton image non géré pour le moment -->
        <ui:loginErrorMessage invalidUsernamePasswordKey="invalidUsernamePasswordKey" cssClassName="error"/>
    </p>
</ui:loginArea>

<c:if test="${requestScope.currentRequest.logged}">
    <p id="loginFormTopTools">
        <span class="currentUser" ><c:out value="${requestScope.currentRequest.currentUserName}"/></span>
        <a class="loginFormTopMySettingsShortcuts" href="<template:composePageURL page="mySettings"/>" ><utility:resourceBundle resourceName="mySettings.title" defaultValue="My settings"/></a>
        <a class="loginFormTopLogoutShortcuts"href="<template:composePageURL page="logout"/>"><utility:resourceBundle resourceName="logout" defaultValue="Logout"/></a>
    </p>
</c:if>
