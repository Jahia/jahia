<%--


    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%--<query:definition id="blogQuery">
    <query:containerQuery targetContainerListName="blogEntries"/>
    <query:selector nodeTypeName="social_templates:blog" selectorName="entries"/>
    <query:descendantNode selectorName="blogList" path="${currentSite.JCRPath}"/>
    <query:sortBy propertyName="date" order="${queryConstants.ORDER_ASCENDING}"/>
</query:definition>--%>
<%@ include file="../../common/declarations.jspf" %>


<template:containerList name="blogEntries" id="blogEntriesPagination" windowSize="${param.numBlogEntries}"
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
            <query:like propertyName="${queryConstants.KEYWORDS}" metadata="true" value="${param.keyword}"/>
        </c:if>
        <c:if test="${!empty param.user}">
            <query:equalTo propertyName="${queryConstants.CREATOR}" metadata="true" value="${param.user}"/>
        </c:if>
    </query:containerQuery>
    <c:set var="blogType" value="full"/>
    <%@ include file="blog.jspf" %>
</template:containerList>