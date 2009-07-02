<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<h2>Page : ${currentNode.name}</h2>
<c:forEach items="${currentNode.children}" var="child">
    <ul>
        <li>${child.name}</li>
        <li><template:module node="child" /></li>
    </ul>
</c:forEach>

<p><a href="<%= request.getContextPath() %>/render/default${currentNode.path}.html"><%= request.getContextPath() %>/render/default/${currentNode.path}.html</a></p>