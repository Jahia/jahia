<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<c:set var="bindedComponent" value="${ui:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent && jcr:isNodeType(bindedComponent, 'jmix:geotagged,jmix:locationAware,jnt:location')}">
    <c:set var="props" value="${currentNode.propertiesAsString}"/>
    <c:set var="targetProps" value="${bindedComponent.propertiesAsString}"/>
    <div class="location-static-map">
        <c:if test="${not empty props['jcr:title']}">
            <h3>${fn:escapeXml(props['jcr:title'])}</h3>
        </c:if>
    
        <c:url var="mapUrl" value="http://maps.google.com/maps/api/staticmap">
            <c:param name="sensor" value="false"/>
            <c:if test="${not empty props['j:zoom'] && props['j:zoom'] != 'auto'}">
                <c:param name="zoom" value="${props['j:zoom']}"/>
            </c:if>
            <c:param name="size" value="${props['j:width']}x${props['j:height']}"/>
            <c:if test="${not empty props['j:mapType']}">
                <c:param name="maptype" value="${props['j:mapType']}"/>
            </c:if>
            <c:param name="language" value="${currentResource.locale.language}"/>
            <c:choose>
                <c:when test="${not empty targetProps['j:latitude'] && not empty targetProps['j:longitude']}">
                    <c:set var="location" value="${targetProps['j:latitude']},${targetProps['j:longitude']}"/>
                </c:when>
                <c:otherwise>
                    <c:set var="location" value="${targetProps['j:street']}"/>
                    <c:set var="location" value="${location}${not empty location ? ',' : ''}${targetProps['j:zipCode']}"/>
                    <c:set var="location" value="${location}${not empty location ? ',' : ''}${targetProps['j:town']}"/>
                    <jcr:nodePropertyRenderer name="j:country" node="${bindedComponent}" renderer="country" var="country"/>
                    <c:set var="location" value="${location}${not empty location ? ',' : ''}${country.displayName}"/>
                </c:otherwise>
            </c:choose>
            <c:param name="imagetype" value="${props['j:imageType']}"/>
            <c:set var="markers" value="${location}"/>
            <c:if test="${not empty props['j:markerColor'] && props['j:markerColor'] != 'normal'}">
                <c:set var="markers" value="color:${props['j:markerColor']}|${markers}"/>
            </c:if>
            <c:if test="${not empty props['j:markerSize'] && props['j:markerSize'] != 'normal'}">
                <c:set var="markers" value="size:${props['j:markerSize']}|${markers}"/>
            </c:if>
            <c:param name="markers" value="${markers}"/>
        </c:url>
        
        <p class="location-static-map">
            <c:set var="mapTitle" value="${fn:escapeXml(fn:replace(location, ',', ', '))}"/>
            <img src="${fn:escapeXml(mapUrl)}" title="${mapTitle}" alt="${mapTitle}"/>
        </p>
    </div>
</c:if>
