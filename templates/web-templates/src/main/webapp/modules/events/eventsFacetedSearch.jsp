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

<div class="box2 box2-style1">
    <div class="box2-topright"> </div><div class="box2-topleft"> </div>
    <h3 class="box2-header"><span><fmt:message key="statictitle.eventsFacetedSearch"/></span></h3>

  <div class="box2-text">
      <p><fmt:message key="statictitle.eventsFacetedWarning"/></p>
   <query:getAppliedFacetFilters filterQueryParamName="filter" appliedFacetsId="appliedFacets"/>
<c:if test='${!query:isFacetApplied(defaultCategoryFacet, appliedFacets)}'>
    <h4>Categories:</h4>
    <p><query:getHitsPerFacetValue mainQueryBeanId="eventsQuery" facetBeanId="defaultCategoryFacet" facetValueBeanId="categoryFacetValue" filterQueryParamName="filter"/></p>
</c:if>
<c:if test='${!query:isFacetApplied(eventTypeFacet, appliedFacets)}'>
    <h4>Event types:</h4>
    <p><query:getHitsPerFacetValue mainQueryBeanId="eventsQuery" facetBeanId="eventTypeFacet" filterQueryParamName="filter"/></p>
</c:if>
<c:if test='${!query:isFacetApplied(eventDateFacet, appliedFacets)}'>
    <h4>Next 4 months:</h4>
    <p><query:getHitsPerFacetValue mainQueryBeanId="eventsQuery" facetBeanId="eventDateFacet" facetValueBeanId="eventDateFacetValue" filterQueryParamName="filter"/></p>
</c:if>   
  </div>
    <div class="box2-bottomright"></div>
    <div class="box2-bottomleft"></div>
<div class="clear"> </div></div>