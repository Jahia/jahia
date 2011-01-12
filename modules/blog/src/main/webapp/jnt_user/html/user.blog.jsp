<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:if test="${not empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
    <c:set var="person" value="${title.displayName} ${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and not empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:firstName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and empty fields['j:lastName']}">
    <c:set var="person" value=""/>
</c:if>
<jcr:nodeProperty node="${currentNode}" name="j:birthDate" var="birthDate"/>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="yyyy" var="birthYear"/>
    <fmt:formatDate value="${now}" pattern="yyyy" var="currentYear"/>
</c:if>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="dd/MM/yyyy" var="editBirthDate"/>
</c:if>
<fmt:formatDate value="${now}" pattern="dd/MM/yyyy" var="editNowDate"/>

<div class="aboutMeListItem"><!--start aboutMeListItem -->
    <h3><fmt:message key="jnt_blog.aboutMe"/></h3>

        <jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
    <div class="aboutMePhoto">
        <c:if test="${not empty picture}">
            <%--a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html"--%><img
                    src="${picture.node.thumbnailUrls['avatar_60']}"
                    alt="${userNode.properties.title.string} ${userNode.properties.firstname.string} ${userNode.properties.lastname.string}"
                    width="58"
                    height="58"/><%--/a--%>
        </c:if>
        <c:if test="${empty picture}"><%--a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html"--%><img alt=""
                                                                                                src="${pageContext.request.contextPath}/modules/default/css/img/userbig.png" width="58"
                    height="58"/></a>
        </c:if>
    </div>
    <div class="aboutMeBody"><!--start aboutMeBody -->
        <h5>${person}</h5>
        <c:if test="${not empty birthDate}">
        <p class="aboutMeAge"><fmt:message
                                    key="blog.profile.age"/>: <utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/> <fmt:message key="blog.profile.years"/></p>
            </c:if>
        <div class="clear"></div>

    </div>
    <!--stop aboutMeBody -->
    <p class="aboutMeResume">${fields['j:about']}</p>

    <div class="clear"></div>
</div>

