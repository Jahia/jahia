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
<%@ taglib uri="http://www.jahia.org/tags/uiComponentsLib" prefix="ui" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="rss">
    <span class="rssButton">
        <ui:rssButton
                targetURL="${jahia.page.url}?type=${definitionName}&template=%2Fjsp%2Fjahia%2Ftemplates%2Ftest_templates%2Fcontentdef%2Fextensions%2FtechWatchRss.jsp"/>
    </span>
</div>