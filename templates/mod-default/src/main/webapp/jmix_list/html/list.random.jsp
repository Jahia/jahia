<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://www.jahia.org/tags/functions" %>

<c:if test="${currentNode.nodes.size > 0}">
    <c:set value="${fn:randomInt(currentNode.nodes.size)}" var="itemToDisplay"/>
    <c:forEach items="${currentNode.children}" var="subchild" begin="${itemToDisplay}" end="${itemToDisplay}">
        <template:module node="${subchild}" editable="true"/>
    </c:forEach>
</c:if>
<c:if test="${renderContext.editMode}">
    <c:if test="${currentNode.nodes.size <= 0}">
        <template:module path="*" />
    </c:if>
</c:if>