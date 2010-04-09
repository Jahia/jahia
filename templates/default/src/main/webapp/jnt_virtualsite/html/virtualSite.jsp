<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>

<h1>Site: ${currentNode.name}</h1>
<p>Title: <jcr:nodeProperty node="${currentNode}" name="j:title"/></p>
<p>Server name: <jcr:nodeProperty node="${currentNode}" name="j:serverName"/></p>
<p>Description: <jcr:nodeProperty node="${currentNode}" name="j:description"/></p>
<p>Nodes:</p>
<ul>
<c:forEach var="child" items="${currentNode.nodes}">
    <li><a href="${url.base}${child.path}.html">${child.name}</a></li>
</c:forEach>
</ul>
