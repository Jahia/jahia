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
<%@ page import="org.jahia.ajax.gwt.client.util.Constants" %>
<%@ include file="../../declarations.jspf" %>
<template:containerList name="rssContainer" id="rss">
        <template:container id="rssContainer">
            <template:field name="url" display="false" var="url"/>
            <template:field name="entriesCount" display="false" var="entriesCount"/>
            <div id="<%=new StringBuilder(String.valueOf(System.currentTimeMillis() % 3600000)).append("_").append(Math.random()).toString()%>" jahiatype="rss" url="${url}"
                    entriesCount="${not empty entriesCount ? entriesCount.integer : entriesCount}" >
            </div>
        </template:container>
</template:containerList>
