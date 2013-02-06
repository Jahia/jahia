<%@ page contentType="text/html;charset=UTF-8" language="java"
        %><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">


<%@ page import="org.jahia.services.content.JCRStoreService" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.services.content.JCRStoreProvider" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>JCR Providers</title>
    <link rel="stylesheet" href="tools.css" type="text/css" />
</head>

<%
    Map<String,JCRStoreProvider> providers = JCRStoreService.getInstance().getSessionFactory().getProviders();
    pageContext.setAttribute("providers",providers);
%>

<body>
<h1>JCR Providers (${functions:length(providers)} found)</h1>
<table border="1" cellspacing="0" cellpadding="5">
    <thead>
    <tr>
        <th>#</th>
        <th>Provider Name</th>
        <th>Mount point</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${providers}" var="provider" varStatus="status">
    <tr>
        <td align="center"><span style="font-size: 0.8em;">${status.index + 1}</span></td>
        <td align="center"><strong>${provider.key}</strong></td>
        <td align="center">${provider.value.mountPoint}</td>
        <td></td>
    </tr>
    </c:forEach>
    </tbody>
</table>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>