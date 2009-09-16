<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:sql var="numberOfPostsQuery" sql="select jcr:uuid from jahiaForum:post  where jcr:path like '${currentNode.path}/%/%'"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<div>
    <c:if test="${jcr:isNodeType(currentNode.parent, 'jahiaForum:boardIndex')}">
        <a href="${url.base}${currentNode.parent.path}.detail.html">${currentNode.parent.propertiesAsString['boardSubject']}</a>
    </c:if>
</div>
<div class="topic-subject">
    <jcr:nodeProperty node="${currentNode}" name="topicSubject"/> :
    <c:if test="${numberOfPosts > 0}">${numberOfPosts} Posts </c:if>
</div>
<ul>
    <c:forEach items="${currentNode.editableChildren}" var="thread" varStatus="status">
        <li>
            <template:module node="${thread}" template="summary"/>
        </li>
    </c:forEach>
</ul>
<template:module node="${currentNode}" template="newThreadForm"/>
