<%@ page import="org.jahia.bin.Jahia" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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

--%><%@ page import="java.util.*,javax.servlet.http.HttpServletResponse" contentType="text/html;charset=UTF-8" language="java" %><%
Map<String, String> configToPermissionMapping = new HashMap<String, String>();
configToPermissionMapping.put("categorymanager", "category-manager");
configToPermissionMapping.put("contentmanager", "content-manager");
configToPermissionMapping.put("filemanager", "file-manager");
configToPermissionMapping.put("mashupmanager", "mashup-manager");
configToPermissionMapping.put("remotepublicationmanager", "remote-publication-manager");
configToPermissionMapping.put("sitemanager", "site-manager");
configToPermissionMapping.put("tagmanager", "tag-manager");
configToPermissionMapping.put("workflowmanager", "workflow-manager");
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" 
%><%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %><%
    if (request.getParameter("site") != null && request.getParameter("conf") != null && configToPermissionMapping.containsKey(request.getParameter("conf"))) {
        pageContext.setAttribute("permission", "managers/" + configToPermissionMapping.get(request.getParameter("conf")));
        %>
        <jcr:node var="siteNode" uuid="${param.site}"/>
        <c:if test="${not empty siteNode && !functions:isUserPermittedForSite(permission, siteNode.siteKey)}">
        <% response.sendError(HttpServletResponse.SC_FORBIDDEN);%>
        </c:if>
        <%
    }
%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title><internal:message key="label.${param.conf}"/></title>
    <internal:gwtInit standalone="true"/>
    <internal:gwtImport module="org.jahia.ajax.gwt.module.contentmanager.ContentManager"/>
    <c:if test="${param.conf == 'filemanager' || param.conf == 'contentmanager'}">
        <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/templates/assets/css/jquery.Jcrop.css"/>
        <script type="text/javascript" src="${pageContext.request.contextPath}/templates/assets/javascript/jquery.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/templates/assets/javascript/jquery.Jcrop.min.js"></script>
    </c:if>
</head>
<body>
<internal:contentManager conf="${param.conf}"/>
<internal:gwtGenerateDictionary/>
</body>
</html>