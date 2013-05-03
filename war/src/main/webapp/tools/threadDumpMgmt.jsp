<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<%@page import="org.jahia.tools.jvm.ThreadMonitor" %>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="tools.css"/>
<link rel="stylesheet" href="../modules/assets/css/admin-bootstrap.css"/>
<title>Thread Dump Management</title>
</head>
<body>
<h1>Thread Dump Management</h1>
<%@ include file="gotoIndex.jspf" %>
<c:if test="${empty param.threadDumpCount && (param.threadDump == 'sysout' || param.threadDump == 'file')}">
    <div class="alert alert-info">Thread dump created</div>
<% ThreadMonitor.getInstance().dumpThreadInfo("sysout".equals(request.getParameter("threadDump")), "file".equals(request.getParameter("threadDump"))); %>
</c:if>
<c:if test="${not empty param.threadDumpCount && (param.threadDump == 'sysout' || param.threadDump == 'file')}">
    <div class="alert alert-info">Thread dump task started</div>
<% ThreadMonitor.getInstance().dumpThreadInfoWithInterval("sysout".equals(request.getParameter("threadDump")), "file".equals(request.getParameter("threadDump")), Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("threadDumpCount"), "10")), Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("threadDumpInterval"), "10"))); %>
</c:if>
<table class="table table-striped table-bordered table-hover">
    <tr>
        <td><a class="btn" href="<c:url value='/tools/threadDump.jsp'/>" target="_blank">Perform thread dump (view in a new browser window)</a></td>
    </tr>
    <tr>
        <td><a class="btn" href="<c:url value='/tools/threadDump.jsp?file=true'/>" target="_blank">Perform thread dump (download as a file)</a></td>
    </tr>
    <tr>
        <td><a class="btn" href="?threadDump=sysout">Perform thread dump (System.out)</a></td>
    </tr>
    <tr>
        <td><a class="btn" href="?threadDump=file">Perform thread dump (File)</a></td>
    </tr>
    <tr>
        <td><a class="btn" href="#dump" onclick="this.href='?threadDump=file&amp;threadDumpCount=' + document.getElementById('threadDumpCount').value + '&amp;threadDumpInterval=' + document.getElementById('threadDumpInterval').value; return true;">Perform thread dump (multiple to a file)</a>
            &nbsp;&nbsp;
            <label for="threadDumpCount">count:&nbsp;</label><input type="text" id="threadDumpCount" name="threadDumpCount" size="2" value="${not empty param.threadDumpCount ? param.threadDumpCount : '10'}"/>
            &nbsp;&nbsp;
            <label for="threadDumpInterval">interval:&nbsp;</label><input type="text" id="threadDumpInterval" name="threadDumpInterval" size="2" value="${not empty param.threadDumpInterval ? param.threadDumpInterval : '10'}"/>&nbsp;seconds
        </td>
    </tr>
    <tr>
        <td><a class="btn"
            href="http://java.net/projects/tda/downloads/download/webstart/tda.jnlp"
            title="Launch the current TDA 2.2 release from the browser (requires Java Webstart). Disclaimer: this is an external program from http://java.net/projects/tda/"
            target="_blank" onclick="return confirm('This will launch the current TDA 2.2 release from the browser (requires Java Webstart).\nDisclaimer: this is an external program from http://java.net/projects/tda/\nWould you like to continue?');">Launch Thread Dump Analyzer</a>
        </td>
        </td>
    </tr>
</table>

</body>
</html>