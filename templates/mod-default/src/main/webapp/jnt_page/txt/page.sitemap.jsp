<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ page contentType="text/plain; UTF-8" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
${url.current}
<c:forEach items="${jcr:getNodes(currentNode, 'jnt:page')}" var="child">
<template:module node="${child}" template="sitemap" editable="false" />
</c:forEach>
