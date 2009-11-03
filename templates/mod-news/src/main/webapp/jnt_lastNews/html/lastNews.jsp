<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<jcr:nodeProperty node="${currentNode}" name="maxNews" var="maxNews"/>

<jcr:sql var="newsList"
         sql="select * from [jnt:news] as news  order by news.[date] desc"
         limit="${maxNews.long}"/>

<c:if test="${newsList.nodes.size == 0}">
    No News Found
</c:if>

<c:forEach items="${newsList.nodes}" var="news">
    <template:module node="${news}" editable="false" />
</c:forEach>