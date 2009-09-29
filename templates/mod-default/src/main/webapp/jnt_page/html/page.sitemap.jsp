<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>

<c:if test="${empty level}" >
    <c:set var="level" value="1"/>
</c:if>
<c:set var="currentLevel" value="${level}"/>
<a href="${url.current}">${title.string}</a>
<ul class="level_${currentLevel}">
    <c:forEach items="${currentNode.children}" var="child" varStatus="status">
        <c:if test="${jcr:isNodeType(child, 'jnt:page')}">
            <li class="">
                <c:set var="level" scope="request" value="${currentLevel + 1}"/>
                <template:module node="${child}" forcedTemplate="sitemap" editable="false" />
            </li>
        </c:if>
    </c:forEach>
</ul>
