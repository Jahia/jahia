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
<div style="border:1px solid #C9C9C9; padding:2px;">
    <div class="peoplePhoto">
        <jcr:node var="picture" path="${currentNode.path}/picture"/>
        <c:if test="${not empty picture.url}">
            <img src="${picture.url}" alt="${firstname} ${lastname}" width="60"/>
        </c:if>
        </br>
             <p style="font-face:arial; font-size:10px; font-color:#C9C9C9"><a href=mailto:${email}">${firstname}&nbsp;${lastname}&nbsp;(${currentNode.name})</a></p>
    </div>
</div>