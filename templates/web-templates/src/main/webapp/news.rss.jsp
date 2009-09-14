<%@page language="java" contentType="text/xml;charset=UTF-8" %><?xml version="1.0" encoding="UTF-8"?>
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
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
    <%@include file="common/declarations.jspf" %>
    <template:composePageURL fullURL="true" var="url"/>
    <channel>
        <title><fmt:message key="newsChannelTitle"/></title>
        <link><c:out value="${url}" escapeXml="true"/></link>
        <description><fmt:message key="newsChannelDescription"/></description>
        <language>${requestScope.currentRequest.locale}</language>
        <dc:language>${requestScope.currentRequest.locale}</dc:language>
        <generator>Jahia 6.0, http://www.jahia.org</generator>
        <template:containerList name="${param.definitionName}" displayActionMenu="false" id="newsList"
                            displaySkins="false" displayExtensions="false">
            <template:container id="newsContainer" displayActionMenu="false" displayContainerAnchor="false"
                               displaySkins="false" displayExtensions="false" cacheKey="rss">
                <c:url var="detailsUrl" value="${url}" context="/">
                    <c:param name="template" value="tpl.newsDetail"/>
                    <c:param name="queryPath" value="${newsContainer.JCRPath}"/>
                </c:url>
                <template:field name="newsTitle" var="newsTitle" display="false" removeHtmlTags="true"/>
                <template:field name="newsDesc" var="newsDesc" display="false" removeHtmlTags="true"/>
                <template:field name="newsDate" var="newsDate" display="false"/>
                <item>
                    <title><c:out value="${newsTitle}"/></title>
                    <description><![CDATA[<c:out value="${newsDesc}"/>]]></description>
                    <guid isPermaLink="false"><c:out value="${detailsUrl}" escapeXml="true"/></guid>
                    <template:getContentObjectCategories objectKey="ContentContainer_${newsContainer.ID}" asSet="true"
                                                        var="categories"/>
                    <c:forEach items="${categories}" var="category">
                        <category><![CDATA[<c:out value="${category.title}"/>]]></category>
                    </c:forEach>
                    <pubDate><c:out value="${newsDate}"/></pubDate>
                    <dc:date><c:out value="${newsDate}"/></dc:date>
                    <link><c:out value="${detailsUrl}" escapeXml="true"/></link>
                </item>
            </template:container>
        </template:containerList>
    </channel>
</rss>