<!DOCTYPE html>
<%@page import="org.jahia.bin.Jahia,org.jahia.settings.SettingsBean,org.jahia.utils.properties.PropertiesManager"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="tools.css"/>
<link rel="stylesheet" href="../modules/assets/css/admin-bootstrap.css"/>
<title>Maintenance Mode</title>
</head>
<body>
<h1>Maintenance Mode</h1>
<%@ include file="gotoIndex.jspf" %>
<c:if test="${not empty param.maintenance}">
<%
Jahia.setMaintenance(Boolean.valueOf(request.getParameter("maintenance")));
PropertiesManager properties = new PropertiesManager(pageContext.getServletContext().getRealPath(SettingsBean.JAHIA_PROPERTIES_FILE_PATH));
properties.setProperty("maintenanceMode", Boolean.toString(Jahia.isMaintenance()));
properties.storeProperties();
%>
</c:if>
<% pageContext.setAttribute("maintenance", Boolean.valueOf(Jahia.isMaintenance())); %>
<c:if test="${maintenance}">
	<p>The maintenance mode is currently <span class="badge badge-important">ON</span></p>
    <p>Click here to: <a class="btn btn-success" href="?maintenance=false">Disable maintenance mode</a></p>
</c:if>
<c:if test="${not maintenance}">
	<p>The maintenance mode is currently <span class="badge">OFF</span></p>
    <p>Click here to: <a class="btn btn-danger" href="?maintenance=true">Enable maintenance mode</a></p>
</c:if>

</body>
</html>