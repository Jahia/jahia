<%@ page language="java" contentType="text/xml;charset=UTF-8"%><?xml version="1.0" encoding="UTF-8"?>
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
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
    <%@include file="common/declarations.jspf" %>
    <utility:setBundle basename="resources.SocialTemplates"/>
    <template:composePageURL fullURL="true" valueID="url"/>
    <channel>
        <title><fmt:message key="rss.blogChannelTitle"/></title>
        <link><c:out value="${url}"/></link>
        <description><fmt:message key="rss.blogChannelDescription"/></description>
        <language>${requestScope.currentRequest.locale}</language>
        <dc:language>${requestScope.currentRequest.locale}</dc:language>
        <generator>Jahia 6.0, http://www.jahia.org</generator>
        <template:containerList name="${param.definitionName}" displayActionMenu="false" id="blogArticles"
                            displaySkins="false" displayExtensions="false">
            <template:container id="blogArticle" displayActionMenu="false" displayContainerAnchor="false"
                               displaySkins="false" displayExtensions="false" cacheKey="rss">
                <template:field name="title" var="blogTitle" display="false" removeHtmlTags="true"/>
                <template:field name="content" var="blogDesc" display="false" removeHtmlTags="true"/>
                <template:field name="date" var="blogDate" display="false"/>
                <item>
                    <title><c:out value="${blogTitle}"/></title>
                    <description><![CDATA[<c:out value="${blogDesc}"/>]]></description>
                    <guid isPermaLink="false">${url}/template/tpl.blog?queryPath=${blogArticle.JCRPath}</guid>
                    <template:getContentObjectCategories objectKey="ContentContainer_${blogArticle.ID}" asSet="true"
                                                        valueID="categories"/>
                    <c:forEach items="${categories}" var="category">
                        <category><![CDATA[<c:out value="${category.title}"/>]]></category>
                    </c:forEach>
                    <pubDate><c:out value="${blogDate}"/></pubDate>
                    <dc:date><c:out value="${blogDate}"/></dc:date>
                    <link>${url}/template/tpl.newsDetail?queryPath=${newsContainer.JCRPath}</link>
                </item>
            </template:container>
        </template:containerList>
    </channel>
</rss>