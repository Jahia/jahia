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
<c:set var="maxLimitUnused" value="${functions:default(fn:escapeXml(param.maxLimitUnused), '10000')}"/>
<c:set var="age" value="${functions:default(fn:escapeXml(param.age), '30')}"/>
<%
if ("orphanedStop".equals(request.getParameter("action"))) {
    NodeVersionHistoryHelper.forceStopOrphanedCheck();
} else if ("unusedStop".equals(request.getParameter("action"))) {
    NodeVersionHistoryHelper.forceStopUnusedCheck();
}
pageContext.setAttribute("orphanedCheckRunning", NodeVersionHistoryHelper.isCheckingOrphans());
pageContext.setAttribute("unusedCheckRunning", NodeVersionHistoryHelper.isCheckingUnused());
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
    VersionHistoryCheckStatus status = NodeVersionHistoryHelper.checkOrphaned(maxLimit, "orphanedDelete".equals(request.getParameter("action")), out);

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
<c:if test="${param.action == 'unusedReport' || param.action == 'unusedDelete'}">
<pre>
<%
long timer = System.currentTimeMillis();
final Set<String> ids = new HashSet<String>();
final long maxLimit = Long.parseLong((String) pageContext.getAttribute("maxLimitUnused"));
try {
    long age = Long.parseLong((String) pageContext.getAttribute("age"));
    long purgeOlderThanTimestamp = age > 0 ? (System.currentTimeMillis() - age * 24L * 60L * 60L * 1000L) : 0;  
    VersionHistoryCheckStatus status = NodeVersionHistoryHelper.checkUnused(maxLimit, "unusedDelete".equals(request.getParameter("action")), purgeOlderThanTimestamp, out);

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
<p>Checked <strong>${status.checked}</strong> items. Found <strong>${status.orphaned}</strong> unused versions.
<c:if test="${param.action == 'unusedDelete'}">
<strong>${status.deletedVersionItems}</strong> version items successfully deleted.
</c:if>
</p>
<c:if test="${status.orphaned >= maxLimitUnused}">
<p>Please, note, that the check was stopped when more than <strong>${maxLimitUnused}</strong> unused versions were found (maximum limit).</p>
</c:if>
</c:if>
<c:if test="${not empty error}">
    <pre style="color: red">${error}</pre>
</c:if>
</fieldset>
</c:if>

<form id="navigateForm" action="?" method="get">
    <input type="hidden" name="action" id="action" value=""/>

    <fieldset>
        <legend>Orphaned version histories</legend>
        <c:if test="${not orphanedCheckRunning}">
            <p>Limit orphaned histories to: <input type="text" name="maxLimit" id="maxLimit" value="${maxLimit}" size="11"/></p>
            <p><input type="submit" name="reportOrphaned" onclick="if (confirm('Start checking for the orhpaned version history?')) { go('action', 'orphanedReport'); } return false;" value="Check for orphaned version history"/> - searches for version history of already deleted nodes and prints a report</p>
            <p><input type="submit" name="deleteOrphaned" onclick="if (confirm('The version history for no longer existing nodes will be permanently deleted. Do you want to continue?')) { go('action', 'orphanedDelete'); } return false;" value="Delete orphaned version history"/> - searches for version history of already deleted nodes and deletes version items</p>
        </c:if>
        <c:if test="${orphanedCheckRunning}">
            <p>The version history is currently being checked for orphans.</p>
            <p><input type="submit" name="stopOrphaned" onclick="if (confirm('Do you want to stop the process running version history check?')) { go('action', 'orphanedStop'); } return false;" value="Stop running orphaned version history check process"/></p>
        </c:if>
    </fieldset>    

    <fieldset>
        <legend>Unused versions</legend>
        <c:if test="${not unusedCheckRunning}">
            <p>Limit unused versions to: <input type="text" name="maxLimitUnused" id="maxLimitUnused" value="${maxLimitUnused}" size="11"/></p>
            <p>Remove only versions older than: <input type="text" name="age" id="age" value="${age}" size="2"/> days</p>
            <p><input type="submit" name="reportUnused" onclick="if (confirm('Start checking for the unused versions?')) { go('action', 'unusedReport'); } return false;" value="Check for unused versions"/> - searches for old unused versions of nodes and prints a report</p>
            <p><input type="submit" name="deleteUnused" onclick="if (confirm('The unused versions for all nodes will be permanently deleted. Do you want to continue?')) { go('action', 'unusedDelete'); } return false;" value="Delete unused versions"/> - searches for versions which are no longer used and deletes those version items</p>
        </c:if>
        <c:if test="${unusedCheckRunning}">
            <p>The version history is currently being checked for orphans.</p>
            <p><input type="submit" name="stopUnused" onclick="if (confirm('Do you want to stop the process running unused versions check?')) { go('action', 'unusedStop'); } return false;" value="Stop running unused versions check process"/></p>
        </c:if>
    </fieldset>    

</form>

<%@ include file="gotoIndex.jspf" %>
</body>
</html>