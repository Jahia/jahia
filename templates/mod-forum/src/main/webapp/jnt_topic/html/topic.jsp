<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:sql var="numberOfPostsQuery"
         sql="select [jcr:uuid] from [jnt:post] as p  where isdescendantnode(p,['${currentNode.path}'])"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<jcr:sql var="numberOfThreadsQuery"
         sql="select [jcr:uuid] from [jnt:thread] as t  where isdescendantnode(t,['${currentNode.path}'])"/>
<c:set var="numberOfThreads" value="${numberOfThreadsQuery.rows.size}"/>
<div id="forum-body">
    <div class="topics">
        <h2><a href="${url.base}${currentNode.parent.path}.detail.html"><jcr:nodeProperty node="${currentNode}"
                                                                                   name="topicSubject"/></a></h2>

        <div class="forum-actions">

            <div class="forum-buttons">
                <div class="forum-post-icon"><a title="<fmt:message key="create.new.thread"/>" href="#"><span/><fmt:message key="create.new.thread"/></a></div>
            </div>
            <div class="forum-pagination">
                ${fn:length(currentNode.children)} <fmt:message key="threads"/>
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
                    <c:if test="${not status.last and not renderContext.editMode}">
                        <li class="row">
                            <template:module node="${thread}" template="summary"/>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
            <div class="clear"></div>
            <span class="forum-corners-bottom"><span></span></span>
        </div>
        <template:module node="${currentNode}" template="newThreadForm"/>
        <div class="forum-actions">
            <div class="forum-pagination">
                ${fn:length(currentNode.children)} <fmt:message key="threads"/>
            </div>
        </div>
        <span><fmt:message key="total.threads"/>: ${numberOfThreads}</span>
        <span><fmt:message key="total.posts"/>: ${numberOfPosts}</span>
    </div>
</div>