<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.jahia.bin.Jahia,org.jahia.settings.SettingsBean,org.jahia.utils.properties.PropertiesManager"%> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Maintenance Mode</title>
</head>
<body>
<h1>Maintenance Mode</h1>
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
	<p>The maintenance mode is currently <strong>ON</strong>.<br/>Click here to <a href="?maintenance=false">disable maintenance mode</a></p>
</c:if>
<c:if test="${not maintenance}">
	<p>The maintenance mode is currently <strong>OFF</strong>.<br/>Click here to <a href="?maintenance=true">enable maintenance mode</a></p>
</c:if>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>