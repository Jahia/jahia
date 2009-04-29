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
<%@ include file="declarations.jspf" %>
<div id="pageTop"><!--start top-->
    <div id="logotop">
        <div class="name">
            <a href='<template:composePageURL pageID="${requestScope.currentSite.homepageID}"/>'>
                <%--<img class="logotop" src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/logo-top.png'/>" alt="logo" />--%>
                <h1><span>${currentPage.title}</span></h1>
            </a>
        </div>
        <template:include page="modules/introContent.jsp"/>
        <blockquote>&nbsp;</blockquote>
    </div>
</div><!--stop top-->