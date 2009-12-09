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
<%@ include file="common/declarations.jspf" %>
<template:template gwtForGuest="${not empty param.useGWT}">
    <template:templateHead>
        <%@ include file="common/template-head.jspf" %>
        <link rel="stylesheet" type="text/css" href="${jahia.includes.webPath['common/css/web.css']}" media="screen"/>
        <c:if test="${!empty param.opensearch}">
            <s:openSearchLink searchFor="pages"/>
            <s:openSearchLink searchFor="pages" format="rss"/>
            <s:openSearchLink searchFor="files"/>
            <s:openSearchLink searchFor="files" format="rss"/>
        </c:if>
        <utility:applicationResources/>
    </template:templateHead>
    <template:templateBody>
        <div id="header">
            <div id="utilities">
                <div class="content">
                    <a name="pagetop"></a>
                    <span class="breadcrumbs">
                        <fmt:message key='youAreHere'/>
                    </span>
                    <ui:currentPagePath cssClassName="breadcrumbs"/>
                    <ui:langBar display="horizontal" linkDisplay="flag" />
                    <template:include page="common/breadcrumbs.jsp" cache="false"/>
                </div>
            </div>
        </div>
        <div id="pagecontent">
            <div class="content2cols">
                <div id="columnA">
                    <template:include page="common/columnA.jsp"/>
                </div>
                <div id="columnB">
                    <h2><c:out value="${requestScope.currentPage.highLightDiffTitle}"/></h2>

                    <template:include page="modules/maincontent/maincontentDisplay.jsp"/>

                    <c:if test="${ !empty param.workAreaJSP }">
                        <div class="embeddedPart">
                            <jsp:include page='<%=request.getParameter("workAreaJSP") %>' flush='true'/>
                        </div>
                    </c:if>

                    <div>
                        <a class="bottomanchor" href="#pagetop"><fmt:message key='pageTop'/></a>
                    </div>
                </div>

                <br class="clear"/>
            </div>
            <!-- end of content2cols section -->
        </div>
        <!-- end of pagecontent section-->
    </template:templateBody>
</template:template>