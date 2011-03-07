<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<c:if test="${renderContext.editMode}"><h4><fmt:message key="label.topStoriesArea"/></h4>
    <p><fmt:message key="label.componentDescription"/></p>
</c:if>
<c:if test="${currentNode.properties['j:limit'].long gt 0}">
    <query:definition var="listQuery"
                      statement="select * from [jmix:topStory] as story where isdescendantnode(story, ['${renderContext.site.path}'])
         and story.[j:level]='${currentNode.properties['j:level'].string}' order by story.[jcr:lastModified] desc"/>

    <c:set target="${moduleMap}" property="editable" value="false" />
    <c:set target="${moduleMap}" property="listQuery" value="${listQuery}" />
</c:if>
