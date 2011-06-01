<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="nodeTypes" value="${functions:default(currentResource.moduleParams.nodeTypes, param.nodeTypes)}"/>
<c:set var="selectableNodeTypes" value="${functions:default(currentResource.moduleParams.selectableNodeTypes, param.selectableNodeTypes)}"/>
<json:array>
<json:object>
	<json:property name="id" value="${currentNode.identifier}"/>
	<json:property name="path" value="${currentNode.path}"/>
	<c:if test="${jcr:isNodeType(currentNode, 'mix:title')}">
	<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
	</c:if>
	<json:property name="text" value="${not empty title ? title.string : currentNode.name}"/>
	<json:property name="expanded" value="true"/>
	<json:property name="nodeType" value="${currentNode.primaryNodeTypeName}"/>
	<c:if test="${empty selectableNodeTypes || jcr:isNodeType(currentNode, selectableNodeTypes)}">
		<json:property name="classes" value="selectable"/>
	</c:if>
	<template:module node="${currentNode}" templateType="json" editable="false" view="tree">
		<template:param name="arrayName" value="children" />
	</template:module>
</json:object>
</json:array>