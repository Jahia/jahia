<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="tasks.css"/>

<jcr:node var="portal" path="${currentNode.path}/myportal${fn:replace(renderContext.mainResource.node.path,'/','_')}"/>

<c:set var="writeable" value="${currentResource.workspace eq 'live'}"/>
<c:if test="${writeable}">
    <c:if test="${empty portal}">
        <form action="${url.base}${currentNode.path}/myportal${fn:replace(renderContext.mainResource.node.path,'/','_')}" method="post">
            <input type="hidden" name="nodeType" value="jnt:portal"/>
            <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
            <input type="hidden" name="newNodeOutputFormat" value="html"/>
            Create my portal : <input type="submit" name="submit">
        </form>
    </c:if>
    <c:if test="${not empty portal}">
        <template:module path="myportal${fn:replace(renderContext.mainResource.node.path,'/','_')}" editable="false"/>
    </c:if>
</c:if>
<c:if test="${not writeable}">
    Portal is only available in live
    <template:area path="portalComponents"/>
</c:if>
