<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--<jcr:sql var="numberOfPostsQuery"
         sql="select jcr:uuid from jahiaForum:post  where jcr:path like '${currentNode.path}/%/%'"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<div>
    <c:if test="${jcr:isNodeType(currentNode.parent, 'jahiaForum:boardIndex')}">
        <a href="${url.base}${currentNode.parent.path}.detail.html">${currentNode.parent.propertiesAsString['boardSubject']}</a>
    </c:if>
</div>
<div class="topic-subject">
    <jcr:nodeProperty node="${currentNode}" name="topicSubject"/> :
    <c:if test="${numberOfPosts > 0}">${numberOfPosts} Posts </c:if>
</div>
<ul>
    <c:forEach items="${currentNode.editableChildren}" var="thread" varStatus="status">
        <li>
            <template:module node="${thread}" template="summary"/>
        </li>
    </c:forEach>
</ul>
<c:if test="${renderContext.user.name != 'guest'}">
    <template:module node="${currentNode}" template="newThreadForm"/>
</c:if>--%>
<jcr:sql var="numberOfPostsQuery"
         sql="select jcr:uuid from jahiaForum:post  where jcr:path like '${currentNode.path}/%/%'"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<jcr:sql var="numberOfThreadsQuery"
         sql="select jcr:uuid from jahiaForum:thread  where jcr:path like '${currentNode.path}/%'"/>
<c:set var="numberOfThreads" value="${numberOfThreadsQuery.rows.size}"/>
<div id="forum-body">
<div class="topics">
    <h2><a href="${url.base}${currentNode.parent.path}.html"><jcr:nodeProperty node="${currentNode}"
                                                                               name="topicSubject"/></a></h2>

    <div class="forum-actions">

        <div class="forum-buttons">
            <div class="forum-post-icon"><a title="Post a new topic" href="#"><span/>Post a new thread</a></div>
        </div>
        <div class="forum-pagination">
            ${fn:length(currentNode.children)} threads
        </div>

    </div>
    <div class="forum-box forum-box-style1 topics">
        <span class="forum-corners-top"><span></span></span>

        <ul class="forum-list">
            <li class="forum-list-header">
                <dl class="icon">
                    <dt><a href="#">Threads</a></dt>
                    <dd class="topics">Posts</dd>
                    <%--<dd class="posts">View</dd>--%>
                    <dd class="lastpost"><span>Last post</span></dd>
                </dl>
            </li>
        </ul>


        <ul class="forum-list forums">
            <c:forEach items="${currentNode.editableChildren}" var="thread" varStatus="status">
                <li class="row">
                    <template:module node="${thread}" template="summary"/>
                </li>
            </c:forEach>
        </ul>
        <div class="clear"></div>
        <span class="forum-corners-bottom"><span></span></span>
    </div>
    <div class="forum-actions">

        <div class="forum-buttons">
            <div class="forum-post-icon">
                <c:if test="${renderContext.user.name != 'guest'}">
                    <template:module node="${currentNode}" template="newThreadForm"/>
                </c:if>
            </div>
        </div>
        <div class="forum-pagination">
            ${fn:length(currentNode.children)} threads
        </div>

    </div>

    <span>Total Threads : ${numberOfThreads}</span>
    <span>Total Posts : ${numberOfPosts}</span>
</div>
</div>