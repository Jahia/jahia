<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<c:choose>
<c:when test="${!empty title.string}">
    <c:set var="title" value="${title.string}"/>
</c:when>
<c:otherwise>
    <c:set var="title"><fmt:message key="backToPreviousPage"/></c:set>
</c:otherwise>
</c:choose>
<div class="backToParent">
   <c:if test="${!empty jcr:findDisplayableNode(renderContext.mainResource.node.parent, renderContext)}">
		<c:url value='${url.base}${jcr:findDisplayableNode(renderContext.mainResource.node.parent, renderContext).path}.html' var="action"/>
    </c:if>
    <c:if test="${empty jcr:findDisplayableNode(renderContext.mainResource.node.parent, renderContext)}">
        <c:set var="action">javascript:history.back()</c:set>
    </c:if>
    <a class="returnLink" href="${action}" title='<fmt:message key="backToPreviousPage"/>'>${title}</a>
</div>