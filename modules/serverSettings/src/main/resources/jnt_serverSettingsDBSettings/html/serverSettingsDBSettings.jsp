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

<c:if test="${ds.class.name == 'org.apache.commons.dbcp.BasicDataSource'}">
    <%-- Special DBCP pool case, so we can display more info --%>
    <p><strong><fmt:message key="serverSettings.dbSettings.pool"/>:</strong></p>
    <p style="padding-left: 20px"><strong><fmt:message key="serverSettings.dbSettings.pool.maxActive"/>:</strong>&nbsp;${ds.maxActive}</p>

    <p style="padding-left: 20px"><strong><fmt:message key="serverSettings.dbSettings.pool.numActive"/>:</strong>&nbsp;${ds.numActive}</p>

    <p style="padding-left: 20px"><strong><fmt:message key="serverSettings.dbSettings.pool.numIdle"/>:</strong>&nbsp;${ds.numIdle}</p>

</c:if>
<% } finally {
   conn.close(); 
}%>