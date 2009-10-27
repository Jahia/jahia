<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jcr:sql var="topStories"
         sql="select * from [jmix:topStory] as story where isdescendantnode(story, ['${renderContext.siteNode.path}']) order by story.[jcr:lastModified] desc"/>


<c:if test="${topStories.nodes.size == 0}">
    No Top story
</c:if>

<ul>
<c:forEach items="${topStories.nodes}" var="topic" end="10">
    <template:module node="${topic}" />
</c:forEach>
</ul>
