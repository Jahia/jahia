<%@page import="java.io.PrintWriter,java.util.Date,java.text.SimpleDateFormat,org.jahia.bin.errors.ErrorFileDumper"%> 
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" 
%><c:if test="${param.file}"><%
response.setContentType("text/plain; charset=ISO-8859-1");
response.setHeader("Content-Disposition", "attachment; filename=\"system-info-"
        + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".txt\"");
%>System Status Information at <%= new java.util.Date() %><% pageContext.getOut().append("\n"); %>
<% ErrorFileDumper.outputSystemInfoAll(new PrintWriter(pageContext.getOut())); %></c:if><c:if test="${not param.file}">
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>System Status Information</title>
</head>
<body>
<h1>System Status Information at <%= new Date() %></h1>
<pre>
    <% ErrorFileDumper.outputSystemInfoAll(new PrintWriter(pageContext.getOut())); %>
</pre>
</body>
</html>
</c:if>