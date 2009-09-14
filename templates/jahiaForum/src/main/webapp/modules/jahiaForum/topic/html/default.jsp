<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="topic-subject">
    <jcr:nodeProperty node="${currentNode}" name="topicSubject"/> : 
    <c:if test="${not empty currentNode.editableChildren}">${fn:length(currentNode.editableChildren)} Posts </c:if>
</div>
<ul>
    <c:forEach items="${currentNode.editableChildren}" var="thread" varStatus="status">
        <li>
            <template:module node="${thread}" template="default"/>${status.count}
        </li>
    </c:forEach>
</ul>
