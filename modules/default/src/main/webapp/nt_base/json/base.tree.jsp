<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="nodeTypes" value="${functions:default(currentResource.moduleParams.nodeTypes, param.nodeTypes)}"/>
<json:array name="${currentResource.moduleParams.arrayName}">
	<c:forEach items="${not empty nodeTypes ? jcr:getChildrenOfType(currentNode, nodeTypes) : currentNode.nodes}" var="child">
		<template:module node="${child}" templateType="json" editable="false" view="treeItem"/>
	</c:forEach>
</json:array>