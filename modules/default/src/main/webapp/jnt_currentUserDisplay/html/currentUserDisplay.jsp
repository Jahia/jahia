<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="mainresource.css"/>
<c:set var="mainTemplate" value="${currentNode.properties['j:referenceTemplate'].string}"/>
<c:choose>
    <c:when test="${renderContext.editMode}">
        <div class="mainResourceArea">
            <c:if test="${not empty currentNode.properties['j:referenceTemplate'].string}">
                <div class="mainResourceTemplate">
                    <span>${currentNode.properties['j:referenceTemplate'].string}</span>
                </div>
            </c:if>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed dignissim tellus in metus viverra pharetra.
            Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Mauris eu risus
            elit. Donec nibh diam, commodo in adipiscing et, euismod sed orci. Donec eu metus eget mauris fringilla
            pretium. Mauris vehicula, arcu malesuada malesuada varius, est leo porttitor lacus, id fermentum lacus eros
            ac sem. Proin non nunc magna, nec euismod diam. Ut faucibus dignissim erat sit amet sagittis. Aenean
            vestibulum, odio a imperdiet semper, diam lacus egestas velit, non lobortis libero massa et risus. Nunc quis
            sagittis est. Duis non orci vel quam posuere rutrum. Fusce et fringilla lorem. Nam tempus, dolor pretium
            consequat bibendum, odio leo feugiat odio, vitae pulvinar velit ipsum sit amet augue. Fusce ultrices
            ultricies tortor. Nunc vel pulvinar ipsum. Cras et nibh turpis, ac ornare leo. Cras elementum magna et risus
            porta accumsan. Duis dui leo, tincidunt at blandit non, euismod eu odio.
        </div>
    </c:when>
    <c:otherwise>
        <template:module path="/users/${renderContext.user.username}" forcedTemplate="${mainTemplate}"/>
    </c:otherwise>
</c:choose>

