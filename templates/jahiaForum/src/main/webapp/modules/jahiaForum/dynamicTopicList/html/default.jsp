<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:xpath var="topics" xpath="//element(*,jahiaForum:topic)"/>
<c:if test="${topics.nodes.size == 0}">
    No Topics Found
</c:if>

<ul>
<c:forEach items="${topics.nodes}" var="topic">
    <li><a href="${url.base}${topic.path}.html"><jcr:nodeProperty node="${topic}" name="topicSubject"/> (${fn:length(topic.children)} threads)</a></li>
</c:forEach>
</ul>
