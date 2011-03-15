<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h2><fmt:message key="label.system.rootfolder"/> <a href="${url.current}">${currentNode.name}</a></h2>

<ul>
    <c:set var="parent" value="${currentNode.parent}"/>
    <c:if test="${parent.name != ''}">
        <li><a href="<c:url value='${url.base}${parent.path}.html'/>">..</a></li>
    </c:if>
<c:forEach var="child" items="${currentNode.nodes}">
    <li>
        <a href="<c:url value='${url.base}${child.path}.html'/>">${child.name}</a>
    </li>
</c:forEach>
</ul>