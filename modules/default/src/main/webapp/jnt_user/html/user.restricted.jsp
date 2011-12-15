<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<jsp:useBean id="now" class="java.util.Date"/>

<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:if test="${currentResource.moduleParams.displayFirstName == 'true' and currentResource.moduleParams.displayLastName == 'true' and currentResource.moduleParams.displayTitle == 'true'}">
    <c:if test="${not empty fields['j:firstName'] or not empty fields['j:lastName'] or not empty fields['j:title']}">
        <c:set var="person" value="${title.displayName} ${fields['j:firstName']} ${fields['j:lastName']}"/>
    </c:if>
</c:if>
<c:if test="${currentResource.moduleParams.displayFirstName == 'true' and currentResource.moduleParams.displayLastName == 'true' and currentResource.moduleParams.displayTitle == 'false'}">
    <c:if test="${not empty fields['j:firstName'] or not empty fields['j:lastName']}">
        <c:set var="person" value="${fields['j:firstName']} ${fields['j:lastName']}"/>
    </c:if>
</c:if>
<c:if test="${currentResource.moduleParams.displayFirstName == 'false' and currentResource.moduleParams.displayLastName == 'true' and currentResource.moduleParams.displayTitle == 'false' and not empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:lastName']}"/>
</c:if>
<c:if test="${currentResource.moduleParams.displayFirstName == 'true' and currentResource.moduleParams.displayLastName == 'false' and currentResource.moduleParams.displayTitle == 'false' and not empty fields['j:firstName']}">
    <c:set var="person" value="${fields['j:firstName']}"/>
</c:if>
<c:if test="${currentResource.moduleParams.displayFirstName == 'false' and currentResource.moduleParams.displayLastName == 'false' and currentResource.moduleParams.displayTitle == 'false'}">
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
    <c:if test="${currentResource.moduleParams.displayPicture}">
        <c:if test="${not empty fields['j:picture']}">
            <div class="aboutMePhoto">
                <jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
                <c:if test="${not empty picture}">
                    <img src="${picture.node.thumbnailUrls['avatar_60']}" alt="${fn:escapeXml(person)}"/>
                </c:if>
            </div>
        </c:if>
    </c:if>
    <div class="aboutMeBody"><!--start aboutMeBody -->
        <h5><a href="<c:url value='${url.base}${currentNode.path}.html'/>">${fn:escapeXml(person)}</a></h5>
        <c:if test="${currentResource.moduleParams.displayBirthDate}">
            <c:if test="${not empty birthDate}">
                <p class="aboutMeAge"><fmt:message
                        key="jnt_user.age"/>: <utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}"
                                                                format="years"/> <fmt:message
                        key="jnt_user.profile.years"/></p>
            </c:if>
        </c:if>
        <c:if test="${currentResource.moduleParams.displayGender}">
            <c:if test="${not empty fields['j:gender']}">
                <p class="aboutMeGender"><fmt:message
                        key="jnt_user.j_gender"/>: ${fields['j:gender']}</p>
            </c:if>
        </c:if>
        <c:if test="${currentResource.moduleParams.displayEmail}">
            <c:if test="${not empty fields['j:email']}">
                <p class="aboutMeMail"><fmt:message
                        key="jnt_user.j_email"/>: ${fn:escapeXml(fields['j:email'])}</p>
            </c:if>
        </c:if>
        <div class="clear"></div>

    </div>
    <!--stop aboutMeBody -->
    <c:if test="${currentResource.moduleParams.displayAbout}">
        <c:if test="${not empty fields['j:about']}">
            <p class="aboutMeResume">${fields['j:about']}</p>
        </c:if>
    </c:if>

    <div class="clear"></div>
</div>

