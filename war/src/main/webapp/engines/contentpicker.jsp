<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/functions" prefix="functions" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<c:set var="config" value="${functions:default(param.type, 'filepicker')}"/>
<html>
	<head>
        <meta http-equiv="X-UA-Compatible" content="IE=8"/>
        <meta name="robots" content="noindex, nofollow"/>
		<title><fmt:message key="org.jahia.admin.sitepermissions.permission.engines.importexport.ManageContentPicker.label"/></title>
        <internal:gwtGenerateDictionary/>
		<internal:gwtInit locale="${param.lang}" uilocale="${param.uilang}" />
		<internal:gwtImport module="org.jahia.ajax.gwt.module.contentpicker.ContentPicker" />
        <c:if test="${config == 'filepicker' || config == 'imagepicker'}">
            <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/modules/assets/css/jquery.Jcrop.css"/>
            <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.js'/>"></script>
            <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.Jcrop.js'/>"></script>
        </c:if>
	</head>
	<body>
        <internal:contentPicker conf="${fn:escapeXml(config)}" mimeTypes="${fn:escapeXml(param.mime)}" jahiaServletPath="/cms" filesServletPath="/files" jahiaContextPath="${pageContext.request.contextPath}"  callback="${fn:escapeXml(param.CKEditorFuncNum)}"/>    
	</body>
</html>