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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="common/declarations.jspf" %>
<html>
<head>
    <meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>
    <meta name="apple-mobile-web-app-capable" content="yes"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent"/>

    <link rel="stylesheet" href="<utility:resolvePath value='iphone/Design/Render.css'/>"/>
    <script type="text/javascript" src="<utility:resolvePath value='iphone/Action/Logic.js'/>"></script>
</head>
<body>
<div id="WebApp">

    <div id="iHeader">
        <a href="#" id="waBackButton">Back</a>
        <span id="waHeadTitle">${currentSite.title}</span>
    </div>

    <div id="iGroup">

        <div id="iLoader">Loading, please wait...</div>

        <div class="iLayer" id="waNews" title="News">

            <div class="iMenu">
                <h3><fmt:message key='template.name.blog'/></h3>

                <!--start newslist -->
                <template:containerList name="blogEntries" id="blogEntriesPagination" windowSize="5" displayActionMenu="false">
                    <ul class="iArrow">
                        <template:container id="blogEntry" displayActionMenu="false" cacheKey="iphone_menu"
                                            displaySkins="false" displayContainerAnchor="false" displayExtensions="false">
                            <!--start newsListItem -->
                            <li><a href="#_Container<c:out value='${blogEntry.ID}'/>"><template:field
                                    name="title" diffActive="false"/></a>
                            </li>
                            <!--stop newsListItem -->
                        </template:container>
                    </ul>
                 </template:containerList>

                <!--stop newslist -->
            </div>
        </div>

        <!--start newslist -->
        <template:containerList name="blogEntries" id="blogEntriesPagination" windowSize="5" displayActionMenu="false">
            <template:container id="blogEntry" displayActionMenu="false" cacheKey="iphone_details"
                                displaySkins="false" displayContainerAnchor="false">
                <template:getContentObjectCategories valueID="blogEntryCatKeys"
                                         objectKey="contentContainer_${pageScope.entry.ID}"/>
                <div class="iLayer" id="waContainer<c:out value='${blogEntry.ID}'/>" title="Blog Detail">
                    <div class="iBlock">
       <template:containerList name="comment" id="comment" displayActionMenu="false">
            <c:set var="nbComment" value="${comment.size}"/>
        </template:containerList>
        <div class="post"><!--start post-->
            <div class="post-date"><span><fmt:formatDate pattern="MMMM" value="${date.date}"/></span><fmt:formatDate pattern="dd" value="${date.date}"/></div>
            <h2 class="post-tit:deplle"> <a href="?article=${blogEntry.ID}"><template:field name="title"/></a></h2>
            <template:metadata metadataName="createdBy" contentBean="${blogEntry}" var="createdBy"/>
            <p class="post-info">Par <a href="?user=${createdBy}">${createdBy}.</a> <fmt:formatDate pattern="dd MMMM aaaa h:m" value="${date.date}"/>-
            <c:if test="${!empty blogEntryCatKeys }">
                <c:forEach var="blogEntryCatKey" items="${fn:split(blogEntryCatKeys, '$$$')}">
                    &nbsp;<a href="${currentPage.url}?category=${blogEntryCatKey}"><ui:displayCategoryTitle categoryKeys="${blogEntryCatKey}"/></a>
                </c:forEach>
             </c:if>
            </p>
            <template:metadata metadataName="keywords" contentBean="${blogEntry}" var="keywords"/>
            <ul class="post-tags">
                <c:forEach var="keyword" items="${fn:split(keywords, ',')}">
                    <c:if test="${fn:length(keyword) > 0}">
                        <li><a href="${currentPage.url}?keyword=${keyword}"><c:out value="${keyword}"/></a></li>
                    </c:if>
                </c:forEach>
            </ul>
            <div class="post-content"><p><template:field name="content"/></p>
            </div>
            <template:field name="isCommentable" var="commentable" display="false"/>

             <p class="post-info-links">
                <c:if test="${commentable.boolean}">
                <c:if test="${nbComment > 0}">
                        <fmt:message key="number.comment">
                            <fmt:param value="${nbComment}"/>
                        </fmt:message>
                        <a class="comment_count" href="?article=${blogEntry.ID}#comment"><fmt:message key="number.comment.add"/></a>
                </c:if>
                <c:if test="${nbComment == 0}"><fmt:message key="no.comment"/>
                    <a class="comment_count" href="?article=${blogEntry.ID}"><fmt:message key="no.comment.add"/></a>
                </c:if>
                </c:if>
                <a class="ping_count" href="#">aucun r&eacute;trolien</a>
            </p>
        </div>
                    </div>
                </div>
            </template:container>
        </template:containerList>
        <!--stop newslist -->

    </div>
</div>
</body>
</html>
