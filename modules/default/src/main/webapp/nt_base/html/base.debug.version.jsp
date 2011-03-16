<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>


<c:if test="${currentNode.versioned}">

    <jcr:nodeProperty node="${currentNode}" name="j:published" var="published"/>
    <li>
    <br>${currentNode.name} <c:if test="${currentNode.checkedOut}"><b>checkedout</b></c:if>
        <c:if test="${not empty published}"><b>published</b></c:if>
        Versions :
    <c:forEach var="v" items="${currentNode.versionHistory.allVersions}">
        <c:if test="${v.name eq currentNode.baseVersion.name}"><b>${v.name}</b></c:if>
        <c:if test="${v.name ne currentNode.baseVersion.name}">${v.name}</c:if>
        (<- <c:forEach var="p" items="${v.predecessors}">${p.name} </c:forEach>)
    </c:forEach>
    </br>
    <ul>
    <c:forEach var="child" items="${currentNode.nodes}">
        <template:module node="${child}" view="debug.version"/>
    </c:forEach>
    </ul>
    </li>
</c:if>