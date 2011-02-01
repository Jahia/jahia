<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<template:addResources type="css" resources="forum.css"/>

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
<c:if test="${jcr:hasPermission(currentNode, 'createPost')}">
    <template:tokenizedForm>
    <form action="${url.base}${currentNode.path}" method="post"
          id="jahia-forum-topic-delete-${currentNode.UUID}">
        <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
            <%-- Define the output format for the newly created node by default html or by redirectTo--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <input type="hidden" name="methodToCall" value="delete"/>
    </form>
    </template:tokenizedForm>
</c:if>

<dl>
    <dt title="posts"><a class="forum-title" href="${url.base}${currentNode.path}.html"><jcr:nodeProperty
            node="${currentNode}" name="topicSubject"/></a>
        <br/>
        <p>
            ${currentNode.properties.topicDescription.string}
        </p>
        <c:forEach items="${currentNode.nodes}" var="child" varStatus="count">
            <c:if test="${count.first}"><fmt:message key="subdivision"/></c:if>
            <c:if test="${jcr:isNodeType(child,'jnt:topic' )}">
                <a class="forum-title" href="${url.base}${child.path}.html"><jcr:nodeProperty
                node="${child}" name="topicSubject"/></a>
            </c:if>
        </c:forEach>
        <ul class="forum-profile-icons">
    <c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
        <li class="delete-post-icon"><a title="Delete this topic" href="#"
                                        onclick="document.getElementById('jahia-forum-topic-delete-${currentNode.UUID}').submit();"><span>Delete this topic</span></a>
        </li>
    </c:if>

</ul></dt>
    <%--<dd class="topics">30</dd>--%>
    <dd class="posts">${numberOfPosts}</dd>
    <dd class="lastpost">
        <c:if test="${numberOfPosts > 0}">
        <span>
					<dfn><fmt:message key="last.post"/></dfn> <fmt:message key="by"/> <a href="${url.base}${lastModifiedNode.parent.path}.html"><img height="9"
                                                                                                           width="11"
                                                                                                           title="View the latest post"
                                                                                                           alt="View the latest post"
                                                                                                           src="${url.currentModule}/css/img/icon_topic_latest.gif"/>${createdBy.string}
        </a><br/><fmt:formatDate value="${lastModified.time}" dateStyle="full" type="both"/></span>
        </c:if>
    </dd>
</dl>