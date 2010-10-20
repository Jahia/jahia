<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${not empty currentNode.properties['jcr:title']}">
    <li><a href="#anchor${currentNode.UUID}">${currentNode.properties['jcr:title'].string}</a></li>
</c:if>

<c:if test="${empty currentNode.properties['jcr:title']}">
    <c:if test="${not empty currentNode.properties['j:nodename']}">
        <li><a href="#anchor${currentNode.UUID}">${currentNode.properties['j:nodename'].string}</a></li>
    </c:if>
</c:if>
