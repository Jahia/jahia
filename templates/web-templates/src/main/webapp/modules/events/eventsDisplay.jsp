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

<%@ include file="../../common/declarations.jspf" %>

    <div class="box4 ">
        <div class="box4-topright"></div>
        <div class="box4-topleft"></div>
        <h3 class="box4-header"><span class="publicationTitle">
        <c:if test="${!empty param.startDate}">
            <fmt:message key='statictitle.lastevents.from'/>&nbsp;${param.startDate}    
        </c:if>
        <c:if test="${empty param.startDate}">
            <fmt:message key='statictitle.lastevents'/>
        </c:if>
        </span></h3>
        <template:jahiaPageForm name="eventPageForm" method="get">
            <c:set value="" var="startDateSelected"/>
            <c:set value="" var="locationSelected"/>
            <c:if test="${param.eventsSort == 'startDate'}">
                <c:set value="selected" var="startDateSelected"/>
            </c:if>
            <c:if test="${param.eventsSort == 'location'}">
                <c:set value="selected" var="locationSelected"/>
            </c:if>

            <select class="eventsSort" name="eventsSort" onchange="document.eventPageForm.submit();">
                <option value="startDate" ${startDateSelected}><fmt:message key='sortDate'/></option>
                <option value="location" ${locationSelected}><fmt:message key='sortLocation'/></option>
            </select>

            <span class="hidden"><utility:dropDownFromBundle bundleName="resources.eventsType"/></span>
        </template:jahiaPageForm>

            <div class="box4-bottomright"></div>
            <div class="box4-bottomleft"></div>
            <div class="clear"> </div>
    </div>
    <div class="clear"> </div>

<c:choose>
    <c:when test="${!empty param.eventsSort}">
        <c:set var="sortBy" value="${param.eventsSort}"/>
    </c:when>
    <c:otherwise>
        <c:set var="sortBy" value="startDate"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${sortBy == 'location'}">
        <c:set var="order" value="${queryConstants.ORDER_ASCENDING}"/>
    </c:when>
    <c:otherwise>
        <c:set var="order" value="${queryConstants.ORDER_DESCENDING}"/>
    </c:otherwise>        
</c:choose>

<query:createFacetFilter facetName="categoryFacet" 
    propertyName="defaultCategory" facetBeanId="categoryFacet"/>
<query:createFacetFilter facetName="eventTypeFacet" targetContainerListName="events" 
    propertyName="eventsType" facetBeanId="eventTypeFacet" valueTitle="Unknown"/>

<c:choose>
<c:when test="${!empty param.startDate}">
    <fmt:parseDate var="startDate" pattern="dd/MM/yyyy" value="${param.startDate}"/>
</c:when>
<c:otherwise>
    <jsp:useBean id="startDate" class="java.util.Date" />
</c:otherwise>
</c:choose>
<c:forTokens var="count" items="0,1,2,3" delims=",">
    <utility:dateCalc value="${startDate}" var="beginMonth" 
        months="${count}" days="${utilConstants.TO_MIN}" hours="${utilConstants.TO_MIN}" 
        minutes="${utilConstants.TO_MIN}" seconds="${utilConstants.TO_MIN}" milliseconds="${utilConstants.TO_MIN}"/>                       
    <utility:dateCalc value="${startDate}" var="endMonth" 
        months="${count}" days="${utilConstants.TO_MAX}" hours="${utilConstants.TO_MAX}" 
        minutes="${utilConstants.TO_MAX}" seconds="${utilConstants.TO_MAX}" milliseconds="${utilConstants.TO_MAX}"/>
    <fmt:formatDate value="${beginMonth}" pattern="MMM yyyy" var="dateFacetValueTitle"/>
    <c:set var="facetValues" value="${facetValues},${dateFacetValueTitle}"/>
    <query:createFacetFilter facetName="eventDateFacet" targetContainerListName="events"
        valueTitle="${dateFacetValueTitle}" facetBeanId="eventDateFacet">
        <query:selector nodeTypeName="web_templates:eventContainer" selectorName="eventsSelector"/>
        <query:childNode selectorName="eventsSelector" path="${eventsContainer.JCRPath}"/>

        <query:greaterThanOrEqualTo numberValue="false" propertyName="endDate" value="${beginMonth.time}"/>
        <query:lessThanOrEqualTo numberValue="false" propertyName="startDate" value="${endMonth.time}"/>        
    </query:createFacetFilter>
</c:forTokens>  
   

<template:containerList name="events" id="eventsContainer" actionMenuNamePostFix="events"
                        actionMenuNameLabelKey="events">
    <query:containerQuery queryBeanID="eventsQuery">
        <query:selector nodeTypeName="web_templates:eventContainer" selectorName="eventsSelector"/>
        <query:childNode selectorName="eventsSelector" path="${eventsContainer.JCRPath}"/>
        <query:sortBy propertyName="${sortBy}" order="${order}"/>
        <query:setProperty name="${queryConstants.FACET_FILTER_QUERY_PARAM_NAME}" value="filter"/>
        <c:if test="${!empty param.startDate}">
            <utility:dateUtil currentDate="${param.startDate}" datePattern="dd/MM/yyyy" valueID="today" hours="0"
                              minutes="0"
                              seconds="0"/>
            <query:greaterThanOrEqualTo numberValue="false" propertyName="startDate" value="${today.time}"/>
        </c:if>    
    </query:containerQuery>
    <%@ include file="eventsDisplay.jspf" %>
</template:containerList>

<query:getAppliedFacetFilters filterQueryParamName="filter"/>
<query:getHitsPerFacetValue mainQueryBeanId="eventsQuery" facetBeanId="categoryFacet" filterQueryParamName="filter"/>
<br/>Event types:<br/>
<query:getHitsPerFacetValue mainQueryBeanId="eventsQuery" facetBeanId="eventTypeFacet" filterQueryParamName="filter"/>
<br/>Next 4 months:<br/>
<c:forTokens var="facetValue" items="${facetValues}" delims=",">
    <query:getHitsPerFacetValue mainQueryBeanId="eventsQuery" facetBeanId="eventDateFacet" facetValueName="${facetValue}" filterQueryParamName="filter"/>
</c:forTokens>
