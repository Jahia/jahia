<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.jahia.bin.Jahia,org.jahia.settings.SettingsBean,org.jahia.utils.properties.PropertiesManager"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>System Maintenance</title>
</head>
<body>
<h1>System Maintenance</h1>
<h2>Maintenance Mode</h2>
<c:if test="${not empty param.maintenance}">
<%
Jahia.setMaintenance(Boolean.valueOf(request.getParameter("maintenance")));
PropertiesManager properties = new PropertiesManager(pageContext.getServletContext().getRealPath(SettingsBean.JAHIA_PROPERTIES_FILE_PATH));
properties.setProperty("maintenanceMode", Boolean.toString(Jahia.isMaintenance()));
properties.storeProperties();
%>
</c:if>
<% pageContext.setAttribute("maintenance", Boolean.valueOf(Jahia.isMaintenance())); %>
<p>
If the maintenance mode is enabled only requests to the Jahia Tools Area are allowed. Requests to all other pages, will be blocked.<br/>
The maintenance mode is currently <strong>${maintenance ? 'ON' : 'OFF'}</strong>.<br/>Click here to <a href="?maintenance=${!maintenance}">${maintenance ? 'disable' : 'enable'} maintenance mode</a>
</p>
<h2>Read-only Mode</h2>
<c:if test="${not empty param.readOnlyMode}">
<%
Boolean readOnly = Boolean.valueOf(request.getParameter("readOnlyMode"));
Jahia.getSettings().setReadOnlyMode(readOnly);
PropertiesManager properties = new PropertiesManager(pageContext.getServletContext().getRealPath(SettingsBean.JAHIA_PROPERTIES_FILE_PATH));
properties.setProperty("readOnlyMode", Boolean.toString(readOnly));
properties.storeProperties();
%>
</c:if>
<% pageContext.setAttribute("readOnlyMode", Boolean.valueOf(Jahia.getSettings().isReadOnlyMode())); %>
<p>
If the read-only mode is enabled, requests to the edit/contribute/studio/administration modes will be blocked.<br/>
The read-only mode is currently <strong>${readOnlyMode ? 'ON' : 'OFF'}</strong>.<br/>Click here to <a href="?readOnlyMode=${!readOnlyMode}">${readOnlyMode ? 'disable' : 'enable'} read-only mode</a>
</p>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>