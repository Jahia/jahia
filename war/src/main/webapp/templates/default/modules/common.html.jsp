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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<c:set var="template" value="${functions:default(param.template, 'full')}"/>
<c:set var="level" value="${functions:default(requestScope['org.jahia.modules.level'], 1)}"/>
<div class="render-item type-${fn:replace(currentNode.primaryNodeTypeName, ':', '-')} level-${level}">
	<div class="name">${fn:escapeXml(currentNode.name)}</div>
	<c:if test="${!param.skipType}">
		<div class="type">${currentNode.primaryNodeTypeName}</div>
	</c:if>
	<c:if test="${!param.skipProperties && currentNode.properties.size > 0}">
	<utility:useConstants var="jcrPropertyTypes" className="javax.jcr.PropertyType" scope="application"/>
	<div class="properties">
		<div class="jahia-properties">
			<c:forEach var="property" items="${currentNode.properties}">
				<c:if test="${!property.definition.metadataItem}">
					<%@ include file="property.jspf" %>
				</c:if>
			</c:forEach>
		</div>
		<c:if test="${!param.skipMetadata}">
			<div class="metadata-properties">
				<c:forEach var="property" items="${currentNode.properties}">
					<c:if test="${property.definition.metadataItem}">
						<%@ include file="property.jspf" %>
					</c:if>
				</c:forEach>
			</div>
		</c:if>
	</div>
	</c:if>
	<c:if test="${param.skipProperties && template == 'tree' && jcr:isNodeType(currentNode, 'nt:resource')}">
		<jcr:nodeProperty node="${currentNode}" name="jcr:data" var="dataProp"/>
		<div class="property type-binary">
			<span class="label">${fn:escapeXml(jcr:label(dataProp.definition))}:</span>
			<span class="value">
				<a href ="<c:url value='${currentNode.parent.url}'/>">&lt;binary&gt;</a>
			</span>
		</div>
	</c:if>
	<c:if test="${!param.skipParentLink && level <= 1 && currentNode.depth > 1}">
		<div class="parent">
			<a href="<c:url value='${baseUrl}${currentNode.parent.path}.${template}.html' context='/'/>">..</a>
		</div>
	</c:if>
	<c:if test="${!param.skipNodes && currentNode.nodes.size > 0}">
		<div class="children">
			<c:forEach var="child" items="${currentNode.nodes}">
				<div class="child type-${fn:replace(currentNode.primaryNodeTypeName, ':', '-')}">
					<c:if test="${!param.inlineNodes}" var="nodesAsLinks">
						<a href="<c:url value='${baseUrl}${child.path}.${template}.html' context='/'/>">${fn:escapeXml(child.name)}</a>
					</c:if>
					<c:if test="${!nodesAsLinks}">
						<template:module node="${child}" template="${template}"/>
					</c:if>
				</div>
			</c:forEach>
		</div>
	</c:if>
</div>