<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="location.css"/>


<div class="location">
    <h3>${currentNode.properties.title.string}</h3>

    <p class="location-item"><span class="location-label"><fmt:message key="jahia.location.street"/> : </span>
        <span class="location-value">${currentNode.properties.street.string}</span>
    </p>

    <p class="location-item"><span class="location-label"><fmt:message key="jahia.location.zipCode"/> : </span>
        <span class="location-value">${currentNode.properties.zipCode.string}</span>
    </p>
    <p class="location-item"><span class="location-label"><fmt:message key="jahia.location.town"/> : </span>
        <span class="location-value">${currentNode.properties.town.string}</span>
    </p>
    <p class="location-item"><span class="location-label"><fmt:message key="jahia.location.country"/> : </span>
        <span class="location-value">${currentNode.properties.country.string}</span>
    </p>
    <p class="location-item"><span class="location-label"><fmt:message key="jahia.location.latitude"/> : </span>
        <span class="location-value">${currentNode.properties.latitude.string}</span>
    </p>
    <p class="location-item"><span class="location-label"><fmt:message key="jahia.location.longitude"/> : </span>
        <span class="location-value">${currentNode.properties.longitude.string}</span>
    </p>
</div>