<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<div>
    <c:if test="${jcr:isNodeType(currentNode.parent.parent, 'jahiaForum:boardIndex')}">
        <a href="${url.base}${currentNode.parent.parent.path}.detail.html">${currentNode.parent.parent.propertiesAsString['boardSubject']}</a>&nbsp;>>&nbsp;
    </c:if>
    <c:if test="${jcr:isNodeType(currentNode.parent, 'jahiaForum:topic')}">
        <a href="${url.base}${currentNode.parent.path}.html">${currentNode.parent.propertiesAsString['topicSubject']}</a>&nbsp;>>&nbsp;
    </c:if>
    ${currentNode.propertiesAsString['threadSubject']}
</div>
<ul>
    <c:forEach items="${currentNode.editableChildren}" var="subchild" varStatus="status">
        <li>
            <template:module node="${subchild}" template="default"/>
        </li>
    </c:forEach>
    <li>
        <template:module node="${currentNode}" template="newPostForm"/>
    </li>
</ul>
    