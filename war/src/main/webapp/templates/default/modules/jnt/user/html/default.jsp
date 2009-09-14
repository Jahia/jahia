<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:forEach items="${currentNode.properties}" var="property">
    <c:if test="${property.name == 'j:firstName'}"><c:set var="firstname" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:lastName'}"><c:set var="lastname" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:gender'}"><c:set var="gender" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:title'}"><c:set var="title" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:birthDate'}"><c:set var="birthDate" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:function'}"><c:set var="function" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:email'}"><c:set var="email" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:phoneNumber'}"><c:set var="phoneNumber" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:faxNumber'}"><c:set var="faxNumber" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:skypeID'}"><c:set var="skypeID" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:twitterID'}"><c:set var="twitterID" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:linkedinID'}"><c:set var="linkedinID" value="${property.string}"/></c:if>
    <c:if test="${property.name == 'j:about'}"><c:set var="about" value="${property.string}"/></c:if>
</c:forEach>
<div class="peopleListItem">
    <div class="peoplePhoto">
        <jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
        <c:if test="${not empty picture}">
            <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${title} ${firstname}&nbsp;${lastname} picture"/>
        </c:if>
    </div>
    <div class="peopleBody">
        <h5>${title} ${firstname}&nbsp;${lastname}&nbsp;(logged as : ${currentNode.name})</h5>
        
        <p class="peopleDetail"><span class="peopleLabel">Function :</span> ${function}</p>

        <p class="peopleDetail"><span class="peopleLabel">Phone :</span> ${phoneNumber}</p>

        <p class="peopleDetail"><span class="peopleLabel">Fax :</span> ${faxNumber}</p>

        <p class="peopleDetail"><span class="peopleLabel">Email :</span> <a href="mailto:${email}">${email}</a></p>

        <p class="peopleDetail"><span class="peopleLabel">Skype Name :</span> ${skypeID}</p>

        <p class="peopleDetail"><a href="http://twitter.com/${twitterID}"><span class="peopleLabel">Twitter</span></a></p>
        <p class="peopleDetail"><a href="http://www.linkedin.com/in/${linkedinID}"><span class="peopleLabel">LinkedIn</span></a></p>
    </div>
    <div class="peopleBody">
        <h5>About Me :</h5>

        <p class="peopleDetail">${about}</p>
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
                    <a href="${url.base}${page.path}.html">${title.string}</a>
                </li>
            </c:forEach>
        </ul>
    </div>
</div>