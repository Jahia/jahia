<%@page language="java" contentType="text/html; charset=UTF-8"
%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-1.1.css" type="text/css"/>
    <title><fmt:message key="label.error.404.title"/></title>
</head>
<body class="login" onLoad="if (history.length > 1) { document.getElementById('backLink').style.display=''; }">
    <div id="adminLogin">
    <h2 class="loginlogo"></h2>
            <br class="clearFloat" />
            <h3 class="loginIcon"><fmt:message key="label.error.404.title"/></h3>
        <p><fmt:message key="label.error.404.description"/></p>
        <p id="backLink" style="display:none"><fmt:message key="label.error.backLink.1"/>&nbsp;<a href="javascript:history.back()"><fmt:message key="label.error.backLink.2"/></a>&nbsp;<fmt:message key="label.error.backLink.3"/></p>
        <p><fmt:message key="label.error.homeLink"/>:&nbsp;<a href="<c:url value='/'/>"><fmt:message key="label.homepage"/></a></p>
            <br class="clearFloat" />
    </div>
</body>
</html>