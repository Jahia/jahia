<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../../../common/declarations.jspf" %>
<c:set var="useGWT" value="${!empty param.useGWT}"/>
<template:template gwtForGuest="${useGWT}">
    <template:templateHead>
        <!-- All headers and declarations global meta and css-->
        <%@ include file="../../../common/head.jspf" %>
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
                <c:forEach items="${currentNode.ancestors}" var="page">
                    <c:if test="${jcr:isNodeType(page, 'jnt:page')}">
                        <c:if test="${rootPage == null}">
                            <c:set var="rootPage" value="${page}"/>
                        </c:if>
                    </c:if>
                </c:forEach>
                <c:if test="${jcr:isNodeType(currentNode, 'jnt:page')}">
                    <c:if test="${rootPage == null}">
                        <c:set var="rootPage" value="${currentNode}"/>
                    </c:if>
                </c:if>
                </div>
                <div id="page">
                    <div id="pageHeader">
                        <div id="logotop">
                        <jcr:node var="logo" path="${rootPage.path}/logo"/>
                        <template:module node="${logo}"/>
                        </div>
                        <h1 class="hide">${currentSite.title} : ${currentPage.title}</h1>
                        

                        <template:module node="${rootPage}" template="nav.navBar" />
                    </div>

                    <div id="pageData">
                        <div id="wrapper">
                                <%--rssfeed may be need for some template which need it--%>
                            <c:if test="${ !empty param.mainArea }">
                                <div id="${param.position}">
                                    <div class="spacer">
                                        <jsp:include page="${param.mainArea}"/>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                        <c:if test="${ !empty param.areaA }">
                            <div id="areaA">
                                <div class="spacer">
<jsp:include page="${param.areaA}"/>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${ !empty param.areaB }">
                            <div id="areaB">
                                <div class="spacer">
<jsp:include page="${param.areaB}"/>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${ !empty param.areaC }">
                            <div id="areaC">
                                <div class="spacer">
<jsp:include page="${param.areaC}"/>
                                </div>
                            </div>
                        </c:if>
                        <div class="clear"></div>
                    </div>
                    <div>&nbsp;</div>
                    <div id="footer">
template:include file="../../../../common/footer.jspf"
                    </div>
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
            </div>
            <div class="clear"></div>
        </div>
    </template:templateBody>
</template:template>