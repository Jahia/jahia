<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/functions" prefix="functions" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<c:set var="config" value="${functions:default(param.type, 'filepicker')}"/>
<html>
	<head>
		<title><fmt:message key="org.jahia.admin.sitepermissions.permission.engines.importexport.ManageContentPicker.label"/></title>
        <internal:gwtGenerateDictionary/>
		<internal:gwtInit standalone="true"/>
		<internal:gwtImport module="org.jahia.ajax.gwt.module.contentpicker.ContentPicker" />
	</head>
	<body>
        <internal:contentPicker conf="${fn:escapeXml(config)}" mimeTypes="${fn:escapeXml(param.mime)}" jahiaServletPath="/cms" filesServletPath="/files" jahiaContextPath="${pageContext.request.contextPath}"  callback="${fn:escapeXml(param.CKEditorFuncNum)}"/>    
	</body>
</html>