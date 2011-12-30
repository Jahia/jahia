<%@page import="java.io.StringWriter"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.Date"%>
<%@page import="org.jahia.bin.errors.ErrorFileDumper"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Memory Information</title>
</head>
<c:if test="${param.action == 'gc'}">
<% System.gc(); %>
</c:if>
<body>
<h1>Memory Status at <%= new Date() %></h1>
<%
StringWriter s = new StringWriter();
ErrorFileDumper.outputSystemInfo(new PrintWriter(s), false, false, true, false, false, false, false);
pageContext.setAttribute("info", s.toString().replace("\n", "<br/>"));
%>
<p>${info}</p>
<p>
<a href="?refresh=true"><img src="<c:url value='/icons/refresh.png'/>" height="16" width="16" alt=" " align="top"/>Refresh</a>
&nbsp;
<a href="?action=gc"><img src="<c:url value='/icons/showTrashboard.png'/>" height="16" width="16" alt=" " align="top"/>Run Garbage Collector</a></p>
<br/>
<p>
<img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
<a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>