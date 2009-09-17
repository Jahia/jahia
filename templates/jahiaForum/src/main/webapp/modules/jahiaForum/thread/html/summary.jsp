<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:sql var="numberOfPostsQuery" sql="select jcr:lastModified from jahiaForum:post  where jcr:path like '${currentNode.path}/%' order by jcr:lastModified desc"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<c:forEach items="${numberOfPostsQuery.rows}" var="row" varStatus="status" end="2">
    <c:if test="${status.first}">
        <c:set value="${row.values[0].date}" var="lastModified"/>
    </c:if>
</c:forEach>
<div class="thread-subject">
    <a href="${url.base}${currentNode.path}.html"><jcr:nodeProperty node="${currentNode}" name="threadSubject"/></a> :
    <c:if test="${numberOfPosts > 0}">${numberOfPosts} Posts. Last post <fmt:formatDate value="${lastModified.time}" type="both" dateStyle="full"/></c:if><c:if test="${numberOfPosts <= 0}">No Posts</c:if>
</div>
