<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="location.css"/>
<c:set var="props" value="${currentNode.propertiesAsString}"/>

<div class="location">
    <h3>${fn:escapeXml(props['jcr:title'])}</h3>

    <p class="location-item"><span class="location-label"><fmt:message key="jmix_locationAware.j_street"/>:</span>
        <span class="location-value">${fn:escapeXml(props['j:street'])}</span>
    </p>

    <p class="location-item"><span class="location-label"><fmt:message key="jmix_locationAware.j_zipCode"/>:</span>
        <span class="location-value">${fn:escapeXml(props['j:zipCode'])}</span>
    </p>
    <p class="location-item"><span class="location-label"><fmt:message key="jmix_locationAware.j_town"/>:</span>
        <span class="location-value">${fn:escapeXml(props['j:town'])}</span>
    </p>
    <p class="location-item"><span class="location-label"><fmt:message key="jmix_locationAware.j_country"/>:</span>
        <span class="location-value"><jcr:nodePropertyRenderer name="j:country" node="${currentNode}" renderer="country" var="country"/>${fn:escapeXml(country.displayName)}</span>
    </p>
    <p class="location-item"><span class="location-label"><fmt:message key="jmix_geotagged.j_latitude"/>:</span>
        <span class="location-value">${fn:escapeXml(props['j:latitude'])}</span>
    </p>
    <p class="location-item"><span class="location-label"><fmt:message key="jmix_geotagged.j_longitude"/>:</span>
        <span class="location-value">${fn:escapeXml(props['j:longitude'])}</span>
    </p>
</div>