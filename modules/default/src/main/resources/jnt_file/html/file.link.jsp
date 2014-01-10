<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="files.css"/>
<c:set var="useNodeNameAsTitle" value="${not empty param.useNodeNameAsTitle ? param.useNodeNameAsTitle : 'true'}"/>

<jsp:include page="../../nt_base/html/base.link.jsp">
    <jsp:param name="cssClass" value="${functions:fileIcon(currentNode.name)}"/>
    <jsp:param name="useNodeNameAsTitle" value="${useNodeNameAsTitle}"/>
    <jsp:param name="target" value="${currentResource.moduleParams.target}"/>
</jsp:include>