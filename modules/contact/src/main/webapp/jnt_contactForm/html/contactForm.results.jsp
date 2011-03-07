<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:forEach items="${currentNode.nodes}" var="subchild" varStatus="status">
    <div class="forum-box forum-box-style${(status.index mod 2)+1}">
        <template:module node="${subchild}" template="default"/>
    </div>
</c:forEach>
<a href="${url.base}${currentNode.path}.csv" target="_new"><fmt:message key="label.exportCSV"/></a>
