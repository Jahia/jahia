<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addResources type="javascript" resources="jquery.js, textsizer.js"/>

<!-- shortcuts -->
<script type="text/javascript">
    $(document).ready(function() {
        $('#shortcuts').children('ul').hide();
        $('#shortcuts').mouseover(function() {
            $(this).children('ul').show();
        }).mouseout(function() {
            $(this).children('ul').hide();
        });
    });
</script>
<div id="shortcuts">
    <h3><a title="Shortcuts" href="#"><fmt:message key="welcome"/></a></h3>
    <ul>
        <c:if test="${renderContext.loggedIn}">
            <li>
                <fmt:message key="welcome"/>, <span class="currentUser"><c:choose><c:when test="${not empty currentUser.properties['j:firstName']}">${currentUser.properties['j:firstName']} ${currentUser.properties['j:lastName']}</c:when><c:otherwise>${currentUser.username}</c:otherwise></c:choose><c:if test="${not empty currentAliasUser}">(&nbsp;<fmt:message key="as.user"/>&nbsp;${currentAliasUser.username})</c:if>:</span>
            </li>
            <li>
                <a class="loginFormTopLogoutShortcuts"
                   href='${url.logout}'><span><fmt:message
                        key="logout"/></span></a>
            </li>
            <c:if test="${!empty url.myProfile}">
                <li class="topshortcuts-mysettings">
                    <a href="<c:url value='${url.myProfile}'/>"><fmt:message key="mySpace.link"/></a>
                </li>
            </c:if>
<%--
            <c:if test="${jcr:hasPermission(renderContext.mainResource.node, 'editModeAccess')}">
                <li>
                    <a href="${url.edit}"><fmt:message key="edit"/></a>
                </li>
            </c:if>
--%>
<%--
            <c:if test="${jcr:hasPermission(renderContext.mainResource.node, 'contributeModeAccess')}">
                <li>
                    <a href="${url.contribute}"><fmt:message key="contribute"/></a>
                </li>
            </c:if>
--%>
        </c:if>
        <li><a href="#"
               onclick="javascript:window.print();return false">
            <fmt:message key="print"/></a>
        </li>
        <li>
            <a href="javascript:ts('body',1)"><fmt:message key="font.up"/></a>
        </li>
        <li>
            <a href="javascript:ts('body',-1)"><fmt:message key="font.down"/></a>
        </li>
<%--<li>
            <a href="<c:url value='${url.base}${renderContext.site.path}/home.html'/>"><fmt:message key="home"/></a>
        </li>
        <li>
            <a href="<c:url value='${url.base}${renderContext.site.path}/home.sitemap.html'/>"><fmt:message key="sitemap"/></a>
        </li>--%>
    </ul>
</div>
