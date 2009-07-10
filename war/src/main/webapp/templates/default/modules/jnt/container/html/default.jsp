<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<h2>Container : ${currentNode.name}</h2>
<ul>
<c:forEach items="${currentNode.properties}" var="property">
    <c:if test="${property.definition.jahiaContentItem}">
        <li>${property.name} ${property.string}</li>
    </c:if>
</c:forEach>
</ul>

Metadata :
<ul>
<c:forEach items="${currentNode.properties}" var="property">
    <c:if test="${property.definition.metadataItem}">
        <li>${property.name} ${property.string}</li>
    </c:if>
</c:forEach>
</ul>

