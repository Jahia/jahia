<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ tag body-content="empty"
        description="Used to render an autodiscovery link for the Jahia OpenSearch provider in a page: one with HTML and one with RSS 2.0 result type." %>
<%@ attribute name="searchFor" required="false"
              description="Specifies the type of the search. Possible values are: pages (search over Jahia content) and files (search over document repository). Default value: pages." %>
<%@ attribute name="format" required="false"
              description="Specifies the type of the results view. Possible values are: html and rss. Default value: html." %>
<%@ attribute name="title" required="false"
              description="Human-readable plain text string with the search engine title." %>
<%@ attribute name="description" required="false"
              description="Human-readable text description of the search engine." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="searchFor" value="${h:default(fn:toLowerCase(searchFor), 'pages')}"/>
<c:set var="format" value="${h:default(fn:toLowerCase(format), 'html')}"/>
<%
if (!"pages".equals(jspContext.getAttribute("searchFor")) && !"files".equals(jspContext.getAttribute("searchFor"))) {
    throw new IllegalArgumentException("Unsupported search type '" + jspContext.getAttribute("searchFor") + "' for the OpenSearch provider. Supported types are 'pages' and 'files'.");
}
if (!"html".equals(jspContext.getAttribute("format")) && !"rss".equals(jspContext.getAttribute("format"))) {
    throw new IllegalArgumentException("Unsupported format type '" + jspContext.getAttribute("format") + "' for the OpenSearch provider. Supported formats are 'html' and 'rss'.");
}
%>
<c:set var="descriptor" value="/opensearch/descriptor-${searchFor}-${format}.jsp"/>
<c:if test="${empty title}">
    <c:set var="labelContent"><fmt:message key="opensearch.linkTitle.content"/></c:set>
    <c:set var="labelDocuments"><fmt:message key="opensearch.linkTitle.documentRepository"/></c:set>
    <c:set var="title" value="${jahia.site.title} - ${searchFor == 'pages' ? labelContent : labelDocuments}${format == 'rss' ? ' (RSS)' : ''}"/>
</c:if>
<c:url var="url" value="${currentPage.url}" context="/">
    <c:param name="template" value="${jahia.includes.templatePath[descriptor]}"/>
    <c:param name="title" value="${title}"/>
    <c:param name="description" value="${description}"/>
</c:url>
<link rel="search" type="application/opensearchdescription+xml" href="${fn:escapeXml(url)}" title="${fn:escapeXml(title)}" />