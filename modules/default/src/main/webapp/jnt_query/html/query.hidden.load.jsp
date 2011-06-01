<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:statement" var="query"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:language" var="lang"/>
<jcr:nodeProperty node="${currentNode}" name="maxItems" var="maxItems"/>
<c:choose>
	<c:when test="${lang.string == 'JCR-SQL2'}">
		<query:definition var="listQuery" statement="${query.string}" limit="${maxItems.long}" scope="request"/>
        <c:set target="${moduleMap}" property="listQuery" value="${listQuery}" />
	</c:when>
	<c:when test="${lang.string == 'xpath'}">
		<jcr:xpath var="result" xpath="${query.string}" limit="${maxItems.long}"/>
        <c:set target="${moduleMap}" property="currentList" value="${result.nodes}" />
        <c:set target="${moduleMap}" property="end" value="${functions:length(result.nodes)}" />
        <c:set target="${moduleMap}" property="listTotalSize" value="${moduleMap.end}" />
	</c:when>
	<c:otherwise>
		<utility:logger level="error" value="Unsupported query language encountered: ${lang}"/>
		<% request.setAttribute("currentList", java.util.Collections.EMPTY_LIST); %>
	</c:otherwise>
</c:choose>
<c:set target="${moduleMap}" property="editable" value="false" />

<c:set var="editable" value="false" scope="request"/>
