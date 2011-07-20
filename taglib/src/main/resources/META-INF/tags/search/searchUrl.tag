<%@ tag body-content="empty" description="Renders the search URL built from current search request parameters."
%><%@ attribute name="exclude" required="false" type="java.lang.String"
              description="Comma-separated list of parameter names to exclude from the final search URL. None is excluded by default."
%><%@ attribute name="url" required="false" type="org.jahia.services.render.URLGenerator"
              description="Current URL Generator if available"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><c:set var="exclude" value=",${fn:replace(exclude, ' ', '')},"/><c:set var="urlBase" value="${pageContext.request.requestURI}"/><c:if test="${not empty url}"><c:set var="urlBase" value="${url.mainResource}"/></c:if><c:url value="${urlBase}" >
<c:forEach var="aParam" items="${paramValues}">
	<c:set var="paramToCheck" value=",${aParam.key},"/>
	<c:if test="${fn:startsWith(aParam.key, 'src_') && !fn:contains(exclude, paramToCheck)}">
		<c:forEach var="aValue" items="${aParam.value}">
			<c:param name="${aParam.key}" value="${aValue}"/>
		</c:forEach>
	</c:if>
</c:forEach>
</c:url>