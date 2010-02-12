<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:statement" var="query"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:language" var="lang"/>
<jcr:nodeProperty node="${currentNode}" name="maxItems" var="maxItems"/>
<c:if test="${not empty title.string}">
	<h3>${fn:escapeXml(title.string)}</h3>
</c:if>
<c:if test="${renderContext.editMode}">
	<p>${fn:escapeXml(jcr:label(currentNode.primaryNodeType))} (${lang.string}):&nbsp;${fn:escapeXml(query.string)}</p>
</c:if>
<c:choose>
	<c:when test="${lang.string == 'JCR-SQL2'}">
		<jcr:sql var="result" sql="${query.string}" limit="${maxItems.long}"/>
		<c:set var="currentList" value="${result.nodes}" scope="request" />
	</c:when>
	<c:when test="${lang.string == 'xpath'}">
		<jcr:xpath var="result" xpath="${query.string}" limit="${maxItems.long}"/>
		<c:set var="currentList" value="${result.nodes}" scope="request" />
	</c:when>
	<c:otherwise>
		<utility:logger level="error" value="Unsupported query language encountered: ${lang}"/>
		<% request.setAttribute("currentList", java.util.Collections.EMPTY_LIST); %>
	</c:otherwise>
</c:choose>
<c:set var="editable" value="false" scope="request"/>
<c:if test="${renderContext.editMode && empty currentList}">
	<p><fmt:message key="search.results.no.results"/></p>
</c:if>