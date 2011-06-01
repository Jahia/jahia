<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<%@ include file="../../getUser.jspf"%>

<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addCacheDependency node="${user}"/>
<c:set var="fields" value="${user.propertiesAsString}"/>
<jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle" var="title"/>
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
<jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="yyyy" var="birthYear"/>
    <fmt:formatDate value="${now}" pattern="yyyy" var="currentYear"/>
</c:if>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="dd/MM/yyyy" var="editBirthDate"/>
</c:if>
<fmt:formatDate value="${now}" pattern="dd/MM/yyyy" var="editNowDate"/>
<jcr:propertyInitializers node="${user}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${user}" name="j:title" var="titleInit"/>


<jcr:nodeProperty node="${user}" name="j:publicProperties" var="publicProperties" />
    <c:set var="publicPropertiesAsString" value=""/>

    <c:forEach items="${publicProperties}" var="value">
        <c:set var="publicPropertiesAsString" value="${value.string} ${publicPropertiesAsString}"/>
    </c:forEach>

    <form action="<c:url value='${url.basePreview}${user.path}'/>" method="post" id="updateVisibility">
        <ul class="listvisibility">
    <c:forTokens
            items="j:firstName,j:lastName,j:gender,j:title,j:birthDate,age,j:organization,j:function,j:about,j:email,j:skypeID,j:twitterID,j:facebookID,j:linkedinID,j:picture,preferredLanguage"
            delims="," var="key">
        <c:if test="${currentNode.properties[key].boolean}">
            <li><input onchange="$('#updateVisibility').ajaxSubmit();" type="checkbox" name="j:publicProperties" value="${key}" ${fn:contains(publicPropertiesAsString, key) ? 'checked' : ''} /> <fmt:message key="jnt_user.${fn:replace(key, ':','_')}"/></li>
        </c:if>
    </c:forTokens>
        <input type="hidden" name="jcrMethodToCall" value="put" />
            </ul>
    </form>
