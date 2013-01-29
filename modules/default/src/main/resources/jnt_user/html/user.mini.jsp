<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:forEach items="${currentNode.properties}" var="property">
    <c:if test="${property.name == 'j:firstName'}"><c:set var="firstname" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:lastName'}"><c:set var="lastname" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:email'}"><c:set var="email" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:title'}"><c:set var="title" value="${property.string}"/></c:if>
</c:forEach>

<jcr:node var="addresses" path="${currentNode.path}/j:addresses"/>
<c:forEach items="${addresses.nodes}" var="address" varStatus="status">
    <jcr:nodeProperty node="${address}" name="j:country" var="country"/>
</c:forEach>
<div>
    <div>
        <jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
        <c:if test="${not empty picture}">
            <div class='image'>
          <div class='itemImage itemImageLeft'><img class="userProfileImage" src="${picture.node.thumbnailUrls['avatar_120']}" alt="${fn:escapeXml(title)} ${fn:escapeXml(firstname)} ${fn:escapeXml(lastname)}" width="60"
                 height="60"/></div>
        </div><div class="clear"></div>
        </c:if>
        <p>
            <c:if test="${not empty country}">
            <img src="<c:url value='${url.base}/../../../css/images/flags/plain/flag_${fn:toLowerCase(country.string)}.png'/>"/>
        </c:if>
            <c:if test="${not empty email}">
            <a href=mailto:${fn:escapeXml(email)}>${fn:escapeXml(firstname)}&nbsp;${fn:escapeXml(lastname)}</a>
            </c:if>
            <c:if test="${empty email}">
            ${fn:escapeXml(firstname)}&nbsp;${fn:escapeXml(lastname)}
            </c:if>
            (${fn:escapeXml(currentNode.name)})</p>
    </div>
</div>
