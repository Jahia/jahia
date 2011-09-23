<%@ page contentType="text/html; charset=UTF-8" language="java"%> 
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/tools/tools.css" type="text/css" />
<title>Jahia Text Extraction Service</title>
</head>
<body>
<h1>Jahia Text Extraction Service</h1>
<form id="extraction" action="${pageContext.request.contextPath}/cms/text-extract" enctype="multipart/form-data" method="post">
<p>
<label for="file">Choose a file to upload:&nbsp;</label><input name="file" id="file" type="file" />
</p>
<p><input type="submit" value="Extract content" /></p>
</form>
<c:if test="${extracted}">
<hr/>
<h2>Content extracted in ${extractionTime} ms</h2>
<fieldset>
    <legend><strong>Metadata</strong></legend>
    <c:forEach items="${metadata}" var="item">
        <p>
            <strong><c:out value="${item.key}"/>:&nbsp;</strong>
            <c:out value="${item.value}"/>
        </p>
    </c:forEach>
</fieldset>
<fieldset>
    <legend><strong>Content (${fn:length(content)} characters)</strong></legend>
    <p><c:out value="${content}"/></p>
</fieldset>
</c:if>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>