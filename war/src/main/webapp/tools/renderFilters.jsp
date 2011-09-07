<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.jahia.services.render.RenderService"%>
<%@page import="org.jahia.services.render.filter.AbstractFilter"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Render Filters</title>
    <link rel="stylesheet" href="tools.css" type="text/css" />
</head>
<%
pageContext.setAttribute("filters", RenderService.getInstance().getRenderChainInstance().getFilters());
pageContext.setAttribute("newline", "\n");
%>
<body>
	<h1>Render Filters (${functions:length(filters)} found)</h1>
<table border="1" cellspacing="0" cellpadding="5">
    <thead>
        <tr>
            <th>#</th>
            <th>Priority</th>
            <th>Class</th>
            <th>Description</th>
            <th>Conditions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${filters}" var="filter" varStatus="status">
        <tr>
            <td align="center"><span style="font-size: 0.8em;">${status.index + 1}</span></td>
            <td align="center"><strong>${filter.priority}</strong></td>
            <td title="${filter.class.name}"><c:set var="parts" value="${fn:split(filter.class.name, '.')}"/>${parts[fn:length(parts) - 1]}</td>
            <td>
                <% if (pageContext.getAttribute("filter") instanceof AbstractFilter) {%>
                ${fn:escapeXml(filter.description)}
                <% } %>
            </td>
            <td>${fn:escapeXml(filter.conditionsSummary)}</td>
        </tr>
        </c:forEach>
    </tbody>
</table>

<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>