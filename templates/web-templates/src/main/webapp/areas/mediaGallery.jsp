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
        <template:include page="modules/mediaGallery.jsp"/>
<!--start jobsSearchForm -->
		<ui:thumbView path="${picts}" cssClassName="thumbView"/>