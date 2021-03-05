<%@page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html>
<head>
    <meta charset="utf-8">
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/errors.css" type="text/css"/>
    <title><fmt:message key="label.error.license.title"/></title>
</head>
<body class="login">
<div class="row-fluid login-wrapper">
    <img class="logo" alt="jahia" src="${pageContext.request.contextPath}/css/images/jahia-logo.png">
    <div class="span4 box box error-box">
        <div class="content-wrap">
            <h1 class="message-big"><fmt:message key="label.error.license.title"/></h1>
            <p>
                <c:choose>
                    <c:when test="${not empty customMessage}">
                        <fmt:message key="${customMessage.resourceKey}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="label.error.license.description"/>
                    </c:otherwise>
                </c:choose>
            </p>
            <a class="btn btn-block btn-info" href="<c:url value='/tools/licenseInfo.jsp'/>" title="<fmt:message key='label.information'/>">
                <i class="icon-info-sign icon-white"></i>
                &nbsp;<fmt:message key='label.information'/>
            </a>
        </div>
    </div>
</body>
</html>