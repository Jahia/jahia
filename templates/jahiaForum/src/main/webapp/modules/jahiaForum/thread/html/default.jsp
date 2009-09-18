<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--<div>
    <c:if test="${jcr:isNodeType(currentNode.parent.parent, 'jahiaForum:boardIndex')}">
        <a href="${url.base}${currentNode.parent.parent.path}.detail.html">${currentNode.parent.parent.propertiesAsString['boardSubject']}</a>&nbsp;>>&nbsp;
    </c:if>
    <c:if test="${jcr:isNodeType(currentNode.parent, 'jahiaForum:topic')}">
        <a href="${url.base}${currentNode.parent.path}.html">${currentNode.parent.propertiesAsString['topicSubject']}</a>&nbsp;>>&nbsp;
    </c:if>
    ${currentNode.propertiesAsString['threadSubject']}
</div>
<ul>
    <c:forEach items="${currentNode.editableChildren}" var="subchild" varStatus="status">
        <li>
            <template:module node="${subchild}" template="default"/>
        </li>
    </c:forEach>
    <c:if test="${renderContext.user.name != 'guest'}">
        <li>
            <template:module node="${currentNode}" template="newPostForm"/>
        </li>
    </c:if>
</ul>--%>
<div id="forum-body">
    <div class="posts">
        <h2><a href="${url.base}${currentNode.parent.path}.html"><jcr:nodeProperty node="${currentNode}"
                                                                                   name="threadSubject"/></a></h2>

        <div class="forum-actions">

            <div class="forum-buttons">
                <div class="forum-post-icon"><a title="Post a new post" href="#"><span/>Post a new post</a></div>
            </div>
            <div class="forum-pagination">
                ${fn:length(currentNode.children)} posts
            </div>

        </div>

        <c:forEach items="${currentNode.editableChildren}" var="subchild" varStatus="status">
            <c:if test="${not status.last and not renderContext.editMode}">
                <div class="forum-box forum-box-style${(status.index mod 2)+1}">
                    <template:module node="${subchild}" template="default"/>
                </div>
            </c:if>
        </c:forEach>
        <template:module node="${currentNode}" template="newPostForm"/>
        <div class="forum-actions">
            <div class="forum-pagination">
                ${fn:length(currentNode.children)} posts
            </div>

        </div>
    </div>
</div>