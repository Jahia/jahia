<%@ page import="org.jahia.bin.Jahia,java.util.*,javax.servlet.http.HttpServletResponse" contentType="text/html;charset=UTF-8" language="java" %><%
Map<String, String> configToPermissionMapping = new HashMap<String, String>();
configToPermissionMapping.put("categorymanager", "category-manager");
configToPermissionMapping.put("contentmanager", "content-manager");
configToPermissionMapping.put("filemanager", "file-manager");
configToPermissionMapping.put("portletmanager", "portlet-manager");
configToPermissionMapping.put("remotepublicationmanager", "remote-publication-manager");
configToPermissionMapping.put("sitemanager", "site-manager");
configToPermissionMapping.put("tagmanager", "tag-manager");
configToPermissionMapping.put("workflowmanager", "workflow-manager");
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" 
%><%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" 
%><%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" 
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
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <title><fmt:message key="label.${param.conf}"/></title>
    <internal:gwtGenerateDictionary/>
    <internal:gwtInit standalone="true"/>
    <internal:gwtImport module="org.jahia.ajax.gwt.module.contentmanager.ContentManager"/>
    <c:if test="${param.conf == 'filemanager' || param.conf == 'contentmanager'}">
        <link rel="stylesheet" type="text/css" media="screen" href="${pageContext.request.contextPath}/modules/assets/css/jquery.Jcrop.css"/>
        <script type="text/javascript" src="${pageContext.request.contextPath}/modules/assets/javascript/jquery.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/modules/assets/javascript/jquery.Jcrop.min.js"></script>
    </c:if>
</head>
<body onload="window.focus()">
<internal:contentManager conf="${param.conf}" selectedPaths="${param.selectedPaths}"/>
</body>
</html>