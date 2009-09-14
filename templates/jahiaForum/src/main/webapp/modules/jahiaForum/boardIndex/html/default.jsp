<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="board-subject">
    <a href="${url.base}${currentNode.path}.detail.html"><jcr:nodeProperty node="${currentNode}" name="boardSubject"/> : 
    <c:if test="${not empty currentNode.children}">${fn:length(currentNode.children)} Topics </c:if></a>
</div>
