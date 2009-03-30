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
                <h3><fmt:message key='news'/></h3>

                <!--start newslist -->
                <template:containerList name="blogEntries" id="blogEntriesPagination" windowSize="5" displayActionMenu="false">
                    <ul class="iArrow">
                        <template:container id="entry" displayActionMenu="false" cacheKey="iphone_menu"
                                            displaySkins="false" displayContainerAnchor="false">
                            <!--start newsListItem -->
                            <li><a href="#_Container<c:out value='${entry.ID}'/>"><template:field
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
            <template:container id="entry" displayActionMenu="false" cacheKey="iphone_details"
                                displaySkins="false" displayContainerAnchor="false">
                <template:getContentObjectCategories valueID="blogEntryCatKeys"
                                         objectKey="contentContainer_${pageScope.entry.ID}"/>
                <div class="iLayer" id="waContainer<c:out value='${entry.ID}'/>" title="Blog Detail">
                    <div class="iBlock">
                        <template:field name="title" var="title" display="false"/>
                        <h4><c:out value="${title}"/></h4>

                        <p class="newsInfo">
                            <span class="newsLabelName">Auteur :</span>
                            <span class="newsName"></span>
                            <span class="newsLabelDate">Date :</span>
                            <span class="newsDate"><template:field name='date'/></span>
                        </p>

                        <p class="newsResume">
                            <template:field name="newsDesc"/>
                        </p>

                        <div class="newsMeta">
                            <span class="categoryLabel"><fmt:message key='category'/>  :</span>
                            <<c:if test="${!empty blogEntryCatKeys }">
                <c:forEach var="blogEntryCatKey" items="${fn:split(blogEntryCatKeys, '$$$')}">
                    &nbsp;<a href="#"><ui:displayCategoryTitle categoryKeys="${blogEntryCatKey}"/></a>
                </c:forEach>
             </c:if>
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
