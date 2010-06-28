<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<template:addResources type="css" resources="forum.css"/>
<template:addResources type="css" resources="blog.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="text" var="text"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>

<fmt:formatDate value="${created.time}" type="date" pattern="dd" var="userCreatedDay"/>
<fmt:formatDate value="${created.time}" type="date" pattern="MM" var="userCreatedMonth"/>
<c:if test="${jcr:isNodeType(currentNode, 'jnt:blogContent')}">
    <c:set var="blogHome" value="${url.base}${currentResource.node.parent.path}.html"/>
</c:if>
<c:if test="${!jcr:isNodeType(currentNode, 'jnt:blogContent')}">
    <c:set var="blogHome" value="${url.current}"/>
</c:if>
<div class="boxblog">
        <div class="boxblogshadow boxblogpadding16 boxblogmarginbottom16">
            <div class="boxblog-inner">
                <div class="boxblog-inner-border"><!--start boxblog -->
<div class="post">
    <div class="post-date"><span>${userCreatedMonth}</span>${userCreatedDay}</div>
    <h2 class="post-title"><a href="#"><c:out value="${title.string}"/></a></h2>

    <jcr:node path="/users/${createdBy.string}" var="contentUser"/>
    <c:set var="fields" value="${contentUser.propertiesAsString}"/>
    <p class="post-info"><fmt:message key="by"/>&nbsp;<a href="${url.base}/users/${createdBy.string}.html">${createdBy.string}</a>&nbsp;-&nbsp;<fmt:formatDate value="${created.time}" type="date" dateStyle="medium"/></p>
    <ul class="post-tags">
        <jcr:nodeProperty node="${currentNode}" name="j:tags" var="assignedTags"/>
        <c:forEach items="${assignedTags}" var="tag" varStatus="status">
            <li><a href="${blogHome}?addTag=${tag.node.name}">${tag.node.name}</a></li>
        </c:forEach>
    </ul>
    <div class="post-content">
        <p>${text.string}</p>
    </div>
    <jcr:sql var="numberOfPostsQuery"
             sql="select [jcr:uuid] from [jnt:post] as p  where isdescendantnode(p,['${currentNode.path}'])"/>
    <c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
    <p class="post-info-links">
        <a href="${url.base}${currentResource.node.path}.edit.html"><fmt:message key="edit"/></a>
        <c:if test="${numberOfPosts == 0}">
            <a class="comment_count" href="${url.current}#comments">0&nbsp;<fmt:message key="comments"/></a>
        </c:if>
        <c:if test="${numberOfPosts > 0}">
            <a class="comment_count" href="${url.current}#comments">${numberOfPosts}&nbsp;<fmt:message key="comments"/></a>
        </c:if>
        <a class="ping_count" href="#"><fmt:message key="jnt_blog.noTrackback"/></a>
    </p>
                    <div class="clear"></div>
                </div>

            </div>
        </div>
    </div>
    <!--stop boxblog -->
    <template:option nodetype="jmix:comments" template="hidden.options.wrapper" node="${currentNode}"/>
</div>

