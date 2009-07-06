<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>

<jcr:nodeProperty node="${currentNode}" name="jcr:statement" var="query"/>
<h2>Saved search: ${currentNode.name}</h2>
<p>Query&nbsp;(<jcr:nodeProperty node="${currentNode}" name="jcr:language"/>): ${fn:escapeXml(query.string)}</p>
<jcr:xpath var="savedSearchIterator" xpath="${query.string}"/>
<c:if test="${savedSearchIterator.size == 0}">
    <p>No search results found</p>
</c:if>
<c:if test="${savedSearchIterator.size != 0}">
    <p>${savedSearchIterator.size}&nbsp;results found:</p>
    <c:set var="itemsPerPage" value="10"/>
    <pg:pager maxPageItems="${itemsPerPage}" url="${pageContext.request.contextPath}/render/default${currentNode.path}.html" export="currentPageNumber=pageNumber">
        <c:forEach var="aParam" items="${paramValues}">
            <c:if test="${not fn:startsWith(aParam.key, 'pager.')}"><pg:param name="${aParam.key}"/></c:if>
        </c:forEach>
        <ol start="${itemsPerPage * (currentPageNumber - 1) + 1}">
        <c:forEach items="${savedSearchIterator}" var="hit">
            <pg:item>
                <li><a href="${pageContext.request.contextPath}/render/default${hit.path}.html">${hit.name}</a></li>
            </pg:item>
        </c:forEach>
        </ol>
        <pg:index export="itemCount">
            <p>
            <pg:pages>
                <c:if test="${pageNumber != currentPageNumber}"><a href="${pageUrl}">${pageNumber}</a></c:if>
                <c:if test="${pageNumber == currentPageNumber}">${pageNumber}</c:if>
                &nbsp;
            </pg:pages>
            </p>
            <p>${itemsPerPage * (currentPageNumber - 1) + 1}&nbsp;-&nbsp;${itemsPerPage * currentPageNumber < itemCount ? itemsPerPage * currentPageNumber : itemCount}&nbsp;of&nbsp;${itemCount}</p>
        </pg:index>
        
    </pg:pager>
</c:if>