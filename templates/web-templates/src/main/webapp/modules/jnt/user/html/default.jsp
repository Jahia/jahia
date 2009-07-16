<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<h2>User : ${currentNode.name}</h2>
<ul>
<c:forEach items="${currentNode.properties}" var="property">
    <c:if test="${property.definition.jahiaContentItem}">
        <li>${property.name} ${property.string}</li>
    </c:if>
</c:forEach>
</ul>
<jcr:node var="picture" path="${currentNode.path}/picture"/>
<img src="${picture.url}" alt=""/>

<jcr:xpath var="result" xpath="//element(*, jnt:page)[@jcr:createdBy='${currentNode.name}']" />


User pages :

<c:if test="${result.size == 0}">
    No results.
</c:if>
<ul>
<c:forEach items="${result}" var="page">
        <jcr:nodeProperty node="${page}" name="jcr:title" var="title"/>
        <li>
        <a href="${baseUrl}${page.path}.html">${title.string}</a>
        </li>

</c:forEach>
</ul>
