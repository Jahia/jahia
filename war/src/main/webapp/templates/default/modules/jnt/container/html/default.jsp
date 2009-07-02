<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<h2>Container : ${currentNode.name}</h2>
<c:forEach items="${currentNode.properties}" var="property">
    <ul>
        <li>${property.name} ${property.string} ${property.definition.declaringNodeType.name}</li>
    </ul>
</c:forEach>

<p><a href="<%= request.getContextPath() %>/render/default${currentNode.path}.html"><%= request.getContextPath() %>/render/default/${currentNode.path}.html</a></p>