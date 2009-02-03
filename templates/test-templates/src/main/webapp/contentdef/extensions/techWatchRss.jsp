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
                <template:field name="title" valueBeanID="title" display="false"/>
                <template:field name="teaser" valueBeanID="teaser" display="false"/>
                <template:field name="date" valueBeanID="date" display="false"/>
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
