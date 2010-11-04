<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:forEach items="${currentNode.nodes}" var="subchild" varStatus="status">
    <div class="forum-box forum-box-style${(status.index mod 2)+1}">
        <template:module node="${subchild}" template="small"/>
    </div>
</c:forEach>
<a href="${url.base}${currentNode.path}.csv" target="_new">csv export</a>
