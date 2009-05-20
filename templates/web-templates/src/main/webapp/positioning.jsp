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
<c:set var="useGWT" value="${!empty param.useGWT}"/>
<template:template gwtForGuest="${useGWT}">
    <template:templateHead>
        <template:meta name="keywords" metadata="keywords"/>
        <template:meta name="description" metadata="description"/>
        <!-- All headers and declarations global meta and css-->
        <%@ include file="common/head.jspf" %>
        <utility:applicationResources/>
        <c:if test="${ !empty param.rssFeed }">
            <link rel="alternate" type="application/rss+xml" title="web templates : news"
                  href="${currentPage.url}/${param.rssFeed}"/>
        </c:if>
        <c:if test="${!empty param.opensearch}">
            <s:openSearchLink searchFor="pages"/>
            <s:openSearchLink searchFor="files"/>
        </c:if>
    </template:templateHead>
    <template:templateBody gwtScript="${param.gwtScript}">
        <div id="bodywrapper">
            <div id="contentArea">
                <div id="toplinks">
                    <%@ include file="common/top.jspf" %>
                </div>
                <div id="page">
                    <div id="pageHeader">
                        <div id="logotop">
                            <template:include page="modules/logo.jsp"/>
                        </div>
                        <h1 class="hide">${currentSite.title} : ${currentPage.title}</h1>
                        <%@ include file="common/nav/mainNav.jspf" %>
                    </div>

                    <div id="pageData">
                        <div id="wrapper">
                                <%--rssfeed may be need for some template which need it--%>
                            <c:if test="${ !empty param.mainArea }">
                                <div id="${param.position}">
                                    <div class="spacer">
                                        <template:include page="${param.mainArea}">
                                            <template:param name="rssFeed" value="${param.rssFeed}"/>
                                        </template:include>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                        <c:if test="${ !empty param.areaA }">
                            <div id="areaA">
                                <div class="spacer">
                                    <template:include page="${param.areaA}"/>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${ !empty param.areaB }">
                            <div id="areaB">
                                <div class="spacer">
                                    <template:include page="${param.areaB}"/>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${ !empty param.areaC }">
                            <div id="areaC">
                                <div class="spacer">
                                    <template:include page="${param.areaC}"/>
                                </div>
                            </div>
                        </c:if>
                        <div class="clear"></div>
                    </div>
                    <div>&nbsp;</div>
                    <div id="footer">
                        <%@ include file="common/footer.jspf" %>
                    </div>
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
            </div>
            <div class="clear"></div>
        </div>
    </template:templateBody>
</template:template>