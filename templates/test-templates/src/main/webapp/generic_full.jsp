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
<%@ include file="common/declarations.jspf"%>
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
    <template:templateBody gwtScript="${param.gwtScript}">
        <div id="header">
          <div id="utilities">
            <div class="content">
              <a name="pagetop"></a>
              <span class="breadcrumbs">
                <fmt:message key='youAreHere'/>:
              </span>
              <ui:currentPagePath cssClassName="breadcrumbs"/>
              <ui:languageSwitchingLinks display="horizontal" linkDisplay="flag" displayLanguageState="true"/>
            </div>
          </div>
        </div>
        <div id="pagecontent">
            <div class="content1cols">
                    <div id="columnB">
                        <h2><c:out value="${requestScope.currentPage.highLightDiffTitle}"/></h2>

                        <template:include page="common/maincontent/maincontentDisplay.jsp"/>

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