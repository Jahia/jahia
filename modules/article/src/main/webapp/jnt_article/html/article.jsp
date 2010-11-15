<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="article.css"/>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>

<h2>${currentNode.properties["jcr:title"].string}</h2>

<div class="intro">
    ${currentNode.properties.intro.string}
</div>
<c:forEach items="${currentNode.nodes}" var="paragraph">
    <template:module node="${paragraph}" template="default"/>
</c:forEach>
<c:if test="${renderContext.editMode}">
    <template:module path="*"/>
</c:if>