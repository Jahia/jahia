<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.apache.jackrabbit.core.JahiaRepositoryImpl"%>
<%@page import="org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository"%>
<%@page import="org.apache.jackrabbit.api.stats.QueryStat"%>
<%@page import="org.jahia.services.content.JCRSessionFactory"%>
<%@page import="java.io.File"%>
<%@page import="java.io.FileFilter"%>
<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="org.apache.commons.io.filefilter.DirectoryFileFilter"%>
<%@page import="org.jahia.settings.SettingsBean"%>
<%@page import="org.jahia.services.search.spell.CompositeSpellChecker"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="tools.css" type="text/css" />
    <title>JCR Query statistics</title>
</head>
<body>
<%
QueryStat queryStat = ((JahiaRepositoryImpl)((SpringJackrabbitRepository)JCRSessionFactory.getInstance().getDefaultProvider().getRepository()).getRepository()).getContext().getStatManager().getQueryStat();
pageContext.setAttribute("queryStat", queryStat);
%>
<h1>JCR Query statistics</h1>
<p>
	<a href="?refresh=true"><img src="<c:url value='/icons/refresh.png'/>" height="16" width="16" alt=" " align="top"/>Refresh</a>
	<a href="?action=reset"><img src="<c:url value='/icons/showTrashboard.png'/>" height="16" width="16" alt=" " align="top"/>Reset statistics</a>
</p>
<c:if test="${not empty param.action}">
	<c:choose>
		<c:when test="${param.action == 'enable'}">
			<% queryStat.setEnabled("on".equals(request.getParameter("status"))); %>
			<p style="color: blue">Query statistics now ${param.status == 'on' ? 'enabled' : 'disabled'}.</p>
		</c:when>
		<c:when test="${param.action == 'reset'}">
			<%
			queryStat.clearPopularQueriesQueue();
			queryStat.clearSlowQueriesQueue();
			%>
			<p style="color: blue">Query statistics was cleared.</p>
		</c:when>
	</c:choose>
</c:if>
<p>Query statistics when enabled provides information about slow queries and most popular queries.</p>
<p>The JCR query statistics is currently ${queryStat.enabled ? 'enabled' : 'disabled'}.
<a href="?action=enable&amp;status=${queryStat.enabled ? 'off' : 'on'}">${queryStat.enabled ? 'Disable it' : 'Enable it'}</a></p>
<c:if test="${queryStat.enabled}">
<fieldset>
<legend>Slow queries</legend>
<c:if test="${not empty queryStat.slowQueries}" var="statsAvailable">
<ol>
	<c:forEach items="${queryStat.slowQueries}" var="q">
		<li>
			${fn:escapeXml(q.statement)}
			<br/>
			<c:url var="executeUrl" value="/tools/jcrQuery.jsp">
				<c:param name="lang" value="${q.language}"/>
				<c:param name="query" value="${q.statement}"/>
			</c:url>
			<a title="Execute in JCR Query Tool"
            	href="${executeUrl}"
                target="_blank"><img src="<c:url value='/icons/tab-search.png'/>" width="16" height="16" alt="run" title="Execute in JCR Query Tool">execute</a>		
			<br/>
			duration: <strong>${q.duration} ms</strong><br/>
			language: <strong>${q.language}</strong><br/>
			created on: <strong>${q.creationTime}</strong><br/>
		</li>
	</c:forEach>
</ol>
</c:if>
<c:if test="${!statsAvailable}">
<p>There is no statistics collected so far</p>
</c:if>
</fieldset>

<fieldset>
<legend>Popular queries</legend>
<c:if test="${not empty queryStat.popularQueries}" var="statsAvailable">
<ol>
	<c:forEach items="${queryStat.popularQueries}" var="q">
		<li>
			${fn:escapeXml(q.statement)}
			<br/>
			<c:url var="executeUrl" value="/tools/jcrQuery.jsp">
				<c:param name="lang" value="${q.language}"/>
				<c:param name="query" value="${q.statement}"/>
			</c:url>
			<a title="Execute in JCR Query Tool"
            	href="${executeUrl}"
                target="_blank"><img src="<c:url value='/icons/tab-search.png'/>" width="16" height="16" alt="run" title="Execute in JCR Query Tool">execute</a>		
			<br/>
			duration: <strong>${q.duration} ms</strong><br/>
			language: <strong>${q.language}</strong><br/>
			created on: <strong>${q.creationTime}</strong><br/>
		</li>
	</c:forEach>
</ol>
</c:if>
<c:if test="${!statsAvailable}">
<p>There is no statistics collected so far</p>
</c:if>
</fieldset>
</c:if>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>