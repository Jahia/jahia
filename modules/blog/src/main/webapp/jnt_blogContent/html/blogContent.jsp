<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="css" resources="forum.css"/>
<template:addResources type="css" resources="blog.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="text" var="text"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>

<fmt:formatDate value="${created.time}" type="date" pattern="dd" var="userCreatedDay"/>
<fmt:formatDate value="${created.time}" type="date" pattern="MMM" var="userCreatedMonth"/>
<c:if test="${jcr:isNodeType(currentNode, 'jnt:blogContent')}">
    <c:set var="blogHome" value="${url.base}${currentResource.node.parent.path}.html"/>
</c:if>
<c:if test="${!jcr:isNodeType(currentNode, 'jnt:blogContent')}">
    <c:set var="blogHome" value="${url.current}"/>
</c:if>
<c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
    <form action="${url.base}${currentNode.path}" method="post"
          id="jahia-blog-article-delete-${currentNode.UUID}">
        <input type="hidden" name="redirectTo" value="${url.base}${jcr:getParentOfType(renderContext.mainResource.node, 'jnt:page').path}"/>
            <%-- Define the output format for the newly created node by default html or by redirectTo--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <input type="hidden" name="methodToCall" value="delete"/>
    </form>
</c:if>

<div class="post">
    <c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
        <span class="posteditdelete">
            <a class="postdelete"  href="#" onclick="document.getElementById('jahia-blog-article-delete-${currentNode.UUID}').submit();"><fmt:message key="blog.label.delete"/></a>
            <a class="postedit" href="${url.base}${currentResource.node.path}.blog-edit.html"><fmt:message key="blog.label.edit"/></a>
        </span>
    </c:if>
    <div class="post-date"><span>${userCreatedMonth}</span>${userCreatedDay}</div>
    <h2 class="post-title"><c:out value="${title.string}"/></h2>

    <jcr:node path="/users/${createdBy.string}" var="contentUser"/>
    <c:set var="fields" value="${contentUser.propertiesAsString}"/>
    <p class="post-info"><fmt:message key="blog.label.by"/>&nbsp;${createdBy.string}&nbsp;-&nbsp;<fmt:formatDate value="${created.time}" type="date" dateStyle="medium"/></p>
    <ul class="post-tags">
        <jcr:nodeProperty node="${currentNode}" name="j:tags" var="assignedTags"/>
        <c:forEach items="${assignedTags}" var="tag" varStatus="status">
            <li>${tag.node.name}</li>
        </c:forEach>
    </ul>
    <div class="post-content">
        <p>${text.string}</p>
    </div>
    <jcr:sql var="numberOfPostsQuery"
             sql="select [jcr:uuid] from [jnt:post] as p  where isdescendantnode(p,['${currentNode.path}'])"/>
    <c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
    <p class="post-info-links">
        <c:if test="${numberOfPosts == 0}">
            <a class="comment_count" href="${url.current}#comments">0&nbsp;<fmt:message key="blog.label.comments"/></a>
        </c:if>
        <c:if test="${numberOfPosts > 0}">
            <a class="comment_count" href="${url.current}#comments">${numberOfPosts}&nbsp;<fmt:message key="blog.label.comments"/></a>
        </c:if>
    </p>
 <div class="clear"></div>
</div>

