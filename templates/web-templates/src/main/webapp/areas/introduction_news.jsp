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

<%@ include file="../common/declarations.jspf" %>

        <template:include page="common/breadcrumb.jsp"/>
        <h2><c:out value="${requestScope.currentPage.highLightDiffTitle}" escapeXml="false"/></h2>
        <template:include page="modules/introduction/introductionDisplay.jsp"/>

        <div class="box4 "><!--start box 4 default-->
            <div class="box4-topright"></div>
            <div class="box4-topleft"></div>
            <h3 class="box4-header"><span class="newsTitle"><c:out
                    value="${requestScope.currentPage.highLightDiffTitle}" escapeXml="false"/></span></h3>

            <div class="newsRss"><a href="${param.rssFeed}"><fmt:message key="rss.subscribe"/></a>
            </div>
            <div class="box4-bottomright"></div>
            <div class="box4-bottomleft"></div>
            <div class="clear"> </div>
        </div>
        <template:include page="modules/news/newsDisplay.jsp"/>

