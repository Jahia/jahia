<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>

<div id="navigationN2"><!--start navigationN2-->
<ul class="level_2">
    <c:forEach items="${currentNode.children}" var="child" varStatus="status">
        <c:if test="${jcr:isNodeType(child, 'jnt:page')}">
            <li class="">
                <jcr:nodeProperty node="${child}" name="jcr:title" var="title"/>
                <a href="${pageContext.request.contextPath}/render/${currentResource.workspace}/${currentResource.locale}${child.path}.html">${title.string}</a>
            </li>
        </c:if>
    </c:forEach>
</ul>
</div>