<%@ page import="javax.jcr.Node" %>
<?xml version="1.0" encoding="iso-8859-1"?>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
    <channel>
        <title>${currentNode.name}</title>
        <link><%= request.getContextPath() %>/render/default${currentNode.path}.rss</link>
        <description>${currentNode.name}</description>
        <generator>Jahia 6.0, http://www.jahia.org</generator>
            <c:forEach items="${currentNode.children}" var="child">
                <% if (((Node)pageContext.getAttribute("child")).isNodeType("nt:file")) { %>
                <item>
                    <title>${child.name}</title>
                    <link><%= request.getContextPath() %>/files${child.path}</link>
                    <description>${child.name}</description>
                    <jcr:nodeProperty node="${child}" name="jcr:created" var="created"/>
                    <pubDate>${newsDate}.date</pubDate>
                    <dc:date>${newsDate}.date</dc:date>
                </item>
                <% } %>
            </c:forEach>
    </channel>
</rss>