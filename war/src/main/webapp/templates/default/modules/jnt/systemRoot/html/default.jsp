<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>

<h2>System root folder: <a href="${pageContext.request.contextPath}/render/default${currentNode.path}.html">${currentNode.name}</a></h2>

<ul>
    <c:set var="parent" value="${currentNode.parent}"/>
    <c:if test="${parent.name != ''}">
        <li><a href="${pageContext.request.contextPath}/render/default${parent.path}.html">..</a></li>
    </c:if>
<c:forEach var="child" items="${currentNode.nodes}">
    <li>
        <a href="${pageContext.request.contextPath}/render/default${child.path}.html">${child.name}</a>            
    </li>
</c:forEach>
</ul>