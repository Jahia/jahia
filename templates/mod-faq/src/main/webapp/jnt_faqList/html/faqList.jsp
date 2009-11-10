<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<div class="resultsList">
    <ol>
        <c:forEach items="${currentNode.editableChildren}" var="subchild">
            <h4><template:module node="${subchild}" template="summary" /></h4>
        </c:forEach>
    </ol>
</div>

<div class="resultsList">
    <ol>
        <c:forEach items="${currentNode.editableChildren}" var="subchild">
            <li><template:module node="${subchild}"/></li>
        </c:forEach>
    </ol>
</div>