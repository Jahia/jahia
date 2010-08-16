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

<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<template:addResources type="css" resources="shortcuts-simple.css"/>
<template:addResources type="javascript" resources="textsizer.js"/>

<div class="shortcuts-simple">
    <ul>
        <c:if test="${renderContext.loggedIn}">
            <li class="shortcuts-login">
                <a href='${url.logout}'><span><fmt:message key="logout"/></span></a>
            </li>
            <li>
                <span class="currentUser"><utility:userProperty/></span>
            </li>
            <li class="shortcuts-mysettings">
                <a href="${url.base}${renderContext.site.path}/my-profile.html"><fmt:message key="userProfile.link"/></a>
            </li>
            <li class="shortcuts-edit">
                <a href="${url.edit}"><fmt:message key="edit"/></a>
            </li>
            <li class="shortcuts-contribute">
                <a href="${url.contribute}"><fmt:message key="contribute"/></a>
            </li>
        </c:if>
        <li class="shortcuts-print"><a href="base.wrapper.bodywrapper.jsp#"
                                          onclick="javascript:window.print()">
            <fmt:message key="print"/></a>
        </li>
        <li class="shortcuts-typoincrease">
            <a href="javascript:ts('body',1)"><fmt:message key="font.up"/></a>
        </li>
        <li class="shortcuts-typoreduce">
            <a href="javascript:ts('body',-1)"><fmt:message key="font.down"/></a>
        </li>
        <li class="shortcuts-home">
            <a href="${url.base}${rootPage.path}.html"><fmt:message key="home"/></a>
        </li>
        <li class="shortcuts-sitemap">
            <a href="${url.base}${rootPage.path}.sitemap.html"><fmt:message
                    key="sitemap"/></a>
        </li>
    </ul>
</div>
