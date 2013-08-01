<%@ page contentType="text/html;charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.io.File"%>
<%@page import="org.jahia.settings.SettingsBean"%>
<%@page import="org.jahia.tools.jvm.ThreadMonitor" %>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Thread Dump Management</title>
</head>
<body>
<h1>Thread Dump Management</h1>
<c:if test="${empty param.threadDumpCount && (param.threadDump == 'sysout' || param.threadDump == 'file')}">
<% pageContext.setAttribute("outputFile", ThreadMonitor.getInstance().dumpThreadInfo("sysout".equals(request.getParameter("threadDump")), "file".equals(request.getParameter("threadDump")))); %>
<p style="color: blue">
Thread dump created<c:if test="${not empty outputFile}"> in a file:<br/><code>${outputFile}</code></c:if>
</p>
</c:if>
<c:if test="${not empty param.threadDumpCount && (param.threadDump == 'sysout' || param.threadDump == 'file')}">
<% pageContext.setAttribute("outputFile", ThreadMonitor.getInstance().dumpThreadInfoWithInterval("sysout".equals(request.getParameter("threadDump")), "file".equals(request.getParameter("threadDump")), Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("threadDumpCount"), "10")), Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("threadDumpInterval"), "10")))); %>
<p style="color: blue">
Thread dump task started<c:if test="${not empty outputFile}">. The output fill be done into a file:<br/><code>${outputFile}</code></c:if>
</p>
</c:if>
<ul>
    <li><img src="<c:url value='/icons/filePreview.png'/>" height="16" width="16" alt=" " align="top"/>&nbsp;<a href="<c:url value='/tools/threadDump.jsp'/>" target="_blank">Perform thread dump (view in a new browser window)</a></li>
	<li><img src="<c:url value='/icons/download.png'/>" height="16" width="16" alt=" " align="top"/>&nbsp;<a href="<c:url value='/tools/threadDump.jsp?file=true'/>" target="_blank">Perform thread dump (download as a file)</a></li>
    <li><img src="<c:url value='/icons/tab-workflow.png'/>" height="16" width="16" alt=" " align="top"/>&nbsp;<a href="?threadDump=sysout">Perform thread dump (System.out)</a></li>
    <li><img src="<c:url value='/icons/globalRepository.png'/>" height="16" width="16" alt=" " align="top"/>&nbsp;<a href="?threadDump=file">Perform thread dump (File)</a>&nbsp;*</li>
    <li>
        <img src="<c:url value='/icons/workflowManager.png'/>" height="16" width="16" alt=" " align="top"/>&nbsp;
        <a href="#dump" onclick="this.href='?threadDump=file&amp;threadDumpCount=' + document.getElementById('threadDumpCount').value + '&amp;threadDumpInterval=' + document.getElementById('threadDumpInterval').value; return true;">Perform thread dump (multiple to a file)</a>&nbsp;*
        &nbsp;&nbsp;
        <label for="threadDumpCount">count:&nbsp;</label><input type="text" id="threadDumpCount" name="threadDumpCount" size="2" value="${not empty param.threadDumpCount ? param.threadDumpCount : '10'}"/>
        &nbsp;&nbsp;
        <label for="threadDumpInterval">interval:&nbsp;</label><input type="text" id="threadDumpInterval" name="threadDumpInterval" size="2" value="${not empty param.threadDumpInterval ? param.threadDumpInterval : '10'}"/>&nbsp;seconds
    </li>
    <li>
       <img src="<c:url value='/icons/tda.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
       <a
        href="http://java.net/projects/tda/downloads/download/webstart/tda.jnlp"
        title="Launch the current TDA 2.2 release from the browser (requires Java Webstart). Disclaimer: this is an external program from http://java.net/projects/tda/"
        target="_blank" onclick="return confirm('This will launch the current TDA 2.2 release from the browser (requires Java Webstart).\nDisclaimer: this is an external program from http://java.net/projects/tda/\nWould you like to continue?');">Launch Thread Dump Analyzer</a>
    </li>
</ul>
<p>------------------------------------------------------------------------------------------------------------------------------------<br/>
* - The thread dumps are performed into a folder:
<pre>        <%= SettingsBean.getThreadDir() %></pre>
This location can be overridden with a system property named <code>jahia.thread.dir</code>,<br/>
e.g. by adding <code>-Djahia.thread.dir=/var/logs/jahia/threads</code> to the JVM options (<code>CATALINA_OPTS</code> for Apache Tomcat).
</p>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>