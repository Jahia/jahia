<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<jcr:nodeProperty node="${currentNode}" name="j:tags" var="assignedTags"/>
<c:set var="separator" value="${functions:default(renderContext.moduleParams.separator, ', ')}"/>
<jsp:useBean id="filteredTags" class="java.util.LinkedHashMap"/>
<c:forEach items="${assignedTags}" var="tag" varStatus="status">
	<c:if test="${not empty tag.node}">
		<c:set target="${filteredTags}" property="${tag.node.name}" value="${tag.node.name}"/>
	</c:if>
</c:forEach>
<span id="jahia-tags-${currentNode.identifier}">
	<c:forEach items="${filteredTags}" var="tag" varStatus="status">
		<span>${fn:escapeXml(tag.value)}</span>${!status.last ? separator : ''}
	</c:forEach>
</span>