<?xml version="1.0" encoding="ISO-8859-1"?>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set target="${renderContext}" property="contentType" value="text/xml"/>
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
    <channel>
        <title>RSS Blog</title>
        <link><c:url value='${url.base}${currentResource.node.path}.html'/> </link>
        <description>Blog rss feed</description>
        <language>${requestScope.currentRequest.locale}</language>
        <dc:language>${requestScope.currentRequest.locale}</dc:language>
        <generator>Jahia 6.0, http://www.jahia.org</generator>
        <c:forEach items="${currentNode.nodes}" var="child">
            <template:module node="${child}"/>
        </c:forEach>
    </channel>
</rss>
