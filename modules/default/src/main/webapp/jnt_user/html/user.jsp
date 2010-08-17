<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<%--todo : find another way to dispatch user page/detail--%>
<c:choose>
    <c:when test="${renderContext.mainResource == currentResource }">
        <template:module node="${currentNode}" template="page"/>
    </c:when>
    <c:otherwise>
        <template:module node="${currentNode}" template="detail"/>        
    </c:otherwise>
</c:choose>