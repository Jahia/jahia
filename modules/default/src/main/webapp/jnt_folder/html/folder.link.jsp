<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<c:url var="url" value="${url.base}${currentNode.path}.html"/>
<c:url var="url" value="${jcr:isNodeType(currentNode, 'nt:file') ? currentNode.url : url}" context="/"/>
<c:if test="${not empty currentNode.properties.description.string}"><c:set var="linkTitle"> title="${fn:escapeXml(currentNode.properties.description.string)}"</c:set></c:if>
<a href="${url}"${linkTitle}>${fn:escapeXml(not empty currentNode.properties.title.string ? currentNode.properties.title.string : currentNode.name)}</a>