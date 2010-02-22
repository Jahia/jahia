<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${currentNode.properties['j:limit'].long gt 0}">
    <jcr:sql var="result"
             sql="select * from [jmix:topStory] as story where isdescendantnode(story, ['${renderContext.siteNode.path}'])
         and story.[j:level]='${currentNode.properties['j:level']}' order by story.[jcr:lastModified] desc"/>

    <c:set var="forcedSkin" value="none" />
    <c:set var="renderOptions" value="none" />
    <c:set var="currentList" value="${result.nodes}" scope="request"/>
</c:if>
