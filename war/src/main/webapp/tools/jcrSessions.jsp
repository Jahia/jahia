<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.jahia.services.content.JCRSessionWrapper" buffer="16kb" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.UUID" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<c:set var="workspace" value="${functions:default(param.workspace, 'default')}"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="tools.css" type="text/css"/>
    <title>JCR Sessions</title>
</head>
<body>
<h1>JCR Sessions</h1>
There are currently <%= JCRSessionWrapper.getActiveSessions() %> open sessions and we are
retaining <%= JCRSessionWrapper.getActiveSessionsObjects().size() %>.
<ol>

    <%
        final PrintWriter s = new PrintWriter(pageContext.getOut());
        for (Map.Entry<UUID, JCRSessionWrapper> entry : JCRSessionWrapper.getActiveSessionsObjects().entrySet()) {
    %>
    <li>
<pre><%

    entry.getValue().getSessionTrace().printStackTrace(s);
%>
        </pre>
    </li>
    <%
            pageContext.getOut().flush();
        }
    %>

</ol>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>