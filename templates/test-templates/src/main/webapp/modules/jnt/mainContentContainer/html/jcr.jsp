<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
jcr template
<ul>
    <li>Node: ${currentNode.name}</li>
    <li>Date: ${currentNode.lastModifiedAsDate}</li>
    <jcr:nodeProperty node="${currentNode}" name="mainContentTitle" var="mainContentTitle"/>
    <li>Title : ${mainContentTitle.string}</li>
</ul>
URL: <a href="<%= request.getContextPath() %>/render/default${currentNode.path}.jcr.html"><%= request.getContextPath() %>/render/default/${currentNode.path}.jcr.html</a>
