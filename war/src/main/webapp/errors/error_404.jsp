<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal"%>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex, nofollow"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/andromeda.css" type="text/css"/>
    <title><fmt:message key="label.error.404.title"/></title>

</head>
<body class="login">
<div class="grass"></div>
<div class="grass2"></div>
<div class="hive"></div>
<div class="bear"></div>
<div class="cloud"></div>
<div class="cloud2"></div>
<h2 class="loginlogo_community"></h2>

    <div id="adminLogin">
            <br class="clearFloat" />
            <h3 class="loginIcon"><fmt:message key="label.error.404.title"/></h3>
        <p><fmt:message key="label.error.404.description"/></p>
            <br class="clearFloat" />
    </div>
</body>
</html>