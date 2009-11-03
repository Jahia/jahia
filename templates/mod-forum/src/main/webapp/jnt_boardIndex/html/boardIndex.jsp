<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="forum.css" nodetype="jnt:boardIndex"/>
<jcr:sql var="numberOfPostsQuery"
         sql="select * from [jnt:post] as post  where isdescendantnode(post, ['${currentNode.path}']) order by post.[jcr:lastModified] desc"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.nodes.size}"/>
<c:forEach items="${numberOfPostsQuery.nodes}" var="node" varStatus="status" end="2">
    <c:if test="${status.first}">
        <c:set value="${node}" var="lastModifiedNode"/>
        <jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
        <jcr:nodeProperty node="${lastModifiedNode}" name="jcr:createdBy" var="createdBy"/>
    </c:if>
</c:forEach>
<div id="forum-body">
    <div class="forum-box forum-box-style1 room">
        <span class="forum-corners-top"><span></span></span>
        <ul class="forum-list forums">
            <li class="row">
                <dl>
                    <dt title="posts">
                        <a class="forum-title" href="${url.base}${currentNode.path}.detail.html"><jcr:nodeProperty
                                node="${currentNode}" name="boardSubject"/></a><br/>
                        <jcr:nodeProperty node="${currentNode}" name="boardDescription"/></dt>
                    <dd class="topics">${fn:length(currentNode.children)}<dfn>Topics</dfn></dd>
                    <dd class="posts">${numberOfPosts} <dfn>Posts</dfn></dd>
                    <dd class="lastpost"><c:if test="${numberOfPosts > 0}">
                        <span>
					<dfn>Last post</dfn> by <a href="${url.base}${lastModifiedNode.parent.path}.html">
                            <img height="9" width="11" title="View the latest post" alt="View the latest post"
                                 src="/jahia/templates/jahia_forum/img/icon_topic_latest.gif"/>${createdBy.string}
                        </a><br/><fmt:formatDate value="${lastModified.time}" dateStyle="full" type="both"/></span>
                    </c:if></dd>
                </dl>
            </li>
        </ul>
        <div class="clear"></div>
        <span class="forum-corners-bottom"><span></span></span>
    </div>
</div>