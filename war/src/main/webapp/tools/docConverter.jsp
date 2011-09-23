<%@ page contentType="text/html; charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import="org.jahia.services.transform.DocumentConverterService"%>
<%@page import="org.jahia.services.SpringContextSingleton"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/tools/tools.css" type="text/css" />
<title>Jahia Document Conversion Service</title>
</head>
<body>
<h1>Jahia Document Conversion Service</h1>
<%
DocumentConverterService service = (DocumentConverterService) SpringContextSingleton.getBean("DocumentConverterService");
pageContext.setAttribute("serviceEnabled", service != null && service.isEnabled());
%>
<c:if test="${serviceEnabled}">
<form id="conversion" action="${pageContext.request.contextPath}/cms/convert" enctype="multipart/form-data" method="post">
<p>
<label for="file">Choose a file to upload:&nbsp;</label><input name="file" id="file" type="file" />
</p>
<p>
<label for="mimeType">Target document type:&nbsp;</label>
<select id="mimeType" name="mimeType">
    <option value="application/pdf">Adobe PDF</option>
    <option value="application/msword">Microsoft Word Document</option>
    <option value="application/vnd.ms-excel">Microsoft Excel Sheet</option>
    <option value="application/vnd.ms-powerpoint">Microsoft Powerpoint Presentation</option>
    <option value="application/vnd.oasis.opendocument.text">OpenDocument Text</option>
    <option value="application/vnd.oasis.opendocument.spreadsheet">OpenDocument Spreadsheet</option>
    <option value="application/vnd.oasis.opendocument.presentation">OpenDocument Presentation</option>
    <option value="application/x-shockwave-flash">Flash</option>
</select>
</p>
<p><input type="submit" value="Convert file" /></p>
</form>
</c:if>
<c:if test="${!serviceEnabled}">
<p>Conversion service is not enabled.</p>
</c:if>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>