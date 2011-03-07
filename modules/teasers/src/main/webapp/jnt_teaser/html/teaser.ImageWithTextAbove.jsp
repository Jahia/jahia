<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<template:addResources type="css" resources="teaser.css"/>
<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

<%--<jcr:nodeProperty node="${currentNode}" name="link" var="link"/>--%>
<div class="visuel">
    <c:if test="${not empty currentNode.properties.link.node}">
        <a class="visuel" href="${url.base}${currentNode.properties.link.node.path}.html" title="">
    </c:if>
        <img src="${image.node.url}" alt="">
    <c:if test="${not empty currentNode.properties.link.node}">
        </a>
    </c:if>
     <template:addCacheDependency uuid="${currentNode.properties.link.string}"/>
    <c:if test="${not empty currentNode.properties.link.node}">
        <a href="${url.base}${currentNode.properties.link.node.path}.html">
    </c:if>
        <jcr:nodeProperty node="${currentNode}" name="jcr:title"/>
    <c:if test="${not empty currentNode.properties.link.node}">
        </a>
    </c:if>
</div>
