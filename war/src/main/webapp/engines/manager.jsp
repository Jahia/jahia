<%@ page contentType="text/html;charset=UTF-8" language="java" 
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

%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility"%>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<c:set var="cfg" value="${functions:default(param.conf, 'repositoryexplorer')}"/>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=8"/>
    <meta name="robots" content="noindex, nofollow"/>
    <fmt:message key="label.${fn:escapeXml(cfg)}" var="title"/>
    <title>${fn:escapeXml(title)}</title>
    <internal:gwtGenerateDictionary/>
    <internal:gwtInit/>
    <internal:gwtImport module="org.jahia.ajax.gwt.module.contentmanager.ContentManager"/>
    <c:if test="${cfg == 'filemanager' || cfg == 'repositoryexplorer' || cfg == 'editorialcontentmanager'}">
        <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/modules/assets/css/jquery.Jcrop.css"/>
        <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.js'/>"></script>
        <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.Jcrop.js'/>"></script>
    </c:if>
</head>
<body onload="window.focus()">
<internal:contentManager conf="${fn:escapeXml(cfg)}" selectedPaths="${fn:escapeXml(param.selectedPaths)}"/>
</body>
</html>