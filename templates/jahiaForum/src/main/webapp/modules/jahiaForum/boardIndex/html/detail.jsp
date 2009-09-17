<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="board-subject">
    <jcr:nodeProperty node="${currentNode}" name="boardSubject"/> :
    <c:if test="${not empty currentNode.children}">${fn:length(currentNode.children)} Topics </c:if>
    <c:if test="${empty currentNode.children}">No Topics </c:if>
</div>
<ul>
    <c:forEach items="${currentNode.editableChildren}" var="topic" varStatus="status">
        <li>
            <template:module node="${topic}" template="summary"/>
        </li>
    </c:forEach>
</ul>

<jcr:sql var="numberOfPostsQuery"
         sql="select jcr:uuid from jahiaForum:post  where jcr:path like '${currentNode.path}/%/%'"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<jcr:sql var="numberOfThreadsQuery"
         sql="select jcr:uuid from jahiaForum:thread  where jcr:path like '${currentNode.path}/%'"/>
<c:set var="numberOfThreads" value="${numberOfThreadsQuery.rows.size}"/>
<span>Total Threads : ${numberOfThreads}</span>
<span>Total Posts : ${numberOfPosts}</span>
<c:if test="${renderContext.user.name != 'guest'}">
    <template:module node="${currentNode}" template="newTopicForm"/>
</c:if>
