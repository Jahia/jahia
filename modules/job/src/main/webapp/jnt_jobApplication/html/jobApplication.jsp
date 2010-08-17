<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="info" value="${currentNode.propertiesAsString}"/>
<div class="job-application">
	<c:if test="${not empty info.email}">
		<a href="mailto:${info.email}">${fn:escapeXml(info.firstname)}&nbsp;${fn:escapeXml(info.lastname)}</a>
	</c:if>
	<c:if test="${empty info.email}">
		${fn:escapeXml(info.firstname)}&nbsp;${fn:escapeXml(info.lastname)}
	</c:if>
	<div class="job-application-text">
		${fn:escapeXml(info.text)}
	</div>
</div>
