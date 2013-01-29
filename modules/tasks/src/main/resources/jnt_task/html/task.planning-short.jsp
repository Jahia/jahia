<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="now" class="java.util.Date"/>
<c:if test="${currentNode.properties['dueDate'].date.time gt now and empty todayDisplayed}">
    <c:set value="todayDisplayed" var="todayDisplayed" scope="request"/>
    ------- today -------
</c:if>
<c:choose>
    <c:when test="${currentNode.properties['state'].string eq 'finished'}">
        <div class="finishedTask">
            <span class="value"><fmt:formatDate value="${currentNode.properties['dueDate'].date.time}"
                                                pattern="dd/MM/yyyy"/></span>
            <span class="value">${currentNode.properties['jcr:title'].string}</span>
        </div>
    </c:when>
    <c:otherwise>
        <div class="unfinishedTask">
            <span class="value"><fmt:formatDate value="${currentNode.properties['dueDate'].date.time}"
                                                pattern="dd/MM/yyyy"/></span>
            <span class="value">${currentNode.properties['jcr:title'].string}</span>
        </div>
    </c:otherwise>
</c:choose>
