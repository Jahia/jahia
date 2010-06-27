<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<item>
    <title>${currentNode.name}</title>
    <link>${pageContext.request.contextPath}/files${currentNode.path}</link>
    <description>${currentNode.name}</description>
    <jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
    <pubDate>${newsDate}.date</pubDate>
    <dc:date>${newsDate}.date</dc:date>
</item>