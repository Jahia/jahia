
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="targetNode" value="${renderContext.mainResource.node}"/>
<c:if test="${!empty currentNode.properties.folder}">
    <c:set var="targetNode" value="${currentNode.properties.folder.node}"/>
</c:if>

<template:addResources type="css" resources="fileList.css"/>
<ul class="filesList">
    <c:forEach items="${targetNode.nodes}" var="subchild">
        <c:if test="${jcr:isNodeType(subchild, 'jnt:file')}">
        <li>
            <template:module node="${subchild}"/>
        </li>
        </c:if>
    </c:forEach>
</ul>