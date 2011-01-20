<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:if test="${renderContext.editMode}">
    <fmt:message key="${fn:replace(currentNode.primaryNodeTypeName,':','_')}"/>
    <template:linker property="j:bindedComponent"/>
</c:if>

<%--<c:if test="${not jcr:isNodeType(user, 'jnt:user')}">--%>
<%--<jcr:node var="user" path="/users/${user.properties['jcr:createdBy'].string}"/>--%>
<%--</c:if>--%>
<c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="/users/${renderContext.user.username}"/>
</c:if>

<template:addResources type="css" resources="userProfile.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery-ui.datepicker.min.js,jquery.jeditable.datepicker.js"/>

<template:addResources type="javascript" resources="datepicker.js,timepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="i18n/jquery.ui.datepicker-${renderContext.mainResourceLocale.language}.js"/>

<template:addResources type="css" resources="timepicker.css"/>
<template:addResources type="javascript" resources="jquery.jeditable.treeItemSelector.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/adapters/jquery.js"/>

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

<script>
    $(document).ready(function() {
        initEditFields("${currentNode.identifier}");
    });
</script>

<ul class="user-profile-list">
    <c:if test="${currentNode.properties['j:firstName'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_firstName'/></span>

            <span jcr:id="j:firstName" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_firstName"
                  jcr:url="${url.base}${user.path}"><c:if test="${empty fields['j:firstName']}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:firstName']}">${fields['j:firstName']}</c:if></span>
        </li>
    </c:if>
    <c:if test="${currentNode.properties['j:lastName'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_lastName'/></span>

            <span jcr:id="j:lastName" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_lastName"
                  jcr:url="${url.base}${user.path}"><c:if test="${empty fields['j:lastName']}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:lastName']}">${fields['j:lastName']}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:gender'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.profile.sexe"/> : </span>

            <div jcr:id="j:gender" class="choicelistEdit${currentNode.identifier}"
                  jcr:url="${url.base}${user.path}"
                  jcr:options="{<c:forEach items="${genderInit}" varStatus="status" var="gender"><c:if test="${status.index > 0}">,</c:if>'${gender.value.string}':'${gender.displayName}'</c:forEach>}"><jcr:nodePropertyRenderer node="${user}" name="j:gender" renderer="resourceBundle"/></div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:title'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.j_title"/></span>

            <div jcr:id="j:title" class="choicelistEdit${currentNode.identifier}"
                  jcr:url="${url.base}${user.path}"
                  jcr:options="{<c:forEach items="${titleInit}" varStatus="status" var="title"><c:if test="${status.index > 0}">,</c:if>'${title.value.string}':'${title.displayName}'</c:forEach>}"><jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle"/></div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:birthDate'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.j_birthDate"/></span>
            <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
            <c:if test="${not empty birthDate}">
                <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
            </c:if>
            <c:if test="${empty birthDate}">
                <c:set var="displayBirthDate"><fmt:message key="label.clickToEdit"/></c:set>
            </c:if>
            <div jcr:id="j:birthDate" class="dateEdit${currentNode.identifier}"
                 id="dateEdit${currentNode.identifier}j_birthDate"
                 jcr:url="${url.base}${user.path}">${displayBirthDate}</div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:organization'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_organization'/></span>

            <span jcr:id="j:organization" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_organization"
                  jcr:url="${url.base}${user.path}"><c:if test="${empty fields['j:organization']}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:organization']}">${fields['j:organization']}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:function'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_function'/></span>

            <span jcr:id="j:function" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_function"
                  jcr:url="${url.base}${user.path}"><c:if test="${empty fields['j:function']}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:function']}">${fields['j:function']}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:about'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_about'/></span>

            <div jcr:id="j:about" class="ckeditorEdit${currentNode.identifier}"
                  id="ckeditorEdit${currentNode.identifier}j_about"
                  jcr:url="${url.base}${user.path}">${fields['j:about']}</div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:email'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.j_email"/> : </span>

            <span jcr:id="j:email" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_email"
                  jcr:url="${url.base}${user.path}"><c:if test="${empty fields['j:email']}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:email']}">${fields['j:email']}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:skypeID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_skypeID'/></span>

            <span jcr:id="j:skypeID" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_skypeID"
                  jcr:url="${url.base}${user.path}"><c:if test="${empty fields['j:skypeID']}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:skypeID']}">${fields['j:skypeID']}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:twitterID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_twitterID'/></span>

            <span jcr:id="j:twitterID" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_twitterID"
                  jcr:url="${url.base}${user.path}"><c:if test="${empty fields['j:twitterID']}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:twitterID']}">${fields['j:twitterID']}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:facebookID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_facebookID'/></span>

            <span jcr:id="j:facebookID" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_facebookID"
                  jcr:url="${url.base}${user.path}"><c:if test="${empty fields['j:facebookID']}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:facebookID']}">${fields['j:facebookID']}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:linkedinID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_linkedinID'/></span>

            <span jcr:id="j:linkedinID" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_linkedinID"
                  jcr:url="${url.base}${user.path}"><c:if test="${empty fields['j:linkedinID']}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:linkedinID']}">${fields['j:linkedinID']}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:picture'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_picture'/></span>

            <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>

            <c:if test="${not empty picture}">
                <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${person}"/>
            </c:if>

            <div class="file${currentNode.identifier}" jcr:id="j:picture"
                 jcr:url="${url.base}${user.path}">
                <span><fmt:message key="add.file"/></span>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:preferredLanguage'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.preferredLanguage"/></span>

            <div jcr:id="preferredLanguage" class="choicelistEdit${currentNode.identifier}"
                  jcr:url="${url.base}${user.path}"
                  jcr:options="{<c:forEach items='${functions:availableAdminBundleLocale(renderContext.mainResourceLocale)}' var="adLocale" varStatus="status"><c:if test="${status.index > 0}">,</c:if>'${adLocale}':'${adLocale}'</c:forEach>}"><jcr:nodePropertyRenderer node="${user}" name="preferredLanguage" renderer="resourceBundle"/></div>
        </li>

    </c:if>
</ul>

