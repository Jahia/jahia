<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="forum.css" nodetype="jnt:boardIndex"/>
<jcr:sql var="numberOfPostsQuery"
         sql="select [jcr:uuid] from [jnt:post] as p  where isdescendantnode(p,['${currentNode.path}'])"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<jcr:sql var="numberOfThreadsQuery"
         sql="select [jcr:uuid] from [jnt:thread] as t  where isdescendantnode(t,['${currentNode.path}'])"/>
<c:set var="numberOfThreads" value="${numberOfThreadsQuery.rows.size}"/>
<div id="forum-body">
    <div class="topics">
        <h2><a href="${url.base}${currentNode.parent.path}.html"><jcr:nodeProperty node="${currentNode}"
                                                                                   name="boardSubject"/></a></h2>

        <div class="forum-actions">

            <div class="forum-buttons">
                <div class="forum-post-icon"><a title="Post a new topic" href="#"><span/>Post a new topic</a></div>
            </div>
            <div class="forum-pagination">
                ${fn:length(currentNode.children)} topics
            </div>

        </div>
        <div class="forum-box forum-box-style1 topics">
            <span class="forum-corners-top"><span></span></span>

            <ul class="forum-list">
                <li class="forum-list-header">
                    <dl class="icon">
                        <dt>Topics</dt>
                        <dd class="topics">Posts</dd>
                        <%--<dd class="posts">View</dd>--%>
                        <dd class="lastpost"><span>Last post</span></dd>
                    </dl>
                </li>
            </ul>


            <ul class="forum-list forums">
                <c:forEach items="${currentNode.children}" var="topic" varStatus="status">
                    <li class="row">
                        <template:module node="${topic}" template="summary"/>
                    </li>
                </c:forEach>
            </ul>
            <div class="clear"></div>
            <span class="forum-corners-bottom"><span></span></span>
        </div>
        <template:module node="${currentNode}" template="newTopicForm"/>
        <div class="forum-actions">
            <div class="forum-pagination">
                ${fn:length(currentNode.children)} topics
            </div>
        </div>
        <span>Total Threads : ${numberOfThreads}</span>
        <span>Total Posts : ${numberOfPosts}</span>
    </div>
</div>