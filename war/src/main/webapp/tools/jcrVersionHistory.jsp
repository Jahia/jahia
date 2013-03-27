<%@ page contentType="text/html; charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="java.util.*"%>
<%@page import="org.jahia.services.history.NodeVersionHistoryHelper"%>
<%@page import="org.jahia.services.history.NodeVersionHistoryHelper.VersionHistoryCheckStatus"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>JCR Version History Management</title>
<script type="text/javascript">
    function go(id1, value1, id2, value2, id3, value3) {
        document.getElementById(id1).value=value1;
        if (id2) {
            document.getElementById(id2).value=value2;
        }
        if (id3) {
            document.getElementById(id3).value=value3;
        }
        document.getElementById('navigateForm').submit();
    }
</script>
</head>
<c:set var="maxLimit" value="${functions:default(fn:escapeXml(param.maxLimit), '10000')}"/>
<%
if ("orphanedStop".equals(request.getParameter("action"))) {
    NodeVersionHistoryHelper.forceStop();
}
pageContext.setAttribute("checkRunning", NodeVersionHistoryHelper.isCheckingOrphans());
%>
<body>
<h1>JCR Version History Management</h1>
<p>This tool aims to perform cleanup tasks on the version store, e.g. find version history for nodes that no longer exists and purge them or purge old versions for existing nodes.</p>
<c:if test="${param.action == 'orphanedReport' || param.action == 'orphanedDelete'}">
<pre>
<%
long timer = System.currentTimeMillis();
final Set<String> ids = new HashSet<String>();
final long maxLimit = Long.parseLong((String) pageContext.getAttribute("maxLimit"));
try {
    VersionHistoryCheckStatus status = NodeVersionHistoryHelper.checkOrphaned(null, maxLimit, "orphanedDelete".equals(request.getParameter("action")), out);

    pageContext.setAttribute("status", status);
} catch (IllegalStateException e) {
    pageContext.setAttribute("error", e);
} catch (Exception e) {
    e.printStackTrace();
    pageContext.setAttribute("error", e);
} finally {
    pageContext.setAttribute("took", System.currentTimeMillis() - timer);
}
%>
</pre>
<fieldset>
<c:if test="${empty error}">
<legend style="color: blue">Successfully executed in <strong>${took}</strong> ms</legend>
<p>Checked <strong>${status.checked}</strong> items. Found <strong>${status.orphaned}</strong> orphaned version histories.
<c:if test="${param.action == 'orphanedDelete'}">
<strong>${status.deleted}</strong> version histories successfully deleted.
</c:if>
</p>
<c:if test="${status.orphaned >= maxLimit}">
<p>Please, note, that the check was stopped when more than <strong>${maxLimit}</strong> orphaned histories were found (maximum limit).</p>
</c:if>
</c:if>
<c:if test="${not empty error}">
    <pre style="color: red">${error}</pre>
</c:if>
</fieldset>
</c:if>

<fieldset>
    <legend>Orphaned version histories</legend>
    <form id="navigateForm" action="?" method="get">
        <input type="hidden" name="action" id="action" value=""/>
        <c:if test="${not checkRunning}">
            <p>Limit orphaned histories to: <input type="text" name="maxLimit" id="maxLimit" value="${maxLimit}" size="11"/></p>
            <p><input type="submit" name="report" onclick="if (confirm('Start checking for the orhpaned version history?')) { go('action', 'orphanedReport'); } return false;" value="Check for orphaned version history"/> - searches for version history of already deleted nodes and prints a report</p>
            <p><input type="submit" name="delete" onclick="if (confirm('The version history for no longer existing nodes will be permanently deleted. Do you want to continue?')) { go('action', 'orphanedDelete'); } return false;" value="Delete orphaned version history"/> - searches for version history of already deleted nodes and deletes version items</p>
        </c:if>
        <c:if test="${checkRunning}">
            <p>The version history is currently being checked for orphans.</p>
            <p><input type="submit" name="stop" onclick="if (confirm('Do you want to stop the check running version history check process?')) { go('action', 'orphanedStop'); } return false;" value="Stop running orphaned version history check process"/></p>
        </c:if>
    </form>
</fieldset>    
<%@ include file="gotoIndex.jspf" %>
</body>
</html>