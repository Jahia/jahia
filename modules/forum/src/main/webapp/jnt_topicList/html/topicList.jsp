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
             sql="select [jcr:uuid] from [jnt:page] as t  where isdescendantnode(t,['${linked.path}'])"/>
    <c:set var="numberOfThreads" value="${numberOfThreadsQuery.rows.size}"/>
    <template:addResources type="css" resources="forum.css"/>
    <template:addCacheDependency node="${linked}"/>
    <div class="topics">
        <jcr:nodeProperty node="${linked}" name="jcr:title" var="topicSubject"/>
            <%--<c:if test="${!empty topicSubject.string}">--%>
            <%--<h2><a href="${url.base}${linked.parent.path}.forum-topic.html">${topicSubject.string}</a></h2>--%>
            <%--</c:if>--%>
        <div class="forum-box forum-box-style1 topics">
            <span class="forum-corners-top"><span></span></span>

            <ul class="forum-list">
                <li class="forum-list-header">
                    <dl class="icon">
                        <dt></dt>
                        <dd class="topics">Posts</dd>
                            <%--<dd class="posts">View</dd>--%>
                        <dd class="lastpost"><span>Last post</span></dd>
                    </dl>
                </li>
            </ul>


            <ul class="forum-list forums">
                <c:forEach items="${linked.nodes}" var="topic">
                    <c:if test="${jcr:isNodeType(topic, 'jnt:topic')}">
                        <li class="row">


                            <jcr:sql var="numberOfPostsQuery"
                                     sql="select * from [jnt:post] as post  where isdescendantnode(post, ['${topic.path}']) order by post.[jcr:lastModified] desc"/>
                            <c:set var="numberOfPosts" value="${numberOfPostsQuery.nodes.size}"/>
                            <c:forEach items="${numberOfPostsQuery.nodes}" var="node" varStatus="status" end="2">
                                <c:if test="${status.first}">
                                    <c:set value="${node}" var="lastModifiedNode"/>
                                    <jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
                                    <jcr:nodeProperty node="${lastModifiedNode}" name="jcr:createdBy" var="createdBy"/>
                                </c:if>
                            </c:forEach>
                            <c:if test="${jcr:hasPermission(section, 'deleteTopic')}">
                                <template:tokenizedForm>
                                    <form action="${url.base}${topic.path}" method="post"
                                          id="jahia-forum-topic-delete-${topic.UUID}">
                                        <input type="hidden" name="redirectTo"
                                               value="${url.base}${renderContext.mainResource.node.path}"/>
                                            <%-- Define the output format for the newly created node by default html or by redirectTo--%>
                                        <input type="hidden" name="newNodeOutputFormat" value="html"/>
                                        <input type="hidden" name="methodToCall" value="delete"/>
                                    </form>
                                </template:tokenizedForm>
                            </c:if>

                            <dl>
                                <dt title="posts"><a class="forum-title"
                                                     href="${url.base}${topic.path}.html"><jcr:nodeProperty
                                        node="${topic}" name="topicSubject"/></a>
                                    <br/>
                                <p>
                                        ${topic.properties.topicSubject.string}
                                </p>

                                <ul class="forum-profile-icons">
                                    <c:if test="${jcr:hasPermission(section, 'deleteTopic')}">
                                        <li class="delete-post-icon"><a title="Delete this topic" href="#"
                                                                        onclick="document.getElementById('jahia-forum-topic-delete-${topic.UUID}').submit();"><span>Delete this topic</span></a>
                                        </li>
                                    </c:if>

                                </ul>
                                </dt>
                                    <%--<dd class="topics">30</dd>--%>
                                <dd class="posts">${numberOfPosts}</dd>
                                <dd class="lastpost">
                                    <c:if test="${numberOfPosts > 0}">
                                        <span>
                                                    <dfn><fmt:message key="last.post"/></dfn> <fmt:message key="by"/> <a
                                                href="${url.base}${lastModifiedNode.parent.path}.html"><img height="9"
                                                                                                            width="11"
                                                                                                            title="View the latest post"
                                                                                                            alt="View the latest post"
                                                                                                            src="${url.currentModule}/css/img/icon_topic_latest.gif"/>${createdBy.string}
                                        </a><br/><fmt:formatDate value="${lastModified.time}" dateStyle="full"
                                                                 type="both"/></span>
                                    </c:if>
                                </dd>
                            </dl>
                        </li>
                        <c:set var="found" value="true"/>
                    </c:if>
                </c:forEach>

                <c:if test="${not found}">
                    <li class="row">
                        No topic found
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