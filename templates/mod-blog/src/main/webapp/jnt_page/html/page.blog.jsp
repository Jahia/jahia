<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<template:addResources type="css" resources="blog.css"/>
<template:addWrapper name="blogWrapper"/>
<div class="post">
<c:if test="${currentNode.nodes.size > 0}">
    List of entries :
    <c:forEach items="${currentNode.nodes}" var="child">
        <template:module node="${child}" template="short"/>
    </c:forEach>
</c:if>
</div>