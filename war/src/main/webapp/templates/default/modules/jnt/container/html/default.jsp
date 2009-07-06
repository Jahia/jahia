<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<h2>Container : ${currentNode.name}</h2>
<ul>
<c:forEach items="${currentNode.properties}" var="property">
    <li>${property.name} ${property.string} ${property.definition.declaringNodeType.name}</li>
</c:forEach>
</ul>
<p><a href="${pageContext.request.contextPath}/render/default${currentNode.path}.html">${pageContext.request.contextPath}/render/default/${currentNode.path}.html</a></p>