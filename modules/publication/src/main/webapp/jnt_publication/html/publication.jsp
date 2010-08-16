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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="publication.css"/>
<jcr:nodeProperty var="file" node="${currentNode}" name="file"/>
<jcr:nodeProperty var="preview" node="${currentNode}" name="preview"/>

<div class="publicationListItem"><!--start publicationListItem -->
    <div class="publicationListSpace"><!--start publicationListSpace -->
        <div class="publicationPhoto">
            <a href="${file.node.url}">
                <c:if test="${not empty preview.node.url}">
                    <img src="${preview.node.url}" alt="${preview.node.propertiesAsString['jcr:title']}">
                </c:if>
                <c:if test="${empty preview.node.url}">
                    <img src="${url.currentModule}/images/no_preview.png" alt="no preview"/>
                </c:if>
            </a>
        </div>
        <div class="publicationBody"><!--start publicationBody -->
            <h5><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h5>

            <p class="publicationAuthor"><c:if test="${!empty currentNode.properties.author.string}"><fmt:message
                    key="publication.author"/>: ${currentNode.properties.author.string}</c:if></p>

            <p class="publicationSource"><c:if test="${!empty currentNode.properties.source.string}"><fmt:message
                    key="publication.source"/>: ${currentNode.properties.source.string}</c:if></p>

            <p class="publicationDate"><c:if
                    test="${!empty currentNode.properties.date.string && currentNode.properties.date.string !=''}">${currentNode.properties.date.string}</c:if></p>

            <div class="publicationDescription">${currentNode.properties.body.string}</div>
            <div class="publicationAction">
                <c:if test="${file.node.fileContent.contentLength > 0}">
                    <fmt:formatNumber var="num" pattern="### ### ###.##" type="number"
                                      value="${(file.node.fileContent.contentLength/1024)}"/>
                    <a class="publicationDownload" href="${file.node.url}"><fmt:message key="publication.download"/></a>
                    <span class="publicationDocSize">(${num} KB) </span>
                </c:if>
            </div>
            <div class="clear"></div>
        </div>
        <!--stop publicationBody -->
        <div class="clear"></div>
    </div>
    <!--stop publicationListSpace -->
    <div class="clear"></div>
</div>
<!--stop publicationListItem -->
