<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="search" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="docspace.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<div id="${currentNode.identifier}">
    <jcr:sql var="result"
             sql="select * from [jnt:folder] as file order by file.[jcr:lastModified] desc"/>
    <c:set var="currentList" value="${result.nodes}" scope="request"/>
    <c:set var="listTotalSize" value="${functions:length(result.nodes)}" scope="request"/>
    <c:choose>
        <c:when test="${empty param.pagesize}">
            <c:set var="pageSize" value="40"/>
        </c:when>
        <c:otherwise>
            <c:set var="pageSize" value="${param.pagesize}"/>
        </c:otherwise>
    </c:choose>
    <template:initPager totalSize="${moduleMap.listTotalSize}" pageSize="${pageSize}" id="${currentNode.identifier}"/>
    <ul class="docspacelist">
        <c:forEach items="${moduleMap.currentList}" var="subchild" varStatus="status" begin="${moduleMap.begin}" end="${moduleMap.end}">
            <c:if test="${jcr:hasPermission(subchild, 'write') and (not empty jcr:getParentOfType(subchild, 'jnt:page'))}">
                <li>
                    <a class="adocspace" href="${url.basePreview}${subchild.path}.html"
                       title="${subchild.name}">${functions:abbreviate(subchild.name,20,30,'...')}</a>

                    &nbsp;<span><fmt:message key="label.lastModif"/>:&nbsp;<fmt:formatDate
                        value="${subchild.properties['jcr:lastModified'].date.time}" dateStyle="short"
                        type="both"/></span>
                </li>
            </c:if>
        </c:forEach>
    </ul>
    <template:displayPagination nbItemsList="5,10,20,40,60,80,100,200"/>
    <template:removePager id="${currentNode.identifier}"/>
</div>
