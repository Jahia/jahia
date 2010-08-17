<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/functions" prefix="functions" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
        <internal:contentPicker conf='${config}' mimeTypes='${param["mime"]}' jahiaServletPath='/cms' jahiaContextPath='${pageContext.request.contextPath}'  callback='${param["CKEditorFuncNum"]}'/>    
	</body>
</html>