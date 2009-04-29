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
<%--<query:definition id="blogQuery">
    <query:containerQuery targetContainerListName="blogEntries"/>
    <query:selector nodeTypeName="social_templates:blog" selectorName="entries"/>
    <query:descendantNode selectorName="blogList" path="${currentSite.JCRPath}"/>
    <query:sortBy propertyName="date" order="${queryConstants.ORDER_ASCENDING}"/>
</query:definition>--%>
<%@ include file="../../common/declarations.jspf" %>


<template:containerList name="blogEntries" id="blogEntriesPagination" windowSize="${blogPrefsMaxEntries.integer}"
                        actionMenuNameLabelKey="article" actionMenuNamePostFix="manage">
    <%--query search --%>
    <query:containerQuery queryBeanID="blogQuery">
        <query:selector nodeTypeName="social_templates:blogEntry" selectorName="blogSelector"/>
        <query:childNode selectorName="blogSelector" path="${blogEntriesPagination.JCRPath}"/>
        <query:sortBy propertyName="date" order="${queryConstants.ORDER_DESCENDING}"/>
        <c:if test="${!(empty param.year || empty param.month)}">
            <fmt:parseDate var="startDate" pattern="dd/MM/yyyy" value="01/${param.month}/${param.year}"/>

            <utility:dateCalc value="${startDate}" var="beginMonth"
                              days="${utilConstants.TO_MIN}" hours="${utilConstants.TO_MIN}"
                              minutes="${utilConstants.TO_MIN}" seconds="${utilConstants.TO_MIN}"
                              milliseconds="${utilConstants.TO_MIN}"/>
            <utility:dateCalc value="${startDate}" var="endMonth"
                              days="${utilConstants.TO_MAX}" hours="${utilConstants.TO_MAX}"
                              minutes="${utilConstants.TO_MAX}" seconds="${utilConstants.TO_MAX}"
                              milliseconds="${utilConstants.TO_MAX}"/>
            <query:greaterThanOrEqualTo numberValue="false" propertyName="date" value="${beginMonth.time}"/>
            <query:lessThanOrEqualTo numberValue="false" propertyName="date" value="${endMonth.time}"/>
        </c:if>
        <c:if test="${!empty param.search}">
            <query:fullTextSearch searchExpression="${param.search}"/>
        </c:if>
        <c:if test="${!empty param.category}">
            <query:equalTo propertyName="${queryConstants.CATEGORY_LINKS}" value="${param.category}"/>
        </c:if>
        <c:if test="${!empty param.keyword}">
            <query:equalTo propertyName="${queryConstants.KEYWORDS}" metadata="true" value="${param.keyword}"/>
        </c:if>
        <c:if test="${!empty param.user}">
            <query:equalTo propertyName="${queryConstants.CREATOR}" metadata="true" value="${param.user}"/>
        </c:if>
    </query:containerQuery>
    <c:set var="blogType" value="full"/>
    <%@ include file="blog.jspf" %>
</template:containerList>