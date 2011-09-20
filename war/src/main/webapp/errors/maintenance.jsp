<%@page language="java" contentType="text/html; charset=UTF-8"
%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-1.1.css" type="text/css"/>
    <title><fmt:message key="label.error.maintenance.title"/></title>
</head>
<body class="login">
    <div id="adminLogin">
    <h2 class="loginlogo"></h2>
        <br class="clearFloat" />
        <h3 class="loginIcon"><fmt:message key="label.error.maintenance.title"/></h3>
        <p><fmt:message key="label.error.maintenance.description"/></p>
        <br class="clearFloat" />
    </div>
</body>
</html>