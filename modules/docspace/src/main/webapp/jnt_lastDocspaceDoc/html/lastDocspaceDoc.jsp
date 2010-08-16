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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css"/>
<c:set var="startNode" value="${currentNode.properties.startNode.node}"/>
<c:if test="${empty startNode}">
    <c:set var="startNode" value="${jcr:getMeAndParentsOfType(renderContext.mainResource.node, 'jnt:page')[0]}"/>
</c:if>
<h4 class="boxdocspace-title"><fmt:message key="docspace.label.docspace.last.document"/></h4>
<ul class="docspacelist">
    <jcr:sql var="result"
             sql="select * from [jnt:file] as file where isdescendantnode(file, ['${startNode.path}']) order by file.[jcr:lastModified] desc"/>
    <c:forEach items="${result.nodes}" var="document" end="9">
        <li>
            <c:if test="${jcr:hasPermission(document, 'write')}">
                <span class="icon ${functions:fileIcon(document.name)}"></span><a
                    href="${url.basePreview}${document.path}.html"
                    title="${document.name}">${functions:abbreviate(document.name,20,30,'...')}</a>
            </c:if>
            <c:if test="${not jcr:hasPermission(document, 'write')}">
                <span class="icon ${functions:fileIcon(document.name)}"></span><a
                    href="${url.baseLive}${document.path}.docspace.html"
                    title="${document.name}">${functions:abbreviate(document.name,20,30,'...')}</a>
            </c:if>
            <span class="docspacelistinfo"><fmt:message
                            key="docspace.label.document.lastModification"/>&nbsp;<fmt:formatDate
                            value="${document.properties['jcr:lastModified'].time}" dateStyle="medium"/></span>
            <p class="docspacelistinfo2">${functions:abbreviate(functions:removeHtmlTags(document.properties['jcr:description'].string),100,150,'...')}</p>
        </li>
    </c:forEach>
</ul>