<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="text" var="text"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<template:addResources type="css" resources="blog.css"/>
<c:if test="${jcr:isNodeType(currentNode, 'jnt:blogContent')}">
    <c:set var="blogHome" value="${url.base}${currentResource.node.parent.path}.html"/>
</c:if>
<c:if test="${!jcr:isNodeType(currentNode, 'jnt:blogContent')}">
    <c:set var="blogHome" value="${url.current}"/>
</c:if>
<div class="post">
    <fmt:formatDate value="${created.time}" type="date" pattern="dd" var="userCreatedDay"/>
    <fmt:formatDate value="${created.time}" type="date" pattern="MM" var="userCreatedMonth"/>
    <div class="post-date"><span>${userCreatedMonth}</span>${userCreatedDay}</div>
    <h2 class="post-title"><a href="${url.current}"><c:out value="${title.string}"/></a></h2>

    <p class="post-info"><fmt:message key="by"/> <c:set var="fields" value="${currentNode.propertiesAsString}"/>
        <a href="${url.base}/users/${createdBy.string}.html">${createdBy.string}</a>
        - <fmt:formatDate value="${created.time}" type="date" dateStyle="medium"/>
        <!-- <a href="#"><fmt:message key="category"/></a>    -->
    </p>
    <ul class="post-tags">
        <jcr:nodeProperty node="${currentNode}" name="j:tags" var="assignedTags"/>
        <c:forEach items="${assignedTags}" var="tag" varStatus="status">
            <li><a href="${blogHome}?addTag=${tag.node.name}">${tag.node.name}</a></li>
        </c:forEach>
    </ul>
    <div class="post-resume">
        <p>
            ${fn:substring(text.string,0,1200)}
        </p>
    </div>
    <p class="read-more"><a title="#" href="${url.current}"><fmt:message key="jnt_blog.readPost"/></a></p>
    <jcr:sql var="numberOfPostsQuery"
             sql="select [jcr:uuid] from [jnt:post] as p  where isdescendantnode(p,['${currentNode.path}'])"/>
    <c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
    <p class="post-info-links">
        <c:if test="${numberOfPosts == 0}">
            <a class="comment_count" href="${url.current}#comments">0 <fmt:message key="comments"/></a>
        </c:if>
        <c:if test="${numberOfPosts > 0}">
            <a class="comment_count" href="${url.current}#comments">${numberOfPosts} <fmt:message key="comments"/></a>
        </c:if>
        <a class="ping_count" href="#"><fmt:message key="jnt_blog.noTrackback"/></a>
    </p>
    <!--stop post-->
</div>
