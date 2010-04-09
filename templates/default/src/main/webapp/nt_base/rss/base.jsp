<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<item>
    <title>${fn:escapeXml(currentNode.name)}</title>
    <link>${pageContext.request.contextPath}/files${currentNode.path}</link>
    <description>${fn:escapeXml(currentNode.name)}</description>
    <jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
    <pubDate>${created.date}</pubDate>
    <dc:date>${created.date}</dc:date>
</item>