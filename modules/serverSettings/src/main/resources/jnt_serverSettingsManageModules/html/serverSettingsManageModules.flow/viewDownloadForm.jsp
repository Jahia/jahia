<%@ page import="org.springframework.web.servlet.tags.form.FormTag" %>
<%@ page import="javax.servlet.jsp.tagext.*" %>
<%@ page import="java.util.Arrays" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<h2>
    <fmt:message key='serverSettings.manageModules.downloadSources' />
</h2>
<c:if test="${not empty error}">
    <div class="error"><fmt:message key='${error}'/></div>
</c:if>

<form class="form-inline" style="margin: 0;" action="${flowExecutionUrl}" method="POST">
    <input class="input" type="text" name="scmUri" value="${scmUri}" size="80"/>
    <input class="btn" type="submit" name="_eventId_downloadSources" value="<fmt:message key='serverSettings.manageModules.downloadSources'/>"/>
</form>
