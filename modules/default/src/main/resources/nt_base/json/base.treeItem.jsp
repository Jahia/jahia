<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<c:set var="nodeTypes" value="${functions:default(currentResource.moduleParams.nodeTypes, param.nodeTypes)}"/>
<c:set var="selectableNodeTypes" value="${functions:default(currentResource.moduleParams.selectableNodeTypes, param.selectableNodeTypes)}"/>
<c:if test="${empty nodeTypes || jcr:isNodeType(currentNode, nodeTypes)}">
<json:object>
	<json:property name="id" value="${currentNode.identifier}"/>
	<json:property name="path" value="${currentNode.path}"/>
	<c:if test="${jcr:isNodeType(currentNode, 'mix:title')}">
	<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
	</c:if>
	<json:property name="text" value="${not empty title ? title.string : currentNode.name}"/>
	<c:if test="${(empty selectableNodeTypes || jcr:isNodeType(currentNode, selectableNodeTypes)) and (empty param.displayablenodeonly or (param.displayablenodeonly eq 'true' and jcr:isDisplayable(currentNode, renderContext)))}">
		<json:property name="classes" value="selectable"/>
    </c:if>
	<json:property name="hasChildren" value="${not empty nodeTypes ? jcr:hasChildrenOfType(currentNode, nodeTypes) : currentNode.nodes.size > 0}"/>
</json:object>
</c:if>
