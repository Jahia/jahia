<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:sql var="numberOfPostsQuery"
         sql="select * from jahiaForum:post  where jcr:path like '${currentNode.path}/%/%' order by jcr:lastModified desc"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.nodes.size}"/>
<c:forEach items="${numberOfPostsQuery.nodes}" var="node" varStatus="status" end="2">
    <c:if test="${status.first}">
        <c:set value="${node}" var="lastModifiedNode"/>
        <jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
        <jcr:nodeProperty node="${lastModifiedNode}" name="jcr:createdBy" var="createdBy"/>
    </c:if>
</c:forEach>
<div class="forum-box forum-box-style1 room">
    <span class="forum-corners-top"><span></span></span>
    <%--<c:if test="${empty requestScope.param['roomHeaders']}">
    <ul class="forum-list">
        <li class="forum-list-header">
            <dl class="icon">
                <dt><a href="#">Room</a></dt>
                <dd class="topics">Topics</dd>
                <dd class="posts">Posts</dd>
                <dd class="lastpost"><span>Last post</span></dd>
            </dl>
        </li>
    </ul>
        <c:set var="roomHeaders" value="alreadySet" scope="session"/>
    </c:if>--%>
    <ul class="forum-list forums">
        <li class="row">
            <dl>
                <dt title="posts">
                    <a class="forum-title" href="${url.base}${currentNode.path}.detail.html"><jcr:nodeProperty
                            node="${currentNode}" name="boardSubject"/></a><br/>
                    <jcr:nodeProperty node="${currentNode}" name="boardDescription"/></dt>
                <dd class="topics">${fn:length(currentNode.children)}<dfn>Topics</dfn></dd>
                <dd class="posts">${numberOfPosts} <dfn>Posts</dfn></dd>
                <dd class="lastpost"><span>
					<dfn>Last post</dfn> by <a href="${url.base}/content/users/${createdBy.string}"><img height="9" width="11" title="View the latest post"
                                                             alt="View the latest post"
                                                             src="img/icon_topic_latest.gif"/>${createdBy.string}</a><br/>
			  at <fmt:formatDate value="${lastModified.time}" dateStyle="full" type="both"/></span></dd>
            </dl>
            <div class="clear"></div>
            <span class="forum-corners-bottom"><span></span></span>
        </li>
    </ul>
</div>