<%@ tag body-content="empty" description="Renders the label of a facet." %>
<%@ attribute name="display" required="false" type="java.lang.Boolean" description="Should we display the label or just return it in the parameter set by attribute var."%>
<%@ attribute name="var" required="false" type="java.lang.String" description="Request scoped attribute name for setting the label."%>
<%@ attribute name="currentFacetField" required="false" type="org.apache.solr.client.solrj.response.FacetField" description="Either the FacetField for the current facet." %>
<%@ attribute name="currentActiveFacet" required="false" type="java.lang.Object" description="Alternatively the Map.Entry with KeyValue from the active facet filters variable." %>
<%@ attribute name="facetLabels" required="true" type="java.util.Map" description="Mapping between facet name and label." %>
<%@ variable name-given="facetLabel" scope="AT_END"%>
<%@ variable name-given="mappedFacetLabel" scope="AT_END"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>

<c:set var="display" value="${functions:default(display, true)}"/>

<c:choose>
    <c:when test="${not empty currentFacetField}">
        <c:set var="currentFacetName" value="${currentFacetField.name}"/>
        <c:if test="${not empty facetLabels and (not empty facetLabels[currentFacetName])}">
            <c:set var="mappedFacetLabel" value="${facetLabels[currentFacetName]}"/>        
        </c:if>        
    </c:when>
    <c:otherwise>
        <c:set var="currentFacetName" value="${currentActiveFacet != null ? currentActiveFacet.key : ''}"/>
        <c:if test="${not empty facetLabels}">
            <c:forEach items="${facetLabels}" var="currentFacetLabel">
                <c:if test="${empty mappedFacetLabel and fn:endsWith(currentFacetName, currentFacetLabel.key)}">
                    <c:set var="mappedFacetLabel" value="${currentFacetLabel.value}"/>
                </c:if>
            </c:forEach>
        </c:if>        
    </c:otherwise>
</c:choose>  

<c:set var="facetLabel" value="${not empty mappedFacetLabel ? mappedFacetLabel : (not empty currentFacetField ? currentFacetName : '')}"/>
<c:if test="${display}">
    ${facetLabel}
</c:if>