<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="files.css"/>
<jsp:include page="../../nt_base/html/base.link.jsp">
    <jsp:param name="cssClass" value="${functions:fileIcon(currentNode.name)}"/>
    <jsp:param name="useNodeNameAsTitle" value="true"/>
</jsp:include>