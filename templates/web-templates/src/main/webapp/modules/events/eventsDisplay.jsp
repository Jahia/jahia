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
<%@ include file="../../common/declarations.jspf" %>

    <div class="box4 ">
        <div class="box4-topright"></div>
        <div class="box4-topleft"></div>
        <c:choose>
        <c:when test="${!empty param.startDate}">
            <fmt:parseDate var="startDate" pattern="dd/MM/yyyy" value="${param.startDate}"/>
        </c:when>
        <c:otherwise>
            <jsp:useBean id="startDate" class="java.util.Date" />
        </c:otherwise>
        </c:choose>
        <h3 class="box4-header"><span class="publicationTitle">
        <c:if test="${!empty param.startDate}">
            <fmt:message key='statictitle.lastevents.from'/>&nbsp;<fmt:formatDate value="${startDate}" dateStyle="long" type="date"/>  
        </c:if>
        <c:if test="${empty param.startDate}">
            <fmt:message key='statictitle.lastevents'/>
        </c:if>
        </span></h3>
        <template:jahiaPageForm name="eventPageForm" method="post">
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
            <input type="hidden" name="startDate" value="${param.startDate}"/>
            <utility:dropDownFromBundle bundleName="resources.eventsType" var="eventsList"/>
            <select class="eventsSort" name="eventsTypeFilter" onchange="document.eventPageForm.submit();">
                <option value=""><fmt:message key="events.form.types.all"/> </option>
            <c:set value="" var="selected"/>
            <c:forEach var="event" items="${eventsList}">
                <c:if test="${param.eventsTypeFilter == event}">
                    <c:set value="selected" var="selected"/>
                </c:if>
                <option value="${event}" ${selected}>${event}</option>
                <c:set value="" var="selected"/>
            </c:forEach>
        </select>
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

<query:createFacetFilter facetName="defaultCategoryFacet" 
    propertyName="defaultCategory" facetBeanId="defaultCategoryFacet" facetValueBeanId="categoryFacetValue"/>
<query:createFacetFilter facetName="eventTypeFacet" targetContainerListName="events" 
    propertyName="eventsType" facetBeanId="eventTypeFacet" valueTitle="Unknown"/>


<c:forTokens var="count" items="0,1,2,3" delims=",">
    <utility:dateCalc value="${startDate}" var="beginMonth" 
        months="${count}" days="${utilConstants.TO_MIN}" hours="${utilConstants.TO_MIN}" 
        minutes="${utilConstants.TO_MIN}" seconds="${utilConstants.TO_MIN}" milliseconds="${utilConstants.TO_MIN}"/>                       
    <utility:dateCalc value="${startDate}" var="endMonth" 
        months="${count}" days="${utilConstants.TO_MAX}" hours="${utilConstants.TO_MAX}" 
        minutes="${utilConstants.TO_MAX}" seconds="${utilConstants.TO_MAX}" milliseconds="${utilConstants.TO_MAX}"/>
    <query:createFacetFilter facetName="eventDateFacet" targetContainerListName="events"
        valueTitle="{0,date,MMM yyyy}" facetBeanId="eventDateFacet" facetValueBeanId="eventDateFacetValue">
        <query:propertyValue value="${beginMonth}"/>
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
        <c:if test="${!empty param.eventsTypeFilter}">
            <query:equalTo propertyName="eventsType" value="${param.eventsTypeFilter}"/>
        </c:if>
        <c:if test="${!empty param.startDate}">
            <utility:dateUtil currentDate="${param.startDate}" datePattern="dd/MM/yyyy" var="today" hours="0"
                              minutes="0"
                              seconds="0"/>
            <query:greaterThanOrEqualTo numberValue="false" propertyName="startDate" value="${today.time}"/>
        </c:if>    
    </query:containerQuery>
    <%@ include file="eventsDisplay.jspf" %>
</template:containerList>


