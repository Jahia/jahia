<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="java.sql.Connection"%>
<%@ page import="org.jahia.utils.DatabaseUtils"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
Connection conn = DatabaseUtils.getDatasource().getConnection();
try {
    pageContext.setAttribute("info", conn.getMetaData());
%>
<p>
<strong><fmt:message key="serverSettings.dbSettings.dbInfo"/>:</strong>&nbsp;${fn:escapeXml(info.databaseProductName)}&nbsp;${fn:escapeXml(info.databaseProductVersion)}
</p>
<p>
<strong><fmt:message key="serverSettings.dbSettings.driverInfo"/>:</strong>&nbsp;${fn:escapeXml(info.driverName)}&nbsp;${fn:escapeXml(info.driverVersion)}
</p>
<c:if test="${info.driverName == 'Apache Derby Embedded JDBC Driver'}">
<p>
<strong><fmt:message key="serverSettings.dbSettings.dbHome"/>:</strong>&nbsp;<c:out value='<%= System.getProperty("derby.system.home") %>'/>
</p>
</c:if>
<p>
<strong><fmt:message key="serverSettings.dbSettings.dbUrl"/>:</strong>&nbsp;${fn:escapeXml(info.URL)}
</p>
<% } finally {
   conn.close(); 
}%>