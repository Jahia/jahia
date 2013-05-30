<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="jahia" uri="http://www.jahia.org/tags/templateLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="j:alternateText" var="title"/>
<c:set var="node" value="${reference.node}"/>
<c:if test="${not empty node}">
    <jahia:addCacheDependency node="${node}" />
    <c:url var="url" value="${node.url}" context="/"/>
    <c:set var="height" value=""/>
    <c:set var="width" value=""/>
    <c:if test="${not empty node.properties['j:height']}">
        <c:set var="height">height="${node.properties['j:height'].string}"</c:set>
    </c:if>
    <c:if test="${not empty node.properties['j:width']}">
        <c:set var="width">width="${node.properties['j:width'].string}"</c:set>
    </c:if>
    <img src="${url}" alt="${fn:escapeXml(not empty title.string ? title.string : currentNode.name)}" <c:out value="${height} ${width}" escapeXml="false"/> />
</c:if>
<c:if test="${empty node and renderContext.editMode}">
    Missing image
</c:if>
