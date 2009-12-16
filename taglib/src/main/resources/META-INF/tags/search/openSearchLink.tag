<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="searchFor" value="${functions:default(fn:toLowerCase(searchFor), 'pages')}"/>
<c:set var="format" value="${functions:default(fn:toLowerCase(format), 'html')}"/>
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