<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="option" type="org.jahia.services.content.nodetypes.initializers.ChoiceListValue"--%>
<c:set target="${renderContext}" property="contentType" value="text/html;charset=UTF-8"/>
<template:addResources type="css" resources="slickmap.css"/>


<c:if test="${empty currentResource.moduleParams.level}">
    <c:set var="level" value="1"/>
</c:if>

<c:if test="${not empty currentResource.moduleParams.level}">
    <c:set var="level" value="${currentResource.moduleParams.level}"/>
</c:if>


<c:if test="${level eq 1}"><div class="sitemap"><ul><li></c:if>
<c:if test="${level > 1}">
    <li>
</c:if>
<a href='<c:url value="${currentNode.path}.html" context="${url.base}"/>'>${currentNode.properties["jcr:title"].string}
    level ${level}</a>
<c:forEach items="${jcr:getChildrenOfType(currentNode,'jmix:sitemap')}" var="child" varStatus="childStatus">
    <c:if test="${childStatus.first}">
        <ul <c:if test="${level eq 1}"><c:set var="nbSubItems" value="${jcr:getChildrenOfType(currentNode,'jmix:sitemap')}"/> id="primaryNav" class="col${fn:length(nbSubItems)}"</c:if>>
    </c:if>
    <template:module node="${child}" template="sitemap" editable="false">
        <template:param name="level" value="${level +1}"/>
    </template:module>
    <c:if test="${childStatus.last}">
        </ul>
    </c:if>
</c:forEach>
<c:if test="${level > 1}">
    </li>
</c:if>
<c:if test="${level eq 1}"></li></ul></div></c:if>
<c:set var="level" value="${level - 1}" scope="request"/>
