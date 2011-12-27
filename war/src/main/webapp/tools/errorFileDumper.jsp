<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.jahia.bin.errors.ErrorFileDumper" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Error File Dumper</title>
</head>
<body>
<h1>Error File Dumper</h1>
<c:if test="${not empty param.active}">
<%
ErrorFileDumper.setFileDumpActivated(Boolean.valueOf(request.getParameter("active")));
%>
</c:if>
<% pageContext.setAttribute("active", Boolean.valueOf(ErrorFileDumper.isFileDumpActivated())); %>
<c:if test="${active}">
	<p>The dumping of error and thread information to a file is currently <strong>ON</strong>.<br/>Click here to <a href="?active=false">disable error file dumper</a></p>
</c:if>
<c:if test="${not active}">
	<p>The dumping of error and thread information to a file is currently <strong>OFF</strong>.<br/>Click here to <a href="?active=true">enable error file dumper</a></p>
</c:if>
<p>Please note that these settings are valid only during server run time and are not persisted between server restarts.</p>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>