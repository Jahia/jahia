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

    <link rel="stylesheet" href="<c:out value='${request.contextPath}'/>/iphone/Design/Render.css"/>
    <script type="text/javascript" src="<c:out value='${request.contextPath}'/>/iphone/Action/Logic.js"></script>
</head>
<body>
<div id="WebApp">

    <div id="iHeader">
        <a href="#" id="waBackButton">Back</a>
        <span id="waHeadTitle">Jahia News</span>
    </div>

    <div id="iGroup">

        <div id="iLoader">Loading, please wait...</div>

        <div class="iLayer" id="waNews" title="News">

            <div class="iMenu">
                <h3><fmt:message key='news'/></h3>

                <!--start newslist -->
                <template:containerList id="newsList" windowSize="10" displayActionMenu="false">
                    <query:containerQuery>
                        <query:selector nodeTypeName="community_templates:communityNews" selectorName="news"/>
                        <query:descendantNode selectorName="newsList" path="${currentSite.JCRPath}"/>
                        <query:sortBy propertyName="newsDate" order="${queryConstants.ORDER_DESCENDING}"/>
                    </query:containerQuery>
                    <ul class="iArrow">
                        <template:container id="newsContainer" displayActionMenu="false" cacheKey="iphone_menu"
                                            displaySkins="false" displayContainerAnchor="false">
                            <!--start newsListItem -->
                            <li><a href="#_Container<c:out value='${newsContainer.id}'/>"><template:field
                                    name="newsTitle" diffActive="false"/></a>
                            </li>
                            <!--stop newsListItem -->
                        </template:container>
                    </ul>

                </template:containerList>
                <!--stop newslist -->
            </div>
        </div>

        <!--start newslist -->
        <template:containerList id="newsList" windowSize="10" displayActionMenu="false">
            <query:containerQuery>
                <query:selector nodeTypeName="community_templates:communityNews" selectorName="news"/>
                <query:descendantNode selectorName="newsList" path="${currentSite.JCRPath}"/>
                <query:sortBy propertyName="newsDate" order="${queryConstants.ORDER_DESCENDING}"/>
            </query:containerQuery>

            <template:container id="newsContainer" displayActionMenu="false" cacheKey="iphone_details"
                                displaySkins="false" displayContainerAnchor="false">
                <div class="iLayer" id="waContainer<c:out value='${newsContainer.id}'/>" title="News Detail">
                    <div class="iBlock">
                        <template:image file="newsImage" />
                        <template:field name="newsTitle" var="newsTitle" display="false"/>
                        <h4><template:link page="newsLink" linkBody="${newsTitle}"/></h4>

                        <p class="newsInfo">
                            <span class="newsLabelName">Auteur :</span>
                            <span class="newsName"></span>
                            <span class="newsLabelDate">Date :</span>
                            <span class="newsDate"><template:field name='newsDate'/></span>
                        </p>

                        <p class="newsResume">
                            <template:field name="newsDesc"/>
                        </p>

                        <div class="newsMeta">
                            <span class="categoryLabel"><fmt:message key='category'/>  :</span>
                            <template:getContentObjectCategories valueID="newsContainerCatKeys"
                                                                 objectKey="contentContainer_${pageScope.newsContainer.ID}"/>
                            <ui:displayCategoryTitle categoryKeys="${newsContainerCatKeys}"/>
                        </div>
                        <!--stop newsListItem -->
                    </div>
                </div>
            </template:container>
        </template:containerList>
        <!--stop newslist -->

    </div>
</div>
</body>
</html>
