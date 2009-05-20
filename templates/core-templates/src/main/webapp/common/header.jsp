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

<%@ include file="declarations.jspf" %>
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
<div id="identification">
    <div class="content"><h1>${requestScope.currentSite.templatePackageName}</h1></div>
</div>
<div id="mainmenu">
    <div class="content">
        <div class="left">
            <ui:navigationMenu cssClassName="menu" kind="topTabs" labelKey="pages.add" requiredTitle="true"/>
        </div>
        <div class="right">
            <template:include page="modules/links/imageLinksDisplay.jsp">
                <template:param name="cssClassName" value="shortcuts"/>
            </template:include>
        </div>
    </div>
</div>