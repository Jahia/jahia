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

<%@ include file="../../../../../common/declarations.jspf" %>
<c:set var="useGWT" value="${!empty param.useGWT}"/>
<template:module node="${currentNode}" forcedTemplate="${currentResource.resolvedTemplate}" var="content"/>
<template:template gwtForGuest="${useGWT}">
    <template:templateHead>
        <!-- All headers and declarations global meta and css-->
        <%@ include file="../../../../../common/head.jspf" %>
        <utility:applicationResources/>
        <c:if test="${ !empty param.rssFeed }">
            <link rel="alternate" type="application/rss+xml" title="web templates : news"
                  href="${currentPage.url}/${param.rssFeed}"/>
        </c:if>
        <c:if test="${!empty param.opensearch}">
            <s:openSearchLink searchFor="pages"/>
            <s:openSearchLink searchFor="files"/>
        </c:if>
        <%--CSS--%>
        <c:forEach var="css" items="${renderContext.externalLinks.css}">
            <link rel="stylesheet" href="${renderContext.request.contextPath}/${css}" media="screen"/>
        </c:forEach>
    </template:templateHead>
    <template:templateBody gwtScript="${param.gwtScript}">
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
        <div id="bodywrapper">
            <div id="contentArea">
                <div id="toplinks">

                    <div id="languages">
                            <%--<ui:languageSwitchingLinks display="horizontal" linkDisplay="${linkDisplay.isoLocaleCountryCode}" displayLanguageState="true"/>--%>
                    </div>

                    <div id="topshortcuts">
                        <ul>
                            <c:if test="${requestScope.currentRequest.logged}">
                                <li class="topshortcuts-login">
                                    <a class="loginFormTopLogoutShortcuts"
                                       href="<template:composePageURL page="logout"/>"><span><fmt:message
                                            key="web_templates.logout"/></span></a>
                                </li>
                                <li>
                                    <span class="currentUser"><utility:userProperty/></span>
                                </li>
                                <li class="topshortcuts-mysettings">
                                    <a href="${url.base}"><fmt:message key="web_templates.mySettings.title"/></a>
                                </li>
                                <li class="topshortcuts-edit">
                                    <a href="${url.edit}"><fmt:message key="edit"/></a>
                                </li>
                            </c:if>
                            <li class="topshortcuts-print"><a href="#" onclick="javascript:window.print()">
                                <fmt:message key="web_templates.print"/></a>
                            </li>
                            <li class="topshortcuts-typoincrease">
                                <a href="javascript:ts('body',1)"><fmt:message key="web_templates.up"/></a>
                            </li>
                            <li class="topshortcuts-typoreduce">
                                <a href="javascript:ts('body',-1)"><fmt:message key="web_templates.down"/></a>
                            </li>
                            <li class="topshortcuts-home">
                                <a href="${url.base}${rootPage.path}.html"><fmt:message key="web_templates.home"/></a>
                            </li>
                            <li class="topshortcuts-sitemap">
                                <a href="${url.base}${rootPage.path}.sitemap.html"><fmt:message
                                        key="web_templates.sitemap"/></a>
                            </li>
                        </ul>
                    </div>
                    <div class="clear"></div>


                </div>
                <div id="page">
                    <div id="pageHeader">
                        <div id="logotop">
                                <%--<jcr:node var="logo" path="${rootPage.path}/logo"/>--%>
                            <template:module path="${rootPage.path}/logo"/>
                        </div>
                        <h1 class="hide">${currentSite.title} : ${currentPage.title}</h1>


                        <template:module node="${rootPage}" forcedTemplate="nav.navBar" editable="false"/>
                    </div>


                        ${content}
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
            </div>
            <div class="clear"></div>
        </div>
    </template:templateBody>
</template:template>