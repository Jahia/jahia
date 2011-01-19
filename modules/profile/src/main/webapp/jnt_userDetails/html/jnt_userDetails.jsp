<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:if test="${jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="/users/${user.properties['jcr:createdBy'].string}"/>
</c:if>

<template:addResources type="css" resources="userProfile.css"/>


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

<c:set var="userProperties" property="propertyName" value="${fn:escapeXml(fields['j:function'])}"/>

<h3 class="boxtitleh3" id="personDisplay2"><c:out value="${person}"/></h3>

<div class="clear"></div>
<!-- twoCol clear -->
<ul class="user-profile-list twoCol">
    <c:forTokens
            items="j:firstName,j:lastName,j:organization,j:function,j:skypeID,j:twitterID,j:facebookID,j:linkedinID"
            delims="," var="key">
        <li>
            <span class="label"><fmt:message key='jnt_user.${fn:replace(key,":","_")}'/></span>

            <div class="edit" id="${fn:replace(key,":","_")}"><c:if
                    test="${empty fields[key]}"><fmt:message key="label.clickToEdit"/></c:if><c:if
                    test="${!empty fields[key]}">${fields[key]}</c:if></div>
            <c:set var="pubKey" value="${key}Public"/>
            <span class="visibilityEdit" id="${fn:replace(key,":","_")}Public">
            <c:if test="${fields[pubKey] eq 'true'}"><fmt:message key="jnt_user.profile.public"/></c:if>
            <c:if test="${fields[pubKey] eq 'false' or empty fields[pubKey]}"><fmt:message
                    key="jnt_user.profile.nonpublic"/></c:if>
            </span>
        </li>
    </c:forTokens>
    <li>
        <span class="label"><fmt:message key="jnt_user.j_birthDate"/></span>
        <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
        <c:if test="${not empty birthDate}">
            <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
        </c:if>
        <c:if test="${empty birthDate}">
            <jsp:useBean id="now" class="java.util.Date"/>
            <fmt:formatDate value="${now}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
        </c:if>
        <div class="dateEdit" id="j_birthDate">${displayBirthDate}</div>
    </li>
    <li>
        <span class="label"><fmt:message key="jnt_user.j_title"/></span>

        <div class="titleEdit" id="j_title"><jcr:nodePropertyRenderer node="${user}" name="j:title"
                                                                      renderer="resourceBundle"/></div>
            <span class="visibilityEdit j_titlePublicEdit" id="j_titlePublic">
            <c:if test="${fields['j:titlePublic'] eq 'true'}">
                <fmt:message key="jnt_user.profile.public"/>
            </c:if>
            <c:if test="${fields['j:titlePublic'] eq 'false' or empty fields['j:titlePublic']}">
                <fmt:message key="jnt_user.profile.nonpublic"/>
            </c:if>
            </span>
    </li>
</ul>
