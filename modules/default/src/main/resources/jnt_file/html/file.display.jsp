<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div style="background-color: white; height: 100%; width: 100%; text-align: center">
    &nbsp;
    <c:choose>
        <c:when test="${fn:contains(currentNode.fileContent.contentType,'x-shockwave-flash')}">
            <object data="${currentNode.url}" width="80%" height="80%"></object>
        </c:when>
        <c:when test="${fn:contains(currentNode.fileContent.contentType,'video')}">
            <object data="${currentNode.url}" width="80%" height="80%"></object>
        </c:when>
        <c:when test="${fn:contains(currentNode.fileContent.contentType,'audio')}">
            <object data="${currentNode.url}" width="80%" height="80%"></object>
        </c:when>
        <c:when test="${fn:contains(currentNode.fileContent.contentType,'image')}">
            <img src="${currentNode.url}">
        </c:when>
        <c:otherwise>
            <a href="${currentNode.url}">${currentNode.name}</a>
        </c:otherwise>
    </c:choose>
    &nbsp;
</div>