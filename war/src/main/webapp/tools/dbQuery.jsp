<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Jahia DB Query Tool</title>
</head>
<body>
<h1>Jahia DB Query Tool</h1>
<c:set var="offset" value="${not empty param.offset ? param.offset : '0'}"/>
<fieldset>
    <legend>DB query</legend>
    <form id="queryForm" action="?" method="get">
        <textarea rows="3" cols="75" name="query" id="query"
            onkeyup="if ((event || window.event).keyCode == 13 && (event || window.event).ctrlKey) document.getElementById('queryForm').submit();"
        >${not empty param.query ? param.query : 'SELECT * FROM jahia_db_test'}</textarea>
        <span>
        Max rows:
        <select name="maxRows" id="maxRows">
            <option value="50"${param.maxRows == '50' ? 'selected="selected"' : ''}>50</option>
            <option value="100"${param.maxRows == '100' ? 'selected="selected"' : ''}>100</option>
            <option value="500"${param.maxRows == '500' ? 'selected="selected"' : ''}>500</option>
            <option value="1000"${param.maxRows == '1000' ? 'selected="selected"' : ''}>1000</option>
            <option value="-1"${param.maxRows == '-1' ? 'selected="selected"' : ''}>all</option>
        </select>
        &nbsp;Offset:
        <input type="text" size="2" name="offset" id="offset" value="${offset}"/>
        <input type="submit" name="action" value="Execute query ([Ctrl+Enter])"  title="Use this button to execute SELECT queries only" />
        </span>
        <br/>
        <input type="submit" name="action" value="Execute update" title="Use this button to execute any DB data/structure modifications queries, i.e. INSERT, UPDATE, DELETE, CREATE, ALTER etc." />
    </form> 
</fieldset>

<c:if test="${not empty param.query}">
    <c:catch var="dbError">
        <% long actionTime = System.currentTimeMillis(); %>
        <c:choose>
            <c:when test="${param.action == 'Execute update'}">
                <sql:update dataSource="jdbc/jahia" sql="${param.query}" var="affected"/>
                <% pageContext.setAttribute("took", Long.valueOf(System.currentTimeMillis() - actionTime));  %>
                <fieldset>
                    <legend>Update executed in ${took} ms</legend>
                    <p>Affected <strong>${affected}</strong> row${affected > 1 ? 's' : ''}</p>
                </fieldset>
            </c:when>
            <c:otherwise>
                <sql:query dataSource="jdbc/jahia" sql="${param.query}" var="results" maxRows="${not empty param.maxRows ? param.maxRows : '-1'}" startRow="${not empty param.offset ? param.offset : '0'}"/>
                <% pageContext.setAttribute("took", Long.valueOf(System.currentTimeMillis() - actionTime));  %>
                <fieldset>
                    <legend>Displaying <strong>${results.rowCount} rows</strong> (query took ${took} ms)</legend>
                    <table border="1" cellspacing="0" cellpadding="5">
                        <thead>
                            <tr>
                                <th>#</th>
                                <c:forEach var="col" items="${results.columnNames}">
                                <th>${fn:escapeXml(col)}</th>
                                </c:forEach>
                            </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="row" items="${results.rows}" varStatus="status">
                            <tr>
                                <td><strong>${offset + status.index}</strong></td>
                                <c:forEach var="col" items="${results.columnNames}">
                                <td>${fn:escapeXml(row[col])}</td>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </fieldset>
            </c:otherwise>
        </c:choose>
    </c:catch>
    <c:if test="${not empty dbError}">
        <fieldset style="color: red">
            <legend><strong>Error</strong></legend>
            <pre>${fn:escapeXml(dbError)}</pre>
        </fieldset>
    </c:if>
</c:if>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>