<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@page import="java.io.PrintWriter,java.util.Date,org.jahia.bin.errors.ErrorFileDumper"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ page import="org.jahia.settings.SettingsBean" %>
<% pageContext.setAttribute("devMode", Boolean.valueOf(SettingsBean.getInstance().isDevelopmentMode())); %>
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/jahia65.css" type="text/css"/>
    <title><fmt:message key="label.error"/></title>

</head>
<body class="login" onload="if (history.length > 1) { document.getElementById('backLink').style.display=''; }">
<div class="grass"></div>
<div class="grass2"></div>
<div class="hive"></div>
<div class="bear"></div>
<div class="cloud"></div>
<div class="cloud2"></div>
<h2 class="loginlogo_community"></h2>

    <div id="adminLogin">
            <br class="clearFloat" />
            <h3 class="loginIcon"><fmt:message key="label.error"/></h3>
        <p><fmt:message key="label.error.500.description"/></p>
        <p id="backLink" style="display:none"><fmt:message key="label.error.backLink.1"/>&nbsp;<a href="javascript:history.back()"><fmt:message key="label.error.backLink.2"/></a>&nbsp;<fmt:message key="label.error.backLink.3"/></p>
        <p><fmt:message key="label.error.homeLink"/>:&nbsp;<a href="<c:url value='/'/>"><fmt:message key="label.homepage"/></a></p>
            <br class="clearFloat" />
    </div>
<c:if test="${devMode && (not empty requestScope['org.jahia.exception'] || not empty requestScope['javax.servlet.error.exception'])}">
<div style="display:none">
<pre>
Error: <c:out value="${not empty requestScope['org.jahia.exception'] ? requestScope['org.jahia.exception'].message : requestScope['javax.servlet.error.exception'].message}"/>

<c:out value="${not empty requestScope['org.jahia.exception.trace'] ? requestScope['org.jahia.exception.trace'] : requestScope['javax.servlet.error.exception']}"/>
</pre>
<strong>System Status Information at <%= new Date() %></strong>
<pre>
    <% ErrorFileDumper.outputSystemInfoAll(new PrintWriter(pageContext.getOut())); %>
</pre>
</div>
</c:if>
</body>
</html>