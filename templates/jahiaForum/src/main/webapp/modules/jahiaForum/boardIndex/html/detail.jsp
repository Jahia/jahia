<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="board-subject">
    <jcr:nodeProperty node="${currentNode}" name="boardSubject"/> :
    <c:if test="${not empty currentNode.children}">${fn:length(currentNode.children)} Topics </c:if>
</div>
<ul>
<c:forEach items="${currentNode.editableChildren}" var="topic">
    <li>
        <template:module node="${topic}" template="summary"/>
    </li>
</c:forEach>
</ul>