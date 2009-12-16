<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>

<div id="search-bar">
	<s:form class="active" method="get">
    	<div class="form-container">
			<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
			<c:if test="${not empty title.string}">
			<label>${fn:escapeXml(title.string)}:&nbsp;</label>
			</c:if>
			<fmt:message key='search.startSearching' var="startSearching"/>
        	<s:term value="${startSearching}" class="text-input"/>
            <input type="submit" value="<fmt:message key='search.submit'/>" title="<fmt:message key='search.submit'/>" class="submit"/>
        </div>
    </s:form>
</div>