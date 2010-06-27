<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<template:addResources type="css" resources="news.css"/>

<div class="newsMLComponent"><!--start newsMLComponent -->

    <!-- Role : ${currentNode.properties.role.string} -->

    <c:set var="currentList" value="${currentNode.nodes}" scope="request"/>
    <c:forEach items="${currentList}" var="subchild" varStatus="status">
        <div class="newsMLContentItem newMLContentItem-box-style${(status.index mod 2)+1}">
            <template:module node="${subchild}" template="default"/>
        </div>
    </c:forEach>
    
</div>