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
<h1>Jahia Tools Area</h1>
<fieldset>
    <legend>System</legend>
    <ul>
        <li><a href="systemInfo.jsp">System information</a></li>
        <li><a href="dumpThreads.jsp">Thread state information</a></li>
        <li><a href="log4jAdmin.jsp">Log4j administration</a></li>
        <li><a href="maintenance.jsp">System maintenance</a></li>
        <li><a href="viewsession.jsp">View HTTP session information</a></li>
        <li><a href="precompileServlet">JSP pre-compilation</a></li>
    </ul>
</fieldset>
<fieldset>
    <legend>Data</legend>
    <ul>
        <li><a href="jcrBrowser.jsp">JCR repository browser</a></li>
        <li><a href="jobadmin.jsp">Background job administration</a></li>
        <li><a href="search.jsp">Search engine management</a></li>
    </ul>
</fieldset>
<fieldset>
    <legend>Test</legend>
    <ul>
        <li><a href="docConverter.jsp">Document Converter</a></li>
        <li><a href="textExtractor.jsp">Document text extractor</a></li>
        <li><a href="wcagChecker.jsp">WCAG checker</a></li>
    </ul>
</fieldset>
</body>
</html>