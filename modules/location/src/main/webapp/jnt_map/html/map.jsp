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
    <c:if test="${!renderContext.editMode}">
	    <c:set var="targetProps" value="${bindedComponent.propertiesAsString}"/>
	    <template:addResources type="javascript" resources="http://maps.google.com/maps/api/js?sensor=false&amp;language=${currentResource.locale.language}"/>
	    <template:addResources type="javascript" resources="jquery.js"/>
	    <template:addResources type="javascript" resources="jquery.jahia-googlemaps.js"/>
	
	    <c:choose>
	        <c:when test="${not empty targetProps['j:latitude'] && not empty targetProps['j:longitude']}">
	            <c:set var="location" value="${targetProps['j:latitude']},${targetProps['j:longitude']}" />
	        </c:when>
	        <c:otherwise>
	            <c:set var="location" value="${targetProps['j:street']}" />
	            <c:set var="location" value="${location}${not empty location ? ', ' : ''}${targetProps['j:zipCode']}" />
	            <c:set var="location" value="${location}${not empty location ? ', ' : ''}${targetProps['j:town']}" />
	            <jcr:nodePropertyRenderer name="j:country" node="${bindedComponent}" renderer="country" var="country" />
	            <c:set var="location" value="${location}${not empty location ? ', ' : ''}${country.displayName}" />
	        </c:otherwise>
	    </c:choose>
	    <template:addResources>
	        <script type="text/javascript">
	            $(document).ready(function() {
	                $("#map-${currentNode.identifier}").googleMaps({
	                	mapTypeId: google.maps.MapTypeId.${fn:toUpperCase(props['j:mapType'])},
	                    markers:[{
	                        <c:if test="${not empty targetProps['j:latitude']}">
	                        latitude: '${targetProps['j:latitude']}',
	                        longitude: '${targetProps['j:longitude']}',
	                        </c:if>
	                        <c:if test="${empty targetProps['j:latitude']}">
	                        address: '${functions:escapeJavaScript(location)}',
	                        </c:if>
	                        icon: '${functions:escapeJavaScript(currentNode.properties['j:markerImage'].node.url)}',
	                        <c:if test="${not empty targetProps['jcr:title']}">
	                        title: '${functions:escapeJavaScript(targetProps['jcr:title'])}',
	                        </c:if>
	                        info: ""
	                                <c:if test="${not empty targetProps['jcr:title']}">
	                                + "<strong>${functions:escapeJavaScript(targetProps['jcr:title'])}</strong>"
	                                </c:if>
	                                <c:if test="${not empty targetProps['j:street']}">
	                                + "<br/>${functions:escapeJavaScript(targetProps['j:street'])}"
	                                </c:if>
	                                <c:if test="${not empty targetProps['j:zipCode'] || not empty targetProps['j:town']}">
	                                + "<br/>"
	                                <c:if test="${not empty targetProps['j:zipCode']}">
	                                + "${functions:escapeJavaScript(targetProps['j:zipCode'])}&nbsp;"
	                                </c:if>
	                                + "${not empty targetProps['j:town'] ? functions:escapeJavaScript(targetProps['j:town']) : ''}"
	                                </c:if>
	                                <jcr:nodePropertyRenderer name="j:country" node="${currentNode}" renderer="country" var="country"/>
	                                +"<br/>${functions:escapeJavaScript(country.displayName)}"
	                    }]
	                });
	            });
	        </script>
	    </template:addResources>
	</c:if>

    <div>
        <c:if test="${not empty props['jcr:title']}">
            <h3>${fn:escapeXml(props['jcr:title'])}</h3>
        </c:if>
        <div id="map-${currentNode.identifier}" style="width:${props['j:width']}px; height:${props['j:height']}px">
        	<c:if test="${renderContext.editMode}">
        		<p><fmt:message key="jnt_map.noPreviewInEditMode"/></p>
        	</c:if>
        </div>
    </div>
</c:if>
