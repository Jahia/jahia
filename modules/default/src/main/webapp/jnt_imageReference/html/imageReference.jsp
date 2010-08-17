<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="j:alternateText" var="title"/>
<c:set var="node" value="${reference.node}"/>
<c:if test="${not empty node}">
<c:url var="url" value="${node.url}" context="/"/>
</c:if>
<img src="${url}" alt="${fn:escapeXml(not empty title.string ? title.string : currentNode.name)}" />