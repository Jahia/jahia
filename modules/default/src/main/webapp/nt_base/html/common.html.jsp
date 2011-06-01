<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<c:set var="template" value="${functions:default(param.template, 'debug.full')}"/>
<c:set var="level" value="${functions:default(requestScope['org.jahia.modules.level'], 1)}"/>
<div class="render-item type-${fn:replace(currentNode.primaryNodeTypeName, ':', '-')} level-${level}">
	<div class="name">${fn:escapeXml(currentNode.name)}</div>
	<c:if test="${!param.skipType}">
		<div class="type">${currentNode.primaryNodeTypeName}</div>
	</c:if>
	<c:if test="${!param.skipProperties && functions:length(currentNode.properties) > 0}">
	<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType" scope="application"/>
    <utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType" scope="application"/>
	<div class="properties">
		<div class="jahia-properties">
			<c:forEach var="property" items="${currentNode.properties}">
				<c:if test="${!property.definition.metadataItem}">
					<%@ include file="property.jspf" %><br/>
				</c:if>
			</c:forEach>
		</div>
		<c:if test="${!param.skipMetadata}">
			<div class="metadata-properties">
				<c:forEach var="property" items="${currentNode.properties}">
					<c:if test="${property.definition.metadataItem}">
						<%@ include file="property.jspf" %><br/>
					</c:if>
				</c:forEach>
			</div>
		</c:if>
	</div>
	</c:if>
	<c:if test="${param.skipProperties && template == 'tree' && jcr:isNodeType(currentNode, 'nt:resource')}">
		<jcr:nodeProperty node="${currentNode}" name="jcr:data" var="dataProp"/>
		<div class="property type-binary">
			<span class="label">${fn:escapeXml(jcr:label(dataProp.definition,currentResource.locale))}:</span>
			<span class="value">
				<a href ="<c:url value='${currentNode.parent.url}'/>">&lt;binary&gt;</a>
			</span>
		</div>
	</c:if>
	<c:if test="${!param.skipNodes}">
		<div class="nodes">
			<c:if test="${!param.skipParentLink && level <= 1 && currentNode.depth > 1}">
				<div class="parent">
					<a href="<c:url value='${url.base}${currentNode.parent.path}.${template}.html'/>">..</a>
				</div>
			</c:if>
			<c:forEach var="child" items="${currentNode.nodes}">
				<div class="child type-${fn:replace(currentNode.primaryNodeTypeName, ':', '-')}">
					<c:if test="${!param.inlineNodes}" var="nodesAsLinks">
						<a href="<c:url value='${url.base}${child.path}.${template}.html'/>">${fn:escapeXml(child.name)}</a>
					</c:if>
					<c:if test="${!nodesAsLinks}">
						<template:module node="${child}" view="${template}"/>
					</c:if>
				</div>
			</c:forEach>
		</div>
	</c:if>
</div>