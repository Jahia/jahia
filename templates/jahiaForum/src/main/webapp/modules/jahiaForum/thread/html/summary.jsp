<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:sql var="numberOfPostsQuery" sql="select jcr:uuid from jahiaForum:post  where jcr:path like '${currentNode.path}/%'"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<div class="thread-subject">
    <a href="${url.base}${currentNode.path}.html"><jcr:nodeProperty node="${currentNode}" name="threadSubject"/> :
    <c:if test="${numberOfPosts > 0}">${numberOfPosts} Posts </c:if><c:if test="${numberOfPosts <= 0}">No Posts</c:if></a>
</div>
