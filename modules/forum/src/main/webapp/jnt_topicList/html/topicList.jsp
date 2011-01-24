<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<template:linker property="j:bindedComponent"/>
<template:addResources type="css" resources="forum.css"/>
<c:set var="linked" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:if test="${not empty linked}">
    <jcr:sql var="numberOfPostsQuery"
             sql="select [jcr:uuid] from [jnt:post] as p  where isdescendantnode(p,['${linked.path}'])"/>
    <c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
    <jcr:sql var="numberOfThreadsQuery"
             sql="select [jcr:uuid] from [jnt:thread] as t  where isdescendantnode(t,['${linked.path}'])"/>
    <c:set var="numberOfThreads" value="${numberOfThreadsQuery.rows.size}"/>
    <template:addResources type="css" resources="forum.css"/>
    <template:addDependency node="${linked}"/>
    <div class="topics">
        <h2><a href="${url.base}${linked.parent.path}.html"><jcr:nodeProperty node="${linked}"
                                                                              name="topicSubject"/></a></h2>

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
                <c:forEach items="${linked.nodes}" var="thread">
                    <c:if test="${(currentNode.properties.viewTopics.boolean and jcr:isNodeType(thread, 'jnt:topic')) or (currentNode.properties.viewThreads.boolean and jcr:isNodeType(thread, 'jnt:thread'))}">
                        <li class="row">
                            <template:module node="${thread}" template="summary"/>
                        </li>
                        <c:set var="found" value="true"/>
                    </c:if>
                </c:forEach>

                <c:if test="${not found}">
                    <li class="row">
                        No thread or topic found
                    </li>
                </c:if>

            </ul>
            <div class="clear"></div>
            <span class="forum-corners-bottom"><span></span></span>
        </div>
        <span><fmt:message key="total.threads"/>: ${numberOfThreads}</span>
        <span><fmt:message key="total.posts"/>: ${numberOfPosts}</span>
    </div>

</c:if>