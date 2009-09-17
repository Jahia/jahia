<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:sql var="numberOfPostsQuery"
         sql="select * from jahiaForum:post  where jcr:path like '${currentNode.path}/%' order by jcr:lastModified desc"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.nodes.size}"/>
<c:forEach items="${numberOfPostsQuery.nodes}" var="node" varStatus="status" end="2">
    <c:if test="${status.first}">
        <c:set value="${node}" var="lastModifiedNode"/>
        <jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
        <jcr:nodeProperty node="${lastModifiedNode}" name="jcr:createdBy" var="createdBy"/>
    </c:if>
</c:forEach>
<%--<div class="thread-subject">
    <a href="${url.base}${currentNode.path}.html"><jcr:nodeProperty node="${currentNode}" name="threadSubject"/></a> :
    <c:if test="${numberOfPosts > 0}">${numberOfPosts} Posts. Last post <fmt:formatDate value="${lastModified.time}" type="both" dateStyle="full"/></c:if><c:if test="${numberOfPosts <= 0}">No Posts</c:if>
</div>--%>
<div id="forum-body">
<dl>
    <dt title="posts"><a class="forum-title" href="${url.base}${currentNode.path}.html"><jcr:nodeProperty
            node="${currentNode}" name="threadSubject"/></a>
        <br/></dt>
    <%--<dd class="topics">30</dd>--%>
    <dd class="posts">${numberOfPosts}</dd>
    <dd class="lastpost"><span>
					<dfn>Last post</dfn> by <a href="${url.base}/content/users/${createdBy.string}"><img height="9" width="11"
                                                                                                  title="View the latest post"
                                                                                                  alt="View the latest post"
                                                                                                  src="img/icon_topic_latest.gif"/>${createdBy.string}
    </a><br/>at <fmt:formatDate value="${lastModified.time}" dateStyle="full" type="both"/></span></dd>
</dl>
</div>