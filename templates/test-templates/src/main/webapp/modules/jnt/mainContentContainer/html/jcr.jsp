<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<p>
<ul>
    <li>Node: ${currentNode.name}</li>
    <li>Date: ${currentNode.lastModifiedAsDate}</li>
    <jcr:nodeProperty node="${currentNode}" name="mainContentTitle" var="mainContentTitle"/>
    <li>Title : ${mainContentTitle.string}</li>
    <li>URL: <a href="<%= request.getContextPath() %>/render/default${currentNode.path}.jcr.html">${currentNode.name}.jcr.html</a></li>
</ul>
</p>
