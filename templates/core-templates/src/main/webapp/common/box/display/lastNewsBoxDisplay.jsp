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

<%@ include file="../../declarations.jspf" %>
<template:containerList name="${boxID}_lastNewsContainer" id="lastNews"
                       actionMenuNamePostFix="lastnewss" actionMenuNameLabelKey="lastnewss.add">
    <template:container id="lastNewsContainer" cache="off" actionMenuNamePostFix="lastnews"
                       actionMenuNameLabelKey="lastnews.update">
        <template:field name="maxNews" var="maxNews" display="false"/>
        <template:containerList name="news${param.id}" windowSize="${not empty maxNews ? maxNews.integer : 10}" id="newsList" displayActionMenu="false">
            <query:containerQuery>
                <query:selector nodeTypeName="jnt:newsContainer" selectorName="newsList"/>
                <query:descendantNode selectorName="newsList" path="${currentSite.JCRPath}"/>
                <query:sortBy propertyName="newsDate" order="${queryConstants.ORDER_DESCENDING}"/>
            </query:containerQuery>
            <%@ include file="../../../modules/news/newsDisplay.jspf" %>
        </template:containerList>
    </template:container>
</template:containerList>
