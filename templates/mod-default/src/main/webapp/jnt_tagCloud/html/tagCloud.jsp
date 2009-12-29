<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="tags.css" nodetype="jmix:tagged"/>
<c:set var="usageThreshold" value="${not empty currentNode.properties['j:usageThreshold'] ? currentNode.properties['j:usageThreshold'].string : 1}"/>
<jcr:node var="tagsRoot" path="${renderContext.siteNode.path}/tags"/>
<div class="tags">
<h3><c:if test="${not empty currentNode.properties['jcr:title'] && not empty currentNode.properties['jcr:title'].string}" var="titleProvided">${fn:escapeXml(currentNode.properties['jcr:title'].string)}</c:if><c:if test="${not titleProvided}"><fmt:message key="tags"/></c:if></h3>
<jcr:sql var="tags" sql="select * from [jnt:tag] as sel where ischildnode(sel,['${tagsRoot.path}']) order by sel.[j:nodename]"/>
<c:set var="totalUsages" value="0"/>
<jsp:useBean id="filteredTags" class="java.util.LinkedHashMap"/>
<c:forEach items="${tags.nodes}" var="tag">
	<c:set var="count" value="${tag.weakReferences.size}"/>
	<c:if test="${usageThreshold <= 0 || count >= usageThreshold}">
		<c:set target="${filteredTags}" property="${tag.name}" value="${tag}"/>
		<c:set var="totalUsages" value="${totalUsages + count}"/>
	</c:if>
</c:forEach>

<c:if test="${not empty filteredTags}">
		<ul>
			<c:forEach items="${filteredTags}" var="tag">
				<c:set var="tagCount" value="${tag.value.weakReferences.size}"/>
				<li><a class="tag${functions:round(10 * tagCount / totalUsages)}0" title="${tag.value.name} (${tagCount})">${tag.value.name}</a></li>
			</c:forEach>
		</ul>
</c:if>
<c:if test="${empty filteredTags}">
	<fmt:message key="tags.noTags"/>
</c:if>
</div>