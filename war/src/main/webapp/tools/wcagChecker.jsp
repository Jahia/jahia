<%@ page contentType="text/html; charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/tools/tools.css" type="text/css" />
<title>WCAG Validator Service</title>
</head>
<body>
<h1>WCAG Validator Service</h1>
<form id="wcag" action="${pageContext.request.contextPath}/cms/wcag/validate" method="post">
<p>Paste here the HTML text to be validated:</p>
<p><textarea rows="5" cols="80" id="text" name="text"></textarea></p>
<p><input type="submit" value="Validate" /></p>
</form>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>