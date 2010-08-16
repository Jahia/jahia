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
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<template:addResources type="css" resources="news.css"/>

<div class="newsMLItem"><!--start newsListItem -->
    <div class="article-meta">
        <fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
            value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/>
        <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>
        <span class="clear"/>
    </div>
    <h2><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h2>
    <div class="article-left"></div>

    <div class="article-text">
    <c:set var="currentList" value="${currentNode.nodes}" scope="request"/>
    <c:forEach items="${moduleMap.currentList}" var="subchild" varStatus="status">
        <div class="newsMLItem newsMLItem-box-style${(status.index mod 2)+1}">
            <template:module node="${subchild}" template="default"/>
        </div>
    </c:forEach>
    </div>
    
    <jcr:nodeProperty node="${currentNode}" name="associatedWith" var="associatedWiths"/>

    On the same subject...
    
    <c:forEach items="${associatedWiths}" var="prop">
        <jcr:sql var="relatedNodes"
                 sql="select * from [jnt:newsMLItem] as news where news.[publicIdentifier] like '${prop.string}%'" limit="5"/>
        <c:forEach items="${relatedNodes.nodes}" var="relatedNode">
            <template:module path="${relatedNode.path}" />
        </c:forEach>
    </c:forEach>

</div>