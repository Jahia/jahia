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
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
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
<jsp:useBean id="datas" class="java.util.LinkedHashMap"/>
<template:addResources type="css" resources="fullcalendar.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="fullcalendar.js"/>

<c:set var="linked" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:forEach items="${linked.nodes}" var="linkedChild" varStatus="status">
    <fmt:formatDate pattern="yyyy-MM-dd" value="${linkedChild.properties[currentNode.properties.startDateProperty.string].date.time}" var="startDate"/>
    <c:choose>
        <c:when test="${empty datas[startDate]}">
            <c:set target="${datas}" property="${startDate}" value="1"/>
        </c:when>
        <c:otherwise>
            <c:set target="${datas}" property="${startDate}" value="${datas[startDate]+1}"/>
        </c:otherwise>
    </c:choose>
</c:forEach>
<template:addResources type="inlinejavascript">
    $(document).ready(function() {
    // page is now ready, initialize the calendar...
    $('#calendar${currentNode.identifier}').fullCalendar({
    <c:if test="${not empty param.calStartDate}">
        year : ${fn:substring(param.calStartDate,0,4)},
        month : (${fn:substring(param.calStartDate,5,7)}-1),
        date : ${fn:substring(param.calStartDate,8,10)},
    </c:if>
    events: [
    <c:forEach items="${datas}" var="data" varStatus="status">
        <c:if test="${not status.first}">,</c:if>
        {
        title : '${data.value}',
        start : '${data.key}',
        url : '${url.base}${renderContext.mainResource.node.path}.html?filter={name:"${currentNode.properties.startDateProperty.string}",value:"${data.key}",op:"eq",uuid:"${linked.identifier}",format:"yyyy-MM-dd",type:"date"}&calStartDate=${data.key}'
        }
    </c:forEach>
    ]
    })
    });
</template:addResources>
<div class="calendar" id="calendar${currentNode.identifier}"></div>
<template:linker property="j:bindedComponent" />
