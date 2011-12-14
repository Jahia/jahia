<%@page import="java.io.PrintWriter,java.util.Date,java.text.SimpleDateFormat,org.jahia.bin.errors.ErrorFileDumper"%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" 
%><c:if test="${param.file}"><%
response.setContentType("text/plain; charset=ISO-8859-1");
response.setHeader("Content-Disposition", "attachment; filename=\"system-info-"
        + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".txt\"");
%>System Status Information at <%= new java.util.Date() %><% pageContext.getOut().append("\n"); %>
<% ErrorFileDumper.outputSystemInfo(new PrintWriter(pageContext.getOut())); %></c:if><c:if test="${not param.file}">
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>System Status Information</title>
</head>
<body>
<h1>System Status Information at <%= new Date() %></h1>
<a href="?file=true" target="_blank">download as a file</a>
<br/>
<img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
<a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
<pre>
    <% ErrorFileDumper.outputSystemInfo(new PrintWriter(pageContext.getOut())); %>
</pre>
</body>
</html>
</c:if>