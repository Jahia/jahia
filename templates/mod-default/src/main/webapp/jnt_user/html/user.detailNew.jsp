<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="960.css,userProfile.css"/>


<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:set var="person" value="${fields['j:title']} ${fields['j:firstName']} ${fields['j:lastName']}"/>

<c:set var="userProperties" property="propertyName" value="${fn:escapeXml(fields['j:function'])}"/>

<h3 class="boxtitleh3" id="personDisplay2"><c:out value="${person}"/></h3>

<div class="clear"></div>
<!-- twoCol clear -->
<ul class="list3 user-profile-list">
    <c:forTokens
            items="j:firstName,j:lastName,j:organization,j:function,j:phoneNumber,j:faxNumber,j:skypeID,j:twitterID,j:facebookID,j:linkedinID"
            delims="," var="key">
        <li>
            <span class="label"><fmt:message key='jnt_user.${fn:replace(key,":","_")}'/></span>

            <div class="edit" id="${fn:replace(key,":","_")}"><c:if test="${empty fields[key]}">Click here to edit</c:if><c:if test="${!empty fields[key]}">${fields[key]}</c:if></div>
            <c:set var="pubKey" value="${key}Public"/>
            <span class="visibilityEdit" id="${fn:replace(key,":","_")}Public">
            <c:if test="${fields[pubKey] eq 'true'}">Public</c:if>
            <c:if test="${fields[pubKey] eq 'false' or empty fields[pubKey]}">Private</c:if>
            </span>
        </li>
    </c:forTokens>
        <li>
            <span class="label"><fmt:message key="j_birthDate"/></span>
            <jcr:nodeProperty node="${currentNode}" name="j:birthDate" var="birthDate"/>
            <c:if test="${not empty birthDate}">
                <fmt:formatDate value="${birthDate.date.time}" pattern="dd/MM/yyyy" var="displayBirthDate"/>
            </c:if>
            <c:if test="${empty birthDate}">
                <jsp:useBean id="now" class="java.util.Date"/>
                <fmt:formatDate value="${now}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
            </c:if>
            <div class="dateEdit" id="j_birthDate">${displayBirthDate}</div>
        </li>        
        <li>
            <span class="label"><fmt:message key="j_title"/></span>
            <div class="edit" id="j_title">${fields['j:title']}</div>
            <span class="visibilityEdit j_titlePublicEdit" id="j_titlePublic">
            <c:if test="${fields['j:titlePublic'] eq 'true'}">
                Public
            </c:if>
            <c:if test="${fields['j:titlePublic'] eq 'false' or empty fields['j:titlePublic']}">
                Non Public
            </c:if>
            </span>
        </li>
</ul>
<div class="clear"></div>
<!-- twoCol clear -->


<div class="AddItemForm">
    <!--start AddItemForm -->
    <form method="post" action="#">
        <fieldset>
            <legend>AddItemForm</legend>
            <p class="field">

                <label for="label2">Label :</label>
                <input type="text" name="label2" id="label2" class="AddItemFormLabel" value="Label" tabindex="9"/>
                <span> : </span>
                <label for="value2">Value :</label>
                <input type="text" name="value2" id="value2" class="AddItemFormValue" value="Value" tabindex="10"/>

                <input class="png gobutton" type="image" src="img/more.png" alt="Sidentifier" tabindex="11"/>
            </p>

        </fieldset>
    </form>
</div>
<!--stop sendMailForm -->
<div class="divButton">
    <a class="aButton" href="#"><span>Sauvegarder</span></a>
    <a class="aButton" href="#"><span>Annuler</span></a>

    <div class="clear"></div>
</div>
