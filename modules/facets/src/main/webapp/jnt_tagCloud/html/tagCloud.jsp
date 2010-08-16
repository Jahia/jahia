<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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

<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="tags.css"/>
<c:set var="usageThreshold"
       value="${not empty currentNode.properties['j:usageThreshold'] ? currentNode.properties['j:usageThreshold'].string : 1}"/>
<c:set var="numberOfTagsLimit"
       value="${not empty currentNode.properties['limit'] ? currentNode.properties['limit'].string : 50}"/>       
<jcr:node var="tagsRoot" path="${renderContext.site.path}/tags"/>
<div class="tags">
    <h3><c:if
            test="${not empty currentNode.properties['jcr:title'] && not empty currentNode.properties['jcr:title'].string}"
            var="titleProvided">${fn:escapeXml(currentNode.properties['jcr:title'].string)}</c:if><c:if
            test="${not titleProvided}"><fmt:message key="tags"/></c:if></h3>

    <query:definition var="listQuery" scope="request">
        <query:selector nodeTypeName="nt:base"/>
        <query:descendantNode path="${renderContext.site.path}"/>
        <query:column columnName="rep:facet(nodetype=jmix:tagged&key=j:tags&facet.mincount=${usageThreshold}&facet.limit=${numberOfTagsLimit}&facet.sort=true)" propertyName="j:tags"/>
    </query:definition>
    <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>
    
    <jsp:useBean id="tagCloud" class="java.util.HashMap"/>
    <c:forEach items="${result.facetFields}" var="tags">
        <c:forEach items="${tags.values}" var="tag">
            <c:set var="totalUsages" value="${totalUsages + tag.count}"/>
            <c:set target="${tagCloud}" property="${tag.name}" value="${tag.count}"/>            
        </c:forEach>
    </c:forEach>

    <c:if test="${not empty tagCloud}">
        <ul>
            <c:forEach items="${tagCloud}" var="tag">
                <jcr:node var="tagName" uuid="${tag.key}"/>            
                <c:url var="facetUrl" value="${url.base}${currentNode.properties.resultPage.node.path}.html" context="/">
                    <c:param name="src_terms[0].term" value="${tagName.name}"/>                    
                    <c:param name="src_terms[0].fields.tags" value="true"/>
                    <c:param name="src_sites.values" value="${renderContext.site.siteKey}"/>
                </c:url>
                <li><a href="${facetUrl}" class="tag${functions:round(10 * tag.value / totalUsages)}0"
                       title="${tagName.name} (${tag.value})">${tagName.name}</a></li>
            </c:forEach>
        </ul>
    </c:if>
</div>