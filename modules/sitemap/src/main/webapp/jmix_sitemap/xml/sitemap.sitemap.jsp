<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/xml;UTF-8" language="java" %>
<c:set target="${renderContext}" property="contentType" value="text/xml;charset=UTF-8"/>
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd"
         xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
<jcr:jqom var="sitemaps">
    <query:selector nodeTypeName="jmix:sitemap" selectorName="stmp"/>
    <query:or>
        <query:descendantNode path="${currentNode.path}" selectorName="stmp"/>
        <query:sameNode path="${currentNode.path}" selectorName="stmp"/>
    </query:or>
</jcr:jqom>
<c:if test="${pageContext.request.serverPort != 80}">
    <c:set var="serverUrl" value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}"/>
</c:if>
<c:if test="${pageContext.request.serverPort == 80}">
    <c:set var="serverUrl" value="${pageContext.request.scheme}://${pageContext.request.serverName}"/>
</c:if>
    <c:forEach items="${sitemaps.nodes}" varStatus="status" var="sitemapEL">
        <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModif"/>
        <url>
            <loc>${serverUrl}<c:url value="${sitemapEL.path}.html" context="${url.base}"/></loc>
            <lastmod><fmt:formatDate value="${lastModif.date.time}" pattern="yyyy-MM-dd"/></lastmod>
            <changefreq>${sitemapEL.properties.changefreq.string}</changefreq>
            <priority>${sitemapEL.properties.priority.string}</priority>
        </url>
    </c:forEach>
</urlset>
