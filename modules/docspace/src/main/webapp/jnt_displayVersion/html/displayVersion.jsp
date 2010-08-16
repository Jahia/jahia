<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>

<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<c:set var="bindedComponent" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
    <c:set value="${jcr:hasPermission(bindedComponent, 'write')}" var="hasWriteAccess"/>
    <h4 class="boxdocspace-title"><fmt:message key="docspace.label.document.version.tile"/></h4>
    <ul class="docspacelist docspacelistversion">
        <c:set var="checkPublishedVersion" value="true"/>
        <c:forEach items="${functions:reverse(bindedComponent.versionsAsVersion)}" var="version" varStatus="status">
            <li>
                <c:if test="${checkPublishedVersion}">
                    <c:set var="publishedVersion" value="false"/>
                    <c:forEach items="${functions:reverse(bindedComponent.versionInfos)}" var="versionInfo">
                        <c:if test="${not empty versionInfo.checkinDate}">
                            <c:if test="${version.created.time.time <= versionInfo.checkinDate.time.time}">
                                <c:set var="publishedVersion" value="true"/>
                            </c:if>
                        </c:if>
                    </c:forEach>
                </c:if>
                <img class="floatleft" alt="user default icon" src="${url.currentModule}/css/img/version.png"/>
                <c:choose>
                    <c:when test="${jcr:hasPermission(bindedComponent, 'write')}">
                        <a href="${bindedComponent.url}?v=${version.name}">Version ${version.name}</a>
                    </c:when>
                    <c:when test="${publishedVersion}">
                        <a href="${bindedComponent.url}">Version ${version.name}</a>
                    </c:when>
                    <c:otherwise>
                        Version ${version.name}
                    </c:otherwise>
                </c:choose>
                <p class="docspacedate"><fmt:formatDate
                        value="${version.created.time}" pattern="yyyy/MM/dd HH:mm"/>
                    <c:if test="${publishedVersion eq 'true'}">
                        &nbsp;<fmt:message key="docspace.label.published"/>
                        <c:set var="checkPublishedVersion" value="false"/>
                        <c:set var="publishedVersion" value="false"/>
                    </c:if>
                </p>

                <div class='clear'></div>
            </li>
        </c:forEach>
    </ul>
    <c:if test="${hasWriteAccess}">
        <form method="POST" name="publishFile" action="${url.base}${bindedComponent.path}.publishFile.do"
              id="publishFile">
            <p><fmt:message key="docspace.text.document.publish"/></p>
            <input class="button" type="submit" value="<fmt:message key="docspace.label.document.publish"/>"/>
        </form>
    </c:if>
</c:if>
<template:linker property="j:bindedComponent"/>