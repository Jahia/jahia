<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<c:if test="${jcr:isNodeType(currentNode, 'mix:title')}">
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:description" var="description"/>
<c:if test="${not empty description.string}"><c:set var="linkTitle"> title="${fn:escapeXml(description.string)}"</c:set></c:if>
</c:if>
<c:if test="${not omitFormatting && not empty param.cssClass}"><c:set var="cssClass">${param.cssClass}</c:set></c:if>
<c:choose>
    <c:when test='${jcr:isNodeType(currentNode, "nt:file")}'>
        <c:url var="urlValue" value="${currentNode.url}" context="/"/>
    </c:when>
    <c:when test='${jcr:isNodeType(currentNode, "jmix:nodeReference")}'>
        <jcr:nodeProperty node="${currentNode.properties['j:node'].node}" name="jcr:title" var="title"/>
        <c:url var="urlValue" value="${url.base}${currentNode.properties['j:node'].node.path}.html"/>
    </c:when>
    <c:otherwise>
        <c:url var="urlValue" value="${url.base}${currentNode.path}.html"/>
    </c:otherwise>
</c:choose>
<c:if test="${not omitFormatting}"><span class="icon ${cssClass}"></span></c:if><a target="${param.target}" href="${urlValue}"${linkTitle}>${fn:escapeXml(not currentResource.moduleParams.useNodeNameAsTitle && not empty title.string ? title.string : currentNode.name)}</a>