<%@page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html>
<head>
    <meta charset="utf-8">
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/errors.css" type="text/css"/>
    <title><fmt:message key="label.error.503.title" var="i18nTitle"/><c:set var="i18nTitle" value="${fn:escapeXml(i18nTitle)}"/>${i18nTitle}</title>
</head>
<body class="error-page">
<div class="row-fluid login-wrapper">
    <div class="span4 box error-box">
        <div class="content-wrap">
            <h1 class="message-big">${i18nTitle}</h1>
            <p><fmt:message key="label.error.503.description"/></p>
            <p><fmt:message key="label.error.maintenance.description"/></p>
        </div>
    </div>
</div>
</body>
</html>