<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.jahia.services.cache.CacheHelper"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Cache Management</title>
</head>
<body>
<h1>Cache Management</h1>
<c:if test="${not empty param.action}">
	<c:choose>
		<c:when test="${param.action == 'flushOutputCaches'}">
			<% CacheHelper.flushOutputCaches(); %>
			<p style="color: blue">Output HTML caches were successfully flushed</p>
		</c:when>
        <c:when test="${param.action == 'flushHibernateCaches'}">
			<% CacheHelper.flushHibernateCaches(); %>
            <p style="color: blue">Hibernate second level caches were successfully flushed</p>
        </c:when>
        <c:when test="${param.action == 'flushAllCaches'}">
			<% CacheHelper.flushAllCaches(); %>
            <p style="color: blue">All caches were successfully flushed</p>
        </c:when>
	</c:choose>
</c:if>
<p>Available actions:</p>
<ul>
	<li><a href="?action=flushOutputCaches">Flush HTML output caches</a> - performs flush of module output caches that are responsible for caching HTML page and fragment output, rendered in Live mode.</li>
    <li><a href="?action=flushHibernateCaches">Flush Hibernate caches</a> - does the flush of Hibernate second level caches.</li>
    <li><a href="?action=flushAllCaches">Flush all caches</a> - triggers the flush of all caches, including back-end, front-end (module output) and HIbernate second level caches.</li>
</ul>
<p>Please, not that except for the Hibernate caches, all others are flushed only on the current cluster node (flushes are not propagated to other nodes).</p>
<p>
    <a href="<c:url value='/administration/?do=status&sub=display'/>">to Server and Cache status (Jahia Administration)</a>
</p>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>