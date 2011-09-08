<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Jahia Tools</title>
</head>
<body>
<h1>Jahia Tools Area (<%= org.jahia.bin.Jahia.getFullProductVersion() %>)</h1>
<fieldset>
    <legend>System</legend>
    <ul>
        <li><a href="systemInfo.jsp">System information</a></li>
        <li><a href="threadDumpMgmt.jsp">Thread state information</a></li>
        <li><a href="log4jAdmin.jsp">Log4j administration</a></li>
        <li><a href="maintenance.jsp">System maintenance</a></li>
        <%--
        <li><a href="viewsession.jsp">View HTTP session information</a></li>
        --%>
        <li><a href="precompileServlet">JSP pre-compilation</a></li>
    </ul>
</fieldset>
<fieldset>
    <legend>Data</legend>
    <ul>
        <li><a href="jcrBrowser.jsp">JCR repository browser</a></li>
        <li><a href="jcrQuery.jsp">JCR query tool</a></li>
        <li><a href="modulesBrowser.jsp">Installed modules browser</a></li>
        <li><a href="definitionsBrowser.jsp">Installed definitions browser</a></li>
        <li><a href="renderFilters.jsp">Render filters</a></li>
        <li><a href="jobadmin.jsp">Background job administration</a></li>
        <li><a href="search.jsp">Search engine management</a></li>
        <li><a href="dbQuery.jsp">DB query tool</a></li>
        <li><a href="groovyConsole.jsp">Groovy console</a></li>
    </ul>
</fieldset>
<fieldset>
    <legend>Cache</legend>
    <ul>
        <li><a href="cache.jsp">Cache management</a></li>
        <li><a href="ehcache/ehcache_cj.jsp">Modules</a></li>
        <li><a href="ehcache/ehcache_cj_dep.jsp">Dependencies</a></li>
    </ul>
</fieldset>
<fieldset>
    <legend>Test</legend>
    <ul>
        <li><a href="docConverter.jsp">Document converter</a></li>
        <li><a href="textExtractor.jsp">Document text extractor</a></li>
        <li><a href="wcagChecker.jsp">WCAG checker</a></li>
    </ul>
</fieldset>
<p>&copy; Copyright 2002-2011 Jahia Solutions Group SA - All rights reserved.</p> 
</body>
</html>