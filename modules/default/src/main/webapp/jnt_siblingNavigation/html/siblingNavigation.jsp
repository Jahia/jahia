<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<c:set var="mainNode" value="${renderContext.mainResource.node}"/>
<c:set var="nodeType" value="${mainNode.primaryNodeTypeName}"/>
<c:set var="parent" value="${mainNode.parent}"/>
<c:set var="sibblings" value="${jcr:getChildrenOfType(parent, nodeType)}"/>
<c:forEach items="${sibblings}" var="sibbling" varStatus="status">
    <c:if test="${mainNode == sibbling}">
        <c:if test="${not status.first}">
            <c:set var="previousNode" value="${sibblings[status.index - 1]}"/>
        </c:if>
        <c:if test="${not status.last}">
            <c:set var="nextNode" value="${sibblings[status.index + 1]}"/>
        </c:if>
    </c:if>
</c:forEach>
<c:set var="upperNode" value="${jcr:findDisplayableNode(parent, renderContext)}"/>
<c:if test="${not empty upperNode}">
    <a class="upperNode" href="<c:url value='${url.base}${upperNode.path}.html'/>"><span><fmt:message key="siblings.up"/></span></a>
</c:if>
<c:if test="${not empty previousNode}">
    <a class="previousNode" href="<c:url value='${url.base}${previousNode.path}.html'/>"><span><fmt:message key="siblings.previous"/></span></a>
</c:if>
<c:if test="${not empty nextNode}">
    <a class="nextNode" href="<c:url value='${url.base}${nextNode.path}.html'/>"><span><fmt:message key="siblings.next"/></span></a>
</c:if>
