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
        <span id="waHeadTitle"><fmt:message key='iphone.label.home'/></span>
    </div>
    <div id="iGroup">
        <div id="iLoader"><fmt:message key='iphone.label.loading'/></div>
        <div class="iLayer" id="waNews"
             title="<fmt:message key='iphone.label.teasers'/>">
            <div class="iMenu">
                <h3>Teasers</h3>
                <template:containerList name="promo" id="promoList" displayActionMenu="false">
                    <ul class="iArrow">
                        <template:container id="promoContainer" displayActionMenu="false" cacheKey="iphone_menu"
                                            displaySkins="false" displayContainerAnchor="false">
                            <li><a href="#_Container<c:out value='${promoContainer.ID}'/>"><template:field name="title"
                                                                                                           diffActive="false"/></a>
                            </li>
                        </template:container>
                    </ul>
                </template:containerList>
            </div>
        </div>

        <!--start newslist -->
        <template:containerList name="promo" id="promoList" displayActionMenu="false">
            <template:container id="promoContainer" displayActionMenu="false" cacheKey="iphone_details"
                                displaySkins="false" displayContainerAnchor="false">

                <div class="iLayer" id="waContainer<c:out value='${promoContainer.ID}'/>"
                     title="<fmt:message key='iphone.label.teaser.detail'/>">
                    <div class="iBlock">
                        <h4><template:field name="title" diffActive="false"/></h4>

                        <p>
                            <template:field name="abstract" diffActive="false"/><br/>
                            <template:field name="link" display="false" var="promolink"/>
                            <c:if test="${!empty promolink}">
                                <template:link page="link" maxChar="20"/>
                            </c:if>
                        </p>
                    </div>
                </div>
            </template:container>
        </template:containerList>
    </div>
</div>

</body>
</html>