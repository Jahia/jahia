<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="j:url" var="url"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:description" var="description"/>
<jcr:nodeProperty node="${currentNode}" name="j:target" var="target"/>
<jcr:nodeProperty node="${currentNode}" name="protocol" var="protocol"/>

<c:if test="${not empty description.string}"><c:set var="linkTitle"> title="${fn:escapeXml(description.string)}"</c:set></c:if>
<c:if test="${not empty target.string}"><c:set var="target"> target="${target.string}"</c:set></c:if>

<c:choose>
    <c:when test="${(empty protocol.string) && !fn:startsWith(url.string, 'http')}">
      <c:set var="protocol">http://</c:set>
    </c:when>
    <c:otherwise>      
      <c:set var="protocol">${protocol.string}</c:set>
    </c:otherwise>
</c:choose>


<a href="${protocol}${url.string}" ${target} ${linkTitle}>${fn:escapeXml(not empty title.string ? title.string : currentNode.name)}</a>