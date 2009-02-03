<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.
--%>
<%@page language="java" contentType="text/xml;charset=UTF-8" %><?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/">
    <%@include file="common/declarations.jspf" %>
    <template:composePageURL fullURL="true" valueID="url"/>
    <channel>
        <title><utility:resourceBundle name="newsChannelTitle" defaultValue="Homepage news"/></title>
        <link><c:out value="${url}"/></link>
        <description><utility:resourceBundle name="newsChannelDescription"
                                             defaultValue="Lists all the news that are on the given page"/></description>
        <language>${requestScope.currentRequest.locale}</language>
        <dc:language>${requestScope.currentRequest.locale}</dc:language>
        <generator>Jahia 6.0, http://www.jahia.org</generator>
        <template:containerList name="${param.definitionName}" displayActionMenu="false" id="newsList"
                            displaySkins="false" displayExtensions="false">
            <template:container id="newsContainer" displayActionMenu="false" displayContainerAnchor="false"
                               displaySkins="false" displayExtensions="false" cacheKey="rss">
                <template:field name="newsTitle" valueBeanID="newsTitle" display="false"/>
                <template:field name="newsDesc" valueBeanID="newsDesc" display="false"/>
                <template:field name="newsDate" valueBeanID="newsDate" display="false"/>
                <item>
                    <title><c:out value="${newsTitle}"/></title>
                    <description><![CDATA[<c:out value="${newsDesc}"/>]]></description>
                    <guid isPermaLink="false"><c:out value="${url}"/>#ContentContainer_${newsContainer.ID}</guid>
                    <template:getContentObjectCategories objectKey="ContentContainer_${newsContainer.ID}" asSet="true"
                                                        valueID="categories"/>
                    <c:forEach items="${categories}" var="category">
                        <category><![CDATA[<c:out value="${category.title}"/>]]></category>
                    </c:forEach>
                    <pubDate><c:out value="${newsDate}"/></pubDate>
                    <dc:date><c:out value="${newsDate}"/></dc:date>
                    <link><c:out value="${url}"/>#ContentContainer_${newsContainer.ID}</link>
                </item>
            </template:container>
        </template:containerList>
    </channel>
</rss>