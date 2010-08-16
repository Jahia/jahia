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

<div class="tc-article"><!--start newsListItem -->
    <div class="image">
        <jcr:sql var="imageQuery"
                 sql="select * from [jnt:newsMLContentItem] as c  where isdescendantnode(c,['${currentNode.path}'])"/>
        <c:forEach items="${imageQuery.nodes}" var="contentItemNode">
            <jcr:nodeProperty node="${contentItemNode}" name="image" var="newsImage"/>
            <c:if test="${not empty newsImage}">
                <img src="${newsImage.node.url}" alt="" width="73" height="73"/>
            </c:if>
        </c:forEach>
    </div>
    <span class="newsDate">
        <fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate
            value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/>
        <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>
    </span>
    <h3><a href="${url.base}${currentNode.path}.detail.html"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></a></h3>
    <p>
    </p>
    <div class="tc-separator"></div>
</div>