<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="location.css"/>
<c:set var="props" value="${currentNode.propertiesAsString}"/>

<div class="location">
    <c:if test="${not empty props['jcr:title']}">
        <h3>${fn:escapeXml(props['jcr:title'])}</h3>
    </c:if>

    <c:if test="${not empty props['j:street']}">
        <p class="location-item">${fn:escapeXml(props['j:street'])}</p>
    </c:if>
    <c:if test="${not empty props['j:zipCode'] || not empty props['j:town']}">
        <p class="location-item">
            <c:if test="${not empty props['j:zipCode']}">
                ${fn:escapeXml(props['j:zipCode'])}&nbsp;
            </c:if>
            ${not empty props['j:town'] ? fn:escapeXml(props['j:town']) : ''}
        </p>
    </c:if>
    <p class="location-item">
        <jcr:nodePropertyRenderer name="j:country" node="${currentNode}" renderer="country" var="country"/>${fn:escapeXml(country.displayName)}
    </p>
</div>