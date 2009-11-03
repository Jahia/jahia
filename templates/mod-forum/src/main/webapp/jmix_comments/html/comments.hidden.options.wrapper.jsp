<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<template:addResources type="css" resources="forum.css" nodetype="jmix:comments"/>
<jcr:node var="comments" path="${currentNode.path}/comments"/>
<c:if test="${currentNode.properties.shortView.boolean == true}">
   <template:module node="${comments}" forcedTemplate="summary"/>
</c:if>
<c:if test="${currentNode.properties.shortView.boolean == false}">
   <template:module node="${comments}" forcedTemplate="default"/>
</c:if>