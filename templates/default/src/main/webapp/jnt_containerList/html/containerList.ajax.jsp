<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:forEach items="${currentNode.children}" var="subchild">
<c:if test="${jcr:isNodeType(subchild, 'jnt:container')}">
<p>
    ${currentNode.name}&nbsp;<a href="${url.base}${subchild.path}.html">link</a>
    <div id ="content${subchild.UUID}"></div>
    <script type="text/javascript">
        replace("${url.base}${subchild.path}.html","content${subchild.UUID}");
    </script>
</p>
</c:if>
</c:forEach>
