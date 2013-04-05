<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="org.jahia.utils.DatabaseUtils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
DataSource ds = DatabaseUtils.getDatasource();
Connection conn = ds.getConnection();
try {
    pageContext.setAttribute("ds", ds);
    pageContext.setAttribute("info", conn.getMetaData());
%>
<table class="table table-striped table-bordered table-hover">
    <tbody>
    <tr>
        <td><strong><fmt:message key="serverSettings.dbSettings.dbInfo"/>:</strong></td>
        <td>${fn:escapeXml(info.databaseProductName)}&nbsp;${fn:escapeXml(info.databaseProductVersion)}</td>
    </tr>
    <tr>
        <td> <strong><fmt:message key="serverSettings.dbSettings.driverInfo"/>:</strong></td>
        <td>${fn:escapeXml(info.driverName)}&nbsp;${fn:escapeXml(info.driverVersion)}</td>

    </tr>
<c:if test="${info.driverName == 'Apache Derby Embedded JDBC Driver'}">
    <tr>
        <td><strong><fmt:message key="serverSettings.dbSettings.dbHome"/>:</strong></td>
        <td><c:out value='<%= System.getProperty("derby.system.home") %>'/></td>

    </tr>
</c:if>
    <tr>
        <td><strong><fmt:message key="serverSettings.dbSettings.dbUrl"/>:</strong></td>
        <td>${fn:escapeXml(info.URL)}</td>

    </tr>
<c:if test="${ds.class.name == 'org.apache.commons.dbcp.BasicDataSource'}">
    <%-- Special DBCP pool case, so we can display more info --%>
    <tr>
        <td><strong><fmt:message key="serverSettings.dbSettings.pool"/>:</strong></td>
        <td>
            <ul>
                <li><strong><fmt:message key="serverSettings.dbSettings.pool.maxActive"/>:</strong>&nbsp;<span class="badge badge-success">${ds.maxActive}</span></li>

                <li><strong><fmt:message key="serverSettings.dbSettings.pool.numActive"/>:</strong>&nbsp;<span class="badge badge-info">${ds.numActive}</span></li>

                <li><strong><fmt:message key="serverSettings.dbSettings.pool.numIdle"/>:</strong>&nbsp;<span class="badge badge-info">${ds.numIdle}</span></li>
            </ul>
        </td>
    </tr>
</c:if>
    </tbody>
</table>

<% } finally {
   conn.close();
}%>