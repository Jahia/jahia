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
<template:addResources type="css" resources="event.css"/>


        <div class="eventsListItem"><!--start eventsListItem -->
            <div class="eventsInfoDate">
                <div class="eventsDate">
                    <span class="day"><fmt:formatDate pattern="dd" value="${currentNode.properties.startDate.time}"/></span>
                    <span class="month"><fmt:formatDate pattern="MM" value="${currentNode.properties.startDate.time}"/></span>
                    <span class="year"><fmt:formatDate pattern="yyyy" value="${currentNode.properties.startDate.time}"/></span>
                </div>
                <c:if test="${not empty currentNode.properties.endDate}">
                    <div class="eventsTxtDate">
                        <span><fmt:message key='to'/></span>
                    </div>
                    <div class="eventsDate">
                        <span class="day"><fmt:formatDate pattern="dd" value="${currentNode.properties.endDate.time}"/></span>
                        <span class="month"><fmt:formatDate pattern="MM" value="${currentNode.properties.endDate.time}"/></span>
                        <span class="year"><fmt:formatDate pattern="yyyy" value="${currentNode.properties.endDate.time}"/></span>
                    </div>
                </c:if>
            </div>
            <div class="eventsBody"><!--start eventsBody -->
                <p class="eventsLocation"><span>${currentNode.properties.location.string}</span></p>
                <p class="eventsType"><span>${currentNode.properties.eventsType.string}</span></p>
                <h4><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h4>

                <div class="eventsResume">
                    ${currentNode.properties.body.string}</div>
                    <div class="eventsMeta">
        	    <span class="categoryLabel"><fmt:message key='label.categories'/>:</span>
                        <jcr:nodeProperty node="${currentNode}" name="j:defaultCategory" var="cat"/>
                        <c:if test="${cat != null}">
                                    <c:forEach items="${cat}" var="category">
                                        <span class="categorytitle">${category.node.properties['j:nodename'].string}</span>
                                    </c:forEach>

                        </c:if>
                    </div>
            </div>
            <!--start eventsBody -->
            <div class="clear"> </div>
        </div>