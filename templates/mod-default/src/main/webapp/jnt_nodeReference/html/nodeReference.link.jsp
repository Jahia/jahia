<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<c:if test="${ not empty reference.node }">
    <c:if test="${ not empty title }">
        <a href="${url.base}${reference.node.path}.html">${title.string}</a>
    </c:if>
    <c:if test="${ empty title }">
        <a href="${url.base}${reference.node.path}.html">${reference.node.name}</a>
    </c:if>
</c:if>