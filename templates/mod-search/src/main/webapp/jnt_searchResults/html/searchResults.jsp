<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>

<c:if test="${renderContext.editMode}">
	<fieldset>
		<legend>${fn:escapeXml(jcr:label(currentNode.primaryNodeType))}</legend>
</c:if>
<s:results>
	<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
	<c:if test="${not empty title.string}">
		<h2>${fn:escapeXml(title.string)}</h2>
	</c:if>
	<div>
		<c:if test="${count > 0}">
    	<h4><fmt:message key="search.results.found"><fmt:param value="${count}"/></fmt:message></h4>
        <ul>
			<s:resultIterator>
				<li>
					<div><a href="${hit.link}">${fn:escapeXml(hit.title)}</a></div>
                    <div>${hit.excerpt}</div>
                    <div>${hit.contentType}</div>
                    <div><fmt:formatDate value="${hit.lastModified}" pattern="dd.MM.yyyy HH:mm"/></div>
				</li>
			</s:resultIterator>
        </ul>
		</c:if>
        <c:if test="${count == 0}">
        	<h4><fmt:message key="search.results.no.results"/></h4>
        </c:if>
    </div>
</s:results>
<c:if test="${renderContext.editMode}">
	</fieldset>
</c:if>
