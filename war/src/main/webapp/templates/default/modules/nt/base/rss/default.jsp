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
        <link>${pageContext.request.contextPath}/render/default${currentNode.path}.rss</link>
        <description>${currentNode.name}</description>
        <generator>Jahia 6.0, http://www.jahia.org</generator>
            <c:forEach items="${currentNode.children}" var="child">
                <template:module templateType="rss" template="item" node="${child}" />
            </c:forEach>
    </channel>
</rss>