<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<jcr:nodeProperty node="${currentNode}" name="maxNews" var="maxNews"/>
<jcr:nodeProperty node="${currentNode}" name="display" var="display"/>
<jcr:nodeProperty node="${currentNode}" name="filter" var="filter"/>

<jcr:sql var="newsList"
         sql="select * from [web_templates:newsContainer] as news  order by news.[newsDate] desc"
         limit="${maxNews.long}"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found
</c:if>
<c:choose>
    <c:when test="${display.string == 'small'}">
        <ul class="newsList3">
    </c:when>
    <c:when test="${display.string == 'medium'}">
        <ul class="summary">
    </c:when>
</c:choose>
<c:forEach items="${newsList.nodes}" var="news">
    <template:module node="${news}" template="${display.string}"/>
</c:forEach>
<c:choose>
    <c:when test="${display.string == 'small' || display.string == 'medium'}">
        </ul>
    </c:when>
</c:choose>