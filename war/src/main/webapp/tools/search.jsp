<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
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
    <title>Search Engine Manager</title>
    <link type="text/css" href="<c:url value='/modules/assets/css/jquery.fancybox.css'/>" rel="stylesheet"/>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/modules/assets/javascript/jquery.fancybox.pack.js'/>"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $('.detailsLink').fancybox({
                        'hideOnContentClick': false,
                        'titleShow' : false,
                        'transitionOut' : 'none'
                    });
        });
    </script>
</head>
<body>
<h1>Search Engine Management</h1>
<c:if test="${not empty param.action}">
	<c:choose>
		<c:when test="${param.action == 'updateSpellCheckerIndex'}">
			<% CompositeSpellChecker.updateSpellCheckerIndex(); %>
			<p style="color: blue">Spell checker index update triggered</p>
		</c:when>
        <c:when test="${param.action == 'reindex'}">
            <% FileUtils.touch(new File(SettingsBean.getInstance().getRepositoryHome(), "reindex")); %>
            <p style="color: blue">Re-indexing of the repository content will be done on next Jahia startup</p>
        </c:when>
        <c:when test="${param.action == 'index-fix'}">
            <% FileUtils.touch(new File(SettingsBean.getInstance().getRepositoryHome(), "index-fix")); %>
            <p style="color: blue">Repository indexes check and fix will be done on next Jahia startup</p>
        </c:when>
        <c:when test="${param.action == 'index-check'}">
            <% FileUtils.touch(new File(SettingsBean.getInstance().getRepositoryHome(), "index-check")); %>
            <p style="color: blue">Repository indexes check (no repair) will be done on next Jahia startup</p>
        </c:when>
        <c:when test="${param.action == 'index-check-physical'}">
            <%
            long actionTime = System.currentTimeMillis();
            %>
            <p style="color: blue">Start checking indexes for repository home <%= SettingsBean.getInstance().getRepositoryHome() %> (<%= org.jahia.utils.FileUtils.humanReadableByteCount(FileUtils.sizeOfDirectory(SettingsBean.getInstance().getRepositoryHome())) %>)</p>
            <jsp:include page="searchIndexCheck.jsp">
                <jsp:param name="indexPath" value="index"/>
            </jsp:include>
            <jsp:include page="searchIndexCheck.jsp">
                <jsp:param name="indexPath" value="workspaces/default/index"/>
            </jsp:include>
            <jsp:include page="searchIndexCheck.jsp">
                <jsp:param name="indexPath" value="workspaces/live/index"/>
            </jsp:include>
            <%  pageContext.setAttribute("took", Long.valueOf(System.currentTimeMillis() - actionTime)); %>
            <p style="color: blue">...done in ${took} ms</p>
        </c:when>
	</c:choose>
</c:if>
<fieldset>
<legend>On next Jahia startup</legend>
<ul>
    <li><a href="?action=reindex">Repository re-indexing</a> - Do repository re-indexing on the next Jahia start</li>
    <li><a href="?action=index-fix">Repository index check and fix</a> - Do repository search indexes logical check and fix inconsistencies on the next Jahia start</li>
    <li><a href="?action=index-check">Repository index check (no repair)</a> - Do repository search indexes logical check just reporting inconsistencies in the log on the next Jahia start</li>
</ul>
</fieldset>
<fieldset>
<legend>Immediate</legend>
<ul>
    <li><a href="?action=index-check-physical">Repository index physical check</a> - Do immediate repository search indexes physical consistency check and print out the results (Lucene CheckIndex tool)</li>
    <li><a href="?action=updateSpellCheckerIndex">Spell checker index update</a> - triggers an immediate update (no restart needed) of the spell checker
    dictionary index used by the "Did you mean" search feature</li>
</ul>
</fieldset>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>