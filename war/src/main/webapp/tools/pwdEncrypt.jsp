<%@ page contentType="text/html; charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ page import="org.jahia.utils.EncryptionUtils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/tools/tools.css" type="text/css" />
<title>Password encryption</title>
</head>
<body>
<h1>Password encryption</h1>
<c:if test="${not empty param.pwd}">
<p style="color: blue">Encrypted password for <strong>${fn:escapeXml(param.pwd)}</strong> is: <strong><%= EncryptionUtils.sha1DigestLegacy(request.getParameter("pwd")) %></strong></p>
</c:if>
<form id="pwdEncrypt" action="?" method="get">
<p>
<label for="pwd">Provide a password you would like to encrypt (SHA-1 + Base64):</label><br/>
<input type="text" id="pwd" name="pwd" value="${fn:escapeXml(param.pwd)}" size="30"/>
<input type="submit" value="Encrypt" />
</p>
</form>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>