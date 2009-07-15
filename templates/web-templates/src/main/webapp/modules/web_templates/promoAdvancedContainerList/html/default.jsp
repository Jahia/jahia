<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<div class="columns2"><!--start 2columns -->
    <c:forEach items="${currentNode.children}" var="subchild">
<c:if test="${jcr:isNodeType(subchild, 'jnt:container')}">
    <template:module node="${subchild}" />
</c:if>
</c:forEach>
        <div class="clear"> </div>
</div>