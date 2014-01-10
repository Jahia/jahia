<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="jahia" uri="http://www.jahia.org/tags/templateLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="j:alternateText" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="j:url" var="linkurl"/>
<jcr:nodeProperty node="${currentNode}" name="j:target" var="target"/>
<c:if test="${not empty target.string}"><c:set var="target"> target="${target.string}"</c:set></c:if>
<c:set var="node" value="${reference.node}"/>
<c:if test="${not empty node}">
    <jahia:addCacheDependency node="${node}" />
<c:url var="url" value="${node.url}" context="/"/>
</c:if>
<a href="${linkurl.string}" ${target} ${linkTitle}><img src="${url}" alt="${fn:escapeXml(not empty title.string ? title.string : currentNode.name)}" /></a>