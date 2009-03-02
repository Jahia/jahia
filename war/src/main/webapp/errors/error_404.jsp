<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@page language="java" contentType="text/html; charset=UTF-8" 
%><%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" 
%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <title><fmt:message key="org.jahia.bin.JahiaErrorDisplay.httpNotFound.label"/></title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
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
        <p class="bold"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.pageNotFound.label"/></p>
        <p><fmt:message key="org.jahia.bin.JahiaErrorDisplay.fileNotFound.label"/></p>
        <p><fmt:message key="org.jahia.bin.JahiaErrorDisplay.clickHere1stPart.label"/>&nbsp;<a href="javascript:history.back()"><fmt:message key="org.jahia.bin.JahiaErrorDisplay.clickHere2ndPartLink.label"/></a>&nbsp;<fmt:message key="org.jahia.bin.JahiaErrorDisplay.clickHere3rdPart.label"/></p>
    </td>
</tr>
</table>

</body>
</html>