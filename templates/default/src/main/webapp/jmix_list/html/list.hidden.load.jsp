<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="renderOptionsOnChild" value="true" scope="request"/>
<%-- list mode --%>
<c:choose>
    <c:when test="${jcr:isNodeType(currentNode, 'jmix:facets')}">
        <query:definition var="listQuery" scope="request">
            <query:selector nodeTypeName="nt:base"/>
            <query:childNode path="${currentNode.realNode.path}"/>
        </query:definition>
    </c:when>
    <c:otherwise>
        <c:set value="true" var="editable" scope="request"/>
        <c:set value="${currentNode.nodes}" var="currentList" scope="request"/>
        <c:set var="end" value="${fn:length(currentNode.nodes)}" scope="request"/>
        <c:set var="listTotalSize" value="${end}" scope="request"/>
    </c:otherwise>
</c:choose>
