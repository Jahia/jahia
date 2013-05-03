<%@page import="java.io.PrintWriter,java.util.Date,java.text.SimpleDateFormat,org.jahia.bin.errors.ErrorFileDumper"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"
        %><c:if test="${param.file}"><%
    response.setContentType("text/plain; charset=ISO-8859-1");
    response.setHeader("Content-Disposition", "attachment; filename=\"system-info-"
            + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".txt\"");
%>System Status Information at <%= new java.util.Date() %><% pageContext.getOut().append("\n"); %>
    <% ErrorFileDumper.outputSystemInfo(new PrintWriter(pageContext.getOut())); %></c:if><c:if test="${not param.file}">
    <!DOCTYPE html>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <html lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="stylesheet" href="tools.css" type="text/css" />
        <link rel="stylesheet" href="../modules/assets/css/admin-bootstrap.css" type="text/css" />
        <title>System Status Information</title>
    </head>
    <body>
    <h1>System Status Information at <%= new Date() %></h1>
    <%@ include file="gotoIndex.jspf" %>&nbsp;
    <a class="btn btn-primary" href="?file=true" target="_blank">download as a file</a>

    <pre>
        <% ErrorFileDumper.outputSystemInfo(new PrintWriter(pageContext.getOut())); %>
    </pre>
    </body>
    </html>
</c:if>