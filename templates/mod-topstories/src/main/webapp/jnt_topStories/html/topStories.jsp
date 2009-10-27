<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${currentNode.properties['j:firstLimit'].long gt 0}">
    <jcr:sql var="topStories"
             sql="select * from [jmix:topStory] as story where isdescendantnode(story, ['${renderContext.siteNode.path}'])
         and story.[j:level]='first' order by story.[jcr:lastModified] desc"/>
    <c:forEach items="${topStories.nodes}" var="topic" end="${currentNode.properties['j:firstLimit'].long - 1}">
        <template:module node="${topic}" forcedTemplate="large" editable="false">
            <template:param name="withoutOptions"/>
            <template:param name="withoutSkins"/>
        </template:module>
    </c:forEach>
</c:if>

<c:if test="${currentNode.properties['j:secondLimit'].long gt 0}">
    <jcr:sql var="topStories"
             sql="select * from [jmix:topStory] as story where isdescendantnode(story, ['${renderContext.siteNode.path}'])
         and story.[j:level]='second' order by story.[jcr:lastModified] desc"/>
    <c:forEach items="${topStories.nodes}" var="topic" end="${currentNode.properties['j:secondLimit'].long - 1}">
        <template:module node="${topic}" forcedTemplate="default" editable="false">
            <template:param name="withoutOptions"/>
            <template:param name="withoutSkins"/>
        </template:module>
    </c:forEach>
</c:if>

<c:if test="${currentNode.properties['j:thirdLimit'].long gt 0}">
    <jcr:sql var="topStories"
             sql="select * from [jmix:topStory] as story where isdescendantnode(story, ['${renderContext.siteNode.path}'])
         and story.[j:level]='third' order by story.[jcr:lastModified] desc"/>
    <c:forEach items="${topStories.nodes}" var="topic" end="${currentNode.properties['j:thirdLimit'].long - 1}">
        <template:module node="${topic}" forcedTemplate="small" editable="false">
            <template:param name="withoutOptions"/>
            <template:param name="withoutSkins"/>
        </template:module>
    </c:forEach>
</c:if>