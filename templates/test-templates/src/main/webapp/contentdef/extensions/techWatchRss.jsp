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
<%@page language="java" contentType="text/xml;charset=UTF-8" %><?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0"
     xmlns:dc="http://purl.org/dc/elements/1.1/">
    <%@ include file="../../common/declarations.jspf"%>
    <template:composePageURL fullURL="true" valueID="url"/>
    <channel>
        <title>Technology news</title>
        <link><c:out value="${url}"/></link>
        <description>Hot news in the technology area</description>
        <language>${jahia.requestInfo.locale}</language>
        <dc:language>${jahia.requestInfo.locale}</dc:language>
        <generator>Jahia 6.0, http://www.jahia.org</generator>
        <template:containerList name="${param.type}" displayActionMenu="false" id="myList"
                            displaySkins="false" displayExtensions="false">
            <template:container id="myContainer" displayActionMenu="false" displayContainerAnchor="false"
                               displaySkins="false" displayExtensions="false" cacheKey="myrss">
                <template:field name="title" var="title" display="false" removeHtmlTags="true"/>
                <template:field name="teaser" var="teaser" display="false" removeHtmlTags="true"/>
                <template:field name="date" var="date" display="false"/>
                <item>
                    <title><c:out value="${title}"/></title>
                    <description><![CDATA[<c:out value="${teaser}"/>]]></description>
                    <guid isPermaLink="false"><c:out value="${url}"/>#ContentContainer_${myContainer.ID}</guid>
                    <pubDate><c:out value="${date}"/></pubDate>
                    <dc:date><c:out value="${date}"/></dc:date>
                    <link><c:out value="${url}"/>#ContentContainer_${myContainer.ID}</link>
                </item>
            </template:container>
        </template:containerList>
    </channel>
</rss>
