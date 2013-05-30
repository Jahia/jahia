<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="jahia" uri="http://www.jahia.org/tags/templateLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="j:target" var="target"/>
<jcr:nodeProperty node="${currentNode}" name="j:linknode" var="linkreference"/>
<jcr:nodeProperty node="${currentNode}" name="j:alternateText" var="title"/>
<c:set var="node" value="${reference.node}"/>
<jahia:addCacheDependency node="${node}" />
<c:if test="${not empty node}">
    <c:url var="urlNode" value="${node.url}" context="/"/>
</c:if>
<c:if test="${not empty target.string}"><c:set var="target"> target="${target.string}"</c:set></c:if>
<c:set var="linknode" value="${linkreference.node}"/>
<c:if test="${not empty linknode}">
    <c:url var="linkurl" value="${url.base}${linknode.path}.html"/>
</c:if>
<a href="${linkurl}" ${target}><img src="${urlNode}" alt="${fn:escapeXml(not empty title.string ? title.string : currentNode.name)}" /></a>