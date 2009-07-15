<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>

<div id="navigationN1"><!--start navigationN1-->
    <ul class="level_1">
    <c:if test="${currentNode != null}">

        <jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>

        <li class="item_1 standard first">
        <a href="${baseUrl}${currentNode.path}.html">${title.string}</a>
        </li>
    </c:if>

    <c:forEach items="${currentNode.children}" var="child" varStatus="status">
        <c:if test="${jcr:isNodeType(child, 'jnt:page')}">
            <li class="item_${status.index + 2} standard ">
            <jcr:nodeProperty node="${child}" name="jcr:title" var="title"/>
            <a href="${baseUrl}${child.path}.html">${title.string}</a>
            </li>
        </c:if>
    </c:forEach>
    </ul>
</div><!--stop navigationN1-->
