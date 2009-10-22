<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<jcr:nodeProperty node="${currentNode}" name="j:tags" var="assignedTags"/>
<c:if test="${not empty assignedTags}">
<c:set var="separator" value="${functions:default(renderContext.moduleParams.separator, ', ')}"/>
<div>
	<c:forEach items="${assignedTags}" var="tag" varStatus="status">
		<span>${tag.node.name}${!status.last ? separator : ''}</span>
	</c:forEach>
</div>
</c:if>