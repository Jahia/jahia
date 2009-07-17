<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:forEach items="${currentNode.properties}" var="property">
    <c:if test="${property.definition.jahiaContentItem}">
        <c:if test="${property.name == 'firstname'}"><c:set var="firstname"  value="${property.string}"/></c:if>
        <c:if test="${property.name == 'lastname'}"><c:set var="lastname"  value="${property.string}"/></c:if>
        <c:if test="${property.name == 'email'}"><c:set var="email"  value="${property.string}"/></c:if>
    </c:if>
</c:forEach>
<div class="peopleListItem">
    <div class="peoplePhoto">
        <jcr:node var="picture" path="${currentNode.path}/picture"/>
        <c:if test="${not empty picture.url}">
            <img src="${picture.url}" alt=" "/>
        </c:if>
    </div>
    <div class="peopleBody">
        <h5>${firstname}&nbsp;${lastname}&nbsp;(${currentNode.name})</h5>
        <p class="peopleEmail">${email}</p>
    </div>
    <div>
        <jcr:xpath var="result" xpath="//element(*, jnt:page)[@jcr:createdBy='${currentNode.name}']"/>
        User pages:
        <c:if test="${result.size == 0}">
            ${currentNode.name} has not created any pages so far 
        </c:if>
        <ul>
            <c:forEach items="${result}" var="page">
                <jcr:nodeProperty node="${page}" name="jcr:title" var="title"/>
                <li>
                    <a href="${baseUrl}${page.path}.html">${title.string}</a>
                </li>
            </c:forEach>
        </ul>
    </div>
</div>