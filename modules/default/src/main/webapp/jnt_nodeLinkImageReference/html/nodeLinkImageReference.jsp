<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="j:target" var="target"/>
<jcr:nodeProperty node="${currentNode}" name="j:linknode" var="linkreference"/>
<jcr:nodeProperty node="${currentNode}" name="j:alternateText" var="title"/>
<c:set var="node" value="${reference.node}"/>
<c:if test="${not empty node}">
<c:url var="url" value="${node.url}" context="/"/>
</c:if>
<c:if test="${not empty target.string}"><c:set var="target"> target="${target.string}"</c:set></c:if>
<c:set var="linknode" value="${linkreference.node}"/>
<c:if test="${not empty linknode}">
<c:url var="linkurl" value="${linknode.url}" context="/"/>
</c:if>
<a href="${linkurl.string}"${target}><img src="${url}" alt="${fn:escapeXml(not empty title.string ? title.string : currentNode.name)}" /></a>