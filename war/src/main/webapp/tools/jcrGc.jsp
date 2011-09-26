<%@ page contentType="text/html; charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ page import="org.jahia.services.content.JCRContentUtils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>JCR Data Store Garbage Collection</title>
</head>
<body>
<h1>JCR Data Store Garbage Collection</h1>
<c:if test="${param.action == 'gc'}">
<%
long timer = System.currentTimeMillis();
try {
    JCRContentUtils.callDataStoreGarbageCollector();
} catch (Exception e) {
    
} finally {
    pageContext.setAttribute("took", System.currentTimeMillis() - timer);
}
%>
<p style="color: blue">Successfully executed in <strong>${took}</strong> ms</p>
</c:if>
<p>Available actions:</p>
<ul>
    <li><a href="?action=gc" onclick="return confirm('You are about to start the DataStore Garbage Collector. All unused files in the data store will be permanently deleted. Do you want to continue?');">Run JCR DataStore garbage collector now</a></li>
</ul>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>