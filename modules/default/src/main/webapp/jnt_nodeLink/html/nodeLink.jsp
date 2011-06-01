<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:description" var="description"/>
<jcr:nodeProperty node="${currentNode}" name="j:target" var="target"/>
<c:set var="node" value="${reference.node}"/>
<template:addCacheDependency uuid="${reference.string}"/>
<c:if test="${not empty node}">
<c:url var="link" value="${url.base}${node.path}.html"/>
</c:if>
<c:if test="${not empty target.string}"><c:set var="target"> target="${target.string}"</c:set></c:if>
<c:if test="${not empty description.string}"><c:set var="linkTitle"> title="${fn:escapeXml(description.string)}"</c:set></c:if>
<a href="${link}" ${target} ${linkTitle}>${fn:escapeXml(not empty title.string ? title.string : currentNode.name)}</a>