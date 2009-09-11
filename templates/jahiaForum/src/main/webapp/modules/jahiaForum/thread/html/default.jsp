<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<ul>
<c:set var="i" value="0"/>
<c:forEach items="${currentNode.editableChildren}" var="subchild">
    <li>
        <template:module node="${subchild}" template="default"/>
    </li>
<c:set var="i" value="${i + 1}"/>
</c:forEach>
    <li>
        <template:module node="${currentNode}" template="form"/>
    </li>
</ul>
    