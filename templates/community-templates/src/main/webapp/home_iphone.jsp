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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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
