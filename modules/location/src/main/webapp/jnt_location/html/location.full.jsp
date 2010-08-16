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