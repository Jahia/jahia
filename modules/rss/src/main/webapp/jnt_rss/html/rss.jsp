<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="rss.css" />

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="url" var="feedUrl"/>
<jcr:nodeProperty node="${currentNode}" name="nbEntries" var="nbEntries"/>
<jsp:useBean id="rss" class="org.jahia.modules.rss.RSSUtil"/>
<c:set target="${rss}" property="url" value="${feedUrl.string}"/>
<%-- load the feed using RSSUtil --%>
<c:set var="syndFeed" value="${rss.feed}"/>
<c:if test="${not empty title && not empty title.string}">
    <h3 class="titlerss">
        <img title="" alt="" src="${url.currentModule}/images/rss.png"/> ${fn:escapeXml(title.string)}
    </h3>
</c:if>
<c:if test="${empty syndFeed}">
    <jcr:nodeProperty node="${currentNode}" name="url" var="feedUrl"/>
    <fmt:message key="jnt_rss.rssLoadError"><fmt:param value="${feedUrl.string}"/></fmt:message>
</c:if>
<c:if test="${not empty syndFeed}">
    <div class="syndFeed">
        <div class="syndFeedTitle">
            <a href="${fn:escapeXml(syndFeed.link)}">${fn:escapeXml(syndFeed.title)}</a>
            <c:if test="${not empty syndFeed.image}">
                  <img src="${fn:escapeXml(syndFeed.image.url)}" title="${fn:escapeXml(syndFeed.image.title)}" alt="${fn:escapeXml(syndFeed.image.description)}"/>
            </c:if>
        </div>
        <div class="syndFeedEntries">
            <c:forEach items="${syndFeed.entries}" var="syndEntry" begin="0" end="${nbEntries.long - 1}">
                <div class="syndEntryTitle">
                    <a href="${fn:escapeXml(syndEntry.link)}">${fn:escapeXml(syndEntry.title)}&nbsp;[${fn:escapeXml(syndEntry.updatedDate)}]</a>
                </div>
                <div class="syndEntryDescription">
                    ${syndEntry.description.value}
                </div>
                <div class="syndEntryContents">
                    <c:forEach items="${syndEntry.contents}" var="syndContent">
                        <div class="syndEntryContent">
                        	${syndContent.value}
                        </div>
                    </c:forEach>
                </div>
                <div class="syndEntryAuthor">
                        ${fn:escapeXml(syndEntry.author)}
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>