<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:set value="true" var="editable" scope="request"/>
<c:choose>
    <c:when test="${jcr:isNodeType(currentNode, 'jmix:pager')}">
        <c:set scope="request" var="paginationActive" value="true"/>
        <c:set scope="request" value="${currentNode.properties['pageSize'].long}" var="pageSize"/>
        <c:if test="${not empty param.begin}">
            <c:set var="begin" value="param.begin" scope="request"/>
        </c:if>
        <c:if test="${empty param.begin}">
            <c:set var="begin" value="0" scope="request"/>
        </c:if>
    </c:when>
    <c:otherwise>
        <c:set var="begin" value="0" scope="request"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${jcr:isNodeType(currentNode, 'jmix:orderedList')}">
        <jcr:jqom var="sortedChildren">
            <query:selector nodeTypeName="nt:base"/>
            <query:childNode path="${currentNode.realNode.path}"/>
            <c:forTokens var="prefix" items="first,second,third" delims=",">
                <jcr:nodeProperty node="${currentNode}" name="${prefix}Field" var="sortPropertyName"/>
                <c:if test="${!empty sortPropertyName}">
                    <jcr:nodeProperty node="${currentNode}" name="${prefix}Direction" var="order"/>
                    <query:sortBy propertyName="${sortPropertyName.string}" order="${order.string}"/>
                </c:if>
            </c:forTokens>
        </jcr:jqom>
        <c:set value="${sortedChildren.nodes}" var="currentList" scope="request"/>
        <c:set var="end" value="${fn:length(sortedChildren.nodes)}" scope="request"/>
        <c:set var="totalSize" value="${end}" scope="request"/>
    </c:when>
    <c:otherwise>
        <c:set value="${currentNode.nodes}" var="currentList" scope="request"/>
        <c:set var="end" value="${fn:length(currentNode.nodes)}" scope="request"/>
        <c:set var="totalSize" value="${end}" scope="request"/>
    </c:otherwise>
</c:choose>
