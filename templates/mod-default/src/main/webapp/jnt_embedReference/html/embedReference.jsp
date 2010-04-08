<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:description" var="description"/>
<c:set var="node" value="${reference.node}"/>
<c:if test="${not empty node}">
<c:url var="url" value="${node.path}.html" context="${url.base}"/>
<c:url var="url" value="${jcr:isNodeType(node, 'nt:file') ? node.url : url}" context="/"/>
</c:if>
<c:if test="${not empty description.string}"><c:set var="linkTitle"> title="${fn:escapeXml(description.string)}"</c:set></c:if>
<img src="${url}" alt="${fn:escapeXml(not empty title.string ? title.string : currentNode.name)}" />