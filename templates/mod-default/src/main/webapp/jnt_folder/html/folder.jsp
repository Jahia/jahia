<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>

<h2>Folder: <a href="${url.base}${currentNode.path}.html">${currentNode.name}</a></h2>

<ul>
    <c:set var="parent" value="${currentNode.parent}"/>
    <c:if test="${parent.name != ''}">
        <li><a href="${url.base}${parent.path}.html">..</a></li>
    </c:if>
<c:forEach var="child" items="${currentNode.nodes}">
    <li>
        <c:if test="${jcr:isNodeType(child, 'jnt:group')}" var="isGroup">
            <%@ include file="folder.groups.jspf" %>
        </c:if>
        <c:if test="${!isGroup}">
            <a href="${url.base}${child.path}.html">${child.name}</a>
        </c:if>
    </li>
</c:forEach>
</ul>
