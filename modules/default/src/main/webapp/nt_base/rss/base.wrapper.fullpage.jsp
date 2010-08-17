<%@ page import="org.jahia.bin.Jahia" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<c:set target="${renderContext}" property="contentType" value="application/rss+xml;charset=UTF-8"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:description" var="description"/>
<c:if test="${not empty title}">
    <c:set var="title" value="${title.string}"/>
</c:if>
<c:if test="${empty title}">
    <c:set var="title" value="${currentNode.name}"/>
</c:if>
<c:if test="${not empty description}">
    <c:set var="description" value="${description.string}"/>
</c:if>
<c:if test="${empty description}">
    <c:set var="description" value="${title}"/>
</c:if>
<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
    <channel>
        <title>${fn:escapeXml(title)}</title>
        <link><c:url value="${url.server}${url.base}${currentNode.path}.html" context="/"/></link>
        <description>${fn:escapeXml(description)}</description>
        <generator>Jahia <%= Jahia.VERSION + "." + Jahia.getPatchNumber() + " r" + Jahia.getBuildNumber() %>, http://www.jahia.org</generator>
        ${wrappedContent}
    </channel>
</rss>