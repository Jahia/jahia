<%@page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<%@page import="java.io.PrintWriter,java.util.Date,org.jahia.bin.errors.ErrorFileDumper"%>
<%@ page import="org.jahia.settings.SettingsBean" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html>
<% pageContext.setAttribute("devMode", Boolean.valueOf(SettingsBean.getInstance().isDevelopmentMode())); %>
<head>
    <meta charset="utf-8">
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-front.css" type="text/css"/>
    <title><fmt:message key="label.error"/></title>
</head>
<body class="login" onLoad="if (history.length > 1) { document.getElementById('backLink').style.display=''; }">
<div class="row-fluid login-wrapper">
    <img class="logo" alt="jahia" src="${pageContext.request.contextPath}/css/images/jahia-logo-white.png">
    <div class="span4 box error-box">
        <div class="content-wrap">
            <h1 class="message-big"><fmt:message key="label.error"/></h1>
            <p><fmt:message key="label.error.500.description"/></p>
            <p id="backLink" style="display:none">
                <a class="btn btn-block" href="javascript:history.back()">
                    <i class="icon-chevron-left"></i>
                    &nbsp;<fmt:message key="label.error.backLink.1"/>
                    &nbsp;<fmt:message key="label.error.backLink.2"/>
                    &nbsp;<fmt:message key="label.error.backLink.3"/>
                </a>
            </p>
            <p><fmt:message key="label.error.homeLink"/></p>
            <a class="btn btn-block btn-primary" href="<c:url value='/'/>">
                <i class="icon-home icon-white"></i>
                &nbsp;<fmt:message key="label.homepage"/>
            </a>
        </div>
    </div>
</div>

<c:if test="${devMode && (not empty requestScope['org.jahia.exception'] || not empty requestScope['javax.servlet.error.exception'])}">
    <div class="row-fluid dev-mode">
        <div class="span10 offset1">
            <div style="display:none">
                <pre class="pre-dev">Error: <c:out value="${not empty requestScope['org.jahia.exception'] ? requestScope['org.jahia.exception'].message : requestScope['javax.servlet.error.exception'].message}"/>

                <c:out value="${not empty requestScope['org.jahia.exception.trace'] ? requestScope['org.jahia.exception.trace'] : requestScope['javax.servlet.error.exception']}"/>
                </pre>
                <strong>System Status Information at <%= new Date() %></strong>
                <pre class="pre-dev">
                    <% ErrorFileDumper.outputSystemInfo(new PrintWriter(pageContext.getOut())); %>
                </pre>
            </div>
        </div>
    </div>
</c:if>
</body>
</html>