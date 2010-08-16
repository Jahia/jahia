<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

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
    <template:addResources type="javascript" resources="http://maps.google.com/maps/api/js?sensor=false&amp;language=${currentResource.locale.language}"/>
    <template:addResources type="javascript" resources="jquery.min.js"/>
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
    <template:addResources type="inlinejavascript">
        $(document).ready(function() {
            $("#map-${currentNode.identifier}").googleMaps({
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
    </template:addResources>
    
    <div>
        <c:if test="${not empty props['jcr:title']}">
            <h3>${fn:escapeXml(props['jcr:title'])}</h3>
        </c:if>
        <div id="map-${currentNode.identifier}" style="width:${props['j:width']}px; height:${props['j:height']}px"></div>
    </div>
</c:if>
<template:linker property="j:bindedComponent" />