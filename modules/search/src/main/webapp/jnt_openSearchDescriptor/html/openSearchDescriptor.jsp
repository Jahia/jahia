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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<c:set var="descriptorUrl" value="${url.server}${url.templateTypes['xml']}"/>
<c:set var="title" value="${functions:default(currentNode.propertiesAsString['jcr:title'], '')}"/>
<template:addResources type="opensearch" resources="${descriptorUrl}" title="${title}"/>
<c:if test="${empty requestScope['org.jahia.modules.search.addOpenSearch']}">
<template:addResources type="inlinejavascript">
function addOpenSearch(provider) {
  if ((typeof window.external == "object") && ((typeof window.external.AddSearchProvider == "unknown") || (typeof window.external.AddSearchProvider == "function"))) {
    window.external.AddSearchProvider(provider);
  } else {
    alert("You will need a browser which supports OpenSearch to install this plugin.");
  }
}
</template:addResources>
<c:set var="org.jahia.modules.search.addOpenSearch" value="true" scope="request"/>
</c:if>
<a href="#opensearch" onclick="addOpenSearch('${descriptorUrl}'); return false;" title="${fn:escapeXml(title)}"><img src="${url.currentModule}/icons/jnt_openSearchDescriptor.png" height="16" width="16" alt=" "/>&nbsp;${fn:escapeXml(title)}</a>