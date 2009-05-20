<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" 
%><%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><fmt:message key="org.jahia.engines.filemanager.Filemanager_Engine.fileLocation.label"/></title>
<internal:gwtInit modules="org.jahia.ajax.gwt.module.filepicker.FilePicker"/>
<script type="text/javascript">
function handleSelection(path) {
    <c:if test="${not empty param.callback}">
    if (window.opener) {
        <c:if test="${not empty param.callbackType}">
        window.opener.${param.callback}('${param.callbackType}' == 'url' ? '${pageContext.request.contextPath}/repository/default' + path : path);
        </c:if>
        <c:if test="${empty param.callbackType}">
        window.opener.${param.callback}(path, '${pageContext.request.contextPath}/repository/default' + path);
        </c:if>
    }
    </c:if>
    window.close();
}
</script>
</head>
<body>
<internal:fileManager rootPath='${not empty param.rootPath ? param.rootPath : "files"}' startPath='${not empty param.startPath ? param.startPath : ""}'
                      nodeTypes='${not empty param.foldersOnly ? "nt:folder" : "nt:file"}' filters='${not empty param.filters ? param.filters : ""}'
                      mimeTypes='${not empty param.mimeTypes ? param.mimeTypes : ""}' callback="handleSelection" conf="filepicker" />
<internal:gwtGenerateDictionary/>
</body>
</html>