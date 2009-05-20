<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language = "java" 
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" 
%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%@ page import="org.jahia.settings.SettingsBean"
%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>

<c:if test="${not empty requestScope['org.jahia.exception'] || not empty requestScope['javax.servlet.error.exception']}">
<!--
Error:
<c:out value="${not empty requestScope['org.jahia.exception'] ? requestScope['org.jahia.exception'].message : requestScope['javax.servlet.error.exception'].message}"/>

Exception StackTrace: <c:out value="${not empty requestScope['org.jahia.exception.trace'] ? requestScope['org.jahia.exception.trace'] : requestScope['javax.servlet.error.exception']}"/>
-->
</c:if>
    <title><fmt:message key="org.jahia.bin.JahiaErrorDisplay.jahiaError.label"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css" type="text/css"/>
</head>

<body>

<br/><br/><br/>

<table class="errorbox" align="center" width="530" height="63" border="0" cellspacing="0" cellpadding="0">
<tr>
    <td class="boxtitle"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.errorPage.label"/></td>
</tr>
<tr>
    <td class="boxcontent">
        <p class="bold"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.internalError.label"/></p>
        <%
        if (SettingsBean.getInstance().isDevelopmentMode()) {
        %>
        <p><a href="#viewSource" onclick="document.location='view-source:' + document.location.href; return false;"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.viewSource2part.label"/></a></p>
        <% } %>
        <p><fmt:message key="org.jahia.bin.JahiaErrorDisplay.clickHere1stPart.label"/>&nbsp;<a href="javascript:history.back()"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.clickHere2ndPartLink.label"/></a>&nbsp;<fmt:message key="org.jahia.bin.JahiaErrorDisplay.clickHere3rdPart.label"/></p>
    </td>
</tr>
</table>
</body>
</html>