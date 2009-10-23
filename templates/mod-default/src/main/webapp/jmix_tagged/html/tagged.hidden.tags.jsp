<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<jcr:nodeProperty node="${currentNode}" name="j:tags" var="assignedTags"/>
<c:set var="separator" value="${functions:default(renderContext.moduleParams.separator, ', ')}"/>
<span id="jahia-tags-${currentNode.identifier}">
	<c:forEach items="${assignedTags}" var="tag" varStatus="status">
		<span>${tag.node.name}</span>${!status.last ? separator : ''}
	</c:forEach>
</span>