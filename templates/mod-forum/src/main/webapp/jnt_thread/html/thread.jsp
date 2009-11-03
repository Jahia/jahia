<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="forum.css" nodetype="jnt:thread"/>
<div id="forum-body">
    <div class="posts">
        <h2><a href="${url.base}${currentNode.parent.path}.html"><jcr:nodeProperty node="${currentNode}"
                                                                                   name="threadSubject"/></a></h2>
        <a name="wrap"></a>
        <div class="forum-actions">

            <div class="forum-buttons">
                <div class="forum-post-icon"><a title="Post a new post" href="#threadPost"><span/><fmt:message key="new.post"/></a></div>
            </div>
            <div class="forum-pagination">
                ${fn:length(currentNode.children)}&nbsp;<fmt:message key="posts"/>
            </div>

        </div>

        <c:forEach items="${currentNode.editableChildren}" var="subchild" varStatus="status" end="20">
            <c:if test="${not status.last and not renderContext.editMode}">
                <div class="forum-box forum-box-style${(status.index mod 2)+1}">
                    <template:module node="${subchild}" template="default"/>
                </div>
            </c:if>
        </c:forEach>
        <template:module node="${currentNode}" template="newPostForm"/>
        <div class="forum-actions">
            <div class="forum-pagination">
                ${fn:length(currentNode.children)}&nbsp;<fmt:message key="posts"/>
            </div>

        </div>
    </div>
</div>
