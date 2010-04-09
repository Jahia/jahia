<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty currentNode.properties['jcr:title']}">
    <a href="${url.current}">${currentNode.properties['jcr:title'].string}</a>
</c:if>
<c:if test="${empty currentNode.properties['jcr:title']}">
    <a href="${url.current}">${currentNode.name}</a>
</c:if>